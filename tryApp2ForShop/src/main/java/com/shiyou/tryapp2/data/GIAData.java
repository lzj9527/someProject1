package com.shiyou.tryapp2.data;

import android.extend.data.BaseData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GIAData extends BaseData
{
	public String sn;// 货号
	public String style;// 形状
	public String weight;// 重量
	public String color;// 颜色
	public String clarity;// 净度
	public String cut;// 切工
	public String polish;// 抛光
	public String symmetry;// 对称
	public String fluorescence;// 荧光
	public String certtype;// 证书
	public String certno; // 证书号
	public String price;// 价格
	public String changeprice;// 原价
	public String careprice;// 戒托售价
	public String carechangeprice;// 戒托原价
	public String carematerial;// 戒托材质
	public String caresize;// 戒托手寸

	public static String toJson(GIAData info)
	{
		GsonBuilder gb = new GsonBuilder();
		Gson gson = gb.create();
		return gson.toJson(info);
	}

	public static GIAData fromJson(String json)
	{
		GsonBuilder gb = new GsonBuilder();
		Gson gson = gb.create();
		return gson.fromJson(json, GIAData.class);
	}
}
