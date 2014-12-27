package com.courysky.picturepicker.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.courysky.picturepicker.R;
import com.courysky.picturepicker.pojo.DayImages;
import com.courysky.picturepicker.pojo.ImageFloder;
import com.courysky.picturepicker.pojo.LocalImageInfo;
import com.courysky.ycommonutils.bitmap.AsyncImageDisplayManager;
import com.courysky.ycommonutils.ui.UIUtil;


public class FloderAdapter<T extends AbsListView> extends BaseAdapter implements SpinnerAdapter{//
	private static final String TAG = FloderAdapter.class.getSimpleName();
	
	private Context mContext;
	private LayoutInflater inflater;
	private ViewHolder holder;
//	private AsyncImageLoader asyncImageLoader ;
	private AsyncImageDisplayManager asyncImageDisplayManager;
	private int thumbImgWidth ;
	private int thumbImgHeight ;
	private int updateViewStartPos = -1;
	private int updateViewEndPos = 0;

	/** 是否在快速滑动  */
	boolean isFling = false;
	
	private List<ImageFloder> photoFloderList = new ArrayList<ImageFloder>();

	private T parentView;
	
	
	public void setParentView(T parentView) {
		this.parentView = parentView;
	}

	public FloderAdapter(Context context, List<ImageFloder> _photoFloderList) {
		mContext = context;
		photoFloderList = _photoFloderList;
		inflater = LayoutInflater.from(mContext);
//		asyncImageLoader = new AsyncImageLoader();
		asyncImageDisplayManager = new AsyncImageDisplayManager();
		
		thumbImgWidth = (UIUtil.getScreenWidth((Activity)mContext)-30)/3;
        thumbImgHeight = thumbImgWidth;
        updateViewEndPos= photoFloderList.size();
	}
	
	public void addPhoto(ImageFloder photo) {
		photoFloderList.add(photo);
	}
	
	public int getCount() {
		return photoFloderList.size();
	}

	public Object getItem(int position) {
		return photoFloderList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
		Log.v(TAG, "--- getView :"+position);
		isFling = false;
		/** 判断是否在快速滑动  */
		parentView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					isFling= true;
				}
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
		final ImageFloder imageFloder = photoFloderList.get(position);
		Log.i(TAG, "--- path is :"+imageFloder.getPath());
		if (null == imageFloder.getPath() || "".equals(imageFloder.getPath())) {
			Log.e(TAG, "--- path is NULL pos :"+position);
			
		}
		
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = (View) inflater.inflate(R.layout.item_image_floder, null);
			holder.coverImageView = (ImageView) convertView.findViewById(R.id.img_cover);
			holder.nameTextView = (TextView) convertView.findViewById(R.id.tlt_name);
			holder.numTextView = (TextView) convertView.findViewById(R.id.tlt_num);
			holder.chosenNumTextView = (TextView) convertView.findViewById(R.id.tlt_chosen_num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		if (imageFloder.getChosenNum()>0) {
			holder.chosenNumTextView.setText("已选: "+imageFloder.getChosenNum());
			holder.chosenNumTextView.setVisibility(View.VISIBLE);
		} else {

			holder.chosenNumTextView.setVisibility(View.GONE);
		}
		holder.nameTextView.setText(imageFloder.getDisplayName());
		int totalNum = 0;
		for (DayImages dayImages : imageFloder.getDayImagesList()) {
			totalNum += dayImages.getImageInfoList().size();
		}
		holder.numTextView.setText("("+totalNum+")");
		
		holder.coverImageView.getLayoutParams().height = thumbImgHeight;
		holder.coverImageView.getLayoutParams().width = thumbImgWidth;
		
//		holder.coverImageView.setTag(imageFloder.getPath());
		if (null != imageFloder.getCoverPath() && !imageFloder.getCoverPath().equals("")) {
			convertView.findViewById(R.id.img_cover).setTag(imageFloder.getCoverPath());
		} else {
			convertView.findViewById(R.id.img_cover).setTag(imageFloder.getPath());
		}
		
		if (isFling || position < updateViewStartPos|| position >= updateViewEndPos) {
			if (isFling) {
				holder.coverImageView.setImageResource(R.color.grey);
			}
			if (position >= parentView.getLastVisiblePosition()) {
				updateViewStartPos = -1;
				updateViewEndPos = photoFloderList.size();
			}
			return convertView;
		}
		holder.coverImageView.setImageResource(R.color.grey);
		if (null != imageFloder.getCoverPath() && !"".equals(imageFloder.getCoverPath())) {
			Log.v(TAG, "CoverPath exist :"+imageFloder.getCoverPath());
			asyncImageDisplayManager.displayImage(holder.coverImageView, imageFloder.getCoverPath(),
					thumbImgWidth, thumbImgHeight);
			
		} else {
			Log.v(TAG, "CoverPath do not exist ");
			
			final Handler handler = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					
					final String coverPath = (String) msg.obj;
					final ImageView coverImageView =
							(ImageView) parentView.findViewWithTag(imageFloder.getPath());
					if (null != coverImageView) {
						asyncImageDisplayManager.displayImage(coverImageView, coverPath,
								thumbImgWidth, thumbImgHeight);
					}
				}
			};
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					/*
					 * 获取指定的缩略图信息
					 */
					LocalImageInfo imageInfo = imageFloder.getDayImagesList().get(0).getImageInfoList().get(0);
					Cursor thumbnailCursor = mContext.getContentResolver().query(
							MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
							null,
							MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
							new String[]{""+imageInfo.get_id()},
							null);
					if (thumbnailCursor.moveToFirst()) {
						String thumbnailPath= thumbnailCursor.getString(thumbnailCursor
								.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
						imageInfo.setThumbnailPath(thumbnailPath);
						imageFloder.setCoverPath(thumbnailPath);
					} else {
						// TODO [yaojian]重新生成新的图片
						imageFloder.setCoverPath(imageInfo.getPath());
					}
					if (null == imageFloder.getCoverPath() || "".equals(imageFloder.getCoverPath())) {
						Log.e(TAG, "cover is not exist ! "+ imageInfo.getPath());
					}
					thumbnailCursor.close();
					Message msg = handler.obtainMessage(0, imageFloder.getCoverPath());
					handler.sendMessage(msg);
				}
			}).start();
		}
//		imageFloder.getCoverPath();
//		Bitmap bitmap = BitmapFactory.decodeFile(imageFloder.getCoverPath());
//		holder.coverImageView.setImageBitmap(bitmap);
		
//		imageView.setImageBitmap(photoFloderList.get(position).getCoverPath());
		if (position >= photoFloderList.size()-1) {
			updateViewStartPos = -1;
			updateViewEndPos = photoFloderList.size();
		}
		return convertView;
	}
	
	public void notifyDataSetChanged() {
		updateViewStartPos = -1;
		updateViewEndPos = photoFloderList.size();
		super.notifyDataSetChanged();
	}
	
	public void notifyDataSetChanged(int offset) {
		updateViewStartPos = offset;
		updateViewEndPos = photoFloderList.size();
		super.notifyDataSetChanged();
	}
	public void notifyDataSetChanged(int offset, int endPos) {
		updateViewStartPos = offset;
		updateViewEndPos = endPos;
		super.notifyDataSetChanged();
	}
	private class ViewHolder{
		ImageView coverImageView;
		TextView nameTextView;
		TextView numTextView;
		TextView chosenNumTextView;
	}
}
