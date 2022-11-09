package org.lasque.tubeautysetting;

import android.graphics.RectF;
import android.text.TextUtils;

import com.tusdk.pulse.Config;
import com.tusdk.pulse.filter.Filter;
import com.tusdk.pulse.filter.FilterPipe;
import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.filter.filters.SimultaneouslyFilter;
import com.tusdk.pulse.filter.filters.TusdkBeautFaceV2Filter;
import com.tusdk.pulse.filter.filters.TusdkCosmeticFilter;
import com.tusdk.pulse.filter.filters.TusdkFaceMonsterFilter;
import com.tusdk.pulse.filter.filters.TusdkFacePlasticFilter;
import com.tusdk.pulse.filter.filters.TusdkImageFilter;
import com.tusdk.pulse.filter.filters.TusdkLiveStickerFilter;
import com.tusdk.pulse.filter.filters.TusdkReshapeFilter;

import org.lasque.tusdkpulse.core.seles.SelesParameters;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.utils.TLog;

import java.util.HashMap;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/21  18:50
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class BeautyManager implements Beauty {

    public final static int DOUBLE_VIEW_INDEX = 50;

    public final static HashMap<SelesParameters.FilterModel, Integer> mFilterMap = new HashMap<SelesParameters.FilterModel, Integer>();

    static {
        mFilterMap.put(SelesParameters.FilterModel.Reshape, 13);
        mFilterMap.put(SelesParameters.FilterModel.CosmeticFace, 14);
        mFilterMap.put(SelesParameters.FilterModel.MonsterFace, 15);
        mFilterMap.put(SelesParameters.FilterModel.PlasticFace, 16);
        mFilterMap.put(SelesParameters.FilterModel.StickerFace, 17);
        mFilterMap.put(SelesParameters.FilterModel.SkinFace, 18);
        mFilterMap.put(SelesParameters.FilterModel.Filter, 19);
    }


    /**
     * RenderPipe
     */
    private RenderPipe mRenderPipe;

    private TusdkReshapeFilter.PropertyBuilder mReshapeProperty;

    private TusdkCosmeticFilter.PropertyBuilder mCosmeticProperty;

    private TusdkFacePlasticFilter.PropertyBuilder mPlasticProperty;

    private TusdkImageFilter.SkinNaturalPropertyBuilder mNaturalProperty;

    private TusdkImageFilter.SkinHazyPropertyBuilder mHazyProperty;

    private TusdkBeautFaceV2Filter.PropertyBuilder mBeautyProperty;

    private TusdkImageFilter.MixedPropertyBuilder mFilterProperty;

    private SimultaneouslyFilter.PropertyBuilder mDoubleViewProperty;

    private FilterPipe mFP;

    private boolean isReady = false;

    private double mCurrentVideoStretch = 1.0;

    private TuSdkSize mRenderSize;

    private long mJoinerPlayStartPos = 0L;


    /**
     * @param renderPipe 渲染队列
     * @return 初始化状态
     */
    public boolean requestInit(RenderPipe renderPipe) {
        mRenderPipe = renderPipe;
        mReshapeProperty = new TusdkReshapeFilter.PropertyBuilder();
        mCosmeticProperty = new TusdkCosmeticFilter.PropertyBuilder();
        mPlasticProperty = new TusdkFacePlasticFilter.PropertyBuilder();
        mNaturalProperty = new TusdkImageFilter.SkinNaturalPropertyBuilder();
        mHazyProperty = new TusdkImageFilter.SkinHazyPropertyBuilder();
        mBeautyProperty = new TusdkBeautFaceV2Filter.PropertyBuilder();
        mFilterProperty = new TusdkImageFilter.MixedPropertyBuilder();

        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                mFP = new FilterPipe();
                isReady = mFP.create();
            }
        });
        return isReady;
    }

    public boolean isReady() {
        return isReady;
    }

    /**
     * 图像处理
     * @param in 输入纹理
     * @return 输出纹理
     */
    public Image processFrame(final Image in){
        final Image[] out = new Image[1];
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                out[0] = mFP.process(in);
                in.release();
            }
        });
        return out[0];
    }

    /**
     * @return 判断是否需要高级人脸检测
     */
    public boolean checkEnableMarkSence() {
        int cosmeticEnable = mCosmeticProperty.blushEnable + mCosmeticProperty.browEnable + mCosmeticProperty.eyelashEnable + mCosmeticProperty.eyelineEnable + mCosmeticProperty.eyeshadowEnable + mCosmeticProperty.lipEnable + mCosmeticProperty.facialEnable;

        int reshapeIndex = mFilterMap.get(SelesParameters.FilterModel.Reshape);
        Filter reshapeFilter = mFP.getFilter(reshapeIndex);

        boolean makeSence = (reshapeFilter != null) || (cosmeticEnable > 0);

        return makeSence;

    }

    /**
     * @param code 更换滤镜
     */
    private void changedFilter(String code) {
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.Filter);
                mFP.deleteFilter(index);
                if (!TextUtils.isEmpty(code)) {
                    Filter filter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                    Config config = new Config();
                    config.setString(TusdkImageFilter.CONFIG_NAME, code);
                    filter.setConfig(config);
                    mFP.addFilter(index, filter);
                }
            }
        });
    }

    /**
     * 更新滤镜参数
     */
    private void updateFilter() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.Filter);
                Filter filter = mFP.getFilter(index);
                if (filter != null) {
                    filter.setProperty(TusdkImageFilter.PROP_PARAM, mFilterProperty.makeProperty());
                }
            }
        });
    }

    /**
     * @param mode 更换美肤模式
     */
    private void changedSkinMode(BeautySkinMode mode) {
        if (mode == mCurrentSkinMode) return;
        mCurrentSkinMode = mode;
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.SkinFace);
                mFP.deleteFilter(index);
                switch (mode) {
                    case None:{
                        break;
                    }

                    case SkinNatural: {
                        Filter skinNaturalFilter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                        Config config = new Config();
                        config.setString(TusdkImageFilter.CONFIG_NAME, TusdkImageFilter.NAME_SkinNatural);
                        skinNaturalFilter.setConfig(config);
                        mFP.addFilter(index, skinNaturalFilter);
                        break;
                    }

                    case SkinMoist: {
                        Filter skinMoistFilter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                        Config config = new Config();
                        config.setString(TusdkImageFilter.CONFIG_NAME, TusdkImageFilter.NAME_SkinHazy);
                        skinMoistFilter.setConfig(config);
                        mFP.addFilter(index, skinMoistFilter);
                        break;
                    }

                    case Beauty: {
                        Filter beauty = new Filter(mFP.getContext(), TusdkBeautFaceV2Filter.TYPE_NAME);
                        mFP.addFilter(index, beauty);
                        break;
                    }

                }
            }
        });
    }

    /**
     * 更新美肤参数
     */
    private void updateSkinProperty() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.SkinFace);
                Filter filter = mFP.getFilter(index);
                switch (mCurrentSkinMode) {
                    case None:{
                        break;
                    }
                    case SkinNatural: {
                        filter.setProperty(TusdkImageFilter.PROP_PARAM, mNaturalProperty.makeProperty());
                        break;
                    }
                    case SkinMoist: {
                        filter.setProperty(TusdkImageFilter.PROP_PARAM, mHazyProperty.makeProperty());
                        break;
                    }
                    case Beauty: {
                        filter.setProperty(TusdkBeautFaceV2Filter.PROP_PARAM, mBeautyProperty.makeProperty());
                        break;
                    }
                }
            }
        });
    }

    /**
     * @param stickerId 更新人脸贴纸
     */
    private void updateSticker(long stickerId) {
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                int monsterIndex = mFilterMap.get(SelesParameters.FilterModel.MonsterFace);
                int stickerIndex = mFilterMap.get(SelesParameters.FilterModel.StickerFace);
                mFP.deleteFilter(stickerIndex);
                if (stickerId > 0){
                    mFP.deleteFilter(monsterIndex);

                    Filter sticker = new Filter(mFP.getContext(), TusdkLiveStickerFilter.TYPE_NAME);
                    Config config = new Config();
                    config.setNumber(TusdkLiveStickerFilter.CONFIG_ID, stickerId);
                    sticker.setConfig(config);

                    mFP.addFilter(stickerIndex, sticker);
                }
            }
        });
    }

    /**
     * 检测当前是否存在微整形滤镜
     */
    private void checkPlastic() {
        int index = mFilterMap.get(SelesParameters.FilterModel.PlasticFace);
        Filter plastic = mFP.getFilter(index);
        if (plastic != null) return;
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.PlasticFace);
                Filter plastic = mFP.getFilter(index);
                if (plastic == null) {
                    plastic = new Filter(mFP.getContext(), TusdkFacePlasticFilter.TYPE_NAME);
                    mFP.addFilter(index, plastic);
                }
            }
        });
    }

    /**
     * 更新微整形参数
     */
    private void updatePlastic() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.PlasticFace);
                Filter plastic = mFP.getFilter(index);
                if (plastic != null) {
                    plastic.setProperty(TusdkFacePlasticFilter.PROP_PARAM, mPlasticProperty.makeProperty());
                }
            }
        });
    }

    /**
     * @param code 更新哈哈镜
     */
    private void updateMonsterFace(String code) {
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {



                int monsterIndex = mFilterMap.get(SelesParameters.FilterModel.MonsterFace);
                int stickerIndex = mFilterMap.get(SelesParameters.FilterModel.StickerFace);
                mFP.deleteFilter(monsterIndex);
                mFP.deleteFilter(stickerIndex);

                if (TextUtils.isEmpty(code)) return;


                int reshareIndex = mFilterMap.get(SelesParameters.FilterModel.Reshape);
                int plasticIndex = mFilterMap.get(SelesParameters.FilterModel.PlasticFace);

                mFP.deleteFilter(reshareIndex);
                mFP.deleteFilter(plasticIndex);


                Filter monster = new Filter(mFP.getContext(), TusdkFaceMonsterFilter.TYPE_NAME);
                Config config = new Config();
                config.setString(TusdkFaceMonsterFilter.CONFIG_TYPE, code);
                boolean configRet = monster.setConfig(config);
                boolean ret = mFP.addFilter(monsterIndex, monster);
                TLog.e("add monster filter ret %s code %s monster index %s config ret %s",ret,code,monsterIndex,configRet);
            }
        });
    }

    /**
     * 检测当前是否添加了美妆滤镜
     */
    private void checkCosmetic() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.CosmeticFace);
                Filter cosmetic = mFP.getFilter(index);
                if (cosmetic == null) {
                    cosmetic = new Filter(mFP.getContext(), TusdkCosmeticFilter.TYPE_NAME);
                    mFP.addFilter(index, cosmetic);
                }
            }
        });
    }

    /**
     * 更新美妆滤镜
     */
    private void updateCosmetic() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.CosmeticFace);
                Filter cosmetic = mFP.getFilter(index);
                if (cosmetic != null) {
                    cosmetic.setProperty(TusdkCosmeticFilter.PROP_PARAM, mCosmeticProperty.makeProperty());
                }
            }
        });
    }

    /**
     * 检测当前是否添加了微整形扩展滤镜
     */
    private void checkReshape() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.Reshape);
                Filter reshape = mFP.getFilter(index);
                if (reshape == null) {
                    reshape = new Filter(mFP.getContext(), TusdkReshapeFilter.TYPE_NAME);
                    mFP.addFilter(index, reshape);
                }
            }
        });
    }

    /**
     * 更新微整形扩展参数
     */
    private void updateReshape() {
        mRenderPipe.getRenderPool().runAsync(new Runnable() {
            @Override
            public void run() {
                int index = mFilterMap.get(SelesParameters.FilterModel.Reshape);
                Filter reshape = mFP.getFilter(index);
                if (reshape != null) {
                    reshape.setProperty(TusdkReshapeFilter.PROP_PARAM, mReshapeProperty.makeProperty());
                }
            }
        });
    }

    private BeautySkinMode mCurrentSkinMode;


    @Override
    public void setBeautyStyle(BeautySkinMode beautyStyle) {
        changedSkinMode(beautyStyle);
    }

    @Override
    public void setSmoothLevel(float level) {
        switch (mCurrentSkinMode) {

            case SkinNatural:
                mNaturalProperty.smoothing = level;
                break;
            case SkinMoist:
                mHazyProperty.smoothing = level;
                break;
            case Beauty:
                mBeautyProperty.smoothing = level;
                break;
        }
        updateSkinProperty();
    }

    @Override
    public void setWhiteningLevel(float level) {
        switch (mCurrentSkinMode) {

            case SkinNatural:
                mNaturalProperty.fair = level;
                break;
            case SkinMoist:
                mHazyProperty.fair = level;
                break;
            case Beauty:
                mBeautyProperty.whiten = level;
                break;
        }
        updateSkinProperty();
    }

    @Override
    public void setRuddyLevel(float level) {
        switch (mCurrentSkinMode) {

            case SkinNatural:
                mNaturalProperty.ruddy = level;
                break;
            case SkinMoist:
                mHazyProperty.ruddy = level;
                break;
            case Beauty:
                return;
        }
        updateSkinProperty();
    }

    @Override
    public void setSharpenLevel(float level) {
        switch (mCurrentSkinMode) {

            case SkinNatural:
            case SkinMoist:
                return;
            case Beauty:
                mBeautyProperty.sharpen = level;
                break;
        }
        updateSkinProperty();
    }

    @Override
    public boolean hasPlastic() {
        int index = mFilterMap.get(SelesParameters.FilterModel.PlasticFace);
        return mFP.getFilter(index) != null;
    }

    @Override
    public void setEyeEnlargeLevel(float level) {
        checkPlastic();
        mPlasticProperty.eyeEnlarge = level;
        updatePlastic();
    }

    @Override
    public void setCheekThinLevel(float level) {
        checkPlastic();
        mPlasticProperty.cheekThin = level;
        updatePlastic();
    }

    @Override
    public void setCheekNarrowLevel(float level) {
        checkPlastic();
        mPlasticProperty.cheekNarrow = level;
        updatePlastic();
    }

    @Override
    public void setFaceSmallLevel(float level) {
        checkPlastic();
        mPlasticProperty.faceSmall = level;
        updatePlastic();
    }

    @Override
    public void setNoseWidthLevel(float level) {
        checkPlastic();
        mPlasticProperty.noseWidth = level;
        updatePlastic();
    }

    @Override
    public void setNoseHeightLevel(float level) {
        checkPlastic();
        mPlasticProperty.noseHeight = level;
        updatePlastic();
    }

    @Override
    public void setMouthWidthLevel(float level) {
        checkPlastic();
        mPlasticProperty.mouthWidth = level;
        updatePlastic();
    }

    @Override
    public void setLipsThicknessLevel(float level) {
        checkPlastic();
        mPlasticProperty.lipsThickness = level;
        updatePlastic();
    }

    @Override
    public void setPhilterumThicknessLevel(float level) {
        checkPlastic();
        mPlasticProperty.philterumThickness = level;
        updatePlastic();
    }

    @Override
    public void setBrowThicknessLevel(float level) {
        checkPlastic();
        mPlasticProperty.browThickness = level;
        updatePlastic();
    }

    @Override
    public void setBrowHeightLevel(float level) {
        checkPlastic();
        mPlasticProperty.browHeight = level;
        updatePlastic();
    }

    @Override
    public void setChinThicknessLevel(float level) {
        checkPlastic();
        mPlasticProperty.chinThickness = level;
        updatePlastic();
    }

    @Override
    public void setCheekLowBoneNarrowLevel(float level) {
        checkPlastic();
        mPlasticProperty.cheekLowBoneNarrow = level;
        updatePlastic();
    }

    @Override
    public void setEyeAngleLevel(float level) {
        checkPlastic();
        mPlasticProperty.eyeAngle = level;
        updatePlastic();
    }

    @Override
    public void setEyeInnerConerLevel(float level) {
        checkPlastic();
        mPlasticProperty.eyeInnerConer = level;
        updatePlastic();
    }

    @Override
    public void setEyeOuterConerLevel(float level) {
        checkPlastic();
        mPlasticProperty.eyeOuterConer = level;
        updatePlastic();
    }

    @Override
    public void setEyeDistanceLevel(float level) {
        checkPlastic();
        mPlasticProperty.eyeDistance = level;
        updatePlastic();
    }

    @Override
    public void setEyeHeightLevel(float level) {
        checkPlastic();
        mPlasticProperty.eyeHeight = level;
        updatePlastic();
    }

    @Override
    public void setForeheadHeightLevel(float level) {
        checkPlastic();
        mPlasticProperty.foreheadHeight = level;
        updatePlastic();
    }

    @Override
    public void setCheekBoneNarrowLevel(float level) {
        checkPlastic();
        mPlasticProperty.cheekBoneNarrow = level;
        updatePlastic();
    }

    @Override
    public void setEyelidLevel(float level) {
        checkReshape();
        mReshapeProperty.eyelidOpacity = level;
        updateReshape();
    }

    @Override
    public void setEyemazingLevel(float level) {
        checkReshape();
        mReshapeProperty.eyemazingOpacity = level;
        updateReshape();
    }

    @Override
    public void setWhitenTeethLevel(float level) {
        checkReshape();
        mReshapeProperty.whitenTeethOpacity = level;
        updateReshape();
    }

    @Override
    public void setEyeDetailLevel(float level) {
        checkReshape();
        mReshapeProperty.eyeDetailOpacity = level;
        updateReshape();
    }

    @Override
    public void setRemovePouchLevel(float level) {
        checkReshape();
        mReshapeProperty.removePouchOpacity = level;
        updateReshape();
    }

    @Override
    public void setRemoveWrinklesLevel(float level) {
        checkReshape();
        mReshapeProperty.removeWrinklesOpacity = level;
        updateReshape();
    }

    @Override
    public void setLipEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.lipEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setLipColor(int color) {
        checkCosmetic();
        mCosmeticProperty.lipColor = color;
        mCosmeticProperty.lipEnable = 1;

        updateCosmetic();

    }

    @Override
    public void setLipOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.lipOpacity = opacity;
        mCosmeticProperty.lipEnable = 1;

        updateCosmetic();

    }

    @Override
    public void setLipStyle(BeautyLipstickStyle style) {
        checkCosmetic();
        mCosmeticProperty.lipStyle = style.mType;
        mCosmeticProperty.lipEnable = 1;
        updateCosmetic();

    }

    @Override
    public void setBlushEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.blushEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setBlushOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.blushEnable = 1;
        mCosmeticProperty.blushOpacity = opacity;
        updateCosmetic();
    }

    @Override
    public void setBlushStickerId(long stickerId) {
        checkCosmetic();
        mCosmeticProperty.blushEnable = 1;
        mCosmeticProperty.blushId = stickerId;
        updateCosmetic();
    }

    @Override
    public void setBrowEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.browEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setBrowOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.browOpacity = opacity;
        mCosmeticProperty.browEnable = 1;
        updateCosmetic();
    }

    @Override
    public void setBrowStickerId(long stickerId) {
        checkCosmetic();
        mCosmeticProperty.browEnable = 1;
        mCosmeticProperty.browId = stickerId;
        updateCosmetic();
    }

    @Override
    public void setEyeshadowEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.eyeshadowEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setEyeshadowOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.eyeshadowEnable = 1;
        mCosmeticProperty.eyeshadowOpacity = opacity;
        updateCosmetic();
    }

    @Override
    public void setEyeshadowStickerId(long stickerId) {
        checkCosmetic();
        mCosmeticProperty.eyeshadowEnable = 1;
        mCosmeticProperty.eyeshadowId = stickerId;
        updateCosmetic();
    }

    @Override
    public void setEyelineEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.eyelineEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setEyelineOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.eyelineEnable = 1;
        mCosmeticProperty.eyelineOpacity = opacity;
        updateCosmetic();
    }

    @Override
    public void setEyelineStickerId(long stickerId) {
        checkCosmetic();
        mCosmeticProperty.eyelineEnable = 1;
        mCosmeticProperty.eyelineId = stickerId;
        updateCosmetic();
    }

    @Override
    public void setEyelashEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.eyelashEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setEyelashOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.eyelashEnable = 1;
        mCosmeticProperty.eyelashOpacity = opacity;
        updateCosmetic();
    }

    @Override
    public void setEyelashStickerId(long stickerId) {
        checkCosmetic();
        mCosmeticProperty.eyelashEnable = 1;
        mCosmeticProperty.eyelashId = stickerId;
        updateCosmetic();
    }

    @Override
    public void setFacialEnable(boolean enable) {
        checkCosmetic();
        mCosmeticProperty.facialEnable = enable ? 1 : 0;
        updateCosmetic();
    }

    @Override
    public void setFacialOpacity(float opacity) {
        checkCosmetic();
        mCosmeticProperty.facialEnable = 1;
        mCosmeticProperty.facialOpacity = opacity;
        updateCosmetic();
    }

    @Override
    public void setFacialStickerId(long stickerId) {
        checkCosmetic();
        mCosmeticProperty.facialEnable = 1;
        mCosmeticProperty.facialId = stickerId;
        updateCosmetic();
    }

    @Override
    public void setFilter(String filterCode) {
        changedFilter(filterCode);
    }

    @Override
    public void setFilterStrength(float strength) {
        mFilterProperty.strength = strength;
        updateFilter();
    }

    @Override
    public void setDynamicSticker(long stickerId) {
        updateSticker(stickerId);
    }

    @Override
    public boolean hasDynamicSticker() {
        boolean res = false;
        int stickerIndex = mFilterMap.get(SelesParameters.FilterModel.StickerFace);
        res =  (mFP.getFilter(stickerIndex) != null);
        return res;
    }

    @Override
    public void setMonsterFace(String code) {
        updateMonsterFace(code);
    }

    @Override
    public boolean hasMonsterFace() {
        boolean res = false;
        int monsterIndex = mFilterMap.get(SelesParameters.FilterModel.MonsterFace);
        res = (mFP.getFilter(monsterIndex) != null);
        return res;
    }

    @Override
    public void setJoiner(RectF videoRect, RectF cameraRect, String videoPath,boolean useSoftDecoding,RectF videoSrcRect,RectF cameraSrcRect) {
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                mFP.deleteFilter(DOUBLE_VIEW_INDEX);
                Filter filter = new Filter(mFP.getContext(), SimultaneouslyFilter.TYPE_NAME);
                Config config = new Config();
                config.setNumber(SimultaneouslyFilter.CONFIG_WIDTH,mRenderSize.width);
                config.setNumber(SimultaneouslyFilter.CONFIG_HEIGHT,mRenderSize.height);

                config.setString(SimultaneouslyFilter.CONFIG_PATH,videoPath);
                config.setString(SimultaneouslyFilter.CONFIG_FIRST_LAYER,SimultaneouslyFilter.INDEX_CAMERA);
                config.setNumber(SimultaneouslyFilter.CONFIG_STRETCH,mCurrentVideoStretch);
                config.setNumber(SimultaneouslyFilter.CONFIG_FRAMERATE,30);

                config.setBoolean(SimultaneouslyFilter.CONFIG_USE_SOFT_DECODING,useSoftDecoding);

                if (!filter.setConfig(config)){
                    return;
                }

                if (!mFP.addFilter(DOUBLE_VIEW_INDEX,filter)){
                    return;
                }

                SimultaneouslyFilter.PropertyBuilder builder = new SimultaneouslyFilter.PropertyBuilder();
                builder.holder.video_dst_rect = videoRect;
                builder.holder.camera_dst_rect = cameraRect;

                builder.holder.video_src_rect = videoSrcRect;
                builder.holder.camera_src_rect = cameraSrcRect;

                filter.setProperty(SimultaneouslyFilter.PROP_RECT_PARAM,builder.makeRectProperty());

                if (mJoinerPlayStartPos > 0){
                    builder.holder.current_pos = (int) mJoinerPlayStartPos;

                    filter.setProperty(SimultaneouslyFilter.PROP_SEEK_PARAM,builder.makeSeekProperty());

                }

                mDoubleViewProperty = builder;

            }
        });

    }

    @Override
    public void deleteJoiner() {
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                mFP.deleteFilter(DOUBLE_VIEW_INDEX);
            }
        });
    }

    @Override
    public boolean hasJoiner() {
        return mFP.getFilter(DOUBLE_VIEW_INDEX) != null;
    }

    @Override
    public boolean joinerSeek(long ts) {
        if (!hasJoiner()) return false;
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                Filter filter = mFP.getFilter(DOUBLE_VIEW_INDEX);
                mDoubleViewProperty.holder.current_pos = (int) ts;

                filter.setProperty(SimultaneouslyFilter.PROP_SEEK_PARAM,mDoubleViewProperty.makeSeekProperty());

            }
        });
        return true;
    }

    @Override
    public void updateVideoStretch(double videoStretch) {
        mCurrentVideoStretch = videoStretch;
    }

    @Override
    public void updateJoiner(RectF videoRect, RectF cameraRect) {
        if (mFP.getFilter(DOUBLE_VIEW_INDEX) == null) return;
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                Filter filter = mFP.getFilter(DOUBLE_VIEW_INDEX);

                mDoubleViewProperty.holder.camera_dst_rect = cameraRect;
                mDoubleViewProperty.holder.video_dst_rect = videoRect;

                filter.setProperty(SimultaneouslyFilter.PROP_RECT_PARAM,mDoubleViewProperty.makeRectProperty());
            }
        });
    }

    @Override
    public void updateJoinerBound(JoinerBoundType type, double width, int color, double miter) {
        if (mFP.getFilter(DOUBLE_VIEW_INDEX) == null) return;
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                Filter filter = mFP.getFilter(DOUBLE_VIEW_INDEX);
                if (type == JoinerBoundType.Camera){
                    mDoubleViewProperty.holder.camera_bound_color = color;
                    mDoubleViewProperty.holder.camera_bound_width = width;
                    mDoubleViewProperty.holder.camera_bound_miter = miter;
                } else if (type == JoinerBoundType.Video){
                    mDoubleViewProperty.holder.video_bound_color = color;
                    mDoubleViewProperty.holder.video_bound_width = width;
                    mDoubleViewProperty.holder.video_bound_miter = miter;
                }
                filter.setProperty(SimultaneouslyFilter.PROP_RECT_PARAM,mDoubleViewProperty.makeRectProperty());
            }
        });

    }

    @Override
    public void setJoinerPlayerState(boolean isPlaying) {
        if (mFP.getFilter(DOUBLE_VIEW_INDEX) == null) return;
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {

                Filter filter = mFP.getFilter(DOUBLE_VIEW_INDEX);

                mDoubleViewProperty.holder.enable_play = isPlaying;
                filter.setProperty(SimultaneouslyFilter.PROP_PARAM,mDoubleViewProperty.makeProperty());
            }
        });
    }

    @Override
    public void updateJoinerPlayerStartPos(long ts) {
        mJoinerPlayStartPos = ts;
    }

    @Override
    public void setRenderSize(TuSdkSize size) {
        mRenderSize = size;
    }


    public void release(){
        mRenderPipe.getRenderPool().runSync(new Runnable() {
            @Override
            public void run() {
                mFP.clearFilters();
                mFP.destroy();
            }
        });
        isReady = false;
    }
}
