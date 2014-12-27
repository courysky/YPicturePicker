package com.courysky.picturepicker.view;

import com.courysky.picturepicker.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class NetLoadingDialog {
	
		
		private Dialog mDialog;

		private Context context;

		private NetLoadingDialog loadingDailog=null;

		private boolean isLDShow = false;

		
public NetLoadingDialog(Context context) {
	this.context=context;
}

//		public static NetLoadingDailog getDialog(Context context) {
//			if (loadingDailog == null) {
//				loadingDailog = new NetLoadingDailog(context);
//			}
//			return loadingDailog;
//		}
		

		public void loading() {
			try {
				if (isLDShow) {
					hideLoadingDialog();
				}
				createDialog();
//				mDialog.setCancelable(false);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.show();
				isLDShow = true;
			} catch (Exception e) {
				if (isLDShow && mDialog != null) {
					hideLoadingDialog();
				}
			}
		}
		

		/**
		 * 隐藏对话框
		 */
		private void hideLoadingDialog() {
			isLDShow = false;
			if (mDialog != null) {
				mDialog.dismiss();
			}
		}

		/**
		 * 创建对话框
		 */
		private void createDialog() {
			mDialog = null;
			mDialog = new Dialog(context,R.style.loading_dialog);
			View view = LayoutInflater.from(context).inflate(
					R.layout.progressbar_dialog, null);
			mDialog.setContentView(view);
		}

		public void dismissDialog() {
			hideLoadingDialog();
		}

	}


