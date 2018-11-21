package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

public class GoodsTagsResponse extends BaseResponse
{
	public TagList datas;

	public static class TagList extends BaseData
	{
		public TagItem[] list;
	}

	public static class TagItem extends BaseData
	{
		public String id;
		public String tagname;
	}
}
