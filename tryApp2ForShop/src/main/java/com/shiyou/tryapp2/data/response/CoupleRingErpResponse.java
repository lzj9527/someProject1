package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

//import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse.ErpDetail;

public class CoupleRingErpResponse extends BaseResponse
{
	public ErpDatas datas;

	public static class ErpDatas extends BaseData
	{
//		public ErpDetail erp;
		public int count;
		public String tag;
	}
}
