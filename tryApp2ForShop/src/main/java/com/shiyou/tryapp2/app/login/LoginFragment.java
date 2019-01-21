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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;
import android.webkit.WebViewFragment;
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
import com.shiyou.tryapp2.app.product.MainIndexFragment;
import com.shiyou.tryapp2.app.product.MainWebFragment;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse.GoodsItem;
import com.shiyou.tryapp2.data.response.LoginResponse;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
		getToken();
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
//
// replace(MainFragment.instance, new MainWebFragment("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?token="+Config.token, 0,true), false);
//			AndroidUtils.MainHandler.post(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					WebViewFragment webViewFragment=new WebViewFragment();
//					webViewFragment.getWebView().loadUrl("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?token="+Config.token);
//				}
//			});
		}
//		WebView webView=new WebView(getContext());
//		webView.loadUrl("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?&"+getToken());
//		Log.d(TAG, "onLoginFinished: token="+getToken());
	}
	//获取toKen
	public String getToken(){


		FormBody formBody=new FormBody.Builder().add("username",LoginHelper.getUserName(getContext())).add("password",LoginHelper.getUserPassword(getContext())).build();
		Request request=new Request.Builder().url("https://api.zsa888.cn/login").addHeader("accept","application/vnd.zsmt.shop.v1+json").post(formBody).build();
		OkHttpClient okHttpClient=new OkHttpClient();
		okHttpClient.newCall(request).enqueue(new Callback() {
			private String token;

			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println(e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String all = response.body().string();
				int i = all.indexOf("access_token");
				int j = all.indexOf("token_type");
				Log.d(TAG, "onResponse: all=" + all);
				Log.d(TAG, "onResponse: i=" + i);
				Log.d(TAG, "onResponse: j=" + j);
				try {
					token = all.substring(i + 15, j - 3);
					AndroidUtils.MainHandler.post(new Runnable() {
						@Override
						public void run() {
//									int id=ResourceUtil.getId(getContext(),"test_token");
//									WebView webView=(WebView) getView().findViewById(id);
//									webView.loadUrl("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?token="+token);
							Config.token=token;
//									replace(instance, new MainWebFragment("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?token="+token, 0,true), false);

//									Log.d(TAG, "run: textToken="+textView.getText());
//                                	OkHttpClient okHttpClient1=new OkHttpClient();
//									FormBody formBody=new FormBody.Builder().add("token",token).build();
//									final Request request=new Request.Builder().url("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html").post(formBody).build();
//									OkHttpClient okHttpClient=new OkHttpClient();
//									okHttpClient.newCall(request).enqueue(new Callback() {
//										@Override
//										public void onFailure(Call call, IOException e) {
//											Log.d(TAG, "onFailure: 222执行");
//										}
//
//										@Override
//										public void onResponse(Call call, Response response) throws IOException {
//											String token=response.body().string();
//
//											Log.d(TAG, "onResponse: 222执行 token="+token);
//										}
//									});
//                                    Log.d(TAG, "run: token222="+token);
//									int id=ResourceUtil.getId(getContext(),"test_token");
//									WebView webView= (WebView) getView().findViewById(id);
//									webView.setWebViewClient(new WebViewClient());
//									webView.evaluateJavascript("http://www.zsa888.com/addons/ewei_shop/template/pad/default/shop/getToken.html?token=" + token, new ValueCallback<String>() {
//										@Override
//										public void onReceiveValue(String value) {
//											Log.d(TAG, "onReceiveValue: value="+value);
//										}
//									});
						}
					});
				}catch (Exception e){
					i=all.indexOf("data");
					j=all.lastIndexOf("}");
					token=all.substring(i+7,j-1);
				}
			}
		});
//			RequestManager.getToken(getContext(), LoginHelper.getUserName(getCo ntext()), LoginHelper.getUserPassword(getContext()), new RequestCallback() {
//				@Override
//				public void onRequestError(int requestCode, long taskId, ErrorInfo error) {
//

//				}
//
//				@Override
//				public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from) {
//					TokenResponse tokenResponse=(TokenResponse)response;
//					token=tokenResponse.tokenInfo.token;
//
//					Log.d(TAG, "onRequestResult: token="+token);
//				}
//			});
		return  Config.token;
	}
}
