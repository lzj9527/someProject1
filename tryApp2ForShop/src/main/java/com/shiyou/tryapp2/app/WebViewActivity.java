package com.shiyou.tryapp2.app;

import java.util.List;

import org.apache.http.NameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.extend.app.BaseFragment;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup.LayoutParams;

public class WebViewActivity extends BaseAppActivity
{
	// public static WebViewActivity instance;

	private static String mFirstUrl;
	private static List<NameValuePair> mFirstRequestPairs;
	private static List<NameValuePair> mBaseRequestPairs;

	public static void launchMe(Activity activity, String firstUrl, List<NameValuePair> firstRequestPairs,
			List<NameValuePair> baseRequestPairs)
	{
		mFirstUrl = firstUrl;
		mFirstRequestPairs = firstRequestPairs;
		mBaseRequestPairs = baseRequestPairs;
		Intent intent = new Intent(activity, WebViewActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// instance = this;

		int layout = ResourceUtil.getLayoutId(getApplicationContext(), "default_fragment_layout");
		View view = View.inflate(getApplicationContext(), layout, null);
		setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		if (AndroidUtils.checkDeviceHasNavigationBar(this))
			view.setFitsSystemWindows(false);
		else
			view.setFitsSystemWindows(true);
		view.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener()
		{
			@Override
			public void onSystemUiVisibilityChange(int visibility)
			{
				LogUtil.v(TAG, "onSystemUiVisibilityChange: " + visibility);
				setSystemUiVisibility();
			}
		});

		BaseFragment.add(this, new WebViewFragment(mFirstUrl, mFirstRequestPairs, mBaseRequestPairs), false);
	}

	// @Override
	// protected void onDestroy()
	// {
	// super.onDestroy();
	// instance = null;
	// }
}
