package com.shiyou.tryapp2.app.product;

import java.util.List;

import org.apache.http.NameValuePair;

import android.extend.util.AndroidUtils;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendWebView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.WebViewFragment;

public class MainWebFragment extends WebViewFragment
{
	public static MainWebFragment instance = null;

	private int index;
	private boolean isFirst;
	private static long lastClickTime;

	public MainWebFragment(String firstUrl, List<NameValuePair> firstRequestPairs,
			List<NameValuePair> baseRequestPairs, int index)
	{
		super(firstUrl, firstRequestPairs, baseRequestPairs);
		instance = this;
		this.index = index;
	}


	public MainWebFragment(String firstUrl, int index)
	{
		super(firstUrl);
		instance = this;
		this.index = index;
	}

	public MainWebFragment(String firstUrl, int index,boolean isFirst)
	{
		super(firstUrl);
		instance = this;
		this.index = index;
		this.isFirst=isFirst;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{

		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "main_webview_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		((android.extend.widget.ExtendFrameLayout)view).setInterceptTouchEventToDownward(true);
		ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
		int id = ResourceUtil.getId(getActivity(), "middle_back");
		View middle_back = view.findViewById(id);
		switch (index)
		{
			case 0:
				middle_back.setVisibility(View.GONE);
				break;
			case 1:
			case 2:
				middle_back.setVisibility(View.VISIBLE);
				break;
		}

		middle_back.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;

				if (index == 1)
				{
					// MainFragment.instance.onBackPressed();
					// if (ProductDetailsFragment.instance != null) {
					// ProductDetailsFragment.instance.attachUnityPlayer(false);
					// }
					onBackPressed();
					MainFragment.instance.onBackPressed();
				}
				else if (index == 2)
				{
					getActivity().onBackPressed();
				}
			}
		});
//		id = ResourceUtil.getId(getActivity(), "test_token");
//		ExtendWebView webView=(ExtendWebView) view.findViewById(id);
//		id = ResourceUtil.getId(getActivity(), "webview");
//		ExtendWebView webView2=(ExtendWebView) view.findViewById(id);
//		long curClickTime = System.currentTimeMillis();
//		if ((curClickTime - lastClickTime)>=72000000) {
//			Log.d(TAG, "run: 执行 LastTime="+lastClickTime);
//			webView.loadUrl("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?token="+ Config.token);
//			lastClickTime = curClickTime;
//		}
		return view;
	}

	@Override
	public boolean onBackPressed()
	{
		if (index == 0 && mWebView.canGoBack())
		{
			mWebView.goBack();
			return true;
		}
		// if (ProductDetailsFragment.instance != null)
		// {
		// ProductDetailsFragment.instance.attachUnityPlayer(false);
		// }
		return false;
	}

}
