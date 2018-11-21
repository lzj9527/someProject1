package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

import com.shiyou.tryapp2.data.ImageInfo;

public class ScreenSaversResponse extends BaseResponse
{
	public ScreenSaversList datas;

	public static class ScreenSaversList extends BaseData
	{
		public ImageInfo[] list;
	}
}
