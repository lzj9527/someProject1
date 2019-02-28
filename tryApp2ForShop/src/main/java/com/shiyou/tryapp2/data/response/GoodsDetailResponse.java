package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.ImageInfo;

public class GoodsDetailResponse extends BaseResponse
{

//		@Override
//		public String toString() {
//			return "id="+id+"  title="+title+"   thumb_url="+thumb_url+"  gcate="+gcate+"  thumb_url="+thumb_url+"   customization="+customization+"   specailProcess= "+specialProcess;
//		}

		public int id;
		public String title;
//		public String goodssn;
//		public String sku;
		public String tagname;// 分类标记
		//		public ImageInfo thumb;// 主图
		public String[] thumb_url;// 相册
        public  ImageInfo[] thumb_url2;
//		public ParamItem[] param;// 商品属性
//		public ErpGoods[] erp;
		public FileInfo model_info;// 模型文件
		public int gcate;
		public int customization;	//定制
		public String specialProcess;	//特殊工艺对戒
		public int ccate;   //分类id

//	public GoodsDetailResponse(){
//		thumb_url2=new ImageInfo[thumb_url.length];
//		for(int i=0;i<thumb_url.length;i++){
//			thumb_url2[i].url=thumb_url[0];
//		}
//	}


		public String getTagname() {
			return tagname;
		}

		public void setTagname(String tagname) {
			this.tagname = tagname;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	public String[] getThumb_url() {
		return thumb_url;
	}

	public void setThumb_url(String[] thumb_url) {
		this.thumb_url = thumb_url;
	}

	public ImageInfo[] getThumb_url2() {
		return thumb_url2;
	}

	public void setThumb_url2(ImageInfo[] thumb_url2) {
		this.thumb_url2 = thumb_url2;
	}

	//		public ErpGoods[] getErp() {
//			return erp;
//		}
//
//		public void setErp(ErpGoods[] erp) {
//			this.erp = erp;
//		}



		public FileInfo getModel_info() {
			return model_info;
		}

		public void setModel_info(FileInfo model_info) {
			this.model_info = model_info;
		}


		public int getGcate() {
			return gcate;
		}

		public void setGcate(int gcate) {
			this.gcate = gcate;
		}

		public int getCustomization() {
			return customization;
		}

		public void setCustomization(int customization) {
			this.customization = customization;
		}

		public String getSpecialProcess() {
			return specialProcess;
		}

		public void setSpecialProcess(String specialProcess) {
			this.specialProcess = specialProcess;
		}

		public int getCcate() {
			return ccate;
		}

		public void setCcate(int ccate) {
			this.ccate = ccate;
		}



		public static String toJson(GoodsDetailResponse info)
		{

			GsonBuilder gb = new GsonBuilder();
			Gson gson = gb.create();
			return gson.toJson(info);
		}

		public static GoodsDetailResponse fromJson(String json)
		{
			GsonBuilder gb = new GsonBuilder();
			Gson gson = gb.create();
			return gson.fromJson(json, GoodsDetailResponse.class);
		}


	public static class ErpGoods extends BaseData
	{
		public String erpid;// 货号
		public String p1;// 金重
		public String p2;// 主石净度
		public String p3;// 主石颜色
		public String p4;// 材质
		public String C0115;// 戒托价格
		public String p5;// 价格
		public String p6;// 主石数量
		public String p7;// 主石重量
		public String p8;// 副石数量
		public String p9;// 副石重量
		public String p128;// 手寸
		public String zs;// 证书
	}

	}

//	public static class ParamItem extends BaseData
//	{
//		public String id;
//		public String title;
//		public String value;
//		public String value2;
//	}
//


