package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.PageInfo;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse.ModelInfos;

public class GoodsListResponse extends BaseResponse
{
	public GoodsList datas;
	public PageInfo pageInfo;

	public static class GoodsList extends BaseData
	{
		public GoodsItem[] list;
	}

	public static class GoodsItem extends BaseData
	{
		public String id;
		public String title;
		public String ccate;
		public String sku1;
		public String sku2;
		public String skus;
		public ImageInfo thumb;
		public FileInfo model_info;// 模型文件
		public ModelInfos model_infos;// 对戒模型文件
		public String tag;// 标记，one单戒，two对戒
		public String tagname;// 分类标记
		public int count;
		public long createtime;// 创建时间
		public short isshop;// 是否门店可见

		public void copyFrom(GoodsDetailResponse.GoodsDetail detail)
		{
			this.id = detail.id;
			this.title = detail.title;
			this.thumb = detail.thumb;
			this.model_info = detail.model_info;
			this.tag = Define.TAG_RING;
		}

		public void copeFrom(CoupleRingDetailResponse.GoodsDetail detail)
		{
			this.id = detail.id;
			this.title = detail.title;
			this.thumb = detail.thumb;
			this.model_infos = detail.model_infos;
			this.tag = Define.TAG_COUPLE;
		}
	}
}
