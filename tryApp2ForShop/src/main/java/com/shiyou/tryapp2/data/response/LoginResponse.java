package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

public class LoginResponse extends BaseResponse
{
	public static final int RESULT_UNBIND = -2;// 设备未绑定

	public LoginInfo datas;

	public static class LoginInfo extends BaseData
	{
		public String realname;
		public String key;
		public String xs_gia;
	}
}
