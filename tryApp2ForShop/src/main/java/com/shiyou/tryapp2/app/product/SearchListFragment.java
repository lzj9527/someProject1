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
import android.extend.widget.recycler.AbsAdapterItem;
import android.extend.widget.recycler.BaseRecyclerAdapter;
import android.extend.widget.recycler.BaseRecyclerAdapter.BaseViewHolder;
import android.extend.widget.recycler.GridRecyclerView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
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

public class SearchListFragment extends BaseFragment
{
	private String mKeyword;

	private View mGoodsListLayout;
	private GridRecyclerView mGoodsListView;
	private BaseRecyclerAdapter mGoodsListAdapter;

	public SearchListFragment(String keyword)
	{
		mKeyword = keyword;
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

		id = ResourceUtil.getId(getContext(), "condition_layout");
		View condition_layout = view.findViewById(id);
		condition_layout.setVisibility(View.GONE);

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

		loadGoodsList();

		return view;
	}

	private void loadGoodsList()
	{
		mGoodsListAdapter.clear();
		showLoadingIndicator();
		RequestManager.searchGoodsList(getContext(), LoginHelper.getUserKey(getContext()), mKeyword,
				new RequestCallback()
				{
					@Override
					public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
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
					}

					@Override
					public void onRequestError(int requestCode, long taskId, ErrorInfo error)
					{
						hideLoadingIndicator();
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
					boolean isShop = (mGoodsItem.isshop == 1);
					MainFragment.instance.addProductDetailFragmentToCurrent(mGoodsItem.id, mGoodsItem.tag, isShop,
							hasModelInfo, false);
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
