package com.courysky.picturepicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.Activity;

import com.courysky.picturepicker.pojo.LocalImageInfo;

public class Cache {
	private static boolean isNeedUpdateGrowbookProgress = false;

	private static boolean isNeedUpdateClassActivityList = false;

	public static boolean isNeedUpdateClassActivityList() {
		return isNeedUpdateClassActivityList;
	}
	public static void setNeedUpdateClassActivityList(
			boolean isNeedUpdateClassActivityList) {
		Cache.isNeedUpdateClassActivityList = isNeedUpdateClassActivityList;
	}
	public static boolean isNeedUpdateGrowbookProgress() {
		return isNeedUpdateGrowbookProgress;
	}
	public static void setNeedUpdateGrowbookProgress(
			boolean isNeedUpdateGrowbookProgress) {
		Cache.isNeedUpdateGrowbookProgress = isNeedUpdateGrowbookProgress;
	}
	private static int sChooseImageLimitNum = 9;
	/**
	 * 已经选择的本地图片 的 列表
	 */
	public static List<LocalImageInfo> sChosenLocalImageInfoList = new ArrayList<LocalImageInfo>();
	/**
	 * 已经选择的学生相册中图片 的 列表
	 */
//	public static List<StudentPhotoInfo> sChosenStudentImageInfoList = new ArrayList<StudentPhotoInfo>();
	public static Stack<Activity> sActivityStack = new Stack<Activity>();
	/**
	 * 已经选择的网络相册中图片 的 列表
	 */
//	public static List<PhotoInfo> sChosenAlbumImageInfoList = new ArrayList<PhotoInfo>();
	/**
	 * 
	 */
//	public static DataJsonInfo sDataJsonInfo;
//	public static List<AlbumNameInte> sChosenAlbumNameInteList = new ArrayList<AlbumNameInte>();
	
	public static int getsChooseImageLimitNum() {
		return sChooseImageLimitNum;
	}
	public static void setsChooseImageLimitNum(int sChooseImageLimitNum) {
		Cache.sChooseImageLimitNum = sChooseImageLimitNum;
	}
}
