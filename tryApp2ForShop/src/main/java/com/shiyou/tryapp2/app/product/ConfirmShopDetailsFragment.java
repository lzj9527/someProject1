package com.shiyou.tryapp2.app.product;

import java.util.List;

import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.loader.Loader.CacheMode;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendDialog;
import android.extend.widget.ExtendImageView;
import android.extend.widget.ExtendLinearLayout;
import android.extend.widget.FlowLayout;
import android.extend.widget.MenuBar;
import android.extend.widget.MenuBar.OnMenuListener;
import android.extend.widget.MenuView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BasePagerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.GIAData;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GIADeliveryResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;

public class ConfirmShopDetailsFragment extends BaseFragment
{
	private String goodsId;
	private String giaJson;
	private GIAData giaData;
	private int customization = 0;

	private View ConfirmDetails;

	private GoodsDetailResponse mGDResponse;

	private TextView productName;// 商品名字
	private TextView productPirce;// 商品价格

	private TextView product_type;
	private TextView ringPriceText; // 戒托价格

	private TextView ringsPriceText; // 裸钻价格

	// 戒托属性
	private TextView product_number; // 货号
	private TextView product_deputy_number; // 副石数量
	private TextView product_deputy_weight; // 副石重量
	private TextView product_main_jingdu; // 主石净度
	private TextView product_main_weight; // 主石重量
	private TextView product_gold_weight; // 黄金重量
	private TextView product_main_color; // 主石颜色

	// 裸钻属性
	private TextView ring_sn; // 货号
	private TextView ring_style; // 形状
	private TextView ring_weight; // 重量
	private TextView ring_color; // 颜色
	private TextView ring_clarity; // 净度
	private TextView ring_cut; // 切工
	private TextView ring_polish; // 抛光
	private TextView ring_symmetry; // 对称
	private TextView ring_fluorescence; // 荧光
	private TextView ring_certtype; // 证书
	private TextView ring_certno; // 证书号

	// private TextView product_kh;
	private TextView send_time;

	private MenuBar product_material_menubar;
	private View ringsize_select_layout;
	private MenuBar ringsize_menubar;
	private List<String> ringsizeList;

	private TextView lettering;

	ProductImagePopupWindow mPopupWindow;
	private ViewPager mViewPager;
	private BasePagerAdapter<AbsAdapterItem> mPagerAdapter;
	private LinearLayout mDotContainer;
	View arrow_left;
	View arrow_right;

	public ConfirmShopDetailsFragment(String goodsId, String giaJson)
	{
		this.goodsId = goodsId;
		this.giaJson = giaJson;
	}
	
	public ConfirmShopDetailsFragment(String goodsId, String giaJson, int customization)
	{
		this.goodsId = goodsId;
		this.giaJson = giaJson;
		this.customization = customization;
	}

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

	private void ensureDots(final int count)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mDotContainer.removeAllViews();
				for (int i = 0; i < count; i++)
				{
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
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "confirm_shop_details_layout");
		ConfirmDetails = super.onCreateView(inflater, container, savedInstanceState);
		((ExtendLinearLayout)ConfirmDetails).setInterceptTouchEventToDownward(true);
		ViewTools.adapterAllViewMarginInChildren(ConfirmDetails, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(ConfirmDetails, MainActivity.scaled);
		ViewTools.adapterAllTextViewTextSizeInChildren(ConfirmDetails, MainActivity.fontScaled);

		ensureDetailsMiddle();
		ensureDetailsRing();
		giaParse();
//		ProductDetailsData();

		return ConfirmDetails;
	}

	public void giaParse()
	{
		// gia = jiaJson;
		giaData = GIAData.fromJson(giaJson);
		giaData.printData(TAG, 0);

		ring_sn.setText(giaData.sn); // 货号
		ring_style.setText(giaData.style); // 形状
		ring_weight.setText(giaData.weight); // 重量
		ring_color.setText(giaData.color); // 颜色
		ring_clarity.setText(giaData.clarity); // 净度
		ring_cut.setText(giaData.cut); // 切工
		ring_polish.setText(giaData.polish); // 抛光
		ring_symmetry.setText(giaData.symmetry); // 对称
		ring_fluorescence.setText(giaData.fluorescence); // 荧光
		ring_certtype.setText(giaData.certtype); // 证书
		ring_certno.setText(giaData.certno); // 证书号
		ringsPriceText.setText("￥" + giaData.price); // 价格

		giapirce = giaData.price;
		giasrcprice = giaData.changeprice;
	}

	private void ensureDetailsMiddle()
	{
		int id = ResourceUtil.getId(getActivity(), "middle_back");
		View middle_back = ConfirmDetails.findViewById(id);
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

		// id = ResourceUtil.getId(getActivity(), "middle_image");
		// productImage = (ExtendImageView) ConfirmDetails.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "selected_list");
		// selectedList = (HorizontalScrollListView) ConfirmDetails
		// .findViewById(id);
		// selectedListAdapter = new BaseAdapter<AbsAdapterItem>();
		// selectedList.setAdapter(selectedListAdapter);
		// selectedListAdapter.clear();

		id = ResourceUtil.getId(getContext(), "select_item");
		mViewPager = (ViewPager)ConfirmDetails.findViewById(id);
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
				mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				LayoutParams params = mViewPager.getLayoutParams();
				params.width = width;
				params.height = height;
				mViewPager.setLayoutParams(params);
			}
		});

		id = ResourceUtil.getId(getContext(), "dot_container");
		mDotContainer = (LinearLayout)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getContext(), "arrow_left");
		arrow_left = ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getContext(), "arrow_right");
		arrow_right = ConfirmDetails.findViewById(id);
	}

	public void ensureDetailsRing()
	{
		int id = ResourceUtil.getId(getActivity(), "rings_name");
		productName = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_pirce");
		productPirce = (TextView)ConfirmDetails.findViewById(id);

		// id = ResourceUtil.getId(getActivity(), "product_kh");
		// product_kh = (TextView) ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "send_time");
		send_time = (TextView)ConfirmDetails.findViewById(id);
		getGIADelivery();

		id = ResourceUtil.getId(getContext(), "product_type");
		product_type = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_price_text");
		ringPriceText = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "rings_price_text");
		ringsPriceText = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_sn");
		ring_sn = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_style");
		ring_style = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_weight");
		ring_weight = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_color");
		ring_color = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_clarity");
		ring_clarity = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_cut");
		ring_cut = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_polish");
		ring_polish = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_symmetry");
		ring_symmetry = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_fluorescence");
		ring_fluorescence = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_certtype");
		ring_certtype = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ring_certno");
		ring_certno = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_number");
		product_number = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_number");
		product_deputy_number = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_deputy_weight");
		product_deputy_weight = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_jingdu");
		product_main_jingdu = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_weight");
		product_main_weight = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_gold_weight");
		product_gold_weight = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_main_color");
		product_main_color = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "product_material_menubar");
		product_material_menubar = (MenuBar)ConfirmDetails.findViewById(id);
		product_material_menubar.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
					int oldRight, int oldBottom)
			{
				if (product_material_menubar.getWidth() == 0 || product_material_menubar.getHeight() == 0)
					return;
				product_material_menubar.removeOnLayoutChangeListener(this);
				int width = product_material_menubar.getWidth();
				MenuView menu = product_material_menubar.getMenu(1);
				MarginLayoutParams mlParams = (MarginLayoutParams)menu.getLayoutParams();
				width = (width - mlParams.leftMargin) / 2;
				int count = product_material_menubar.getMenuCount();
				for (int i = 0; i < count; i++)
				{
					menu = product_material_menubar.getMenu(i);
					LayoutParams params = menu.getLayoutParams();
					params.width = width;
					menu.setLayoutParams(params);
				}
			}
		});

		id = ResourceUtil.getId(getContext(), "ringsize_select_layout");
		ringsize_select_layout = ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "ringsize_menubar");
		ringsize_menubar = (MenuBar)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getContext(), "lettering");
		lettering = (TextView)ConfirmDetails.findViewById(id);

		id = ResourceUtil.getId(getActivity(), "place_order");
		View place_order = ConfirmDetails.findViewById(id);
		place_order.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				PlaceOrder();
			}
		});
	}

	private void getGIADelivery()
	{
		RequestManager.getGIADelivery(getContext(), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					final GIADeliveryResponse gdResponse = (GIADeliveryResponse)response;
					if (gdResponse.datas != null && !TextUtils.isEmpty(gdResponse.datas.sendtime))
					{
						AndroidUtils.MainHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								send_time.setText(gdResponse.datas.sendtime);
							}
						});
					}
				}
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
			}
		});
	}

//	public void ProductDetailsData()
//	{
//		showLoadingIndicator();
//		RequestManager.loadGoodsDetail(getContext(), LoginHelper.getUserKey(getActivity()), goodsId,
//				new RequestCallback()
//				{
//					@Override
//					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
//					{
//						hideLoadingIndicator();
//						if (response.resultCode == BaseResponse.RESULT_OK)
//						{
//							mGDResponse = (GoodsDetailResponse)response;
//							if (mGDResponse.datas.param[2].value2 == null)
//							{
//								showLoadingIndicator();
//								RequestManager.loadGoodsDetail(getContext(), LoginHelper.getUserKey(getContext()),
//										goodsId, this, CacheMode.PERFER_NETWORK);
//								return;
//							}
//							if (mGDResponse != null && mGDResponse.datas != null)
//							{
//								AndroidUtils.MainHandler.post(new Runnable()
//								{
//									@Override
//									public void run()
//									{
//										productID = String.valueOf(mGDResponse.datas.id);
//										productName.setText(mGDResponse.datas.title);
//
//										if (mGDResponse.datas.param != null && mGDResponse.datas.param.length > 0)
//										{
//											product_number.setText(mGDResponse.datas.sku);
//
//											product_material_menubar.setOnMenuListener(new OnMenuListener()
//											{
//												@Override
//												public void onMenuUnSelected(MenuBar menuBar, MenuView menuView,
//														int menuIndex)
//												{
//												}
//
//												@Override
//												public void onMenuSelected(MenuBar menuBar, MenuView menuView,
//														int menuIndex)
//												{
//													float a = 0f, b, c = 0f, d;
//													switch (menuIndex)
//													{
//														case 0:
//														case 1:
//														case 2:
//															ringPriceText.setText("￥"
//																	+ mGDResponse.datas.param[2].value);
//															giaData.careprice = mGDResponse.datas.param[2].value;
//															giaData.carechangeprice = mGDResponse.datas.param[2].value2;
//															giaData.carematerial = Define.getMaterialList().get(
//																	menuIndex);
//															if (!TextUtils.isEmpty(mGDResponse.datas.param[2].value))
//															{
//																a = Float.parseFloat(mGDResponse.datas.param[2].value);
//																c = Float.parseFloat(mGDResponse.datas.param[2].value2);
//															}
//															break;
//														default:
//															ringPriceText.setText("￥"
//																	+ mGDResponse.datas.param[3].value);
//															giaData.careprice = mGDResponse.datas.param[3].value;
//															giaData.carechangeprice = mGDResponse.datas.param[3].value2;
//															giaData.carematerial = Define.getMaterialList().get(
//																	menuIndex);
//															if (!TextUtils.isEmpty(mGDResponse.datas.param[3].value))
//															{
//																a = Float.parseFloat(mGDResponse.datas.param[3].value);
//																c = Float.parseFloat(mGDResponse.datas.param[3].value2);
//															}
//															break;
//													}
//													b = Float.parseFloat(giapirce);
//													d = Float.parseFloat(giasrcprice);
//													productPirce.setText("￥" + (b + a));
//													allpirce = (a + b) + "";
//													srcprice = (c + d) + "";
//													LogUtil.i(TAG, "allpirce: " + allpirce + "; srcprice: " + srcprice);
//												}
//											});
//											product_material_menubar.setCurrentMenu(0);
//										}
//
//										if (mGDResponse.datas.tagname != null
//												&& mGDResponse.datas.tagname.contains(Define.TAGNAME_PENDANT))
//										{
//											product_type.setText("空托");
//											ringsize_select_layout.setVisibility(View.GONE);
//										}
//										else
//										{
//											product_type.setText("戒托");
//											ringsize_select_layout.setVisibility(View.VISIBLE);
//											FlowLayout menuGroup = (FlowLayout)ringsize_menubar.getMenuGroup();
//											float scaled = MainActivity.scaled;
//											float fontScaled = MainActivity.fontScaled;
//											menuGroup.setVerticalDividerWidth((int)(20 * scaled));
//											menuGroup.setHorizontalDividerHeight((int)(20 * scaled));
//											ringsize_menubar.removeAllMenus();
//											if ((mGDResponse.datas.tagname != null && mGDResponse.datas.tagname
//													.contains(Define.TAGNAME_MAN)))
//											{
//												ringsizeList = Define.getMaleRingsizeList();
//											}
//											else
//											{
//												ringsizeList = Define.getFemaleRingsizeList();
//											}
//											int layout = ResourceUtil.getLayoutId(getContext(), "ringsize_select_item");
//											int id = ResourceUtil.getId(getContext(), "textView");
//											for (String size : ringsizeList)
//											{
//												View view = View.inflate(getContext(), layout, null);
//												TextView textView = (TextView)view.findViewById(id);
//												textView.setText(size);
//												ViewTools.adapterAllTextViewTextSizeInChildren(view, fontScaled);
//												ringsize_menubar.addMenu((MenuView)view);
//											}
//											ringsize_menubar.setCurrentMenu(0);
//										}
//
//										productSelectedList(mGDResponse.datas.thumb_url);
//									}
//								});
//							}
//						}
//						else
//						{
//							showToast(response.error);
//						}
//					}
//
//					@Override
//					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
//					{
//						showToast("网络错误: " + error.errorCode);
//					}
//				});
//	}

	public void productSelectedList(ImageInfo[] mImageInfo)
	{
		for (ImageInfo image : mImageInfo)
		{
			mPagerAdapter.addItem(new ProductImageList(image));
		}
		mPopupWindow = new ProductImagePopupWindow(getActivity(), mImageInfo);
		ensureDots(mImageInfo.length);
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

	// 添加购物车
	String productID = null;
	// String gia = null;
	String giapirce = null;
	String giasrcprice = null;
	public int productSize = 0;
	String allpirce = null;
	String srcprice = null;

	public void PlaceOrder()
	{
		try
		{
			String productHText = ringsizeList.get(ringsize_menubar.getCurrentMenuIdx());
			productHText = productHText.substring(0, productHText.length() - 1);
			productSize = Integer.parseInt(productHText);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			productSize = 0;
		}
		giaData.caresize = "" + productSize;
		giaData.printData(TAG, 0);
		String letteringText = lettering.getText().toString();
		LogUtil.v(TAG, "allpirce: " + allpirce + "; srcprice: " + srcprice + "; letteringText:" + letteringText);
		showLoadingIndicator();
		RequestCallback callback = new RequestCallback()
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
		};
		if (customization==0)
			RequestManager.appendGIAShoppingcart(getActivity(), LoginHelper.getUserKey(getActivity()), productID,
				GIAData.toJson(giaData), allpirce, srcprice, productSize, letteringText, callback);
		else RequestManager.appendGIAShoppingcart(getActivity(), LoginHelper.getUserKey(getActivity()), productID,
				GIAData.toJson(giaData), allpirce, srcprice, productSize, letteringText, customization, callback);
	}
}
