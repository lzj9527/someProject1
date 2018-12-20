package com.shiyou.tryapp2.app.product;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendDialog;
import android.extend.widget.FragmentContainer;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseAdapter;
import android.extend.widget.adapter.BasePagerAdapter;
import android.extend.widget.adapter.ScrollListView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse.ErpGoods;
import com.shiyou.tryapp2.data.response.GoodsErpResponse;

@SuppressLint("ValidFragment")
public class ShopProductDetailsFragment extends BaseFragment
{
	private View mDetailRightLayout;
	private GoodsDetailResponse mGDResponse;
	private boolean isShop;
	private float[] weightRange;
	private int[] priceRange;

	private FragmentContainer product_attribute;
	// 添加购物车
	String productID = null;
	String erp_id = null;
	public int productSize = 0;

	// 商品名字价格
	private TextView productName;
	private TextView productPirce;

	private TextView product_m;

	// 材质
	// private TextView productMaterial;
	// private List<String> materialList = new ArrayList<String>();

	// 手寸
	private TextView productHand;
	// private List<String> handList = new ArrayList<String>();

	private final int pageSize = 6;
	// private int pages;
	// private int take_more;
	private ViewPager mViewPager;
	private BasePagerAdapter<AbsAdapterItem> mPagerAdapter;
	private LinearLayout mDotContainer;

	private TextView ringText; // 戒托名字
	private TextView ringPriceText; // 戒托价格
	// 商品属性
	private TextView product_number; // 货号
	private TextView product_deputy_number; // 副石数量
	private TextView product_deputy_weight; // 副石重量
	private TextView product_main_jingdu; // 主石净度
	private TextView product_main_weight; // 主石重量
	private TextView product_gold_weight; // 黄金重量
	private TextView product_main_color; // 主石颜色
	private TextView product_deputy_color; // 副石颜色
	private TextView product_deputy_jingdu; // 副石净度
	private TextView product_zs;// 证书号

	private TextView product_kh;

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener()
	{
		@Override
		public void onPageSelected(int position)
		{
			setSelectdDot(position);
			mPagerAdapter.notifyPageSelected(position);
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

	public ShopProductDetailsFragment(GoodsDetailResponse response, boolean isShop, float[] weightRange,
			int[] priceRange)
	{
		this.mGDResponse = response;
		this.isShop = isShop;
		this.productID = response.datas.id;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
	}

	public ShopProductDetailsFragment(GoodsDetailResponse response, boolean isShop)
	{
		this(response, isShop, null, null);
	}

	public ShopProductDetailsFragment(boolean isShop)
	{
		this.isShop = isShop;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "product_details_right_layout");
		mDetailRightLayout = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendLinearLayout)mDetailRightLayout).setInterceptTouchEventToDownward(true);

		int id=ResourceUtil.getId(getContext(),"product_details_attribute");
		product_attribute= (FragmentContainer) mDetailRightLayout.findViewById(id);

		replace(getActivity(),ResourceUtil.getId(getContext(),"product_details_attribute"),new MainWebFragment("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/new-singleGoodsDetail.html",0),false);

		ViewTools.adapterAllViewMarginInChildren(mDetailRightLayout, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(mDetailRightLayout, MainActivity.scaled);
		ViewTools.adapterAllTextViewTextSizeInChildren(mDetailRightLayout, MainActivity.fontScaled);

		id = ResourceUtil.getId(getContext(), "product_name");
		productName = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "product_kh");
		product_kh = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "product_pirce");
		productPirce = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "product_m");
		product_m = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring");
		ringText = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_price");
		ringPriceText = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_number");
		product_number = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_zs");
		product_zs = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_number");
		product_deputy_number = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_weight");
		product_deputy_weight = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_jingdu");
		product_main_jingdu = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_weight");
		product_main_weight = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_gold_weight");
		product_gold_weight = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_color");
		product_main_color = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_color");
		product_deputy_color = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_jingdu");
		product_deputy_jingdu = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "product_material");
		// productMaterial = (TextView)mDetailRightLayout.findViewById(id);
		// materialList.add("18K白金");
		// materialList.add("pt金");
		// mSpinerPopWindowMaterial = new SpinerPopWindow(getContext());
		// mSpinerPopWindowMaterial.refreshData(materialList, 0);
		// productMaterial.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (AndroidUtils.isFastClick() || TextUtils.isEmpty(productID))
		// return;
		// showMaterialPopWindow();
		// }
		// });
		// mSpinerPopWindowMaterial.setItemListener(new IOnItemSelectListener()
		// {
		// @Override
		// public void onItemClick(int pos)
		// {
		// setMaterialHero(pos, materialList, productMaterial);
		// if (mGDResponse == null || mGDResponse.datas == null || mGDResponse.datas.param == null)
		// return;
		// if (productMaterial.getText().toString().equals("18K白金"))
		// {
		// LogUtil.w(TAG, "18K白金");
		// ringPriceText.setText("￥" + mGDResponse.datas.param[2].value);
		// }
		// else
		// {
		// LogUtil.w(TAG, "pt金");
		// ringPriceText.setText("￥" + mGDResponse.datas.param[3].value);
		// }
		// }
		// });

		id = ResourceUtil.getId(getContext(), "product_hand");
		productHand = (TextView)mDetailRightLayout.findViewById(id);
		productHand.setText("12号");
		// for (int i = 8; i < 25; i++)
		// {
		// handList.add(i + "号");
		// }
		// mSpinerPopWindowHand = new SpinerPopWindow(getContext());
		// mSpinerPopWindowHand.refreshData(handList, 0);
		// productHand.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (AndroidUtils.isFastClick() || TextUtils.isEmpty(productID))
		// return;
		// showSpinWindow(mSpinerPopWindowHand, productHand);
		// }
		// });
		// mSpinerPopWindowHand.setItemListener(new IOnItemSelectListener()
		// {
		// @Override
		// public void onItemClick(int pos)
		// {
		// setMaterialHero(pos, handList, productHand);
		// }
		// });

		id = ResourceUtil.getId(getContext(), "select_item");
		mViewPager = (ViewPager)mDetailRightLayout.findViewById(id);
		mPagerAdapter = new BasePagerAdapter<AbsAdapterItem>();
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.addOnPageChangeListener(mPageChangeListener);
		// mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(
		// new OnGlobalLayoutListener() {
		// @Override
		// public void onGlobalLayout() {
		// mViewPager.getViewTreeObserver()
		// .removeOnGlobalLayoutListener(this);
		// int width = mViewPager.getWidth();
		// int height = mViewPager.getHeight();
		// LayoutParams params = mViewPager.getLayoutParams();
		// params.width = width;
		// params.height = 510;
		// mViewPager.setLayoutParams(params);
		// }
		// });

		id = ResourceUtil.getId(getContext(), "dot_container");
		mDotContainer = (LinearLayout)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "place_order");
		View place_order = mDetailRightLayout.findViewById(id);
		place_order.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick() || TextUtils.isEmpty(productID))
					return;
				PlaceOrder();
			}
		});

		id = ResourceUtil.getId(getActivity(), "select_ring");
		View select_ring = mDetailRightLayout.findViewById(id);
		select_ring.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick() || TextUtils.isEmpty(productID))
					return;
				// ProductDetailsFragment.instance.onBackPressed();
				String productHText = (String)productHand.getText();
				try
				{
					productHText = productHText.substring(0, productHText.length() - 1);
					productSize = Integer.parseInt(productHText);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
					productSize = 0;
				}
				String url = Config.WebGIADiamonds + "?id=" + productID + "&shape=1" + "&key="
						+ LoginHelper.getUserKey(getContext());
				// if (isShop)
				MainFragment.instance.addWebFragmentToCurrent(url, false);
				// else
				// MainFragment.instance.addWebFragmentToOther(url, false);
				// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new MainRecommendWebFragment(
				// Config.WebRings + "?id=" + productID + "&shape=1", 1), true);
			}
		});

		id = ResourceUtil.getId(getContext(), "customize_layout");
		View customize_layout = mDetailRightLayout.findViewById(id);
		if (!isShop)
			customize_layout.setVisibility(View.GONE);
		else
			customize_layout.setVisibility(View.VISIBLE);

		if (mGDResponse != null)
			updateGoodsDetailResponse(mGDResponse, weightRange, priceRange);

		return mDetailRightLayout;
	}

	public void updateGoodsDetailResponse(GoodsDetailResponse response, float[] weightRange, int[] priceRange)
	{
		this.mGDResponse = response;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
		this.productID = mGDResponse.datas.id;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached())
					return;
				if (isResumed())
				{
					if (mGDResponse.datas.title != null)
						productName.setText(mGDResponse.datas.title);

					if (mGDResponse.datas.sku != null)
						product_kh.setText(mGDResponse.datas.sku);

					// if (mGDResponse.datas.param != null && mGDResponse.datas.param.length > 0)
					// {
					// product_deputy_number.setText(mGDResponse.datas.param[0].value);
					// product_deputy_weight.setText(mGDResponse.datas.param[1].value);
					// }

					// setMaterialHero(0, materialList, productMaterial);
					loadGoodsErp();
				}
				else
				{
					AndroidUtils.MainHandler.postDelayed(this, 50L);
				}
			}
		});
	}

	private boolean checkNeedAdd(ErpGoods erp)
	{
		boolean add = true;
		if (add && priceRange != null && priceRange.length > 1)
		{
			if (!TextUtils.isEmpty(erp.p5))
			{
				int price = Integer.parseInt(erp.p5);
				if (price < priceRange[0] || price > priceRange[1])
					add = false;
			}
			else
				add = false;
		}
		if (add && weightRange != null && weightRange.length > 1)
		{
			if (!TextUtils.isEmpty(erp.p7))
			{
				if (erp.p7.endsWith("ct"))
				{
					int end = erp.p7.indexOf("ct");
					erp.p7 = erp.p7.substring(0, end);
				}
				float weight = Float.parseFloat(erp.p7);
				if (weight < weightRange[0] || weight > weightRange[1])
					add = false;
			}
			else
				add = false;
		}
		return add;
	}

	private void loadGoodsErp()
	{
		RequestManager.loadGoodsErp(getContext(), LoginHelper.getUserKey(getContext()), productID,
				new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							final GoodsErpResponse geResponse = (GoodsErpResponse)response;
							if (geResponse.datas != null && geResponse.datas.erp != null
									&& geResponse.datas.erp.length > 0)
							{
								AndroidUtils.MainHandler.post(new Runnable()
								{
									@Override
									public void run()
									{
										if (isDetached() || getContext() == null)
											return;
										List<ErpGoods> erpList = new ArrayList<ErpGoods>();
										for (ErpGoods erp : geResponse.datas.erp)
										{
											if (checkNeedAdd(erp))
												erpList.add(erp);
										}
										ErpGoods[] erpArray = new ErpGoods[erpList.size()];
										erpArray = erpList.toArray(erpArray);
										if (erpArray.length > 0)
										{
											updateProductErpInfo(erpArray[0]);
											ensureErpList(erpArray);
										}
									}
								});
							}
						}
						else
							showToast(response.error);
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						showToast("网络异常: " + error.errorCode);
					}
				});
	}

	private void updateProductErpInfo(ErpGoods erp)
	{
		if (erp.p1 != null)
			product_gold_weight.setText(erp.p1);
		else
			product_gold_weight.setText("-");

		if (erp.p7 != null)
			product_main_weight.setText(erp.p7);
		else
			product_main_weight.setText("-");

		if (erp.p3 != null)
			product_main_color.setText(erp.p3);
		else
			product_main_color.setText("-");

		if (erp.p2 != null)
			product_main_jingdu.setText(erp.p2);
		else
			product_main_jingdu.setText("-");

		if (erp.p4 != null)
			product_m.setText(erp.p4);
		else
			product_m.setText("-");

		if (erp.p5 != null)
			productPirce.setText("￥" + erp.p5);
		else
			productPirce.setText("￥-");

		if (erp.p8 != null)
			product_deputy_number.setText(erp.p8);
		else
			product_deputy_number.setText("-");

		if (erp.p9 != null)
			product_deputy_weight.setText(erp.p9);
		else
			product_deputy_weight.setText("-");

		if (erp.p128 != null)
			productHand.setText(erp.p128 + "号");
		else
			productHand.setText("12号");

		if (erp.erpid != null)
			product_number.setText(erp.erpid);
		else
			product_number.setText("-");

		if (erp.zs != null)
			product_zs.setText(erp.zs);
		else
			product_zs.setText("-");
	}

	// private SpinerPopWindow mSpinerPopWindowMaterial;
	// private SpinerPopWindow mSpinerPopWindowHand;

	// private void showSpinWindow(SpinerPopWindow mSpinerPopWindow, TextView mProduct)
	// {
	// Log.e("", "showSpinWindow" + mProduct.getText());
	// mSpinerPopWindow.setWidth(mProduct.getWidth());
	// mSpinerPopWindow.showAsDropDown(mProduct);
	// }

	// private void showMaterialPopWindow()
	// {
	// mSpinerPopWindowMaterial.setWidth(productMaterial.getWidth());
	//
	// int layout = ResourceUtil.getLayoutId(getContext(), "spiner_window_layout");
	// View view = View.inflate(getContext(), layout, null);
	// view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	// int windowHeight = view.getMeasuredHeight();
	//
	// layout = ResourceUtil.getLayoutId(getContext(), "spiner_item_layout");
	// view = View.inflate(getContext(), layout, null);
	// view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	// int itemHeight = view.getMeasuredHeight();
	// mSpinerPopWindowMaterial.showAsDropDown(productMaterial, 0,
	// -(productMaterial.getHeight() + windowHeight + itemHeight * materialList.size()));
	// }

	// private void setMaterialHero(int pos, List<String> mList, TextView mProduct)
	// {
	// if (pos >= 0 && pos <= mList.size())
	// {
	// String value = mList.get(pos);
	// mProduct.setText(value);
	// if (pos == 0)
	// ringPriceText.setText("￥" + mGDResponse.datas.param[2].value);
	//
	// }
	// }

	// public void addMateria(final GoodsDetailResponse mGoodsDetailResponse)
	// {
	// if (mGoodsDetailResponse.datas.specs != null &&
	// mGoodsDetailResponse.datas.specs.length > 0)
	// {
	// SpecItem mSpecItem = mGoodsDetailResponse.datas.findMaterialSpecItem();
	// for (SpecValue mSpecValue : mSpecItem.value)
	// {
	// materialList.add(mSpecValue.title);
	// }
	// ringText.setText(mSpecItem.value[0].title + "戒托价");
	// ringPriceText.setText(mSpecItem.value[0].jt_price);
	// List<SpecGoods> mSpecGoods =
	// mGoodsDetailResponse.datas.findSpecGoodsList(mSpecItem.value[0].id);
	// selectPrice(mSpecGoods);
	//
	// mSpinerPopWindowMaterial = new SpinerPopWindow(getContext());
	// mSpinerPopWindowMaterial.refreshData(materialList, 0);
	// setMaterialHero(0, materialList, productMaterial);
	// mSpinerPopWindowMaterial.setItemListener(new IOnItemSelectListener()
	// {
	//
	// @Override
	// public void onItemClick(int pos)
	// {
	// setMaterialHero(pos, materialList, productMaterial);
	// String material = materialList.get(pos);
	// SpecItem mSpecItem = mGoodsDetailResponse.datas.findMaterialSpecItem();
	// for (SpecValue mSpecValue : mSpecItem.value)
	// {
	// if (material.equals(mSpecValue.title))
	// {
	// ringText.setText(mSpecValue.title + "戒托价");
	// ringPriceText.setText(mSpecValue.jt_price);
	// List<SpecGoods> mSpecGoods =
	// mGoodsDetailResponse.datas.findSpecGoodsList(mSpecValue.id);
	// selectPrice(mSpecGoods);
	// }
	// }
	// }
	// });
	// }
	//
	// }

	private List<AbsAdapterItem> mList;

	private void ensureErpList(ErpGoods[] erp)
	{
		if (isDetached() || getContext() == null)
			return;
		mPagerAdapter.clear();
		int allnum = erp.length;
		int pageCount = (int)Math.ceil((double)allnum / (double)pageSize);
		int more = allnum % pageSize;
		mList = new ArrayList<AbsAdapterItem>();
		for (int page = 0; page < pageCount; page++)
		{
			int size = pageSize;
			if (page == pageCount - 1 && more > 0)
			{
				size = more;
			}
			ErpGoods[] array = new ErpGoods[size];
			for (int j = 0; j < size; j++)
			{
				array[j] = erp[page * pageSize + j];
			}
			SelectPriceList mSelectPriceList = new SelectPriceList(page, array);
			mPagerAdapter.addItem(mSelectPriceList);
		}

		// ErpGoods[][] mErpGoods = new ErpGoods[pages + 1][pageSize];
		// mList = new ArrayList<AbsAdapterItem>();
		// if (allnum <= pageSize) {
		// take_more = allnum;
		// for (int j = 0; j < allnum; j++) {
		// mErpGoods[0][j] = erp[j];
		// }
		// SelectPriceList mSelectPriceList = new SelectPriceList(0,
		// mErpGoods);
		// mPagerAdapter.addItem(mSelectPriceList);
		// } else {
		// take_more = (int) (allnum % pageSize);
		// if (take_more == 0) {
		// for (int i = 0; i < pages; i++) {
		// for (int j = 0; j < pageSize; j++) {
		// mErpGoods[i][j] = erp[j + (pageSize * i)];
		// }
		// SelectPriceList mSelectPriceList = new SelectPriceList(
		// i, mErpGoods);
		// mPagerAdapter.addItem(mSelectPriceList);
		// }
		// } else {
		// for (int i = 0; i < pages; i++) {
		// if (i < pages - 1) {
		// for (int j = 0; j < pageSize; j++) {
		// mErpGoods[i][j] = erp[j + (pageSize * i)];
		// }
		// } else {
		// for (int j = 0; j < take_more; j++) {
		// mErpGoods[i][j] = erp[j + (pageSize * i)];
		// }
		// }
		// SelectPriceList mSelectPriceList = new SelectPriceList(
		// i, mErpGoods);
		// mPagerAdapter.addItem(mSelectPriceList);
		// }
		// }
		// }
		ensureDots(pageCount);
	}

	private void setSelectdDot(final int index)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached() || getContext() == null)
					return;
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

	private void ensureDots(final int length)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached() || getContext() == null)
					return;
				mDotContainer.removeAllViews();
				for (int i = 0; i < length; i++)
				{
					if (isDetached() || getContext() == null)
						return;
					ImageView view = new ImageView(getContext());
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.leftMargin = 10;
					params.rightMargin = 10;
					view.setLayoutParams(params);
					int dotUnfocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg1");
					view.setImageResource(dotUnfocusId);
					view.setScaleType(ScaleType.CENTER);
					mDotContainer.addView(view);
				}
				setSelectdDot(0);
			}
		});
	}

	private void showAddShoppingcartSuccessDialog()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				int layout = ResourceUtil.getLayoutId(getContext(), "add_shoppingcart_success_dialog");
				View view = View.inflate(getContext(), layout, null);
				final ExtendDialog dialog = AndroidUtils.createDialog(getActivity(), view, true, false);
				int id = ResourceUtil.getId(getContext(), "look_shopping");
				View look_shopping = view.findViewById(id);
				look_shopping.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
						MainActivity.backToHomepage(getActivity(), 2);
					}
				});
				id = ResourceUtil.getId(getContext(), "leave");
				View leave = view.findViewById(id);
				leave.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}

	public void PlaceOrder()
	{
		String productHText = (String)productHand.getText();
		try
		{
			productHText = productHText.substring(0, productHText.length() - 1);
			productSize = Integer.parseInt(productHText);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			productSize = 0;
		}
		LogUtil.w(TAG, "productID=" + productID + ",spec_ID=" + erp_id + ",productSize=" + productSize);
		if (TextUtils.isEmpty(erp_id))
		{
			showToast("请选择商品");
			return;
		}
		showLoadingIndicator();
		RequestManager.appendShoppingcart(getActivity(), LoginHelper.getUserKey(getActivity()), productID,
				new String[] { erp_id }, new int[] { productSize }, new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						hideLoadingIndicator();
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							if (MainFragment.instance != null)
								MainFragment.instance.updateShoppingcartNum();
							showToast("加入购物车成功！");
							showAddShoppingcartSuccessDialog();
						}
						else
						{
							showToast(response.error);
						}
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						hideLoadingIndicator();
						showToast("网络错误: " + error.errorCode);
					}
				});
	}

	public class SelectPriceList extends AbsAdapterItem
	{
		private ErpGoods[] mErpGoodsArray;
		// private int index;
		private ScrollListView select_price;
		private BaseAdapter<AbsAdapterItem> selectPrice_Adapter;

		public SelectPriceList(int i, ErpGoods[] mErpGoodsArray)
		{
			// index = i;
			this.mErpGoodsArray = mErpGoodsArray;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onCreateView: " + position + "; " + parent);

			int layout = ResourceUtil.getLayoutId(getContext(), "product_details_list_item");
			View view = View.inflate(getContext(), layout, null);

			int id = ResourceUtil.getId(getContext(), "select_price");
			select_price = (ScrollListView)view.findViewById(id);
			select_price.getListView().setContinueRunInDetachedFromWindow(true);
			select_price.setHorizontalDividerHeight(10);
			selectPrice_Adapter = new BaseAdapter<AbsAdapterItem>();
			select_price.setAdapter(selectPrice_Adapter);
			selectPrice_Adapter.clear();

			for (ErpGoods erp : mErpGoodsArray)
			{
				SelectPrice mSelectPrice = new SelectPrice(position, erp);
				selectPrice_Adapter.addItem(mSelectPrice);
				mList.add(mSelectPrice);
			}

			// if (take_more == 0) {
			// if (index < pages) {
			// for (int j = 0; j < pageNum; j++) {
			// SelectPrice mSelectPrice = new SelectPrice(
			// mErpGoods2[index][j]);
			// selectPrice_Adapter.addItem(mSelectPrice);
			// mList.add(mSelectPrice);
			// }
			// }
			// } else {
			// if (index < pages - 1) {
			// for (int j = 0; j < pageNum; j++) {
			// SelectPrice mSelectPrice = new SelectPrice(
			// mErpGoods2[index][j]);
			// selectPrice_Adapter.addItem(mSelectPrice);
			// mList.add(mSelectPrice);
			// }
			// } else {
			// for (int j = 0; j < take_more; j++) {
			// SelectPrice mSelectPrice = new SelectPrice(
			// mErpGoods2[index][j]);
			// selectPrice_Adapter.addItem(mSelectPrice);
			// mList.add(mSelectPrice);
			// }
			// }
			// }

			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onUpdateView: " + view + "; " + position + "; " + parent);
			select_price.getListView().notifyComputeVisibleContent(false);
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{
		}


		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
		}
	}

	// public SelectPrice msp;
	boolean Oneflag = true;

	public class SelectPrice extends AbsAdapterItem
	{
		private int mParentPosition;
		private ErpGoods mErpGoods;
		public View delectprice;
		public View product_details_selectprice_true;

		// public SelectPriceList mSelectPriceList;
		// public List<AbsAdapterItem> mLista;

		public SelectPrice(int parentPosition, ErpGoods erpGoods)
		{
			mParentPosition = parentPosition;
			this.mErpGoods = erpGoods;
			// mLista = list;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onCreateView: " + mParentPosition + "; " + position + "; " + parent);

			int layout = ResourceUtil.getLayoutId(getContext(), "product_details_selectprice");
			View view = View.inflate(getContext(), layout, null);
			ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
			ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
			ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);

			int selectId = ResourceUtil.getId(getContext(), "delectprice");

			delectprice = view.findViewById(selectId);

			int id = ResourceUtil.getId(getActivity(), "product_details_selectprice_true");
			product_details_selectprice_true = view.findViewById(id);

			if (Oneflag)
			{
				updateProductErpInfo(mErpGoods);
				product_details_selectprice_true.setVisibility(View.VISIBLE);
				delectprice.setSelected(true);
				Oneflag = false;
				erp_id = mErpGoods.erpid;
			}

			id = ResourceUtil.getId(getContext(), "product_main_stone");
			TextView product_main_stone = (TextView)view.findViewById(id);
			if (mErpGoods.p7 != null)
				product_main_stone.setText(mErpGoods.p7 + "ct");

			id = ResourceUtil.getId(getContext(), "product_main_stone_jingdu");
			TextView product_main_stone_jingdu = (TextView)view.findViewById(id);
			if (mErpGoods.p2 != null)
				product_main_stone_jingdu.setText(mErpGoods.p2);

			id = ResourceUtil.getId(getContext(), "product_main_stone_color");
			TextView product_main_stone_color = (TextView)view.findViewById(id);
			if (mErpGoods.p3 != null)
				product_main_stone_color.setText(mErpGoods.p3);

			id = ResourceUtil.getId(getContext(), "product_main_stone_title");
			TextView product_main_stone_title = (TextView)view.findViewById(id);
			if (mErpGoods.p4 != null)
				product_main_stone_title.setText(mErpGoods.p4);

			id = ResourceUtil.getId(getContext(), "product_main_stone_price");
			TextView product_main_stone_price = (TextView)view.findViewById(id);
			if (mErpGoods.p5 != null)
				product_main_stone_price.setText("￥" + mErpGoods.p5);

			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onUpdateView: " + mParentPosition + "; " + view + "; " + position + "; " + parent);

			int id = ResourceUtil.getId(getContext(), "product_main_stone");
			TextView product_main_stone = (TextView)view.findViewById(id);
			if (mErpGoods.p7 != null)
				product_main_stone.setText(mErpGoods.p7 + "ct");

			id = ResourceUtil.getId(getContext(), "product_main_stone_jingdu");
			TextView product_main_stone_jingdu = (TextView)view.findViewById(id);
			if (mErpGoods.p2 != null)
				product_main_stone_jingdu.setText(mErpGoods.p2);

			id = ResourceUtil.getId(getContext(), "product_main_stone_color");
			TextView product_main_stone_color = (TextView)view.findViewById(id);
			if (mErpGoods.p3 != null)
				product_main_stone_color.setText(mErpGoods.p3);

			id = ResourceUtil.getId(getContext(), "product_main_stone_title");
			TextView product_main_stone_title = (TextView)view.findViewById(id);
			if (mErpGoods.p4 != null)
				product_main_stone_title.setText(mErpGoods.p4);

			id = ResourceUtil.getId(getContext(), "product_main_stone_price");
			TextView product_main_stone_price = (TextView)view.findViewById(id);
			if (mErpGoods.p5 != null)
				product_main_stone_price.setText("￥" + mErpGoods.p5);
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{

		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{

		}

		public void onItemClick(View adapterView, ViewGroup parent, View view, int position, long id)
		{
			for (int j = 0; j < mList.size(); j++)
			{
				SelectPrice mspdf = (SelectPrice)mList.get(j);
				if (mspdf.product_details_selectprice_true != null)
					mspdf.product_details_selectprice_true.setVisibility(View.INVISIBLE);
				if (mspdf.delectprice != null)
					mspdf.delectprice.setSelected(false);
			}

			product_details_selectprice_true.setVisibility(View.VISIBLE);
			delectprice.setSelected(true);

			updateProductErpInfo(mErpGoods);
			erp_id = mErpGoods.erpid;
		}
	}
}
