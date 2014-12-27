package com.courysky.picturepicker.adapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.courysky.picturepicker.Cache;
import com.courysky.picturepicker.R;
import com.courysky.picturepicker.pojo.LocalImageInfo;
import com.courysky.ycommonutils.bitmap.AsyncImageDisplayManager;
import com.courysky.ycommonutils.bitmap.AsyncImageLoader;
import com.courysky.ycommonutils.ui.UIUtil;

public class LocalImageThumbAdapter<T extends AbsListView> extends BaseAdapter{
	private static final String TAG = LocalImageThumbAdapter.class.getSimpleName();
	
	private Context mContext;
	private LayoutInflater inflater;
	private ViewHolder holder;

	private AsyncImageDisplayManager asyncImageDisplayManager ;
	
	private int thumbImgWidth ;
	private int thumbImgHeight ;
//	private int degree = 0;

	private List<LocalImageInfo> imageInfoList;
	
	private T mParent;
	private OnCheckBoxClickListener mOnCheckBoxClickListener;

	/** 是否在快速滑动  */
	private boolean isFling = false;
	
	public boolean isFling() {
		return isFling;
	}

	public void setFling(boolean isFling) {
		this.isFling = isFling;
	}
	public OnCheckBoxClickListener getmOnCheckBoxClickListener() {
		return mOnCheckBoxClickListener;
	}

	public void setmOnCheckBoxClickListener(
			OnCheckBoxClickListener mOnCheckBoxClickListener) {
		this.mOnCheckBoxClickListener = mOnCheckBoxClickListener;
	}

	public LocalImageThumbAdapter(Context _context, List<LocalImageInfo> _imageList) {
		mContext = _context;
		imageInfoList = _imageList;
		inflater = LayoutInflater.from(mContext);
		
		asyncImageDisplayManager = new AsyncImageDisplayManager();
		String cacheDir = mContext.getCacheDir().getAbsolutePath();
		asyncImageDisplayManager.setLocalCacheDir(cacheDir);
		
		thumbImgWidth = (UIUtil.getScreenWidth((Activity)mContext)-6)/4;
        thumbImgHeight = thumbImgWidth;
	}

	public T getParent() {
		return mParent;
	}

	public void setParent(T parent) {
		this.mParent = parent;
	}
	
	public void setData(List<LocalImageInfo> _imageInfoList) {
		imageInfoList = _imageInfoList;
	}
	
	@Override
	public int getCount() {
		return imageInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return imageInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final LocalImageInfo imageInfo = imageInfoList.get(position);
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_local_image_thumb, null);
			holder.thumbImageView = (ImageView) convertView.findViewById(R.id.img_thumb);
			holder.chooseCheckBox = (CheckBox) convertView.findViewById(R.id.check_img_choose);
			holder.maskView = (ImageView) convertView.findViewById(R.id.view_mask);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (imageInfo.isChecked()) {
			holder.chooseCheckBox.setChecked(true);
			holder.maskView.setVisibility(View.VISIBLE);
		} else {
			holder.chooseCheckBox.setChecked(false);
			holder.maskView.setVisibility(View.INVISIBLE);
		}
		holder.chooseCheckBox.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
//		holder.chooseCheckBox.getLayoutParams().height = thumbImgHeight;
//        holder.chooseCheckBox.getLayoutParams().width = thumbImgWidth;
//		holder.thumbImageView.setImageDrawable(null);
		if (null != imageInfo.getThumbnailPath() && !imageInfo.getThumbnailPath().equals("")) {
			holder.thumbImageView.setTag(imageInfo.getThumbnailPath());
		} else {
			holder.thumbImageView.setTag(imageInfo.getPath());
		}
		
        holder.thumbImageView.getLayoutParams().height = thumbImgHeight;
        holder.thumbImageView.getLayoutParams().width = thumbImgWidth;
        if(AsyncImageLoader.getsImageCacheMap().containsKey(imageInfo.getThumbnailPath())){
        	Log.i(TAG, "contain bitmap key");
        	Bitmap bit = AsyncImageLoader.getsImageCacheMap().get(imageInfo.getThumbnailPath()).get();
        	if(null != bit && !bit.isRecycled()){
        		holder.thumbImageView.setImageBitmap(bit);
        	} else {
        		Log.i(TAG, "contain bitmap key, but recycled");
        		holder.thumbImageView.setImageResource(R.color.grey);
        	}
        } else {
        	 holder.thumbImageView.setImageResource(R.color.grey);
        }
        
        if (isFling) {
			return convertView;
		}

		holder.thumbImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(TAG, "--- onClick ---");
				CheckBox chooseCheckBox = (CheckBox) ((View)(v.getParent())).findViewById(R.id.check_img_choose);
				ImageView maskImageView = (ImageView) ((View)(v.getParent())).findViewById(R.id.view_mask);
				chooseCheckBox.setChecked(!chooseCheckBox.isChecked());
				if (chooseCheckBox.isChecked()) {
					/** 验证数量限制 */
					if (Cache.sChosenLocalImageInfoList.size()>=Cache.getsChooseImageLimitNum()) {
						chooseCheckBox.setChecked(false);
						Toast.makeText(mContext, "数量最多为"+Cache.getsChooseImageLimitNum()+"个！", Toast.LENGTH_LONG).show();
						return ;
					}
					maskImageView.setVisibility(View.VISIBLE);
					// XXX [yaojian] 
					if (!Cache.sChosenLocalImageInfoList.contains(imageInfo)) {
						Cache.sChosenLocalImageInfoList.add(imageInfo);
					}
				} else {
					maskImageView.setVisibility(View.INVISIBLE);
					// XXX [yaojian] 
					Cache.sChosenLocalImageInfoList.remove(imageInfo);
				}
				imageInfo.setChecked(chooseCheckBox.isChecked());
				if (null != mOnCheckBoxClickListener) {
					mOnCheckBoxClickListener.onCheckBoxClick(chooseCheckBox);
				}
			}
		});
        holder.chooseCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.v(TAG, "--- onCheckedChanged :"+isChecked);
			}
		});
        final int degree;
		ExifInterface exif;
		String orientation ="1";
		try {
			exif = new ExifInterface(imageInfo.getPath());
			orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int orientationInt = Integer.parseInt(orientation);
		switch (orientationInt) {
		case ExifInterface.ORIENTATION_NORMAL:
			degree = 0;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:{
			degree = 90;
		}
		break;
		case ExifInterface.ORIENTATION_ROTATE_180:{
			degree = 180;
		}
		break;
		case ExifInterface.ORIENTATION_ROTATE_270:{
			degree = 270;
		}
		break;
		default:{
			degree = 0;
		}
		break;
		}
		if (null != imageInfo.getThumbnailPath() && ! "".equals(imageInfo.getThumbnailPath())) {
			Log.i(TAG, "Thumbnail path exist :"+imageInfo.getThumbnailPath());
			asyncImageDisplayManager.displayImage(holder.thumbImageView,
					imageInfo.getThumbnailPath(), thumbImgWidth, thumbImgHeight,degree);
		} else {
			/** Thumbnail path 不存在，查询数据库获取 */
			Log.v(TAG, "Thumbnail path do not exist ");
			
			final Handler handler = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					String thumbPath = (String) msg.obj;
					final ImageView thumbImageView = (ImageView) mParent.findViewWithTag(imageInfo.getPath());
					/**
					 * 对以上代码进行分装
					 */
					if (null != thumbImageView) {
						asyncImageDisplayManager.displayImage(thumbImageView,
								thumbPath, thumbImgWidth, thumbImgHeight,degree);
					}
				}
			};
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Cursor thumbnailCursor = mContext.getContentResolver().query(
							MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
							null,
							MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
							new String[]{""+imageInfo.get_id()},
							null);
					if (thumbnailCursor.moveToFirst()) {
						String thumbnailPath = thumbnailCursor.getString(thumbnailCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
						if (null == thumbnailPath || "".equals(thumbnailPath)) {
							Log.e(TAG, "thumbnailPath not useful ,path:"+imageInfo.getPath());
						} else {
							File file = new File(thumbnailPath);
							if (file.exists()) {
								/** 缩略图文件存在 */
								imageInfo.setThumbnailPath(thumbnailPath);
							} else {
								/** 缩略图文件不存在，使用原始图片 */
								imageInfo.setThumbnailPath(imageInfo.getPath());
							}
						}
						
					} else {
						Log.e(TAG, "thumbnailPath not exist ,path:"+imageInfo.getPath());
						// TODO [yaojian]重新生成新的图片
						imageInfo.setThumbnailPath(imageInfo.getPath());
					}
//					String thumbPath = "/storage/sdcard0/ComeOnBaby/Cache/logo_s.png";
//					String thumbPath = "http://www.doupad.cn/templets/default/images/logo_s.png";
//					String thumbPath = "http://img3.douban.com/view/movie_poster_cover/spst/public/p2177928873.jpg";
//					imageInfo.setThumbnailPath(thumbPath);
					thumbnailCursor.close();
					Message msg = handler.obtainMessage(0,imageInfo.getThumbnailPath());
					handler.sendMessage(msg);
				}
			}).start();
		}
		
		return convertView;
	}
	
	private class ViewHolder{
		private ImageView thumbImageView;
		private CheckBox chooseCheckBox;
		private ImageView maskView;
	}
	
	public int getThumbImgWidth() {
		return thumbImgWidth;
	}

	public void setThumbImgWidth(int thumbImgWidth) {
		this.thumbImgWidth = thumbImgWidth;
	}

	public int getThumbImgHeight() {
		return thumbImgHeight;
	}

	public void setThumbImgHeight(int thumbImgHeight) {
		this.thumbImgHeight = thumbImgHeight;
	}
	
	public interface OnCheckBoxClickListener{
		public void onCheckBoxClick(CheckBox chooseCheckBox);
	}
}
