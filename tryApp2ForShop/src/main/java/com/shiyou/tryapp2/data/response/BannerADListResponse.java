package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

import com.shiyou.tryapp2.data.ImageInfo;

public class BannerADListResponse extends BaseResponse
{
	public BannerADList datas;

	public static class BannerADList extends BaseData
	{
		public BannerADItem[] list;
	}

	public static class BannerADItem extends BaseData
	{
		public String id;
		public String uniacid;
		public String advname;
		public ImageInfo thumb;
		public String displayorder;
		public String enabled;
		public String goodsid;
		public int shopsee;
		public String tag;
		public String link;
	}
}
