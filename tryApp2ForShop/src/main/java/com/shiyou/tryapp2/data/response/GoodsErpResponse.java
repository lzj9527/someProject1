package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

import com.shiyou.tryapp2.data.response.GoodsDetailResponse.ErpGoods;

public class GoodsErpResponse extends BaseResponse
{
	public ErpDatas datas;

	public static class ErpDatas extends BaseData
	{
		public ErpGoods[] erp;
		public int count;
		public String tag;
	}
}
