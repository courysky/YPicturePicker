package com.courysky.picturepicker;

import java.util.Comparator;

import com.courysky.picturepicker.pojo.DayImages;

public class DayImagesComparator implements Comparator<DayImages>{
	private final String TAG = DayImagesComparator.class.getSimpleName();
	@Override
	public int compare(DayImages lhs, DayImages rhs) {
//		Log.v(TAG, "l day :"+lhs.getTimeDay() +" r day :"+rhs.getTimeDay());
		return (int) ( rhs.getTimeDay() - lhs.getTimeDay());
	}

}
