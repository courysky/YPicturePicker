package com.courysky.picturepicker.pojo;

import java.io.Serializable;

public class LocalImageInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8773528691765671028L;

	private int _id;

	private String path;
	
	/**
	 * 
	 */
	private String thumbnailPath;

	/**
	 * 拍摄时间,单位毫秒
	 */
	private long takenTime;
	
	private boolean isChecked = false;


	public long getTakenTime() {
		return takenTime;
	}

	public void setTakenTime(long takenTime) {
		this.takenTime = takenTime;
	}

	public LocalImageInfo(String _path) {
		path = _path;
	}
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getThumbnailPath() {
		return thumbnailPath;
	}

	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	@Override
	public boolean equals(Object o) {
		boolean isEqual = false;
		if (o == this) {
			return true;
		}
		if (o instanceof LocalImageInfo) {
			LocalImageInfo anotherLocalImageInfo = (LocalImageInfo)o;
			isEqual = (path.equals(anotherLocalImageInfo.path));
		}
		return isEqual;
	}
	
	
}
