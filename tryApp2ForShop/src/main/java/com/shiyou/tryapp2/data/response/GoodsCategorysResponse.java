package com.shiyou.tryapp2.data.response;

import java.util.Date;

import android.extend.data.BaseData;

import com.shiyou.tryapp2.data.ImageInfo;

public class GoodsCategorysResponse extends BaseResponse
{
	public CategoryList datas;

	public CategoryItem[] findZuanShiCategoryList()
	{
		for (CategoryItem item : datas.list)
		{
			if (item.name.equals("钻饰"))
				return item.child;
		}
		return null;
	}

	public static class CategoryList extends BaseData
	{
		public CategoryItem[] list;
	}

	public static class CategoryItem extends BaseData
	{
		public String id;
		public String name;
		public ImageInfo thumb;
		public CategoryItem[] child;
		public String theNewTime;
		
		public long getTheNewTime(){
			
			if (theNewTime!=null){
				return Long.parseLong(theNewTime + "");
			}
			return new Date().getTime();
		}
	}
}
