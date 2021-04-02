/** 
 * TuSDKCore
 * TuFocusRangeViewInterface.java
 *
 * @author 		Clear
 * @Date 		2015-8-30 下午5:38:23 
 * @Copyright 	(c) 2015 tusdk.com. All rights reserved.
 * 
 */
package org.lasque.effectcamerademo.views.record;

import android.graphics.PointF;

/**
 * 聚焦区域视图接口
 * 
 * @author Clear
 */
public interface TuFocusRangeViewInterface
{
	/**
	 * 设置显示位置
	 * 
	 * @param lastPoint
	 */
	public void setPosition(PointF lastPoint);

	/**
	 * 设置聚焦状态
	 * 
	 * @param success
	 */
	public void setFoucsState(boolean success);
}
