/**
 * TuSDKVideo
 * TuVideoFocusTouchView.java
 *
 * @author		Yanlin
 * @Date		3:43:00 PM
 * @Copright	(c) 2015 tusdk.com. All rights reserved.
 *
 */
package org.lasque.effectcamerademo.views.record;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCamera;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频交互辅助类
 *
 * @author Yanlin
 *
 */
public class TuSdkVideoFocusTouchView extends TuSdkVideoFocusTouchViewBase
{
	/** 对焦视图 */
	private TuFocusRangeView focusRangeView;
	private final List<View> markViews = new ArrayList<View>();

	public TuSdkVideoFocusTouchView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public TuSdkVideoFocusTouchView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public TuSdkVideoFocusTouchView(Context context)
	{
		super(context);

		// 聚焦区域视图
		this.showViewIn(this.getFocusRangeView(), false);
	}

	/** 脸部定位视图布局ID */
	private int mFaceDetectionLayoutID;

	/** 脸部定位视图布局ID */
	public int getFaceDetectionLayoutID()
	{
		if (mFaceDetectionLayoutID < 1)
		{
			mFaceDetectionLayoutID = TuSdkContext.getLayoutResId("tusdk_impl_component_camera_face_detection_view");
		}
		return mFaceDetectionLayoutID;
	}

	/** 创建脸部定位视图 */
	@Override
	public View buildFaceDetectionView()
	{
		return null;
		/*
		View view = this.buildView(this.getFaceDetectionLayoutID(), this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(params);
		return view;
		*/
	}

	/*创建标点视图*/
	private void buildMarkViews(int index, PointF point, RectF rect, PointF offset)
	{

		View mark = null;
		RelativeLayout.LayoutParams params = null;
		if (markViews.size() > index)
		{
			mark = markViews.get(index);
			params = (RelativeLayout.LayoutParams) mark.getLayoutParams();
			this.showView(mark, true);
		}
		else
		{
			mark = new View(this.getContext());
			mark.setBackgroundColor(Color.argb(255, 0, 255, 0));
			params = new RelativeLayout.LayoutParams(8, 8);
			markViews.add(mark);
			this.addView(mark);
		}

		params.leftMargin = (int) ((point.x + offset.x) * rect.width() - rect.left) - 4;
		params.topMargin = (int) ((point.y + offset.y) * rect.height() - rect.top) - 4;
		mark.setLayoutParams(params);
	}

	@Override
	public void setCamera(TuCamera tuSdkStillCamera) {
		super.setCamera(tuSdkStillCamera);
	}

	@Override
	public void setGuideLineViewState(boolean mDisplayGuideLine) 
	{
		
	}

	@Override
	public void setEnableFilterConfig(boolean enableFilterConfig)
	{
		
	}

	@Override
	public void showRangeView() 
	{

	}

	/** 设置选区焦点视图状态 */
	@Override
	public void setRangeViewFoucsState(boolean success) 
	{
		if (this.getFocusRangeView() != null) this.getFocusRangeView().setFoucsState(true);
	}

	@Override
	public void setGestureListener(GestureListener listener){
		this.listener = listener;
	}


	/**
	 * 通知聚焦
	 *
	 * @param lastPoint
	 *            最后的聚焦点
	 * @return 是否允许聚焦
	 */
	@Override
	public void showFocusView(PointF lastPoint)
	{
		//|| getCamera().getState() != TuSdkStillCameraAdapter.CameraState.StateStarted
		if (getCamera() == null || !getCamera().cameraFocus().canSupportAutoFocus() ) return;

		if (this.getFocusRangeView() != null)
		{
			this.getFocusRangeView().setPosition(lastPoint);
		}
	}

	@Override
	public void cameraStateChanged(TuCamera camera, CameraConfigs.CameraState status) {
		super.cameraStateChanged(camera, status);

		if (status == CameraConfigs.CameraState.START && this.getFocusRangeView() != null)
		{
			// 防止准心因为意外关闭相机无法关闭
			this.showViewIn(this.getFocusRangeView(), false);
		}
	}

	@Override
	public void cameraStateChanged(boolean isCanSupportAutoFocus, TuCamera camera, CameraConfigs.CameraState status) {
		super.cameraStateChanged(isCanSupportAutoFocus, camera, status);
	}

	/**
	 * 获取聚焦视图
	 * @return
	 */
	private TuFocusRangeView getFocusRangeView(){
		if(focusRangeView == null) {
			int focusRangeViewId = TuSdkContext.getLayoutResId("tusdk_impl_component_camera_focus_range_view");

			if(focusRangeViewId == 0){
				TLog.e("video not find tusdk_impl_component_camera_focus_range_view layout");
				return null;
			}

			focusRangeView = (TuFocusRangeView) LayoutInflater.from(getContext()).inflate(focusRangeViewId,null);
			this.addView(focusRangeView,TuSdkContext.dip2px(90),TuSdkContext.dip2px(90));
		}
		return focusRangeView;
	}
}
