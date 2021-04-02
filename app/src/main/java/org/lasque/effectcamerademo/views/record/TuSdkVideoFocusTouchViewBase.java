/**
 * TuSDKVideo
 * TuVideoFocusTouchViewBase.java
 *
 * @author		Yanlin
 * @Date		3:07:11 PM
 * @Copright	(c) 2015 tusdk.com. All rights reserved.
 *
 */
package org.lasque.effectcamerademo.views.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.lasque.tusdkpulse.core.TuSdkBundle;
import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.struct.ViewSize;
import org.lasque.tusdkpulse.core.utils.RectHelper;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.core.view.TuSdkRelativeLayout;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCamera;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCameraFocus;
import org.lasque.tusdkpulse.impl.view.widget.RegionHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Focus Touch View 基础类
 *
 * @author Clear
 */
public abstract class TuSdkVideoFocusTouchViewBase extends TuSdkRelativeLayout implements TuSDKVideoCameraFocusViewInterface
{
	/** 采样频率 (单位毫秒) */
	public static final long SamplingDistance = 2000;
	/** 采样范围 */
	public static final float SamplingRange = 50.f;
	/** 脸部定位时间间隔 (单位毫秒) */
	public static final long FaceDetectionDistance = 5000;

	public TuSdkVideoFocusTouchViewBase(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public TuSdkVideoFocusTouchViewBase(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public TuSdkVideoFocusTouchViewBase(Context context)
	{
		super(context);
	}

	/** 最后脸部定位时间 */
	private long mFaceDetectionLastTime = 0;

	/** 相机对象 */
	private TuCamera mCamera;
	/** 最后点击坐标 */
	private PointF mLastPoint;
	/** 聚焦声音播放 */
	private MediaPlayer mFocusPlayer;
	/** 触摸是否结束 */
	private boolean mIsTouchCanceled;
	/** 禁用聚焦声音 */
	private boolean mDisableFocusBeep;
	/** 禁用持续自动对焦 */
	private boolean mDisableContinueFoucs;
	/** 显示区域百分比 */
	private RectF mRegionPercent;
	/** 是否开启脸部特征检测 (智能美颜 | 动态贴纸 都需要开启该选项) */
	private boolean mEnableFaceFeatureDetection;

	private RegionHandler mRegionHandler;

	@Override
	public void setRegionHandler(RegionHandler handler) {
		mRegionHandler = handler;
	}

	/** 设置选区焦点视图状态 */
	@Override
	public abstract void setRangeViewFoucsState(boolean success);

	/** 相机对象 */
	public TuCamera getCamera()
	{
		return mCamera;
	}

	/** 相机对象 */
	public void setCamera(TuCamera camera)
	{
		this.mCamera = camera;
	}

	/** 禁用聚焦声音 (默认：false) */
	public boolean isDisableFocusBeep()
	{
		return mDisableFocusBeep;
	}

	/** 禁用聚焦声音 (默认：false) */
	@Override
	public void setDisableFocusBeep(boolean disableFocusBeep)
	{
		this.mDisableFocusBeep = disableFocusBeep;
	}

	/** 禁用持续自动对焦 (默认：false) */
	public boolean isDisableContinueFoucs()
	{
		return mDisableContinueFoucs;
	}

	/** 禁用持续自动对焦 (默认：false) */
	@Override
	public void setDisableContinueFoucs(boolean disableContinueFoucs)
	{
		this.mDisableContinueFoucs = disableContinueFoucs;
	}

	/** 是否开启脸部特征检测 (智能美颜 | 动态贴纸 都需要开启该选项) */
	public boolean isEnableFaceFeatureDetection()
	{
		return mEnableFaceFeatureDetection;
	}

	/** 是否开启脸部特征检测 (智能美颜 | 动态贴纸 都需要开启该选项) */
	public void setEnableFaceFeatureDetection(boolean enableFaceFeatureDetection)
	{
		this.mEnableFaceFeatureDetection = enableFaceFeatureDetection;
	}

	/** 聚焦声音播放 */
	private MediaPlayer getFocusPlayer()
	{
		if (this.isDisableFocusBeep()) return null;

		if (mFocusPlayer == null)
		{
			String source = TuSdkBundle.sdkBundleOther(TuSdkBundle.CAMERA_FOCUS_BEEP_AUDIO_RAW);
			mFocusPlayer = TuSdkContext.loadMediaAsset(source);
		}
		return mFocusPlayer;
	}

	/** 最后点击坐标 */
	protected PointF getLastPoint()
	{
		if (mLastPoint == null)
		{
			mLastPoint = new PointF(this.getWidth() * 0.5f, this.getHeight() * 0.5f);
		}
		return mLastPoint;
	}

	/** 最后点击坐标 */
	private void setLastPoint(PointF lastPoint)
	{
		this.mLastPoint = lastPoint;
	}

	@Override
	/** 显示区域百分比 */
	public void setRegionPercent(RectF regionPercent)
	{
		mRegionPercent = regionPercent;
	}

	/** 显示区域百分比 */
	public RectF getRegionPercent()
	{
		if (mRegionPercent == null)
		{
			mRegionPercent = new RectF(0, 0, 1, 1);
		}
		return mRegionPercent;
	}

	@Override
	public void viewWillDestory()
	{
		super.viewWillDestory();
		this.mCamera = null;
		if (mFocusPlayer != null)
		{
			mFocusPlayer.release();
			mFocusPlayer = null;
		}
	}


	/**
	 * 通知聚焦
	 *
	 * @param lastPoint
	 *            最后的聚焦点
	 * @param capture
	 *            是否拍摄
	 * @return 是否允许聚焦
	 */
	protected boolean notifyFoucs(PointF lastPoint, final boolean capture)
	{
//		|| mCamera.getState() != CameraState.StateStarted
		if (mCamera == null || !mCamera.cameraFocus().canSupportAutoFocus()  || !this.isInRegion(lastPoint)) return false;

		mCamera.cameraFocus().setFocus(this.getRatioPoint(lastPoint), new TuCameraFocus.TuCameraFocusListener() {
			@Override
			public void onFocusStart() {
				setFoucsState(true);
			}

			@Override
			public void onFocusEnd(boolean success) {
				setFoucsState(false);
			}
		});

		return true;
	}

	/** 设置聚焦状态 */
	private void setFoucsState(boolean success)
	{
		this.setRangeViewFoucsState(success);
		this.startFocusPlayer();

	}

	/** Get Region RectF */
	protected RectF getRegionRectF()
	{
		TuSdkSize size = ViewSize.create(this);

		float sl = this.getRegionPercent().left * size.width;
		float st = this.getRegionPercent().top * size.height;
		float sr = this.getRegionPercent().right * size.width;
		float sb = this.getRegionPercent().bottom * size.height;
		RectF selfRect = new RectF(sl, st, sr, sb);

		return selfRect;
	}

	/** 是否在选区范围内 */
	private boolean isInRegion(PointF point)
	{
		return this.getRegionRectF().contains(point.x, point.y);
	}

	/** 是否显示对焦 */
	private boolean mIsShowFocus = true;
	/** 手势反馈 */
	protected GestureListener listener;
	/** 是否点击结束 */
	private boolean mIsDown;
	/** 点击x */
	private float mPosX, mCurPosX;
	/** 移动最小距离 */
	private static final int FLING_MIN_DISTANCE = 50;

	public void isShowFoucusView(boolean isShowFocus){
		mIsShowFocus = isShowFocus;
	}


	public void setGestureListener(GestureListener listener) {
		this.listener = listener;
	}

	/** 手势接口 */
	public interface GestureListener{
		void onLeftGesture();
		void onRightGesture();
		void onClick();
	}

	/***************************** Touch ***********************************

	 /** 触摸事件 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// 仅允许单点触摸
		if (event.getPointerCount() > 1) return super.onTouchEvent(event);
		mFaceDetectionLastTime = Calendar.getInstance().getTimeInMillis();
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				this.onTouchDown(event);
				break;
			case MotionEvent.ACTION_UP:
				this.onTouchUp(event);
				break;
			case MotionEvent.ACTION_MOVE:
				this.onTouchMove(event);
				break;
			default:
				this.mIsTouchCanceled = true;
				this.mIsDown = false;
				break;
		}
		return true;
	}

	/** 重置最后事件 */
	private void restLastEvent(MotionEvent event)
	{
		PointF point = this.getLastPoint();
		point.x = event.getX();
		point.y = event.getY();
	}

	/** 视图按下事件 */
	private void onTouchDown(MotionEvent event)
	{
		this.mIsTouchCanceled = false;
		this.mIsDown = true;
		mCurPosX = mPosX = event.getX();
		this.restLastEvent(event);
	}

	/** 触摸释放 */
	private void onTouchUp(MotionEvent event)
	{
		this.mIsDown = false;

		if (this.mIsTouchCanceled) return;

		this.mIsTouchCanceled = true;
		this.restLastEvent(event);


		if(mIsShowFocus && (Math.abs(mCurPosX - mPosX) < FLING_MIN_DISTANCE))
		{
			if (canShowFocusView(this.getLastPoint())){
				this.showFocusView(this.getLastPoint());
				this.notifyFoucs(this.getLastPoint(), false);
			}
		}
		if(listener != null && (Math.abs(mCurPosX - mPosX) < FLING_MIN_DISTANCE))
			listener.onClick();


	}

	public abstract void showFocusView(PointF lastPoint);

	/** 移动 */
	private void onTouchMove(MotionEvent event)
	{
		mCurPosX = event.getX();
		// 滑动效果处理
		if (mCurPosX - mPosX > 0
				&& (Math.abs(mCurPosX - mPosX) > FLING_MIN_DISTANCE) && mIsDown) {
			mIsDown = false;
			//向右滑动
			if(listener != null) listener.onRightGesture();
		} else if (mCurPosX - mPosX < 0
				&& (Math.abs(mCurPosX - mPosX) > FLING_MIN_DISTANCE) && mIsDown) {
			mIsDown = false;
			//向左滑动
			if(listener != null) listener.onLeftGesture();
		}

		if (this.mIsTouchCanceled) return;

		this.restLastEvent(event);
	}

	/** 获取坐标比例
	 * @param lastPoint
	 * @return*/
	protected final PointF getRatioPoint(PointF lastPoint)
	{
		if (lastPoint == null) return null;

		PointF point = new PointF();
		float width = this.getWidth();
		float height = this.getHeight();
		if (this.mCamera.getFacing() == CameraConfigs.CameraFacing.Front)
		{
			point.x = 1.0f - Math.min(1.0f,lastPoint.x / width);
			point.y = 1.0f - Math.min(1.0f,lastPoint.y / height);
		}
		else
		{
			point.x = Math.max(0.0f,lastPoint.x / width);
			point.y = Math.max(0.0f,lastPoint.y / height);
		}
		/**
		 *  2020.03.10
		 *  修复预览比例变更后,测光点,对焦区位置错误的问题
		 * */
//		if (mRegionHandler.getRatio() > 0){
//			point.y -= (mRegionHandler.getCenterRectPercent().bottom - getRegionPercent().bottom);
//		}


		return point;
	}

	protected boolean canShowFocusView(PointF lastPoint){
		RectF regionRect = new RectF();
		RectF regionPercentRect = getRegionPercent();
		float width = this.getWidth();
		float height = this.getHeight();
		regionRect.left = width * regionPercentRect.left;
		regionRect.right = width * regionPercentRect.right;
		regionRect.top = height * regionPercentRect.top;
		regionRect.bottom = height * regionPercentRect.bottom;
		return regionRect.contains(lastPoint.x,lastPoint.y);

	}

	/** 播放聚焦声音 */
	private void startFocusPlayer()
	{
		MediaPlayer player = this.getFocusPlayer();
		if (player == null) return;
		player.start();
	}

	/** 相机状态改变 */
	public void cameraStateChanged(boolean isCanSupportAutoFocus,TuCamera camera, CameraConfigs.CameraState state)
	{
		if (state == CameraConfigs.CameraState.START_PREVIEW) this.setLastPoint(null);
		else this.hiddenFaceViews();

		if (camera == null || !isCanSupportAutoFocus) return;

	}

	public void cameraStateChanged(TuCamera camera, CameraConfigs.CameraState status)
	{
		if (status == CameraConfigs.CameraState.START) this.setLastPoint(null);
		else this.hiddenFaceViews();

		if (camera == null || !camera.cameraFocus().canSupportAutoFocus()) return;
	}

	/************************* Face Detection ******************************/
	/** Face Views */
	protected final List<View> mFaceViews = new ArrayList<View>();

	/** hidden Face Views */
	protected void hiddenFaceViews()
	{
		for (View view : mFaceViews)
			this.showView(view, false);
	}

	/** 创建脸部定位视图 */
	protected abstract View buildFaceDetectionView();

	/** 按照宽高计算相对于图片的范围 */
	protected final RectF makeRectRelativeImage(TuSdkSize size)
	{
		if (size == null || !size.isSize()) return null;

		RectF region = this.getRegionRectF();
		RectF rect = RectHelper.makeRectWithAspectRatioOutsideRect(size, region);

		return rect;
	}

	/** transfor Rect */
	private Rect transforRect(RectF rect, RectF cRect)
	{
		if (rect == null || cRect == null) return null;

		Rect nRect = new Rect();
		nRect.left = (int) (rect.left * cRect.width() - cRect.left);
		nRect.right = (int) (rect.right * cRect.width() - cRect.left);
		nRect.top = (int) (rect.top * cRect.height() - cRect.top);
		nRect.bottom = (int) (rect.bottom * cRect.height() - cRect.top);

		return nRect;
	}

	/** face Detection Auto focus */
	private void faceDetectionAutofocus(Rect rect)
	{
		long time = Calendar.getInstance().getTimeInMillis();
		if (rect == null || time - mFaceDetectionLastTime < FaceDetectionDistance) return;
		mFaceDetectionLastTime = time;
		mLastPoint = new PointF(rect.centerX(), rect.centerY());

		if (mCamera == null) return;

		// 自动聚焦
		if (mCamera.cameraFocus().canSupportAutoFocus()) this.notifyFoucs(this.getLastPoint(), false);
		// 自动测光
//		else mCamera.autoMetering(this.getLastPoint());
	}

	/******************************* Light Sensor **********************************/

	/***
	 * 设置自动持续对焦
	 *
	 * @param enable
	 *            是否开始聚焦
	 *
	 * Video 3.5.5 废弃 预计之后删除
	 * 内部实现已删除
	 */
	@Deprecated
	protected void setAutoContinueFocus(boolean enable)
	{

	}
}