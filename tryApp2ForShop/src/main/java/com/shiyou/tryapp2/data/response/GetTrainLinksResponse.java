package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

public class GetTrainLinksResponse extends BaseResponse
{
	public TrainList datas;

	public static class TrainList extends BaseData
	{
		public TrainItem[] list;
	}

	public static class TrainItem extends BaseData
	{
		public String id;
		public String title;
		public String link;
	}
}
