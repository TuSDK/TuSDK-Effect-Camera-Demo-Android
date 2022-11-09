package org.lasque.tubeautysetting;

import android.graphics.RectF;

import org.lasque.tusdkpulse.core.struct.TuSdkSize;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/22  10:29
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public interface Beauty {

    /**
     * 美肤模式
     */
    public static enum BeautySkinMode {
        None(""),
        /**
         * 精准美肤
         */
        SkinNatural(""),
        /**
         * 极致美肤
         */
        SkinMoist(""),
        /**
         * 自然美肤
         */
        Beauty("");

        public String skinCode;

        BeautySkinMode(String code) {
            skinCode = code;
        }
    }

    /**
     * 美妆-口红-口红样式
     */
    public enum BeautyLipstickStyle {
        /**
         * 雾面
         */
        Matte(0),
        /**
         * 水润
         */
        Moisturizing(2),
        /**
         * 滋润
         */
        Moisturize(1);

        public int mType;

        BeautyLipstickStyle (int type) {
            this.mType = type;
        }

        public static BeautyLipstickStyle getStyleFromValue(int i){
            if (i == 1) return Moisturize;
            if (i == 2) return Moisturizing;
            return Matte;
        }

    }


    public enum JoinerBoundType{
        Camera,Video;
    }

    /**
     * @param beautyStyle 美肤模式
     */
    void setBeautyStyle(BeautySkinMode beautyStyle);

    /**
     * @param level 磨皮级别
     */
    void setSmoothLevel(float level);

    /**
     * @param level 美白级别
     */
    void setWhiteningLevel(float level);

    /**
     * 仅限 精准/极致 美肤模式使用
     * @param level 红润级别
     */
    void setRuddyLevel(float level);

    /**
     * 仅限 自然美肤模式使用
     * @param level 锐化级别
     */
    void setSharpenLevel(float level);

    /**
     * @return 当前是否开启了微整形
     */
    boolean hasPlastic();

    /**
     * @param level 设置大眼级别 [0-1]
     */
    void setEyeEnlargeLevel(float level);

    /**
     * @param level 设置瘦脸级别 [0-1]
     */
    void setCheekThinLevel(float level);

    /**
     * @param level 设置窄脸级别 [0-1]
     */
    void setCheekNarrowLevel(float level);

    /**
     * @param level 设置小脸级别 [0-1]
     */
    void setFaceSmallLevel(float level);

    /**
     * @param level 设置瘦鼻级别 [0 - 1]
     */
    void setNoseWidthLevel(float level);

    /**
     * @param level 设置长鼻级别 [0 - 1]
     */
    void setNoseHeightLevel(float level);

    /**
     * @param level 设置嘴型级别 [-1 - 1]
     */
    void setMouthWidthLevel(float level);

    /**
     * @param level 设置唇厚级别 [-1 - 1]
     */
    void setLipsThicknessLevel(float level);

    /**
     * @param level 设置缩人中界别 [-1 - 1]
     */
    void setPhilterumThicknessLevel(float level);

    /**
     * @param level 设置细眉级别 [-1 - 1]
     */
    void setBrowThicknessLevel(float level);

    /**
     * @param level 设置眉高级别 [-1 - 1]
     */
    void setBrowHeightLevel(float level);

    /**
     * @param level 设置下巴(拉伸或收缩)级别 [-1 - 1]
     */
    void setChinThicknessLevel(float level);

    /**
     * @param level 设置下颌骨级别 [0 - 1]
     */
    void setCheekLowBoneNarrowLevel(float level);

    /**
     * @param level 设置眼角级别 [0 - 1]
     */
    void setEyeAngleLevel(float level);

    /**
     * @param level 设置开内眼角级别 [0 - 1]
     */
    void setEyeInnerConerLevel(float level);

    /**
     * @param level 设置开外眼角级别 [0 - 1]
     */
    void setEyeOuterConerLevel(float level);

    /**
     * @param level 设置眼距级别 [-1 - 1]
     */
    void setEyeDistanceLevel(float level);

    /**
     * @param level 设置眼移动级别 [-1 - 1]
     */
    void setEyeHeightLevel(float level);

    /**
     * @param level 设置发际线级别 [-1 - 1]
     */
    void setForeheadHeightLevel(float level);

    /**
     * @param level 设置瘦颧骨级别 [-1 - 1]
     */
    void setCheekBoneNarrowLevel(float level);

    /**
     * @param level 这是双眼皮级别 [0 - 1]
     */
    void setEyelidLevel(float level);

    /**
     * @param level 设置卧蚕级别 [0 - 1]
     */
    void setEyemazingLevel(float level);

    /**
     * @param level 设置白牙级别 [0 - 1]
     */
    void setWhitenTeethLevel(float level);

    /**
     * @param level 设置亮眼级别 [0 - 1]
     */
    void setEyeDetailLevel(float level);

    /**
     * @param level 设置祛黑眼圈级别 [0 - 1]
     */
    void setRemovePouchLevel(float level);

    /**
     * @param level 设置祛法令纹级别 [0 - 1]
     */
    void setRemoveWrinklesLevel(float level);

    /**
     * @param enable 设置开启美妆-口红
     */
    void setLipEnable(boolean enable);

    /**
     * @param style 设置口红样式 水润 滋润 雾面
     */
    void setLipStyle(BeautyLipstickStyle style);

    /**
     * @param opacity 设置口红透明度 [0 - 1]
     */
    void setLipOpacity(float opacity);

    /**
     * @param color 设置口红颜色
     */
    void setLipColor(int color);

    /**
     * @param enable 设置开启美妆-腮红
     */
    void setBlushEnable(boolean enable);

    /**
     * @param opacity 设置腮红透明度 [0 - 1]
     */
    void setBlushOpacity(float opacity);

    /**
     * @param stickerId 设置腮红ID
     */
    void setBlushStickerId(long stickerId);

    /**
     * @param enable 设置开启美妆-眉毛
     */
    void setBrowEnable(boolean enable);

    /**
     * @param opacity 设置眉毛透明度 [0 - 1]
     */
    void setBrowOpacity(float opacity);

    /**
     * @param stickerId 设置眉毛ID
     */
    void setBrowStickerId(long stickerId);

    /**
     * @param enable 设置开启美妆-眼影
     */
    void setEyeshadowEnable(boolean enable);

    /**
     * @param opacity 设置眼影透明度 [0 - 1]
     */
    void setEyeshadowOpacity(float opacity);

    /**
     * @param stickerId 设置眼影ID
     */
    void setEyeshadowStickerId(long stickerId);

    /**
     * @param enable 设置开启美妆-眼线
     */
    void setEyelineEnable(boolean enable);

    /**
     * @param opacity 设置眼线透明度 [0 - 1]
     */
    void setEyelineOpacity(float opacity);

    /**
     * @param stickerId 设置眼线ID
     */
    void setEyelineStickerId(long stickerId);

    /**
     * @param enable 设置开启美妆-睫毛
     */
    void setEyelashEnable(boolean enable);

    /**
     * @param opacity 设置睫毛透明度 [0 - 1]
     */
    void setEyelashOpacity(float opacity);

    /**
     * @param stickerId 设置睫毛ID
     */
    void setEyelashStickerId(long stickerId);

    /**
     * @param enable 设置开启美妆-修容
     */
    void setFacialEnable(boolean enable);

    /**
     * @param opacity 设置修容透明度 [0 - 1]
     */
    void setFacialOpacity(float opacity);

    /**
     * @param stickerId 设置修容ID
     */
    void setFacialStickerId(long stickerId);

    /**
     * 设置滤镜
     * @param filterCode 滤镜代号
     */
    void setFilter(String filterCode);

    /**
     * @param strength 滤镜强度 [0 - 1]
     */
    void setFilterStrength(float strength);

    /**
     * 设置人脸动态贴纸
     * @param stickerId 贴纸ID
     */
    void setDynamicSticker(long stickerId);

    /**
     * @return 当前是否使用了人脸动态贴纸
     */
    boolean hasDynamicSticker();

    /**
     * 设置人脸哈哈镜
     * @param code 哈哈镜代号
     */
    void setMonsterFace(String code);

    /**
     * @return 当前是否使用了人脸哈哈镜
     */
    boolean hasMonsterFace();

    /**
     * 设置视频合拍
     * @param videoRect 合拍素材渲染区域 [0,0,1,1]
     * @param cameraRect 相机画面渲染区域 [0,0,1,1]
     * @param videoPath 合拍素材路径
     */
    void setJoiner(RectF videoRect,RectF cameraRect,String videoPath,boolean useSoftDecoding,RectF videoSrcRect,RectF cameraSrcRect);

    /**
     * 取消合拍
     */
    void deleteJoiner();

    /**
     * 更新合拍素材以及相机渲染区域
     * @param videoRect 合拍素材渲染区域 [0,0,1,1]
     * @param cameraRect 相机画面渲染区域 [0,0,1,1]
     */
    void updateJoiner(RectF videoRect,RectF cameraRect);

    /**
     * @param type 边框类型 视频区域或相机区域
     * @param width 边框宽度 小于1则视为无边框
     * @param color 边框颜色
     * @param miter 边框圆角幅度
     */
    void updateJoinerBound(JoinerBoundType type,double width,int color,double miter);

    /**
     * @return 当前是否应用了合拍功能
     */
    boolean hasJoiner();

    /**
     * 合拍素材跳转
     * @param ts 视频跳转位置
     * @return 是否跳转成功
     */
    boolean joinerSeek(long ts);

    /**
     * 调节合拍视频播放速度
     * @param videoStretch 播放速度
     */
    void updateVideoStretch(double videoStretch);

    /**
     * 设置渲染宽度
     * @param size 渲染宽度
     */
    void setRenderSize(TuSdkSize size);

    /**
     * 设置合拍视频开始播放位置
     * @param ts
     */
    void updateJoinerPlayerStartPos(long ts);

    /**
     * 设置合拍视频播放状态
     * @param isPlaying 是否开始播放
     */
    void setJoinerPlayerState(boolean isPlaying);


}
