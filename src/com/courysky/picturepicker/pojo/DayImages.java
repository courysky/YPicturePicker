package com.courysky.picturepicker.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Administrator
 * 记录 属于同一天的 图片
 */
public class DayImages implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -800597782291110237L;

	/**
	 * 日期，单位天,从1970.1.1起始
	 * 主键
	 */
	private long timeDay;
	
	private boolean isAllChecked = false;

	private List<LocalImageInfo> imageInfoList = new ArrayList<LocalImageInfo>();
	
	public DayImages(long _timeDay) {
		timeDay = _timeDay;
	}

	public long getTimeDay() {
		return timeDay;
	}

	public void setTimeDay(long timeDay) {
		this.timeDay = timeDay;
	}
	
	
	public void addImageInfo(LocalImageInfo _imageInfo) {
		this.imageInfoList.add(_imageInfo);
	}

	public List<LocalImageInfo> getImageInfoList() {
		return imageInfoList;
	}

	public void setImageInfoList(List<LocalImageInfo> imageInfoList) {
		this.imageInfoList = imageInfoList;
	}
	
	public boolean isAllChecked() {
		return isAllChecked;
	}

	public void setAllChecked(boolean isAllChecked) {
		this.isAllChecked = isAllChecked;
	}

	@Override
	public boolean equals(Object o) {
		boolean isEqual = false;
		if (o == this) {
			return true;
		}
		if (o instanceof DayImages) {
			DayImages anotherDayImages = (DayImages)o;
			isEqual = (timeDay == anotherDayImages.timeDay);
		}
		return isEqual;
	}
	
}
