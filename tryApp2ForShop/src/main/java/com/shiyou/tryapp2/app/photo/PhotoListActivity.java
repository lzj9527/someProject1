package com.shiyou.tryapp2.app.photo;

import android.content.Intent;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.util.ViewTools.FitMode;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseGridAdapter;
import android.extend.widget.adapter.ScrollGridView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.app.BaseAppActivity;
import com.shiyou.tryapp2.app.photo.data.PhotoAlbumData;
import com.shiyou.tryapp2.app.photo.data.PhotoData;

public class PhotoListActivity extends BaseAppActivity
{
	private class PhotoListItem extends AbsAdapterItem
	{
		private PhotoData mPhoto;

		public PhotoListItem(PhotoData data)
		{
			mPhoto = data;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onCreateView: " + position);
			int layout = ResourceUtil.getLayoutId(getApplicationContext(), "photo_album_item");
			View view = View.inflate(getApplicationContext(), layout, null);

			int id = ResourceUtil.getId(getApplicationContext(), "text");
			TextView textView = (TextView)view.findViewById(id);
			textView.setVisibility(View.GONE);
			id = ResourceUtil.getId(getApplicationContext(), "image");
			ImageView imageView = (ImageView)view.findViewById(id);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			ViewTools.autoFitViewDimension(imageView, view, FitMode.FIT_IN_PARENT_WIDTH, 1f);

			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onUpdateView: " + position + "; " + view);
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onLoadViewResource: " + position + "; " + view);
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ImageView imageView = (ImageView)view.findViewById(id);
			if (mPhoto.id > 0)
			{
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							int origId = mPhoto.id;
							final Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(),
									origId, Thumbnails.MINI_KIND, null);
							AndroidUtils.MainHandler.post(new Runnable()
							{
								@Override
								public void run()
								{
									imageView.setImageBitmap(bitmap);
								}
							});
						}
						catch (Throwable e)
						{
							e.printStackTrace();
						}
					}
				}).start();
			}
			else
			{
				// TODO
			}
		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
			LogUtil.v(TAG, "onRecycleViewResource: " + position + "; " + view);
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ImageView imageView = (ImageView)view.findViewById(id);
			ViewTools.recycleImageView(imageView);
		}

		@Override
		public void onItemClick(View adapterView, ViewGroup parent, View view, int position, long id)
		{
			Intent intent = new Intent();
			intent.putExtra(Define.Name_ID, mPhoto.id);
			intent.putExtra(Define.Name_Path, mPhoto.path);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	private ScrollGridView mPhotoView;
	private PhotoAlbumData mAlbumData;
	private BaseGridAdapter<AbsAdapterItem> mPhotoAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		int layout = ResourceUtil.getLayoutId(getApplicationContext(), "photo_album_layout");
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

		mAlbumData = (PhotoAlbumData)getIntent().getExtras().get(Define.Name_Album);
		int id = ResourceUtil.getId(getApplicationContext(), "grid");
		mPhotoView = (ScrollGridView)findViewById(id);
		mPhotoAdapter = new BaseGridAdapter<AbsAdapterItem>();
		mPhotoView.setAdapter(mPhotoAdapter);
		mPhotoView.setNumColumns(2);
		int space = AndroidUtils.dp2px(getApplicationContext(), 10);
		mPhotoView.setVerticalDividerWidth(space);
		mPhotoView.setHorizontalDividerHeight(space);
		mPhotoView.setPadding(space, space, space, space);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// checkFiles();
				makePhotoListItems();
			}
		}).start();
	}

	// private void checkFiles()
	// {
	// File directory = new File(mAlbumData.path);
	// File[] files = directory.listFiles();
	// boolean find = false;
	// for (File file : files)
	// {
	// find = false;
	// for (PhotoData data : mAlbumData.photoList)
	// {
	// if (data.path.contains(file.getPath()))
	// {
	// find = true;
	// break;
	// }
	// }
	// if (!find)
	// {
	// PhotoData pd = new PhotoData(-1, file.getPath());
	// mAlbumData.photoList.add(pd);
	// }
	// }
	// }

	private void makePhotoListItems()
	{

		for (PhotoData data : mAlbumData.photoList)
		{
			LogUtil.i(TAG, data.toString());
			mPhotoAdapter.addItem(new PhotoListItem(data));
		}
	}
}
