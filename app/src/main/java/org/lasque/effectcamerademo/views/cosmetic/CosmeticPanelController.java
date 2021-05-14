package org.lasque.effectcamerademo.views.cosmetic;

import android.content.Context;

import com.tusdk.pulse.filter.Filter;
import com.tusdk.pulse.filter.FilterPipe;
import com.tusdk.pulse.filter.filters.TusdkCosmeticFilter;

import org.lasque.effectcamerademo.views.record.RecordView;
import org.lasque.tusdkpulse.core.seles.SelesParameters;
import org.lasque.effectcamerademo.views.cosmetic.panel.BasePanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.blush.BlushPanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.eyebrow.EyebrowPanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.eyelash.EyelashPanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.eyeliner.EyelinerPanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.eyeshadow.EyeshadowPanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.facial.FacialPanel;
import org.lasque.effectcamerademo.views.cosmetic.panel.lipstick.LipstickPanel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * TuSDK
 * org.lasque.effectcamerademo.views.cosmetic
 * droid-sdk-video-refresh
 *
 * @author H.ys
 * @Date 2020/10/20  11:18
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class CosmeticPanelController {
    public static HashMap<String, Float> mDefaultCosmeticPercentParams = new HashMap<String, Float>() {
        {
            put("lipAlpha",0.5f);
            put("blushAlpha",0.5f);
            put("eyebrowAlpha",0.4f);
            put("eyeshadowAlpha",0.5f);
            put("eyelineAlpha",0.5f);
            put("eyelashAlpha",0.5f);
            put("facialAlpha",0.4f);
        }
    };

    private static HashMap<String,Float> mDefaultCosmeticMaxPercentParams = new HashMap<String, Float>(){
        {
            put("lipAlpha",0.8f);
            put("blushAlpha",1.0f);
            put("eyebrowAlpha",0.7f);
            put("eyeshadowAlpha",1.0f);
            put("eyelineAlpha",1.0f);
            put("eyelashAlpha",1.0f);
            put("facialAlpha",1.0f);
        }
    };

    /**
     * 口红列表
     */
    public static List<CosmeticTypes.LipstickType> mLipstickTypes = Arrays.asList(CosmeticTypes.LipstickType.values());

    /**
     * 睫毛列表
     */
    public static List<CosmeticTypes.EyelashType> mEyelashTypes = Arrays.asList(CosmeticTypes.EyelashType.values());

    /**
     * 眉毛列表
     */
    public static List<CosmeticTypes.EyebrowType> mEyebrowTypes = Arrays.asList(CosmeticTypes.EyebrowType.values());

    /**
     * 腮红列表
     */
    public static List<CosmeticTypes.BlushType> mBlushTypes = Arrays.asList(CosmeticTypes.BlushType.values());

    /**
     * 眼影类型
     */
    public static List<CosmeticTypes.EyeshadowType> mEyeshadowTypes = Arrays.asList(CosmeticTypes.EyeshadowType.values());

    /**
     * 眼线类型
     */
    public static List<CosmeticTypes.EyelinerType> mEyelinerTypes = Arrays.asList(CosmeticTypes.EyelinerType.values());

    /**
     * 修容类型
     */
    public static List<CosmeticTypes.FacialType> mFacialTypes = Arrays.asList(CosmeticTypes.FacialType.values());



    private SelesParameters mEffect = new SelesParameters();

    private Context mContext;

    private Filter mCosmeticFilter;

    private FilterPipe mFilterPipe;

    private ExecutorService mRenderPool;

    private TusdkCosmeticFilter.PropertyBuilder mProperty;

    public CosmeticPanelController(Context context){
        this.mContext = context;


    }

    public void initCosmetic(FilterPipe filterPipe, ExecutorService renderPool){
        mFilterPipe = filterPipe;
        mRenderPool = renderPool;



        Future<Boolean> res = mRenderPool.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mCosmeticFilter = new Filter(mFilterPipe.getContext(),TusdkCosmeticFilter.TYPE_NAME);
                boolean ret = mFilterPipe.addFilter(RecordView.mFilterMap.get(SelesParameters.FilterModel.CosmeticFace),mCosmeticFilter);
                mProperty = new TusdkCosmeticFilter.PropertyBuilder();

                mEffect.setListener(new SelesParameters.SelesParametersListener() {
                    @Override
                    public void onUpdateParameters(SelesParameters.FilterModel model, String code, SelesParameters.FilterArg arg) {
                        double value = arg.getValue();
                        switch (arg.getKey()){
                            case "lipAlpha":
                                mProperty.lipOpacity = value;
                                mProperty.lipEnable = 1;
                                break;
                            case "blushAlpha":
                                mProperty.blushOpacity = value;
                                mProperty.blushEnable = 1;
                                break;
                            case "eyebrowAlpha":
                                mProperty.browOpacity = value;
                                mProperty.browEnable = 1;
                                break;
                            case "eyeshadowAlpha":
                                mProperty.eyeshadowOpacity = value;
                                mProperty.eyeshadowEnable = 1;
                                break;
                            case "eyelineAlpha":
                                mProperty.eyelineOpacity = value;
                                mProperty.eyelineEnable = 1;
                                break;
                            case "eyelashAlpha":
                                mProperty.eyelashOpacity = value;
                                mProperty.eyelashEnable = 1;
                                break;
                            case "facialAlpha":
                                mProperty.facialOpacity = value;
                                mProperty.facialEnable = 1;
                                break;
                        }
                        updateProperty();
                    }
                });

                for (String key : mDefaultCosmeticPercentParams.keySet()){
                    mEffect.appendFloatArg(key,mDefaultCosmeticPercentParams.get(key));
                }

//                for (String key : mDefaultCosmeticMaxPercentParams.keySet()){
//                    SelesParameters.FilterArg arg = mEffect.getFilterArg(key);
//                    arg.setMaxValueFactor(mDefaultCosmeticMaxPercentParams.get(key));
//                }
                return ret;
            }
        });

        try {
            res.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LipstickPanel getLipstickPanel() {
        if (mLipstickPanel == null){
            mLipstickPanel = new LipstickPanel(this);
        }
        return mLipstickPanel;
    }

    public BlushPanel getBlushPanel() {
        if (mBlushPanel == null){
            mBlushPanel = new BlushPanel(this);
        }
        return mBlushPanel;
    }

    public EyebrowPanel getEyebrowPanel() {
        if (mEyebrowPanel == null){
            mEyebrowPanel = new EyebrowPanel(this);
        }
        return mEyebrowPanel;
    }

    public EyeshadowPanel getEyeshadowPanel() {
        if (mEyeshadowPanel == null){
            mEyeshadowPanel = new EyeshadowPanel(this);
        }
        return mEyeshadowPanel;
    }

    public EyelinerPanel getEyelinerPanel() {
        if (mEyelinerPanel == null){
            mEyelinerPanel = new EyelinerPanel(this);
        }
        return mEyelinerPanel;
    }

    public EyelashPanel getEyelashPanel() {
        if (mEyelashPanel == null){
            mEyelashPanel = new EyelashPanel(this);
        }
        return mEyelashPanel;
    }

    public FacialPanel getFacialPanel(){
        if (mFacialPanel == null){
            mFacialPanel = new FacialPanel(this);
        }
        return mFacialPanel;
    }

    public BasePanel getPanel(CosmeticTypes.Types types){
        BasePanel panel = null;
        switch (types){
            case Lipstick:
                panel = getLipstickPanel();
                break;
            case Blush:
                panel = getBlushPanel();
                break;
            case Eyebrow:
                panel = getEyebrowPanel();
                break;
            case Eyeshadow:
                panel = getEyeshadowPanel();
                break;
            case Eyeliner:
                panel = getEyelinerPanel();
                break;
            case Eyelash:
                panel = getEyelashPanel();
                break;
            case Facial:
                panel = getFacialPanel();
                break;
        }
        return panel;
    }

    private LipstickPanel mLipstickPanel;
    private BlushPanel mBlushPanel;
    private EyebrowPanel mEyebrowPanel;
    private EyeshadowPanel mEyeshadowPanel;
    private EyelinerPanel mEyelinerPanel;
    private EyelashPanel mEyelashPanel;
    private FacialPanel mFacialPanel;


    public Context getContext(){
        return mContext;
    }

    public SelesParameters getEffect(){
        return mEffect;
    }

    public TusdkCosmeticFilter.PropertyBuilder getProperty(){
        return mProperty;
    }

    public Filter getCosmeticFilter(){
        return mCosmeticFilter;
    }

    public void setPanelClickListener(BasePanel.OnPanelClickListener listener){
        getLipstickPanel().setOnPanelClickListener(listener);
        getBlushPanel().setOnPanelClickListener(listener);
        getEyebrowPanel().setOnPanelClickListener(listener);
        getEyeshadowPanel().setOnPanelClickListener(listener);
        getEyelinerPanel().setOnPanelClickListener(listener);
        getEyelashPanel().setOnPanelClickListener(listener);
        getFacialPanel().setOnPanelClickListener(listener);
    }

    public void clearAllCosmetic(){
        for (CosmeticTypes.Types type : CosmeticTypes.Types.values()){
            getPanel(type).clear();
        }
    }

    public void updateProperty(){
        Future<Boolean> res = mRenderPool.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean ret = mCosmeticFilter.setProperty(TusdkCosmeticFilter.PROP_PARAM,mProperty.makeProperty());
                return ret;
            }
        });

        try {
            res.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkMarkSence(){
        int cosmeticEnable = mProperty.blushEnable + mProperty.browEnable + mProperty.eyelashEnable + mProperty.eyelineEnable + mProperty.eyeshadowEnable + mProperty.lipEnable + mProperty.facialEnable;

        return cosmeticEnable>0;
    }

}
