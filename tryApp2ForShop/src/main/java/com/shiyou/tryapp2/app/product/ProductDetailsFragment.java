package com.shiyou.tryapp2.app.product;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.loader.Loader.CacheMode;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendImageView;
import android.extend.widget.FragmentContainer;
import android.extend.widget.MenuBar;
import android.extend.widget.MenuBar.OnMenuListener;
import android.extend.widget.MenuView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BasePagerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.FileDownloadHelper;
import com.shiyou.tryapp2.FileDownloadHelper.DownloadStatus;
import com.shiyou.tryapp2.FileDownloadHelper.OnMultiFileDownloadCallback;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainActivity.OnModelLoadListener;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.UnityModelInfo;
import com.shiyou.tryapp2.data.db.BrowseHistoryDBHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse.GoodsDetail;
import com.unity3d.player.UnityPlayer;

public class ProductDetailsFragment extends BaseFragment implements OnModelLoadListener
{
	private String goodsId;
	// private String jiaJson;
	private String tag = Define.TAG_RING;
	private boolean isShop;
	private int type;
	private boolean hasModelInfo;
	private float[] weightRange;
	private int[] priceRange;

	private View mProductDetails;
	private View mDetailMiddleLayout;
	 private View mDetailRightLayout;


	View product_photo;
	View arrow_left;
	View arrow_right;
	FrameLayout unity_container;
	ImageView mUnityViewConver;
	LinearLayout photo_show;

	private boolean m3DShow = false;
	MenuBar product_details_3d;
	int mCurrentMenuIndex = -1;
	String mSelectedMaterialTag;
	String url;
//	View product_details_photo;
	// View product_details_3d_bt;
	// View loading_indicator;
	// private List<String> mDownloadUrlList = Collections.synchronizedList(new ArrayList<String>());

	// private HorizontalScrollListView selectedList;
	// private BaseAdapter<AbsAdapterItem> selectedListAdapter;

	public static ProductDetailsFragment instance = null;

	public GoodsDetailResponse mGDResponse = null;
	public CoupleRingDetailResponse mCoupleRingDetailResponse = null;
	private ShopProductDetailsFragment mShopProductDetailsFragment;
	private CoupleRingsDetailsFragment mCoupleRingsDetailsFragment;

	// private int tryonType = Config.Type_Ring;

	// private ExtendImageView productImage;

	// boolean flag = false;

	ProductImagePopupWindow mPopupWindow;

	// public ProductDetailsFragment(String goosId) {
	// this.goosId = goosId;
	// }

	public ProductDetailsFragment(String tag, String goodsId, boolean isShop, boolean hasModelInfo,
			float[] weightRange, int[] priceRange)
	{
		LogUtil.d(TAG, "ProductDetailsFragment: " + tag + "; " + goodsId + "; " + isShop + "; " + hasModelInfo);
		LogUtil.v(TAG, "ProductDetailsFragment weightRange: "
				+ (weightRange == null ? "null" : (weightRange[0] + "-" + weightRange[1])));
		LogUtil.v(TAG, "ProductDetailsFragment priceRange: "
				+ (priceRange == null ? "null" : (priceRange[0] + "-" + priceRange[1])));
		this.tag = tag;
		this.goodsId = goodsId;
		this.isShop = isShop;
		this.hasModelInfo = hasModelInfo;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
	}
	public ProductDetailsFragment(String tag, String goodsId, boolean isShop, boolean hasModelInfo,
								  float[] weightRange, int[] priceRange,int type)
	{
		LogUtil.d(TAG, "ProductDetailsFragment: " + tag + "; " + goodsId + "; " + isShop + "; " + hasModelInfo);
		LogUtil.v(TAG, "ProductDetailsFragment weightRange: "
				+ (weightRange == null ? "null" : (weightRange[0] + "-" + weightRange[1])));
		LogUtil.v(TAG, "ProductDetailsFragment priceRange: "
				+ (priceRange == null ? "null" : (priceRange[0] + "-" + priceRange[1])));
		this.tag = tag;
		this.goodsId = goodsId;
		this.isShop = isShop;
		this.hasModelInfo = hasModelInfo;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
		this.type=type;
	}


	public ProductDetailsFragment(String tag, String goodsId, boolean isShop, boolean hasModelInfo,
								  float[] weightRange, int[] priceRange,String url)
	{
		LogUtil.d(TAG, "ProductDetailsFragment: " + tag + "; " + goodsId + "; " + isShop + "; " + hasModelInfo);
		LogUtil.v(TAG, "ProductDetailsFragment weightRange: "
				+ (weightRange == null ? "null" : (weightRange[0] + "-" + weightRange[1])));
		LogUtil.v(TAG, "ProductDetailsFragment priceRange: "
				+ (priceRange == null ? "null" : (priceRange[0] + "-" + priceRange[1])));
		this.tag = tag;
		this.goodsId = goodsId;
		this.isShop = isShop;
		this.hasModelInfo = hasModelInfo;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
		this.url=url;
	}

	public ProductDetailsFragment(String tag, String goodsId, boolean isShop, boolean hasModelInfo)
	{
		this(tag, goodsId, isShop, hasModelInfo, null, null);
	}

	public ProductDetailsFragment(String tag, String goodsId, boolean isShop, boolean hasModelInfo,String url)
	{
		this(tag, goodsId, isShop, hasModelInfo, null, null,url);
	}

	public ProductDetailsFragment(String tag, String goodsId, boolean isShop, boolean hasModelInfo,int type)
	{
		this(tag, goodsId, isShop, hasModelInfo, null, null,type);
	}

	// public ProductDetailsFragment(int detailsIndex, String goosId, boolean flag) {
	// this.detailsIndex = detailsIndex;
	// this.goosId = goosId;
	// this.flag = flag;
	// }

	public View fragmentC6;
	public int fragmentC6ID;

	private ViewPager mViewPager;
	private ViewPager mViewPager2;
	private BasePagerAdapter<AbsAdapterItem> mPagerAdapter;
	private BasePagerAdapter<AbsAdapterItem> mPagerAdapter2;
	private LinearLayout mDotContainer;
	private int pages;

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener()
	{
		@Override
		public void onPageSelected(int position)
		{
			setSelectdDot(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
		}

		@Override
		public void onPageScrollStateChanged(int state)
		{
		}
	};

	private void setSelectdDot(final int index)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.d(TAG, "setSelectdDot: " + index);
				int dotFocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg");
				int dotUnfocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg1");
				int count = mDotContainer.getChildCount();
				for (int i = 0; i < count; i++)
				{
					ImageView child = (ImageView)mDotContainer.getChildAt(i);
					if (i == index)
					{
						child.setImageResource(dotFocusId);
					}
					else
					{
						child.setImageResource(dotUnfocusId);
					}
				}
				if (count > 1)
				{
					arrow_left.setVisibility(View.VISIBLE);
					arrow_right.setVisibility(View.VISIBLE);
					if (index == 0)
						arrow_left.setVisibility(View.INVISIBLE);
					else if (index == count - 1)
						arrow_right.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
	private void showHadGia(){
		int id=ResourceUtil.getId(getContext(),"product_HadGia");
		FragmentContainer fragmentContainer= (FragmentContainer) mProductDetails.findViewById(id);
		fragmentContainer.setVisibility(View.VISIBLE);
		replace(getActivity(),ResourceUtil.getId(getContext(),"product_HadGia"),new MainWebFragment(url,0),false);
 	}

	private void ensureDots()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached())
					return;
				mDotContainer.removeAllViews();
				int count = pages;
				for (int i = 0; i < count; i++)
				{
					if (isDetached() || getContext() == null)
						return;
					ImageView view = new ImageView(getContext());
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.leftMargin = AndroidUtils.dp2px(getContext(), 23);
					params.rightMargin = AndroidUtils.dp2px(getContext(), 23);
					view.setLayoutParams(params);
					int dotUnfocusId = ResourceUtil.getDrawableId(getContext(), "dot_unfocus");
					view.setImageResource(dotUnfocusId);
					view.setScaleType(ScaleType.CENTER);
					mDotContainer.addView(view);
				}
				if (count > 1)
				{
					arrow_left.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
						}
					});
					arrow_right.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
						}
					});
				}
				setSelectdDot(0);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "product_details_layout");
		mProductDetails = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendLinearLayout)mProductDetails).setInterceptTouchEventToDownward(true);

		fragmentC6ID = ResourceUtil.getId(getContext(), "fragment_container6");
		fragmentC6 = mProductDetails.findViewById(fragmentC6ID);



		replace(instance,ResourceUtil.getId(getContext(),"product_details_attribute"),new MainWebFragment(url,0),false);

		ensureDetailsMiddle();
		ensureUnityPlayer();
		if (tag.equals(Define.TAG_RING))
			ensureDetailRight(0);
		else
			ensureDetailRight(1);

		loadProductDetailsData();
		return mProductDetails;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		instance = this;
	}

	private void ensureDetailRight(int index)
	{
		switch (index)
		{
			case 0:
				if (mShopProductDetailsFragment == null)
					mShopProductDetailsFragment = new ShopProductDetailsFragment(isShop);
				replace(ProductDetailsFragment.instance, ProductDetailsFragment.instance.fragmentC6ID,
						mShopProductDetailsFragment, false);
				break;
			case 1:
				if (mCoupleRingsDetailsFragment == null)
					mCoupleRingsDetailsFragment = new CoupleRingsDetailsFragment();
				replace(ProductDetailsFragment.instance, ProductDetailsFragment.instance.fragmentC6ID,
						mCoupleRingsDetailsFragment, false);
				break;
		}
	}

	@Override
	public void onDestroyView()
	{
		doUnityPlayerBackPressed();
		super.onDestroyView();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		instance = null;
	}

	// @Override
	// public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
	// {
	// if (enter)
	// return super.onCreateAnimation(transit, enter, nextAnim);
	// return AnimationUtils.loadAnimation(getContext(), R.anim.empty);
	// }

	@Override
	public boolean onBackPressed()
	{
		LogUtil.d(TAG, "onBackPressed...");
		doUnityPlayerBackPressed();
		return false;
	}

	private boolean mDoUnityPlayerBackPressed = false;

	private void doUnityPlayerBackPressed()
	{
		if (mDoUnityPlayerBackPressed)
			return;
		LogUtil.v(TAG, "doUnityPlayerBackPressed...");
		mDoUnityPlayerBackPressed = true;
		mUnityViewConver.setVisibility(View.VISIBLE);
		UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyBackToWaiting", "");
		detachUnityPlayer();
		MainActivity.instance.attachUnityPlayer();
		MainActivity.instance.removeModelLoadListener(this);
	}

	// private boolean mDoBackPressed = false;
	//
	// public void doBackPressed(final boolean isTryon)
	// {
	// if (mDoBackPressed)
	// return;
	// mDoBackPressed = true;
	// // getCreatedView().setDrawingCacheEnabled(true);
	// // Bitmap bitmap = getCreatedView().getDrawingCache();
	// Bitmap bitmap = BitmapUtils.viewToBitmap(getCreatedView());
	// LogUtil.v(TAG, "doBackPressed: " + bitmap);
	// final ImageView imageView = new ImageView(getContext());
	// imageView.setImageBitmap(bitmap);
	// MainFragment.instance.getFragmentContainer().addView(imageView, LayoutParams.MATCH_PARENT,
	// LayoutParams.MATCH_PARENT);
	// Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
	// imageView.setAnimation(anim);
	// anim.setAnimationListener(new AnimationListener()
	// {
	// @Override
	// public void onAnimationStart(Animation animation)
	// {
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animation animation)
	// {
	// }
	//
	// @Override
	// public void onAnimationEnd(Animation animation)
	// {
	// MainFragment.instance.getFragmentContainer().removeView(imageView);
	// doUnityPlayerBackPressed();
	// if (isTryon)
	// {
	// MainFragment.instance.backToHomepage();
	// if (mGDResponse != null)
	// {
	// MainActivity.launchTryonScene(getActivity(), mGDResponse.datas);
	// }
	// else if (mCoupleRingDetailResponse != null)
	// {
	// MainActivity.launchTryonSceneWithCoupleRing(getActivity(), mCoupleRingDetailResponse.datas);
	// }
	// else
	// {
	// showToast("没有模型");
	// }
	// }
	// else
	// {
	// MainFragment.instance.onBackPressed();
	// }
	// }
	// });
	// anim.start();
	// getCreatedView().setVisibility(View.GONE);
	// UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyBackToWaiting", "");
	// }

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

	// private void show3DLoadingIndicator()
	// {
	// AndroidUtils.MainHandler.post(new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// loading_indicator.setVisibility(View.VISIBLE);
	//
	// ObjectAnimator oa = ObjectAnimator.ofFloat(loading_indicator, "rotation", 0f, 360f);
	// ReflectHelper.setDeclaredFieldValue(oa, "android.animation.ValueAnimator", "sDurationScale", 1.0f);
	// oa.setDuration(1000L);
	// oa.setInterpolator(new LinearInterpolator());
	// oa.setRepeatCount(ValueAnimator.INFINITE);
	// oa.start();
	// }
	// });
	// }

	// private void hide3DLoadingIndicator()
	// {
	// AndroidUtils.MainHandler.post(new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// loading_indicator.setVisibility(View.GONE);
	// }
	// });
	// }

	private void ensureDetailsMiddle()
	{
		int id = ResourceUtil.getId(getContext(), "details_middle_layout");
		mDetailMiddleLayout = mProductDetails.findViewById(id);

		id = ResourceUtil.getId(getContext(), "unity_container");
		unity_container = (FrameLayout)mDetailMiddleLayout.findViewById(id);

		mUnityViewConver = new ImageView(getContext());
		mUnityViewConver.setBackgroundColor(0xffffffff);
		unity_container.addView(mUnityViewConver, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		id = ResourceUtil.getId(getActivity(), "product_photo");
		product_photo = mDetailMiddleLayout.findViewById(id);
		// product_photo.setVisibility(View.GONE);

		id = ResourceUtil.getId(getContext(), "arrow_left");
		arrow_left = mDetailMiddleLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "arrow_right");
		arrow_right = mDetailMiddleLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_details_tryon");
		View product_details_tryon = mDetailMiddleLayout.findViewById(id);

		id=ResourceUtil.getId(getActivity(),"photo_show");
		photo_show= (LinearLayout) mDetailMiddleLayout.findViewById(id);
		if (!isShop || !hasModelInfo)
			product_details_tryon.setVisibility(View.GONE);

		else
		{
			product_details_tryon.setVisibility(View.VISIBLE);
			product_details_tryon.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					onBackPressed();
					MainFragment.instance.backToHomepage();
					if (mGDResponse != null)
					{
						MainActivity.launchTryonScene(getActivity(), mGDResponse.datas, mSelectedMaterialTag);
					}
					else if (mCoupleRingDetailResponse != null)
					{
						MainActivity.launchTryonSceneWithCoupleRing(getActivity(), mCoupleRingDetailResponse.datas,
								mSelectedMaterialTag);
					}
					else
					{
						showToast("没有模型");
					}
					// doBackPressed(true);
				}
			});
		}
		if (mGDResponse != null && mGDResponse.datas.tagname != null
				&& mGDResponse.datas.tagname.contains(Define.TAGNAME_PENDANT))
			product_details_tryon.setVisibility(View.GONE);

		id = ResourceUtil.getId(getActivity(), "middle_back");
		View middle_back = mDetailMiddleLayout.findViewById(id);
		middle_back.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				onBackPressed();
				MainFragment.instance.onBackPressed();
			}
		});

		id = ResourceUtil.getId(getActivity(), "product_details_3d");
		product_details_3d = (MenuBar)mDetailMiddleLayout.findViewById(id);
		product_details_3d.setAllowRepeatClickMenu(false);
		// product_details_3d.setSelected(true);
		// unity_container.setVisibility(View.VISIBLE);
		// m3DShow = true;

//		id = ResourceUtil.getId(getActivity(), "product_details_photo");
//		product_details_photo = mDetailMiddleLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "product_details_3d_bt");
		// product_details_3d_bt = mDetailMiddleLayout.findViewById(id);

		if (!isShop || !hasModelInfo)
		{
			product_details_3d.setVisibility(View.GONE);
			// material_menubar.setVisibility(View.GONE);
		}
		else
		{
			product_details_3d.setVisibility(View.VISIBLE);
			// product_details_3d.setOnClickListener(new View.OnClickListener()
			// {
			// @Override
			// public void onClick(View v)
			// {
			// if (AndroidUtils.isFastClick())
			// return;
			// set3DShow(true);
			// }
			// });
			product_details_3d.setOnMenuListener(new OnMenuListener()
			{
				@Override
				public void onMenuUnSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
				{
				}

				@Override
				public void onMenuSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
				{
					if (menuIndex > -1 && !m3DShow)
					{
						mCurrentMenuIndex = menuIndex;
						set3DShow(true);
					}
					switch (menuIndex)
					{
						case 0:
							mSelectedMaterialTag = Define.MATERIAL_WHITE_KGOLD;
							break;
						case 1:
							mSelectedMaterialTag = Define.MATERIAL_RED_KGOLD;
							break;
						case 2:
							mSelectedMaterialTag = Define.MATERIAL_YELLOW_KGOLD;
							break;
					}
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyChangeAllModelMaterial",
							mSelectedMaterialTag);
				}
			});
		}
		if(hasModelInfo){
			photo_show.setVisibility(View.VISIBLE);
		}
//		if (!isShop || !hasModelInfo) {
//			product_details_photo.setVisibility(View.GONE);
//		}else{
//			product_details_photo.setVisibility(View.VISIBLE);
//			product_details_photo.setOnClickListener(new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					if (AndroidUtils.isFastClick())
//						return;
//					set3DShow(false);
//				}
//			});
//		}

		id = ResourceUtil.getId(getContext(), "select_item");
		mViewPager = (ViewPager)mDetailMiddleLayout.findViewById(id);
		id = ResourceUtil.getId(getContext(), "select_item2");
		mViewPager2 = (ViewPager)mDetailMiddleLayout.findViewById(id);
		mViewPager2.setOffscreenPageLimit(3);
		mViewPager2.setPageMargin(20);
		photo_show.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mViewPager2.dispatchTouchEvent(event);
			}
		});
		mPagerAdapter = new BasePagerAdapter<AbsAdapterItem>();
		mPagerAdapter2=new BasePagerAdapter<AbsAdapterItem>();
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager2.setAdapter(mPagerAdapter2);
		mViewPager.addOnPageChangeListener(mPageChangeListener);
		mViewPager2.addOnPageChangeListener(mPageChangeListener);
		mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				int width = mViewPager.getWidth();
				int height = mViewPager.getHeight();
				if (width == 0 || height == 0)
					return;
				LogUtil.v(TAG, "mViewPager size: " + width + "x" + height);
				mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				LayoutParams params = mViewPager.getLayoutParams();
				params.width = width;
				params.height = height;
				mViewPager.setLayoutParams(params);
			}
		});
		mViewPager2.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				int width = 500;
				int height = mViewPager2.getHeight();
				if (width == 0 || height == 0)
					return;
				LogUtil.v(TAG, "mViewPager size: " + width + "x" + height);
				mViewPager2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				LayoutParams params = mViewPager2.getLayoutParams();
				params.width = width;
				params.height = height;
				mViewPager2.setLayoutParams(params);
			}
		});


		id = ResourceUtil.getId(getContext(), "dot_container");
		mDotContainer = (LinearLayout)mDetailMiddleLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "image");
		// productImage = (ExtendImageView)
		// mDetailMiddleLayout.findViewById(id);
		//
		// id = ResourceUtil.getId(getActivity(), "selected_list");
		// selectedList = (HorizontalScrollListView) mDetailMiddleLayout
		// .findViewById(id);
		// selectedListAdapter = new BaseAdapter<AbsAdapterItem>();
		// selectedList.setAdapter(selectedListAdapter);
		// selectedListAdapter.clear();

		// id = ResourceUtil.getId(getContext(), "loading_indicator");
		// loading_indicator = mProductDetails.findViewById(id);
		// hide3DLoadingIndicator();
		MainActivity.instance.addModelLoadListener(this);

		if (isShop && hasModelInfo)
			set3DShow(true);
		else
			set3DShow(false);
	}

	private void set3DShow(boolean flag)
	{
		if (flag)
		{
			product_photo.setVisibility(View.GONE);
			unity_container.setVisibility(View.VISIBLE);
			m3DShow = true;
//			product_details_photo.setSelected(false);
			// product_details_3d.setSelected(true);
			product_details_3d.setCurrentMenu(mCurrentMenuIndex);
			// material_menubar.setVisibility(View.VISIBLE);
			// product_details_3d_bt.setVisibility(View.GONE);
		}
		else
		{
			product_photo.setVisibility(View.VISIBLE);
			unity_container.setVisibility(View.INVISIBLE);
			m3DShow = false;
//			product_details_photo.setSelected(true);
			// product_details_3d.setSelected(false);
			mCurrentMenuIndex = product_details_3d.getCurrentMenuIdx();
			product_details_3d.setCurrentMenu(-1);
			// material_menubar.setVisibility(View.GONE);
			// product_details_3d_bt.setVisibility(View.GONE);
		}
	}

	public void ensureUnityPlayer()
	{
		// showLoadingIndicatorDialog();
		if (m3DShow)
			unity_container.setVisibility(View.VISIBLE);
		else
			unity_container.setVisibility(View.INVISIBLE);
		// if (MainActivity.instance.mUnityPlayer == null)
		// {
		// MainActivity.instance.mUnityPlayer = new UnityPlayer(getActivity());
		// }
		if (!ViewTools.containsView(unity_container, MainActivity.instance.mUnityPlayer))
		{
			mUnityViewConver.setVisibility(View.VISIBLE);
			MainActivity.instance.detachUnityPlayer();
			unity_container.addView(MainActivity.instance.mUnityPlayer, 0);
			MainActivity.instance.mUnityPlayer.setVisibility(View.VISIBLE);
			MainActivity.instance.mUnityPlayer.resume();
		}
		ProductDetailsFragment.launchTryon(getActivity());
		// hideLoadingIndicatorDialog();
	}

	public void attachUnityPlayer(boolean flag)
	{
		ensureUnityPlayer();
		if (!flag)
		{
			if (mGDResponse != null)
			{
				loadStartFrom(mGDResponse.datas);
			}
			if (mCoupleRingDetailResponse != null)
			{
				loadCoupleStartFrom(mCoupleRingDetailResponse.datas);
			}
		}
	}

	public void detachUnityPlayer()
	{
		mUnityViewConver.setVisibility(View.VISIBLE);
		if (ViewTools.containsView(unity_container, MainActivity.instance.mUnityPlayer))
			unity_container.removeView(MainActivity.instance.mUnityPlayer);
	}

	private void loadProductDetailsData()
	{
		// showLoadingIndicator();

		if (tag.equals(Define.TAG_RING))
			ShopProduct();
		else
			CoupleProduct();
	}

	public void ShopProduct()
	{
		showLoadingIndicator();
		RequestManager.loadGoodsDetail(getContext(), LoginHelper.getUserKey(getActivity()), goodsId,
				new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						hideLoadingIndicator();
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							mGDResponse = (GoodsDetailResponse)response;
							if (mGDResponse != null)
							{
								BrowseHistoryDBHelper.getInstance().put(getContext(), mGDResponse.datas, isShop);
								AndroidUtils.MainHandler.post(new Runnable()
								{
									@Override
									public void run()
									{
										if (isDetached())
											return;
										if (mGDResponse.datas.thumb_url != null)
										{
											productSelectedList(mGDResponse.datas.thumb_url);
										}
										if (mShopProductDetailsFragment != null)
											mShopProductDetailsFragment.updateGoodsDetailResponse(mGDResponse,
													weightRange, priceRange);
										if (isShop && hasModelInfo)
											loadStartFrom(mGDResponse.datas);
									}
								});
							}
						}
						else
						{
							showToast(response.error);
						}
						// 更新缓存数据
						if (from != DataFrom.SERVER)
							RequestManager.loadGoodsDetail(getContext(), LoginHelper.getUserKey(getContext()), goodsId,
									null, CacheMode.PERFER_NETWORK);
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						hideLoadingIndicator();
						showToast("网络错误: " + error.errorCode);
						RequestManager.loadGoodsDetail(getContext(), LoginHelper.getUserKey(getContext()), goodsId,
								null, CacheMode.PERFER_NETWORK);
					}
				});
	}

	public void CoupleProduct()
	{
		showLoadingIndicator();
		RequestManager.loadCoupleRingDetail(getContext(), LoginHelper.getUserKey(getActivity()), goodsId,
				new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						hideLoadingIndicator();
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							mCoupleRingDetailResponse = (CoupleRingDetailResponse)response;
							BrowseHistoryDBHelper.getInstance().put(getContext(), mCoupleRingDetailResponse.datas,
									isShop);
							if (mCoupleRingDetailResponse != null)
							{
								AndroidUtils.MainHandler.post(new Runnable()
								{
									@Override
									public void run()
									{
										if (isDetached())
											return;
										if (mCoupleRingDetailResponse.datas.thumb_url != null)
										{
											productSelectedList(mCoupleRingDetailResponse.datas.thumb_url);
										}
										if (mCoupleRingsDetailsFragment != null)
											mCoupleRingsDetailsFragment.updateCoupleRingDetailsResponse(
													mCoupleRingDetailResponse, weightRange, priceRange);
										if (isShop && hasModelInfo)
											loadCoupleStartFrom(mCoupleRingDetailResponse.datas);
									}
								});
							}
						}
						else
						{
							showToast(response.error);
						}
						// 更新缓存数据
						if (from != DataFrom.SERVER)
							RequestManager.loadCoupleRingDetail(getContext(), LoginHelper.getUserKey(getContext()),
									goodsId, null, CacheMode.PERFER_NETWORK);
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						hideLoadingIndicator();
						showToast("网络错误: " + error.errorCode);
						RequestManager.loadCoupleRingDetail(getContext(), LoginHelper.getUserKey(getContext()),
								goodsId, null, CacheMode.PERFER_NETWORK);
					}
				});
	}

	public void productSelectedList(ImageInfo[] mImageInfo)
	{
		// productImage.setImageDataSource(mImageInfo[0].url,
		// mImageInfo[0].filemtime, DecodeMode.FIT_RECT);
		// productImage.startImageLoad(false);
		if (isDetached() || getContext() == null)
			return;
		pages = mImageInfo.length;
		for (ImageInfo image : mImageInfo)
		{
			mPagerAdapter.addItem(new ProductImageList(image));
			mPagerAdapter2.addItem(new ProductImageList(image));
		}
		mPopupWindow = new ProductImagePopupWindow(getActivity(), mImageInfo);
		ensureDots();
	}

	public class ProductImageList extends AbsAdapterItem
	{
		ImageInfo mImageInfo;

		public ProductImageList(ImageInfo mImageInfo)
		{
			this.mImageInfo = mImageInfo;
		}

		@Override
		public View onCreateView(final int position, ViewGroup parent)
		{
			// int layout = ResourceUtil.getLayoutId(getActivity(), "product_details_photo_image");
			// View view = View.inflate(getActivity(), layout, null);
			// int id = ResourceUtil.getId(getActivity(), "image");
			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
			ExtendImageView image = new ExtendImageView(getContext());
			image.setLayoutParams(new ViewPager.LayoutParams());
			image.setScaleType(ScaleType.CENTER_CROP);
			image.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					if (mPopupWindow != null)
						mPopupWindow.show(position);
				}
			});
			return image;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{
			// int id = ResourceUtil.getId(getActivity(), "image");
			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
			ExtendImageView image = (ExtendImageView)view;
			if (mImageInfo != null)
			{
				image.setImageDataSource(mImageInfo.url, mImageInfo.filemtime, DecodeMode.FIT_RECT);
			}
			image.startImageLoad(false);
		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
			// int id = ResourceUtil.getId(getActivity(), "image");
			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
			ExtendImageView image = (ExtendImageView)view;
			image.recyleBitmapImage();
		}

		public void onItemClick(View adapterView, ViewGroup parent, View view, int position, long id)
		{
			// productImage.setImageDataSource(mImageInfo.url,
			// mImageInfo.filemtime, DecodeMode.FIT_RECT);
			// productImage.startImageLoad(false);
		}
	}

	public static void launchTryon(Activity activity)
	{
		UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyEnter3DShowScene", "");
	}

	private void loadCoupleStartFrom(com.shiyou.tryapp2.data.response.CoupleRingDetailResponse.GoodsDetail datas)
	{
		if (datas.model_infos == null)
		{
			showToast("没有模型");
			hideLoadingIndicator();
			return;
		}
		else
		{
			List<UnityModelInfo> models = new ArrayList<UnityModelInfo>();
			if (datas.model_infos.men != null)
			{
				UnityModelInfo menModel = new UnityModelInfo(datas.model_infos.men);
				menModel.id = datas.id;
				menModel.type = Config.Type_CoupleRing_Male;
				menModel.weight = 100;
				models.add(menModel);
			}
			else
			{
				showToast("没有男戒模型");
				hideLoadingIndicator();
				return;
			}
			if (datas.model_infos.wmen != null)
			{
				UnityModelInfo wmenmodel = new UnityModelInfo(datas.model_infos.wmen);
				wmenmodel.id = datas.id;
				wmenmodel.type = Config.Type_CoupleRing_FeMale;
				wmenmodel.weight = 100;
				models.add(wmenmodel);
			}
			else
			{
				showToast("没有女戒模型");
				hideLoadingIndicator();
				return;
			}
			startModelDownload(models);
		}
	}

	private void loadStartFrom(GoodsDetail detail)
	{
		showLoadingIndicator();
		if (detail.model_info == null)
		{
			showToast("没有模型");
			hideLoadingIndicator();
			return;
		}
		UnityModelInfo model = new UnityModelInfo(detail.model_info);
		model.id = detail.id;
		if (detail.tagname != null && detail.tagname.contains(Define.TAGNAME_PENDANT))
			model.type = Config.Type_Necklace;
		else
			model.type = Config.Type_Ring;
		model.weight = 100;
		List<UnityModelInfo> models = new ArrayList<UnityModelInfo>();
		models.add(model);
		startModelDownload(models);

		// String modelJson = FileInfo.toJson(mGDResponse.datas.model_info);
		// LogUtil.w(TAG, "modelJson:"+modelJson);
		// UnityPlayer.UnitySendMessage("PlatformMessageHandler",
		// "NotifyAddModel", modelJson);
	}

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
		FileInfo[] infos = new FileInfo[list.size()];
		infos = list.toArray(infos);
		FileDownloadHelper.startMultiDownload(models, getActivity(), infos, mModelFileDownloadCallback, true, true);
	}

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
			// if (mDownloadUrlList.isEmpty())
			// {
			// hideLoadingIndicatorDialog();
			// hide3DLoadingIndicator();
			// }
			// showLoadingIndicator();
			boolean loadingIndicatorsShown = false;
			@SuppressWarnings("unchecked")
			List<UnityModelInfo> models = (List<UnityModelInfo>)tag;
			for (int i = 0; i < models.size(); i++)
			{
				if (status[i] == null || status[i] != DownloadStatus.FINISHED)
					continue;
				if (!loadingIndicatorsShown)
				{
					showLoadingIndicator(10 * 1000L);
					loadingIndicatorsShown = true;
				}
				UnityModelInfo model = models.get(i);
				String modelJson = UnityModelInfo.toJson(model);
				LogUtil.w(TAG, "modelJson:" + modelJson);
				UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyAddModel", modelJson);
			}
		}
	};

	@Override
	public void onModelLoadStarted(String id)
	{
		// show3DLoadingIndicator();
		showLoadingIndicator(10 * 1000L);
		AndroidUtils.MainHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				mUnityViewConver.setVisibility(View.GONE);
			}
		}, 300L);
	}

	@Override
	public void onModelLoadProgress(String id, float progress)
	{
	}

	@Override
	public void onModelLoadFinished(String id, int layer, String faceTag, boolean isClothes)
	{
		// if (mDownloadUrlList.isEmpty())
		// hide3DLoadingIndicator();
		hideLoadingIndicator();
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				// material_menubar.setCurrentMenu(0);
				product_details_3d.setCurrentMenu(0);
			}
		});
	}

	@Override
	public void onModelLoadFailed(String id, String error)
	{
		// if (mDownloadUrlList.isEmpty())
		// hide3DLoadingIndicator();
		hideLoadingIndicator();
		showToast(error);
	}
}
