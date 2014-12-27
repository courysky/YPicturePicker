package com.courysky.picturepicker.adapter;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.courysky.picturepicker.Cache;
import com.courysky.picturepicker.R;
import com.courysky.picturepicker.pojo.DayImages;
import com.courysky.picturepicker.pojo.LocalImageInfo;

public class DayImagesAdapter<T extends AbsListView> extends BaseAdapter implements SpinnerAdapter{
	private static final String TAG = DayImagesAdapter.class.getSimpleName();
	private Context mContext;
	private List<DayImages> mDayImagesList;
	private LayoutInflater mInflater;
	private ViewHolder holder;
	
	/** 是否在快速滑动  */
	private boolean isFling;
	private boolean isStartFling = false;
	private T parentView;
//	private boolean isAllChecked = false;
	
	private int chooseImageLimitNum = 9;

	public T getParentView() {
		return parentView;
	}

	public void setParentView(T parentView) {
		this.parentView = parentView;
	}

	public DayImagesAdapter(Context _context, List<DayImages> _dayImagesList ) {
		mContext = _context;
		mDayImagesList = _dayImagesList;
		mInflater = LayoutInflater.from(mContext);
		
	}
	
	public void setData(List<DayImages> _dayImagesList) {
		mDayImagesList = _dayImagesList;
	}
	public int getChooseImageLimitNum() {
		return chooseImageLimitNum;
	}

	public void setChooseImageLimitNum(int chooseImageLimitNum) {
		Cache.setsChooseImageLimitNum(chooseImageLimitNum);
		this.chooseImageLimitNum = chooseImageLimitNum;
	}
	
	@Override
	public int getCount() {
		return mDayImagesList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDayImagesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		isFling = false;
		/** 判断是否在快速滑动  */
		parentView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.v(TAG, "--- onScrollStateChanged : "+scrollState);
				if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					isStartFling = true;
				} else {
					isStartFling = false;
					isFling = false;
					notifyDataSetChanged();
				}
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.v(TAG, "--- onScroll  ");
				if (isStartFling) {
					isFling= true;
				} else {
					isFling = false;
				}
			}
		});
		Log.i(TAG, "isStartFling :"+isStartFling+" isFling :"+isFling);
		
		final DayImages dayImages = mDayImagesList.get(position);
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_day_images, null);
			holder.imageGridView = (GridView) convertView.findViewById(R.id.grid_images);
			holder.timeTextView = (TextView) convertView.findViewById(R.id.tlt_time);
			holder.selectAllTextView = (TextView) convertView.findViewById(R.id.tlt_select_all);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dayImages.getTimeDay()*24*60*60*1000);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//		Log.i(TAG, "day :"+year+" "+month+" "+day);
		holder.timeTextView.setText(""+year+"年"+month+"月"+day+"日");
		if (dayImages.isAllChecked()) {
			holder.selectAllTextView.setText("取消全选");
		} else {
			holder.selectAllTextView.setText("全选");
		}
		LocalImageThumbAdapter<GridView> localImageThumbAdapter ;
		if (null != holder.imageGridView.getAdapter()) {
			localImageThumbAdapter = (LocalImageThumbAdapter<GridView>) holder.imageGridView.getAdapter();
			localImageThumbAdapter.setData(dayImages.getImageInfoList());
			localImageThumbAdapter.setParent(holder.imageGridView);
			localImageThumbAdapter.notifyDataSetChanged();
		} else {
			localImageThumbAdapter = new LocalImageThumbAdapter<GridView>(mContext, dayImages.getImageInfoList());
			localImageThumbAdapter.setParent(holder.imageGridView);
			holder.imageGridView.setAdapter(localImageThumbAdapter);
		}
		localImageThumbAdapter.setFling(isFling);
		
		/**
		 * 设置 gridview 高度
		 */
		int numColumn = 4; //holder.imageGridView.getNumColumns();
		int numRow = dayImages.getImageInfoList().size() / numColumn ;
		int numRemainder = dayImages.getImageInfoList().size() % numColumn ;
		if (numRemainder > 0) {
			numRow ++;
		}
		
		int gridHeight = (localImageThumbAdapter.getThumbImgHeight()+2) * numRow;
		holder.imageGridView.getLayoutParams().height = gridHeight;
		
//		holder.imageGridView.getVerticalSpacing();
		
		holder.selectAllTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!dayImages.isAllChecked()) {
					/**
					 * 验证全选数量，选择总量不可多过chooseImageLimitNum 
					 * Cache.sChosenLocalImageInfoList.size()+当天未选
					 */
					
					if ((Cache.sChosenLocalImageInfoList.size()+dayImages.getImageInfoList().size())>chooseImageLimitNum) {
						Toast.makeText(mContext, " 选择总量不能超过"+chooseImageLimitNum+"!", Toast.LENGTH_LONG).show();
						return;
					}
				}
				for (LocalImageInfo localImageInfo : dayImages.getImageInfoList()) {
					localImageInfo.setChecked(!dayImages.isAllChecked());
				}
				dayImages.setAllChecked(!dayImages.isAllChecked());
				TextView selectTextView = (TextView) v;
				if (dayImages.isAllChecked()) {
					
					selectTextView.setText("取消全选");
					//XXX [yaojian] abstract
					for (LocalImageInfo _localImageInfo : dayImages.getImageInfoList()) {
						if (!Cache.sChosenLocalImageInfoList.contains(_localImageInfo)) {
							Cache.sChosenLocalImageInfoList.add(_localImageInfo);
						}
					}
				} else {
					selectTextView.setText("全选");
					//XXX [yaojian] abstract
					Cache.sChosenLocalImageInfoList.removeAll(dayImages.getImageInfoList());
				}
				View parentView = (View) v.getParent();
				GridView imagesGridView = (GridView) parentView.findViewById(R.id.grid_images);
				LocalImageThumbAdapter<GridView> adapter = (LocalImageThumbAdapter<GridView>) imagesGridView.getAdapter();
				adapter.notifyDataSetChanged();
//				if (isAllChecked) {
//					
//				} else {
//					
//				}
				
			}
		});
		
		localImageThumbAdapter.setmOnCheckBoxClickListener(new LocalImageThumbAdapter.OnCheckBoxClickListener() {
			@Override
			public void onCheckBoxClick(CheckBox chooseCheckBox) {
				View parentView = (View) chooseCheckBox.getParent().getParent().getParent();
				TextView selectAllTextView = (TextView) parentView.findViewById(R.id.tlt_select_all);
				if (chooseCheckBox.isChecked()) {
					dayImages.setAllChecked(false);//boolean isAllChecked
					for (LocalImageInfo localImageInfo : dayImages.getImageInfoList()) {
						dayImages.setAllChecked(localImageInfo.isChecked());
						if (!dayImages.isAllChecked()) {
							break;
						}
					}
					if (dayImages.isAllChecked()) {
						selectAllTextView.setText("取消全选");
					} else {
						selectAllTextView.setText("全选");
					}
				} else {
					dayImages.setAllChecked(false);
					selectAllTextView.setText("全选");
				}
				
			}
		});
		
		return convertView;
	}

	private class ViewHolder {
		private GridView imageGridView;
		private TextView timeTextView;
		private TextView selectAllTextView ;
	}
	
}
