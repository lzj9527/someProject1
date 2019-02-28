package com.shiyou.tryapp2.app.product;

import android.extend.app.BaseFragment;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.util.AndroidUtils;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendImageView;
import android.extend.widget.FlowLayout;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseAdapter;
import android.extend.widget.adapter.ScrollListView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.db.BrowseHistoryDBHelper;
import com.shiyou.tryapp2.data.db.BrowseHistoryDBHelper.HistoryItem;
import com.shiyou.tryapp2.data.db.BrowseHistoryDBHelper.HistoryList;
import com.shiyou.tryapp2.data.response.CoupleRingDetailResponse;
import com.shiyou.tryapp2.data.response.GoodsDetailResponse;

public class BrowseHistoryFragment extends BaseFragment
{
	private ScrollListView mListView;
	private BaseAdapter<AbsAdapterItem> mListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "product_historylist_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);

		mListView = (ScrollListView)view;
		mListAdapter = new BaseAdapter<AbsAdapterItem>();
		mListView.setAdapter(mListAdapter);
		// int height = AndroidUtils.dp2px(getContext(), 10f);
		mListView.setHorizontalDividerHeight(20);

		ensureHistoryList();

		return view;
	}

	private void ensureHistoryList()
	{
		mListAdapter.clear();
		HistoryList list = BrowseHistoryDBHelper.getInstance().getHistoryList(getContext());
		for (HistoryItem item : list.list)
		{
			mListAdapter.addItem(new HistoryAdapterItem(item));
		}
	}

	private class HistoryAdapterItem extends AbsAdapterItem
	{
		private HistoryItem mItem;

		public HistoryAdapterItem(HistoryItem item)
		{
			mItem = item;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			int layout = ResourceUtil.getLayoutId(getContext(), "product_historylist_item");
			View view = View.inflate(getContext(), layout, null);

			int id = ResourceUtil.getId(getContext(), "product_list");
			FlowLayout product_list = (FlowLayout)view.findViewById(id);
			ensureProductList(product_list);

			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
			int id = ResourceUtil.getId(getContext(), "date");
			TextView date = (TextView)view.findViewById(id);
			date.setText(mItem.dateString);

			id = ResourceUtil.getId(getContext(), "num");
			TextView num = (TextView)view.findViewById(id);
			num.setText("共" + mItem.goodsList.size() + "件");
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{
			int id = ResourceUtil.getId(getContext(), "product_list");
			FlowLayout product_list = (FlowLayout)view.findViewById(id);
			loadProductList(product_list);
		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
			int id = ResourceUtil.getId(getContext(), "product_list");
			FlowLayout product_list = (FlowLayout)view.findViewById(id);
			recycleProductList(product_list);
		}

		private void ensureProductList(FlowLayout product_list)
		{
			int layout = ResourceUtil.getLayoutId(getContext(), "product_history_item");
			for (Object goods : mItem.goodsList)
			{
				View view = View.inflate(getContext(), layout, null);
				ImageInfo thumb = null;
				String title = "";
				String goodsId = null;
				String tag = "";
				boolean isShop = false;
				boolean hasModleInfo = false;
				if (goods instanceof GoodsDetailResponse)
				{
					GoodsDetailResponse detail = (GoodsDetailResponse)goods;
//					thumb = detail.thumb;
					title = detail.title;
					goodsId = String.valueOf(detail.id);
					tag = Define.TAG_RING;
//					isShop = detail.isShop;
					hasModleInfo = (detail.model_info != null);
				}
				else if (goods instanceof CoupleRingDetailResponse)
				{
					CoupleRingDetailResponse detail = (CoupleRingDetailResponse)goods;
//					thumb = detail.thumb;
					title = detail.title;
					goodsId = String.valueOf(detail.id);
					tag = Define.TAG_COUPLE;
//					isShop = detail.isShop;
					if (detail.model_infos != null)
					{
						if (detail.model_infos.men != null || detail.model_infos.wmen != null)
							hasModleInfo = true;
					}
				}
				int id = ResourceUtil.getId(getContext(), "image");
				ExtendImageView image = (ExtendImageView)view.findViewById(id);
				if (thumb != null)
					image.setImageDataSource(thumb.url, thumb.filemtime, DecodeMode.FIT_WIDTH);
				final String _goodsId = goodsId;
				final String _tag = tag;
				final boolean _isShop = isShop;
				final boolean _hasModelInfo = hasModleInfo;
				image.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if (AndroidUtils.isFastClick())
							return;
						// MainFragment.instance.setCurrentFragmentToProductDetail(_goodsId, _tag, false);
						// if (_isShop)
						// {
						MainFragment.instance.addProductDetailFragmentToCurrent(_goodsId, _tag, _isShop, _hasModelInfo,
								true);
						// }
						// else
						// {
						// MainFragment.instance.addProductDetailFragmentToCurrent(_goodsId, _tag, _isShop, true);
						// }
					}
				});

				id = ResourceUtil.getId(getContext(), "name");
				TextView name = (TextView)view.findViewById(id);
				name.setText(title);

				MarginLayoutParams params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.topMargin = 60;
				params.rightMargin = 60;

				product_list.addView(view, params);
			}
		}

		private void loadProductList(FlowLayout product_list)
		{
			int count = product_list.getChildCount();
			for (int i = 0; i < count; i++)
			{
				View view = product_list.getChildAt(i);
				int id = ResourceUtil.getId(getContext(), "image");
				ExtendImageView image = (ExtendImageView)view.findViewById(id);
				image.startImageLoad();
			}
		}

		private void recycleProductList(FlowLayout product_list)
		{
			int count = product_list.getChildCount();
			for (int i = 0; i < count; i++)
			{
				View view = product_list.getChildAt(i);
				int id = ResourceUtil.getId(getContext(), "image");
				ExtendImageView image = (ExtendImageView)view.findViewById(id);
				image.recyleBitmapImage();
			}
		}
	}
}
