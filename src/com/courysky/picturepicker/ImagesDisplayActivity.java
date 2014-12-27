package com.courysky.picturepicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.courysky.picturepicker.adapter.DayImagesAdapter;
import com.courysky.picturepicker.adapter.FloderAdapter;
import com.courysky.picturepicker.adapter.MyspinnerAdapter;
import com.courysky.picturepicker.pojo.DayImages;
import com.courysky.picturepicker.pojo.ImageFloder;
import com.courysky.picturepicker.pojo.LocalImageInfo;
import com.courysky.picturepicker.view.NetLoadingDialog;

public class ImagesDisplayActivity extends Activity implements OnClickListener{
	private static final String TAG = ImagesDisplayActivity.class.getSimpleName();
	private static final int HANDLE_SHOW_PROGRESS_DIALOG = 21;
	private static final int HANDLE_DISMISS_PROGRESS_DIALOG = 22;
	
	private static final int HANDLE_DATA_INITIALIZED = 11;
//	public static final String INTENT_DATA_PICTURES = "intent_data_pictures";
	public static final String INTENT_DATA_IMG_NUM_LIMIT = "intent_data_img_num_limit";
	
	private ArrayList<ImageFloder> mImageFloderList = new ArrayList<ImageFloder>();

	private int mImageNumLimit = 9;
	private int selectionIndex = 0;
	private int mPopXPos;
	/**
	 * 当前打开的文件夹
	 */
	private ImageFloder mImageFloder;
	private List<DayImages> mDayImagesList = new ArrayList<DayImages>();

	private TextView mFloderNameTextView;
	private Spinner mSpinner;
	private Button mCancelButton, mOkButton;
	private ImageButton mBackButton;
	private ImageView mDownArrowImageView;
	private ListView mDayImagesListView;
	private DayImagesAdapter<ListView> mDayImagesAdapter;
	private FloderAdapter<ListView> mFloderAdapter;
	
	private PopupWindow popupWindow;
	private ListView mLocalFloderListView;
	private LinearLayout layout;
	private ListView listView;
	private NetLoadingDialog mDailog;
	
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "--- onCreate ---");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_images_display);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mHandler.sendEmptyMessage(HANDLE_SHOW_PROGRESS_DIALOG);
				initVar();
				mHandler.sendEmptyMessage(HANDLE_DISMISS_PROGRESS_DIALOG);
				mHandler.sendEmptyMessage(HANDLE_DATA_INITIALIZED);
			}
		}).start();
		initView();
	}

	private void initVar() {
		/**
		 * 查询外部图片文件
		 */
		Uri contentExternalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//INTERNAL_CONTENT_URI;//MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI
		addLocalImageInfos(contentExternalUri);
		/**
		 * 查询内部图片文件
		 */
		Uri contentInternalUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
		addLocalImageInfos(contentInternalUri);
		
		
		Intent inIntent = getIntent();
		mImageNumLimit = inIntent.getIntExtra(INTENT_DATA_IMG_NUM_LIMIT, 9);
//		int selectionIndex = 0;
//		index = Collections.binarySearch(mImageFloderList,
//				new ImageFloder(getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath()),
//				new ImageFloderComparator());
		
		String dcimPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
				+ File.separator+"Camera";
//		String dcimPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
		Log.d(TAG, "dcimPath :"+ dcimPath);
		selectionIndex = mImageFloderList.indexOf(new ImageFloder(dcimPath));
		if (selectionIndex < 0 || selectionIndex>=mImageFloderList.size()) {
			selectionIndex= 0;
		}
		if (mImageFloderList.size()==0) {
			mImageFloder = null;
//			finish();
			
		} else {
			
			mImageFloder = mImageFloderList.get(selectionIndex);//(ImageFloder) inIntent.getSerializableExtra(INTENT_DATA_PICTURES);
		}
		if (null != mImageFloder) {
			mDayImagesList = mImageFloder.getDayImagesList();
		}
		for (DayImages dayImages : mDayImagesList) {
			Log.i(TAG, "--time day :"+dayImages.getTimeDay());
			
			List<LocalImageInfo> imageInfosList = dayImages.getImageInfoList();
			for (LocalImageInfo imageInfo : imageInfosList) {
				Log.i(TAG, "----imageInfo :"+imageInfo.getPath()+" "+imageInfo.getTakenTime());
				
			}
		}
		Collections.sort(mDayImagesList, new DayImagesComparator());
	}
	
	private void initView() {
		mFloderNameTextView = (TextView) findViewById(R.id.tlt_floder_name);
		mCancelButton = (Button) findViewById(R.id.btn_cancel);
		mOkButton = (Button) findViewById(R.id.btn_next);
		mBackButton = (ImageButton) findViewById(R.id.btn_back);
		mDayImagesListView = (ListView) findViewById(R.id.list_day_images);
		mSpinner = (Spinner) findViewById(R.id.spinner_local_floder);
		mDownArrowImageView = (ImageView) findViewById(R.id.img_down_arrow);
		
		mFloderNameTextView.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		mOkButton.setOnClickListener(this);
		mBackButton.setOnClickListener(this);
		mDownArrowImageView.setOnClickListener(this);
	}
	
	private void bindView(){
		mDayImagesAdapter = new DayImagesAdapter<ListView>(ImagesDisplayActivity.this, mDayImagesList);
		mDayImagesAdapter.setChooseImageLimitNum(mImageNumLimit);
		mDayImagesAdapter.setParentView(mDayImagesListView);
		mDayImagesListView.setAdapter(mDayImagesAdapter);
		if (null != mImageFloder) {
			mFloderNameTextView.setText(mImageFloder.getDisplayName());
		} else {
			Toast.makeText(ImagesDisplayActivity.this, "未检索到本地图片!", Toast.LENGTH_LONG).show();
			finish();
		}
//		mFloderAdapter = new FloderAdapter<ListView>(getApplicationContext(), null);
//		ArrayAdapter adapter = ArrayAdapter.createFromResource(
//                this, R.array.planets, R.layout.spinner_textview);
//		(mactivity,R.layout.spinner_textview,mSpinnerList)
		
//		String[] s= getResources().getStringArray(R.array.planets);
//		ArrayList<String> sList = new ArrayList<String>();
//		for (int i = 0; i < 4; i++) {
//			sList.add(" "+i);
//		}
//		ArrayAdapter<ImageFloder> adapter = (ArrayAdapter<ImageFloder>) mSpinner.getAdapter();
//		if (null == adapter) {
//			adapter = new ArrayAdapter<ImageFloder>(
//					getApplicationContext(),
//					R.layout.item_spinner_image_floder, mImageFloderList);
//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			mSpinner.setAdapter(adapter);
//			mSpinner.setSelection(selectionIndex);
//			
//		} else {
//			
//		}
		
//		MyspinnerAdapter adapter = (MyspinnerAdapter) mSpinner.getAdapter();
//		if (null == adapter) {
//			adapter = new MyspinnerAdapter(getApplicationContext(), mImageFloderList);
//			mSpinner.setAdapter(adapter);
//			mSpinner.setSelection(selectionIndex);
//		} else {
//			
//		}
		
		
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:{
			setResult(RESULT_CANCELED);
			finish();
		}
			break;
		case R.id.btn_next:{
			
			if(Cache.sChosenLocalImageInfoList.size() == 0){
				Toast.makeText(ImagesDisplayActivity.this, "请选择图片!", Toast.LENGTH_SHORT).show();
				return;
			}
			int count = 0;
			for (DayImages dayImages : mImageFloder.getDayImagesList()) {
				for (LocalImageInfo localImageInfo : dayImages.getImageInfoList()) {
					if (localImageInfo.isChecked()) {
						count ++;
					} else {
						
					}
				}
			}
			mImageFloder.setChosenNum(count);
//			Intent intent = new Intent(ImagesDisplayActivity.this, SelectAlbumActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//			startActivity(intent);

			setResult(RESULT_OK);
			finish();
		}
			break;
		case R.id.btn_back:{
			setResult(RESULT_CANCELED);
			finish();
		}
			break;
		case R.id.tlt_floder_name:
		case R.id.img_down_arrow:{
			showWindow(mFloderNameTextView);
		}
		 	break;
		default:
			break;
		}
		
	}
	
	private Handler mHandler = new Handler () {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_DATA_INITIALIZED:{
				bindView();
			}
				break;
			case HANDLE_DISMISS_PROGRESS_DIALOG:{
				if(null != mDailog ){
					mDailog.dismissDialog();
				}
			}
				break;
			case HANDLE_SHOW_PROGRESS_DIALOG:
				if (null == mDailog) {
					mDailog = new NetLoadingDialog(ImagesDisplayActivity.this);
				}
				mDailog.loading();
				break;
				
			default:
				break;
			}
		}
		
	};
	
	
	/**
	 * 初始化rightPopupWindow
	 */
	private PopupWindow showWindow(View parent) {
		
		
		if (null == popupWindow) {
			LayoutInflater inflater = LayoutInflater.from(this);
			View popView = inflater.inflate(R.layout.dialog_spinner_local_floder, null);
			mLocalFloderListView = (ListView) popView.findViewById(R.id.listView_floder);
			MyspinnerAdapter adapter = new MyspinnerAdapter(getApplicationContext(), mImageFloderList);

			WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE); 
			mLocalFloderListView.setAdapter(adapter); // 创建一个PopuWidow对象  
			popupWindow = new PopupWindow(popView,  windowManager.getDefaultDisplay().getWidth() / 2, 420);
            
            popupWindow.setFocusable(true);  
            // 设置允许在外点击消失  
            popupWindow.setOutsideTouchable(true);  
      
            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景  
            popupWindow.setBackgroundDrawable(new BitmapDrawable());  
            
            
    		// 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半  
            mPopXPos = windowManager.getDefaultDisplay().getWidth() / 2  
                    - popupWindow.getWidth() / 2;
            Log.i("coder", "xPos:" + mPopXPos); 
            
            mLocalFloderListView.setOnItemClickListener(new OnItemClickListener() {  
            	  
                @Override  
                public void onItemClick(AdapterView<?> adapterView, View view,  
                        int position, long id) {  
      
//                    Toast.makeText(PoupWindowDemoActivity.this,  
//                            groups.get(position), 1000)  
//                            .show();  
      
                    if (popupWindow != null) {  
                    	ImageFloder newImageFloder = mImageFloderList.get(position);
                    	if (mImageFloder.equals(newImageFloder)) {
							
						} else {
							mImageFloder = newImageFloder;
	                    	mDayImagesList = mImageFloder.getDayImagesList();
	                    	Collections.sort(mDayImagesList, new DayImagesComparator());
	                    	mFloderNameTextView.setText(mImageFloder.getDisplayName());
	                        
	                        mDayImagesAdapter.setData(mDayImagesList);
	                        mDayImagesAdapter.notifyDataSetChanged();
						}
                    	popupWindow.dismiss();
                    }  
                }  
            }); 
		}
		 
		
        popupWindow.showAsDropDown(parent, mPopXPos, 0);  
        
       
		return popupWindow;
	};
	
	/**
	 * 创建PopupWindow
	 * 
	 * @param popView
	 *            指定PopupWindow的内容
	 */
	private PopupWindow createPopupWindow(View popView) {

		PopupWindow popupInstance = new PopupWindow(popView, -1, -1);
		popupInstance.setFocusable(true);
		popupInstance.setBackgroundDrawable(new ColorDrawable(0));
		popupInstance.setOutsideTouchable(true);
		// 监听器
		popupInstance.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				// ...
			}
		});

		return popupInstance;
	}
	
	private void addLocalImageInfos(Uri contentExternalUri) {
		Cursor imageExternalCursor = getContentResolver().query(contentExternalUri, null, null, null, null);
		if(null != imageExternalCursor){

			int count = imageExternalCursor.getColumnCount();
			Log.v(TAG, "count :"+count);
			while (imageExternalCursor.moveToNext()) {
//				for (int i = 0; i < count; i++) {
//					Log.v(TAG, "column("+i+") name :"+imageCursor.getColumnName(i)+" "+imageCursor.getString(i));
//				}
				String imagePath = imageExternalCursor.getString(imageExternalCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				int _id = imageExternalCursor.getInt(imageExternalCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
				long takenMilliSeconds = imageExternalCursor.getLong(imageExternalCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
				
				Log.i(TAG,"imagePath :"+ imagePath);
				/**
				 * 路径合法性验证
				 */
				if( null != imagePath && !"".equals(imagePath)){
					/** 过滤一部分路径 */
					if (imagePath.contains("digit")||imagePath.contains("djt")) {
						
						continue;
					}
					File imageFile = new File(imagePath);
					if (imageFile.exists() && imageFile.length()>=100*1024) {
						
					} else {
						continue;
					}
					LocalImageInfo imageInfo = new LocalImageInfo(imagePath);
					imageInfo.set_id(_id);
					imageInfo.setTakenTime(takenMilliSeconds);
					/*
					 * 获取缩略图信息
					 */
//					Cursor thumbnailCursor = getContentResolver().query(
//							MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//							null,
//							MediaStore.Images.Thumbnails.IMAGE_ID+" = ?",
//							new String[]{""+imageInfo.get_id()},
//							null);
//					if (thumbnailCursor.moveToFirst()) {
//						String thumbnailPath= thumbnailCursor.getString(thumbnailCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
//						imageInfo.setThumbnailPath(thumbnailPath);
//					}
//					thumbnailCursor.close();
					
					
					int pos = imagePath.lastIndexOf(File.separator);
					String floderPath = imagePath.substring(0, pos);
					int floderNamePos = floderPath.lastIndexOf(File.separator)+1;
					String floderDisplayName = floderPath.substring(floderNamePos);
					ImageFloder imageFloder = new ImageFloder(floderPath);
					
					if (mImageFloderList.contains(imageFloder)) {
						int imageFloderPos = mImageFloderList.indexOf(imageFloder);
						ImageFloder oldImageFloder = mImageFloderList.get(imageFloderPos);
						
						/**
						 * 根据拍摄日期将 图片按天归类
						 */
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(takenMilliSeconds);
//						int year = calendar.get(Calendar.YEAR);
//						int month = calendar.get(Calendar.MONTH);
//						int day = calendar.get(Calendar.DAY_OF_MONTH);
//						Log.i(TAG, "day :"+year+" "+month+" "+day);
						
						long timeDay = takenMilliSeconds/(1000*60*60*24);
						Log.i(TAG, "dayMilli :"+timeDay);
						
						DayImages dayImages = new DayImages(timeDay);
						List<DayImages> dayImagesList = oldImageFloder.getDayImagesList();
						/* 判断改天是否已经被添加过 */
						if (dayImagesList.contains(dayImages)) {
							/** 该天的图片容器已被创建，在已有的图片容器中添加图片信息 */
							int dayImagesPos = dayImagesList.indexOf(dayImages);
							DayImages oldDayImages = dayImagesList.get(dayImagesPos);
							oldDayImages.addImageInfo(imageInfo);
							
						} else {
							dayImages.addImageInfo(imageInfo);
							dayImagesList.add(dayImages);
						}
						
//						oldImageFloder.addImageInfo(imageInfo);
						
						if(Cache.sChosenLocalImageInfoList.contains(imageInfo)) {
							imageInfo.setChecked(true);
							int chosenNum = oldImageFloder.getChosenNum()+1;
							oldImageFloder.setChosenNum(chosenNum);
						}
						
					} else {
						
						if(Cache.sChosenLocalImageInfoList.contains(imageInfo)) {
							imageInfo.setChecked(true);
							int chosenNum = imageFloder.getChosenNum()+1;
							imageFloder.setChosenNum(chosenNum);
						}
						
						/**
						 * 根据拍摄日期将 图片按天归类
						 */
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(takenMilliSeconds);
//						int year = calendar.get(Calendar.YEAR);
//						int month = calendar.get(Calendar.MONTH);
//						int day = calendar.get(Calendar.DAY_OF_MONTH);
//						Log.i(TAG, "day :"+year+" "+month+" "+day);
						
						long timeDay = takenMilliSeconds/(1000*60*60*24);
						Log.i(TAG, "dayMilli :"+timeDay);
						
						DayImages dayImages = new DayImages(timeDay);
							dayImages.addImageInfo(imageInfo);
							imageFloder.getDayImagesList().add(dayImages);
						
						
						
						imageFloder.setDisplayName(floderDisplayName);
//						imageFloder.setCoverPath(imageInfo.getThumbnailPath());
//						imageFloder.addImageInfo(imageInfo);
						mImageFloderList.add(imageFloder);
					}
					
				} else {
					/** 路径无效 */
				}
				
			}
			imageExternalCursor.close();
		}
	}
	
}
