package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

import com.shiyou.tryapp2.data.ImageInfo;

public class ShopLogoAndADResponse extends BaseResponse
{
	public ShopLogoAndADList datas;

	public static class ShopLogoAndADList extends BaseData
	{
		public ShopLogoAndADInfo list;
	}

	public static class ShopLogoAndADInfo extends BaseData
	{
		public ImageInfo logo;
		public ImageInfo ads;
		public String goodsid;
		public int shopsee;
		public String tag;
		public String tagname;
		public String link;
		public ImageInfo[] screen;
	}
}
