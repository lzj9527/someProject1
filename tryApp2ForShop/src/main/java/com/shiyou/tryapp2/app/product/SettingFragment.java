package com.shiyou.tryapp2.app.product;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.cache.FileCacheManager;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.loader.HttpLoader;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendDialog;
import android.extend.widget.ExtendImageView;
import android.extend.widget.ExtendLinearLayout;
import android.extend.widget.MenuBar;
import android.extend.widget.MenuBar.OnMenuListener;
import android.extend.widget.MenuView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shiyou.tryapp2.FileDownloadHelper;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.ResourceHelper;
import com.shiyou.tryapp2.ResourceHelper2;
import com.shiyou.tryapp2.ResourceHelper2.OnResourceDownloadCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.WebViewFragment;
import com.shiyou.tryapp2.app.login.LoginFragment;
import com.shiyou.tryapp2.app.login.LoginHelper;
import com.shiyou.tryapp2.data.db.BrowseHistoryDBHelper;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GetTrainLinksResponse;
import com.shiyou.tryapp2.data.response.GetTrainLinksResponse.TrainItem;
import com.shiyou.tryapp2.data.response.ShopLogoAndADResponse;
import com.unity3d.player.UnityPlayer;

public class SettingFragment extends BaseFragment
{
	public SettingFragment instance = null;

	private MenuBar mMenuBar;
	private View mOthersLayout;
	private ExtendImageView mLogoImageView;
	private TextView mVersionView;

	public SettingFragment()
	{
		instance = this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "setting_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		((ExtendLinearLayout)view).setInterceptTouchEventToDownward(true);
		ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);

		int id = ResourceUtil.getId(getActivity(), "middle_back");
		View middle_back = view.findViewById(id);
		middle_back.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				getActivity().onBackPressed();
			}
		});

		id = ResourceUtil.getId(getContext(), "menubar");
		mMenuBar = (MenuBar)view.findViewById(id);

		id = ResourceUtil.getId(getContext(), "others_layout");
		mOthersLayout = view.findViewById(id);
		mOthersLayout.setVisibility(View.GONE);

		id = ResourceUtil.getId(getContext(), "clear_cache");
		View clear_cache = view.findViewById(id);
		clear_cache.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				showConfirmDialog("确认清空所有数据缓存?", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						clearCache();
					}
				}, null);
			}
		});

		id = ResourceUtil.getId(getContext(), "update_response");
		View update_response = view.findViewById(id);
		update_response.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				showConfirmDialog("确认更新资源?", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						LogUtil.v(TAG, "updateResource...");
						new ResourceHelper2(getActivity(), LoginHelper.getUserKey(getContext()), false, true)
								.downloadResource(new OnResourceDownloadCallback()
								{
									@Override
									public void onDownloadFinished(Object data, boolean canceled)
									{
										if (!canceled)
											showToast("资源更新已完成");
									}
								});
					}
				}, null);
			}
		});

		id = ResourceUtil.getId(getContext(), "update_version");
		View update_version = view.findViewById(id);
		update_version.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				LogUtil.v(TAG, "checkVersion...");
				LoginHelper.checkVersion(getActivity(), false);
			}
		});

		id = ResourceUtil.getId(getContext(), "logout");
		View logout = view.findViewById(id);
		logout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (AndroidUtils.isFastClick())
					return;
				showConfirmDialog("确认更换账号?", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						replace(getActivity(), new LoginFragment(), false);
					}
				}, null);
			}
		});

		id = ResourceUtil.getId(getContext(), "shop_logo");
		mLogoImageView = (ExtendImageView)view.findViewById(id);

		id = ResourceUtil.getId(getContext(), "version");
		mVersionView = (TextView)view.findViewById(id);

		doRefresh();

		mMenuBar.addOnLayoutChangeListener(new OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
					int oldRight, int oldBottom)
			{
				LogUtil.v(TAG, "onLayoutChange: " + v);
				if (mMenuBar.getWidth() == 0 || mMenuBar.getHeight() == 0)
					return;
				mMenuBar.removeOnLayoutChangeListener(this);
				float scaled = MainActivity.scaled;

				ViewTools.adapterViewSize(mLogoImageView, scaled);
			}
		});

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		instance = null;
	}

	private void ensureTrainLinks()
	{
		RequestManager.getTrainLinks(getContext(), LoginHelper.getUserKey(getContext()), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					updateMenubarItems((GetTrainLinksResponse)response);
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

	private void updateMenubarItems(final GetTrainLinksResponse response)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (response.datas != null && response.datas.list != null && response.datas.list.length > 0)
				{
					mMenuBar.removeAllMenus();
					int layout = ResourceUtil.getLayoutId(getContext(), "setting_menuitem");
					for (TrainItem item : response.datas.list)
					{
						View view = View.inflate(getContext(), layout, null);
						int id = ResourceUtil.getId(getContext(), "title");
						TextView title = (TextView)view.findViewById(id);
						title.setText(item.title);
						view.setTag(item.link);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						params.topMargin = 10;
						view.setLayoutParams(params);
						mMenuBar.addMenu((MenuView)view, params);

						ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);
						ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
						ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
					}
					mMenuBar.setOnMenuListener(new OnMenuListener()
					{
						@Override
						public void onMenuUnSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
						{
						}

						@Override
						public void onMenuSelected(MenuBar menuBar, MenuView menuView, int menuIndex)
						{
							String link = (String)menuView.getTag();
							replace(SettingFragment.this, new MainWebFragment(link, 0), false);
						}
					});
					mMenuBar.setCurrentMenu(0);
				}
				mOthersLayout.setVisibility(View.VISIBLE);
				invalidateMenuBar();
			}
		});
	}

	private void ensureShopLogo()
	{
		RequestManager.loadShopLogoAndAD(getContext(), LoginHelper.getUserKey(getContext()), new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					ShopLogoAndADResponse logoResponse = (ShopLogoAndADResponse)response;
					if (logoResponse != null && logoResponse.datas != null && logoResponse.datas.list != null
							&& logoResponse.datas.list.logo != null)
					{
						mLogoImageView.setImageDataSource(logoResponse.datas.list.logo.url,
								logoResponse.datas.list.logo.filemtime, DecodeMode.FIT_WIDTH);
						mLogoImageView.startImageLoad(false);
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
	}

	private void ensureVersion()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					PackageInfo pi = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
					mVersionView.setText("当前版本：" + pi.versionName);
				}
				catch (NameNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public void doRefresh()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (isResumed())
				{
					LogUtil.d(TAG, "doRefresh...");
					ensureTrainLinks();
					ensureShopLogo();
					ensureVersion();
				}
				else
					AndroidUtils.MainHandler.postDelayed(this, 50L);
			}
		});
	}

	private void showConfirmDialog(final String text, final DialogInterface.OnClickListener confirmListener,
			final DialogInterface.OnCancelListener cancelListener)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				int layout = ResourceUtil.getLayoutId(getContext(), "confirm_dialog");
				View view = View.inflate(getContext(), layout, null);
				ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);
				ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
				ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
				final ExtendDialog dialog = AndroidUtils.createDialog(getActivity(), view, true, true);
				int id = ResourceUtil.getId(getContext(), "message");
				TextView message = (TextView)view.findViewById(id);
				message.setText(text);
				id = ResourceUtil.getId(getContext(), "confirm");
				View confirm = view.findViewById(id);
				confirm.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
						if (confirmListener != null)
							confirmListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
					}
				});
				id = ResourceUtil.getId(getContext(), "cancel");
				View cancel = view.findViewById(id);
				cancel.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
						if (cancelListener != null)
							cancelListener.onCancel(dialog);
					}
				});
				dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						dialog.dismiss();
						if (cancelListener != null)
							cancelListener.onCancel(dialog);
					}
				});
				dialog.show();
			}
		});
	}

	public void clearCache()
	{
		LogUtil.v(TAG, "clearCache...");
		showLoadingIndicator();
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.d(TAG, "start clear cache...");
				final long startTime = System.currentTimeMillis();
				if (WebViewFragment.instance != null)
					WebViewFragment.instance.clearCache();
				try
				{
					UnityPlayer.UnitySendMessage("PlatformMessageHandler", "NotifyCleanCache", "");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						HttpLoader.clearMemoryCache();
						FileCacheManager.clearAllCaches(getContext());
						ResourceHelper.cleanResource(getContext());
						FileDownloadHelper.clearAllDownloadedFile(getContext());
						BrowseHistoryDBHelper.getInstance().deleteAll(getContext());
						MainActivity.instance.clearCache();
						long time = System.currentTimeMillis() - startTime;
						LogUtil.v(TAG, "clear cache finished, time = " + time);
						hideLoadingIndicator();
						showToast("缓存清理已完成.");
					}
				}).start();
			}
		});
	}

	public void invalidateMenuBarOnce()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mMenuBar.invalidate();
			}
		});
	}

	public void invalidateMenuBar()
	{
		invalidateMenuBar(20L, 20);
	}

	int invalidateCount;

	private void invalidateMenuBar(final long delayMillis, final int numOfInvalidate)
	{
		invalidateCount = 0;
		AndroidUtils.MainHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				mMenuBar.invalidate();
				invalidateCount++;
				LogUtil.i(TAG, "invalidateMenuBar: " + invalidateCount);
				if (invalidateCount < numOfInvalidate)
				{
					AndroidUtils.MainHandler.postDelayed(this, delayMillis);
				}
			}
		}, delayMillis);
	}
}
