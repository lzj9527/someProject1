package com.shiyou.tryapp2.app.product;

import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.util.AndroidUtils;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.util.ViewTools.FitMode;
import android.extend.widget.ExtendImageView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseGridAdapter;
import android.extend.widget.adapter.ScrollGridView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse;
import com.shiyou.tryapp2.data.response.GoodsCategorysResponse.CategoryItem;
import com.shiyou.tryapp2.shop.zsa.R;

public class OtherIndexFragment extends BaseFragment
{
	private BaseGridAdapter<AbsAdapterItem> mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getActivity(), "other_index_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendLinearLayout)view).setInterceptTouchEventToDownward(true);

		int id = ResourceUtil.getId(getContext(), "index_container");
		ScrollGridView index_container = (ScrollGridView)view.findViewById(id);
		index_container.setNumColumns(3);
		int space = AndroidUtils.dp2px(getContext(), 40);
		index_container.setVerticalDividerWidth(space);
		index_container.setHorizontalDividerHeight(space);
		mAdapter = new BaseGridAdapter<AbsAdapterItem>();
		index_container.setAdapter(mAdapter);

		loadGoodsCategory();

		return view;
	}

	private void loadGoodsCategory()
	{
		RequestManager.loadGoodsCategorys(getActivity(), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					GoodsCategorysResponse mGoodsCategoryResponse = (GoodsCategorysResponse)response;
					mAdapter.clear();
					for (CategoryItem mCategoryItem : mGoodsCategoryResponse.findZuanShiCategoryList())
					{
						mAdapter.addItem(new CategoryAdapterItem(mCategoryItem));
					}
					// mAdapter.addItem(new CategoryAdapterItem(null));
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

	private class CategoryAdapterItem extends AbsAdapterItem
	{
		CategoryItem mCategoryItem;

		// GoodsCategoryResponse mGoodsCategoryResponse;

		public CategoryAdapterItem(CategoryItem categoryItem)
		{
			// this.mGoodsCategoryResponse = mGoodsCategoryResponse;
			this.mCategoryItem = categoryItem;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			 int layout = ResourceUtil.getLayoutId(getActivity(), "product_new_item");
			 View view = View.inflate(getActivity(), layout, null);
			 int id = ResourceUtil.getId(getActivity(), "image");
			 ExtendImageView image = (ExtendImageView) view.findViewById(id);
			 image.setAutoRecyleBitmap(true);
			 
			 view.setLayoutParams(new LayoutParams(450, 450));
			 
//			
//			int layout = ResourceUtil.getLayoutId(getActivity(), "product_new_item");
//			View view = View.inflate(getActivity(), layout, null);
//			 
//			int id = ResourceUtil.getId(getActivity(), "image");
//			ExtendImageView image = (ExtendImageView) view.findViewById(id);
//			image.setScaleType(ScaleType.FIT_CENTER);
//			image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//			int padding = 40;
//			image.setPadding(padding, padding, padding, padding);
//			ViewTools.autoFitViewDimension(image, parent, FitMode.FIT_IN_WIDTH, 1);
//			ViewTools.adapterViewPadding(image, MainActivity.scaled);

			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
			// int id = ResourceUtil.getId(getActivity(), "image");
			// ExtendImageView image = (ExtendImageView)view.findViewById(id);
//			ExtendImageView image = (ExtendImageView)view;
			
			int id = ResourceUtil.getId(getActivity(), "image");
			ExtendImageView image = (ExtendImageView) view.findViewById(id);
			
			id = ResourceUtil.getId(getActivity(), "new_icon");
			ExtendImageView new_icon = (ExtendImageView) view.findViewById(id);
			
			if (mCategoryItem != null && mCategoryItem.thumb != null)
			{
				image.setImageDataSource(mCategoryItem.thumb.url, mCategoryItem.thumb.filemtime, DecodeMode.FIT_WIDTH);
				if (Math.abs(mCategoryItem.getTheNewTime() - mCategoryItem.thumb.filemtime)< 60 * 60 * 24 * 3){
					new_icon.setVisibility(View.VISIBLE);
				}else {
					new_icon.setVisibility(View.GONE);
				}
			}
			else
			{
				image.setImageResource(R.drawable.gia);
				new_icon.setVisibility(View.GONE);
			}
			image.startImageLoad(false);
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{

		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
			// int id = ResourceUtil.getId(getActivity(), "image");
			// ExtendImageView image = (ExtendImageView) view.findViewById(id);
			// image.recyleBitmapImage();
		}

		public void onItemClick(View adapterView, ViewGroup parent, View view, int position, long id)
		{
			if (mCategoryItem != null)
			{
				// MainFragment.instance.setCurrentFragmentToProductList(mCategoryItem.id, false);
				MainFragment.instance.addFragmentToCurrent(new ProductListFragment(mCategoryItem.id, false), false);
			}
			else
			{
				String url = Config.WebGIADiamonds + "?key=" + LoginHelper.getUserKey(getContext());
				MainFragment.instance.addWebFragmentToCurrent(url, false);
			}
		}
	}
}
