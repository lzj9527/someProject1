package com.shiyou.tryapp2.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.app.fragment.SwipeRefreshWebViewFragment;
import android.extend.cache.FileCacheManager;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.HttpLoader;
import android.extend.loader.Loader.CacheMode;
import android.extend.util.AndroidUtils;
import android.extend.util.FileUtils;
import android.extend.util.HttpUtils;
import android.extend.util.LogUtil;
import android.extend.widget.ExtendWebView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.FileDownloadHelper;
import com.shiyou.tryapp2.FileDownloadHelper.OnFileDownloadCallback;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.ResourceHelper;
import com.shiyou.tryapp2.ResourceHelper2;
import com.shiyou.tryapp2.ResourceHelper2.OnResourceDownloadCallback;
import com.shiyou.tryapp2.app.login.LoginFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.app.product.ConfirmShopDetailsFragment;
import com.shiyou.tryapp2.app.product.MainIndexFragment;
import com.shiyou.tryapp2.app.product.PDFViewerFragment;
import com.shiyou.tryapp2.app.product.SearchListFragment;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.db.BrowseHistoryDBHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse.GoodsItem;
import com.unity3d.player.UnityPlayer;

public class WebViewFragment extends SwipeRefreshWebViewFragment
{
	public static WebViewFragment instance = null;

	public static File getAppCacheDirectory(Context context)
	{
		return FileUtils.getDirectory(context, "appcache");
	}

	public WebViewFragment(String firstUrl, List<NameValuePair> firstRequestPairs, List<NameValuePair> baseRequestPairs)
	{
		super(firstUrl, firstRequestPairs, baseRequestPairs);
		instance = this;
	}

	public WebViewFragment(String firstUrl)
	{
		super(firstUrl);
		instance = this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		initWebView();

		return view;
	}

	@Override
	public boolean onBackPressed()
	{
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK)
		{
			switch (requestCode)
			{
				case Define.REQ_LOGIN_FROM_MY:
					refresh();
					break;
			}
		}
	}

	@Override
	public void onPageLoadStarted(ExtendWebView webView, String url)
	{
		super.onPageLoadStarted(webView, url);
		// showLoadingIndicator();
	}

	@Override
	public void onLoadProgressChanged(ExtendWebView webView, int newProgress)
	{
		super.onLoadProgressChanged(webView, newProgress);
		if (MainFragment.instance != null)
			MainFragment.instance.invalidateMenuBarOnce();
	}

	@Override
	public void onPageLoadFinished(ExtendWebView webView, String url)
	{
		super.onPageLoadFinished(webView, url);
		// hideLoadingIndicator();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView()
	{
		getWebView().getSettings().setJavaScriptEnabled(true);
		getWebView().addJavascriptInterface(new JavaScriptInterface(), "android");

		String appCacheDirPath = getAppCacheDirectory(getActivity()).getAbsolutePath();
		LogUtil.v(TAG, "appCacheDirPath=" + appCacheDirPath);
		getWebView().getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		getWebView().getSettings().setDomStorageEnabled(true);
		// getWebView().getSettings().setDatabasePath(databasePath);
		getWebView().getSettings().setDatabaseEnabled(true);
		getWebView().getSettings().setAppCachePath(appCacheDirPath);
		getWebView().getSettings().setAppCacheEnabled(true);
		getWebView().getSettings().setAllowContentAccess(true);
		getWebView().getSettings().setAllowFileAccess(true);
		getWebView().getSettings().setAllowFileAccessFromFileURLs(true);
		getWebView().getSettings().setAllowUniversalAccessFromFileURLs(true);

		// getWebView().getSettings().setBuiltInZoomControls(true);
		getWebView().getSettings().setSupportZoom(false);
		getWebView().getSettings().setUseWideViewPort(false);
		// getWebView().getSettings().setDisplayZoomControls(true);
		// getWebView().getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);

		int scaleInPercent = 200 * MainActivity.windowDisplaySize.x / 2560;
		LogUtil.v(TAG, "setInitialScale: " + scaleInPercent);
		getWebView().setInitialScale(scaleInPercent);
	}

	public void clearCache()
	{
		getWebView().clearCache(true);
		FileUtils.deleteDirectory(getAppCacheDirectory(getActivity()));
	}

	// 购物车删除
	public void deleteShoppingCart()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				getWebView().loadUrl("javascript:deleteShoppingCartFromNative()");
			}
		});
	}

	// 门店价格设置保存
	public void saveShopPrice()
	{
		LogUtil.w(TAG, "门店价格设置保存");
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				getWebView().loadUrl("javascript:saveShopPriceFromNative()");
			}
		});
	}

	// private class hideLoadingIndicatorRunnable implements Runnable
	// {
	// @Override
	// public void run()
	// {
	// LogUtil.i(TAG, "hideLoadingIndicatorRunnable executed");
	// WebViewFragment.this.hideLoadingIndicator();
	// }
	// }
	//
	// private hideLoadingIndicatorRunnable mHideIndicatorRunnable = null;
	//
	// public void showLoadingIndicator(boolean autoHide)
	// {
	// removeHideLoadingIndicatorRunnable();
	// if (mHideIndicatorRunnable == null && autoHide)
	// {
	// mHideIndicatorRunnable = new hideLoadingIndicatorRunnable();
	// AndroidUtils.MainHandler.postDelayed(mHideIndicatorRunnable, 5000L);
	// }
	// WebViewFragment.super.showLoadingIndicator();
	// }
	//
	// @Override
	// public void showLoadingIndicator()
	// {
	// showLoadingIndicator(true);
	// }
	//
	// private void removeHideLoadingIndicatorRunnable()
	// {
	// if (mHideIndicatorRunnable != null)
	// {
	// AndroidUtils.MainHandler.removeCallbacks(mHideIndicatorRunnable);
	// mHideIndicatorRunnable = null;
	// }
	// }
	//
	// @Override
	// public void hideLoadingIndicator()
	// {
	// removeHideLoadingIndicatorRunnable();
	// WebViewFragment.super.hideLoadingIndicator();
	// }

	private void downloadGIACerFile(String title, String url)
	{
		//	如果sdk版本大于4.4
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
		{
			MainFragment.instance.addFragmentToCurrent(new PDFViewerFragment(title, url), false);
			return;
		}
		showLoadingIndicator();
		FileInfo fileInfo = new FileInfo();
		fileInfo.url = url;
		fileInfo.path = PDFViewerFragment.getPDFDirectoryPath(getContext()) + File.separatorChar + title + ".pdf";
		FileDownloadHelper.checkAndDownloadIfNeed(getContext(), TAG, fileInfo, new OnFileDownloadCallback()
		{
			@Override
			public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
			{
			}

			@Override
			public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath, long count, long length,
					float speed)
			{
			}

			@Override
			public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
			{
				hideLoadingIndicator();
				AndroidUtils.launchPDFFile(getContext(), localPath);
			}

			@Override
			public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
			{
				hideLoadingIndicator();
				showToast("下载GIA证书失败，错误码: " + error.errorCode);
			}

			@Override
			public void onDownloadCanceled(Object tag, FileInfo fileInfo)
			{
			}
		}, false);
	}

	public class JavaScriptInterface
	{
		// 打开新页面
		@JavascriptInterface
		public void openWindow(final int index, final String title, final String url)
		{
			LogUtil.v(TAG, "openWindow: " + index + "; " + title + "; " + url);
			AndroidUtils.MainHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					String actualUrl = url;
					if (url.contains("/pad/default"))
					{
						actualUrl = Config.BaseWebUrl + url.substring(url.indexOf("/pad/default"));
					}
					if (!actualUrl.contains("key="))
					{
						List<NameValuePair> pairs = new ArrayList<NameValuePair>();
						pairs.add(new BasicNameValuePair("key", LoginHelper.getUserKey(getContext())));
						actualUrl = HttpUtils.makeHttpGetUrl(actualUrl, pairs);
					}
					LogUtil.d(TAG, "openWindow: actualUrl=" + actualUrl);
					switch (index)
					{
						case 1:// 登录
							replace(getActivity(), new LoginFragment(), false);
							break;
						case 4:// 选定此砖石
								// if (ProductDetailsFragment.instance != null)
								// {
								// ProductDetailsFragment.instance.onBackPressed();
								// }
							MainFragment.instance.setCurrentMenu(2);
							// MainFragment.instance.setCurrentFragmentCar(actualUrl);
							break;
						case 5:// 订单列表
								// MainActivity.backToHomepage(getActivity(), 3);
							MainFragment.instance.setCurrentMenu(4);
							break;
						case 6:// 分类进产品列表1 ProductListFragment
								// add(MainFragment.instance,
								// MainFragment.instance.fragmentC1ID,
								// new ProductListFragment(), true);
							MainFragment.instance.addWebFragmentToCurrent(actualUrl, false);
							break;
						case 7:// 分类进产品列表2
							MainFragment.instance.addWebFragmentToCurrent(actualUrl, false);
							break;
						case 11:// 搜索进产品列表1
							// MainFragment.instance.addWebFragmentToMain(actualUrl, false);
							MainFragment.instance.addWebFragmentToCurrent(actualUrl, false);
							break;
						case 9:// 搜索进产品列表2
							MainFragment.instance.addWebFragmentToCurrent(actualUrl, false);
						case 10:// 分类选砖
							MainFragment.instance.addWebFragmentToCurrent(actualUrl, false);
							break;
						case 18:// 分类选砖
							MainFragment.instance.setCurrentMenu(4);
							// MainActivity.backToHomepage(getActivity(), 3);
							break;
					}
				}
			});
		}

		// 打开弹出页面
		@JavascriptInterface
		public void openPopWindow(final int index, final String title, final String url)
		{
			LogUtil.v(TAG, "openPopWindow: " + index + "; " + title + "; " + url);
		}

		// 打开详情页
		@JavascriptInterface
		public void openDetailWindow(final String goodsId, final String url, final int isShop)
		{
			LogUtil.v(TAG, "openDetailWindow: " + goodsId + "; " + url + "; " + isShop);
			if (AndroidUtils.isFastClick())
				return;
			// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new ProductDetailsFragment(
			// GoodsItem.TAG_RING, goodsId, true), true);
			if (isShop == 1)
				MainFragment.instance.addProductDetailFragmentToCurrent(goodsId, Define.TAG_RING, true, true, false);
			else
				MainFragment.instance.addProductDetailFragmentToCurrent(goodsId, Define.TAG_RING, false, true, false);
		}

		// 打开对戒详情页
		@JavascriptInterface
		public void openDetailWindowInCoupleRing(final String goodsId, final String url, final int isShop)
		{
			if (AndroidUtils.isFastClick())
				return;
			LogUtil.v(TAG, "openDetailWindowInCoupleRing: " + goodsId + "; " + url + "; " + isShop);
			// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new ProductDetailsFragment(
			// GoodsItem.TAG_COUPLE, goodsId, true), true);
			if (isShop == 1)
				MainFragment.instance.addProductDetailFragmentToCurrent(goodsId, Define.TAG_COUPLE, true, true, false);
			else
				MainFragment.instance.addProductDetailFragmentToCurrent(goodsId, Define.TAG_COUPLE, false, true, false);
		}

		// JIA选钻后打开详情页
		@JavascriptInterface
		public void openDetailWindowFromJIA(final String goodsId, final String url, final String jiaJson)
		{
			LogUtil.v(TAG, "openDetailWindowFromJIA: " + goodsId + "; " + url + "; " + jiaJson);
			// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new ConfirmShopDetailsFragment(goodsId,
			// jiaJson), true);
			MainFragment.instance.addFragmentToCurrent(new ConfirmShopDetailsFragment(goodsId, jiaJson, 1), false);
		}

		// JIA选钻后添加至购物车
		@JavascriptInterface
		public void appendShoppingCartFromJIA(final String goodsId, final String jiaJson)
		{
			LogUtil.v(TAG, "appendShoppingCartFromJIA: " + goodsId + "; " + jiaJson);
		}

		// 搜索商品
		@JavascriptInterface
		public void searchGoods(final String keyword)
		{
			LogUtil.v(TAG, "searchGoods: " + keyword);
			if (AndroidUtils.isFastClick())
				return;
			MainFragment.instance.addFragmentToCurrent(new SearchListFragment(keyword), false);
		}

		// 登录或注册完成
		@JavascriptInterface
		public void onLoginFinished(final String userName, final String userKey)
		{
			LogUtil.v(TAG, "onLoginFinished: " + userName + "; " + userKey);
			LoginHelper.onLoginFinished(getActivity(), userName, userName, userKey);
			if (getActivity() != MainActivity.instance)
			{
				getActivity().finish();
				if (MainFragment.instance != null)
				{
					MainFragment.instance.backToHomepage();
					MainFragment.instance.doRefresh();
				}
				if (MainIndexFragment.instance != null)
					MainIndexFragment.instance.doRefresh();
			}
			else
			{
				// new ResourceHelper(getActivity(), userKey, false)
				// .checkAndDownloadResource(new OnResourceDownloadCallback()
				// {
				// @Override
				// public void onDownloadFinished(Object data)
				// {
				showLoadingIndicator();
				RequestManager.loadShopLogoAndAD(getContext(), userKey, new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						if (response != null)
							if (response.resultCode == BaseResponse.RESULT_OK)
							{
							}
							else
							{
								showToast(response.error);
							}
						RequestManager.loadGoodsList(getActivity(), userKey, true, new RequestCallback()
						{
							@Override
							public void onRequestResult(int requestCode, long taskId, BaseResponse response,
									DataFrom from)
							{
								hideLoadingIndicator();
								String id = "5";
								String tag = Define.TAG_RING;
								if (response != null && response.resultCode == BaseResponse.RESULT_OK)
								{
									GoodsListResponse glResponse = (GoodsListResponse)response;
									if (glResponse.datas != null && glResponse.datas.list != null
											&& glResponse.datas.list.length > 0)
									{
										for (GoodsItem item : glResponse.datas.list)
										{
											if (item.tag.equals(Define.TAG_RING))
											{
												id = item.id;
												tag = item.tag;
												break;
											}
										}
									}
								}
								BaseFragment.replace(getActivity(), new MainFragment(id, tag), false);
								if (from != DataFrom.SERVER)
									RequestManager.loadGoodsList(getContext(), userKey, true, null,
											CacheMode.PERFER_NETWORK);
							}

							@Override
							public void onRequestError(int requestCode, long taskId, ErrorInfo error)
							{
								onRequestResult(requestCode, taskId, null, DataFrom.SERVER);
							}
						});
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						showToast("网络错误: " + error.errorCode);
						onRequestResult(requestCode, taskId, null, DataFrom.SERVER);
					}
				});
				// }
				// });
			}
		}

		// 登录或注册失败
		@JavascriptInterface
		public void onLoginFailed(final String errorText)
		{
			LogUtil.v(TAG, "onLoginFailed: " + errorText);
			showToast(errorText);
		}

		// 购物车添加成功
		@JavascriptInterface
		public void onAddShoppingCartSucceed()
		{
			LogUtil.v(TAG, "onAddShoppingCartSucceed...");
			hideLoadingIndicator();
			if (MainFragment.instance != null)
				MainFragment.instance.updateShoppingcartNum();
		}

		// 购物车删除成功
		@JavascriptInterface
		public void onDeleteShoppingCartSucceed()
		{
			LogUtil.v(TAG, "onDeleteShoppingCartSucceed...");
			hideLoadingIndicator();
			if (MainFragment.instance != null)
				MainFragment.instance.updateShoppingcartNum();
		}

		// 下载JIA证书
		@JavascriptInterface
		public void downloadJIACer(final String url)
		{
			LogUtil.v(TAG, "downloadJIACer: " + url);
			// String temp =
			// "https://www.gia.edu/otmm_wcs_int/proxy-pdf/?ReportNumber=5243390559&url=https://myapps.gia.edu/RptChkClient/reportClient.do?ReportNumber=FCD06BADED5FE6200724F654063A16A5";
			// add(getActivity(), new PDFViewerFragment("5243390559", temp), true);
			WebViewFragment.this.downloadGIACerFile("-", url);
		}

		// 下载GIA证书
		@JavascriptInterface
		public void downloadGIACer(final String title, final String url)
		{
			LogUtil.v(TAG, "downloadGIACer: " + title + "; " + url);
			// add(getActivity(), new PDFViewerFragment(title, url), true);
			WebViewFragment.this.downloadGIACerFile(title, url);
		}

		// 清理缓存
		@JavascriptInterface
		public void clearCache()
		{
			LogUtil.v(TAG, "clearCache...");
			showLoadingIndicator();
			AndroidUtils.MainHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					LogUtil.d(TAG, "start clear cache...");
					final long startTime = System.currentTimeMillis();
					WebViewFragment.this.clearCache();
					try
					{
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyCleanCache", "");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							HttpLoader.clearMemoryCache();
							FileCacheManager.clearAllCaches(getActivity());
							ResourceHelper.cleanResource(getContext());
							FileDownloadHelper.clearAllDownloadedFile(getContext());
							PDFViewerFragment.deletePDFDirectory(getContext());
							BrowseHistoryDBHelper.getInstance().deleteAll(getContext());
							MainActivity.instance.clearCache();
							long time = System.currentTimeMillis() - startTime;
							LogUtil.v(TAG, "clear cache finished, time = " + time);
							hideLoadingIndicator();
							showToast("缓存清理已完成.");
						}
					}).start();
				}
			});
		}

		// 登录
		@JavascriptInterface
		public void login()
		{
			LogUtil.v(TAG, "login...");
			AndroidUtils.MainHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
				}
			});
		}

		// 注册
		@JavascriptInterface
		public void register()
		{
			LogUtil.v(TAG, "register...");
			AndroidUtils.MainHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
				}
			});
		}

		// 检查更新
		@JavascriptInterface
		public void checkVersion()
		{
			LogUtil.v(TAG, "checkVersion...");
			LoginHelper.checkVersion(getActivity(), false);
		}

		// 更新资源文件
		@JavascriptInterface
		public void updateResource()
		{
			LogUtil.v(TAG, "updateResource...");
			new ResourceHelper2(getActivity(), LoginHelper.getUserKey(getContext()), false, true)
					.downloadResource(new OnResourceDownloadCallback()
					{
						@Override
						public void onDownloadFinished(Object data, boolean canceled)
						{
							if (!canceled)
								showToast("资源更新已完成");
						}
					});
		}

		// 提示信息
		@JavascriptInterface
		public void showToast(String text)
		{
			AndroidUtils.showToast(getActivity(), text);
			if (text.contains("购物车"))
				if (MainFragment.instance != null)
					MainFragment.instance.updateShoppingcartNum();
		}

		// 刷新当前页面
		@JavascriptInterface
		public void refresh()
		{
			AndroidUtils.MainHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					WebViewFragment.this.refresh();
				}
			});
		}

		// 显示加载动画
		@JavascriptInterface
		public void showLoadingIndicator()
		{
			LogUtil.v(TAG, "showLoadingIndicator...");
			WebViewFragment.this.showLoadingIndicator(true);
		}

		// 隐藏加载动画
		@JavascriptInterface
		public void hideLoadingIndicator()
		{
			LogUtil.v(TAG, "hideLoadingIndicator...");
			WebViewFragment.this.hideLoadingIndicator();
		}
	}
}
