package com.shiyou.tryapp2;

import android.app.Activity;
import android.content.Context;
import android.extend.ErrorInfo;
import android.extend.loader.BaseParser.DataFrom;
import android.extend.loader.BitmapLoader.DecodeMode;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;
import android.extend.util.ResourceUtil;
import android.extend.util.ViewTools;
import android.extend.widget.ExtendImageView;
import android.extend.widget.adapter.AbsAdapterItem;
import android.extend.widget.adapter.BasePagerAdapter;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.shiyou.tryapp2.RequestManager.RequestCallback;
import com.shiyou.tryapp2.data.ImageInfo;
import com.shiyou.tryapp2.data.response.BaseResponse;
import com.shiyou.tryapp2.data.response.ScreenSaversResponse;
import com.shiyou.tryapp2.shop.zsa.R;

public class ScreenSaversHelper
{
	public static final String TAG = ScreenSaversHelper.class.getSimpleName();

	private static final long AUTOSHOW_DElAY = 5 * 60 * 1000L;
	private static final long NEXTPAGE_DELAY = 30 * 1000L;
	private static Activity mActivity;
	private static ImageInfo[] mImageInfos;
	private static PopupWindow mPopupWindow;
	private static View mContentView;
	private static ViewGroup mDotContainer;
	private static ViewPager mViewPager;
	private static BasePagerAdapter<AbsAdapterItem> mPagerAdapter;

	public static void init(Activity activity)
	{
		mActivity = activity;
		loadScreenSavers();
	}

	private static void postLoadScreenSavers()
	{
		AndroidUtils.MainHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				loadScreenSavers();
			}
		}, 30 * 1000L);
	}

	private static void loadScreenSavers()
	{
		RequestManager.loadScreenSavers(mActivity, new RequestCallback()
		{
			@Override
			public void onRequestResult(int requestCode, long taskId, BaseResponse response, DataFrom from)
			{
				if (response.resultCode == BaseResponse.RESULT_OK)
				{
					ScreenSaversResponse ssResponse = (ScreenSaversResponse)response;
					mImageInfos = ssResponse.datas.list;
					updateAutoShowMessage();
				}
				else
					postLoadScreenSavers();
			}

			@Override
			public void onRequestError(int requestCode, long taskId, ErrorInfo error)
			{
				postLoadScreenSavers();
			}
		});
	}

	private static void ensureContentView()
	{
		// if (mScreenSaversView == null)
		// {
		int layout = ResourceUtil.getLayoutId(mActivity, "screensavers_layout");
		mContentView = View.inflate(mActivity, layout, null);

		int id = ResourceUtil.getId(mActivity, "dot_container");
		mDotContainer = (ViewGroup)mContentView.findViewById(id);
		ensureDots(mImageInfos.length);

		id = ResourceUtil.getId(mActivity, "viewpager");
		mViewPager = (ViewPager)mContentView.findViewById(id);
		mPagerAdapter = new BasePagerAdapter<AbsAdapterItem>();
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.addOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				setSelectdDot(position);
				updateNextPageMessage();
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				// updateNextPageRunnable();
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}
		});
		mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				int width = mViewPager.getWidth();
				int height = mViewPager.getHeight();
				if (width == 0 || height == 0)
					return;
				LogUtil.v(TAG, "mViewPager size: " + width + "x" + height);
				mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				LayoutParams params = mViewPager.getLayoutParams();
				params.width = width;
				params.height = height;
				mViewPager.setLayoutParams(params);
			}
		});

		for (ImageInfo imageInfo : mImageInfos)
		{
			mPagerAdapter.addItem(new PagerAdapterItem(imageInfo));
		}
		mViewPager.setCurrentItem(0);
		// }
	}

	// public static void refreshViewPager(ImageInfo[] imageInfos)
	// {
	// if (imageInfos == null || imageInfos.length == 0)
	// return;
	// mImageInfos = imageInfos;
	// ensureDots(mImageInfos.length);
	// ensureViewPagerItems();
	// }

	private static void ensureDots(int length)
	{
		mDotContainer.removeAllViews();
		for (int i = 0; i < length; i++)
		{
			ImageView view = new ImageView(mActivity);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.topMargin = AndroidUtils.dp2px(mActivity, 2);
			params.bottomMargin = AndroidUtils.dp2px(mActivity, 2);
			params.leftMargin = AndroidUtils.dp2px(mActivity, 20);
			params.rightMargin = AndroidUtils.dp2px(mActivity, 20);
			view.setLayoutParams(params);
			int dotUnfocusId = ResourceUtil.getDrawableId(mActivity, "dot_container_bg1");
			view.setImageResource(dotUnfocusId);
			view.setScaleType(ScaleType.CENTER);
			mDotContainer.addView(view);
		}
		setSelectdDot(0);
	}

	private static void setSelectdDot(final int index)
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.d(TAG, "setSelectdDot: " + index);
				int dotFocusId = ResourceUtil.getDrawableId(mActivity, "dot_container_bg");
				int dotUnfocusId = ResourceUtil.getDrawableId(mActivity, "dot_container_bg1");
				int count = mDotContainer.getChildCount();
				for (int i = 0; i < count; i++)
				{
					ImageView child = (ImageView)mDotContainer.getChildAt(i);
					if (i == index)
					{
						child.setImageResource(dotFocusId);
					}
					else
					{
						child.setImageResource(dotUnfocusId);
					}
				}
			}
		});
	}

	private static class NextPageMessage implements Runnable
	{
		@Override
		public void run()
		{
			if (mViewPager != null)
			{
				int index = mViewPager.getCurrentItem() + 1;
				if (index >= mPagerAdapter.getCount())
					index = 0;
				mViewPager.setCurrentItem(index, true);
				updateNextPageMessage();
			}
		}
	}

	private static NextPageMessage mNextPageMessage;

	private static void removeNextPageMessage()
	{
		if (mNextPageMessage != null)
		{
			AndroidUtils.MainHandler.removeCallbacks(mNextPageMessage);
			mNextPageMessage = null;
		}
	}

	private static void updateNextPageMessage()
	{
		removeNextPageMessage();
		mNextPageMessage = new NextPageMessage();
		AndroidUtils.MainHandler.postDelayed(mNextPageMessage, NEXTPAGE_DELAY);
	}

	public static void show()
	{
		AndroidUtils.MainHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				if (mImageInfos == null || mImageInfos.length == 0)
					return;
				ensureContentView();
				mPopupWindow = new PopupWindow(mContentView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				mPopupWindow.setBackgroundDrawable(new ColorDrawable());
				mPopupWindow.setFocusable(true);
				mPopupWindow.setTouchable(true);
				mPopupWindow.setOutsideTouchable(true);
				mPopupWindow.setClippingEnabled(true);
				mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
				mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
				mPopupWindow.showAtLocation(ViewTools.getActivityDecorView(mActivity), Gravity.CENTER, 0, 0);
				Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.grow_in_center);
				mContentView.startAnimation(anim);

				removeAutoShowMessage();
				updateNextPageMessage();
			}
		});
	}

	public static void hide()
	{
		if (mPopupWindow != null)
		{
			Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.shrink_out_center);
			anim.setAnimationListener(new AnimationListener()
			{
				@Override
				public void onAnimationStart(Animation animation)
				{
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					removeNextPageMessage();
					mPopupWindow.dismiss();
					mPopupWindow = null;
					updateAutoShowMessage();
				}
			});
			mContentView.startAnimation(anim);
		}
	}

	private static class AutoShowMessage implements Runnable
	{
		public void run()
		{
			show();
		}
	};

	private static AutoShowMessage mAutoShowMessage;

	private static void removeAutoShowMessage()
	{
		if (mAutoShowMessage != null)
		{
			AndroidUtils.MainHandler.removeCallbacks(mAutoShowMessage);
			mAutoShowMessage = null;
		}
	}

	public static void updateAutoShowMessage()
	{
		removeAutoShowMessage();
		mAutoShowMessage = new AutoShowMessage();
		AndroidUtils.MainHandler.postDelayed(mAutoShowMessage, AUTOSHOW_DElAY);
	}

	private static class PagerAdapterItem extends AbsAdapterItem
	{
		private ImageInfo mImageInfo;
		private float x, y;

		public PagerAdapterItem(ImageInfo imageInfo)
		{
			mImageInfo = imageInfo;
		}

		@Override
		public View onCreateView(int position, ViewGroup parent)
		{
			Context context = parent.getContext();
			ExtendImageView view = new ExtendImageView(context);
			view.setBackgroundColor(context.getResources().getColor(android.R.color.black));
			view.setLayoutParams(new ViewPager.LayoutParams());
			view.setScaleType(ScaleType.FIT_CENTER);
			// view.setGestureMode(GestureMode.VIEWPAGER_TAOBAO);
			// view.setAutoRecyleOnDetachedFromWindow(true);
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
				}
			});
			view.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					// LogUtil.v(TAG, "onTouch: " + event);
					if (event.getPointerCount() == 1)
					{
						switch (event.getActionMasked())
						{
							case MotionEvent.ACTION_DOWN:
								x = event.getX();
								y = event.getY();
								break;
							case MotionEvent.ACTION_UP:
								if (Math.abs(x - event.getX()) < 6 && Math.abs(y - event.getY()) < 6)
								{
									hide();
									v.setOnTouchListener(null);
									return true;
								}
								break;
						}
					}
					return false;
				}
			});
			return view;
		}

		@Override
		public void onUpdateView(View view, int position, ViewGroup parent)
		{
		}

		@Override
		public void onLoadViewResource(View view, int position, ViewGroup parent)
		{
			ExtendImageView imageView = (ExtendImageView)view;
			if (mImageInfo != null)
				imageView.setImageDataSource(mImageInfo.url, mImageInfo.filemtime, DecodeMode.FIT_WIDTH);
			imageView.setScaleType(ScaleType.FIT_CENTER);
			imageView.startImageLoad();
		}

		@Override
		public void onRecycleViewResource(View view, int position, ViewGroup parent)
		{
			ExtendImageView imageView = (ExtendImageView)view;
			imageView.recyleBitmapImage();
		}
	}
}
