package com.shiyou.tryapp2;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.extend.ErrorInfo;
import android.extend.loader.BaseJsonParser;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BasicHttpLoadParams;
import android.extend.loader.HttpFileUploadParams;
import android.extend.loader.HttpLoader.HttpLoadParams;
import android.extend.loader.Loader.CacheMode;
import android.extend.loader.Loader.LoadParams;
import android.extend.loader.UrlLoader;
import android.extend.util.LogUtil;
import android.extend.util.NetworkManager;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shiyou.tryapp2.data.response.BannerADListResponse;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.CheckVersionResponse;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse;
import com.shiyou.tryapp2.data.response.CoupleRingErpResponse;
import com.shiyou.tryapp2.data.response.GIADeliveryResponse;
import com.shiyou.tryapp2.data.response.GetTrainLinksResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsErpResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsTagsResponse;
import com.shiyou.tryapp2.data.response.LoginResponse;
import com.shiyou.tryapp2.data.response.ScreenSaversResponse;
import com.shiyou.tryapp2.data.response.ShopLogoAndADResponse;
import com.shiyou.tryapp2.data.response.ShoppingcartListResponse;
import com.shiyou.tryapp2.data.response.UploadFileResponse;

public class RequestManager
{
	public interface RequestCallback
	{
		void onRequestError(int requestCode, long taskId, ErrorInfo error);

		void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from);
	}

	public static long login(Context context, String userName, String password, String deviceId, String pushRegId,
			RequestCallback callback)
	{
		int requestCode = RequestCode.user_login;
		String url = Config.LoginUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(true);
		params.addRequestParam(new BasicNameValuePair("reqeustCode", "" + requestCode));
		params.addRequestParam(new BasicNameValuePair("uname", userName));
		params.addRequestParam(new BasicNameValuePair("pwd", password));
		params.addRequestParam(new BasicNameValuePair("IMEI", deviceId));
		params.addRequestParam(new BasicNameValuePair("deviceId", deviceId));
		params.addRequestParam(new BasicNameValuePair("pushRegId", pushRegId));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<LoginResponse>(context, requestCode, callback, LoginResponse.class),
				CacheMode.NO_CACHE);
	}

	/**
	 * 检查版本更新
	 * */
	public static long checkVersion(Context context, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.user_check_version;
		String url = Config.CheckVersionUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("reqeustCode", "" + requestCode));
		try
		{
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			params.addRequestParam(new BasicNameValuePair("versionCode", "" + pi.versionCode));
			params.addRequestParam(new BasicNameValuePair("versionName", pi.versionName));
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		params.addRequestParam(new BasicNameValuePair("channel", "" + Config.CHANNEL));
		params.addRequestParam(new BasicNameValuePair("platform", "" + Config.PLATFORM));
		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<CheckVersionResponse>(context, requestCode, callback, CheckVersionResponse.class),
				cacheMode);
	}

	public static long checkVersion(Context context, RequestCallback callback)
	{
		return checkVersion(context, callback, CacheMode.NO_CACHE);
	}

	/**
	 * 获取屏保
	 * */
	public static long loadScreenSavers(Context context, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.tag_brand_list;
		String url = Config.LoadScreenSaversUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<ScreenSaversResponse>(context, requestCode, callback, ScreenSaversResponse.class),
				cacheMode);
	}

	public static long loadScreenSavers(Context context, RequestCallback callback)
	{
		return loadScreenSavers(context, callback, CacheMode.PERFER_MEMORY_OR_NETWORK);
	}

	/**
	 * 获取首页横幅广告
	 * */
	public static long loadBannerADList(Context context, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_main_recommends;
		String url = Config.LoadBannerADListUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<BannerADListResponse>(context, requestCode, callback, BannerADListResponse.class),
				cacheMode);
	}

	public static long loadBannerADList(Context context, RequestCallback callback)
	{
		return loadBannerADList(context, callback, CacheMode.PERFER_MEMORY_OR_NETWORK);
	}

	/**
	 * 获取门店logo、广告、屏保等信息
	 * */
	public static long loadShopLogoAndAD(Context context, String userKey, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.store_info;
		String url = Config.LoadShopLogoAndADUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<ShopLogoAndADResponse>(context, requestCode, callback, ShopLogoAndADResponse.class),
				cacheMode);
	}

	public static long loadShopLogoAndAD(Context context, String userKey, RequestCallback callback)
	{
		return loadShopLogoAndAD(context, userKey, callback, CacheMode.PERFER_MEMORY_OR_NETWORK);
	}

	/**
	 * 获取商品分类
	 * */
	public static long loadGoodsCategorys(Context context, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_category;
		String url = Config.LoadGoodsCategorysUrl;

		BasicHttpLoadParams params = new BasicHttpLoadParams(false);

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<GoodsCategorysResponse>(context, requestCode, callback, GoodsCategorysResponse.class),
				cacheMode);
	}

	public static long loadGoodsCategorys(Context context, RequestCallback callback)
	{
		return loadGoodsCategorys(context, callback, CacheMode.PERFER_MEMORY_OR_NETWORK);
	}

	/**
	 * 获取商品标签
	 * */
	public static long loadGoodsTags(Context context, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_tags;
		String url = Config.LoadGoodsTagsUrl;

		BasicHttpLoadParams params = new BasicHttpLoadParams(false);

		return UrlLoader.getDefault()
				.startLoad(context, url, params,
						new MyJsonParser<GoodsTagsResponse>(context, requestCode, callback, GoodsTagsResponse.class),
						cacheMode);
	}

	public static long loadGoodsTags(Context context, RequestCallback callback)
	{
		return loadGoodsTags(context, callback, CacheMode.PERFER_MEMORY_OR_NETWORK);
	}

	/**
	 * 获取商品列表
	 * 
	 * @param isShop 门店可见
	 * @param ccate 商品分类id
	 * @param tagIds 标签id，可为null
	 * @param weightRange 钻石重量范围，单位ct，可为null
	 * @param priceRange 价格范围，单位元，可以null
	 * */
	public static long loadGoodsList(Context context, String userKey, boolean isShop, String ccate, String[] tagIds,
			float[] weightRange, int[] priceRange, int page, int pageSize, RequestCallback callback, CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_list;
		String url = Config.LoadGoodsListUrl;

		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		if (isShop)
			params.addRequestParam(new BasicNameValuePair("shopsee", "" + 1));
		if (!TextUtils.isEmpty(ccate))
			params.addRequestParam(new BasicNameValuePair("ccate", ccate));
		if (tagIds != null && tagIds.length > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < tagIds.length; i++)
			{
				if (i > 0)
					sb.append(',');
				sb.append(tagIds[i]);
			}
			params.addRequestParam(new BasicNameValuePair("tags", sb.toString()));
		}
		if (weightRange != null && weightRange.length > 1)
		{
			params.addRequestParam(new BasicNameValuePair("weight_f", weightRange[0] + "-" + weightRange[1]));
		}
		if (priceRange != null && priceRange.length > 1)
		{
			params.addRequestParam(new BasicNameValuePair("price", priceRange[0] + "-" + priceRange[1]));
		}
		params.addRequestParam(new BasicNameValuePair("page", "" + page));
		params.addRequestParam(new BasicNameValuePair("psize", "" + pageSize));

		return UrlLoader.getDefault()
				.startLoad(context, url, params,
						new MyJsonParser<GoodsListResponse>(context, requestCode, callback, GoodsListResponse.class),
						cacheMode);
	}

	public static long loadGoodsList(Context context, String userKey, boolean isShop, String ccate, String[] tagIds,
			float[] weightRange, int[] priceRange, RequestCallback callback, CacheMode cacheMode)
	{
		return loadGoodsList(context, userKey, isShop, ccate, tagIds, weightRange, priceRange, 1, Integer.MAX_VALUE,
				callback, cacheMode);
	}

	// public static long loadGoodsList(Context context, String userKey, boolean isShop, String ccate, String[] tagIds,
	// float[] weightRange, int[] priceRange, int page, int pageSize, RequestCallback callback)
	// {
	// return loadGoodsList(context, userKey, isShop, ccate, tagIds, weightRange, priceRange, page, pageSize,
	// callback, CacheMode.PERFER_FILECACHE);
	// }

	public static long loadGoodsList(Context context, String userKey, boolean isShop, String ccate, String[] tagIds,
			float[] weightRange, int[] priceRange, RequestCallback callback)
	{
		return loadGoodsList(context, userKey, isShop, ccate, tagIds, weightRange, priceRange, 1, Integer.MAX_VALUE,
				callback, CacheMode.PERFER_FILECACHE);
	}

	/**
	 * 获取商品列表
	 * */
	public static long loadGoodsList(Context context, String userKey, boolean isShop, int page, int pageSize,
			RequestCallback callback, CacheMode cacheMode)
	{
		return loadGoodsList(context, userKey, isShop, null, null, null, null, page, pageSize, callback, cacheMode);
	}

	/**
	 * 获取商品列表
	 * */
	public static long loadGoodsList(Context context, String userKey, boolean isShop, RequestCallback callback,
			CacheMode cacheMode)
	{
		return loadGoodsList(context, userKey, isShop, 1, Integer.MAX_VALUE, callback, cacheMode);
	}

	public static long loadGoodsList(Context context, String userKey, boolean isShop, RequestCallback callback)
	{
		return loadGoodsList(context, userKey, isShop, callback, CacheMode.PERFER_FILECACHE);
	}

	/**
	 * 获取商品列表
	 * 
	 * @param ccate 商品分类id
	 * */
	public static long loadGoodsList(Context context, String userKey, boolean isShop, String ccate,
			RequestCallback callback, CacheMode cacheMode)
	{
		return loadGoodsList(context, userKey, isShop, ccate, null, null, null, 1, Integer.MAX_VALUE, callback,
				cacheMode);
	}

	// public static long loadGoodsList(Context context, String userKey, boolean isShop, String ccate,
	// RequestCallback callback)
	// {
	// return loadGoodsList(context, userKey, isShop, ccate, callback, CacheMode.PERFER_FILECACHE);
	// }

	public static long searchGoodsList(Context context, String userKey, String keyword, RequestCallback callback)
	{
		int requestCode = RequestCode.search_result;
		String url = Config.SearchGoodsListUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		params.addRequestParam(new BasicNameValuePair("keyword", keyword));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<GoodsListResponse>(context, requestCode, callback, GoodsListResponse.class),
				CacheMode.NO_CACHE);
	}

	/**
	 * 获取商品详情
	 * */
	public static long loadGoodsDetail(Context context, String userKey, String goodsId, RequestCallback callback,
			CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_info;
		String url = Config.LoadGoodsDetailUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		params.addRequestParam(new BasicNameValuePair("id", goodsId));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<GoodsDetailResponse>(context, requestCode, callback, GoodsDetailResponse.class),
				cacheMode);
	}

	public static long loadGoodsDetail(Context context, String userKey, String goodsId, RequestCallback callback)
	{
		return loadGoodsDetail(context, userKey, goodsId, callback, CacheMode.PERFER_FILECACHE);
	}

	/**
	 * 获取对戒详情
	 * */
	public static long loadCoupleRingDetail(Context context, String userKey, String goodsId, RequestCallback callback,
			CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_info;
		String url = Config.LoadGoodsDetailUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		params.addRequestParam(new BasicNameValuePair("id", goodsId));

		return UrlLoader.getDefault().startLoad(
				context,
				url,
				params,
				new MyJsonParser<CoupleRingDetailResponse>(context, requestCode, callback,
						CoupleRingDetailResponse.class), cacheMode);
	}

	public static long loadCoupleRingDetail(Context context, String userKey, String goodsId, RequestCallback callback)
	{
		return loadCoupleRingDetail(context, userKey, goodsId, callback, CacheMode.PERFER_FILECACHE);
	}

	public static long loadGoodsErp(Context context, String userKey, String goodsId, RequestCallback callback,
			CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_info;
		String url = Config.LoadGoodsErpUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		params.addRequestParam(new BasicNameValuePair("id", goodsId));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<GoodsErpResponse>(context, requestCode, callback, GoodsErpResponse.class), cacheMode);
	}

	public static long loadGoodsErp(Context context, String userKey, String goodsId, RequestCallback callback)
	{
		return loadGoodsErp(context, userKey, goodsId, callback, CacheMode.PERFER_NETWORK);
	}

	public static long loadCoupleRingErp(Context context, String userKey, String goodsId, RequestCallback callback,
			CacheMode cacheMode)
	{
		int requestCode = RequestCode.product_info;
		String url = Config.LoadGoodsErpUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		params.addRequestParam(new BasicNameValuePair("id", goodsId));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<CoupleRingErpResponse>(context, requestCode, callback, CoupleRingErpResponse.class),
				cacheMode);
	}

	public static long loadCoupleRingErp(Context context, String userKey, String goodsId, RequestCallback callback)
	{
		return loadCoupleRingErp(context, userKey, goodsId, callback, CacheMode.PERFER_NETWORK);
	}

	/**
	 * 添加购物车
	 * 
	 * @param userKey 用户登录后key
	 * @param id 商品id
	 * @param erpid erpid
	 * @param size 戒指手寸
	 * */
	public static long appendShoppingcart(Context context, String userKey, String id, String[] erpids, int[] sizes,
			RequestCallback callback)
	{
		int requestCode = RequestCode.shoppingcart_add;
		String url = Config.AppendShoppingcartUrl;
		BasicHttpLoadParams params = new BasicHttpLoadParams(false);
		params.addRequestParam(new BasicNameValuePair("key", userKey));
		params.addRequestParam(new BasicNameValuePair("id", id));
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < erpids.length; i++)
		{
			if (i > 0)
				sb.append(',');
			// if (nums != null)
			sb.append(erpids[i]).append('|').append(sizes[i]);
			// else
			// sb.append(ids[i]).append('|').append(1);
		}
		params.addRequestParam(new BasicNameValuePair("erpsize", Uri.encode(sb.toString())));
		// params.addRequestParam(new BasicNameValuePair("erpid", erpid));
		// params.addRequestParam(new BasicNameValuePair("size", "" + size));

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<BaseResponse>(context, requestCode, callback, BaseResponse.class), CacheMode.NO_CACHE);
	}

	/**
	 * 添加GIA购物车
	 * 
	 * @param userKey 用户登录后key
	 * @param id 商品id
	 * @param erpid erpid
	 * @param size 戒指手寸
	 * */
	public static long appendGIAShoppingcart(Context context, String userKey, String id, String gia, String price,
			String sourcePrice, int size, String lettering, RequestCallback callback)
	{
		int requestCode = RequestCode.shoppingcart_add;
		String url = Config.AppendGIAShoppingcartUrl;

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("reqeustCode", "" + requestCode));
		pairs.add(new BasicNameValuePair("id", id));
		pairs.add(new BasicNameValuePair("key", userKey));
		pairs.add(new BasicNameValuePair("gia", gia));
		pairs.add(new BasicNameValuePair("price", price));
//		pairs.add(new BasicNameValuePair("changeprice", sourcePrice));
		pairs.add(new BasicNameValuePair("changeprice", price));
		pairs.add(new BasicNameValuePair("size", "" + size));
		pairs.add(new BasicNameValuePair("lettering", lettering));
		pairs.add(new BasicNameValuePair("IMEI", NetworkManager.getIMEI(context)));

		HttpLoadParams params = new BasicHttpLoadParams(true, pairs);

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<BaseResponse>(context, requestCode, callback, BaseResponse.class), CacheMode.NO_CACHE);
	}
	
	/**
	 * 添加GIA购物车
	 * 
	 * @param userKey 用户登录后key
	 * @param id 商品id
	 * @param erpid erpid
	 * @param size 戒指手寸
	 * */
	public static long appendGIAShoppingcart(Context context, String userKey, String id, String gia, String price,
			String sourcePrice, int size, String lettering, int customization, RequestCallback callback)
	{
		int requestCode = RequestCode.shoppingcart_add;
		String url = Config.AppendGIAShoppingcartUrl;

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("reqeustCode", "" + requestCode));
		pairs.add(new BasicNameValuePair("id", id));
		pairs.add(new BasicNameValuePair("key", userKey));
		pairs.add(new BasicNameValuePair("gia", gia));
		pairs.add(new BasicNameValuePair("price", price));
//		pairs.add(new BasicNameValuePair("changeprice", sourcePrice));
		pairs.add(new BasicNameValuePair("changeprice", price));
		pairs.add(new BasicNameValuePair("size", "" + size));
		pairs.add(new BasicNameValuePair("lettering", lettering));
		pairs.add(new BasicNameValuePair("customization", "" + customization));
		pairs.add(new BasicNameValuePair("IMEI", NetworkManager.getIMEI(context)));

		HttpLoadParams params = new BasicHttpLoadParams(true, pairs);

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<BaseResponse>(context, requestCode, callback, BaseResponse.class), CacheMode.NO_CACHE);
	}

	// /**
	// * 添加购物车 对戒
	// *
	// * @param userKey 用户登录后key
	// * @param id 商品id
	// * @param erpid 男戒erpid
	// * @param erpid2 女戒erpid
	// * @param size 男戒手寸
	// * @param size2 女戒手寸
	// * */
	// public static long appendShoppingcart(Context context, String userKey, String id, String erpid, String erpid2,
	// int size, int size2, RequestCallback callback)
	// {
	// int requestCode = RequestCode.shoppingcart_add;
	// String url = Config.AppendShoppingcartUrl;
	// BasicHttpLoadParams params = new BasicHttpLoadParams(false);
	// params.addRequestParam(new BasicNameValuePair("key", userKey));
	// params.addRequestParam(new BasicNameValuePair("id", id));
	// params.addRequestParam(new BasicNameValuePair("erpid", erpid));
	// params.addRequestParam(new BasicNameValuePair("erpid2", erpid2));
	// params.addRequestParam(new BasicNameValuePair("size", "" + size));
	// params.addRequestParam(new BasicNameValuePair("size2", "" + size2));
	//
	// return UrlLoader.getDefault().startLoad(context, url, params,
	// new MyJsonParser<BaseResponse>(context, requestCode, callback, BaseResponse.class), CacheMode.NO_CACHE);
	// }

	/**
	 * 读取购物车列表
	 * 
	 * @param userKey 用户登录后key
	 * */
	public static long loadShoppingcartList(Context context, String userKey, RequestCallback callback)
	{
		int requestCode = RequestCode.shoppingcart_list;
		String url = Config.LoadShoppingcartListUrl;

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("reqeustCode", "" + requestCode));
		pairs.add(new BasicNameValuePair("key", userKey));

		HttpLoadParams params = new BasicHttpLoadParams(false, pairs);

		return UrlLoader.getDefault().startLoad(
				context,
				url,
				params,
				new MyJsonParser<ShoppingcartListResponse>(context, requestCode, callback,
						ShoppingcartListResponse.class), CacheMode.NO_CACHE);
	}


	/**
	 * 获取GIA发货时间
	 * */
	public static long getGIADelivery(Context context, RequestCallback callback)
	{
		int requestCode = RequestCode.address_info;
		String url = Config.GetGIADeliveryUrl;

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("reqeustCode", "" + requestCode));

		HttpLoadParams params = new BasicHttpLoadParams(true, pairs);

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<GIADeliveryResponse>(context, requestCode, callback, GIADeliveryResponse.class),
				CacheMode.PERFER_NETWORK);
	}

	/**
	 * 获取培训资料
	 * */
	public static long getTrainLinks(Context context, String userKey, RequestCallback callback)
	{
		int requestCode = RequestCode.address_list;
		String url = Config.GetTrainLinksUrl;

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("reqeustCode", "" + requestCode));
		pairs.add(new BasicNameValuePair("key", userKey));

		HttpLoadParams params = new BasicHttpLoadParams(false, pairs);

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<GetTrainLinksResponse>(context, requestCode, callback, GetTrainLinksResponse.class),
				CacheMode.PERFER_NETWORK);
	}

	/** 上传分享图片 */
	public static long uploadShareImage(Context context, String imagePath, RequestCallback callback)
	{
		int requestCode = RequestCode.upload_share_image;
		String url = Config.UploadShareImageUrl;
		HttpFileUploadParams params = new HttpFileUploadParams(null, "share_img", imagePath);

		return UrlLoader.getDefault().startLoad(context, url, params,
				new MyJsonParser<UploadFileResponse>(context, requestCode, callback, UploadFileResponse.class), null);
	}

	public static class MyJsonParser<T extends BaseResponse> extends BaseJsonParser
	{
		private int mRequestCode;
		private RequestCallback mCallback;
		private Class<T> mClass;

		// private String mCacheKey;
		// private Map mCacheMap;

		// public MyJsonParser(Context context, int requestCode, RequestCallback callback, Class<T> classz,
		// String cacheKey, Map cacheMap)
		// {
		// super(context);
		// mRequestCode = requestCode;
		// mCallback = callback;
		// mClass = classz;
		// mCacheKey = cacheKey;
		// mCacheMap = cacheMap;
		// }

		public MyJsonParser(Context context, int requestCode, RequestCallback callback, Class<T> classz)
		{
			// this(context, requestCode, callback, classz, null, null);
			super(context);
			mRequestCode = requestCode;
			mCallback = callback;
			mClass = classz;
		}

		@Override
		public void onJsonParse(String json, String url, String cacheKey, LoadParams params, DataFrom from)
		{
			LogUtil.i(TAG, "json: " + json);
			GsonBuilder gb = new GsonBuilder();
			Gson gson = gb.create();
			T response = gson.fromJson(json, mClass);
			// if (response.resultCode == BaseResponse.RESULT_OK && mCacheMap != null)
			// {
			// if (!TextUtils.isEmpty(mCacheKey))
			// {
			// mCacheMap.put(mCacheKey, response);
			// }
			// }
			response.printData(TAG, 2);
			if (mCallback != null)
				mCallback.onRequestResult(mRequestCode, mTaskId, response, from);
		}

		@Override
		public void onError(String url, LoadParams params, ErrorInfo error)
		{
			if (mCallback != null)
				mCallback.onRequestError(mRequestCode, mTaskId, error);
		}
	}
}
