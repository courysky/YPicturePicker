package com.courysky.picturepicker.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ImageFloder implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -13848640124644514L;
	
	private String path = null;
	private String displayName = null;
	private String coverPath = null;
	/**
	 * 已选择数量
	 */
	private int chosenNum = 0;

	private List<DayImages> dayImagesList = new ArrayList<DayImages>();
	
	public void addDayImages(DayImages _dayImages) {
		dayImagesList.add(_dayImages);
	}
	
	public List<DayImages> getDayImagesList() {
		return dayImagesList;
	}

	public void setDayImagesList(List<DayImages> dayImagesList) {
		this.dayImagesList = dayImagesList;
	}

	@Deprecated
	private List<LocalImageInfo> imageInfoList = new ArrayList<LocalImageInfo>();
	
	public ImageFloder(String _path) {
		path = _path;
	}
	
	public String getCoverPath() {
		return coverPath;
	}
	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	@Deprecated
	public List<LocalImageInfo> getImageInfoList() {
		return imageInfoList;
	}
	
	@Deprecated
	public void setImageInfoList(List<LocalImageInfo> imageInfoList) {
		this.imageInfoList = imageInfoList;
	}
	
	@Deprecated
	public void addImageInfo(LocalImageInfo imageInfo) {
		imageInfoList.add(imageInfo);
	}
	
	public int getChosenNum() {
		return chosenNum;
	}

	public void setChosenNum(int chosenNum) {
		this.chosenNum = chosenNum;
	}

	@Override
	public boolean equals(Object o) {
		boolean isEqual = false;
		if (o == this) {
			return true;
		}
		if (o instanceof ImageFloder) {
			ImageFloder anotherImageFloder = (ImageFloder)o;
			isEqual = path.equals(anotherImageFloder.path);
		}
		
		return isEqual;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
	
	
}
