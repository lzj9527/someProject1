package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

public class GIADeliveryResponse extends BaseResponse
{
	public SendTime datas;

	public static class SendTime extends BaseData
	{
		public String sendtime;
	}
}
