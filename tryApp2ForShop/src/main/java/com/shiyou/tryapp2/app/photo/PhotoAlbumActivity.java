package com.shiyou.tryapp2.app.photo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.ViewGroup;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.app.BaseAppActivity;
import com.shiyou.tryapp2.app.photo.data.PhotoAlbumData;
import com.shiyou.tryapp2.app.photo.data.PhotoData;

public class PhotoAlbumActivity extends BaseAppActivity {
	public static void launchMeForResult(Activity activity, int requestCode) {
		Intent intent = new Intent(activity, PhotoAlbumActivity.class);
		activity.startActivityForResult(intent, requestCode);
	}

	public class PhotoAlbumItem extends AbsAdapterItem {
		private PhotoAlbumData mPhotoAlbum;

		public PhotoAlbumItem(PhotoAlbumData data) {
			mPhotoAlbum = data;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent) {
			int layout = ResourceUtil.getLayoutId(getApplicationContext(),
					"photo_album_item");
			View view = View.inflate(getApplicationContext(), layout, null);

			int id = ResourceUtil.getId(getApplicationContext(), "image");
			ImageView imageView = (ImageView) view.findViewById(id);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			ViewTools.autoFitViewDimension(imageView, view,
					FitMode.FIT_IN_PARENT_WIDTH, 1f);

			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent) {
			int id = ResourceUtil.getId(getApplicationContext(), "text");
			TextView textView = (TextView) view.findViewById(id);
			textView.setText(mPhotoAlbum.name + "("
					+ mPhotoAlbum.photoList.size() + ")");
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent) {
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ImageView imageView = (ImageView) view.findViewById(id);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						int origId = mPhotoAlbum.photoList.get(0).id;
						final Bitmap bitmap = MediaStore.Images.Thumbnails
								.getThumbnail(getContentResolver(), origId,
										Thumbnails.MINI_KIND, null);
						AndroidUtils.MainHandler.post(new Runnable() {
							@Override
							public void run() {
								imageView.setImageBitmap(bitmap);
							}
						});
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		@Override
		public void onRecycleViewResource(View view, int position,
				ViewGroup parent) {
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ImageView imageView = (ImageView) view.findViewById(id);
			ViewTools.recycleImageView(imageView);
		}

		@Override
		public void onItemClick(View adapterView, ViewGroup parent, View view,
				int position, long id) {
			Intent intent = new Intent(getApplicationContext(),
					PhotoListActivity.class);
			intent.putExtra(Define.Name_Album, mPhotoAlbum);
			startActivityForResult(intent, 1);
		}
	}

	private ScrollGridView mAlbumView;
	private BaseGridAdapter<AbsAdapterItem> mAlbumAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layout = ResourceUtil.getLayoutId(getApplicationContext(),
				"photo_album_layout");
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
		
		int id = ResourceUtil.getId(getApplicationContext(), "back");
		View back = findViewById(id);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AndroidUtils.isFastClick())
					return;
				finish();
			}
		});

		id = ResourceUtil.getId(getApplicationContext(), "grid");
		mAlbumView = (ScrollGridView) findViewById(id);
		mAlbumAdapter = new BaseGridAdapter<AbsAdapterItem>();
		mAlbumView.setAdapter(mAlbumAdapter);
		mAlbumView.setNumColumns(2);
		int space = AndroidUtils.dp2px(getApplicationContext(), 10);
		mAlbumView.setVerticalDividerWidth(space);
		mAlbumView.setHorizontalDividerHeight(space);
		mAlbumView.setPadding(space, space, space, space);
		new Thread(new Runnable() {
			@Override
			public void run() {
				makePhotoAlbumItems();
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			setResult(resultCode, data);
			finish();
		}
	}

	private void makePhotoAlbumItems() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<PhotoAlbumData> list = getPhotoAlbum();
				for (PhotoAlbumData data : list) {
					mAlbumAdapter.addItem(new PhotoAlbumItem(data));
				}
			}
		}).start();
	}

	private List<PhotoAlbumData> getPhotoAlbum() {
		List<PhotoAlbumData> albumList = new ArrayList<PhotoAlbumData>();
		Map<String, PhotoAlbumData> albumMap = new HashMap<String, PhotoAlbumData>();
		PhotoAlbumData pa = null;
		Cursor cursor = MediaStore.Images.Media.query(getContentResolver(),
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media._ID));
			String dir_id = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
			String dir = cursor
					.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
			String path = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			if (!albumMap.containsKey(dir_id)) {
				pa = new PhotoAlbumData();
				pa.id = dir_id;
				pa.name = dir;
				File file = new File(path);
				pa.path = file.getParent();
				pa.photoList.add(new PhotoData(Integer.valueOf(id), path));
				albumMap.put(dir_id, pa);
			} else {
				pa = albumMap.get(dir_id);
				pa.photoList.add(new PhotoData(Integer.valueOf(id), path));
			}
		}
		cursor.close();
		Iterable<String> it = albumMap.keySet();
		for (String key : it) {
			albumList.add(albumMap.get(key));
		}
		return albumList;
	}
}
