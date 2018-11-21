package com.shiyou.tryapp2.app;

import android.app.Activity;
import android.content.Intent;
import android.extend.app.BaseFragment;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup.LayoutParams;

public class FragmentActivity extends BaseAppActivity
{
	private static Fragment launchFragment;

	public static void launchMe(Activity activity, BaseFragment launchFragment)
	{
		FragmentActivity.launchFragment = launchFragment;
		Intent intent = new Intent(activity, FragmentActivity.class);
		activity.startActivity(intent);
	}

	public static void launchMeForResult(Activity activity, BaseFragment launchFragment, int requestCode)
	{
		FragmentActivity.launchFragment = launchFragment;
		Intent intent = new Intent(activity, FragmentActivity.class);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void launchMeForResult(Fragment fragment, BaseFragment launchFragment, int requestCode)
	{
		FragmentActivity.launchFragment = launchFragment;
		Intent intent = new Intent(fragment.getActivity(), FragmentActivity.class);
		fragment.startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

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

		if (launchFragment != null)
			BaseFragment.add(this, launchFragment, false);
	}
}
