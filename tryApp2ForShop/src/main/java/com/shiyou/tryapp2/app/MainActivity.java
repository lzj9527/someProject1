package com.shiyou.tryapp2.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.extend.BasicConfig;
import android.extend.ErrorInfo;
import android.extend.app.ActivityProxy.IActivityExtend;
import android.extend.app.BaseFragment;
import android.extend.data.BaseData;
import android.extend.loader.BaseJsonParser;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.loader.Loader;
import android.extend.loader.Loader.CacheMode;
import android.extend.loader.Loader.LoadParams;
import android.extend.loader.UrlLoader;
import android.extend.util.AndroidUtils;
import android.extend.util.FileUtils;
import android.extend.util.LogUtil;
import android.extend.util.ReflectHelper;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.util.ViewTools.FitMode;
import android.extend.widget.DispatchTouchListener;
import android.extend.widget.ExtendImageView;
import android.extend.widget.MenuBar;
import android.extend.widget.MenuBar.OnMenuListener;
import android.extend.widget.MenuView;
import android.extend.widget.ProgressBar;
import android.extend.widget.ProgressBar.ChangeProgressMode;
import android.extend.widget.ProgressBar.OnProgressChangedListener;
import android.extend.widget.recycler.AbsAdapterItem;
import android.extend.widget.recycler.BaseRecyclerAdapter;
import android.extend.widget.recycler.BaseRecyclerAdapter.BaseViewHolder;
import android.extend.widget.recycler.ListRecyclerView;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.FileDownloadHelper;
import com.shiyou.tryapp2.FileDownloadHelper.DownloadStatus;
import com.shiyou.tryapp2.FileDownloadHelper.OnFileDownloadCallback;
import com.shiyou.tryapp2.FileDownloadHelper.OnMultiFileDownloadCallback;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.ScreenSaversHelper;
import com.shiyou.tryapp2.app.camera.CameraActivity;
import com.shiyou.tryapp2.app.login.LoginFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.app.product.ProductDetailsFragment;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.TryonPoseData;
import com.shiyou.tryapp2.data.UnityImageInfo;
import com.shiyou.tryapp2.data.UnityModelInfo;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse.GoodsItem;
import com.shiyou.tryapp2.shop.zsa.R;
import com.umeng.analytics.MobclickAgent;
import com.unity3d.player.UnityPlayer;

public class MainActivity extends BaseAppActivity
{
	public enum StartFrom
	{
		NONE, WomanRing, ManRing, CoupleRing, Necklace
	}

	public static final String TAG = MainActivity.class.getSimpleName();
	public static MainActivity instance = null;
	public static DisplayMetrics windowDisplayMetrics = null;
	public static Point windowDisplaySize;
	public static float scaled = 1.0f;
	public static float fontScaled = 1.0f;

	private static StartFrom mStartFrom = StartFrom.NONE;
	private static BaseData mStartFromData;
	private static String mSelectedMaterialTag;

	public static boolean UnityLocked = false;

	private View mTryonUI;

	private View mFullscreenMask;
	// private View mTryonWindow;
	public UnityPlayer mUnityPlayer;
	private FrameLayout mUnityContainer;
	private View mToolbar;
	private ExtendImageView mLogoImageView;
	private View mCategoryLayout;
	// private int mCategoryMenuIndex = -1;
	private MenuBar mCategoryMenuBar;
	private View mGoodsListLayout;
	private ListRecyclerView mGoodsListView;
	private BaseRecyclerAdapter mGoodsListAdapter;
	private View mMinHandLayout;
	private View mMinHand;
	private View mMinHandMan;
	private View mMinHandTwo;
	private View mMinNecklace;
	private View mEraseLayout;
	private View mEraseOkBtn;
	private View mEraseEditor;
	private View mEraseRemove;
	private ProgressBar mEraseProgressBar;
	private String mEraseState;
	private float mEraseBrushSizePercent;
	private View loading_indicator;
	// private List<String> mDownloadUrlList = Collections.synchronizedList(new ArrayList<String>());
	private OnModelLoadListener mModelLoadListener = new OnModelLoadListener()
	{
		@Override
		public void onModelLoadStarted(String id)
		{
			showLoadingIndicator(10 * 1000L);
			// show3DLoadingIndicator();
		}

		@Override
		public void onModelLoadProgress(String id, float progress)
		{
		}

		@Override
		public void onModelLoadFinished(String id, int layer, String faceTag, boolean isClothes)
		{
			hideLoadingIndicator();
			// hide3DLoadingIndicator();
			if (!TextUtils.isEmpty(mSelectedMaterialTag))
			{
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyChangeAllModelMaterial",
						mSelectedMaterialTag);
			}
		}

		@Override
		public void onModelLoadFailed(String id, String error)
		{
			hideLoadingIndicator();
			// hide3DLoadingIndicator();
			showToast(error);
		}
	};

	private View mSplashView;

	private View mMainUI;

	private String mCurrentUI = Define.UI_SPLASH;
	private String mUIState;

	public enum ScreenShotAfter
	{
		None, Save, Back, Share, Upload,
		// EnterCombinePublish, EnterPosterFactory, EnterBeautyImage,
	}

	private boolean mImageFromCamera = false;
	private ScreenShotAfter mScreenShotAfter = ScreenShotAfter.None;
	private boolean mNeedScreenshot = true;
	private String mScreenshotPath;

	// private GoodsItem[] mGoodsItemList = null;
	private String mSelectedId;
	private Map<String, GoodsItem> mGoodsItemMap = Collections.synchronizedMap(new HashMap<String, GoodsItem>());
	private List<String> mCheckedGoodsList = new ArrayList<String>();

	private boolean mBackPressed = false;

	private TryonPoseData mTryonPoseData;

	private String mSelectedGoodsId = "5";
	private String mSelectedGoodsTag = "one";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		instance = this;

		windowDisplayMetrics = AndroidUtils.getActivityDisplayMetrics(this);
		LogUtil.w(TAG, "windowDisplayMetrics: " + windowDisplayMetrics.widthPixels + " x "
				+ windowDisplayMetrics.heightPixels + "; " + windowDisplayMetrics.density + "; "
				+ windowDisplayMetrics.densityDpi + "; " + windowDisplayMetrics.scaledDensity);
		windowDisplaySize = AndroidUtils.getActivityDisplaySize(this);
		LogUtil.w(TAG, "windowDisplaySize: " + windowDisplaySize.x + " x " + windowDisplaySize.y);
		scaled = windowDisplaySize.x / 2560f;
		fontScaled = Math.min(1.0f, scaled);
		LogUtil.v(TAG, "scaled: " + scaled + "; fontScaled: " + fontScaled);

		int layout = ResourceUtil.getLayoutId(getApplicationContext(), "base_main_layout");
		View view = View.inflate(getApplicationContext(), layout, null);
		setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		if (AndroidUtils.checkDeviceHasNavigationBar(this))
			view.setFitsSystemWindows(false);
		else
			view.setFitsSystemWindows(true);
		view.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener()
		{
			@Override
			public void onSystemUiVisibilityChange(int visibility)
			{
				LogUtil.v(TAG, "onSystemUiVisibilityChange: " + visibility);
				setSystemUiVisibility();
			}
		});

		android.extend.widget.ExtendFrameLayout rootView = (android.extend.widget.ExtendFrameLayout)view;
		rootView.setDispatchTouchListener(new DispatchTouchListener()
		{
			@Override
			public boolean dispatchTouch(View v, MotionEvent event)
			{
				// LogUtil.v(TAG, "dispatchTouch: " + event);
				ScreenSaversHelper.updateAutoShowMessage();
				// if (AndroidUtils.checkDeviceHasNavigationBar(instance))
				// AndroidUtils.MainHandler.postDelayed(new Runnable()
				// {
				// @Override
				// public void run()
				// {
				// setSystemUiVisibility();
				// }
				// }, 100L);
				return false;
			}
		});
		// rootView.setInterceptTouchEventToDownward(true);

		// ScreenSaversHelper.init(instance);

		// ensureUnityPlayer();
		ensureTryonUI();
		ensureSplashView();
		ensureMainUI();

		// if (AndroidUtils.checkDeviceHasNavigationBar(this))
		// {
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// }
		// else
		// {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (!AndroidUtils.checkDeviceHasNavigationBar(this))
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		// }

		// doFullscreen(false);
		setCurrentUI(Define.UI_SPLASH, null);

		// PGEditImageLoader.initImageLoader(this);
		//
		// PGEditSDK.instance().initSDK(this);

		registerAlarmManager();
		// ShareSDK.initSDK(this);
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
		LogUtil.d(TAG, "JPushInterface.getRegistrationID: " + JPushInterface.getRegistrationID(getApplicationContext()));
		BasicConfig.init(this);
	}

	@Override
	public void setSystemUiVisibility()
	{
		super.setSystemUiVisibility();
		if (AndroidUtils.checkDeviceHasNavigationBar(this))
			mUnityPlayer.setSystemUiVisibility(visibility);
	}

	public void registerAlarmManager()
	{
		Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

		long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
		long systemTime = System.currentTimeMillis();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(systemTime);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		// 选择的每天定时时间
		long selectTime = calendar.getTimeInMillis();

		// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
		if (systemTime > selectTime)
		{
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			selectTime = calendar.getTimeInMillis();
		}

		// 计算现在时间到设定时间的时间差
		long time = selectTime - systemTime;
		firstTime += time;

		// 进行闹铃注册
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		// manager.cancel(sender);
		long intervalTime = 24 * 60 * 60 * 1000L;
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, intervalTime, sender);

		LogUtil.i(TAG, "registerAlarmManager time === " + time + ", selectTime === " + selectTime + ", systemTime === "
				+ systemTime + ", firstTime === " + firstTime + ", intervalTime === " + intervalTime);
	}

	@Override
	protected void onDestroy()
	{
		mUnityPlayer.quit();
		super.onDestroy();
		instance = null;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
		setSystemUiVisibility();
		// updateShoppingCartNum();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		// LogUtil.d(TAG, "onNewIntent: " + intent.getExtras());
		if (intent.getExtras() != null)
		{
			Bundle extras = intent.getExtras();
			if (extras.containsKey(JPushInterface.EXTRA_NOTIFICATION_ID))
			{
				if (LoginHelper.isLogined())
				{
					MainActivity.backToHomepage(this, 4);
				}
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
		setSystemUiVisibility();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
		setSystemUiVisibility();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		ScreenSaversHelper.updateAutoShowMessage();
		if (mCurrentUI == null || mCurrentUI.equals(Define.UI_SPLASH))
			return true;
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if (mBackPressed)
				return true;
			if (mCurrentUI != null && mCurrentUI.equals(Define.UI_TRYON_UI))
			{
				onBackPressed();
				return true;
			}
			if (canPopBackStackInChildren())
			{
				return super.onKeyDown(keyCode, event);
			}
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed()
	{
		LogUtil.v(TAG, "onBackPressed: " + mCurrentUI);
		if (mCurrentUI != null && mCurrentUI.equals(Define.UI_SPLASH))
			return;
		if (mCurrentUI != null && mCurrentUI.equals(Define.UI_TRYON_UI))
		{
			if (AndroidUtils.isFastClick())
				return;
			// if (!mDownloadUrlList.isEmpty())
			// {
			// cancelDownloadingList();
			// return;
			// }
			if (mUIState != null)
			{
				if (mUIState.equals(Define.STATE_ERASURE))
				{
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyChangeSelectedModelState",
							Define.STATE_ACTION);
					return;
				}
				// if (mUIState.equals(Define.STATE_ACTION))
				// {
				// UnityPlayer.UnitySendMessage("PlatformMessageHandler",
				// "NotifySetSelectModel", "");
				// return;
				// }
			}
			backToMainUI(null);
			return;
		}
		// if (canPopBackStack())
		// {
		// if (AndroidUtils.isFastClick())
		// return;
		// popBackStackImmediate();
		// return;
		// }
		exitApp();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			switch (requestCode)
			{
				case Define.REQ_CAMERA_FROM_TRYON:
					if (data != null)
					{
						String path = data.getStringExtra(Define.Name_Path);
						LogUtil.d(TAG, "path: " + path);
						UnityImageInfo image = new UnityImageInfo();
						image.tag = Define.TAG_PERSON_IMAGE;
						image.url = path;
						image.path = path;
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyRemoveAllImages", "");
						mImageFromCamera = true;
						setMinHandSelect(min_hand_selected_type, min_hand_selected_index, false, null);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyLoadImage",
								UnityImageInfo.toJson(image));
					}
					break;
			}
		}
	}

	private long exitTime = 0;

	/**
	 * 退出程序
	 */
	private void exitApp()
	{
		// 判断2次点击事件时间
		if ((System.currentTimeMillis() - exitTime) > 2000)
		{
			Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		}
		else
		{
			quit();
		}
	}

	public static void quit()
	{
		LogUtil.d(TAG, "quit");
		MobclickAgent.onKillProcess(instance);
		instance.finish();
		System.exit(0);
	}

	public void clearCache()
	{
		mTryonPoseData = null;
	}

	private void setCurrentUI(final String uiTag, final AnimatorListener listener)
	{
		if (uiTag.equals(mCurrentUI))
			return;
		LogUtil.v(TAG, "setCurrentUI: " + uiTag);
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mCurrentUI = uiTag;
				if (uiTag.equals(Define.UI_SPLASH))
				{
					mSplashView.setVisibility(View.VISIBLE);
				}
				else if (uiTag.equals(Define.UI_MAIN_UI))
				{
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyBackToWaiting", "");
					showMainUI(listener);
				}
				else if (uiTag.equals(Define.UI_DETAIL_UI))
				{

				}
				else if (uiTag.equals(Define.UI_TRYON_UI))
				{
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyBackToWaiting", "");
					mSplashView.setVisibility(View.GONE);
					mTryonUI.setVisibility(View.VISIBLE);
					hideMainUI(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							attachUnityPlayer();
							addModelLoadListener(mModelLoadListener);
							UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyEnterCombineTryonScene", "");
							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
				}
			}
		});
	}

	private void ensureTryonUI()
	{
		if (mTryonUI == null)
		{
			// int layout = ResourceUtil.getLayoutId(getApplicationContext(),
			// "tryon_layout");
			// mTryonUI = View.inflate(getApplicationContext(), layout, null);
			// addContentView(mTryonUI, new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.MATCH_PARENT));

			int id = ResourceUtil.getId(getApplicationContext(), "tryon_layout");
			mTryonUI = findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "fullscreen_mask");
			mFullscreenMask = mTryonUI.findViewById(id);
			mFullscreenMask.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					if (event.getAction() == MotionEvent.ACTION_DOWN)
					{
						// LogUtil.v(TAG, "onTouch: " + event + "; " +
						// mUIState);
						if (mGoodsListLayout.getVisibility() == View.VISIBLE)
						{
							hideGoodsListLayout();
							return true;
						}
					}
					return false;
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "loading_indicator");
			loading_indicator = mTryonUI.findViewById(id);
			hide3DLoadingIndicator();
		}
		ensureUnityPlayer();
		ensureToolbar();
		ensureCategoryLayout();
		ensureGoodsListLayout();
		ensureEraseLayout();
		ensureMinHand();
	}

	private void show3DLoadingIndicator()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				loading_indicator.setVisibility(View.VISIBLE);

				ObjectAnimator oa = ObjectAnimator.ofFloat(loading_indicator, "rotation", 0f, 360f);
				ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
				oa.setDuration(1000L);
				oa.setInterpolator(new LinearInterpolator());
				oa.setRepeatCount(ValueAnimator.INFINITE);
				oa.start();
			}
		});
	}

	private void hide3DLoadingIndicator()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				loading_indicator.setVisibility(View.GONE);
			}
		});
	}

	private void ensureUnityPlayer()
	{
		if (mUnityPlayer == null)
		{
			mUnityPlayer = new UnityPlayer(this);
			if (AndroidUtils.checkDeviceHasNavigationBar(this))
				mUnityPlayer.setFitsSystemWindows(false);
			else
				mUnityPlayer.setFitsSystemWindows(true);
			if (AndroidUtils.checkDeviceHasNavigationBar(this))
				mUnityPlayer.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener()
				{
					@Override
					public void onSystemUiVisibilityChange(int visibility)
					{
						LogUtil.v(TAG, "onSystemUiVisib  ilityChange: " + visibility);
						setSystemUiVisibility();
					}
				});
			// int gles_mode = mUnityPlayer.getSettings().getInt("gles_mode",
			// 1);
			// mUnityPlayer.init(gles_mode, false);
			// UnityPlayer.currentActivity = this;
			// if (mUnityPlayer.getSettings ().getBoolean ("hide_status_bar",
			// true))
			// ReflectHelper.invokeDeclaredMethod(mUnityPlayer, "setFullscreen", new Class[] { boolean.class },
			// new Object[] { false });

			int id = ResourceUtil.getId(getApplicationContext(), "unity_container");
			mUnityContainer = (FrameLayout)mTryonUI.findViewById(id);
			mUnityContainer.addView(mUnityPlayer);
		}
	}

	public void attachUnityPlayer()
	{
		if (ViewTools.containsView(mUnityContainer, mUnityPlayer))
			return;
		if (ProductDetailsFragment.instance != null)
			ProductDetailsFragment.instance.detachUnityPlayer();
		// ViewTools.removeViewParent(mUnityPlayer);
		mUnityContainer.addView(mUnityPlayer);
		mUnityPlayer.setVisibility(View.VISIBLE);
		mUnityPlayer.resume();
	}

	public void detachUnityPlayer()
	{
		if (ViewTools.containsView(mUnityContainer, mUnityPlayer))
			mUnityContainer.removeView(mUnityPlayer);
	}

	private void ensureToolbar()
	{
		if (mToolbar == null)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "toolbar");
			mToolbar = mTryonUI.findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "homepage_button");
			mLogoImageView = (ExtendImageView)mToolbar.findViewById(id);
			// View homepage_button = mToolbar.findViewById(id);
			mLogoImageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					backToMainUI(null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "camera_button");
			View camera_button = mTryonUI.findViewById(id);
			camera_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					CameraActivity.launchMeForResult(instance, min_hand_selected_type, 0, Define.REQ_CAMERA_FROM_TRYON);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "product_details_button");
			View product_details_button = mTryonUI.findViewById(id);
			product_details_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					backToMainUI(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							MainFragment.instance.addProductDetailFragmentToMain(mSelectedGoodsId, mSelectedGoodsTag,
									true, true, true);
						}
					});
					// AndroidUtils.MainHandler.postDelayed(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					// MainFragment.instance.addProductDetailFragmentToMain(mSelectedGoodsId, mSelectedGoodsTag,
					// true, true);
					// }
					// }, 200L);

					// if (mNeedScreenshot) {
					// mScreenShotAfter = ScreenShotAfter.None;
					// showLoadingIndicatorDialog("截屏中...");
					// UnityPlayer.UnitySendMessage("PlatformMessageHandler",
					// "NotifyCaptureScreenShot", "");
					// } else {
					// showToast("截屏成功,文件保存在:" + mScreenshotPath);
					// }
				}
			});

			// id = ResourceUtil.getId(getApplicationContext(), "mproduct");
			// View mproduct = mTryonUI.findViewById(id);
			//
			// id = ResourceUtil.getId(getApplicationContext(), "product_des");
			// View product_des = mproduct.findViewById(id);
			// product_des.setOnClickListener(new View.OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// backToMainUI(null);
			// BaseFragment.replace(instance, new MainFragment(GoodsId), false);
			// }
			// });

		}
	}

	// public void ensureShopLogo()
	// {
	// RequestManager.loadShopLogoAndAD(getApplicationContext(), LoginHelper.getUserKey(getApplicationContext()),
	// new RequestCallback()
	// {
	// @Override
	// public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
	// {
	// if (response.resultCode == BaseResponse.RESULT_OK)
	// {
	// ShopLogoAndADResponse mShopLogoAndADResponse = (ShopLogoAndADResponse)response;
	// if (mShopLogoAndADResponse != null && mShopLogoAndADResponse.datas != null
	// && mShopLogoAndADResponse.datas.list != null
	// && mShopLogoAndADResponse.datas.list.logo != null)
	// {
	// mLogoImageView.setImageDataSource(mShopLogoAndADResponse.datas.list.logo.url,
	// mShopLogoAndADResponse.datas.list.logo.filemtime, DecodeMode.FIT_WIDTH);
	// mLogoImageView.startImageLoad(false);
	// }
	// }
	// else
	// {
	// showToast(response.error);
	// }
	// }
	//
	// @Override
	// public void onRequestError(int requestCode, long taskId, ErrorInfo error)
	// {
	// showToast("网络错误: " + error.errorCode);
	// }
	// });
	// }

	private void showToolbar()
	{
		showToolbar(null);
	}

	private void showToolbar(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mToolbar.getTranslationX() != 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mToolbar, "translationX", mToolbar.getTranslationX(), 0);
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}
			}
		});
	}

	private void hideToolbar()
	{
		hideToolbar(null);
	}

	private void hideToolbar(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mToolbar.getTranslationX() == 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mToolbar, "translationX", 0,
							-(mToolbar.getLeft() + mToolbar.getWidth()));
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}
			}
		});
	}

	private void ensureCategoryLayout()
	{
		if (mCategoryLayout == null)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "category_layout");
			mCategoryLayout = mTryonUI.findViewById(id);

			mCategoryMenuBar = (MenuBar)mCategoryLayout;
			mCategoryMenuBar.setOnMenuListener(new OnMenuListener()
			{
				@Override
				public void onMenuUnSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
				{
					// TODO Auto-generated method stub
				}

				@Override
				public void onMenuSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
				{
					showGoodsList(menuView.getId());
					// switch (menuView.getId())
					// {
					// case R.id.category_all:
					// showGoodsList(Config.Type_Unknown);
					// break;
					// case R.id.category_ring:
					// showGoodsList(Config.Type_Ring);
					// break;
					// case R.id.category_necklace:
					// showGoodsList(Config.Type_Necklace);
					// break;
					// case R.id.category_bracelet:
					// showGoodsList(Config.Type_Wristlet);
					// break;
					// case R.id.category_earring:
					// showGoodsList(Config.Type_Earring);
					// break;
					// }
				}
			});
		}
	}

	private void showCategoryLayout()
	{
		showCategoryLayout(null);
	}

	private void showCategoryLayout(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mCategoryLayout.getTranslationX() != 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mCategoryLayout, "translationX",
							mCategoryLayout.getTranslationX(), 0f);
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}
			}
		});
	}

	private void hideCategoryLayout()
	{
		hideCategoryLayout(null);
	}

	private void hideCategoryLayout(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mCategoryLayout.getRotationX() == 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mCategoryLayout, "translationX", 0,
							mCategoryLayout.getWidth() + mCategoryLayout.getRight());
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}
			}
		});
	}

	private void ensureGoodsListLayout()
	{
		if (mGoodsListLayout == null)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "goodslist_layout");
			mGoodsListLayout = mTryonUI.findViewById(id);

			mGoodsListView = new ListRecyclerView(getApplicationContext());
			((ViewGroup)mGoodsListLayout).addView(mGoodsListView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mGoodsListAdapter = new BaseRecyclerAdapter();
			mGoodsListView.setAdapter(mGoodsListAdapter);
			mGoodsListView.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					resetHideGoodsListLayoutProcess();
					return false;
				}
			});
			mGoodsListView.addOnLayoutChangeListener(new OnLayoutChangeListener()
			{
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom)
				{
					LogUtil.v(TAG, "onLayoutChange: " + v + "; " + left + "; " + top + "; " + right + "; " + bottom);
					mGoodsListView.removeOnLayoutChangeListener(this);
					hideGoodsListLayout();
				}
			});
		}
	}

	private void showGoodsListLayout()
	{
		showGoodsListLayout(null);
	}

	private void showGoodsListLayout(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mGoodsListLayout.setVisibility(View.VISIBLE);
				if (mGoodsListLayout.getTranslationX() == 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mGoodsListLayout, "translationX", 0,
							-mGoodsListLayout.getWidth());
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}
				postHideGoodsListLayoutProcess();

				if (mCategoryLayout.getTranslationX() == 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mCategoryLayout, "translationX", 0,
							-mGoodsListLayout.getWidth());
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					// oa.addListener(new AnimatorListenerAdapter()
					// {
					// @Override
					// public void onAnimationEnd(Animator animation)
					// {
					// animation.removeListener(this);
					// if (listener != null)
					// listener.onAnimationEnd(animation);
					// }
					// });
					oa.start();
				}
			}
		});
	}

	private void hideGoodsListLayout()
	{
		hideGoodsListLayout(null);
	}

	private void hideGoodsListLayout(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mGoodsListLayout.getTranslationX() != 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mGoodsListLayout, "translationX",
							mGoodsListLayout.getTranslationX(), 0f);
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
							if (listener != null)
								listener.onAnimationEnd(animation);

							mGoodsListLayout.setVisibility(View.INVISIBLE);
							// mCategoryMenuIndex = -1;
							mCategoryMenuBar.setCurrentMenu(-1);
						}
					});
					oa.start();
				}
				else
				{
					mGoodsListLayout.setVisibility(View.INVISIBLE);
					// mCategoryMenuIndex = -1;
					mCategoryMenuBar.setCurrentMenu(-1);
				}
				removeHideGoodsListLayoutProcess();

				if (mCategoryLayout.getTranslationX() != 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mCategoryLayout, "translationX",
							mCategoryLayout.getTranslationX(), 0f);
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					// oa.addListener(new AnimatorListenerAdapter()
					// {
					// @Override
					// public void onAnimationEnd(Animator animation)
					// {
					// if (listener != null)
					// listener.onAnimationEnd(animation);
					// }
					// });
					oa.start();
				}
			}
		});
	}

	private HideGoodsListLayoutProcess mHideGoodsListLayoutProcess;

	private void removeHideGoodsListLayoutProcess()
	{
		if (mHideGoodsListLayoutProcess != null)
			AndroidUtils.MainHandler.removeCallbacks(mHideGoodsListLayoutProcess);
	}

	private void postHideGoodsListLayoutProcess()
	{
		removeHideGoodsListLayoutProcess();
		mHideGoodsListLayoutProcess = new HideGoodsListLayoutProcess();
		AndroidUtils.MainHandler.postDelayed(mHideGoodsListLayoutProcess, 8000L);
	}

	private void resetHideGoodsListLayoutProcess()
	{
		if (mHideGoodsListLayoutProcess != null)
			postHideGoodsListLayoutProcess();
	}

	private class HideGoodsListLayoutProcess implements Runnable
	{
		public HideGoodsListLayoutProcess()
		{
		}

		@Override
		public void run()
		{
			hideGoodsListLayout();
		}
	}

	private void showGoodsList(final int category)
	{
		showGoodsListLayout();
		if (mGoodsListView.getTag() != null)
		{
			int curType = (Integer)mGoodsListView.getTag();
			if (curType == category)
			{
				return;
			}
		}
		mGoodsListView.setTag(category);
		mGoodsListAdapter.clear();
		switch (category)
		{
			case R.id.category_women:
			case R.id.category_man:
			case R.id.category_couplering:
			case R.id.category_necklace:
				break;
			default:
				return;
		}
		RequestManager.loadGoodsList(getApplicationContext(), LoginHelper.getUserKey(getApplicationContext()), true,
				new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							GoodsListResponse glResponse = (GoodsListResponse)response;
							if (mGoodsListLayout.getVisibility() == View.VISIBLE)
								makeGoodsList(category, glResponse.datas.list);
						}
						else
						{
							showToast(response.error);
						}
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						showToast("网络错误: " + error.errorCode);
					}
				});
	}

	private int mCurrCategory;

	private void makeGoodsList(final int category, final GoodsItem[] goodsItemList)
	{
		mCurrCategory = category;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mGoodsListAdapter.clear();

				// int count = 0;
				for (final GoodsItem item : goodsItemList)
				{
					if (mCurrCategory != category)
						return;
					boolean needAdd = false;
					if ((Define.TAG_RING.equals(item.tag) && item.model_info == null)
							|| (Define.TAG_COUPLE.equals(item.tag) && item.model_infos == null))
					{
						continue;
					}
					switch (category)
					{
						case R.id.category_women:
							if (Define.TAG_RING.equals(item.tag))
							{
								if ((item.tagname != null && item.tagname.contains(Define.TAGNAME_WOMAN)))
								{
									needAdd = true;
								}
							}
							break;
						case R.id.category_man:
							if (Define.TAG_RING.equals(item.tag))
							{
								if ((item.tagname != null && item.tagname.contains(Define.TAGNAME_MAN)))
								{
									needAdd = true;
								}
							}
							break;
						case R.id.category_couplering:
							if (Define.TAG_COUPLE.equals(item.tag))
							{
								needAdd = true;
							}
							break;
						case R.id.category_necklace:
							if (Define.TAG_RING.equals(item.tag))
							{
								if ((item.tagname != null && item.tagname.contains(Define.TAGNAME_PENDANT)))
								{
									needAdd = true;
								}
							}
							break;
					}
					if (needAdd)
					{
						// AndroidUtils.MainHandler.postDelayed(new Runnable()
						// {
						// @Override
						// public void run()
						// {
						if (mCurrCategory != category)
							return;
						mGoodsListAdapter.addItem(new GoodsAdapterItem(item));
						// }
						// }, count * 10L);
						// count++;
					}
				}
			}
		});
	}

	private void ensureEraseLayout()
	{
		if (mEraseLayout == null)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "erase_layout");
			mEraseLayout = mTryonUI.findViewById(id);
			mEraseLayout.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					return true;
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "ok_button");
			mEraseOkBtn = mTryonUI.findViewById(id);
			mEraseOkBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyChangeSelectedModelState",
							Define.STATE_ACTION);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "progressBar");
			mEraseProgressBar = (ProgressBar)mEraseLayout.findViewById(id);
			mEraseProgressBar.setProgressOrientation(ProgressBar.VERTICAL);
			mEraseProgressBar.setChangeProgressMode(ChangeProgressMode.SLIDE);
			mEraseProgressBar.setOnProgressChangedListener(new OnProgressChangedListener()
			{
				@Override
				public void onProgressChanged(ProgressBar progressBar, float percent)
				{
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetErasureBrushSize", "" + percent);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "erase_edit");
			mEraseEditor = mEraseLayout.findViewById(id);
			mEraseEditor.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetErasureState",
							Define.ERASURE_STATE_EDITOR);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "erase_remove");
			mEraseRemove = mEraseLayout.findViewById(id);
			mEraseRemove.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetErasureState",
							Define.ERASURE_STATE_REMOVE);
				}
			});

			hideEraseLayout();
		}
	}

	View min_hand_sample1;
	View min_hand_sample2;
	View min_hand_sample3;
	View min_hand_sample4;
	View min_hand_sample5;
	View min_hand_man_sample1;
	View min_hand_man_sample2;
	View min_hand_man_sample3;
	View min_hand_man_sample4;
	View min_hand_man_sample5;
	View min_hand_two_sample1;
	View min_hand_two_sample2;
	View min_hand_two_sample3;
	View min_hand_two_sample4;
	View min_hand_two_sample5;
	View min_necklace_sample1;
	int min_hand_selected_type = -1;
	int min_hand_selected_index = -1;
	int min_hand_woman_selected_index = 0;
	int min_hand_man_selected_index = 0;
	int min_hand_two_selected_index = 0;
	int min_necklace_selected_index = 0;

	private void ensureMinHand()
	{
		if (mMinHandLayout == null)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "min_hand_layout");
			mMinHandLayout = mTryonUI.findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "min_hand");
			mMinHand = mMinHandLayout.findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_sample1");
			min_hand_sample1 = mMinHand.findViewById(id);
			min_hand_sample1.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// AndroidUtils.MainHandler.post(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(0, 0, true, null);
					// }
					// });
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_sample2");
			min_hand_sample2 = mMinHand.findViewById(id);
			min_hand_sample2.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// AndroidUtils.MainHandler.post(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(0, 1, true, null);
					// }
					// });
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_sample3");
			min_hand_sample3 = mMinHand.findViewById(id);
			min_hand_sample3.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// AndroidUtils.MainHandler.post(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(0, 2, true, null);
					// }
					// });
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_sample4");
			min_hand_sample4 = mMinHand.findViewById(id);
			min_hand_sample4.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// AndroidUtils.MainHandler.post(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(0, 3, true, null);
					// }
					// });
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_sample5");
			min_hand_sample5 = mMinHand.findViewById(id);
			min_hand_sample5.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// AndroidUtils.MainHandler.post(new Runnable()
					// {
					// @Override
					// public void run()
					// {
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(0, 4, true, null);
					// }
					// });
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_man");
			mMinHandMan = mMinHandLayout.findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_man_sample1");
			min_hand_man_sample1 = mMinHandMan.findViewById(id);
			min_hand_man_sample1.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(1, 0, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_man_sample2");
			min_hand_man_sample2 = mMinHandMan.findViewById(id);
			min_hand_man_sample2.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(1, 1, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_man_sample3");
			min_hand_man_sample3 = mMinHandMan.findViewById(id);
			min_hand_man_sample3.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(1, 2, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_man_sample4");
			min_hand_man_sample4 = mMinHandMan.findViewById(id);
			min_hand_man_sample4.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(1, 3, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_man_sample5");
			min_hand_man_sample5 = mMinHandMan.findViewById(id);
			min_hand_man_sample5.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(1, 4, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_two");
			mMinHandTwo = mMinHandLayout.findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_two_sample1");
			min_hand_two_sample1 = mMinHandTwo.findViewById(id);
			min_hand_two_sample1.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(2, 0, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_two_sample2");
			min_hand_two_sample2 = mMinHandTwo.findViewById(id);
			min_hand_two_sample2.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(2, 1, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_two_sample3");
			min_hand_two_sample3 = mMinHandTwo.findViewById(id);
			min_hand_two_sample3.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(2, 2, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_two_sample4");
			min_hand_two_sample4 = mMinHandTwo.findViewById(id);
			min_hand_two_sample4.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(2, 3, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_hand_two_sample5");
			min_hand_two_sample5 = mMinHandTwo.findViewById(id);
			min_hand_two_sample5.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(2, 4, true, null);
				}
			});

			id = ResourceUtil.getId(getApplicationContext(), "min_necklace");
			mMinNecklace = mMinHandLayout.findViewById(id);

			id = ResourceUtil.getId(getApplicationContext(), "min_necklace_sample1");
			min_necklace_sample1 = mMinNecklace.findViewById(id);
			min_necklace_sample1.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					setMinHandSelect(3, 0, true, null);
				}
			});
		}
	}

	private void setMinHandSelect(final int type, final int index, final boolean selectedPose,
			final OnReadPoseDataCallback callback)
	{
		LogUtil.v(TAG, "setMinHandSelect: " + type + "; " + index + "; " + min_hand_selected_index + "; "
				+ selectedPose);
		// if (min_hand_selected_index == index)
		// return;
		min_hand_selected_type = type;
		min_hand_selected_index = index;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mMinHand.setVisibility(View.GONE);
				mMinHandMan.setVisibility(View.GONE);
				mMinHandTwo.setVisibility(View.GONE);
				mMinNecklace.setVisibility(View.GONE);
				switch (type)
				{
					case 0:// 女戒
						mMinHand.setVisibility(View.VISIBLE);

						min_hand_sample1.setSelected(false);
						min_hand_sample2.setSelected(false);
						min_hand_sample3.setSelected(false);
						min_hand_sample4.setSelected(false);
						min_hand_sample5.setSelected(false);
						min_hand_sample1.setEnabled(true);
						min_hand_sample2.setEnabled(true);
						min_hand_sample3.setEnabled(true);
						min_hand_sample4.setEnabled(true);
						min_hand_sample5.setEnabled(true);

						min_hand_woman_selected_index = index;
						switch (index)
						{
							case 0:
								if (selectedPose)
								{
									min_hand_sample1.setSelected(true);
									min_hand_sample1.setEnabled(false);
									readPoseData("hand_sample1.jpg", "pose1.json", callback);
								}
								break;
							case 1:
								if (selectedPose)
								{
									min_hand_sample2.setSelected(true);
									min_hand_sample2.setEnabled(false);
									readPoseData("hand_sample2.jpg", "pose2.json", callback);
								}
								break;
							case 2:
								if (selectedPose)
								{
									min_hand_sample3.setSelected(true);
									min_hand_sample3.setEnabled(false);
									readPoseData("hand_sample3.jpg", "pose3.json", callback);
								}
								break;
							case 3:
								if (selectedPose)
								{
									min_hand_sample4.setSelected(true);
									min_hand_sample4.setEnabled(false);
									readPoseData("hand_sample4.jpg", "pose4.json", callback);
								}
								break;
							case 4:
								if (selectedPose)
								{
									min_hand_sample5.setSelected(true);
									min_hand_sample5.setEnabled(false);
									readPoseData("hand_sample5.jpg", "pose5.json", callback);
								}
								break;
						}
						break;
					case 1:// 男戒
						mMinHandMan.setVisibility(View.VISIBLE);

						min_hand_man_sample1.setSelected(false);
						min_hand_man_sample2.setSelected(false);
						min_hand_man_sample3.setSelected(false);
						min_hand_man_sample4.setSelected(false);
						min_hand_man_sample5.setSelected(false);
						min_hand_man_sample1.setEnabled(true);
						min_hand_man_sample2.setEnabled(true);
						min_hand_man_sample3.setEnabled(true);
						min_hand_man_sample4.setEnabled(true);
						min_hand_man_sample5.setEnabled(true);

						min_hand_man_selected_index = index;
						switch (index)
						{
							case 0:
								if (selectedPose)
								{
									min_hand_man_sample1.setSelected(true);
									min_hand_man_sample1.setEnabled(false);

									readPoseData("hand_man_sample1.jpg", "pose_man1.json", callback);
								}
								break;
							case 1:
								if (selectedPose)
								{
									min_hand_man_sample2.setSelected(true);
									min_hand_man_sample2.setEnabled(false);

									readPoseData("hand_man_sample2.jpg", "pose_man2.json", callback);
								}
								break;
							case 2:
								if (selectedPose)
								{
									min_hand_man_sample3.setSelected(true);
									min_hand_man_sample3.setEnabled(false);

									readPoseData("hand_man_sample3.jpg", "pose_man3.json", callback);
								}
								break;
							case 3:
								if (selectedPose)
								{
									min_hand_man_sample4.setSelected(true);
									min_hand_man_sample4.setEnabled(false);

									readPoseData("hand_man_sample4.jpg", "pose_man4.json", callback);
								}
								break;
							case 4:
								if (selectedPose)
								{
									min_hand_man_sample5.setSelected(true);
									min_hand_man_sample5.setEnabled(false);

									readPoseData("hand_man_sample5.jpg", "pose_man5.json", callback);
								}
								break;
						}
						break;
					case 2:// 对戒
						mMinHandTwo.setVisibility(View.VISIBLE);

						min_hand_two_sample1.setSelected(false);
						min_hand_two_sample2.setSelected(false);
						min_hand_two_sample3.setSelected(false);
						min_hand_two_sample4.setSelected(false);
						min_hand_two_sample5.setSelected(false);
						min_hand_two_sample1.setEnabled(true);
						min_hand_two_sample2.setEnabled(true);
						min_hand_two_sample3.setEnabled(true);
						min_hand_two_sample4.setEnabled(true);
						min_hand_two_sample5.setEnabled(true);

						min_hand_two_selected_index = index;
						switch (index)
						{
							case 0:
								if (selectedPose)
								{
									min_hand_two_sample1.setSelected(true);
									min_hand_two_sample1.setEnabled(false);

									readPoseData("hand_two_sample1.jpg", "pose_two1.json", callback);
								}
								break;
							case 1:
								if (selectedPose)
								{
									min_hand_two_sample2.setSelected(true);
									min_hand_two_sample2.setEnabled(false);

									readPoseData("hand_two_sample2.jpg", "pose_two2.json", callback);
								}
								break;
							case 2:
								if (selectedPose)
								{
									min_hand_two_sample3.setSelected(true);
									min_hand_two_sample3.setEnabled(false);

									readPoseData("hand_two_sample3.jpg", "pose_two3.json", callback);
								}
								break;
							case 3:
								if (selectedPose)
								{
									min_hand_two_sample4.setSelected(true);
									min_hand_two_sample4.setEnabled(false);

									readPoseData("hand_two_sample4.jpg", "pose_two4.json", callback);
								}
								break;
							case 4:
								if (selectedPose)
								{
									min_hand_two_sample5.setSelected(true);
									min_hand_two_sample5.setEnabled(false);

									readPoseData("hand_two_sample5.jpg", "pose_two5.json", callback);
								}
								break;
						}
						break;
					case 3:// 吊坠
						mMinNecklace.setVisibility(View.VISIBLE);

						min_necklace_sample1.setSelected(false);

						min_necklace_sample1.setEnabled(true);

						min_necklace_selected_index = index;
						switch (index)
						{
							case 0:
								if (selectedPose)
								{
									min_necklace_sample1.setSelected(true);
									min_necklace_sample1.setEnabled(false);

									readPoseData("necklace_sample1.jpg", "pose_necklace1.json", callback);
								}
								break;
						}
						break;
				}
			}
		});
	}

	private void updateEraseStateAndSize(final String state, final float brushSizePercent)
	{
		mEraseState = state;
		mEraseBrushSizePercent = brushSizePercent;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mEraseEditor.setSelected(false);
				mEraseRemove.setSelected(false);
				if (Define.ERASURE_STATE_REMOVE.equals(mEraseState))
				{
					mEraseRemove.setSelected(true);
				}
				else if (Define.ERASURE_STATE_EDITOR.equals(mEraseState))
				{
					mEraseEditor.setSelected(true);
				}
				mEraseProgressBar.setProgress(mEraseBrushSizePercent);
			}
		});
	}

	private void showEraseLayout()
	{
		showEraseLayout(null);
	}

	private void showEraseLayout(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mEraseLayout.setVisibility(View.VISIBLE);
				if (mEraseLayout.getTranslationX() != 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mEraseLayout, "translationX",
							mEraseLayout.getTranslationX(), 0f);
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);

							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}

				mEraseOkBtn.setVisibility(View.VISIBLE);
				if (mEraseOkBtn.getTranslationY() != 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mEraseOkBtn, "translationY",
							mEraseOkBtn.getTranslationY(), 0f);
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);
						}
					});
					oa.start();
				}
			}
		});
	}

	private void hideEraseLayout()
	{
		hideEraseLayout(null);
	}

	private void hideEraseLayout(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mEraseLayout.getTranslationX() == 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mEraseLayout, "translationX", 0f,
							mEraseLayout.getWidth());
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);

							mEraseLayout.setVisibility(View.INVISIBLE);

							if (listener != null)
								listener.onAnimationEnd(animation);
						}
					});
					oa.start();
				}
				else
				{
					if (listener != null)
						listener.onAnimationEnd(null);
				}
				if (mEraseOkBtn.getTranslationY() == 0)
				{
					ObjectAnimator oa = ObjectAnimator.ofFloat(mEraseOkBtn, "translationY", 0f,
							-(mEraseOkBtn.getTop() + mEraseOkBtn.getHeight()));
					ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
					oa.setInterpolator(new LinearInterpolator());
					oa.addListener(new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							animation.removeListener(this);

							mEraseOkBtn.setVisibility(View.INVISIBLE);
						}
					});
					oa.start();
				}
			}
		});
	}

	long millisTime;
	Runnable startMainRunnable;

	private void ensureSplashView()
	{
		startMainRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.v(TAG, "startMainRunnable...");
				setCurrentUI(Define.UI_MAIN_UI, null);
				BaseFragment.add(instance, new LoginFragment(), false);

				// LoginActivity.launchMeForResult(getRootActivity(), 1);
				// BaseFragment.add(instance, new MainFragment(), false);
				// LoginHelper.checkVersion(instance, true);
				// launchTryonScene(instance, "14");
				LoginHelper.checkVersion(instance, true);     
			}
		};

		int id = ResourceUtil.getId(getApplicationContext(), "splash_layout");
		mSplashView = findViewById(id);
		mSplashView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				// 捕获掉触屏事件
				return true;
			}
		});
	}

	private void ensureMainUI()
	{
		if (mMainUI == null)
		{
			// int layout = ResourceUtil.getLayoutId(getApplicationContext(),
			// "default_fragment_layout");
			// mMainUI = View.inflate(getApplicationContext(), layout, null);
			// addContentView(mMainUI, new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.MATCH_PARENT));

			int id = ResourceUtil.getId(getApplicationContext(), "fragment_container");
			mMainUI = findViewById(id);
		}
	}

	private void showMainUI(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mMainUI.setVisibility(View.VISIBLE);
				ObjectAnimator oa = ObjectAnimator.ofFloat(mMainUI, "translationX", mMainUI.getTranslationX(), 0f);
				ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
				oa.setInterpolator(new LinearInterpolator());
				oa.addListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						animation.removeListener(this);
						mTryonUI.setVisibility(View.INVISIBLE);
						if (listener != null)
							listener.onAnimationEnd(animation);
					}
				});
				oa.start();
			}
		});
	}

	private void hideMainUI(final AnimatorListener listener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				ObjectAnimator oa = ObjectAnimator.ofFloat(mMainUI, "translationX", 0f, -mMainUI.getWidth());
				ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
				// oa.setDuration(300L);
				oa.setInterpolator(new LinearInterpolator());
				oa.addListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						animation.removeListener(this);
						mMainUI.setVisibility(View.INVISIBLE);
						if (listener != null)
							listener.onAnimationEnd(animation);
					}
				});
				oa.start();
			}
		});
	}

	public void backToMainUI(final AnimatorListener listener)
	{
		if (mCurrentUI.equals(Define.UI_MAIN_UI))
		{
			if (listener != null)
				listener.onAnimationEnd(null);
			return;
		}
		mBackPressed = true;

		// hideLayerList();
		// shrinkToolBar();
		// hideToolBarLayout();
		// hideCategoryLayout();
		// hideProductList();
		// hideEditLayout();
		// hideModelLayout(false);
		// hideTipsFullscreen();
		// hideTipsDoubleFinger();
		// hideTipsIk();
		// hideTipsMove();

		mScreenShotAfter = ScreenShotAfter.None;
		mCheckedGoodsList.clear();

		hideLoadingIndicator();
		hide3DLoadingIndicator();
		// cancelDownloadingList();
		removeModelLoadListener(mModelLoadListener);

		// doFullscreen(false);
		// AndroidUtils.MainHandler.postDelayed(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		setCurrentUI(Define.UI_MAIN_UI, new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				if (ProductDetailsFragment.instance != null)
					ProductDetailsFragment.instance.attachUnityPlayer(true);
				else
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyBackToWaiting", "");

				mBackPressed = false;
				if (listener != null)
					listener.onAnimationEnd(animation);
			}
		});
		// }
		// }, 1000L);
	}

	// private void cancelDownloadingList()
	// {
	// try
	// {
	// String[] urls = null;
	// synchronized (mDownloadUrlList)
	// {
	// if (!mDownloadUrlList.isEmpty())
	// {
	// urls = new String[mDownloadUrlList.size()];
	// urls = mDownloadUrlList.toArray(urls);
	// }
	// }
	// if (urls != null)
	// for (String url : urls)
	// {
	// FileDownloadHelper.cancelDownload(url);
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	private OnMultiFileDownloadCallback mModelFileDownloadCallback = new OnMultiFileDownloadCallback()
	{
		public void onMultiDownloadStarted(Object tag, FileInfo fileInfo, String localPath, int downloadIndex)
		{
			hideLoadingIndicator();
		}

		public void onMultiDownloadProgress(Object tag, FileInfo fileInfo, String localPath, long count, long length,
				float speed, int downloadIndex)
		{
		}

		public void onMultiDownloadFinished(Object tag, FileInfo fileInfo, String localPath, int downloadIndex)
		{
		}

		public void onMultiDownloadCanceled(Object tag, FileInfo fileInfo, int downloadIndex)
		{
		}

		public void onMultiDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error, int downloadIndex)
		{
			showToast("网络异常:" + error.errorCode);
		}

		public void onMultiAllDownloadFinished(Object tag, FileInfo[] fileInfos, DownloadStatus[] status)
		{
			// showLoadingIndicator();
			boolean loadingIndicatorsShown = false;
			@SuppressWarnings("unchecked")
			List<UnityModelInfo> models = (List<UnityModelInfo>)tag;
			for (int i = 0; i < models.size(); i++)
			{
				UnityModelInfo model = models.get(i);
				if (status[i] == null || status[i] != DownloadStatus.FINISHED)
				{
					String id = model.id;
					if (mSelectedId != null && mSelectedId.equals(id))
						mSelectedId = null;
					mCheckedGoodsList.remove(ensureId(id));
					continue;
				}
				if (!loadingIndicatorsShown)
				{
					showLoadingIndicator(10 * 1000L);
					loadingIndicatorsShown = true;
				}
				// model.printData(TAG, 0);
				String modelJson = UnityModelInfo.toJson(model);
				// mSavedModelMap.put(model.id, model);
				// if (TextUtils.isEmpty(model.replaceId))
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyAddModel", modelJson);
				// else
				// UnityPlayer.UnitySendMessage("PlatformMessageHandler",
				// "NotifyReplaceModel", modelJson);
				// updateCatetorysNum();
				// mProductListAdapter.notifyDataSetChanged();
			}
		}
	};

	private void startModelDownload(List<UnityModelInfo> models)
	{
		// hideLoadingIndicator();
		if (models == null || models.isEmpty())
		{
			hideLoadingIndicator();
			return;
		}
		List<FileInfo> list = new ArrayList<FileInfo>();
		for (UnityModelInfo model : models)
			list.add(model);
		// if (model.textureUrl != null)
		// {
		// for (int i = 0; i < model.textureUrl.length; i++)
		// {
		// FileInfo info = new FileInfo();
		// info.url = model.textureUrl[i];
		// info.filemtime = model.textureFilemtime[i];
		// list.add(info);
		// }
		// }
		FileInfo[] infos = new FileInfo[list.size()];
		infos = list.toArray(infos);
		FileDownloadHelper.startMultiDownload(models, this, infos, mModelFileDownloadCallback, true, true);
	}

	private void addModel(final GoodsItem item)
	{
		LogUtil.d(TAG, "addModel:");
		item.printData(TAG, 0);

		mSelectedGoodsId = item.id;
		mSelectedGoodsTag = item.tag;

		if (mCheckedGoodsList.contains(item.id))
		{
			LogUtil.w(TAG, "the " + item.id + " model has loaded, return");
			if (Define.TAG_RING.equals(item.tag))
			{
				String value = item.id + "@" + Boolean.toString(true);
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
			}
			else if (Define.TAG_COUPLE.equals(item.tag))
			{
				String value = ensureMaleRingId(item.id) + "@" + Boolean.toString(true);
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
				value = ensureFemaleRingId(item.id) + "@" + Boolean.toString(true);
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
			}
			return;
		}

		// cancelDownloadingList();
		OnReadPoseDataCallback callback = new OnReadPoseDataCallback()
		{
			@Override
			public void onCallBack()
			{
				addModel(item);
			}
		};
		if (Define.TAG_RING.equals(item.tag))
		{
			if (item.model_info == null)
			{
				showToast("该商品模型文件异常，请联系管理员");
				return;
			}
			if (!mImageFromCamera)
			{
				if (item.tagname != null && item.tagname.contains(Define.TAGNAME_MAN))// 男戒
				{
					if (mTryonPoseData == null || min_hand_selected_type != 1)
					{
						removeAllModels();
						setMinHandSelect(1, min_hand_man_selected_index, true, callback);
						return;
					}
				}
				else if (item.tagname != null && item.tagname.contains(Define.TAGNAME_PENDANT))// 吊坠
				{
					if (mTryonPoseData == null || min_hand_selected_type != 3)
					{
						removeAllModels();
						setMinHandSelect(3, min_necklace_selected_index, true, callback);
						return;
					}
				}
				else
				{
					if (mTryonPoseData == null || min_hand_selected_type != 0)// 女戒
					{
						removeAllModels();
						setMinHandSelect(0, min_hand_woman_selected_index, true, callback);
						return;
					}
				}
			}
			else
			{
				if (item.tagname != null && item.tagname.contains(Define.TAGNAME_MAN))// 男戒
				{
					if (min_hand_selected_type != 1)
					{
						setMinHandSelect(1, min_hand_man_selected_index, false, null);
					}
				}
				else if (item.tagname != null && item.tagname.contains(Define.TAGNAME_PENDANT))// 吊坠
				{
					if (min_hand_selected_type != 3)
					{
						setMinHandSelect(3, min_necklace_selected_index, false, null);
					}
				}
				else
				{
					if (min_hand_selected_type != 0)// 女戒
					{
						setMinHandSelect(0, min_hand_woman_selected_index, false, null);
					}
				}
			}
			UnityModelInfo model = null;
			if (!mCheckedGoodsList.isEmpty())
			{
				for (String id : mCheckedGoodsList)
				{
					GoodsItem goodsItem = mGoodsItemMap.get(id);
					if (Define.TAG_RING.equals(goodsItem.tag))
					{
						String value = id + "@" + Boolean.toString(false);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
						if (mImageFromCamera || item.tagname.equals(goodsItem.tagname))
						{
							model = new UnityModelInfo(item.model_info);
							model.needReadRecord = 0;
						}
					}
					else if (Define.TAG_COUPLE.equals(goodsItem.tag))
					{
						String value = ensureMaleRingId(id) + "@" + Boolean.toString(false);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
						value = ensureFemaleRingId(id) + "@" + Boolean.toString(false);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
					}
				}
			}
			// if (mCheckedGoodsList.isEmpty())
			int tryonType = Config.Type_Ring;
			if (item.tagname != null && item.tagname.contains(Define.TAGNAME_PENDANT))
				tryonType = Config.Type_Necklace;
			if (model == null && mTryonPoseData != null)
			{
				for (UnityModelInfo info : mTryonPoseData.modelList)
				{
					if (info.type == tryonType)
					{
						model = info;
						model.copyFrom(item.model_info);
						model.needReadRecord = 1;
						break;
					}
				}
			}
			if (model == null)
				model = new UnityModelInfo(item.model_info);
			model.id = item.id;
			model.type = tryonType;

			LogUtil.v(TAG, "check model...");
			model.printData(TAG, 0);

			List<UnityModelInfo> models = new ArrayList<UnityModelInfo>();
			models.add(model);
			startModelDownload(models);

			// if (!mGoodsItemMap.containsKey(item.id))
			mGoodsItemMap.put(item.id, item);
			if (!mCheckedGoodsList.contains(item.id))
				mCheckedGoodsList.add(item.id);
		}
		else if (Define.TAG_COUPLE.equals(item.tag))
		{
			if (item.model_infos == null || item.model_infos.men == null || item.model_infos.wmen == null)
			{
				showToast("该商品模型文件异常，请联系管理员");
				return;
			}
			if (!mImageFromCamera)
			{
				if (mTryonPoseData == null || min_hand_selected_type != 2)
				{
					removeAllModels();
					setMinHandSelect(2, min_hand_two_selected_index, true, callback);
					return;
				}
			}
			else
			{
				if (min_hand_selected_type != 2)
				{
					setMinHandSelect(2, min_hand_two_selected_index, false, null);
				}
			}
			UnityModelInfo[] models = new UnityModelInfo[2];
			if (!mCheckedGoodsList.isEmpty())
			{
				for (String id : mCheckedGoodsList)
				{
					GoodsItem goodsItem = mGoodsItemMap.get(id);
					if (Define.TAG_RING.equals(goodsItem.tag))
					{
						String value = id + "@" + Boolean.toString(false);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
					}
					else if (Define.TAG_COUPLE.equals(goodsItem.tag))
					{
						models[0] = new UnityModelInfo();
						models[0].needReadRecord = 0;
						String value = ensureMaleRingId(id) + "@" + Boolean.toString(false);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);

						models[1] = new UnityModelInfo();
						models[1].needReadRecord = 0;
						value = ensureFemaleRingId(id) + "@" + Boolean.toString(false);
						UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySetModelVisible", value);
					}
				}
			}
			List<UnityModelInfo> modelList = new ArrayList<UnityModelInfo>();
			for (int i = 0; i < models.length; i++)
			{
				UnityModelInfo model = models[i];
				// if (mCheckedGoodsList.isEmpty())
				if (model == null && mTryonPoseData != null)
				{
					for (UnityModelInfo info : mTryonPoseData.modelList)
					{
						switch (i)
						{
							case 0:
								if (info.type == Config.Type_CoupleRing_Male)
								{
									model = info;
									model.copyFrom(item.model_infos.men);
									model.needReadRecord = 1;
									break;
								}
								break;
							case 1:
								if (info.type == Config.Type_CoupleRing_FeMale)
								{
									model = info;
									model.copyFrom(item.model_infos.wmen);
									model.needReadRecord = 1;
									break;
								}
								break;
						}
					}
				}
				if (model == null)
				{
					switch (i)
					{
						case 0:
							model = new UnityModelInfo(item.model_infos.men);
							break;
						case 1:
							model = new UnityModelInfo(item.model_infos.wmen);
							break;
					}
				}
				switch (i)
				{
					case 0:
						model.id = ensureMaleRingId(item.id);
						model.type = Config.Type_CoupleRing_Male;
						break;
					case 1:
						model.id = ensureFemaleRingId(item.id);
						model.type = Config.Type_CoupleRing_FeMale;
						break;
				}

				LogUtil.v(TAG, "check model...");
				model.printData(TAG, 0);

				modelList.add(model);
			}
			startModelDownload(modelList);
			mGoodsItemMap.put(item.id, item);
			if (!mCheckedGoodsList.contains(item.id))
				mCheckedGoodsList.add(item.id);
		}
		else
		{
			showToast("该商品数据异常，请联系管理员");
		}
	}

	private void removeModel(String id)
	{
		LogUtil.d(TAG, "removeModel: " + id);
		mCheckedGoodsList.remove(id);
		// mCheckedTextureMap.remove(goods_id);
		// mCheckedFaceMap.remove(goods_id);
		// mSavedModelMap.remove(goods_id);
		UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyRemoveModel", id);
		// updateLayerTotalPrice();
	}

	private void removeAllModels()
	{
		LogUtil.d(TAG, "removeAllModels...");
		mCheckedGoodsList.clear();
		UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyRemoveAllModels", "");
	}

	public void onUnityStarted()
	{
		LogUtil.d(TAG, "onUnityStarted...");
		long time = System.currentTimeMillis() - millisTime;
		long delay = 5000L - time;
		if (delay > 0)
		{
			AndroidUtils.MainHandler.postDelayed(startMainRunnable, 5000L - time);
		}
		else
		{
			AndroidUtils.MainHandler.post(startMainRunnable);
		}
	}

	/**
	 * Unity通知平台Unity场景加载已完成
	 *
//	 * @param string scene 场景名称
	 */
	public void onSceneStarted(String scene)
	{
		LogUtil.d(TAG, "onSceneStarted: " + scene);
		mNeedScreenshot = true;
		if (scene.equals(Define.Scene_CombineTryOn))
		{
			OnReadPoseDataCallback callback = new OnReadPoseDataCallback()
			{
				@Override
				public void onCallBack()
				{
					loadStartFrom();
				}
			};
			// if (mTryonPoseData == null)
			// {
			// if (mStartFrom != null)
			// {
			// switch (mStartFrom)
			// {
			// case WomanRing:
			// setMinHandSelect(min_hand_woman_selected_index, callback);
			// break;
			// case ManRing:
			// setMinHandSelect(min_hand_man_selected_index, callback);
			// break;
			// case CoupleRing:
			// setMinHandSelect(min_hand_two_selected_index, callback);
			// break;
			// }
			// }
			// else
			// setMinHandSelect(0, callback);
			// }
			// else
			// {
			// boolean changed = false;
			// if (mStartFrom != null)
			// {
			// switch (mStartFrom)
			// {
			// case WomanRing:
			// if (mTryonPoseData == null || min_hand_selected_index < 0 || min_hand_selected_index > 2)
			// {
			// setMinHandSelect(min_hand_woman_selected_index, callback);
			// changed = true;
			// }
			// break;
			// case ManRing:
			// if (mTryonPoseData == null || min_hand_selected_index != 3)
			// {
			// setMinHandSelect(min_hand_man_selected_index, callback);
			// changed = true;
			// }
			// break;
			// case CoupleRing:
			// if (mTryonPoseData == null || min_hand_selected_index != 4)
			// {
			// setMinHandSelect(min_hand_two_selected_index, callback);
			// changed = true;
			// }
			// break;
			// }
			// }
			// if (!changed && mTryonPoseData == null)
			// {
			// setMinHandSelect(0, callback);
			// changed = true;
			// }
			// if (!changed)
			// {
			// mTryonPoseData.printData(MainActivity.TAG, 0);
			//
			// UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyLoadImage",
			// UnityImageInfo.toJson(mTryonPoseData.PERSON_IMAGE));
			//
			// loadStartFrom();
			// }
			// }
			boolean changed = false;
			if (mStartFrom != null)
			{
				switch (mStartFrom)
				{
					case WomanRing:
						if (mTryonPoseData == null || min_hand_selected_type != 0)
						{
							setMinHandSelect(0, min_hand_woman_selected_index, true, callback);
							changed = true;
						}
						break;
					case ManRing:
						if (mTryonPoseData == null || min_hand_selected_type != 1)
						{
							setMinHandSelect(1, min_hand_man_selected_index, true, callback);
							changed = true;
						}
						break;
					case CoupleRing:
						if (mTryonPoseData == null || min_hand_selected_type != 2)
						{
							setMinHandSelect(2, min_hand_two_selected_index, true, callback);
							changed = true;
						}
					case Necklace:
						if (mTryonPoseData == null || min_hand_selected_type != 3)
						{
							setMinHandSelect(3, min_necklace_selected_index, true, callback);
							changed = true;
						}
						break;
				}
			}
			if (!changed && mTryonPoseData == null)
			{
				setMinHandSelect(0, 0, true, callback);
				changed = true;
			}
			if (!changed)
			{
				mTryonPoseData.printData(MainActivity.TAG, 0);

				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyLoadImage",
						UnityImageInfo.toJson(mTryonPoseData.PERSON_IMAGE));

				loadStartFrom();
			}
		}
		else if (scene.equals(Define.Scene_3DShow))
		{
			loadStartFrom();
		}
	}

	private interface OnReadPoseDataCallback
	{
		public void onCallBack();
	}

	private void readPoseData(final String handName, final String handPose, final OnReadPoseDataCallback callback)
	{
		LogUtil.v(TAG, "readPoseData: " + handName + "; " + handPose);
		String path = Loader.PROTOCOL_ASSETS + handPose;
		showLoadingIndicator();
		UrlLoader.getDefault().startLoad(getApplicationContext(), path, null,
				new BaseJsonParser(getApplicationContext())
				{
					@Override
					public void onError(String url, LoadParams params, ErrorInfo error)
					{
						hideLoadingIndicator();
						if (callback != null)
							callback.onCallBack();
						// loadStartFrom();
					}

					@Override
					public void onJsonParse(String json, String url, String cacheKey, LoadParams params, DataFrom from)
					{
						hideLoadingIndicator();
						mTryonPoseData = TryonPoseData.fromJson(json);

						FileInfo fileInfo = new FileInfo();
						fileInfo.url = Loader.PROTOCOL_ASSETS + handName;
						// try
						// {
						// PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
						// fileInfo.filemtime = pi.versionCode;
						// }
						// catch (NameNotFoundException e)
						// {
						// e.printStackTrace();
						// }
						fileInfo.filemtime = Config.TryonData_Version;
						FileDownloadHelper.checkAndDownloadIfNeed(instance, mTryonPoseData, fileInfo,
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
										fileInfo.path = localPath;
										mTryonPoseData.PERSON_IMAGE.copyFrom(fileInfo);
										mTryonPoseData.printData(MainActivity.TAG, 0);

										mImageFromCamera = false;
										UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyLoadImage",
												UnityImageInfo.toJson(mTryonPoseData.PERSON_IMAGE));

										// loadStartFrom();
									}

									@Override
									public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
									{
										// loadStartFrom();
									}

									@Override
									public void onDownloadCanceled(Object tag, FileInfo fileInfo)
									{
									}
								}, false);

						if (callback != null)
							callback.onCallBack();
					}
				}, null);
	}

	private void loadStartFrom()
	{
		if (mStartFromData != null)
		{
			GoodsItem item = new GoodsItem();
			switch (mStartFrom)
			{
				case WomanRing:
					item.copyFrom((GoodsDetailResponse.GoodsDetail)mStartFromData);
					item.tagname = Define.TAGNAME_WOMAN;
					break;
				case ManRing:
					item.copyFrom((GoodsDetailResponse.GoodsDetail)mStartFromData);
					item.tagname = Define.TAGNAME_MAN;
					break;
				case CoupleRing:
					item.copeFrom((CoupleRingDetailResponse.GoodsDetail)mStartFromData);
					break;
				case Necklace:
					item.copyFrom((GoodsDetailResponse.GoodsDetail)mStartFromData);
					item.tagname = Define.TAGNAME_PENDANT;
					break;
			}
			addModel(item);
			mStartFromData = null;
		}
	}

	/**
	 * Unity通知平台当前UI状态已改变
	 * 
//	 * @param string state 状态定义见Consts类"UI状态定义"
	 *
	 */
	public void onUIStateChanged(final String state)
	{
		LogUtil.v(TAG, "onUIStateChanged: " + state + "; " + mUIState);
		if (mUIState != null && mUIState.equals(state))
			return;
		mNeedScreenshot = true;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mUIState = state;
				if (state.equals(Define.STATE_NORMAL))
				{
					showToolbar();
					showCategoryLayout();
					hideEraseLayout();
				}
				else if (state.equals(Define.STATE_ACTION))
				{
					showToolbar();
					showCategoryLayout();
					hideEraseLayout();
				}
				else if (state.equals(Define.STATE_ERASURE))
				{
					hideToolbar();
					hideCategoryLayout();
					showEraseLayout();
				}
			}
		});
	}

	/**
	 * Unity通知平台全屏状态变化
	 * 
//	 * @param bool   fullScreen 是否全屏
	 *
	 */
	public void onFullScreenChanged(boolean fullScreen)
	{
		LogUtil.v(TAG, "onFullScreenChanged: " + fullScreen);
	}

	public void onIKBounds(String bodyBounds, String btnBounds)
	{
	}

	public void onModelTouched(String id, float screenX, float screenY)
	{
	}

	public void onSelectedModelChanged(String id)
	{
		LogUtil.v(TAG, "onSelectedModelChanged: " + id + "; " + mSelectedId);
		mNeedScreenshot = true;
		// mCombinePublished = false;
		if (mSelectedId != null && mSelectedId.equals(id))
			return;
		mSelectedId = id;
	}

	public void onModelRemoved(String id)
	{
		LogUtil.v(TAG, "onModelRemoved: " + id + "; " + mSelectedId);
		mNeedScreenshot = true;
		if (mSelectedId != null && mSelectedId.equals(id))
			mSelectedId = null;
		mCheckedGoodsList.remove(ensureId(id));
	}

	public void onModelLayerChanged(String id, int layer)
	{
		LogUtil.v(TAG, "onModelLayerChanged: " + id + ", " + layer);
		mNeedScreenshot = true;
	}

	public interface OnModelLoadListener
	{
		public void onModelLoadStarted(String id);

		public void onModelLoadProgress(String id, float progress);

		public void onModelLoadFinished(String id, int layer, String faceTag, boolean isClothes);

		public void onModelLoadFailed(String id, String error);
	}

	private List<OnModelLoadListener> mModelLoadListeners = new ArrayList<MainActivity.OnModelLoadListener>();

	public void addModelLoadListener(OnModelLoadListener listener)
	{
		mModelLoadListeners.add(listener);
	}

	public void removeModelLoadListener(OnModelLoadListener listener)
	{
		mModelLoadListeners.remove(listener);
	}

	private void notifyOnModelLoadStarted(String id)
	{
		synchronized (mModelLoadListeners)
		{
			for (OnModelLoadListener listener : mModelLoadListeners)
			{
				listener.onModelLoadStarted(id);
			}
		}
	}

	private void notifyOnModelLoadProgress(String id, float progress)
	{
		synchronized (mModelLoadListeners)
		{
			for (OnModelLoadListener listener : mModelLoadListeners)
			{
				listener.onModelLoadProgress(id, progress);
			}
		}
	}

	private void notifyOnModelLoadFinished(String id, int layer, String faceTag, boolean isClothes)
	{
		synchronized (mModelLoadListeners)
		{
			for (OnModelLoadListener listener : mModelLoadListeners)
			{
				listener.onModelLoadFinished(id, layer, faceTag, isClothes);
			}
		}
	}

	private void notifyOnModelLoadFailed(String id, String error)
	{
		synchronized (mModelLoadListeners)
		{
			for (OnModelLoadListener listener : mModelLoadListeners)
			{
				listener.onModelLoadFailed(id, error);
			}
		}
	}

	public void onModelLoadStarted(String id)
	{
		LogUtil.v(TAG, "onModelLoadStarted: " + id);
		notifyOnModelLoadStarted(id);
	}

	public void onModelLoadProgress(String id, float progress)
	{
		LogUtil.v(TAG, "onModelLoadProgress: " + id + "; " + progress);
		notifyOnModelLoadProgress(id, progress);
	}

	public void onModelLoadFinished(String id, int layer, String faceTag, boolean isClothes)
	{
		LogUtil.v(TAG, "onModelLoadFinished: " + id + "; " + layer + "; " + faceTag + ";" + isClothes);
		mNeedScreenshot = true;
		notifyOnModelLoadFinished(id, layer, faceTag, isClothes);
	}

	public void onModelLoadFailed(String id, String error)
	{
		LogUtil.v(TAG, "onModelLoadFailed: " + id + "; " + error);
		notifyOnModelLoadFailed(id, error);
	}

	public void onImageLoadFinished(String tag, String path)
	{
		LogUtil.v(TAG, "onImageLoadFinished: " + tag + "; " + path);
		mNeedScreenshot = true;
		if (tag.equals(Define.TAG_PERSON_IMAGE))
		{
			UnityModelInfo model;
			for (String id : mCheckedGoodsList)
			{
				GoodsItem item = mGoodsItemMap.get(id);
				if (Define.TAG_RING.equals(item.tag))
				{
					int tryonType = Config.Type_Ring;
					if (item.tagname != null && item.tagname.contains(Define.TAGNAME_PENDANT))
						tryonType = Config.Type_Necklace;
					for (UnityModelInfo info : mTryonPoseData.modelList)
					{
						if (info.type == tryonType)
						{
							model = info;
							model.copyFrom(item.model_info);
							model.needReadRecord = 1;
							model.id = item.id;
							UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyUpdataModelData",
									UnityModelInfo.toJson(model));
							break;
						}
					}
				}
				else if (Define.TAG_COUPLE.equals(item.tag))
				{
					for (UnityModelInfo info : mTryonPoseData.modelList)
					{
						if (info.type == Config.Type_CoupleRing_Male)
						{
							model = info;
							model.copyFrom(item.model_info);
							model.needReadRecord = 1;
							model.id = ensureMaleRingId(item.id);
							UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyUpdataModelData",
									UnityModelInfo.toJson(model));
						}
						else if (info.type == Config.Type_CoupleRing_FeMale)
						{
							model = info;
							model.copyFrom(item.model_info);
							model.needReadRecord = 1;
							model.id = ensureFemaleRingId(item.id);
							UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyUpdataModelData",
									UnityModelInfo.toJson(model));
						}
					}
				}
			}
		}
	}

	public void onImageRemoved(String tag, String url)
	{
		LogUtil.v(TAG, "onImageRemoved: " + tag + "; " + url);
	}

	public void onModelVisibleChanged(String id, boolean visible)
	{
		LogUtil.v(TAG, "onModelVisibleChanged: " + id + "; " + visible);
	}

	public void onModelMaterialChanged(String id, String materialTag)
	{
		LogUtil.v(TAG, "onModelMaterialChanged: " + id + "; " + materialTag);
	}

	public void onSaveModelActData(String json)
	{
		LogUtil.v(TAG, "onSaveModelActData...");
	}

	public void onSaveTryonData(String json)
	{
		LogUtil.v(TAG, "onSaveTryonData...");
		final TryonPoseData poseData = TryonPoseData.fromJson(json);
		poseData.printData(TAG, 0);
	}

	public String newScreenshotFile()
	{
		String name = FileUtils.makeNameInCurrentTime();
		File file = FileUtils.getFile(getApplicationContext(), "screenshot", name + ".png");
		return file.getAbsolutePath();
	}

	public void onScreenShotSucceed(String path)
	{
		LogUtil.d(TAG, "onScreenShotSucceed: " + path + "; " + mScreenShotAfter);
		mNeedScreenshot = false;
		AndroidUtils.sendScanFileBroadcast(this, path);
		hideLoadingIndicatorDialog();
		mScreenshotPath = path;
		showToast("截屏成功,文件保存在:" + mScreenshotPath);
		switch (mScreenShotAfter)
		{
			case None:
				break;
			case Save:
			case Share:
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifySaveTryonData", "");
				break;
			default:
				break;
		}
	}

	public void onScreenShotFailed(int errorCode)
	{
		LogUtil.d(TAG, "onScreenShotFailed: " + errorCode);
		hideLoadingIndicatorDialog();
		mScreenshotPath = null;
		switch (errorCode)
		{
			case 0:
				showToast("截屏失败!");
				break;
			case 1:
				showToast("截屏出错,文件保存失败!");
				break;
		}
	}

	public void onTryonObjectAttributeChanged(String attr, float percent)
	{
		LogUtil.v(TAG, "onTryonObjectAttributeChanged: " + attr + "; " + percent);
		mNeedScreenshot = true;
	}

	public void onErasureStateChanged(String state)
	{
		LogUtil.v(TAG, "onErasureStateChanged: " + state);
		updateEraseStateAndSize(state, mEraseBrushSizePercent);
	}

	public void onErasureBrushSizeChanged(float percent)
	{
		LogUtil.v(TAG, "onErasureBrushSizeChanged: " + percent);
		updateEraseStateAndSize(mEraseState, percent);
	}

	private static String ensureId(String id)
	{
		String result = id;
		if (id.contains("_"))
		{
			int idx = id.indexOf("_");
			result = id.substring(0, idx);
			LogUtil.i(TAG, "ensureId: " + id + " to " + result);
		}
		return result;
	}

	private static String ensureMaleRingId(String id)
	{
		return id + "_" + Config.Type_CoupleRing_Male;
	}

	private static String ensureFemaleRingId(String id)
	{
		return id + "_" + Config.Type_CoupleRing_FeMale;
	}

	/** 进入试衣间 */
	public static void launchTryonScene(Activity activity, GoodsDetailResponse.GoodsDetail detail, String materialTag)
	{
		if (detail == null)
		{
			mStartFrom = StartFrom.NONE;
			mStartFromData = null;
		}
		else
		{
			mStartFrom = StartFrom.WomanRing;
			if (detail.tagname != null)
			{
				if (detail.tagname.contains(Define.TAGNAME_MAN))
					mStartFrom = StartFrom.ManRing;
				else if (detail.tagname.contains(Define.TAGNAME_PENDANT))
					mStartFrom = StartFrom.Necklace;
			}
			mStartFromData = detail;
		}
		mSelectedMaterialTag = materialTag;
		if (instance != null)
			instance.setCurrentUI(Define.UI_TRYON_UI, null);
		if (activity != instance)
			activity.finish();
	}

	// static String GoodsId;

	/** 进入试衣间 */
	public static void launchTryonScene(final Activity activity, String goodsId, final String materialTag)
	{
		LogUtil.v(TAG, "launchTryonScene: " + goodsId + "; " + materialTag);
		// GoodsId = goodsId;
		if (!TextUtils.isEmpty(goodsId))
		{
			((IActivityExtend)activity).showLoadingIndicator();
			RequestManager.loadGoodsDetail(activity, LoginHelper.getUserKey(activity), goodsId, new RequestCallback()
			{
				@Override
				public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
				{
					((IActivityExtend)activity).hideLoadingIndicator();
					if (response.resultCode == BaseResponse.RESULT_OK)
					{
						GoodsDetailResponse gdResponse = (GoodsDetailResponse)response;
						launchTryonScene(activity, gdResponse.datas, materialTag);
					}
					else
					{
						((IActivityExtend)activity).showToast(response.error);
					}
				}

				@Override
				public void onRequestError(int requestCode, long taskId, ErrorInfo error)
				{
					((IActivityExtend)activity).hideLoadingIndicator();
					((IActivityExtend)activity).showToast("网络错误: " + error.errorCode);
				}
			}, CacheMode.PERFER_MEMORY_OR_FILE);
			return;
		}
		if (instance != null)
			instance.setCurrentUI(Define.UI_TRYON_UI, null);
		if (activity != instance)
			activity.finish();
	}

	public static void launchTryonScene(final Activity activity, String goodsId)
	{
		launchTryonScene(activity, goodsId, Define.MATERIAL_WHITE_KGOLD);
	}

	/** 进入试衣间 */
	public static void launchTryonSceneWithCoupleRing(Activity activity, CoupleRingDetailResponse.GoodsDetail detail,
			String materialTag)
	{
		if (detail == null)
		{
			mStartFrom = StartFrom.NONE;
			mStartFromData = null;
		}
		else
		{
			mStartFrom = StartFrom.CoupleRing;
			mStartFromData = detail;
		}
		mSelectedMaterialTag = materialTag;
		if (instance != null)
			instance.setCurrentUI(Define.UI_TRYON_UI, null);
		if (activity != instance)
			activity.finish();
	}

	/** 进入试衣间 */
	public static void launchTryonSceneWithCoupleRing(final Activity activity, String goodsId, final String materialTag)
	{
		LogUtil.v(TAG, "launchTryonSceneWithCoupleRing: " + goodsId + "; " + materialTag);
		if (!TextUtils.isEmpty(goodsId))
		{
			((IActivityExtend)activity).showLoadingIndicator();
			RequestManager.loadCoupleRingDetail(activity, LoginHelper.getUserKey(activity), goodsId,
					new RequestCallback()
					{
						@Override
						public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
						{
							((IActivityExtend)activity).hideLoadingIndicator();
							if (response.resultCode == BaseResponse.RESULT_OK)
							{
								CoupleRingDetailResponse crdResponse = (CoupleRingDetailResponse)response;
								launchTryonSceneWithCoupleRing(activity, crdResponse.datas, materialTag);
							}
							else
							{
								((IActivityExtend)activity).showToast(response.error);
							}
						}

						@Override
						public void onRequestError(int requestCode, long taskId, ErrorInfo error)
						{
							((IActivityExtend)activity).hideLoadingIndicator();
							((IActivityExtend)activity).showToast("网络错误: " + error.errorCode);
						}
					}, CacheMode.PERFER_MEMORY_OR_FILE);
			return;
		}
		if (instance != null)
			instance.setCurrentUI(Define.UI_TRYON_UI, null);
		if (activity != instance)
			activity.finish();
	}

	public static void launchTryonSceneWithCoupleRing(final Activity activity, String goodsId)
	{
		launchTryonSceneWithCoupleRing(activity, goodsId, Define.MATERIAL_WHITE_KGOLD);
	}

	/** 返回首页 */
	public static void backToHomepage(Activity activity, int index, AnimatorListener listener)
	{
		if (activity != instance)
			activity.finish();
		if (instance != null)
			instance.backToHomepage(index, listener);
	}

	/** 返回首页 */
	public static void backToHomepage(Activity activity, int index)
	{
		backToHomepage(activity, index, null);
	}

	/** 返回首页 */
	public static void backToHomepage(Activity activity)
	{
		backToHomepage(activity, 0);
	}

	public void backToHomepage(final int index, final AnimatorListener listener)
	{
		backToMainUI(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				if (listener != null)
					listener.onAnimationEnd(animation);
				LogUtil.d(TAG, "backToHomepage: " + getSupportFragmentManager().getBackStackEntryCount());
				if (getSupportFragmentManager().getBackStackEntryCount() > 0)
				{
					getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
				if (MainFragment.instance != null)
					MainFragment.instance.setCurrentMenu(index);
			}
		});
	}

	private class GoodsAdapterItem extends AbsAdapterItem
	{
		private GoodsItem mItem;

		public GoodsAdapterItem(GoodsItem item)
		{
			mItem = item;
			// mItem.printData(TAG, 0);
		}

		@Override
		public View onCreateView(ViewGroup parent, int position)
		{
			int layout = ResourceUtil.getLayoutId(getApplicationContext(), "tryon_goods_item");
			View view = View.inflate(getApplicationContext(), layout, null);
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			ExtendImageView image = (ExtendImageView)view.findViewById(id);
			image.setAutoRecyleBitmap(true);
			ViewTools.autoFitViewDimension(view, parent, FitMode.FIT_IN_PARENT_WIDTH, 1);
			return view;
		}

		@Override
		public void onBindView(BaseViewHolder holder, View view, int position)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			ExtendImageView image = (ExtendImageView)view.findViewById(id);
			image.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					addModel(mItem);
				}
			});
		}

		@Override
		public void onViewAttachedToWindow(BaseViewHolder holder, View view)
		{
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			ExtendImageView image = (ExtendImageView)view.findViewById(id);
			// mItem.printData(TAG, 0);
			if (mItem != null && mItem.thumb != null)
				image.setImageDataSource(mItem.thumb.url, mItem.thumb.filemtime, DecodeMode.FIT_WIDTH);
			image.startImageLoad(false);

			id = ResourceUtil.getId(getApplicationContext(), "name");
			TextView name = (TextView)view.findViewById(id);
			name.setText(mItem.title);
		}
	}
}
