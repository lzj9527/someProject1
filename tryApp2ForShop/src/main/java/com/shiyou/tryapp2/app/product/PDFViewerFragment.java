package com.shiyou.tryapp2.app.product;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.extend.ErrorInfo;
import android.extend.app.BaseFragment;
import android.extend.loader.Loader;
import android.extend.util.AndroidUtils;
import android.extend.util.FileUtils;
import android.extend.util.ResourceUtil;
import android.extend.widget.PDFRendererView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shiyou.tryapp2.FileDownloadHelper;
import com.shiyou.tryapp2.FileDownloadHelper.OnFileDownloadCallback;
import com.shiyou.tryapp2.app.MainFragment;
import com.shiyou.tryapp2.data.FileInfo;

public class PDFViewerFragment extends BaseFragment
{
	public static String getPDFDirectoryPath(Context context)
	{
		return FileUtils.getDirectory(context, "pdf").getAbsolutePath();
	}

	public static void deletePDFDirectory(Context context)
	{
		FileUtils.deleteDirectory(FileUtils.getDirectory(context, "pdf"));
	}

	private String mTitle;
	private String mUrl;

	private PDFRendererView mPdfRendererView;

	public PDFViewerFragment(String title, String url)
	{
		mTitle = title;
		mUrl = url;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mLayoutResID = ResourceUtil.getLayoutId(getContext(), "pdfviewer_layout");
		View view = super.onCreateView(inflater, container, savedInstanceState);

		int id = ResourceUtil.getId(getContext(), "pdfView");
		mPdfRendererView = (PDFRendererView)view.findViewById(id);

		id = ResourceUtil.getId(getContext(), "middle_back");
		View boss_details_back = view.findViewById(id);
		boss_details_back.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				MainFragment.instance.onBackPressed();
			}
		});

		return view;
	}

	@Override
	public void onFirstStart()
	{
		super.onFirstStart();
		if (!TextUtils.isEmpty(mUrl))
		{
			if (Loader.isAssetUrl(mUrl))
			{
				String fileName = mUrl.substring(Loader.PROTOCOL_ASSETS.length());
				try
				{
					mPdfRendererView.openFromAsset(fileName);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					showToast("读取文件失败，请检查存储卡");
				}
			}
			else if (Loader.isHttpUrl(mUrl))
			{
				// showLoadingIndicator(true);
				FileInfo fileInfo = new FileInfo();
				fileInfo.url = mUrl;
				fileInfo.path = getPDFDirectoryPath(getContext()) + File.separator + mTitle + ".pdf";
				FileDownloadHelper.checkAndDownloadIfNeed(getContext(), TAG, fileInfo, new OnFileDownloadCallback()
				{
					@Override
					public void onDownloadStarted(Object tag, FileInfo fileInfo, String localPath)
					{
					}

					@Override
					public void onDownloadProgress(Object tag, FileInfo fileInfo, String localPath, long count,
							long length, float speed)
					{
					}

					@Override
					public void onDownloadFinished(Object tag, FileInfo fileInfo, String localPath)
					{
						// hideLoadingIndicator();
						openFromFile(localPath);
					}

					@Override
					public void onDownloadFailed(Object tag, FileInfo fileInfo, ErrorInfo error)
					{
						// hideLoadingIndicator();
						showToast("网络错误: " + error.errorCode);
					}

					@Override
					public void onDownloadCanceled(Object tag, FileInfo fileInfo)
					{
					}
				}, true);
			}
			else
			{
				openFromFile(mUrl);
			}
		}
	}

	private void openFromFile(final String path)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					mPdfRendererView.openFromFile(path);
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
					showToast("未找到文件,path=" + path);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					showToast("读取文件失败，请检查存储卡");
					FileUtils.deleteFile(path);
				}
			}
		});
	}
}
