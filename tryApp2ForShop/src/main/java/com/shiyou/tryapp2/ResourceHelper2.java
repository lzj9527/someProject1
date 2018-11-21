package com.shiyou.tryapp2;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.extend.ErrorInfo;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.Loader.CacheMode;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.widget.ExtendDialog;
import android.extend.widget.ProgressBar;
import android.extend.widget.ProgressBar.ChangeProgressMode;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.shiyou.tryapp2.FileDownloadHelper.DownloadStatus;
import com.shiyou.tryapp2.FileDownloadHelper.OnMultiFileDownloadCallback;
import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.data.FileInfo;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse;
import com.shiyou.tryapp2.data.response.GoodsListResponse.GoodsItem;

public class ResourceHelper2
{
	public interface OnResourceDownloadCallback
	{
		public void onDownloadFinished(Object data, boolean canceled);
	}

	public static final String TAG = "ResourceHelper2";

	private static final CharSequence DATEFORMAT = "yyyyMMdd";
	private Context mContext;
	private Activity mActivity;
	private boolean mRunInBackground;
	// private String mShopId;
	private String mUserKey;
	private boolean mCancelable = false;
	// private int mDownloadStep = 0;
	// private ProgressDialog mProgressDialog;
	private ExtendDialog mProgressDialog;
	private OnResourceDownloadCallback mCallback;
	private boolean mCanceled = false;

	private static final String Pref_Name = "resouce_downloaded";

	public ResourceHelper2(Context context, String userKey)
	{
		mContext = context;
		mActivity = null;
		// mShopId = context.getPackageName();
		mUserKey = userKey;
		mRunInBackground = true;
		mCancelable = false;
		mCanceled = false;
		// mDownloadStep = 0;
	}

	public ResourceHelper2(Activity activity, String userKey, boolean runInBackground, boolean cancelable)
	{
		mContext = activity;
		mActivity = activity;
		// mShopId = activity.getPackageName();
		mUserKey = userKey;
		mRunInBackground = runInBackground;
		mCancelable = cancelable;
		mCanceled = false;
		// mDownloadStep = 0;
	}

	private WakeLock mWakeLock;

	private void acquireWakeLock()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.d(TAG, "acquireWakeLock...");
				if (mWakeLock == null)
				{
					try
					{
						PowerManager manager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
						mWakeLock = manager.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
						mWakeLock.acquire();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void releaseWakeLock()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.d(TAG, "releaseWakeLock...");
				if (mWakeLock != null)
				{
					try
					{
						mWakeLock.release();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					mWakeLock = null;
				}
			}
		});
	}

	private String mTitle;

	private void showProgressDialog(final String text)
	{
		mTitle = text;
		if (mActivity == null)
			return;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog == null)
				{
					int layout = ResourceUtil.getLayoutId(mContext, "download_progress_dialog");
					View view = View.inflate(mContext, layout, null);
					mProgressDialog = AndroidUtils.createDialog(mActivity, view, false, false);

					int id = ResourceUtil.getId(mContext, "title");
					TextView title = (TextView)mProgressDialog.findViewById(id);
					title.setText(text);

					id = ResourceUtil.getId(mContext, "progressBar");
					ProgressBar progressBar = (ProgressBar)mProgressDialog.findViewById(id);
					progressBar.setChangeProgressMode(ChangeProgressMode.NONE);
					progressBar.setProgress(0);

					id = ResourceUtil.getId(mContext, "percent");
					TextView percent = (TextView)mProgressDialog.findViewById(id);
					percent.setText("0%");

					id = ResourceUtil.getId(mContext, "cancel");
					View cancel = mProgressDialog.findViewById(id);
					if (mCancelable)
					{
						cancel.setVisibility(View.VISIBLE);
						cancel.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								LogUtil.d(TAG, "update resource canceled.");
								releaseWakeLock();
								mCanceled = true;
								hideProgressDialog();
								FileDownloadHelper.cancelAllDownload(mContext);
								if (mCallback != null)
									mCallback.onDownloadFinished(TAG, mCanceled);
							}
						});
					}
					else
						cancel.setVisibility(View.GONE);
					mProgressDialog.show();
				}
			}
		});
	}

	private void updateProgressDialogTitle(final String text)
	{
		mTitle = text;
		if (mActivity == null)
			return;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
				{
					int id = ResourceUtil.getId(mContext, "title");
					TextView title = (TextView)mProgressDialog.findViewById(id);
					title.setText(text);
				}
			}
		});
	}

	private void updateProgressDialogProgress(final int progress, final int max)
	{
		if (mActivity == null)
			return;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
				{
					int id = ResourceUtil.getId(mContext, "title");
					TextView title = (TextView)mProgressDialog.findViewById(id);
					title.setText(mTitle + " " + progress + "/" + max);
				}
			}
		});
	}

	private void updateDownloadProgress(final long count, final long length, final float speed)
	{
		if (mActivity == null)
			return;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
				{
					float percentF = (100 * count) / length;

					int id = ResourceUtil.getId(mContext, "progressBar");
					ProgressBar progressBar = (ProgressBar)mProgressDialog.findViewById(id);
					progressBar.setChangeProgressMode(ChangeProgressMode.NONE);
					progressBar.setProgress(percentF);

					id = ResourceUtil.getId(mContext, "percent");
					TextView percent = (TextView)mProgressDialog.findViewById(id);
					percent.setText((int)percentF + "%");

					id = ResourceUtil.getId(mContext, "speed");
					TextView speedText = (TextView)mProgressDialog.findViewById(id);
					speedText.setText((int)speed + "k/s");
				}
			}
		});
	}

	private void hideProgressDialog()
	{
		if (mActivity == null)
			return;
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mProgressDialog != null)
				{
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
		});
	}

	private String getCurrentDate()
	{
		return DateFormat.format(DATEFORMAT, System.currentTimeMillis()).toString();
	}

	private synchronized boolean checkResourceDownloaded()
	{
		SharedPreferences pref = mContext.getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
		String value = pref.getString("date", "");
		String date = getCurrentDate();
		boolean result = date.equals(value);
		// boolean result = false;
		// if (!TextUtils.isEmpty(value))
		// result = true;
		LogUtil.i(TAG, "checkResourceDownloaded: " + value + "; " + date + "; " + result);
		return result;
	}

	private synchronized void setResourceDownloaded()
	{
		SharedPreferences pref = mContext.getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putString("date", getCurrentDate());
		edit.commit();
	}

	public void checkAndDownloadResource(OnResourceDownloadCallback callback)
	{
		if (checkResourceDownloaded())
		{
			if (callback != null)
				callback.onDownloadFinished(TAG, false);
			return;
		}
		mCallback = callback;
		startCheckAndDownloadModels();
	}

	public void downloadResource(OnResourceDownloadCallback callback)
	{
		mCallback = callback;
		startCheckAndDownloadModels();
	}

	private long time;

	private void startCheckAndDownloadModels()
	{
		acquireWakeLock();
		time = System.currentTimeMillis();
		if (!mRunInBackground)
		{
			showProgressDialog("获取资源数据");
		}
		RequestManager.loadGoodsList(mContext, mUserKey, true, new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				time = System.currentTimeMillis() - time;
				LogUtil.v(TAG, "获取接口数据time=" + time);
				time = System.currentTimeMillis();
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					GoodsListResponse glResponse = (GoodsListResponse)response;
					if (glResponse.datas != null && glResponse.datas.list != null)
					{
						if (mCanceled)
							return;
						AndroidUtils.MainHandler.post(new checkAndDownloadModelFileRunnable(glResponse.datas.list));
					}
					else
					{
						if (mCanceled)
							return;
						hideProgressDialog();
						if (mCallback != null)
							mCallback.onDownloadFinished(TAG, mCanceled);
					}
				}
				else
				{
					if (mCanceled)
						return;
					AndroidUtils.showToast(mActivity, response.error);
					hideProgressDialog();
					if (mCallback != null)
						mCallback.onDownloadFinished(TAG, mCanceled);
				}
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
				if (mCanceled)
					return;
				if (mActivity != null)
					AndroidUtils.showToast(mActivity, "网络异常: " + error.errorCode);
				hideProgressDialog();
				if (mCallback != null)
					mCallback.onDownloadFinished(TAG, mCanceled);
			}
		}, CacheMode.PERFER_NETWORK);
	}

	private class checkAndDownloadModelFileRunnable implements Runnable
	{
		GoodsItem[] goodsList;
		int length = 0;
		int count = 0;

		public checkAndDownloadModelFileRunnable(GoodsItem[] list)
		{
			this.goodsList = list;
			if (list != null)
				this.length = list.length;
			this.count = 0;
		}

		public void run()
		{
			if (mCanceled)
				return;
			if (!mRunInBackground)
			{
				updateProgressDialogTitle("资源更新");
				updateProgressDialogProgress(count + 1, length);
			}
			LogUtil.v(TAG, "checkAndDownloadModelFile: " + count + "/" + length);
			if (goodsList != null && count < goodsList.length)
			{
				GoodsItem item = goodsList[count];
				count++;
				OnMultiFileDownloadCallback callback = new OnMultiFileDownloadCallback()
				{
					public void onMultiDownloadStarted(Object tag, FileInfo fileInfo, String localPath,
							int downloadIndex)
					{
					}

					public void onMultiDownloadProgress(Object tag, FileInfo fileInfo, String localPath, long count,
							long length, float speed, int downloadIndex)
					{
						updateDownloadProgress(count, length, speed);
					}

					public void onMultiDownloadFinished(Object tag, FileInfo fileInfo, String localPath,
							int downloadIndex)
					{
					}

					public void onMultiDownloadCanceled(Object tag, FileInfo fileInfo, int downloadIndex)
					{
					}

					public void onMultiDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error, int downloadIndex)
					{
					}

					public void onMultiAllDownloadFinished(Object tag, FileInfo[] fileInfos, DownloadStatus[] status)
					{
						AndroidUtils.MainHandler.post(checkAndDownloadModelFileRunnable.this);
					}
				};
				List<FileInfo> list = new ArrayList<FileInfo>();
				if (item.model_info != null)
				{
					list.add(item.model_info);
				}
				else if (item.model_infos != null)
				{
					if (item.model_infos.men != null)
						list.add(item.model_infos.men);
					if (item.model_infos.wmen != null)
						list.add(item.model_infos.wmen);
				}
				if (!list.isEmpty())
				{
					FileInfo[] fileInfos = new FileInfo[list.size()];
					fileInfos = list.toArray(fileInfos);
					if (mCanceled)
						return;
					FileDownloadHelper.startMultiDownload(item, mContext, fileInfos, callback, true, false);
				}
				else
					AndroidUtils.MainHandler.post(checkAndDownloadModelFileRunnable.this);
			}
			else
			{
				time = System.currentTimeMillis() - time;
				LogUtil.v(TAG, "检查下载资源time=" + time);
				time = System.currentTimeMillis();
				setResourceDownloaded();
				hideProgressDialog();
				if (mCanceled)
					return;
				if (mCallback != null)
				{
					mCallback.onDownloadFinished(TAG, mCanceled);
				}
				releaseWakeLock();
			}
		}
	}
}
