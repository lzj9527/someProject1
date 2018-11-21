package com.shiyou.tryapp2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.extend.ErrorInfo;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.Loader.CacheMode;
import android.extend.util.AndroidUtils;
import android.extend.util.FileUtils;
import android.extend.util.LogUtil;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.shiyou.tryapp2.FileDownloadHelper.DownloadStatus;
import com.shiyou.tryapp2.FileDownloadHelper.OnFileDownloadCallback;
import com.shiyou.tryapp2.FileDownloadHelper.OnMultiFileDownloadCallback;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.response.BannerADListResponse;
import com.shiyou.tryapp2.data.response.BannerADListResponse.BannerADItem;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse.CategoryItem;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.ShopLogoAndADResponse;

public class ResourceHelper
{
	public static final String TAG = "ResourceHelper";

	public static File getResourceDirectory(Context context)
	{
		return FileUtils.getDirectory(context, "resource");
	}

	public static File getGoodsImageDirectory(Context context)
	{
		return FileUtils.getDirectory(context, "resource/image");
	}

	public static File getCombineImageDirectory(Context context)
	{
		return FileUtils.getDirectory(context, "resource/combine");
	}

	public static File getTempFileDirectory(Context context)
	{
		return FileUtils.getDirectory(context, "resource/temp_file");
	}

	public static File getTryModelDirectory(Context context)
	{
		return FileUtils.getDirectory(context, "resource/try_model");
	}

	public static void cleanResource(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.clear();
		editor.commit();

		FileUtils.deleteFilesInChildren(getResourceDirectory(context));
	}

	// private static ResourceHelper instance = null;
	//
	// public static ResourceHelper newInstance(Activity activity, String shopId)
	// {
	// instance = new ResourceHelper(activity, shopId);
	// return instance;
	// }
	//
	// public static ResourceHelper getInstance()
	// {
	// return instance;
	// }

	public interface OnResourceDownloadCallback
	{
		public void onDownloadFinished(Object data);
	}

	private static final CharSequence DATEFORMAT = "yyyyMMdd";
	private Context mContext;
	private Activity mActivity;
	private boolean mRunInBackground;
	// private String mShopId;
	private String mUserKey;
	private int mDownloadStep = 0;
	private ProgressDialog mProgressDialog;
	private OnResourceDownloadCallback mCallback;

	private static final String Pref_Name = "resouce_downloaded";

	public ResourceHelper(Context context, String userKey)
	{
		mContext = context;
		mActivity = null;
		// mShopId = context.getPackageName();
		mUserKey = userKey;
		mRunInBackground = true;
		mDownloadStep = 0;
	}

	public ResourceHelper(Activity activity, String userKey, boolean runInBackground)
	{
		mContext = activity;
		mActivity = activity;
		// mShopId = activity.getPackageName();
		mUserKey = userKey;
		mRunInBackground = runInBackground;
		mDownloadStep = 0;
	}

	private void showProgressDialog()
	{
		if (mActivity == null)
			return;
		mActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog == null)
				{
					mProgressDialog = new ProgressDialog(mActivity);
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					mProgressDialog.setCancelable(false);
					mProgressDialog.setCanceledOnTouchOutside(false);
					mProgressDialog.show();
					mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE, "取消", new OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// dialog.dismiss();
						}
					});
				}
			}
		});
	}

	private String mTitle;

	private void updateProgressDialogTitle(final String title)
	{
		mTitle = title;
		if (mActivity == null)
			return;
		mActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
					mProgressDialog.setTitle(title);
			}
		});
	}

	// private void updateProgressDialogMessage(final String message)
	// {
	// mActivity.runOnUiThread(new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// if (mProgressDialog != null)
	// mProgressDialog.setMessage(message);
	// }
	// });
	// }

	private void updateProgressDialog(final int progress, final int max)
	{
		if (mActivity == null)
			return;
		mActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
				{
					mProgressDialog.setMax(max);
					mProgressDialog.setProgress(progress);
					mProgressDialog.setMessage(mTitle + " " + progress + "/" + max);
				}
			}
		});
	}

	private void hideProgressDialog()
	{
		if (mActivity == null)
			return;
		mActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
				{
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
		});
	}

	private abstract class MyRequestCallback implements RequestCallback
	{
		@Override
		public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
		{
			if (response.resultCode != BaseResponse.RESULT_OK)
			{
				onResponseFail(response, null);
			}
			else
			{
				onResponseSuccess(response);
			}
		}

		@Override
		public void onRequestError(int requestCode, long taskId, ErrorInfo error)
		{
			onResponseFail(null, error);
		}

		abstract void onResponseSuccess(BaseResponse response);

		abstract void onResponseFail(BaseResponse response, ErrorInfo error);
	}

	private String getCurrentDate()
	{
		return DateFormat.format(DATEFORMAT, System.currentTimeMillis()).toString();
	}

	private synchronized boolean checkResourceDownloaded()
	{
		SharedPreferences pref = mContext.getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
		String value = pref.getString(mUserKey, "");
		// String date = getCurrentDate();
		// boolean result = date.equals(value);
		boolean result = false;
		if (!TextUtils.isEmpty(value))
			result = true;
		LogUtil.i(TAG, "checkResourceDownloaded: " + mUserKey + "; " + result);
		return result;
	}

	private synchronized void setResourceDownloaded()
	{
		SharedPreferences pref = mContext.getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putString(mUserKey, getCurrentDate());
		edit.commit();
	}

	public void checkAndDownloadResource(OnResourceDownloadCallback callback)
	{
		if (checkResourceDownloaded())
		{
			if (callback != null)
				callback.onDownloadFinished(TAG);
			return;
		}
		mCallback = callback;
		startDonwloadNextResource();
	}

	public void downloadResource(OnResourceDownloadCallback callback)
	{
		mCallback = callback;
		startDonwloadNextResource();
	}

	private void startDonwloadNextResource()
	{
		LogUtil.d(TAG, "startDonwloadNextResource: " + mDownloadStep);
		switch (mDownloadStep)
		{
			case 0:
				if (!mRunInBackground)
					showProgressDialog();
				mDownloadStep++;
				loadAllGoodsList(false);
				break;
			case 1:
				mDownloadStep++;
				loadGoodsCategory(false);
				break;
			case 2:
				mDownloadStep++;
				loadAllGoodsList(true);
				break;
			case 3:
				mDownloadStep++;
				loadGoodsCategory(true);
				break;
			case 4:
				mDownloadStep++;
				loadBannerADList();
				break;
			case 5:
				mDownloadStep++;
				loadShopLogoAndAD();
				break;
			case 6:
				mDownloadStep++;
				setResourceDownloaded();
				if (!mRunInBackground)
					hideProgressDialog();
				if (mCallback != null)
					mCallback.onDownloadFinished(TAG);
				break;
		}
	}

	// 获取商品列表
	private void loadAllGoodsList(boolean isShop)
	{
		if (!mRunInBackground)
		{
			if (isShop)
				updateProgressDialogTitle("下载门店商品");
			else
				updateProgressDialogTitle("下载商品");
		}
		RequestManager.loadGoodsList(mContext, mUserKey, isShop, new MyRequestCallback()
		{
			@Override
			void onResponseSuccess(BaseResponse response)
			{
				GoodsListResponse glResponse = (GoodsListResponse)response;
				if (glResponse.datas != null && glResponse.datas.list != null && glResponse.datas.list.length > 0)
				{
					AndroidUtils.MainHandler.post(new LoadGoodsListDetailRunnable(glResponse.datas.list));
				}
				else
					startDonwloadNextResource();
			}

			@Override
			void onResponseFail(BaseResponse response, ErrorInfo error)
			{
				startDonwloadNextResource();
			}
		}, CacheMode.PERFER_NETWORK);
	}

	// 获取商品列表详情
	private class LoadGoodsListDetailRunnable implements Runnable
	{
		int count = 0;
		GoodsListResponse.GoodsItem[] goodsList;

		// public LoadGoodsListDetailRunnable(List<GoodsListResponse.GoodsItem> list)
		// {
		// goodsList = new GoodsListResponse.GoodsItem[list.size()];
		// goodsList = list.toArray(goodsList);
		// }

		public LoadGoodsListDetailRunnable(GoodsListResponse.GoodsItem[] goodsList)
		{
			this.goodsList = goodsList;
		}

		@Override
		public void run()
		{
			if (!mRunInBackground)
				updateProgressDialog(count, goodsList.length);

			if (count < goodsList.length)
			{
				GoodsListResponse.GoodsItem item = goodsList[count];
				LogUtil.d(TAG, "startDownloadGoodsListRes: " + item.id);
				count++;
				if (item.tag.equals(Define.TAG_RING))
					RequestManager.loadGoodsDetail(mContext, mUserKey, item.id, new MyRequestCallback()
					{
						@Override
						void onResponseSuccess(BaseResponse response)
						{
							GoodsDetailResponse gdResponse = (GoodsDetailResponse)response;
							AndroidUtils.MainHandler.post(new DownloadGoodsDetailResourceRunnable(gdResponse.datas,
									new OnResourceDownloadCallback()
									{
										@Override
										public void onDownloadFinished(Object data)
										{
											AndroidUtils.MainHandler.post(LoadGoodsListDetailRunnable.this);
										}
									}));
						}

						@Override
						void onResponseFail(BaseResponse response, ErrorInfo error)
						{
							AndroidUtils.MainHandler.post(LoadGoodsListDetailRunnable.this);
						}
					}, CacheMode.PERFER_NETWORK);
				else if (item.tag.equals(Define.TAG_COUPLE))
				{
					RequestManager.loadCoupleRingDetail(mContext, mUserKey, item.id, new MyRequestCallback()
					{
						@Override
						void onResponseSuccess(BaseResponse response)
						{
							CoupleRingDetailResponse crdResponse = (CoupleRingDetailResponse)response;
							AndroidUtils.MainHandler.post(new DownloadGoodsDetailResourceRunnable(crdResponse.datas,
									new OnResourceDownloadCallback()
									{
										@Override
										public void onDownloadFinished(Object data)
										{
											AndroidUtils.MainHandler.post(LoadGoodsListDetailRunnable.this);
										}
									}));
						}

						@Override
						void onResponseFail(BaseResponse response, ErrorInfo error)
						{
							AndroidUtils.MainHandler.post(LoadGoodsListDetailRunnable.this);
						}
					}, CacheMode.PERFER_NETWORK);
				}
			}
			else
			{
				startDonwloadNextResource();
			}
		}
	}

	// 下载单个商品相关资源
	private class DownloadGoodsDetailResourceRunnable implements Runnable
	{
		GoodsDetailResponse.GoodsDetail goodsDetail;
		CoupleRingDetailResponse.GoodsDetail coupleRingGoodsDetail;
		OnResourceDownloadCallback callback;
		int downloadStep = 0;

		public DownloadGoodsDetailResourceRunnable(GoodsDetailResponse.GoodsDetail goodsDetail,
				OnResourceDownloadCallback callback)
		{
			this.goodsDetail = goodsDetail;
			this.callback = callback;
		}

		public DownloadGoodsDetailResourceRunnable(CoupleRingDetailResponse.GoodsDetail goodsDetail,
				OnResourceDownloadCallback callback)
		{
			this.coupleRingGoodsDetail = goodsDetail;
			this.callback = callback;
		}

		@Override
		public void run()
		{
			if (goodsDetail != null)
			{
				LogUtil.d(TAG, "startDownloadGoodsDetailRes goods_id=" + goodsDetail.id + "; downloadStep="
						+ downloadStep);
				switch (downloadStep)
				{
					case 0:// 下载商品首图
						downloadStep++;
						if (goodsDetail.thumb != null)
						{
							FileInfo fileInfo = goodsDetail.thumb;
							fileInfo.path = getGoodsImageDirectory(mContext).getAbsolutePath() + File.separator
									+ FileUtils.getFileName(fileInfo.url);
							FileDownloadHelper.checkAndDownloadIfNeed(mContext, goodsDetail, fileInfo,
									new OnFileDownloadCallback()
									{
										@Override
										public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
										{
										}

										@Override
										public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
												long count, long length, float speed)
										{
										}

										@Override
										public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadCanceled(Object tag, FileInfo fileInfo)
										{
										}
									}, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 1:// 下载商品相册图
						downloadStep++;
						if (goodsDetail.thumb_url != null && goodsDetail.thumb_url.length > 0)
						{
							FileInfo[] fileInfos = goodsDetail.thumb_url;
							for (FileInfo fileInfo : fileInfos)
							{
								fileInfo.path = getGoodsImageDirectory(mContext).getAbsolutePath() + File.separator
										+ FileUtils.getFileName(fileInfo.url);
							}
							FileDownloadHelper.startMultiDownload(fileInfos, mContext, fileInfos,
									new OnMultiFileDownloadCallback()
									{
										public void onMultiDownloadStarted(Object tag, FileInfo fileInfo,
												String localPath, int downloadIndex)
										{
										}

										public void onMultiDownloadProgress(Object tag, FileInfo fileInfo,
												String localPath, long count, long length, float speed,
												int downloadIndex)
										{
										}

										public void onMultiDownloadFinished(Object tag, FileInfo fileInfo,
												String localPath, int downloadIndex)
										{
										}

										public void onMultiDownloadCanceled(Object tag, FileInfo fileInfo,
												int downloadIndex)
										{
										}

										public void onMultiDownloadFailed(Object tag, FileInfo fileInfo,
												ErrorInfo error, int downloadIndex)
										{
										}

										public void onMultiAllDownloadFinished(Object tag, FileInfo[] fileInfos,
												DownloadStatus[] status)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}
									}, false, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 2:// 下载商品模型资源
						downloadStep++;
						if (goodsDetail.model_info != null)
						{
							FileInfo fileInfo = goodsDetail.model_info;
							fileInfo.path = getTempFileDirectory(mContext).getAbsolutePath() + File.separator
									+ FileUtils.getFileName(fileInfo.url);
							FileDownloadHelper.checkAndDownloadIfNeed(mContext, goodsDetail, fileInfo,
									new OnFileDownloadCallback()
									{
										@Override
										public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
										{
										}

										@Override
										public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
												long count, long length, float speed)
										{
										}

										@Override
										public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadCanceled(Object tag, FileInfo fileInfo)
										{
										}
									}, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 3:
						if (callback != null)
							callback.onDownloadFinished(goodsDetail);
						break;
				}
			}
			else if (coupleRingGoodsDetail != null)
			{
				LogUtil.d(TAG, "startDownloadCoupleRingGoodsDetailRes goods_id=" + coupleRingGoodsDetail.id
						+ "; downloadStep=" + downloadStep);
				switch (downloadStep)
				{
					case 0:// 下载商品首图
						downloadStep++;
						if (coupleRingGoodsDetail.thumb != null)
						{
							FileInfo fileInfo = coupleRingGoodsDetail.thumb;
							fileInfo.path = getGoodsImageDirectory(mContext).getAbsolutePath() + File.separator
									+ FileUtils.getFileName(fileInfo.url);
							FileDownloadHelper.checkAndDownloadIfNeed(mContext, coupleRingGoodsDetail, fileInfo,
									new OnFileDownloadCallback()
									{
										@Override
										public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
										{
										}

										@Override
										public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
												long count, long length, float speed)
										{
										}

										@Override
										public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadCanceled(Object tag, FileInfo fileInfo)
										{
										}
									}, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 1:// 下载商品相册图
						downloadStep++;
						if (coupleRingGoodsDetail.thumb_url != null && coupleRingGoodsDetail.thumb_url.length > 0)
						{
							FileInfo[] fileInfos = coupleRingGoodsDetail.thumb_url;
							for (FileInfo fileInfo : fileInfos)
							{
								fileInfo.path = getGoodsImageDirectory(mContext).getAbsolutePath() + File.separator
										+ FileUtils.getFileName(fileInfo.url);
							}
							FileDownloadHelper.startMultiDownload(fileInfos, mContext, fileInfos,
									new OnMultiFileDownloadCallback()
									{
										public void onMultiDownloadStarted(Object tag, FileInfo fileInfo,
												String localPath, int downloadIndex)
										{
										}

										public void onMultiDownloadProgress(Object tag, FileInfo fileInfo,
												String localPath, long count, long length, float speed,
												int downloadIndex)
										{
										}

										public void onMultiDownloadFinished(Object tag, FileInfo fileInfo,
												String localPath, int downloadIndex)
										{
										}

										public void onMultiDownloadCanceled(Object tag, FileInfo fileInfo,
												int downloadIndex)
										{
										}

										public void onMultiDownloadFailed(Object tag, FileInfo fileInfo,
												ErrorInfo error, int downloadIndex)
										{
										}

										public void onMultiAllDownloadFinished(Object tag, FileInfo[] fileInfos,
												DownloadStatus[] status)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}
									}, false, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 2:// 下载男戒模型资源
						downloadStep++;
						if (coupleRingGoodsDetail.model_infos != null && coupleRingGoodsDetail.model_infos.men != null)
						{
							FileInfo fileInfo = coupleRingGoodsDetail.model_infos.men;
							fileInfo.path = getTempFileDirectory(mContext).getAbsolutePath() + File.separator
									+ FileUtils.getFileName(fileInfo.url);
							FileDownloadHelper.checkAndDownloadIfNeed(mContext, coupleRingGoodsDetail, fileInfo,
									new OnFileDownloadCallback()
									{
										@Override
										public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
										{
										}

										@Override
										public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
												long count, long length, float speed)
										{
										}

										@Override
										public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadCanceled(Object tag, FileInfo fileInfo)
										{
										}
									}, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 3:// 下载女戒模型资源
						downloadStep++;
						if (coupleRingGoodsDetail.model_infos != null && coupleRingGoodsDetail.model_infos.wmen != null)
						{
							FileInfo fileInfo = coupleRingGoodsDetail.model_infos.wmen;
							fileInfo.path = getTempFileDirectory(mContext).getAbsolutePath() + File.separator
									+ FileUtils.getFileName(fileInfo.url);
							FileDownloadHelper.checkAndDownloadIfNeed(mContext, coupleRingGoodsDetail, fileInfo,
									new OnFileDownloadCallback()
									{
										@Override
										public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
										{
										}

										@Override
										public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
												long count, long length, float speed)
										{
										}

										@Override
										public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
										{
											AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
										}

										@Override
										public void onDownloadCanceled(Object tag, FileInfo fileInfo)
										{
										}
									}, false);
						}
						else
						{
							AndroidUtils.MainHandler.post(DownloadGoodsDetailResourceRunnable.this);
						}
						break;
					case 4:
						if (callback != null)
							callback.onDownloadFinished(coupleRingGoodsDetail);
						break;
				}
			}
		}
	}

	// 获取商品分类
	private void loadGoodsCategory(final boolean isShop)
	{
		if (!mRunInBackground)
		{
			if (isShop)
				updateProgressDialogTitle("下载门店商品分类");
			else
				updateProgressDialogTitle("下载商品分类");
		}
		RequestManager.loadGoodsCategorys(mContext, new MyRequestCallback()
		{
			@Override
			void onResponseSuccess(BaseResponse response)
			{
				GoodsCategorysResponse gcResponse = (GoodsCategorysResponse)response;
				if (gcResponse.datas != null && gcResponse.datas.list != null
						&& gcResponse.findZuanShiCategoryList() != null)
				{
					AndroidUtils.MainHandler.post(new LoadCategoryGoodsListRunnable(isShop, gcResponse
							.findZuanShiCategoryList()));
				}
				else
					startDonwloadNextResource();
			}

			@Override
			void onResponseFail(BaseResponse response, ErrorInfo error)
			{
				startDonwloadNextResource();
			}
		}, CacheMode.PERFER_NETWORK);
	}

	// 获取门店分类商品列表
	private class LoadCategoryGoodsListRunnable implements Runnable
	{
		CategoryItem[] list;
		int count = 0;
		int totalCount;
		boolean isShop;

		public LoadCategoryGoodsListRunnable(boolean isShop, CategoryItem[] list)
		{
			this.isShop = isShop;
			this.list = list;
			totalCount = list.length;
			// for (CategoryInfo info : goods_class_list)
			// {
			// if (info.children != null && info.children.length > 0)
			// totalCount += info.children.length;
			// }
		}

		@Override
		public void run()
		{
			if (!mRunInBackground)
				updateProgressDialog(count, totalCount);

			if (count < totalCount)
			{
				CategoryItem item = list[count];
				count++;
				loadCategoryGoodsList(isShop, item.id);
				return;
			}
			startDonwloadNextResource();
		}

		private void loadCategoryGoodsList(boolean isShop, String ccate)
		{
			RequestManager.loadGoodsList(mContext, mUserKey, isShop, ccate, new MyRequestCallback()
			{
				@Override
				void onResponseSuccess(BaseResponse response)
				{
					AndroidUtils.MainHandler.post(LoadCategoryGoodsListRunnable.this);
				}

				@Override
				void onResponseFail(BaseResponse response, ErrorInfo error)
				{
					AndroidUtils.MainHandler.post(LoadCategoryGoodsListRunnable.this);
				}
			}, CacheMode.PERFER_NETWORK);
		}
	}

	private void loadBannerADList()
	{
		if (!mRunInBackground)
			updateProgressDialogTitle("下载首页广告");
		RequestManager.loadBannerADList(mContext, new MyRequestCallback()
		{
			@Override
			void onResponseSuccess(BaseResponse response)
			{
				BannerADListResponse balResponse = (BannerADListResponse)response;
				if (balResponse.datas != null && balResponse.datas.list != null && balResponse.datas.list.length > 0)
				{
					final List<ImageInfo> list = new ArrayList<ImageInfo>();
					for (BannerADItem item : balResponse.datas.list)
					{
						if (item != null && item.thumb != null)
						{
							item.thumb.path = getGoodsImageDirectory(mContext).getAbsolutePath() + File.separator
									+ FileUtils.getFileFullName(item.thumb.url);
							list.add(item.thumb);
						}
					}
					FileInfo[] fileInfos = new FileInfo[list.size()];
					fileInfos = list.toArray(fileInfos);
					FileDownloadHelper.startMultiDownload(balResponse, mContext, fileInfos,
							new OnMultiFileDownloadCallback()
							{
								public void onMultiDownloadStarted(Object tag, FileInfo fileInfo, String localPath,
										int downloadIndex)
								{
									updateProgressDialog(list.indexOf(fileInfo), list.size());
								}

								public void onMultiDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
										long count, long length, float speed, int downloadIndex)
								{
								}

								public void onMultiDownloadFinished(Object tag, FileInfo fileInfo, String localPath,
										int downloadIndex)
								{
									updateProgressDialog(list.indexOf(fileInfo) + 1, list.size());
								}

								public void onMultiDownloadCanceled(Object tag, FileInfo fileInfo, int downloadIndex)
								{
								}

								public void onMultiDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error,
										int downloadIndex)
								{
								}

								public void onMultiAllDownloadFinished(Object tag, FileInfo[] fileInfos,
										DownloadStatus[] status)
								{
									startDonwloadNextResource();
								}
							}, false, false);
				}
				else
				{
					startDonwloadNextResource();
				}
			}

			@Override
			void onResponseFail(BaseResponse response, ErrorInfo error)
			{
				startDonwloadNextResource();
			}
		}, CacheMode.PERFER_NETWORK);
	}

	private void loadShopLogoAndAD()
	{
		if (!mRunInBackground)
			updateProgressDialogTitle("下载门店数据");
		RequestManager.loadShopLogoAndAD(mContext, mUserKey, new MyRequestCallback()
		{
			@Override
			void onResponseSuccess(BaseResponse response)
			{
				ShopLogoAndADResponse slaResponse = (ShopLogoAndADResponse)response;
				if (slaResponse.datas != null && slaResponse.datas.list != null)
				{
					final List<ImageInfo> list = new ArrayList<ImageInfo>();
					if (slaResponse.datas.list.logo != null)
					{
						slaResponse.datas.list.logo.path = getGoodsImageDirectory(mContext).getAbsolutePath()
								+ File.separator + FileUtils.getFileFullName(slaResponse.datas.list.logo.url);
						list.add(slaResponse.datas.list.logo);
					}
					if (slaResponse.datas.list.ads != null)
					{
						slaResponse.datas.list.ads.path = getGoodsImageDirectory(mContext).getAbsolutePath()
								+ File.separator + FileUtils.getFileFullName(slaResponse.datas.list.ads.url);
						list.add(slaResponse.datas.list.ads);
					}
					if (slaResponse.datas.list.screen != null && slaResponse.datas.list.screen.length > 0)
					{
						for (ImageInfo image : slaResponse.datas.list.screen)
						{
							if (image != null)
							{
								image.path = getGoodsImageDirectory(mContext).getAbsolutePath() + File.separator
										+ FileUtils.getFileFullName(image.url);
							}
						}
					}
					FileInfo[] fileInfos = new FileInfo[list.size()];
					fileInfos = list.toArray(fileInfos);
					FileDownloadHelper.startMultiDownload(slaResponse, mContext, fileInfos,
							new OnMultiFileDownloadCallback()
							{
								public void onMultiDownloadStarted(Object tag, FileInfo fileInfo, String localPath,
										int downloadIndex)
								{
									updateProgressDialog(list.indexOf(fileInfo), list.size());
								}

								public void onMultiDownloadProgress(Object tag, FileInfo fileInfo, String localPath,
										long count, long length, float speed, int downloadIndex)
								{
								}

								public void onMultiDownloadFinished(Object tag, FileInfo fileInfo, String localPath,
										int downloadIndex)
								{
									updateProgressDialog(list.indexOf(fileInfo) + 1, list.size());
								}

								public void onMultiDownloadCanceled(Object tag, FileInfo fileInfo, int downloadIndex)
								{
								}

								public void onMultiDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error,
										int downloadIndex)
								{
								}

								public void onMultiAllDownloadFinished(Object tag, FileInfo[] fileInfos,
										DownloadStatus[] status)
								{
									startDonwloadNextResource();
								}
							}, false, false);
				}
				else
					startDonwloadNextResource();
			}

			@Override
			void onResponseFail(BaseResponse response, ErrorInfo error)
			{
				startDonwloadNextResource();
			}
		}, CacheMode.PERFER_NETWORK);
	}
}
