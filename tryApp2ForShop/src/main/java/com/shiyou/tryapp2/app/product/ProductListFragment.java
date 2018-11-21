package com.shiyou.tryapp2.app.product;

import java.util.ArrayList;
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
import android.extend.util.ViewTools.FitMode;
import android.extend.widget.ExtendImageView;
import android.extend.widget.SpinnerPopupWindow;
import android.extend.widget.SpinnerPopupWindow.OnItemClickListener;
import android.extend.widget.recycler.AbsAdapterItem;
import android.extend.widget.recycler.BaseRecyclerAdapter;
import android.extend.widget.recycler.BaseRecyclerAdapter.BaseViewHolder;
import android.extend.widget.recycler.GridRecyclerView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse.GoodsItem;
import com.shiyou.tryapp2.data.response.GoodsTagsResponse;
import com.shiyou.tryapp2.data.response.GoodsTagsResponse.TagItem;
import com.shiyou.tryapp2.shop.zsa.R;

public class ProductListFragment extends BaseFragment
{
	private View mGoodsListLayout;
	private GridRecyclerView mGoodsListView;
	private BaseRecyclerAdapter mGoodsListAdapter;

	private String ccate;
	private boolean isShop;
	private GoodsTagsResponse mGoodsTagsResponse;

	// 风格
	private TextView condition_style;
	private List<String> styleList = new ArrayList<String>();
	private SpinnerPopupWindow mSpinerPopWindowForStyle;
	private String mSelectStyleId;
	private List<String> styleIdList = new ArrayList<String>();
	// 重量
	private TextView condition_weight;
	private List<String> weightList = new ArrayList<String>();
	private SpinnerPopupWindow mSpinerPopWindowForWeight;
	private float[] mSelectWeightRange;
	private List<float[]> weightRangeList = new ArrayList<float[]>();
	// 价格
	private TextView condition_price;
	private List<String> priceList = new ArrayList<String>();
	private SpinnerPopupWindow mSpinerPopWindowForPrice;
	private int[] mSelectPriceRange;
	private List<int[]> priceRangeList = new ArrayList<int[]>();

	public ProductListFragment(String ccate, boolean isShop)
	{
		this.ccate = ccate;
		this.isShop = isShop;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "product_list_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendLinearLayout)view).setInterceptTouchEventToDownward(true);
		ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);

		int id = ResourceUtil.getId(getActivity(), "middle_back");
		View middle_back = view.findViewById(id);
		middle_back.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				MainFragment.instance.onBackPressed();
			}
		});

		id = ResourceUtil.getId(getContext(), "condition_style");
		condition_style = (TextView)view.findViewById(id);
		condition_style.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick() || mSpinerPopWindowForStyle == null)
					return;
				showSpinnerPopWindow(mSpinerPopWindowForStyle, condition_style);
			}
		});
		ViewTools.adapterViewWidth(condition_style, MainActivity.scaled);

		id = ResourceUtil.getId(getContext(), "condition_weight");
		condition_weight = (TextView)view.findViewById(id);
		weightList.add("不限");
		weightRangeList.add(null);
		weightList.add("0.3ct以下");
		weightRangeList.add(new float[] { 0f, 0.3f });
		weightList.add("0.3-0.5ct");
		weightRangeList.add(new float[] { 0.3f, 0.5f });
		weightList.add("0.5-0.7ct");
		weightRangeList.add(new float[] { 0.5f, 0.7f });
		weightList.add("0.7-1ct");
		weightRangeList.add(new float[] { 0.7f, 1f });
		weightList.add("1-2ct");
		weightRangeList.add(new float[] { 1f, 2f });
		weightList.add("2ct以上");
		weightRangeList.add(new float[] { 2f, 9999f });
		mSpinerPopWindowForWeight = new SpinnerPopupWindow(getActivity(), R.layout.spinner_window_layout,
				R.layout.spinner_item_layout, weightList);
		mSpinerPopWindowForWeight.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(int pos)
			{
				LogUtil.v(TAG, "onItemClick: " + pos + "; " + weightList.get(pos) + "; " + weightRangeList.get(pos));
				mSelectWeightRange = weightRangeList.get(pos);
				switch (pos)
				{
					case 0:
						condition_weight.setText("钻石分数");
						break;
					default:
						condition_weight.setText(weightList.get(pos));
						break;
				}
				reloadGoodsList();
			};
		});
		condition_weight.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				showSpinnerPopWindow(mSpinerPopWindowForWeight, condition_weight);
			}
		});
		ViewTools.adapterViewWidth(condition_weight, MainActivity.scaled);

		id = ResourceUtil.getId(getContext(), "condition_price");
		condition_price = (TextView)view.findViewById(id);
		priceList.add("不限");
		priceRangeList.add(null);
		priceList.add("2000元以下");
		priceRangeList.add(new int[] { 0, 2000 });
		priceList.add("2000-4000元");
		priceRangeList.add(new int[] { 2000, 4000 });
		priceList.add("4000-6000元");
		priceRangeList.add(new int[] { 4000, 6000 });
		priceList.add("6000-8000元");
		priceRangeList.add(new int[] { 6000, 8000 });
		priceList.add("8000-1W元");
		priceRangeList.add(new int[] { 8000, 10000 });
		priceList.add("1W-1.5W元");
		priceRangeList.add(new int[] { 10000, 15000 });
		priceList.add("1.5W-3W元");
		priceRangeList.add(new int[] { 15000, 30000 });
		priceList.add("3W元以上");
		priceRangeList.add(new int[] { 30000, Integer.MAX_VALUE });
		mSpinerPopWindowForPrice = new SpinnerPopupWindow(getActivity(), R.layout.spinner_window_layout,
				R.layout.spinner_item_layout, priceList);
		mSpinerPopWindowForPrice.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(int pos)
			{
				LogUtil.v(TAG, "onItemClick: " + pos + "; " + priceList.get(pos) + "; " + priceRangeList.get(pos));
				mSelectPriceRange = priceRangeList.get(pos);
				switch (pos)
				{
					case 0:
						condition_price.setText("价格");
						break;
					default:
						condition_price.setText(priceList.get(pos));
						break;
				}
				reloadGoodsList();
			}
		});
		condition_price.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				showSpinnerPopWindow(mSpinerPopWindowForPrice, condition_price);
			}
		});
		ViewTools.adapterViewWidth(condition_price, MainActivity.scaled);

		id = ResourceUtil.getId(getActivity(), "goodslist_layout");
		mGoodsListLayout = view.findViewById(id);
		ViewTools.adapterViewMargin(mGoodsListLayout, MainActivity.scaled);
		ViewTools.adapterViewPadding(mGoodsListLayout, MainActivity.scaled);

		mGoodsListView = new GridRecyclerView(getActivity());
		((ViewGroup)mGoodsListLayout).addView(mGoodsListView, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mGoodsListView.setSpanCount(4);
		mGoodsListView.setItemMargin(0);
		mGoodsListAdapter = new BaseRecyclerAdapter();
		mGoodsListView.setAdapter(mGoodsListAdapter);
		mGoodsListView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return false;
			}
		});
		// mGoodsListView.addOnLayoutChangeListener(new OnLayoutChangeListener()
		// {
		// @Override
		// public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
		// int oldRight, int oldBottom)
		// {
		// LogUtil.v(TAG, "onLayoutChange: " + v + "; " + left + "; " + top + "; " + right + "; " + bottom);
		// mGoodsListView.removeOnLayoutChangeListener(this);
		// }
		// });

		reloadGoodsList();
		loadGoodsTags();

		return view;
	}

	private void showSpinnerPopWindow(SpinnerPopupWindow spinnerPopWindow, TextView textView)
	{
		LogUtil.v(TAG, "showSpinWindow: " + textView.getText());
		// spinnerPopWindow.setWidth(textView.getWidth());
		spinnerPopWindow.showAsDropDown(textView);
	}

	private void reloadGoodsList()
	{
		mGoodsListAdapter.clear();
		showLoadingIndicator();
		final String[] tags;
		if (!TextUtils.isEmpty(mSelectStyleId))
			tags = new String[] { mSelectStyleId };
		else
			tags = null;
		RequestManager.loadGoodsList(getActivity(), LoginHelper.getUserKey(getContext()), isShop, ccate, tags,
				mSelectWeightRange, mSelectPriceRange, new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, final BaseResponse response, DataFrom from)
					{
						hideLoadingIndicator();
						if (response.resultCode == BaseResponse.RESULT_OK)
						{
							final GoodsListResponse mGoodsListResponse = (GoodsListResponse)response;
							if (mGoodsListResponse.datas != null && mGoodsListResponse.datas.list != null
									&& mGoodsListResponse.datas.list.length > 0)
							{
								AndroidUtils.MainHandler.post(new Runnable()
								{
									@Override
									public void run()
									{
										int count = 0;
										for (final GoodsItem mGoodsItem : mGoodsListResponse.datas.list)
										{
											AndroidUtils.MainHandler.postDelayed(new Runnable()
											{
												@Override
												public void run()
												{
													if (isDetached())
														return;
													mGoodsListAdapter.addItem(new ProductListItem(mGoodsItem));
												}
											}, count * 20L);
											count++;
										}
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
							RequestManager.loadGoodsList(getActivity(), LoginHelper.getUserKey(getContext()), isShop,
									ccate, tags, mSelectWeightRange, mSelectPriceRange, null, CacheMode.PERFER_NETWORK);
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						hideLoadingIndicator();
						showToast("网络错误: " + error.errorCode);
						RequestManager.loadGoodsList(getActivity(), LoginHelper.getUserKey(getContext()), isShop,
								ccate, tags, mSelectWeightRange, mSelectPriceRange, null, CacheMode.PERFER_NETWORK);
					}
				});
	}

	private void loadGoodsTags()
	{
		RequestManager.loadGoodsTags(getContext(), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					mGoodsTagsResponse = (GoodsTagsResponse)response;
					if (isDetached())
						return;
					if (mGoodsTagsResponse.datas != null && mGoodsTagsResponse.datas.list != null)
					{
						styleList.clear();
						styleIdList.clear();
						styleList.add("不限");
						styleIdList.add(null);
						for (TagItem item : mGoodsTagsResponse.datas.list)
						{
							styleList.add(item.tagname);
							styleIdList.add(item.id);
						}
						mSpinerPopWindowForStyle = new SpinnerPopupWindow(getActivity(),
								R.layout.spinner_window_layout, R.layout.spinner_item_layout, styleList);
						mSpinerPopWindowForStyle.setOnItemClickListener(new OnItemClickListener()
						{
							@Override
							public void onItemClick(int pos)
							{
								LogUtil.v(TAG,
										"onItemClick: " + pos + "; " + styleList.get(pos) + "; " + styleIdList.get(pos));
								mSelectStyleId = styleIdList.get(pos);
								switch (pos)
								{
									case 0:
										condition_style.setText("款式风格");
										break;
									default:
										condition_style.setText(styleList.get(pos));
										break;
								}
								reloadGoodsList();
							}
						});
					}
				}
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
				showToast("网络错误: " + error.errorCode);
			}
		});
	}

	private class ProductListItem extends AbsAdapterItem
	{
		GoodsItem mGoodsItem;

		public ProductListItem(GoodsItem mGoodsItem)
		{
			this.mGoodsItem = mGoodsItem;
		}

		@Override
		public View onCreateView(ViewGroup parent, int position)
		{
			int layout = ResourceUtil.getLayoutId(getActivity(), "product_list_item");
			final View view = View.inflate(getActivity(), layout, null);
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 400));
			int id = ResourceUtil.getId(getActivity(), "image");
			final ExtendImageView image = (ExtendImageView)view.findViewById(id);
			image.setAutoRecyleBitmap(true);
			ViewTools.autoFitViewDimension(view, parent, FitMode.FIT_IN_WIDTH, 1f);
			view.addOnLayoutChangeListener(new OnLayoutChangeListener()
			{
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom)
				{
					LogUtil.v(TAG, "onLayoutChange: " + view.getWidth() + "x" + view.getHeight() + "; " + view);
					if (view.getWidth() == 0 || view.getHeight() == 0
							|| Math.abs(view.getWidth() - view.getHeight()) > 10)
						return;
					view.removeOnLayoutChangeListener(this);
					ViewTools.autoFitViewDimension(image, view, FitMode.FIT_IN_HEIGHT, 1f);
				}
			});
			ViewTools.adapterViewPadding(view, MainActivity.scaled);
			ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);
			return view;
		}

		@Override
		public void onBindView(BaseViewHolder holder, View view, int position)
		{
			int id = ResourceUtil.getId(getActivity(), "image");
			ExtendImageView image = (ExtendImageView)view.findViewById(id);
			image.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (AndroidUtils.isFastClick())
						return;
					// if (mGoodsItem.tag.equals(GoodsItem.TAG_RING)) {
					// add(MainFragment.instance, MainFragment.instance.fragmentC1ID, new ProductDetailsFragment(
					// mGoodsItem.tag, mGoodsItem.id, isShop), true);
					// if (isShop)
					boolean hasModelInfo = false;
					if (mGoodsItem.model_info != null)
						hasModelInfo = true;
					else if (mGoodsItem.model_infos != null)
						if (mGoodsItem.model_infos.men != null || mGoodsItem.model_infos.wmen != null)
							hasModelInfo = true;
					MainFragment.instance.addProductDetailFragmentToCurrent(mGoodsItem.id, mGoodsItem.tag, isShop,
							hasModelInfo, mSelectWeightRange, mSelectPriceRange, false);
					// else
					// MainFragment.instance.addProductDetailFragmentToCurrent(mGoodsItem.id, mGoodsItem.tag, isShop,
					// false);
					// } else {
					// add(MainFragment.instance,
					// MainFragment.instance.fragmentC1ID,
					// new ProductDetailsFragment(1, mGoodsItem.id),
					// true);
					// }
				}
			});

			id = ResourceUtil.getId(getActivity(), "name");
			TextView name = (TextView)view.findViewById(id);
			if (mGoodsItem.title != null)
			{
				name.setText(mGoodsItem.title);
			}

			id = ResourceUtil.getId(getActivity(), "num");
			TextView num = (TextView)view.findViewById(id);
			if (mGoodsItem.count > 0)
			{
				num.setText("共" + mGoodsItem.count + "件");
			}
			// else
			// {
			num.setVisibility(View.INVISIBLE);
			// }
			long currentTime = System.currentTimeMillis();
			// LogUtil.v(TAG, "currentTime=" + currentTime + "; createtime=" + mGoodsItem.createtime);
			if (mGoodsItem.createtime > 0 && (currentTime - mGoodsItem.createtime * 1000 < Config.newGoodsInterval))
			{
				id = ResourceUtil.getId(getContext(), "tag_new");
				View tag_new = view.findViewById(id);
				tag_new.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onViewAttachedToWindow(BaseViewHolder holder, View view)
		{
			int id = ResourceUtil.getId(getActivity(), "image");
			ExtendImageView image = (ExtendImageView)view.findViewById(id);
			if (mGoodsItem != null && mGoodsItem.thumb != null)
				image.setImageDataSource(mGoodsItem.thumb.url, mGoodsItem.thumb.filemtime, DecodeMode.FIT_WIDTH);
			image.startImageLoad(false);
		}
	}
}
