package com.shiyou.tryapp2.app.login;

import android.app.Activity;
import android.content.Context;
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

import com.shiyou.tryapp2.app.BaseAppActivity;

// import com.shiyou.fitsapp.LoginHelper;

public class LoginActivity extends BaseAppActivity
{
	public static void launchMeForResult(Activity activity, int requestCode)
	{
		activity.startActivityForResult(new Intent(activity, LoginActivity.class), requestCode);
	}

	public static void launchMeForResult(Fragment fragment, int requestCode)
	{
		fragment.startActivityForResult(new Intent(fragment.getActivity(), LoginActivity.class), requestCode);
	}

	public static void launchMeForResult(Context context, int reqLogin)
	{
		if (context instanceof Activity)
			((Activity)context).startActivityForResult(new Intent(context, LoginActivity.class), reqLogin);
		else
			throw new ClassCastException("the context is not Activity");
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

		BaseFragment.add(this, new LoginFragment(), false);
	}

	// @Override
	// public void onBackPressed()
	// {
	// if (popBackStackImmediate())
	// return;
	// MainActivity.backToHomepage(LoginActivity.this);
	// }
}