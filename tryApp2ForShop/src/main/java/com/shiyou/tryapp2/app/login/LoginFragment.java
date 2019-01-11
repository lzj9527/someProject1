package com.shiyou.tryapp2.app.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.Loader.CacheMode;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.TextView;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.RequestManager;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.ResourceHelper2;
import com.shiyou.tryapp2.ResourceHelper2.OnResourceDownloadCallback;
import com.shiyou.tryapp2.app.MainActivity;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.app.WebViewFragment;
import com.shiyou.tryapp2.app.product.MainIndexFragment;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse.GoodsItem;
import com.shiyou.tryapp2.data.response.LoginResponse;

import java.io.FileOutputStream;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class LoginFragment extends BaseFragment
{
	EditText edit_user;
	EditText edit_password;
	TextView bindHint;

	boolean mLoginFinished = false;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "login_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);
		ViewTools.adapterAllViewMarginInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllViewPaddingInChildren(view, MainActivity.scaled);
		ViewTools.adapterAllTextViewTextSizeInChildren(view, MainActivity.fontScaled);

		// int id = ResourceUtil.getId(getContext(), "login_box");
		// View login_box = view.findViewById(id);
		// ViewTools.adapterViewSize(login_box, MainActivity.scaled);

		int id = ResourceUtil.getId(getContext(), "edit_user");
		edit_user = (EditText)view.findViewById(id);
		edit_user.setText(LoginHelper.getUserName(getContext()));

		id = ResourceUtil.getId(getContext(), "edit_password");
		edit_password = (EditText)view.findViewById(id);
		edit_password.setText(LoginHelper.getUserPassword(getContext()));

		id = ResourceUtil.getId(getContext(), "login");
		View login = view.findViewById(id);
		login.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				doLogin();
			}
		});

		id = ResourceUtil.getId(getContext(), "bindHint");
		bindHint = (TextView)view.findViewById(id);
		bindHint.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				if (bindHint.getWidth() == 0)
					return;
				bindHint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				int width = bindHint.getWidth();
				LayoutParams params = bindHint.getLayoutParams();
				params.width = width;
				bindHint.setLayoutParams(params);
			}
		});

		return view;
	}

	private void doLogin()
	{
		if (mLoginFinished)
			return;
		final String user = edit_user.getText().toString();
		if (TextUtils.isEmpty(user))
		{
			showToast("请输入用户名");
			return;
		}
		String password = edit_password.getText().toString();
		if (TextUtils.isEmpty(password))
		{
			showToast("请输入密码");
			return;
		}
		showLoadingIndicator();
		LoginHelper.login(getActivity(), user, password, new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				hideLoadingIndicator();
				switch (response.resultCode)
				{
					case BaseResponse.RESULT_OK:
						LoginResponse loginResponse = (LoginResponse)response;
						onLoginFinished(user, loginResponse.datas.realname, loginResponse.datas.key);
						break;
					case LoginResponse.RESULT_UNBIND:
						showToast(response.error);
						showBindHint();
						break;
					default:
						showToast(response.error);
						break;
				}
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
				hideLoadingIndicator();
				showToast("很难受，网络异常: " + error.errorCode);
			}
		});
	}

	private void showBindHint()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				StringBuffer sb = new StringBuffer();
				sb.append("您的设备还未授权，请电话联系客服告知设备号授权").append('\n');
				sb.append("设备号: ").append(android.os.Build.SERIAL).append('\n');
				sb.append("客服电话: ").append(Config.ServicePhone);
				bindHint.setText(sb.toString());
				bindHint.setVisibility(View.VISIBLE);
			}
		});
	}

	public void onLoginFinished(final String userName, final String realName, final String userKey)
	{
		Log.d(TAG, "onLoginFinished: userName="+LoginHelper.getUserName(getContext()));
		Log.d(TAG, "onLoginFinished: userPassword="+LoginHelper.getUserPassword(getContext()));
		LogUtil.v(TAG, "onLoginFinished: " + userName + "; " + realName + "; " + userKey + "; " + mLoginFinished);
		if (mLoginFinished)
			return;
		mLoginFinished = true;
		LoginHelper.onLoginFinished(getActivity(), userName, realName, userKey);
		if (getActivity() != MainActivity.instance)
		{
			Log.d(TAG, "onLoginFinished: 执行");

			Log.d(TAG, "onLoginFinished: 执行完毕");
			getActivity().finish();
			if (MainFragment.instance != null)
			{
				MainFragment.instance.backToHomepage();
				MainFragment.instance.doRefresh();
			}
			if (MainIndexFragment.instance != null)
				MainIndexFragment.instance.doRefresh();
		}
		else
		{

//			new ResourceHelper2(getActivity(), userKey, false, false)               //171229 登录时不检查更新
//					.checkAndDownloadResource(new OnResourceDownloadCallback()
//					{
//						@Override
//						public void onDownloadFinished(Object data, boolean canceled)
//						{
//							showLoadingIndicator();
//							RequestManager.loadShopLogoAndAD(getContext(), userKey, new RequestCallback()
//							{
//								@Override
//								public void onRequestResult(int requestCode, long taskId, BaseResponse response,
//										DataFrom from)
//								{
//									if (response != null)
//										if (response.resultCode == BaseResponse.RESULT_OK)
//										{
//										}
//										else
//										{
//											showToast(response.error);
//										}
									RequestManager.loadGoodsList(getActivity(), userKey, true, new RequestCallback()
									{
										@Override
										public void onRequestResult(int requestCode, long taskId,
												BaseResponse response, DataFrom from)
										{
											hideLoadingIndicator();
											String id = "5";
											String tag = Define.TAG_RING;
											if (response != null && response.resultCode == BaseResponse.RESULT_OK)
											{
												GoodsListResponse glResponse = (GoodsListResponse)response;
												if (glResponse.datas != null && glResponse.datas.list != null
														&& glResponse.datas.list.length > 0)
												{
													for (GoodsItem item : glResponse.datas.list)
													{
														if (item.tag.equals(Define.TAG_RING) && item.model_info != null)
														{
															id = item.id;
															tag = item.tag;
															break;
														}
													}
												}
											}
											BaseFragment.replace(getActivity(), new MainFragment(id, tag), false);
											if (from != DataFrom.SERVER)
												RequestManager.loadGoodsList(getContext(), userKey, true, null,
														CacheMode.PERFER_NETWORK);
										}

										@Override
										public void onRequestError(int requestCode, long taskId, ErrorInfo error)
										{
											onRequestResult(requestCode, taskId, null, DataFrom.SERVER);
										}
									});
//								}

//								@Override
//								public void onRequestError(int requestCode, long taskId, ErrorInfo error)
//								{
//									showToast("网络错误: " + error.errorCode);
//									onRequestResult(requestCode, taskId, null, DataFrom.SERVER);
//								}
//							});
//						}
//					});
		}
	}
}
