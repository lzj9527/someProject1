package com.shiyou.tryapp2.app.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.extend.app.BaseActivity;
import android.extend.util.AndroidUtils;
import android.extend.util.BitmapUtils;
import android.extend.util.DataUtils;
import android.extend.util.FileUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.util.ViewTools.FitMode;
import android.extend.widget.CameraPreview;
import android.extend.widget.ExtendImageView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BaseAdapter;
import android.extend.widget.adapter.HorizontalScrollListView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.shiyou.tryapp2.Config;
import com.shiyou.tryapp2.Define;
import com.shiyou.tryapp2.app.photo.PhotoAlbumActivity;

public class CameraActivity extends BaseActivity {
	public static void launchMeForResult(Activity activity, int tryonType,
			int maskIndex, int requestCode) {
		Intent intent = new Intent(activity, CameraActivity.class);
		intent.putExtra(Define.Name_TryonType, tryonType);
		intent.putExtra(Define.Name_MaskIndex, maskIndex);
		intent.putExtra(Define.Name_RequestCode, requestCode);
		activity.startActivityForResult(intent, requestCode);
	}

	public static File getCameraDirectory(Context context) {
		return FileUtils.getDirectory(context, "camera");
	}

	public static File newJpegFile(Context context) {
		String name = FileUtils.makeNameInCurrentTime();
		return FileUtils.getFile(context, "camera", name + ".jpeg");
	}

	public static String getMaskDirName(int typeIndex) {
		String dirName;
		switch (typeIndex) {
		case Config.Type_Earring:
			dirName = "EarMasks";
			break;
		case Config.Type_Necklace:
		case Config.Type_Ring:
			dirName = "RingMasks";
			break;
		default:
			dirName = "";
			break;
		}
		return dirName;
	}

	private int mTryonType;
	private int mMaskIndex;

	private CameraPreview mCameraPreview;

	private ImageView mImageView;

	private List<String> mMaskPathList;
	private Bitmap mMaskBitmap;
	private HorizontalScrollListView mMaskList;
	private BaseAdapter<AbsAdapterItem> mMaskListAdapter;

	private boolean mCameraButtonLocked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layout = ResourceUtil.getLayoutId(this, "camera_layout");
		View view = View.inflate(getApplicationContext(), layout, null);
		setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		if (AndroidUtils.checkDeviceHasNavigationBar(this))
			view.setFitsSystemWindows(false);
		else
			view.setFitsSystemWindows(true);

		mTryonType = getIntent().getIntExtra(Define.Name_TryonType, 0);
		mMaskIndex = getIntent().getIntExtra(Define.Name_MaskIndex, 0);
		LogUtil.v(TAG, "mTryonType=" + mTryonType + "; mMaskIndex="
				+ mMaskIndex);

		int id = ResourceUtil.getId(this, "camera");
		mCameraPreview = (CameraPreview) findViewById(id);
		mCameraPreview.setExpectedPictureSize(1920, 1080);

		id = ResourceUtil.getId(this, "image");
		mImageView = (ImageView) findViewById(id);

		id = ResourceUtil.getId(this, "mask_list");
		mMaskList = (HorizontalScrollListView) findViewById(id);
		mMaskList.setVerticalDividerWidth(5);
		mMaskList.setSelectorPadding(AndroidUtils.dp2px(
				getApplicationContext(), 1));
		int resId = ResourceUtil.getDrawableId(this,
				"selector_image_background");
		mMaskList.setSelector(resId);

		mMaskListAdapter = new BaseAdapter<AbsAdapterItem>();
		mMaskList.setAdapter(mMaskListAdapter);

		id = ResourceUtil.getId(this, "shutter_button");
		View shutter_button = findViewById(id);
		shutter_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AndroidUtils.isFastClick())
					return;
				takePicture();
			}
		});

		id = ResourceUtil.getId(this, "camera_switch_button");
		View camera_switch_button = findViewById(id);
		camera_switch_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AndroidUtils.isFastClick())
					return;
				if (mCameraPreview.getCameraId() == CameraInfo.CAMERA_FACING_BACK)
					mCameraPreview.openFacingFrontCamera();
				else
					mCameraPreview.openFacingBackCamera();
			}
		});

		ensureMasks(mTryonType);
		mMaskList.setSelection(mMaskIndex);
		setCurrentMask(mMaskIndex);

		id = ResourceUtil.getId(getApplicationContext(), "gallery_button");
		View gallery_button = findViewById(id);
		gallery_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AndroidUtils.isFastClick())
					return;
				PhotoAlbumActivity.launchMeForResult(CameraActivity.this,
						Define.REQ_ALBUM);
			}
		});

		updateLightButton();
		id = ResourceUtil.getId(getApplicationContext(), "light_button");
		View light_button = findViewById(id);
		light_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AndroidUtils.isFastClick())
					return;
				if (mCameraPreview.getFlashMode().equals(
						Parameters.FLASH_MODE_AUTO)) {
					mCameraPreview.setFlashMode(Parameters.FLASH_MODE_ON);
				} else if (mCameraPreview.getFlashMode().equals(
						Parameters.FLASH_MODE_ON)) {
					mCameraPreview.setFlashMode(Parameters.FLASH_MODE_OFF);
				} else if (mCameraPreview.getFlashMode().equals(
						Parameters.FLASH_MODE_OFF)) {
					mCameraPreview.setFlashMode(Parameters.FLASH_MODE_AUTO);
				}
				updateLightButton();
			}
		});

		id = ResourceUtil.getId(getApplicationContext(), "camera_btn_del");
		View camera_btn_del = findViewById(id);
		camera_btn_del.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AndroidUtils.isFastClick())
					return;
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			String path = data.getStringExtra(Define.Name_Path);
			finish(path);
		}
	}

	// @Override
	// public void finish()
	// {
	// super.finish();
	// System.exit(0);
	// }

	private void finish(String path) {
		Intent intent = new Intent();
		intent.putExtra(Define.Name_TryonType, mTryonType);
		intent.putExtra(Define.Name_Path, path);
		intent.putExtra(Define.Name_MaskIndex, mMaskIndex);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void updateLightButton() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int id = ResourceUtil.getId(getApplicationContext(),
						"light_button");
				ImageView light_button = (ImageView) findViewById(id);
				if (mCameraPreview.getFlashMode().equals(
						Parameters.FLASH_MODE_OFF)) {
					int resId = ResourceUtil.getDrawableId(
							getApplicationContext(), "btn_light_close");
					light_button.setImageResource(resId);
				} else if (mCameraPreview.getFlashMode().equals(
						Parameters.FLASH_MODE_ON)) {
					int resId = ResourceUtil.getDrawableId(
							getApplicationContext(), "btn_light_open");
					light_button.setImageResource(resId);
				} else if (mCameraPreview.getFlashMode().equals(
						Parameters.FLASH_MODE_AUTO)) {
					int resId = ResourceUtil.getDrawableId(
							getApplicationContext(), "btn_light_auto");
					light_button.setImageResource(resId);
				}
			}
		});
	}

	public static List<String> getMaskPathList(String tag, AssetManager assets,
			int type) {
		String dirName = getMaskDirName(type);
		List<String> list = new ArrayList<String>();
		// try
		// {
		// String[] names = assets.list(dirName);
		// if (names != null)
		// {
		// String path;
		// for (String name : names)
		// {
		// if (name.contains("location") || name.contains("mini") ||
		// name.contains("noframe"))
		// continue;
		// path = dirName + File.separatorChar + name;
		// LogUtil.v(tag, "Mask Path = " + path);
		// list.add(path);
		// }
		// }
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		return list;
	}

	public static List<String> getMiniMaskPathList(String tag,
			AssetManager assets, int type) {
		String dirName = getMaskDirName(type) + File.separatorChar + "mini";
		List<String> list = new ArrayList<String>();
		// try
		// {
		// String[] names = assets.list(dirName);
		// if (names != null)
		// {
		// String path;
		// for (String name : names)
		// {
		// path = dirName + File.separatorChar + name;
		// LogUtil.v(tag, "MiniMask Path = " + path);
		// list.add(path);
		// }
		// }
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		return list;
	}

	public static List<String> getNoframeMaskPathList(String tag,
			AssetManager assets, int type) {
		String dirName = getMaskDirName(type) + File.separatorChar + "noframe";
		List<String> list = new ArrayList<String>();
		try {
			String[] names = assets.list(dirName);
			if (names != null) {
				String path;
				for (String name : names) {
					path = dirName + File.separatorChar + name;
					LogUtil.v(tag, "NoFrameMask Path = " + path);
					list.add(path);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private void ensureMasks(int type) {
		mMaskPathList = getMaskPathList(TAG, getAssets(), type);
		List<String> miniPathList = getMiniMaskPathList(TAG, getAssets(), type);
		for (String path : miniPathList) {
			mMaskListAdapter.addItem(new MaskAdapterItem(path));
		}
	}

	public static Bitmap getMaskeBitmap(String tag, AssetManager assets,
			String path) {
		InputStream is = null;
		try {
			is = assets.open(path);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			LogUtil.v(tag, "getMaskBitmap: " + path + "; " + bitmap.getWidth()
					+ "x" + bitmap.getHeight());
			return bitmap;
		} catch (Exception e) {
			LogUtil.w(tag, "", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private Bitmap getMaskBitmap(int index) {
		String path = mMaskPathList.get(index);
		return getMaskeBitmap(TAG, getAssets(), path);
	}

	public static Bitmap getMaskThumbnailBitmap(String tag,
			AssetManager assets, String path, int outHeight) {
		InputStream is = null;
		byte[] data;
		try {
			is = assets.open(path);
			data = DataUtils.readInStreamData(is, 10);
			Bitmap bitmap = BitmapUtils.decodeBitmapFitHeight(data, outHeight);// BitmapFactory.decodeStream(is);
			LogUtil.v(tag, "getMaskThumbnailBitmap: " + path + "; " + outHeight
					+ "; " + bitmap.getWidth() + "x" + bitmap.getHeight());
			return bitmap;
		} catch (Exception e) {
			LogUtil.w(tag, "", e);
		} finally {
			data = null;
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.gc();
		}
		return null;
	}

	private void setCurrentMask(int index) {
		LogUtil.i(TAG, "setCurrentMask: " + index);
		if (mMaskPathList.isEmpty()) {
			mImageView.setVisibility(View.GONE);
			return;
		}
		if (index > mMaskPathList.size() - 1) {
			index = 0;
		} else if (index < 0) {
			index = mMaskPathList.size() - 1;
		}
		mMaskIndex = index;
		Bitmap bitmap = getMaskBitmap(index);
		String maskName = mMaskPathList.get(index);
		switch (mTryonType) {
		case Config.Type_Earring:
			if (maskName.contains("left") || maskName.contains("right")) {
				DisplayMetrics dm = AndroidUtils
						.getActivityDisplayMetrics(this);
				int x = 0;
				if (maskName.contains("left"))
					x = dm.widthPixels * 2 / 3 - bitmap.getWidth() / 2;
				else if (maskName.contains("right"))
					x = dm.widthPixels / 3 - bitmap.getWidth() / 2;
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView
						.getLayoutParams();
				params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
				params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
				params.leftMargin = x;
				params.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
				params.addRule(RelativeLayout.CENTER_VERTICAL);
				mImageView.setLayoutParams(params);
				mImageView.setScaleType(ScaleType.CENTER_INSIDE);
				break;
			}
		default:
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView
					.getLayoutParams();
			params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
			params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
			params.setMargins(0, 0, 0, 0);
			params.addRule(RelativeLayout.CENTER_VERTICAL, 0);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			mImageView.setLayoutParams(params);
			mImageView.setScaleType(ScaleType.CENTER_CROP);
			break;
		}
		mMaskBitmap = bitmap;
		mImageView.setVisibility(View.VISIBLE);
		mImageView.setImageBitmap(mMaskBitmap);
	}

	private void takePicture() {
		LogUtil.i(TAG, "takePicture: " + mCameraButtonLocked);
		if (mCameraButtonLocked)
			return;
		mCameraButtonLocked = true;
		mCameraPreview.autoFocusAndTakePicture(null, new ShutterCallback() {
			@Override
			public void onShutter() {
				LogUtil.v(TAG, "onShutter");
			}
		}, null, null, new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				LogUtil.v(TAG, "onPictureTaken: " + data + "; " + camera);
				mCameraPreview.stopPreview();
				saveAndFinish(data, camera, false);
			}
		});
	}

	private void saveAndFinish(final byte[] data, final Camera camera,
			final boolean isPreview) {
		showLoadingIndicatorDialog(getString("default_handling_text"));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File file = newJpegFile(getApplicationContext());
					FileOutputStream fos = new FileOutputStream(file);
					if (isPreview) {
						int format = camera.getParameters().getPreviewFormat();
						Size size = camera.getParameters().getPreviewSize();
						YuvImage image = new YuvImage(data, format, size.width,
								size.height, null);
						image.compressToJpeg(new Rect(0, 0, size.width,
								size.height), 100, fos);
					} else {
						fos.write(data);
					}
					fos.flush();
					fos.close();
					// mCamera.startPreview();
					boolean isFrontCamera = (mCameraPreview.getCameraId() == CameraInfo.CAMERA_FACING_FRONT);
					String path = handleImageOrientation(
							file.getAbsolutePath(), isPreview, isFrontCamera);
					// 发送广播，通知图库刷新
					AndroidUtils.sendScanFileBroadcast(CameraActivity.this,
							new File(path));
					hideLoadingIndicatorDialog();
					// mCameraButtonLocked = false;
					finish(path);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private String handleImageOrientation(String path, boolean isPreview,
			boolean isFrontCamera) {
		try {
			LogUtil.d(TAG, "handleImageOrientation: " + path);
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			LogUtil.i(TAG, "readExifOrientation: " + orientation);
			Bitmap source = null;
			Bitmap dest = null;
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				source = BitmapFactory.decodeFile(path);
				if (isFrontCamera)
					dest = BitmapUtils.rotateBitmapInCenter(source, -90f);
				else
					dest = BitmapUtils.rotateBitmapInCenter(source, 90f);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				source = BitmapFactory.decodeFile(path);
				if (isFrontCamera)
					dest = BitmapUtils.rotateBitmapInCenter(source, -180f);
				else
					dest = BitmapUtils.rotateBitmapInCenter(source, 180f);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				source = BitmapFactory.decodeFile(path);
				if (isFrontCamera)
					dest = BitmapUtils.rotateBitmapInCenter(source, -270f);
				else
					dest = BitmapUtils.rotateBitmapInCenter(source, 270f);
				break;
			default:
				int cameraRotation = mCameraPreview.getCameraRotation();
				if (!isPreview) {
					cameraRotation -= mCameraPreview.getDisplayOrientation();
				}
				LogUtil.v(TAG, "getCameraRotation: " + cameraRotation);
				if (cameraRotation != 0) {
					source = BitmapFactory.decodeFile(path);
					dest = BitmapUtils.rotateBitmapInCenter(source,
							cameraRotation);
				}
				break;
			}
			if (source != null) {
				source.recycle();
				source = null;
			}
			if (dest != null) {
				// 前置摄像头需要对图片做镜像处理
				if (isFrontCamera) {
					source = dest;
					dest = BitmapUtils.mirrorBitmap(source, false);
					source.recycle();
					source = null;
				}

				File destFile = newJpegFile(getApplicationContext());
				LogUtil.v(TAG, "destFile: " + destFile);
				boolean result = BitmapUtils.saveBitmapToFile(dest, destFile);
				dest.recycle();
				if (result) {
					if (!path.equalsIgnoreCase(destFile.getAbsolutePath())) {
						File file = new File(path);
						file.delete();
					}
					return destFile.getAbsolutePath();
				} else {
					if (!path.equalsIgnoreCase(destFile.getAbsolutePath())) {
						destFile.delete();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	public class MaskAdapterItem extends AbsAdapterItem {
		private String mPath;
		private Bitmap mBitmap;

		public MaskAdapterItem(String path) {
			mPath = path;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent) {
			ExtendImageView view = new ExtendImageView(getApplicationContext());
			LayoutParams params = view.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.MATCH_PARENT);
			} else {
				params.width = LayoutParams.WRAP_CONTENT;
				params.height = LayoutParams.MATCH_PARENT;
			}
			view.setLayoutParams(params);
			view.setBackgroundColor(Color.TRANSPARENT);
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			view.setId(id);
			ViewTools.autoFitViewDimension(view, parent, FitMode.FIT_IN_HEIGHT,
					1);
			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent) {
			// LogUtil.d(TAG, "updateView: " + position + "; " + view);
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ExtendImageView imageView = (ExtendImageView) view
					.findViewById(id);
			imageView.setScaleType(ScaleType.CENTER_CROP);
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent) {
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ExtendImageView imageView = (ExtendImageView) view
					.findViewById(id);
			if (mBitmap == null || mBitmap.isRecycled()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						mBitmap = getMaskeBitmap(TAG, getAssets(), mPath);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageView.setImageBitmap(mBitmap);
							}
						});
					}
				}).start();
			} else {
				imageView.setImageBitmap(mBitmap);
			}
		}

		@Override
		public void onRecycleViewResource(View view, int position,
				ViewGroup parent) {
			int id = ResourceUtil.getId(getApplicationContext(), "image");
			final ExtendImageView imageView = (ExtendImageView) view
					.findViewById(id);
			imageView.recyleBitmapImage();
		}

		@Override
		public void onItemClick(View adapterView, ViewGroup parent, View view,
				int position, long id) {
			setCurrentMask(position);
		}
	}

}
