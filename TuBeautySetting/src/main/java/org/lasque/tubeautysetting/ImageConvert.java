package org.lasque.tubeautysetting;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import androidx.core.util.Pair;

import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.utils.gl.OutputSurface;

import org.lasque.tubeautysetting.RenderPipe;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.utils.image.ImageOrientation;

/**
 * TuSDK
 * org.lasque.tubeautysetting.process
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/21  11:33
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class ImageConvert {

    /**
     * 获取处理画面必须的外部属性
     */
    public static interface ProcessProperty{

        /**
         * @return 当前设备角度
         */
        public double getAngle();

        /**
         * @return 是否开启人脸小模型检测 如需使用微整形高级功能与美妆 则返回true 否则返回false
         */
        public boolean getEnableMarkSence();

    }

    /**
     * 输入纹理尺寸
     */
    private TuSdkSize mInputSize;

    /**
     * RenderPipe
     */
    private RenderPipe mRenderPipe;

    /**
     * 渲染宽度,默认值为720
     */
    private int mRenderWidth = 720;

    /**
     * 实际渲染尺寸
     */
    private TuSdkSize mRealRenderSize;

    /**
     * 渲染比例
     */
    private Pair<Double,Double> mAspect;

    /**
     * 属性获取接口
     */
    private ProcessProperty mProcessProperty;

    /**
     * SurfaceTexture 封装对象 持有OES纹理
     */
    private OutputSurface mSurface;

    private boolean isFirstInitTexture = true;

    /**
     * Manager状态
     */
    private boolean isReady = false;

    /**
     * @param pipe 渲染管道
     */
    public ImageConvert(RenderPipe pipe){
        mRenderPipe = pipe;

        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                mSurface = new OutputSurface();
                mSurface.create(mRenderPipe.getContext());
            }
        });
    }


    /**
     * 设置图像输入尺寸
     * @param width 宽
     * @param height 高
     * @param cameraOrientation 相机纹理获取方向
     */
    public void setInputSize(int width,int height,ImageOrientation cameraOrientation){
        mInputSize = TuSdkSize.create(width,height).transforOrientation(cameraOrientation);
    }

    /**
     * 设置渲染宽度
     * @param renderWidth 渲染宽度
     */
    public void setRenderWidth(int renderWidth){
        mRenderWidth = renderWidth;
        if (isReady){
            mRenderPipe.getRenderPool().runSync(new Runnable() {
                @Override
                public void run() {
                    calcRenderSize();
                    prepareTexturePool();
                }
            });
        }
    }

    /**
     * 设置渲染纹理比例
     * @param w 宽
     * @param h 高
     * @return 是否成功
     */
    public boolean setAspect(double w,double h){
        if (mAspect != null) return false;
        mAspect = new Pair<>(w,h);
        return true;
    }

    /**
     * 更新渲染纹理比例
     * @param w 宽
     * @param h 高
     */
    public void updateAspect(double w,double h){
        if (!isReady) return;
        mAspect = new Pair<>(w,h);
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                calcRenderSize();
                prepareTexturePool();
            }
        });
    }

    /**
     * 设置外部属性获取接口
     * @param processProperty 外部属性接口 负责获取设备角度与是否需要高级人脸检测
     */
    public void setProcessProperty(ProcessProperty processProperty){
        mProcessProperty = processProperty;
    }

    /**
     * 状态码 :
     * -1 不可重复初始化
     * -2 缺少RenderPipe
     * -3 缺少输入尺寸
     * -4 缺少渲染比例
     * -5 缺少属性获取接口
     *
     * @return Manager 初始化结果
     */
    public Pair<Boolean,Integer> requestInit(){
        if (isReady) return new Pair<>(false,-1);
        if (mRenderPipe == null) {
            return new Pair<>(false,-2);
        }
        if (mInputSize == null){
            return new Pair<>(false,-3);
        }
        if (mAspect == null){
            return new Pair<>(false,-4);
        }
        if (mProcessProperty == null){
            return new Pair<>(false,-5);
        }

        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                calcRenderSize();
                prepareTexturePool();
            }
        });

        isReady = true;
        return new Pair<>(true,0);
    }

    public boolean isReady(){
        return isReady;
    }

    /**
     * 相机纹理就绪通知
     * @return OES转Image对象
     */
    public Image onFrameAvailable(){
        if (!isReady) return null;
        final Image[] out = new Image[1];
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                out[0] = onDrawFrame();
            }
        });
        return out[0];
    }

    /**
     * 释放
     */
    public void release(){
        if (!isReady) return;
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                GLES20.glDeleteTextures(mTexCount,mTextures,0);
                mProcessProperty = null;
                isReady = false;
            }
        });
    }

    /** Texture ID */
    private int mTexture = -1;
    /**
     * 纹理池数量
     */
    private final int mTexCount = 4;
    /**
     * 纹理池
     */
    private int[] mTextures = new int[mTexCount];
    /**
     * 当前使用纹理索引
     */
    private int mTexIdx = 0;

    /**
     * @return OES 转 Texture2D 纹理
     */
    private Image onDrawFrame(){
        mSurface.getSurfaceTexture().updateTexImage();
        mTexture = mTextures[mTexIdx];
        mTexIdx = (mTexIdx + 1) % mTexCount;

        int drawRes = mSurface.drawImageTo(mTexture,mInputSize.width,mInputSize.height,mRealRenderSize.width,mRealRenderSize.height);
        if (drawRes < 0) return null;

        long inputPos = System.currentTimeMillis();
        Image in = new Image(mTexture,mRealRenderSize.width,mRealRenderSize.height,inputPos);

        in.setAgree(mProcessProperty.getAngle());
        in.setMarkSenceEnable(mProcessProperty.getEnableMarkSence());

        return in;

    }

    /**
     * 计算实际渲染尺寸
     */
    private void calcRenderSize(){
        int renderWidth = mRenderWidth;
        int renderHeight = (int) (mAspect.second * mRenderWidth / mAspect.first);
        mRealRenderSize = TuSdkSize.create(renderWidth,renderHeight);
    }

    /**
     * 准备纹理池
     */
    private void prepareTexturePool(){
        int[] willDeleteTextures = mTextures.clone();
        GLES20.glGenTextures(mTexCount, mTextures, 0);

        for (int idx = 0; idx < mTexCount; idx++) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[idx]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    mRealRenderSize.width, mRealRenderSize.height,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    null);

        }
        if (!isFirstInitTexture){
//            GLES20.glDeleteTextures(mTexCount,willDeleteTextures,0);
        }

        isFirstInitTexture = false;
    }

    /**
     * @return 获取供外部使用的SurfaceTexture对象 内部封装OES纹理
     */
    public SurfaceTexture getSurfaceTexture(){
        return mSurface.getSurfaceTexture();
    }

    /**
     * @return 获取计算出来的实际渲染尺寸
     */
    public TuSdkSize getRenderSize(){
        return mRealRenderSize;
    }



}
