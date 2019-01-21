package com.shiyou.tryapp2.app.product;

import java.util.ArrayList;
import java.util.List;

import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendDialog;
import android.extend.widget.MenuBar;
import android.extend.widget.MenuBar.OnMenuListener;
import android.extend.widget.MenuView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseAdapter;
import android.extend.widget.adapter.BasePagerAdapter;
import android.extend.widget.adapter.ScrollListView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse.ErpDetail;
import com.shiyou.tryapp2.data.response.CoupleRingErpResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse.ErpGoods;

public class CoupleRingsDetailsFragment extends BaseFragment
{
	private String goodsId;

	private CoupleRingDetailResponse mCoupleRingDetailResponse;
	private ErpDetail mCoupleRingErpDetail;
	private float[] weightRange;
	private int[] priceRange;

	private View mDetailRightLayout;

	private TextView productName;
	private TextView productPirce;
	private TextView product_kh;
	private TextView menProductNumber;
	private TextView wmenProductNumber;

	private View men_product_layout;
	private View wmen_product_layout;

	// 商品属性
	private TextView men_product_number; // 货号
	private TextView men_product_deputy_number; // 副石数量
	private TextView men_product_deputy_weight; // 副石重量
	private TextView men_product_main_jingdu; // 主石净度
	private TextView men_product_main_weight; // 主石重量
	private TextView men_product_gold_weight; // 黄金重量
	private TextView men_product_main_color; // 主石颜色
	private TextView men_product_deputy_color; // 副石颜色
	private TextView men_product_deputy_jingdu; // 副石净度
	private TextView men_product_m;
	private TextView men_product_zs;// 证书号

	private TextView menProductHand;
	// private List<String> menHandList = new ArrayList<String>();

	private ViewPager mMenViewPager;
	private BasePagerAdapter<AbsAdapterItem> mMenPagerAdapter;
	private LinearLayout mMenDotContainer;
	private String url;

	private TextView wmen_product_number; // 货号
	private TextView wmen_product_deputy_number; // 副石数量
	private TextView wmen_product_deputy_weight; // 副石重量
	private TextView wmen_product_main_jingdu; // 主石净度
	private TextView wmen_product_main_weight; // 主石重量
	private TextView wmen_product_gold_weight; // 黄金重量
	private TextView wmen_product_main_color; // 主石颜色
	private TextView wmen_product_deputy_color; // 副石颜色
	private TextView wmen_product_deputy_jingdu; // 副石净度
	private TextView wmen_product_m;
	private TextView wmen_product_zs;// 证书号

	private TextView wmenProductHand;
	// private List<String> wmenHandList = new ArrayList<String>();

	private ViewPager mWMenViewPager;
	private BasePagerAdapter<AbsAdapterItem> mWMenPagerAdapter;
	private LinearLayout mWMenDotContainer;

	private final int pageSize = 6;

	public CoupleRingsDetailsFragment()
	{
	}

	public CoupleRingsDetailsFragment(String url)
	{
		this.url=url;
	}

	public CoupleRingsDetailsFragment(CoupleRingDetailResponse response)
	{
		this(response, null, null);
	}

	public CoupleRingsDetailsFragment(CoupleRingDetailResponse response, float[] weightRange, int[] priceRange)
	{
		this.mCoupleRingDetailResponse = response;
		this.goodsId = response.datas.id;
		this.mCoupleRingErpDetail = response.datas.erp;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
	}

	private ErpGoods menErpGoods = null;
	private ErpGoods wmenErpGoods = null;

	public CoupleRingsDetailsFragment(CoupleRingDetailResponse response, ErpDetail mCoupleRingErpDetail,
			ErpGoods menErpGoods, ErpGoods wmenErpGoods)
	{
		this.mCoupleRingDetailResponse = response;
		this.goodsId = response.datas.id;
		this.mCoupleRingErpDetail = mCoupleRingErpDetail;
		this.menErpGoods = menErpGoods;
		this.wmenErpGoods = wmenErpGoods;
	}

	// 选择之后的价格
	// private TextView men_ring_price;
	// private TextView wmen_ring_price;
	// 选择之后的戒托价格
	// private TextView men_ring_up_price;
	// private TextView wmen_ring_up_price;
	// 选择之后的戒托名字
	// private TextView men_ring_up_name;
	// private TextView wmen_ring_up_name;

	// private View men_couple_delectprice, wmen_couple_delectprice;
	// private TextView men_product_main_stone, men_product_main_stone_jingdu, men_product_main_stone_color,
	// men_product_main_stone_title, men_product_main_stone_price;
	// private TextView wmen_product_main_stone, wmen_product_main_stone_jingdu, wmen_product_main_stone_color,
	// wmen_product_main_stone_title, wmen_product_main_stone_price;
	// private View men_rings_select, wmen_rings_select;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreateView: tagname="+mCoupleRingDetailResponse.datas.tagname);
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "couple_rings_details_right_layout");
		mDetailRightLayout = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendLinearLayout)mDetailRightLayout).setInterceptTouchEventToDownward(true);
		ViewTools.adapterAllViewMarginInChildren(mDetailRightLayout, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(mDetailRightLayout, MainActivity.scaled);
		ViewTools.adapterAllTextViewTextSizeInChildren(mDetailRightLayout, MainActivity.fontScaled);

		// productID = mCoupleRingDetailResponse.datas.id;

		int id = ResourceUtil.getId(getContext(), "couple_rings_name");
		productName = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "couple_product_pirce");
		productPirce = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "product_kh");
		product_kh = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "menProductNumber");
		menProductNumber = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "wmenProductNumber");
		wmenProductNumber = (TextView)mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "men_product_layout");
		men_product_layout = mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "wmen_product_layout");
		wmen_product_layout = mDetailRightLayout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "menubar");
		MenuBar menubar = (MenuBar)mDetailRightLayout.findViewById(id);
		menubar.setOnMenuListener(new OnMenuListener()
		{
			@Override
			public void onMenuUnSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
			{
			}

			@Override
			public void onMenuSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
			{
				switch (menuIndex)
				{
					case 0:
						Log.d(TAG, "onMenuSelected: menuIndex="+menuIndex);


						break;
					case 1:
						Log.d(TAG, "onMenuSelected: menuIndex="+menuIndex);

						break;
				}
			}
		});

		menubar.setCurrentMenu(0);

		ensureMenProductLayout();
		ensureWMenProductLayout();

		id = ResourceUtil.getId(getContext(), "place_order");
		View place_order = mDetailRightLayout.findViewById(id);
		place_order.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick() || TextUtils.isEmpty(goodsId))
					return;
				PlaceOrder();
			}
		});

		// id = ResourceUtil.getId(getActivity(), "men_ring_price");
		// men_ring_price = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "wmen_ring_price");
		// wmen_ring_price = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "men_ring_up_price");
		// men_ring_up_price = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "wmen_ring_up_price");
		// wmen_ring_up_price = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "men_ring_up_name");
		// men_ring_up_name = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "wmen_ring_up_name");
		// wmen_ring_up_name = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "men_rings_select");
		// men_rings_select = mDetailRightLayout.findViewById(id);
		// men_rings_select.setOnClickListener(new View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (Utils.isFastClick() || TextUtils.isEmpty(goodsId))
		// return;
		// if (mCoupleRingErpDetail.men != null)
		// {
		// replace(ProductDetailsFragment.instance, ProductDetailsFragment.instance.fragmentC6ID,
		// new CoupleRingsDetailsSelect(mCoupleRingDetailResponse, mCoupleRingErpDetail,
		// mCoupleRingErpDetail.men, wmenErpGoods, true), true);
		// }
		// else
		// {
		// showToast("没有男戒");
		// }
		// }
		// });

		// id = ResourceUtil.getId(getActivity(), "wmen_rings_select");
		// wmen_rings_select = mDetailRightLayout.findViewById(id);
		// wmen_rings_select.setOnClickListener(new View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (Utils.isFastClick() || TextUtils.isEmpty(goodsId))
		// return;
		// if (mCoupleRingErpDetail.wmen != null)
		// {
		// replace(ProductDetailsFragment.instance, ProductDetailsFragment.instance.fragmentC6ID,
		// new CoupleRingsDetailsSelect(mCoupleRingDetailResponse, mCoupleRingErpDetail,
		// mCoupleRingErpDetail.wmen, menErpGoods, false), true);
		// }
		// else
		// {
		// showToast("没有女戒");
		// }
		// }
		// });

		// id = ResourceUtil.getId(getContext(), "men_couple_delectprice");
		// men_couple_delectprice = mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "men_product_main_stone");
		// men_product_main_stone = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "men_product_main_stone_jingdu");
		// men_product_main_stone_jingdu = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "men_product_main_stone_color");
		// men_product_main_stone_color = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "men_product_main_stone_title");
		// men_product_main_stone_title = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "men_product_main_stone_price");
		// men_product_main_stone_price = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "wmen_couple_delectprice");
		// wmen_couple_delectprice = mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "wmen_product_main_stone");
		// wmen_product_main_stone = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "wmen_product_main_stone_jingdu");
		// wmen_product_main_stone_jingdu = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "wmen_product_main_stone_color");
		// wmen_product_main_stone_color = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "wmen_product_main_stone_title");
		// wmen_product_main_stone_title = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "wmen_product_main_stone_price");
		// wmen_product_main_stone_price = (TextView)mDetailRightLayout.findViewById(id);

		// id = ResourceUtil.getId(getContext(), "product_details_selectprice_true");
		// View product_details_selectprice_true = mDetailRightLayout.findViewById(id);
		// product_details_selectprice_true.setOnClickListener(new View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (Utils.isFastClick() || TextUtils.isEmpty(goodsId))
		// return;
		// men_couple_delectprice.setVisibility(View.INVISIBLE);
		// men_rings_select.setVisibility(View.VISIBLE);
		// menErpGoods = null;
		// men_product_main_weight.setText("-");
		// men_product_main_color.setText("-");
		// men_product_main_jingdu.setText("-");
		// men_product_m.setText("-");
		// men_product_number.setText("-");
		// men_product_zs.setText("-");
		// men_product_gold_weight.setText("-");
		// men_ring_price.setText("-");
		// }
		// });

		// id = ResourceUtil.getId(getContext(), "wmen_product_details_selectprice_del");
		// View wmen_product_details_selectprice_del = mDetailRightLayout.findViewById(id);
		// wmen_product_details_selectprice_del.setOnClickListener(new View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (Utils.isFastClick() || TextUtils.isEmpty(goodsId))
		// return;
		// wmen_couple_delectprice.setVisibility(View.INVISIBLE);
		// wmen_rings_select.setVisibility(View.VISIBLE);
		// wmenErpGoods = null;
		// wmen_product_main_weight.setText("-");
		// wmen_product_main_color.setText("-");
		// wmen_product_main_jingdu.setText("-");
		// wmen_product_m.setText("-");
		// wmen_product_number.setText("-");
		// wmen_product_zs.setText("-");
		// wmen_product_gold_weight.setText("-");
		// wmen_ring_price.setText("-");
		// }
		// });

		if (mCoupleRingDetailResponse != null)
			updateCoupleRingDetailsResponse(mCoupleRingDetailResponse, weightRange, priceRange);
		replace(getActivity(),ResourceUtil.getId(getContext(),"product_couple_details_attribute"),new MainWebFragment(url,0),false);
		return mDetailRightLayout;
	}

	private void ensureMenProductLayout()
	{
		int id = ResourceUtil.getId(getContext(), "product_hand");
		menProductHand = (TextView)men_product_layout.findViewById(id);
		menProductHand.setText("12号");
		// for (int i = 8; i < 25; i++)
		// {
		// menHandList.add(i + "号");
		// }
		// menSpinerPopWindowHand = new SpinerPopWindow(getContext());
		// menSpinerPopWindowHand.refreshData(menHandList, 0);
		// if (menErpGoods != null)
		// {
		// menProductHand.setText(menErpGoods.p128 + "号");
		// }

		// menProductHand.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (AndroidUtils.isFastClick() || TextUtils.isEmpty(goodsId))
		// return;
		// showSpinWindow(menSpinerPopWindowHand, menProductHand);
		// }
		// });
		// menSpinerPopWindowHand.setItemListener(new IOnItemSelectListener()
		// {
		// @Override
		// public void onItemClick(int pos)
		// {
		// setMaterialHero(pos, menHandList, menProductHand);
		// }
		// });

		id = ResourceUtil.getId(getContext(), "product_m");
		men_product_m = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_number");
		men_product_number = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_zs");
		men_product_zs = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_number");
		men_product_deputy_number = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_weight");
		men_product_deputy_weight = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_jingdu");
		men_product_main_jingdu = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_weight");
		men_product_main_weight = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_gold_weight");
		men_product_gold_weight = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_color");
		men_product_main_color = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_color");
		men_product_deputy_color = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_jingdu");
		men_product_deputy_jingdu = (TextView)men_product_layout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "select_item");
		mMenViewPager = (ViewPager)men_product_layout.findViewById(id);
		mMenPagerAdapter = new BasePagerAdapter<AbsAdapterItem>();
		mMenViewPager.setAdapter(mMenPagerAdapter);
		mMenViewPager.addOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				setMenSelectdDot(position);
				mMenPagerAdapter.notifyPageSelected(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}
		});
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
		mMenDotContainer = (LinearLayout)men_product_layout.findViewById(id);
	}

	private void ensureWMenProductLayout()
	{
		int id = ResourceUtil.getId(getContext(), "product_hand");
		wmenProductHand = (TextView)wmen_product_layout.findViewById(id);
		wmenProductHand.setText("12号");
		// for (int i = 8; i < 25; i++)
		// {
		// wmenHandList.add(i + "号");
		// }
		// wmenSpinerPopWindowHand = new SpinerPopWindow(getContext());
		// wmenSpinerPopWindowHand.refreshData(wmenHandList, 0);

		if (wmenErpGoods != null)
		{
			wmenProductHand.setText(wmenErpGoods.p128 + "号");
		}

		// wmenProductHand.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (AndroidUtils.isFastClick() || TextUtils.isEmpty(goodsId))
		// return;
		// showSpinWindow(wmenSpinerPopWindowHand, wmenProductHand);
		// }
		// });
		// wmenSpinerPopWindowHand.setItemListener(new IOnItemSelectListener()
		// {
		// @Override
		// public void onItemClick(int pos)
		// {
		// setMaterialHero(pos, wmenHandList, wmenProductHand);
		// }
		// });

		id = ResourceUtil.getId(getContext(), "product_m");
		wmen_product_m = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_number");
		wmen_product_number = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_zs");
		wmen_product_zs = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_number");
		wmen_product_deputy_number = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_weight");
		wmen_product_deputy_weight = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_jingdu");
		wmen_product_main_jingdu = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_weight");
		wmen_product_main_weight = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_gold_weight");
		wmen_product_gold_weight = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_color");
		wmen_product_main_color = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_color");
		wmen_product_deputy_color = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_jingdu");
		wmen_product_deputy_jingdu = (TextView)wmen_product_layout.findViewById(id);

		id = ResourceUtil.getId(getContext(), "select_item");
		mWMenViewPager = (ViewPager)wmen_product_layout.findViewById(id);
		mWMenPagerAdapter = new BasePagerAdapter<AbsAdapterItem>();
		mWMenViewPager.setAdapter(mWMenPagerAdapter);
		mWMenViewPager.addOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				setWMenSelectdDot(position);
				mWMenPagerAdapter.notifyPageSelected(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}
		});
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
		mWMenDotContainer = (LinearLayout)wmen_product_layout.findViewById(id);
	}

	public void updateCoupleRingDetailsResponse(CoupleRingDetailResponse response, float[] weightRange, int[] priceRange)
	{
		this.mCoupleRingDetailResponse = response;
		this.goodsId = response.datas.id;
		this.mCoupleRingErpDetail = response.datas.erp;
		this.weightRange = weightRange;
		this.priceRange = priceRange;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached())
					return;
				if (isResumed())
				{
					if (mCoupleRingDetailResponse.datas.title != null)
						productName.setText(mCoupleRingDetailResponse.datas.title);
					if (mCoupleRingDetailResponse.datas.sku != null)
						product_kh.setText(mCoupleRingDetailResponse.datas.sku);
					if (mCoupleRingDetailResponse.datas.m_sku != null)
						menProductNumber.setText(mCoupleRingDetailResponse.datas.m_sku);
					if (mCoupleRingDetailResponse.datas.w_sku != null)
						wmenProductNumber.setText(mCoupleRingDetailResponse.datas.w_sku);

					// PropertiesPane();
					loadCoupleRingErpDetail();
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

	private void loadCoupleRingErpDetail()
	{
		// if (mCoupleRingErpDetail == null) {
		RequestManager.loadCoupleRingErp(getContext(), LoginHelper.getUserKey(getContext()), goodsId,
				new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
					{
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							CoupleRingErpResponse creResponse = (CoupleRingErpResponse)response;
							mCoupleRingErpDetail = creResponse.datas.erp;
							if (mCoupleRingErpDetail != null)
							{
								AndroidUtils.MainHandler.post(new Runnable()
								{
									@Override
									public void run()
									{
										if (isDetached() || getContext() == null)
											return;
										if (mCoupleRingErpDetail.men != null && mCoupleRingErpDetail.men.length > 0)
										{
											List<ErpGoods> erpList = new ArrayList<ErpGoods>();
											for (ErpGoods erp : mCoupleRingErpDetail.men)
											{
												if (checkNeedAdd(erp))
													erpList.add(erp);
											}
											ErpGoods[] erpArray = new ErpGoods[erpList.size()];
											erpArray = erpList.toArray(erpArray);
											if (erpArray.length > 0)
											{
												updateMenProductErpInfo(erpArray[0]);
												ensureMenErpList(erpArray);
											}
										}
										if (mCoupleRingErpDetail.wmen != null && mCoupleRingErpDetail.wmen.length > 0)
										{
											List<ErpGoods> erpList = new ArrayList<ErpGoods>();
											for (ErpGoods erp : mCoupleRingErpDetail.wmen)
											{
												if (checkNeedAdd(erp))
													erpList.add(erp);
											}
											ErpGoods[] erpArray = new ErpGoods[erpList.size()];
											erpArray = erpList.toArray(erpArray);
											if (erpArray.length > 0)
											{
												updateWMenProductErpInfo(erpArray[0]);
												ensureWMenErpList(erpArray);
											}
										}
									}
								});
							}
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
		// }
	}

	private void updateMenProductErpInfo(ErpGoods erp)
	{
		menErpGoods = erp;
		if (erp.p1 != null)
			men_product_gold_weight.setText(erp.p1);
		else
			men_product_gold_weight.setText("-");

		if (erp.p7 != null)
			men_product_main_weight.setText(erp.p7);
		else
			men_product_main_weight.setText("-");

		if (erp.p3 != null)
			men_product_main_color.setText(erp.p3);
		else
			men_product_main_color.setText("-");

		if (erp.p2 != null)
			men_product_main_jingdu.setText(erp.p2);
		else
			men_product_main_jingdu.setText("-");

		if (erp.p4 != null)
			men_product_m.setText(erp.p4);
		else
			men_product_m.setText("-");

		if (erp.p8 != null)
			men_product_deputy_number.setText(erp.p8);
		else
			men_product_deputy_number.setText("-");

		if (erp.p9 != null)
			men_product_deputy_weight.setText(erp.p9);
		else
			men_product_deputy_weight.setText("-");

		if (erp.p128 != null)
			menProductHand.setText(erp.p128 + "号");
		else
			menProductHand.setText("12号");

		if (erp.erpid != null)
			men_product_number.setText(erp.erpid);
		else
			men_product_number.setText("-");

		if (erp.zs != null)
			men_product_zs.setText(erp.zs);
		else
			men_product_zs.setText("-");

		float price = 0;
		if (erp.p5 != null)
			price = Float.parseFloat(erp.p5);
		if (wmenErpGoods != null && wmenErpGoods.p5 != null)
			price += Float.parseFloat(wmenErpGoods.p5);

		productPirce.setText("￥" + (int)price);
	}

	private void updateWMenProductErpInfo(ErpGoods erp)
	{
		wmenErpGoods = erp;
		if (erp.p1 != null)
			wmen_product_gold_weight.setText(erp.p1);
		else
			wmen_product_gold_weight.setText("-");

		if (erp.p7 != null)
			wmen_product_main_weight.setText(erp.p7);
		else
			wmen_product_main_weight.setText("-");

		if (erp.p3 != null)
			wmen_product_main_color.setText(erp.p3);
		else
			wmen_product_main_color.setText("-");

		if (erp.p2 != null)
			wmen_product_main_jingdu.setText(erp.p2);
		else
			wmen_product_main_jingdu.setText("-");

		if (erp.p4 != null)
			wmen_product_m.setText(erp.p4);
		else
			wmen_product_m.setText("-");

		if (erp.p8 != null)
			wmen_product_deputy_number.setText(erp.p8);
		else
			wmen_product_deputy_number.setText("-");

		if (erp.p9 != null)
			wmen_product_deputy_weight.setText(erp.p9);
		else
			wmen_product_deputy_weight.setText("-");

		if (erp.p128 != null)
			wmenProductHand.setText(erp.p128 + "号");
		else
			wmenProductHand.setText("12号");

		if (erp.erpid != null)
			wmen_product_number.setText(erp.erpid);
		else
			wmen_product_number.setText("-");

		if (erp.zs != null)
			wmen_product_zs.setText(erp.zs);
		else
			wmen_product_zs.setText("-");

		float price = 0;
		if (erp.p5 != null)
			price = Float.parseFloat(erp.p5);
		if (menErpGoods != null && menErpGoods.p5 != null)
			price += Float.parseFloat(menErpGoods.p5);

		productPirce.setText("￥" + (int)price);
	}

	float menPrice = 0f;
	float wmenPrice = 0f;
	float menallPrice = 0f;
	float wmenallPrice = 0f;

	boolean mcz = false;

	// public void PropertiesPane()
	// {
	// if (mCoupleRingDetailResponse.datas.sku != null)
	// {
	// product_kh.setText(mCoupleRingDetailResponse.datas.sku);
	// }
	// if (mCoupleRingDetailResponse.datas.param != null && mCoupleRingDetailResponse.datas.param.length > 0)
	// {
	// if (mCoupleRingDetailResponse.datas.param[0].value != null)
	// {
	// men_product_deputy_number.setText(mCoupleRingDetailResponse.datas.param[0].value);
	// wmen_product_deputy_number.setText(mCoupleRingDetailResponse.datas.param[0].value);
	// }
	// if (mCoupleRingDetailResponse.datas.param[1].value != null)
	// {
	// men_product_deputy_weight.setText(mCoupleRingDetailResponse.datas.param[1].value);
	// wmen_product_deputy_weight.setText(mCoupleRingDetailResponse.datas.param[1].value);
	// }
	// if (mCoupleRingDetailResponse.datas.param[4].value != null) {
	// men_product_gold_weight
	// .setText(mCoupleRingDetailResponse.datas.param[4].value);
	// wmen_product_gold_weight
	// .setText(mCoupleRingDetailResponse.datas.param[4].value);
	// }
	// if (mCoupleRingDetailResponse.datas.param[2].value != null)
	// {
	// wmen_ring_up_price.setText("￥" + mCoupleRingDetailResponse.datas.param[2].value);
	// men_ring_up_price.setText("￥" + mCoupleRingDetailResponse.datas.param[2].value);
	// menallPrice = Float.parseFloat(mCoupleRingDetailResponse.datas.param[2].value);
	// }
	// }

	// if (menErpGoods == null)
	// {
	// men_couple_delectprice.setVisibility(View.INVISIBLE);
	// men_rings_select.setVisibility(View.VISIBLE);
	// if (mCoupleRingDetailResponse.datas.erp.men != null
	// && mCoupleRingDetailResponse.datas.erp.men.length > 0) {
	// men_product_main_weight
	// .setText(mCoupleRingDetailResponse.datas.erp.men[0].p7);
	// men_product_main_color
	// .setText(mCoupleRingDetailResponse.datas.erp.men[0].p2);
	// men_product_main_jingdu
	// .setText(mCoupleRingDetailResponse.datas.erp.men[0].p3);
	// men_product_m
	// .setText(mCoupleRingDetailResponse.datas.erp.men[0].p4);
	// }
	// }
	// else
	// {
	// men_couple_delectprice.setVisibility(View.VISIBLE);
	// men_rings_select.setVisibility(View.GONE);
	// men_product_main_weight.setText(menErpGoods.p7);
	// men_product_main_color.setText(menErpGoods.p2);
	// men_product_main_jingdu.setText(menErpGoods.p3);
	// men_product_m.setText(menErpGoods.p4);
	// men_product_gold_weight.setText(menErpGoods.p1);

	// men_ring_up_name.setText(menErpGoods.p4 + "戒托价：");

	// if (menErpGoods.p4.equals("18K金"))
	// {
	// if (mCoupleRingDetailResponse.datas.param[2].value != null)
	// {
	// men_ring_up_price.setText("￥" + mCoupleRingDetailResponse.datas.param[2].value);
	// menallPrice = Float.parseFloat(mCoupleRingDetailResponse.datas.param[2].value);
	// }
	// }
	// else
	// {
	// if (mCoupleRingDetailResponse.datas.param[3].value != null)
	// {
	// men_ring_up_price.setText("￥" + mCoupleRingDetailResponse.datas.param[3].value);
	// menallPrice = Float.parseFloat(mCoupleRingDetailResponse.datas.param[3].value);
	// }
	// }

	// men_product_number.setText(menErpGoods.erpid);
	// if (menErpGoods.zs != null)
	// men_product_zs.setText(menErpGoods.zs);
	// else
	// men_product_zs.setText("-");
	// men_product_main_stone.setText(menErpGoods.p7 + "ct");
	// men_product_main_stone_jingdu.setText(menErpGoods.p2);
	// men_product_main_stone_color.setText(menErpGoods.p3);
	// men_product_main_stone_title.setText(menErpGoods.p4);
	// men_product_main_stone_price.setText("￥" + menErpGoods.p5);
	// if (menErpGoods.p5 != null)
	// menPrice = Float.parseFloat(menErpGoods.p5);

	// menProductHand.setText(menErpGoods.p128 + "号");
	// }

	// if (wmenErpGoods == null)
	// {
	// wmen_couple_delectprice.setVisibility(View.INVISIBLE);
	// wmen_rings_select.setVisibility(View.VISIBLE);
	// if (mCoupleRingDetailResponse.datas.erp.wmen != null
	// && mCoupleRingDetailResponse.datas.erp.wmen.length > 0) {
	// wmen_product_main_weight
	// .setText(mCoupleRingDetailResponse.datas.erp.wmen[0].p7);
	// wmen_product_main_color
	// .setText(mCoupleRingDetailResponse.datas.erp.wmen[0].p2);
	// wmen_product_main_jingdu
	// .setText(mCoupleRingDetailResponse.datas.erp.wmen[0].p3);
	// wmen_product_m
	// .setText(mCoupleRingDetailResponse.datas.erp.wmen[0].p4);
	// }
	// }
	// else
	// {
	// wmen_couple_delectprice.setVisibility(View.VISIBLE);
	// wmen_rings_select.setVisibility(View.GONE);
	// wmen_product_main_weight.setText(wmenErpGoods.p7);
	// wmen_product_main_color.setText(wmenErpGoods.p2);
	// wmen_product_main_jingdu.setText(wmenErpGoods.p3);
	// wmen_product_m.setText(wmenErpGoods.p4);
	// wmen_product_number.setText(wmenErpGoods.erpid);
	// if (wmenErpGoods.zs != null)
	// wmen_product_zs.setText(wmenErpGoods.zs);
	// else
	// wmen_product_zs.setText("-");
	// wmen_product_gold_weight.setText(wmenErpGoods.p1);

	// wmen_ring_up_name.setText(wmenErpGoods.p4 + "戒托价：");
	//
	// if (wmenErpGoods.p4.equals("18K金"))
	// {
	// if (mCoupleRingDetailResponse.datas.param[2].value != null)
	// {
	// wmen_ring_up_price.setText("￥" + mCoupleRingDetailResponse.datas.param[2].value);
	// menallPrice = Float.parseFloat(mCoupleRingDetailResponse.datas.param[2].value);
	// }
	// }
	// else
	// {
	// if (mCoupleRingDetailResponse.datas.param[3].value != null)
	// {
	// wmen_ring_up_price.setText("￥" + mCoupleRingDetailResponse.datas.param[3].value);
	// menallPrice = Float.parseFloat(mCoupleRingDetailResponse.datas.param[3].value);
	// }
	// }

	// wmen_product_main_stone.setText(wmenErpGoods.p7);
	// wmen_product_main_stone_jingdu.setText(wmenErpGoods.p2);
	// wmen_product_main_stone_color.setText(wmenErpGoods.p3);
	// wmen_product_main_stone_title.setText(wmenErpGoods.p4);
	// wmen_product_main_stone_price.setText("￥" + wmenErpGoods.p5);
	// if (wmenErpGoods.p5 != null)
	// wmenPrice = Float.parseFloat(wmenErpGoods.p5);

	// wmenProductHand.setText(wmenErpGoods.p128 + "号");
	// }

	// float allss = menPrice + wmenPrice;
	// men_ring_price.setText("￥" + menPrice);
	// wmen_ring_price.setText("￥" + wmenPrice);
	// productPirce.setText("￥" + allss);
	// }

	// private SpinerPopWindow menSpinerPopWindowHand;
	// private SpinerPopWindow wmenSpinerPopWindowHand;

	// private void showSpinWindow(SpinerPopWindow mSpinerPopWindow, TextView mProduct)
	// {
	// Log.e("", "showSpinWindow" + mProduct.getText());
	// mSpinerPopWindow.setWidth(mProduct.getWidth());
	// mSpinerPopWindow.showAsDropDown(mProduct);
	// }

	// private void setMaterialHero(int pos, List<String> mList, TextView mProduct)
	// {
	// if (pos >= 0 && pos <= mList.size())
	// {
	// String value = mList.get(pos);
	// mProduct.setText(value);
	// }
	// }

	List<AbsAdapterItem> mMenList;

	private void ensureMenErpList(ErpGoods[] erp)
	{
		if (isDetached() || getContext() == null)
			return;
		mMenPagerAdapter.clear();
		int allnum = erp.length;
		int pageCount = (int)Math.ceil((double)allnum / (double)pageSize);
		int more = allnum % pageSize;
		mMenList = new ArrayList<AbsAdapterItem>();
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
				array[j] = erp[page * size + j];
			}
			SelectPriceList mSelectPriceList = new SelectPriceList(page, array, false);
			mMenPagerAdapter.addItem(mSelectPriceList);
		}
		ensureMenDots(pageCount);
	}

	List<AbsAdapterItem> mWMenList;

	private void ensureWMenErpList(ErpGoods[] erp)
	{
		if (isDetached() || getContext() == null)
			return;
		mWMenPagerAdapter.clear();
		int allnum = erp.length;
		int pageCount = (int)Math.ceil((double)allnum / (double)pageSize);
		int more = allnum % pageSize;
		mWMenList = new ArrayList<AbsAdapterItem>();
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
				array[j] = erp[page * size + j];
			}
			SelectPriceList mSelectPriceList = new SelectPriceList(page, array, true);
			mWMenPagerAdapter.addItem(mSelectPriceList);
		}
		ensureWMenDots(pageCount);
	}

	private void setMenSelectdDot(final int index)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached() || getContext() == null)
					return;
				LogUtil.d(TAG, "setMenSelectdDot: " + index);
				int dotFocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg");
				int dotUnfocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg1");
				int count = mMenDotContainer.getChildCount();
				for (int i = 0; i < count; i++)
				{
					ImageView child = (ImageView)mMenDotContainer.getChildAt(i);
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

	private void ensureMenDots(final int length)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached() || getContext() == null)
					return;
				mMenDotContainer.removeAllViews();
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
					mMenDotContainer.addView(view);
				}
				setMenSelectdDot(0);
			}
		});
	}

	private void setWMenSelectdDot(final int index)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached() || getContext() == null)
					return;
				LogUtil.d(TAG, "setWMenSelectdDot: " + index);
				int dotFocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg");
				int dotUnfocusId = ResourceUtil.getDrawableId(getContext(), "dot_container_bg1");
				int count = mWMenDotContainer.getChildCount();
				for (int i = 0; i < count; i++)
				{
					ImageView child = (ImageView)mWMenDotContainer.getChildAt(i);
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

	private void ensureWMenDots(final int length)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isDetached() || getContext() == null)
					return;
				mWMenDotContainer.removeAllViews();
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
					mWMenDotContainer.addView(view);
				}
				setWMenSelectdDot(0);
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

	// String productID = null;

	int[] productSize = null;
	String[] erpId = null;

	public void PlaceOrder()
	{
		if (menErpGoods != null || wmenErpGoods != null)
		{
			String menproductHText = (String)menProductHand.getText();
			menproductHText = menproductHText.substring(0, menproductHText.length() - 1);
			String wmenproductHText = (String)wmenProductHand.getText();
			wmenproductHText = wmenproductHText.substring(0, wmenproductHText.length() - 1);

			if (menErpGoods != null && wmenErpGoods != null)
			{
				erpId = new String[2];
				productSize = new int[2];
				erpId[0] = menErpGoods.erpid;
				erpId[1] = wmenErpGoods.erpid;
				productSize[0] = Integer.parseInt(menproductHText);
				productSize[1] = Integer.parseInt(wmenproductHText);
			}
			else
			{
				erpId = new String[1];
				productSize = new int[1];
				if (wmenErpGoods != null)
				{
					erpId[0] = wmenErpGoods.erpid;
					productSize[0] = Integer.parseInt(wmenproductHText);
				}
				if (menErpGoods != null)
				{
					erpId[0] = menErpGoods.erpid;
					productSize[0] = Integer.parseInt(menproductHText);

				}
			}

			RequestManager.appendShoppingcart(getContext(), LoginHelper.getUserKey(getActivity()), goodsId, erpId,
					productSize, new RequestCallback()
					{
						@Override
						public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
						{
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
							showToast("网络错误: " + error.errorCode);
						}
					});
		}
		else
		{
			showToast("请选择商品");
		}
	}

	public class SelectPriceList extends AbsAdapterItem
	{
		private ErpGoods[] mErpGoodsArray;
		private boolean isWMen = false;
		// private int index;
		private ScrollListView select_price;
		private BaseAdapter<AbsAdapterItem> selectPrice_Adapter;

		public SelectPriceList(int i, ErpGoods[] mErpGoodsArray, boolean isWMen)
		{
			// index = i;
			this.mErpGoodsArray = mErpGoodsArray;
			this.isWMen = isWMen;
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
				SelectPrice mSelectPrice = new SelectPrice(position, erp, isWMen);
				selectPrice_Adapter.addItem(mSelectPrice);
				if (!isWMen)
					mMenList.add(mSelectPrice);
				else
					mWMenList.add(mSelectPrice);
			}

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
	// boolean Oneflag = true;

	public class SelectPrice extends AbsAdapterItem
	{
		private int mParentPosition;
		private ErpGoods mErpGoods;
		private boolean isWMen = false;

		View delectprice;
		View product_details_selectprice_true;

		// public SelectPriceList mSelectPriceList;
		// public List<AbsAdapterItem> mLista;

		public SelectPrice(int parentPosition, ErpGoods erpGoods, boolean isWMen)
		{
			mParentPosition = parentPosition;
			this.mErpGoods = erpGoods;
			this.isWMen = isWMen;
			// mLista = list;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onCreateView: " + mParentPosition + "; " + isWMen + "; " + position + "; " + parent);

			int layout = ResourceUtil.getLayoutId(getContext(), "product_details_selectprice");
			View view = View.inflate(getContext(), layout, null);
			ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
			ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
			ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);

			int selectId = ResourceUtil.getId(getContext(), "delectprice");
			delectprice = view.findViewById(selectId);

			int id = ResourceUtil.getId(getActivity(), "product_details_selectprice_true");
			product_details_selectprice_true = view.findViewById(id);

			// if (Oneflag)
			// {
			// updateProductErpInfo(mErpGoods);
			// product_details_selectprice_true.setVisibility(View.VISIBLE);
			// delectprice.setSelected(true);
			// Oneflag = false;
			// erp_id = mErpGoods.erpid;
			// }
			if (!isWMen)
			{
				if (menErpGoods != null && menErpGoods.equals(mErpGoods))
				{
					product_details_selectprice_true.setVisibility(View.VISIBLE);
					delectprice.setSelected(true);
				}
			}
			else
			{
				if (wmenErpGoods != null && wmenErpGoods.equals(mErpGoods))
				{
					product_details_selectprice_true.setVisibility(View.VISIBLE);
					delectprice.setSelected(true);
				}
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
			LogUtil.v(TAG, "onUpdateView: " + mParentPosition + "; " + isWMen + "; " + view + "; " + position + "; "
					+ parent);

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
			if (!isWMen)
				for (int j = 0; j < mMenList.size(); j++)
				{
					SelectPrice mspdf = (SelectPrice)mMenList.get(j);
					if (mspdf.product_details_selectprice_true != null)
						mspdf.product_details_selectprice_true.setVisibility(View.INVISIBLE);
					if (mspdf.delectprice != null)
						mspdf.delectprice.setSelected(false);
				}
			else
				for (int j = 0; j < mWMenList.size(); j++)
				{
					SelectPrice mspdf = (SelectPrice)mWMenList.get(j);
					if (mspdf.product_details_selectprice_true != null)
						mspdf.product_details_selectprice_true.setVisibility(View.INVISIBLE);
					if (mspdf.delectprice != null)
						mspdf.delectprice.setSelected(false);
				}

			product_details_selectprice_true.setVisibility(View.VISIBLE);
			delectprice.setSelected(true);

			if (!isWMen)
				updateMenProductErpInfo(mErpGoods);
			else
				updateWMenProductErpInfo(mErpGoods);
		}
	}
}