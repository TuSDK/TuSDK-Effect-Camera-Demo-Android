/**
 * TuSDKVideo
 * TuSdkVideoCameraFocusViewInterface.java
 *
 * @author		Yanlin
 * @Date		4:03:55 PM
 * @Copright	(c) 2015 tusdk.com. All rights reserved.
 *
 */
package org.lasque.effectcamerademo.views.record;

import android.graphics.RectF;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCamera;
import org.lasque.tusdkpulse.impl.view.widget.RegionHandler;

import java.util.List;

/**
 * 视频扩展视图接口
 *
 * @author Yanlin
 *
 */
public interface TuSDKVideoCameraFocusViewInterface 
{

	/** 视图即将销毁 */
	void viewWillDestory();

	/** 设置相机对象 */
	void setCamera(TuCamera tuSdkStillCamera);

	/** 禁用聚焦声音 (默认：false) */
	void setDisableFocusBeep(boolean disableFocusBeep);

	/** 禁用持续自动对焦 (默认：false) */
	void setDisableContinueFoucs(boolean disableContinueFoucs);

	/** 显示区域百分比 */
	void setRegionPercent(RectF regionPercent);

	/** 设置辅助线显示状态 */
	void setGuideLineViewState(boolean mDisplayGuideLine);

	/** 开启滤镜配置选项 */
	void setEnableFilterConfig(boolean enableFilterConfig);

	/** 相机运行状态改变 */
	void cameraStateChanged(TuCamera camera, CameraConfigs.CameraState status);

	void cameraStateChanged(boolean isCanSupportAutoFocus, TuCamera camera, CameraConfigs.CameraState status);

	/** 显示选区焦点视图 */
	void showRangeView();

	/** 设置选区焦点视图状态 */
	void setRangeViewFoucsState(boolean success);

	/** 设置手势监听 */
	void setGestureListener(TuSdkVideoFocusTouchViewBase.GestureListener listener);

	void setRegionHandler(RegionHandler handler);
}
