package org.lasque.tubeautysetting;

import android.graphics.SurfaceTexture;

import com.tusdk.pulse.DispatchQueue;
import com.tusdk.pulse.Engine;
import com.tusdk.pulse.filter.FilterPipe;
import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.utils.gl.GLContext;
import com.tusdk.pulse.utils.gl.OutputSurface;

import org.lasque.tusdkpulse.core.utils.TLog;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/21  11:33
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class RenderPipe{
    /**
     * OpenGL上下文对象
     */
    private GLContext mGLCtx;

    /**
     * 任务队列
     */
    private DispatchQueue mRenderPool;

    private boolean isInit = false;

    public boolean initRenderPipe(){
        if (mRenderPool != null) return false;

        mRenderPool = new DispatchQueue();
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                TLog.e("Renderpipe initRenderPipe()");

                mGLCtx = new GLContext();
                mGLCtx.createForRender(Engine.getInstance().getMainGLContext().getEGLContext());
                mGLCtx.makeCurrent();
            }
        });
        return isInit;
    }

    /**
     * @return 获取任务队列
     */
    public DispatchQueue getRenderPool(){
        return mRenderPool;
    }

    /**
     * @return 获取OpenGL 上下文
     */
    public GLContext getContext(){
        return mGLCtx;
    }

    /**
     * 释放
     */
    public void release(){
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                mGLCtx.unMakeCurrent();
                mGLCtx.destroy();
            }
        });

//        mRenderPool = null;
    }
}
