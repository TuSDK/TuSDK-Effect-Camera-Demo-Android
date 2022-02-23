package org.lasque.tubeautysetting;

import android.content.Context;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import androidx.core.util.Pair;

import com.tusdk.pulse.Engine;
import com.tusdk.pulse.filter.FilterDisplayView;
import com.tusdk.pulse.filter.Image;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/23  11:11
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class PreviewManager {

    /**
     * DisplayView
     */
    private FilterDisplayView mDisplayView;

    /**
     * DisplayView父容器
     */
    private ViewGroup mDisplayParent;

    private boolean isReady = false;

    /**
     * @param context 上下文对象
     * @param parent 父控件
     * @return 初始化状态
     */
    public Pair<Boolean,Integer> requestInit(Context context, ViewGroup parent){
        if (isReady) return new Pair<>(false,-1);

        mDisplayView = new FilterDisplayView(context);
        mDisplayView.init(Engine.getInstance().getMainGLContext());

        mDisplayParent = parent;
        LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT );
        mDisplayParent.addView(mDisplayView,params);

        isReady = true;

        return new Pair<>(true,0);
    }

    public boolean isReady(){
        return isReady;
    }

    public void release(){
        mDisplayView.release();
        mDisplayParent.removeView(mDisplayView);
    }

    /**
     * 更新预览画面
     * @param image 纹理资源
     */
    public void updateImage(Image image){
        if (!isReady) return;
        mDisplayView.updateImage(image);
    }

    /**
     * 更新预览画面
     * @param image 纹理资源
     * @param rectF 渲染区域
     */
    public void updateImage(Image image, RectF rectF){
        if (!isReady) return;
        mDisplayView.updateImage(image,rectF);
    }

    /**
     * 设置背景颜色
     * @param color 画布背景颜色
     */
    public void setBackgroundColor(int color){
        if (!isReady) return;
        mDisplayView.setBackgroundColor(color);
    }

}
