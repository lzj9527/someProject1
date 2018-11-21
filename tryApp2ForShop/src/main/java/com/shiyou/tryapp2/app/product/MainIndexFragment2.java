package com.shiyou.tryapp2.app.product;

import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.util.ViewTools.FitMode;
import android.extend.widget.ExtendImageView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseAdapter;
import android.extend.widget.adapter.BasePagerAdapter;
import android.extend.widget.adapter.HorizontalScrollListView;
import android.extend.widget.recycler.BaseRecyclerAdapter;
import android.extend.widget.recycler.BaseRecyclerAdapter.BaseViewHolder;
import android.extend.widget.recycler.GridRecyclerView;
import android.extend.widget.recycler.HorizontalStaggeredGridRecyclerView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.response.BannerADListResponse;
import com.shiyou.tryapp2.data.response.BannerADListResponse.BannerADItem;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse.CategoryItem;
import com.shiyou.tryapp2.data.response.ShopLogoAndADResponse;
import com.shiyou.tryapp2.shop.zsa.R;

public class MainIndexFragment2 extends BaseFragment
{
	public static MainIndexFragment2 instance;

	private ViewPager mViewPager;
	private LinearLayout mDotContainer;
	private BasePagerAdapter<AbsAdapterItem> mPagerAdapter;
	private View indexProductLayout;
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

	ShopLogoAndADResponse mShopLogoAndADResponse;
	BannerADListResponse mBannerADListResponse;

//	HorizontalScrollListView indexProduct;
	HorizontalStaggeredGridRecyclerView indexProduct;
	
	BaseRecyclerAdapter indexProductAdapter;
	
//	GridRecyclerView indexProduct;
//	private BaseRecyclerAdapter indexProductAdapter;
	
	private int itemNum = 0;
	private int pos;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		instance = this;

		mLayoutResID = ResourceUtil.getLayoutId(getActivity(), "main_index_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendLinearLayout)view).setInterceptTouchEventToDownward(true);

		int id = ResourceUtil.getId(getActivity(), "viewpager");
		mViewPager = (ViewPager)view.findViewById(id);
		mPagerAdapter = new BasePagerAdapter<AbsAdapterItem>();
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.addOnPageChangeListener(mPageChangeListener);
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

		id = ResourceUtil.getId(getActivity(), "dot_container");	
		mDotContainer = (LinearLayout)view.findViewById(id);

//		id = ResourceUtil.getId(getContext(), "index_product");
////		indexProduct = (HorizontalScrollListView)view.findViewById(id);
//		indexProduct = (HorizontalStaggeredGridRecyclerView)view.findViewById(id);
		
		indexProduct = new HorizontalStaggeredGridRecyclerView(getContext());
		
		id = ResourceUtil.getId(getContext(), "index_product_layout");
		indexProductLayout = (View) view.findViewById(id);
		
		// indexProduct.setVerticalDividerWidth(AndroidUtils.dp2px(getContext(), 10));
//		indexProductAdapter = new BaseAdapter<AbsAdapterItem>();
		indexProductAdapter = new BaseRecyclerAdapter();
		((ViewGroup)indexProductLayout).addView(indexProduct, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		indexProduct.setAdapter(indexProductAdapter);

		doRefresh();

		// if (GoodsId != null) {
		// MainActivity.launchTryonScene(getActivity(), "5");
		// BaseFragment.add(MainFragment.instance,
		// MainFragment.instance.fragmentC1ID,
		// new ProductDetailsFragment(0, GoodsId), true);
		// }
		
		scrollControl();

		return view;
	}
	
	public void scrollControl(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				pos = 0;
				while (true){
					
//					itemNum = mViewPager.getChildCount();
					if (isResumed() ){
						
						try {
							if (itemNum>0){
								AndroidUtils.MainHandler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										mViewPager.setCurrentItem(pos);
									}
								});							
								pos = (pos+1) % itemNum;
							}
							Thread.sleep(5*1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			}
		}).start();
	}
	
	

	// @Override
	// public void onFirstStart()
	// {
	// super.onFirstStart();
	// }

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		instance = null;
	}

	public void doRefresh()
	{
		
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isResumed())
				{
					LogUtil.d(TAG, "doRefresh...");
					loadAdvertisements();
					
				}
				else
					AndroidUtils.MainHandler.postDelayed(this, 50L);
			}
		});
		loadGoodsCategorys();
	}

	// public MainIndexFragment getAdvertisementResponse()
	// {
	// return mMainIndexFragment;
	// }

	private void loadAdvertisements()
	{
		loadShopLogoAndAD();
	}

	private void loadShopLogoAndAD()
	{
		RequestManager.loadShopLogoAndAD(getContext(), LoginHelper.getUserKey(getContext()), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					mShopLogoAndADResponse = (ShopLogoAndADResponse)response;
				}
				else
				{
					mShopLogoAndADResponse = null;
					showToast(response.error);
				}
				loadBannerADList();
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
				showToast("网络错误: " + error.errorCode);
				mShopLogoAndADResponse = null;
				loadBannerADList();
			}
		});
	}

	private void loadBannerADList()
	{
		RequestManager.loadBannerADList(getContext(), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					mBannerADListResponse = (BannerADListResponse)response;
				}
				else
				{
					mBannerADListResponse = null;
					showToast(response.error);
				}
				ensureAdvertisementPager();
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
				showToast("网络错误: " + error.errorCode);
				mBannerADListResponse = null;
				ensureAdvertisementPager();
			}
		});
	}

	private void ensureAdvertisementPager()
	{
		mPagerAdapter.clear();
		int length = 0;
		if (mShopLogoAndADResponse != null && mShopLogoAndADResponse.datas != null
				&& mShopLogoAndADResponse.datas.list != null && mShopLogoAndADResponse.datas.list.ads != null)
		{
			if (!TextUtils.isEmpty(mShopLogoAndADResponse.datas.list.goodsid))
				if (mShopLogoAndADResponse.datas.list.shopsee == 1)
					mPagerAdapter.addItem(new AdvertisementPagerAdapterItem(mShopLogoAndADResponse.datas.list.ads,
							mShopLogoAndADResponse.datas.list.goodsid, mShopLogoAndADResponse.datas.list.tag, true));
				else
					mPagerAdapter.addItem(new AdvertisementPagerAdapterItem(mShopLogoAndADResponse.datas.list.ads,
							mShopLogoAndADResponse.datas.list.goodsid, mShopLogoAndADResponse.datas.list.tag, false));
			else
				mPagerAdapter.addItem(new AdvertisementPagerAdapterItem(mShopLogoAndADResponse.datas.list.ads,
						mShopLogoAndADResponse.datas.list.link));
			length++;
			itemNum++;
		}
		if (mBannerADListResponse != null && mBannerADListResponse.datas != null
				&& mBannerADListResponse.datas.list != null && mBannerADListResponse.datas.list.length > 0)
		{
			for (BannerADItem item : mBannerADListResponse.datas.list)
			{
				if (!TextUtils.isEmpty(item.goodsid))
					if (item.shopsee == 1)
						mPagerAdapter.addItem(new AdvertisementPagerAdapterItem(item.thumb, item.goodsid, item.tag,
								true));
					else
						mPagerAdapter.addItem(new AdvertisementPagerAdapterItem(item.thumb, item.goodsid, item.tag,
								false));
				else
					mPagerAdapter.addItem(new AdvertisementPagerAdapterItem(item.thumb, item.link));
				length++;
				itemNum++;
			}
		}
		ensureDots(length);
	}

	private void ensureDots(final int length)
	{
		// if (mAdvertisementResponse != null && mAdvertisementResponse.datas !=
		// null
		// && mAdvertisementResponse.datas.adv_list != null)
		// {
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mDotContainer.removeAllViews();
				for (int i = 0; i < length; i++)
				{
					ImageView view = new ImageView(getActivity());
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.topMargin = AndroidUtils.dp2px(getActivity(), 2);
					params.bottomMargin = AndroidUtils.dp2px(getActivity(), 2);
					params.leftMargin = AndroidUtils.dp2px(getActivity(), 20);
					params.rightMargin = AndroidUtils.dp2px(getActivity(), 20);
					view.setLayoutParams(params);
					int dotUnfocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg1");
					view.setImageResource(dotUnfocusId);
					view.setScaleType(ScaleType.CENTER);
					mDotContainer.addView(view);
				}
				setSelectdDot(0);
			}
		});
		// }
	}

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
			}
		});
	}

	private class AdvertisementPagerAdapterItem extends AbsAdapterItem
	{
		private ImageInfo mImageInfo;
		private String mGoodsId;
		private String mTag;
		private String mLink;
		private boolean mIsShop;

		public AdvertisementPagerAdapterItem(ImageInfo imageInfo, String goodsId, String tag, boolean isShop)
		{
			mImageInfo = imageInfo;
			mGoodsId = goodsId;
			mTag = tag;
			mIsShop = isShop;
		}

		public AdvertisementPagerAdapterItem(ImageInfo imageInfo, String link)
		{
			mImageInfo = imageInfo;
			mLink = link;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			ExtendImageView view = new ExtendImageView(getActivity());
			view.setBackgroundColor(getResources().getColor(android.R.color.white));
			ViewPager.LayoutParams params = new ViewPager.LayoutParams();
			view.setLayoutParams(params);
			view.setScaleType(ScaleType.CENTER_CROP);
			// view.setAutoRecyleOnDetachedFromWindow(true);
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					LogUtil.d(TAG, "onClick: " + mGoodsId + "; " + mLink);
					if (!TextUtils.isEmpty(mGoodsId))
					{
						// if (TextUtils.isEmpty(mTag) || mTag.equals(GoodsItem.TAG_RING))
						// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new
						// ProductDetailsFragment(mTag,
						// mGoodsId, true), true);
						MainFragment.instance.addProductDetailFragmentToCurrent(mGoodsId, mTag, mIsShop, true, false);
						// else
						// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new ProductDetailsFragment(
						// 1, mGoodsId), true);
					}
					else if (!TextUtils.isEmpty(mLink))
					{
						String actualUrl = mLink;
						if (mLink.contains("/pad/default"))
						{
							actualUrl = Config.BaseWebUrl + mLink.substring(mLink.indexOf("/pad/default"));
						}
						LogUtil.d(TAG, "openWindow: actualUrl=" + actualUrl);
						// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new MainRecommendWebFragment(
						// actualUrl, 1), true); 
						MainFragment.instance.addWebFragmentToCurrent(actualUrl, false);
					}
				}
			});
			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{

		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{
			ExtendImageView imageView = (ExtendImageView)view;
			if (mImageInfo != null)
				imageView.setImageDataSource(mImageInfo.url, mImageInfo.filemtime, DecodeMode.FIT_WIDTH);
			imageView.startImageLoad();
		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
			ExtendImageView imageView = (ExtendImageView)view;
			imageView.recyleBitmapImage();
		}
	}

	private void loadGoodsCategorys()
	{
		RequestManager.loadGoodsCategorys(getActivity(), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, final BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)                   //180111
				{
					AndroidUtils.MainHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if (isResumed())
							{
								
								GoodsCategorysResponse mGoodsCategoryResponse = (GoodsCategorysResponse)response;
								indexProductAdapter.clear();
//								for (int i=0; i<20; i++)
									indexProductAdapter.addItem(new CategoryAdapterItem(0));
								for (CategoryItem mCategoryItem : mGoodsCategoryResponse.findZuanShiCategoryList())
								{
									indexProductAdapter.addItem(new CategoryAdapterItem(mCategoryItem));
								}
							}
							else
								AndroidUtils.MainHandler.postDelayed(this, 50L);
						}
					});
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
	
	private class CategoryAdapterItem extends android.extend.widget.recycler.AbsAdapterItem
	{
		
		CategoryItem mCategoryItem;
		int mIndex;

		public CategoryAdapterItem(CategoryItem categoryItem)
		{
			this.mCategoryItem = categoryItem;
		}

		public CategoryAdapterItem(int index)
		{
			this.mIndex = index;
		}

		@Override
		public View onCreateView(ViewGroup parent, int position) {
			// TODO Auto-generated method stub
			ExtendImageView view = new ExtendImageView(getContext());
			view.setScaleType(ScaleType.FIT_CENTER);
//			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			view.setLayoutParams(new LayoutParams(220, 220));
			int padding = 20;
			view.setPadding(padding, padding, padding, padding);
//			ViewTools.autoFitViewDimension(view, parent, FitMode.FIT_IN_HEIGHT, 1);
//			ViewTools.adapterViewPadding(view, MainActivity.scaled);

			// int id = ResourceUtil.getId(getActivity(), "image");
			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (mCategoryItem != null)
						{
							// MainFragment.instance.setCurrentFragmentToProductList(mCategoryItem.id, true);
							MainFragment.instance.addFragmentToCurrent(new ProductListFragment(mCategoryItem.id, true), false);
						}
						else
						{
							String url;
							switch (mIndex)
							{
							// case 1:
							// url = Config.WebGems + "?key=" + LoginHelper.getUserKey(getContext());
							// break;
								default:                                                                                                                            
									url = Config.WebGIADiamonds + "?key=" + LoginHelper.getUserKey(getContext());
									break;
							}
							MainFragment.instance.addWebFragmentToCurrent(url, false);
						}
				}
			});
			return view;
		}

		@Override
		public void onBindView(BaseViewHolder holder, View view, int position) {
			// TODO Auto-generated method stub
			// int id = ResourceUtil.getId(getActivity(), "image");
//			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
//			ExtendImageView image = (ExtendImageView)view;
//			if (mCategoryItem != null && mCategoryItem.thumb != null)
//			{
//				image.setImageDataSource(mCategoryItem.thumb.url, mCategoryItem.thumb.filemtime, DecodeMode.FIT_HEIGHT);
//			}
//			else
//			{
//				switch (mIndex)
//				{
//				// case 1:
//				// image.setImageResource(R.drawable.gem);
//				// break;
//					default:
//						image.setImageResource(R.drawable.gia);
//						break;
//				}
//				ViewTools.autoFitViewDimension(image, parent, FitMode.FIT_IN_HEIGHT, 1);
//			}
//			image.startImageLoad(false);
		}

		@Override
		public void onViewAttachedToWindow(BaseViewHolder holder, View view) {
			// TODO Auto-generated method stub
			ExtendImageView image = (ExtendImageView)view;
			if (mCategoryItem != null && mCategoryItem.thumb != null)
			{
				image.setImageDataSource(mCategoryItem.thumb.url, mCategoryItem.thumb.filemtime, DecodeMode.FIT_HEIGHT);
			}
			else
			{
				switch (mIndex)
				{
				// case 1:
				// image.setImageResource(R.drawable.gem);
				// break;
					default:
						image.setImageResource(R.drawable.gia);
						break;
				}
//				ViewTools.autoFitViewDimension(image, parent, FitMode.FIT_IN_HEIGHT, 1);
			}
			image.startImageLoad(false);
		}

		
	}
//	private class CategoryAdapterItem extends AbsAdapterItem
//	{
//		CategoryItem mCategoryItem;
//		int mIndex;
//
//		// GoodsCategoryResponse mGoodsCategoryResponse;
//
//		public CategoryAdapterItem(CategoryItem categoryItem)
//		{
//			// this.mGoodsCategoryResponse = mGoodsCategoryResponse;
//			this.mCategoryItem = categoryItem;
//		}
//
//		public CategoryAdapterItem(int index)
//		{
//			this.mIndex = index;
//		}
//
//		@Override
//		public View onCreateView(int position, ViewGroup parent)
//		{
//			// int layout = ResourceUtil.getLayoutId(getActivity(), "main_index_item");
//			// View view = View.inflate(getActivity(), layout, null);
//			// int id = ResourceUtil.getId(getActivity(), "image");
//			// ExtendImageView image = (ExtendImageView) view.findViewById(id);
//			// image.setAutoRecyleBitmap(true);
//
//			ExtendImageView view = new ExtendImageView(getContext());
//			view.setScaleType(ScaleType.FIT_CENTER);
//			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//			int padding = 40;
//			view.setPadding(padding, padding, padding, padding);
//			ViewTools.autoFitViewDimension(view, parent, FitMode.FIT_IN_HEIGHT, 1);
//			ViewTools.adapterViewPadding(view, MainActivity.scaled);
//
//			return view;
//		}
//
//		@Override
//		public void onUpdateView(View view, int position, ViewGroup parent)
//		{
//			// int id = ResourceUtil.getId(getActivity(), "image");
//			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
//			ExtendImageView image = (ExtendImageView)view;
//			if (mCategoryItem != null && mCategoryItem.thumb != null)
//			{
//				image.setImageDataSource(mCategoryItem.thumb.url, mCategoryItem.thumb.filemtime, DecodeMode.FIT_HEIGHT);
//			}
//			else
//			{
//				switch (mIndex)
//				{
//				// case 1:
//				// image.setImageResource(R.drawable.gem);
//				// break;
//					default:
//						image.setImageResource(R.drawable.gia);
//						break;
//				}
//				ViewTools.autoFitViewDimension(image, parent, FitMode.FIT_IN_HEIGHT, 1);
//			}
//			image.startImageLoad(false);
//		}
//
//		@Override
//		public void onLoadViewResource(View view, int position, ViewGroup parent)
//		{
//
//		}
//
//		@Override
//		public void onRecycleViewResource(View view, int position, ViewGroup parent)
//		{
//			// int id = ResourceUtil.getId(getActivity(), "image");
//			// ExtendImageView image = (ExtendImageView) view.findViewById(id);
//			// image.recyleBitmapImage();
//		}
//
//		public void onItemClick(View adapterView, ViewGroup parent, View view, int position, long id)
//		{
//			if (mCategoryItem != null)
//			{
//				// MainFragment.instance.setCurrentFragmentToProductList(mCategoryItem.id, true);
//				MainFragment.instance.addFragmentToCurrent(new ProductListFragment(mCategoryItem.id, true), false);
//			}
//			else
//			{
//				String url;
//				switch (mIndex)
//				{
//				// case 1:
//				// url = Config.WebGems + "?key=" + LoginHelper.getUserKey(getContext());
//				// break;
//					default:                                                                                                                            
//						url = Config.WebGIADiamonds + "?key=" + LoginHelper.getUserKey(getContext());
//						break;
//				}
//				MainFragment.instance.addWebFragmentToCurrent(url, false);
//			}
//		}
//	}
	
	
}