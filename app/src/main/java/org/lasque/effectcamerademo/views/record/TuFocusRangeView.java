/** 
 * TuSDKCore
 * TuFocusRangeView.java
 *
 * @author 		Clear
 * @Date 		2014-11-21 下午2:05:43 
 * @Copyright 	(c) 2014 tusdk.com. All rights reserved.
 * 
 */
package org.lasque.effectcamerademo.views.record;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.core.view.ViewCompat;

import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.struct.ViewSize;
import org.lasque.tusdkpulse.core.utils.ColorUtils;
import org.lasque.tusdkpulse.core.utils.anim.AnimHelper;
import org.lasque.tusdkpulse.core.view.TuSdkRelativeLayout;

/**
 * 聚焦区域视图
 * 
 * @author Clear
 */
public class TuFocusRangeView extends TuSdkRelativeLayout implements TuFocusRangeViewInterface
{
	/** 选取范围比例 */
	public static final float FocusRangeScale = 0.6f;

	/** 布局ID */
	public static int getLayoutId()
	{
		return TuSdkContext.getLayoutResId("tusdk_impl_component_camera_focus_range_view");
	}

	public TuFocusRangeView(Context context)
	{
		super(context);
	}

	public TuFocusRangeView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public TuFocusRangeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/** 聚焦外框视图 */
	private View mFocusOutView;
	/** 聚焦准心视图 */
	private View mFocusCrosshair;
	/** 默认颜色 */
	private int mNormalColor;
	/** 聚焦成功颜色颜色 */
	private int mSucceedColor;
	/** 聚焦失败颜色 */
	private int mFailedColor;
	/** 最大选区长宽 */
	private TuSdkSize maxRangeSize;
	/** 最小选取长宽 */
	private TuSdkSize minRangeSize;
	/** 最小准心视图长宽 */
	private TuSdkSize minCrosshairSize;

	/** 最大选区长宽 */
	public TuSdkSize getMaxRangeSize()
	{
		if (maxRangeSize == null)
		{
			maxRangeSize = ViewSize.create(this);
			this.getMinCrosshairSize();
		}
		return maxRangeSize;
	}

	/** 最大选区长宽 */
	public void setMaxRangeSize(TuSdkSize maxRangeSize)
	{
		this.maxRangeSize = maxRangeSize;
	}

	/** 最小选区长宽 */
	public TuSdkSize getMinRangeSize()
	{
		if (minRangeSize == null)
		{
			TuSdkSize max = this.getMaxRangeSize();
			minRangeSize = new TuSdkSize((int) Math.floor(max.width * FocusRangeScale), (int) Math.floor(max.height * FocusRangeScale));
		}
		return minRangeSize;
	}

	/** 最小选区长宽 */
	public void setMinRangeSize(TuSdkSize minRangeSize)
	{
		this.minRangeSize = minRangeSize;
	}

	/** 最小准心视图长宽 */
	public TuSdkSize getMinCrosshairSize()
	{
		if (minCrosshairSize == null)
		{
			minCrosshairSize = ViewSize.create(this.getFocusCrosshair());
		}
		return minCrosshairSize;
	}

	/** 最小准心视图长宽 */
	public void setMinCrosshairSize(TuSdkSize minCrosshairSize)
	{
		this.minCrosshairSize = minCrosshairSize;
	}

	/** 初始化视图 */
	@Override
	protected void initView()
	{
		super.initView();

		// 默认颜色
		mNormalColor = TuSdkContext.getColor("lsq_focus_normal");
		// 聚焦成功颜色颜色
		mSucceedColor = TuSdkContext.getColor("lsq_focus_succeed");
		// 聚焦失败颜色
		mFailedColor = TuSdkContext.getColor("lsq_focus_failed");
	}

	/** 获取聚焦外框视图 */
	public View getFocusOutView()
	{
		if (mFocusOutView == null)
		{
			mFocusOutView = this.getViewById("lsq_range_wrap");
		}
		return mFocusOutView;
	}

	/** 获取聚焦准心视图 */
	public View getFocusCrosshair()
	{
		if (mFocusCrosshair == null)
		{
			mFocusCrosshair = this.getViewById("lsq_crosshair");
		}
		return mFocusCrosshair;
	}

	/** 获取默认颜色 */
	public int getNormalColor()
	{
		return mNormalColor;
	}

	/** 设置默认颜色 */
	public void setNormalColor(int color)
	{
		mNormalColor = color;
	}

	/** 获取聚焦成功颜色颜色 */
	public int getSucceedColor()
	{
		return mSucceedColor;
	}

	/** 设置聚焦成功颜色颜色 */
	public void setSucceedColor(int color)
	{
		mSucceedColor = color;
	}

	/** 获取聚焦失败颜色 */
	public int getFailedColor()
	{
		return mFailedColor;
	}

	/** 设置聚焦失败颜色 */
	public void setFailedColor(int color)
	{
		mFailedColor = color;
	}

	/** 设置显示颜色 */
	public void setDisplayColor(int color)
	{
		ColorUtils.setBackgroudImageColor(this.getFocusOutView(), color);
		ColorUtils.setBackgroudImageColor(this.getFocusCrosshair(), color);
	}

	/** 延迟执行处理器 */
	private Handler mHandler = new Handler();

	/** 延迟处理方法 */
	private Runnable mAutoHiddenRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			showViewIn(false);
		}
	};

	/** 设置聚焦状态 */
	public void setFoucsState(boolean success)
	{
		mHandler.postDelayed(mAutoHiddenRunnable, 500);
		this.setDisplayColor(success ? mSucceedColor : mFailedColor);
	}

	/** 设置显示位置 */
	public void setPosition(PointF lastPoint)
	{
		if (lastPoint == null) return;
		this.initNormalState(lastPoint);

		CameraFocusAnimation animation = new CameraFocusAnimation();
		animation.setDuration(200);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		this.startAnimation(animation);
	}

	/** 初始化为默认状态 */
	private void initNormalState(PointF lastPoint)
	{
		mHandler.removeCallbacks(mAutoHiddenRunnable);
		AnimHelper.clear(this);
		ViewCompat.setAlpha(this.getFocusCrosshair(), 0);
		this.setDisplayColor(mNormalColor);
		this.showViewIn(true);

		this.setCenter(lastPoint);
	}

	/** 设置中心位置 */
	private void setCenter(PointF point)
	{
		TuSdkSize rangeSize = this.getMaxRangeSize();
		TuSdkSize parentSize = ViewSize.create((View) this.getParent());

		float left = point.x - (rangeSize.width * 0.5f);
		float top = point.y - (rangeSize.height * 0.5f);

		if (left < 0)
		{
			left = 0;
		}
		else if ((left + rangeSize.width) > parentSize.width)
		{
			left = parentSize.width - rangeSize.width;
		}

		if (top < 0)
		{
			top = 0;
		}
		else if ((top + rangeSize.height) > parentSize.height)
		{
			top = parentSize.height - rangeSize.height;
		}

		this.setMargin((int) Math.floor(left), (int) Math.floor(top), 0, 0);

		this.setSize(this.getFocusOutView(), rangeSize);
		this.setSize(this.getFocusCrosshair(), rangeSize);
	}

	/** 聚焦视图动画 */
	private class CameraFocusAnimation extends Animation
	{
		// 选区减小范围
		private TuSdkSize mRSize;
		// 准心减小范围
		private TuSdkSize mCSize;

		public CameraFocusAnimation()
		{
			TuSdkSize rangeSize = getMaxRangeSize();
			TuSdkSize minRangeSize = getMinRangeSize();
			TuSdkSize minCrosshairSize = getMinCrosshairSize();

			mRSize = new TuSdkSize();
			mRSize.width = rangeSize.width - minRangeSize.width;
			mRSize.height = rangeSize.height - minRangeSize.height;

			mCSize = new TuSdkSize();
			mCSize.width = rangeSize.width - minCrosshairSize.width;
			mCSize.height = rangeSize.height - minCrosshairSize.height;
		}

		@Override
		public boolean willChangeBounds()
		{
			return true;
		}

		/* 执行动画 interpolatedTime =》 0-1
		 * @see android.view.animation.Animation#applyTransformation(float, android.view.animation.Transformation)
		 */
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t)
		{
			ViewCompat.setAlpha(getFocusCrosshair(), interpolatedTime);

			TuSdkSize rangeSize = getMaxRangeSize();

			TuSdkSize ovSize = new TuSdkSize();
			ovSize.width = (int) (rangeSize.width - interpolatedTime * mRSize.width);
			ovSize.height = (int) (rangeSize.height - interpolatedTime * mRSize.height);

			TuSdkSize csSize = new TuSdkSize();
			csSize.width = (int) (rangeSize.width - interpolatedTime * mCSize.width);
			csSize.height = (int) (rangeSize.height - interpolatedTime * mCSize.height);

			setSize(getFocusOutView(), ovSize);
			setSize(getFocusCrosshair(), csSize);
		}
	}
}