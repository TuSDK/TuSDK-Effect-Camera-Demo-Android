package org.lasque.effectcamerademo.views.record;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tusdk.pulse.Config;
import com.tusdk.pulse.DispatchQueue;
import com.tusdk.pulse.audio.processors.AudioPitchProcessor;
import com.tusdk.pulse.filter.Filter;
import com.tusdk.pulse.filter.FilterDisplayView;
import com.tusdk.pulse.filter.FilterPipe;
import com.tusdk.pulse.filter.filters.AspectRatioFilter;
import com.tusdk.pulse.filter.filters.CropFilter;
import com.tusdk.pulse.filter.filters.TusdkBeautFaceV2Filter;
import com.tusdk.pulse.filter.filters.TusdkFaceMonsterFilter;
import com.tusdk.pulse.filter.filters.TusdkFacePlasticFilter;
import com.tusdk.pulse.filter.filters.TusdkImageFilter;
import com.tusdk.pulse.filter.filters.TusdkLiveStickerFilter;
import com.tusdk.pulse.filter.filters.TusdkReshapeFilter;

import org.lasque.effectcamerademo.MovieRecordFullScreenActivity;
import org.lasque.effectcamerademo.audio.AudioListActivity;
import org.lasque.effectcamerademo.views.props.model.PropsItemMonster;
import org.lasque.effectcamerademo.views.props.model.PropsItemSticker;
import org.lasque.tusdkpulse.core.TuSdk;
import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.TuSdkResult;
import org.lasque.tusdkpulse.core.seles.SelesParameters;
import org.lasque.tusdkpulse.core.seles.tusdk.FilterGroup;
import org.lasque.tusdkpulse.core.seles.tusdk.FilterLocalPackage;
import org.lasque.tusdkpulse.core.seles.tusdk.FilterOption;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.struct.ViewSize;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.core.utils.image.AlbumHelper;
import org.lasque.tusdkpulse.core.utils.image.RatioType;
import org.lasque.tusdkpulse.core.utils.sqllite.ImageSqlHelper;
import org.lasque.tusdkpulse.core.view.TuSdkViewHelper;
import org.lasque.effectcamerademo.R;
import org.lasque.effectcamerademo.utils.Constants;
import org.lasque.effectcamerademo.views.BeautyPlasticRecyclerAdapter;
import org.lasque.effectcamerademo.views.BeautyRecyclerAdapter;
import org.lasque.effectcamerademo.views.FilterConfigSeekbar;
import org.lasque.effectcamerademo.views.FilterRecyclerAdapter;
import org.lasque.effectcamerademo.views.HorizontalProgressBar;
import org.lasque.effectcamerademo.views.ParamsConfigView;
import org.lasque.effectcamerademo.views.TabPagerIndicator;
import org.lasque.effectcamerademo.views.cosmetic.CosmeticPanelController;
import org.lasque.effectcamerademo.views.cosmetic.CosmeticTypes;
import org.lasque.effectcamerademo.views.cosmetic.panel.BasePanel;
import org.lasque.effectcamerademo.views.newFilterUI.FilterFragment;
import org.lasque.effectcamerademo.views.newFilterUI.FilterViewPagerAdapter;
import org.lasque.effectcamerademo.views.props.PropsItemMonsterPageFragment;
import org.lasque.effectcamerademo.views.props.PropsItemPageFragment;
import org.lasque.effectcamerademo.views.props.PropsItemPagerAdapter;
import org.lasque.effectcamerademo.views.props.StickerPropsItemPageFragment;
import org.lasque.effectcamerademo.views.props.model.PropsItem;
import org.lasque.effectcamerademo.views.props.model.PropsItemCategory;
import org.lasque.effectcamerademo.views.props.model.PropsItemMonsterCategory;
import org.lasque.effectcamerademo.views.props.model.PropsItemStickerCategory;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCamera;
import org.lasque.tusdkpulse.cx.hardware.utils.TuCameraAspectRatio;
import org.lasque.tusdkpulse.cx.hardware.utils.TuCameraSizeMap;
import org.lasque.tusdkpulse.impl.view.widget.RegionDefaultHandler;
import org.lasque.tusdkpulse.impl.view.widget.RegionHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Girl;
import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Lolita;
import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Monster;
import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Normal;
import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Uncle;

/**
 * Created by zuojindong on 2018/6/20.
 */

public class RecordView extends RelativeLayout {

    public final static String DEFAULT_FILTER_CODE = "default_filter_code";
    public final static String DEFAULT_FILTER_GROUP = "default_filter_group";

    private final static int mCropIndex = 20;

    public final static HashMap<SelesParameters.FilterModel, Integer> mFilterMap = new HashMap<SelesParameters.FilterModel, Integer>();

    public final static int DOUBLE_VIEW_INDEX = 50;

    static {


        mFilterMap.put(SelesParameters.FilterModel.Reshape, 13);
        mFilterMap.put(SelesParameters.FilterModel.CosmeticFace, 14);
        mFilterMap.put(SelesParameters.FilterModel.MonsterFace, 15);
        mFilterMap.put(SelesParameters.FilterModel.PlasticFace, 16);
        mFilterMap.put(SelesParameters.FilterModel.StickerFace, 17);
        mFilterMap.put(SelesParameters.FilterModel.SkinFace, 18);
        mFilterMap.put(SelesParameters.FilterModel.Filter, 19);


    }

    public FilterDisplayView mCameraView;

    public void setCameraView(FilterDisplayView cameraView) {
        mCameraView = cameraView;
    }

    public enum RecordState {
        Recording, Paused, RecordCompleted, RecordTimeOut, Saving, SaveCompleted;
    }

    public enum DoubleViewMode {
        None, ViewInView, TopBottom, LeftRight;
    }

    /**
     * 录制类型状态
     */
    public interface RecordType {
        // 拍摄
        int CAPTURE = 0;
        // 单击拍摄
        int SHORT_CLICK_RECORD = 2;
        // 短按录制中
        int SHORT_CLICK_RECORDING = 4;
        //合拍
        int DOUBLE_VIEW_RECORD = 5;
    }

    /**
     * 录制视频动作委托
     */
    public interface TuSDKMovieRecordDelegate {
        /**
         * 开始录制视频
         */
        boolean startRecording();

        /**
         * 是否正在录制
         *
         * @return
         */
        boolean isRecording();

        /**
         * 暂停录制视频
         */
        void pauseRecording();

        /**
         * 停止录制视频
         */
        boolean stopRecording();

        /**
         * 关闭录制界面
         */
        void finishRecordActivity();

        void changedAudioEffect(String effect);

        void changedSpeed(double speed);

        void changedRatio(TuSdkSize ratio);

        void changedRect(RectF rectF);

        int getFragmentSize();

        void popFragment();

        void selectVideo();

        void updateDoubleViewMode(DoubleViewMode mode);

        void selectAudio();

        void updateMicState(boolean isOpen);
    }

    public void setDelegate(TuSDKMovieRecordDelegate delegate) {
        mDelegate = delegate;
    }

    public TuSDKMovieRecordDelegate getDelegate() {
        return mDelegate;
    }

    private Context mContext;
    /**
     * 录制视频动作委托
     */
    private TuSDKMovieRecordDelegate mDelegate;
    /**
     * 拍照获得的Bitmap
     */
    private Bitmap mCaptureBitmap;

    private TuSdkResult mCurrentResult;

    private SharedPreferences mFilterValueMap;

    private TuCamera mCamera;

    /******************************* FilterPipe ********************************/

    private FilterPipe mFP;

    private DispatchQueue mRenderPool;

    private HashMap<SelesParameters.FilterModel, Filter> mCurrentFilterMap = new HashMap<>();

    private HashMap<SelesParameters.FilterModel, Object> mPropertyMap = new HashMap<>();

    private Filter mRatioFilter;

    private AspectRatioFilter.PropertyBuilder mRatioProperty = new AspectRatioFilter.PropertyBuilder();

    private DoubleViewMode mCurrentDoubleViewMode = DoubleViewMode.LeftRight;

    /******************************* View ********************************/
    private TuSdkVideoFocusTouchView mFocusTouchView;

    /**
     * 顶部按键
     */
    private LinearLayout mTopBar;
    /**
     * 关闭按键
     */
    private TextView mCloseButton;
    /**
     * 切换摄像头按键
     */
    private TextView mSwitchButton;
    /**
     * 美颜按键
     */
    private TextView mBeautyButton;
    /**
     * 速度按键
     */
    private TextView mSpeedButton;
    /**
     * 更多设置
     */
    private TextView mMoreButton;

    /**
     * 美颜设置
     */
    private LinearLayout mSmartBeautyTabLayout;
    private RecyclerView mBeautyRecyclerView;

    /**
     * 录制进度
     **/
    private HorizontalProgressBar mRecordProgress;
    /**
     * 录制的视频之间的断点
     */
    private RelativeLayout interuptLayout;
    /**
     * 回退按钮
     */
    private TextView mRollBackButton;

    /**
     * 底部功能按键视图
     */
    private LinearLayout mBottomBarLayout;
    /**
     * 录制按键
     */
    private ImageView mRecordButton;
    /**
     * 确认保存视频
     **/
    private TextView mConfirmButton;
    /**
     * 贴纸
     */
    private TextView mStickerWrapButton;
    /**
     * 滤镜
     */
    private TextView mFilterButton;

    /**
     * 视频速度模式视图
     */
    private ViewGroup mSpeedModeBar;
    /**
     * 速度选项是否开启
     */
    private boolean isSpeedChecked = false;

    /**
     * 拍摄模式视图
     */
    private RelativeLayout mRecordModeBarLayout;
    /**
     * 拍照按键
     */
    private TextView mShootButton;
    /**
     * 单击拍摄
     */
    private TextView mClickButton;

    private TextView mDoubleViewButton;

    /**
     * 更多设置视图
     */
    private LinearLayout mMoreConfigLayout;
    /**
     * 自动对焦开关
     */
    private TextView mFocusOpen;
    private TextView mFocusClose;
    /**
     * 闪关灯开关
     */
    private TextView mLightingOpen;
    private TextView mLightingClose;
    /**
     * Radio设置
     */
    private ImageView mRadioFull;
    private ImageView mRadio3_4;
    private ImageView mRadio1_1;
    /**
     * 变声
     */
    private RelativeLayout mChangeAudioLayout;
    private RadioGroup mChangeAudioGroup;

    private RelativeLayout mSimultaneouslyLayer;
    private TextView mTopBottomMode;
    private TextView mLeftRightMode;
    private TextView mViewInViewMode;

    private boolean canChangeLayer = true;


    // 道具布局 贴纸+哈哈镜

    /**
     * 道具布局
     */
    private LinearLayout mPropsItemLayout;
    /**
     * 取消道具
     */
    private ImageView mPropsItemCancel;
    /**
     * 道具 Layout
     */
    private ViewPager2 mPropsItemViewPager;
    /**
     * 道具  PropsItemPagerAdapter
     */
    private PropsItemPagerAdapter<PropsItemPageFragment> mPropsItemPagerAdapter;

    private TabPagerIndicator mPropsItemTabPagerIndicator;
    /**
     * 道具分类类别
     */
    private List<PropsItemCategory> mPropsItemCategories = new ArrayList<>();

    //曝光补偿
    private SeekBar mExposureSeekbar;


    /**
     * 图片预留视图
     **/
    private ImageView mPreViewImageView;
    /**
     * 返回拍照按钮
     **/
    private TextView mBackButton;
    /**
     * 保存按钮
     **/
    private TextView mSaveImageButton;

    private LinearLayout mSelectAudio;
    private TextView mAudioName;

    private TextView mMicOpen;
    private TextView mMicClose;

    private boolean isBeautyClose = false;


    private int mCameraMaxEV = 0;

    private int mCameraMinEV = 0;

    private int mCurrentCameraEV = 0;


    private ViewPager2 mFilterViewPager;
    private TabPagerIndicator mFilterTabIndicator;
    private FilterViewPagerAdapter mFilterViewPagerAdapter;
    private ImageView mFilterReset;
    private boolean isFilterReset = false;

    private List<FilterFragment> mFilterFragments;

    private List<FilterGroup> mFilterGroups;

    public RecordView(Context context) {
        super(context);
    }

    public RecordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected int getLayoutId() {
        return R.layout.record_view;
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this,
                true);

        // TopLayout
        mTopBar = findViewById(R.id.lsq_topBar);
        mCloseButton = findViewById(R.id.lsq_closeButton);
        mSwitchButton = findViewById(R.id.lsq_switchButton);
        mBeautyButton = findViewById(R.id.lsq_beautyButton);
        mSpeedButton = findViewById(R.id.lsq_speedButton);
        mMoreButton = findViewById(R.id.lsq_moreButton);

        mCloseButton.setOnClickListener(onClickListener);
        mSwitchButton.setOnClickListener(onClickListener);
        mBeautyButton.setOnClickListener(onClickListener);
        mSpeedButton.setOnClickListener(onClickListener);
        mMoreButton.setOnClickListener(onClickListener);

        // more_config_layout
        mMoreConfigLayout = findViewById(R.id.lsq_more_config_layout);
        // 自动对焦
        mFocusOpen = findViewById(R.id.lsq_focus_open);
        mFocusClose = findViewById(R.id.lsq_focus_close);
        mFocusOpen.setOnClickListener(onClickListener);
        mFocusClose.setOnClickListener(onClickListener);
        // 闪光灯
        mLightingOpen = findViewById(R.id.lsq_lighting_open);
        mLightingClose = findViewById(R.id.lsq_lighting_close);
        mLightingOpen.setOnClickListener(onClickListener);
        mLightingClose.setOnClickListener(onClickListener);
        // 比例
        mRadioFull = findViewById(R.id.lsq_radio_full);
        mRadio3_4 = findViewById(R.id.lsq_radio_3_4);
        mRadio1_1 = findViewById(R.id.lsq_radio_1_1);
        mRadioFull.setOnClickListener(onClickListener);
        mRadio3_4.setOnClickListener(onClickListener);
        mRadio1_1.setOnClickListener(onClickListener);
        // 变声
        mChangeAudioLayout = findViewById(R.id.lsq_audio_layout);
        mChangeAudioGroup = findViewById(R.id.lsq_audio_group);
        mChangeAudioGroup.setOnCheckedChangeListener(mAudioOnCheckedChangeListener);

        mSimultaneouslyLayer = findViewById(R.id.lsq_simultaneously_layer);
        mTopBottomMode = findViewById(R.id.lsq_top_bottom);
        mLeftRightMode = findViewById(R.id.lsq_left_right);
        mViewInViewMode = findViewById(R.id.lsq_view_in_view);
        mTopBottomMode.setOnClickListener(mOnSimultaneouslyModeChanged);
        mLeftRightMode.setOnClickListener(mOnSimultaneouslyModeChanged);
        mViewInViewMode.setOnClickListener(mOnSimultaneouslyModeChanged);

        mMicOpen = findViewById(R.id.lsq_mic_open);
        mMicOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) mDelegate.updateMicState(true);

                mMicOpen.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                mMicClose.setTextColor(getResources().getColor(R.color.lsq_color_white));
            }
        });
        mMicClose = findViewById(R.id.lsq_mic_close);
        mMicClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) mDelegate.updateMicState(false);

                mMicClose.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                mMicOpen.setTextColor(getResources().getColor(R.color.lsq_color_white));
            }
        });

        // 底部功能按键视图
        mBottomBarLayout = findViewById(R.id.lsq_button_wrap_layout);
        // 贴纸按键
        mStickerWrapButton = findViewById(R.id.lsq_stickerWrap);
        mStickerWrapButton.setOnClickListener(onClickListener);
        // 滤镜按键
        mFilterButton = findViewById(R.id.lsq_tab_filter_btn);
        mFilterButton.setOnClickListener(onClickListener);
        // 保存视频
        mConfirmButton = findViewById(R.id.lsq_confirmWrap);
        mConfirmButton.setOnClickListener(onClickListener);
        // 录制按钮
        mRecordButton = findViewById(R.id.lsq_recordButton);
        mRecordButton.setOnTouchListener(onTouchListener);

        // 模式切换视图
        mRecordModeBarLayout = findViewById(R.id.lsq_record_mode_bar_layout);
        mRecordModeBarLayout.setOnTouchListener(onModeBarTouchListener);

        // 录制进度条
        mRecordProgress = findViewById(R.id.lsq_record_progressbar);
        Button minTimeButton = (Button) findViewById(R.id.lsq_minTimeBtn);
        LayoutParams minTimeLayoutParams = (LayoutParams) minTimeButton.getLayoutParams();
        minTimeLayoutParams.leftMargin = (int) (((float) Constants.MIN_RECORDING_TIME * TuSdkContext.getScreenSize().width) / Constants.MAX_RECORDING_TIME)
                - TuSdkContext.dip2px(minTimeButton.getWidth());
        // 一进入录制界面就显示最小时长标记
        interuptLayout = (RelativeLayout) findViewById(R.id.interuptLayout);
        // 回退按钮
        mRollBackButton = (TextView) findViewById(R.id.lsq_backWrap);
        mRollBackButton.setOnClickListener(onClickListener);

        // 模式切换
        mShootButton = findViewById(R.id.lsq_shootButton);
        mClickButton = findViewById(R.id.lsq_clickButton);
        mDoubleViewButton = findViewById(R.id.lsq_double_view_Button);
        mShootButton.setOnTouchListener(onModeBarTouchListener);
        mClickButton.setOnTouchListener(onModeBarTouchListener);
        mDoubleViewButton.setOnTouchListener(onModeBarTouchListener);

        // PreviewLayout
        mBackButton = findViewById(R.id.lsq_backButton);
        mBackButton.setOnClickListener(onClickListener);
        mSaveImageButton = findViewById(R.id.lsq_saveImageButton);
        mSaveImageButton.setOnClickListener(onClickListener);
        mPreViewImageView = findViewById(R.id.lsq_cameraPreviewImageView);
        mPreViewImageView.setOnClickListener(onClickListener);

        mSelectAudio = findViewById(R.id.lsq_select_audio);
        mSelectAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.selectAudio();
                }
            }
        });

        mAudioName = findViewById(R.id.lsq_audio_name);

        // 速度控制条
        mSpeedModeBar = findViewById(R.id.lsq_movie_speed_bar);
        int childCount = mSpeedModeBar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            mSpeedModeBar.getChildAt(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectSpeedMode(Double.parseDouble((String) view.getTag()));
                }
            });
        }

        // 美颜Bar
        mSmartBeautyTabLayout = findViewById(R.id.lsq_smart_beauty_layout);
        setBeautyLayout(false);
        mBeautyRecyclerView = findViewById(R.id.lsq_beauty_recyclerView);
        mBeautyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 美颜类型
        mBeautyRecyclerAdapter = new BeautyRecyclerAdapter(getContext());
        mBeautyRecyclerAdapter.setOnSkinItemClickListener(beautyItemClickListener);
        // 微整形
        mBeautyPlasticRecyclerAdapter = new BeautyPlasticRecyclerAdapter(getContext(), mBeautyPlastics);
        mBeautyPlasticRecyclerAdapter.setOnBeautyPlasticItemClickListener(beautyPlasticItemClickListener);

        // 美妆
        mController = new CosmeticPanelController(getContext());
        initCosmeticView();

        // 滤镜调节
        mFilterConfigView = findViewById(R.id.lsq_filter_config_view);
        mFilterConfigView.setSeekBarDelegate(mFilterConfigViewSeekBarDelegate);
        // 微整形调节
        mBeautyPlasticsConfigView = findViewById(R.id.lsq_beauty_plastics_config_view);
        mBeautyPlasticsConfigView.setPrefix("lsq_beauty_");
        mBeautyPlasticsConfigView.setSeekBarDelegate(mBeautyPlasticConfigViewSeekBarDelegate);


        //曝光补偿控制
        mExposureSeekbar = findViewById(R.id.lsq_exposure_compensation_seek);
        mExposureSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (!fromUser) return;
                mCurrentCameraEV = progress - mCameraMaxEV;
                mCamera.cameraParams().setExposureCompensation(mCurrentCameraEV);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFilterValueMap = getContext().getSharedPreferences("TUTUFilter", Context.MODE_PRIVATE);

        initFilterRecyclerView();
        initStickerLayout();

    }

    public DoubleViewMode getCurrentDoubleViewMode() {
        return mCurrentDoubleViewMode;
    }

    private String mCurrentFilterCode = "";

    public TuSdkSize mCurrentRatio;

    /**
     * @param filterPipe
     * @param renderPool
     */
    public void initFilterPipe(FilterPipe filterPipe, DispatchQueue renderPool) {
        mFP = filterPipe;
        mRenderPool = renderPool;
        mController.initCosmetic(mFP, mRenderPool);

//        Future<Boolean> res = mRenderPool.submit(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                mRatioFilter = new Filter(mFP.getContext(), AspectRatioFilter.TYPE_NAME);
//                boolean ret = mFP.addFilter(mCropIndex, mRatioFilter);
//                TuSdkSize size = TuSdkContext.getScreenSize();
//                TLog.e("screen size %s",size.toString());
//                mRatioProperty.widthRatio = size.width;
//                mRatioProperty.heightRatio = size.height;
//                mCurrentRatio = TuSdkSize.create(size);
//                mRatioFilter.setProperty(AspectRatioFilter.PROP_PARAM,mRatioProperty.makeProperty());
//                return ret;
//            }
//        });
//
//        try {
//            res.get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        initPlastic();

        switchConfigSkin(Constants.SkinMode.SkinMoist);
    }


    public void initFilterGroupsViews(FragmentManager fragmentManager, Lifecycle lifecycle, List<FilterGroup> filterGroups) {
        mFilterGroups = filterGroups;
        mFilterReset = findViewById(R.id.lsq_filter_reset);
        mFilterReset.setOnClickListener(new TuSdkViewHelper.OnSafeClickListener() {
            @Override
            public void onSafeClick(View view) {
                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.Filter));
                    }
                });
                mFilterFragments.get(mFilterTabIndicator.getCurrentPosition()).removeFilter();
                mFilterConfigView.setVisibility(View.GONE);
                mFilterViewPagerAdapter.notifyDataSetChanged();
                mCurrentFilterCode = "";
                mFilterValueMap.edit().remove(DEFAULT_FILTER_CODE).apply();
                mFilterValueMap.edit().remove(DEFAULT_FILTER_GROUP).apply();
                isFilterReset = true;

            }
        });

        mFilterTabIndicator = findViewById(R.id.lsq_filter_tabIndicator);

        mFilterViewPager = findViewById(R.id.lsq_filter_view_pager);
        mFilterViewPager.requestDisallowInterceptTouchEvent(true);
        List<String> tabTitles = new ArrayList<>();
        List<FilterFragment> fragments = new ArrayList<>();
        for (FilterGroup group : mFilterGroups) {
            if (group == null){
                continue;
            }
            FilterFragment fragment = FilterFragment.newInstance(group);
            if (group.groupId == 252) {
                fragment.setOnFilterItemClickListener(new FilterFragment.OnFilterItemClickListener() {
                    @Override
                    public void onFilterItemClick(String code, int position) {
                        mCurrentFilterCode = code;
                        mCurrentPosition = position;
                        //设置滤镜
                        changeVideoComicEffectCode(mCurrentFilterCode);
                    }
                });
            } else {
                fragment.setOnFilterItemClickListener(new FilterFragment.OnFilterItemClickListener() {
                    @Override
                    public void onFilterItemClick(String code, int position) {
                        if (TextUtils.equals(mCurrentFilterCode, code)) {
                            mFilterConfigView.setVisibility(mFilterConfigView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                        } else {
                            mCurrentFilterCode = code;
                            mCurrentPosition = position;
                            //设置滤镜
                            changeVideoFilterCode(mCurrentFilterCode);
                        }
                    }
                });
            }

            fragments.add(fragment);
            tabTitles.add(group.getName());
        }
        mFilterFragments = fragments;
        mFilterViewPagerAdapter = new FilterViewPagerAdapter(fragmentManager, lifecycle, fragments);
        mFilterViewPager.setAdapter(mFilterViewPagerAdapter);
        mFilterTabIndicator.setViewPager(mFilterViewPager, 0);
        mFilterTabIndicator.setDefaultVisibleCounts(tabTitles.size());
        mFilterTabIndicator.setTabItems(tabTitles);


    }

    /**
     * 初始化进度
     */
    public void initRecordProgress() {
        mRecordProgress.clearProgressList();
        interuptLayout.removeAllViews();
        if (mBottomBarLayout.getVisibility() == VISIBLE)
            setViewHideOrVisible(true);
    }

    /**
     * 传递录制相机对象
     *
     * @param camera
     */
    public void setUpCamera(Context context, TuCamera camera) {
        this.mContext = context;
        this.mCamera = camera;

        getFocusTouchView();
    }

    public void setExposure() {
        mCameraMaxEV = mCamera.cameraParams().getMaxExposureCompensation();
        mCameraMinEV = mCamera.cameraParams().getMinExposureCompensation();
        mExposureSeekbar.setMax(mCameraMaxEV + Math.abs(mCameraMinEV));
        mExposureSeekbar.setProgress(mCameraMaxEV);
    }

    /**
     * 录制按键
     */
    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (getDelegate() == null) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (TuSdkViewHelper.isFastDoubleClick()) return false;
                    return true;
                case MotionEvent.ACTION_UP:
                    // 点击拍照
                    if (mRecordMode == RecordType.CAPTURE) {
                        //todo 拍照
                        mCamera.shotPhoto();
                    }
                    // 点击录制
                    else if (mRecordMode == RecordType.SHORT_CLICK_RECORD || mRecordMode == RecordType.DOUBLE_VIEW_RECORD) {
                        // 是否录制中
                        if (getDelegate().isRecording()) {
                            getDelegate().pauseRecording();
                            updateRecordButtonResource(RecordType.SHORT_CLICK_RECORD);
                        } else {
                            //todo 录制
                            setViewHideOrVisible(false);
                            if (getDelegate().startRecording()) {
                                updateRecordButtonResource(RecordType.SHORT_CLICK_RECORDING);
                            }
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }
    };

    /**
     * 变声切换
     */
    RadioGroup.OnCheckedChangeListener mAudioOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.lsq_audio_normal:
                    getDelegate().changedAudioEffect(AudioPitchProcessor.TYPE_NORMAL);
                    // 正常
                    break;
                case R.id.lsq_audio_monster:
                    getDelegate().changedAudioEffect(AudioPitchProcessor.TYPE_MONSTER);
                    // 怪兽
                    break;
                case R.id.lsq_audio_uncle:
                    getDelegate().changedAudioEffect(AudioPitchProcessor.TYPE_UNCLE);
                    // 大叔
                    break;
                case R.id.lsq_audio_girl:
                    getDelegate().changedAudioEffect(AudioPitchProcessor.TYPE_GIRL);
                    // 女生
                    break;
                case R.id.lsq_audio_lolita:
                    getDelegate().changedAudioEffect(AudioPitchProcessor.TYPE_LOLITA);
                    // 萝莉
                    break;
            }
        }
    };

    /**
     * 属性动画监听事件
     */
    private ViewPropertyAnimatorListener mViewPropertyAnimatorListener = new ViewPropertyAnimatorListener() {

        @Override
        public void onAnimationCancel(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {
            ViewCompat.animate(mPropsItemLayout).setListener(null);
            ViewCompat.animate(mFilterContent).setListener(null);
        }

        @Override
        public void onAnimationStart(View view) {
        }
    };

    /******************************** 滤镜 ********************************************/
    /**
     * 默认选中滤镜
     */
    private static final int DEFAULT_POSITION = 1;
    /**
     * 滤镜视图
     */
    private RelativeLayout mFilterContent;
    /**
     * 参数调节视图
     */
    protected ParamsConfigView mFilterConfigView;
    /**
     * 滤镜列表
     */
    private RecyclerView mFilterRecyclerView;
    /**
     * 滤镜列表Adapter
     */
    private FilterRecyclerAdapter mFilterAdapter;
    /**
     * 滤镜列表
     */
    private RecyclerView mComicsFilterRecyclerView;
    /**
     * 滤镜列表Adapter
     */
    private FilterRecyclerAdapter mComicsFilterAdapter;
    /**
     * 用于记录上一次位置
     */
    private int mCurrentPosition = DEFAULT_POSITION;
    /**
     * 用于记录上一次位置
     */
    private int mComicsCurrentPosition = 0;
    /**
     * 滤镜名称
     */
    private TextView mFilterNameTextView;
    /**
     * 是否切换漫画滤镜
     */
    private boolean isComicsFilterChecked = false;

    /**
     * 初始化滤镜
     */
    private void initFilterRecyclerView() {
        mFilterNameTextView = findViewById(R.id.lsq_filter_name);
        mFilterContent = findViewById(R.id.lsq_filter_content);
        /** 屏蔽在滤镜栏显示时 在滤镜栏上面的手势操作  如不需要 可删除*/
        mFilterContent.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        setFilterContentVisible(false);

    }

    /**
     * 显示滤镜列表
     */
    private void showFilterLayout() {
        // 滤镜栏向上动画并显示
        ViewCompat.setTranslationY(mFilterContent,
                mFilterContent.getHeight());
        ViewCompat.animate(mFilterContent).translationY(0).setDuration(200).setListener(mViewPropertyAnimatorListener);

        setFilterContentVisible(true);

        // 设置滤镜参数调节
        if (mCurrentPosition > 0 && mFilterConfigView != null) {
            mFilterConfigView.invalidate();
        }
    }

    /**
     * 滤镜调节栏
     */
    private ParamsConfigView.FilterConfigViewSeekBarDelegate mFilterConfigViewSeekBarDelegate = new ParamsConfigView.FilterConfigViewSeekBarDelegate() {
        @Override
        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {
            float progress = seekbar.getSeekbar().getProgress();
            mFilterValueMap.edit().putFloat(mCurrentFilterCode, progress).apply();
        }
    };

    private void setDefaultFilter() {
        mFilterViewPager.setCurrentItem(0);
        int filterViewPagerPos = 0;
        mFilterGroups = Constants.getCameraFilters(true);
        String defaultCode = mFilterValueMap.getString(DEFAULT_FILTER_CODE, "");
        if (TextUtils.isEmpty(defaultCode)) {
            return;
        }
        long defaultFilterGroupId = mFilterValueMap.getLong(DEFAULT_FILTER_GROUP, -1);
        List<FilterOption> defaultFilters = mFilterGroups.get(0).filters;
        FilterGroup defaultGroup = mFilterGroups.get(0);
        if (defaultFilterGroupId != -1) {
            for (FilterGroup group : mFilterGroups) {
                if (group.groupId == defaultFilterGroupId) {
                    defaultFilters = group.filters;
                    defaultGroup = group;
                    filterViewPagerPos = mFilterGroups.indexOf(group);
                    break;
                }
            }
        }
        for (int i = 0; i < defaultFilters.size(); i++) {
            if (defaultFilters.get(i).code.equals(defaultCode)) {
                mCurrentPosition = i;
                break;
            }
        }
        mCurrentFilterCode = defaultCode;
        mFilterViewPager.setCurrentItem(filterViewPagerPos, false);
        final FilterGroup finalDefaultGroup = defaultGroup;
        ThreadHelper.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (finalDefaultGroup.groupId == 252) {
                    changeVideoComicEffectCode(mCurrentFilterCode);
                } else {
                    changeVideoFilterCode(mCurrentFilterCode);
                }
            }
        }, 1000);
    }

    /**
     * 滤镜组列表点击事件
     */
    private FilterRecyclerAdapter.ItemClickListener mFilterItemClickListener = new FilterRecyclerAdapter.ItemClickListener() {
        @Override
        public void onItemClick(int position) {
            mFilterConfigView.setVisibility((position == 0) ? INVISIBLE :
                    ((mCurrentPosition == position) ? (mFilterConfigView.getVisibility() == VISIBLE ? INVISIBLE : VISIBLE)
                            : INVISIBLE));
            if (mCurrentPosition == position) return;
            mCurrentPosition = position;
            changeVideoFilterCode(mFilterAdapter.getFilterList().get(position));
        }
    };

    /**
     * 漫画滤镜组列表点击事件
     */
    private FilterRecyclerAdapter.ItemClickListener mComicsFilterItemClickListener = new FilterRecyclerAdapter.ItemClickListener() {
        @Override
        public void onItemClick(int position) {
            mComicsCurrentPosition = position;
            changeVideoComicEffectCode(mComicsFilterAdapter.getFilterList().get(position));
        }
    };

    /**
     * 切换滤镜
     *
     * @param code
     */
    protected void changeVideoFilterCode(final String code) {
        isFilterReset = false;
        SelesParameters selesParameters = new SelesParameters(code, SelesParameters.FilterModel.Filter);

        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                int filterIndex = mFilterMap.get(SelesParameters.FilterModel.Filter);
                mFP.deleteFilter(filterIndex);
                List<FilterOption> options = FilterLocalPackage.shared().getFilters(Arrays.asList(code));
                if (options.size() > 0) {
                    FilterOption option = options.get(0);
                    for (String arg : option.args.keySet()) {
                        selesParameters.appendFloatArg(arg, Float.parseFloat(option.args.get(arg)));
                    }
                }
                FilterOption option = options.get(0);
                double value = Double.parseDouble(option.args.get("mixied"));
                Filter filter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                Config config = new Config();
                config.setString(TusdkImageFilter.CONFIG_NAME, code);
                filter.setConfig(config);
                TusdkImageFilter.MixedPropertyBuilder mFilterProperty = new TusdkImageFilter.MixedPropertyBuilder();
                mFilterProperty.strength = value;
                mPropertyMap.put(SelesParameters.FilterModel.Filter, mFilterProperty);
                boolean ret = mFP.addFilter(filterIndex, filter);
                filter.setProperty(TusdkImageFilter.PROP_PARAM, mFilterProperty.makeProperty());
                mCurrentFilterMap.put(SelesParameters.FilterModel.Filter, filter);
            }
        });
        selesParameters.setListener(new SelesParameters.SelesParametersListener() {
            @Override
            public void onUpdateParameters(SelesParameters.FilterModel model, String code, SelesParameters.FilterArg arg) {

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        TusdkImageFilter.MixedPropertyBuilder mFilterProperty = (TusdkImageFilter.MixedPropertyBuilder) mPropertyMap.get(SelesParameters.FilterModel.Filter);
                        mFilterProperty.strength = arg.getPrecentValue();
                        boolean ret = mCurrentFilterMap.get(SelesParameters.FilterModel.Filter).setProperty(TusdkImageFilter.PROP_PARAM, mFilterProperty.makeProperty());
                    }
                });
            }
        });
        mFilterConfigView.setFilterArgs(selesParameters.getArgs());

        mFilterValueMap.edit().putString(DEFAULT_FILTER_CODE, code).apply();
        mFilterValueMap.edit().putLong(DEFAULT_FILTER_GROUP, mFilterGroups.get(mFilterViewPager.getCurrentItem()).groupId).apply();
        if (mFilterTabIndicator.getCurrentPosition() != -1) {
            for (int i = 0; i < mFilterFragments.size(); i++) {
                if (i == mFilterTabIndicator.getCurrentPosition()) {
                    mFilterFragments.get(i).setCurrentPosition(mCurrentPosition);
                } else {
                    mFilterFragments.get(i).setCurrentPosition(-1);
                }
            }
        }
        // 滤镜名显示
        showHitTitle(TuSdkContext.getString("lsq_filter_" + code));
    }


    /**
     * 显示提示文字
     *
     * @param title
     */
    private void showHitTitle(String title) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFilterNameTextView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFilterNameTextView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFilterNameTextView.setText(title);
        mFilterNameTextView.setAnimation(alphaAnimation);
        alphaAnimation.setDuration(2000);
        alphaAnimation.start();
    }

    /**
     * TouchView滑动监听
     */
    private TuSdkVideoFocusTouchViewBase.GestureListener gestureListener = new TuSdkVideoFocusTouchViewBase.GestureListener() {
        @Override
        public void onLeftGesture() {
            // 美颜开启禁止滑动切换
            if (mSmartBeautyTabLayout.getVisibility() == VISIBLE) return;

            FilterGroup current = mFilterGroups.get(mFilterTabIndicator.getCurrentPosition());
            final String filterCode;
            if (mCurrentPosition == current.filters.size() - 1) {
                int targetViewPagerPos = mFilterTabIndicator.getCurrentPosition() + 1 == mFilterFragments.size() ? 0 : mFilterTabIndicator.getCurrentPosition() + 1;
                current = mFilterGroups.get(targetViewPagerPos);
                mFilterViewPager.setCurrentItem(targetViewPagerPos);
                mCurrentPosition = 0;
            } else {
                ++mCurrentPosition;
            }
            filterCode = current.filters.get(mCurrentPosition).code;
            mCurrentFilterCode = filterCode;
            final FilterGroup finalCurrent = current;
            ThreadHelper.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (finalCurrent.groupId == 252) {
                        changeVideoComicEffectCode(filterCode);
                    } else {
                        changeVideoFilterCode(filterCode);
                    }
                }
            }, 100);


        }

        @Override
        public void onRightGesture() {
            // 美颜开启禁止滑动切换
            if (mSmartBeautyTabLayout.getVisibility() == VISIBLE) return;

            FilterGroup current = mFilterGroups.get(mFilterTabIndicator.getCurrentPosition());
            final String filterCode;
            if (mCurrentPosition == 0) {
                int targetViewPagerPos = mFilterTabIndicator.getCurrentPosition() - 1 == -1 ? mFilterFragments.size() - 1 : mFilterTabIndicator.getCurrentPosition() - 1;
                current = mFilterGroups.get(targetViewPagerPos);
                mFilterViewPager.setCurrentItem(targetViewPagerPos);
                mCurrentPosition = current.filters.size() - 1;
            } else {
                --mCurrentPosition;
            }
            filterCode = current.filters.get(mCurrentPosition).code;
            mCurrentFilterCode = filterCode;
            final FilterGroup finalCurrent = current;
            ThreadHelper.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (finalCurrent.groupId == 252) {
                        changeVideoComicEffectCode(filterCode);
                    } else {
                        changeVideoFilterCode(filterCode);
                    }
                }
            }, 100);
        }

        @Override
        public void onClick() {
            if (!isRecording()) {
                mExposureSeekbar.setProgress(mCameraMaxEV);
                setFilterContentVisible(false);
                setBeautyViewVisible(false);
                setBottomViewVisible(true);
                setStickerVisible(false);
                mMoreConfigLayout.setVisibility(GONE);
                setTextButtonDrawableTop(mMoreButton, R.drawable.video_nav_ic_more);
                mPropsItemViewPager.getAdapter().notifyDataSetChanged();
                getFocusTouchView().isShowFoucusView(true);
            }
        }
    };

    private RegionHandler mRegionHandle = new RegionDefaultHandler();

    private TuSdkVideoFocusTouchView getFocusTouchView() {
        if (mFocusTouchView == null) {
            mFocusTouchView = findViewById(R.id.lsq_focus_touch_view);
            mFocusTouchView.setCamera(mCamera);

            mFocusTouchView.setRegionHandler(mRegionHandle);
            mFocusTouchView.setGestureListener(gestureListener);
        }
        return mFocusTouchView;
    }

    public void setDisplaySize(int width, int height) {
        TLog.e("Surface Size %s || %s", width, height);
    }

    private boolean isRecording() {
        return false;
    }

    public void setWrapSize(TuSdkSize wrapSize) {
        mRegionHandle.setWrapSize(wrapSize);
    }

    /********************** 动漫 ****************************/

    /**
     * 切换漫画滤镜
     *
     * @param code
     */
    protected void changeVideoComicEffectCode(final String code) {
        isFilterReset = false;

        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                int filterIndex = mFilterMap.get(SelesParameters.FilterModel.Filter);
                mFP.deleteFilter(filterIndex);
                Filter filter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                Config config = new Config();
                config.setString(TusdkImageFilter.CONFIG_NAME, code);
                filter.setConfig(config);
                TusdkImageFilter.MixedPropertyBuilder mFilterProperty = new TusdkImageFilter.MixedPropertyBuilder();
                mPropertyMap.put(SelesParameters.FilterModel.Filter, mFilterProperty);
                mFP.addFilter(filterIndex, filter);
            }
        });
        mFilterValueMap.edit().putString(DEFAULT_FILTER_CODE, code).apply();
        mFilterValueMap.edit().putLong(DEFAULT_FILTER_GROUP, mFilterGroups.get(mFilterViewPager.getCurrentItem()).groupId).apply();
        if (mFilterTabIndicator.getCurrentPosition() != -1) {
            for (int i = 0; i < mFilterFragments.size(); i++) {
                if (i == mFilterTabIndicator.getCurrentPosition()) {
                    mFilterFragments.get(i).setCurrentPosition(mCurrentPosition);
                } else {
                    mFilterFragments.get(i).setCurrentPosition(-1);
                }
            }
        }
        mFilterConfigView.setVisibility(View.GONE);
        // 滤镜名显示
        showHitTitle(TuSdkContext.getString("lsq_filter_" + code));
    }

    /******************************* 贴纸 **************************/
    /**
     * 初始化贴纸
     */
    private void initStickerLayout() {
        mPropsItemViewPager = findViewById(R.id.lsq_viewPager);
        mPropsItemTabPagerIndicator = findViewById(R.id.lsq_TabIndicator);

        mPropsItemCancel = findViewById(R.id.lsq_cancel_button);
        mPropsItemCancel.setOnClickListener(onClickListener);

        // 贴纸视图
        mPropsItemLayout = findViewById(R.id.lsq_sticker_layout);
        setStickerVisible(false);
    }

    /**
     * 设置贴纸视图
     *
     * @param isVisible 是否可见
     */
    private void setStickerVisible(boolean isVisible) {
        mPropsItemLayout.setVisibility(isVisible ? VISIBLE : INVISIBLE);
    }

    /**
     * 显示贴纸视图
     */
    private void showStickerLayout() {
        setStickerVisible(true);
        // 滤镜栏向上动画并显示
        ViewCompat.setTranslationY(mPropsItemLayout,
                mPropsItemLayout.getHeight());
        ViewCompat.animate(mPropsItemLayout).translationY(0).setDuration(200).setListener(mViewPropertyAnimatorListener);
    }

    /**
     * 选择贴纸道具物品后回调
     */
    private StickerPropsItemPageFragment.StickerItemDelegate mStickerPropsItemDelegate = new StickerPropsItemPageFragment.StickerItemDelegate() {
        /**
         * 移除道具
         * @param propsItem
         */

        private boolean removeRes = false;

        private boolean selectRes = false;

        @Override
        public void removePropsItem(PropsItem propsItem) {
            if (propsItemUsed(propsItem)) {
                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        int stickerIndex = mFilterMap.get(SelesParameters.FilterModel.StickerFace);
                        removeRes = mFP.deleteFilter(stickerIndex);
                    }
                });

                if (removeRes) {
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.StickerFace);
                }
            }
        }

        private long mCurrentGroupId = 0l;

        @Override
        public void didSelectPropsItem(PropsItem propsItem) {
            mPropsItemPagerAdapter.notifyAllPageData();

            mRenderPool.runSync(new Runnable() {
                @Override
                public void run() {
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.MonsterFace);
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.MonsterFace));
                    int stickerIndex = mFilterMap.get(SelesParameters.FilterModel.StickerFace);
                    mFP.deleteFilter(stickerIndex);
                    Filter sticker = new Filter(mFP.getContext(), TusdkLiveStickerFilter.TYPE_NAME);
                    Config config = new Config();
                    config.setNumber(TusdkLiveStickerFilter.CONFIG_ID, ((PropsItemSticker) propsItem).getStickerGroup().groupId);
                    sticker.setConfig(config);
                    mCurrentFilterMap.put(SelesParameters.FilterModel.StickerFace, sticker);
                    selectRes = mFP.addFilter(stickerIndex, sticker);
                }
            });
            if (selectRes) {
                mCurrentGroupId = ((PropsItemSticker) propsItem).getStickerGroup().groupId;
            }
        }

        /**
         * 当前道具是否正在被使用
         *
         * @param propsItem 道具
         * @return
         */
        @Override
        public boolean propsItemUsed(PropsItem propsItem) {
            if (mCurrentFilterMap.get(SelesParameters.FilterModel.StickerFace) == null)
                return false;
            long groupId = ((PropsItemSticker) propsItem).getStickerGroup().groupId;
            return mCurrentGroupId == groupId;
        }
    };

    /**
     * 选择道具物品后回调
     */
    private PropsItemPageFragment.ItemDelegate mPropsItemDelegate = new PropsItemPageFragment.ItemDelegate() {
        @Override
        public void didSelectPropsItem(PropsItem propsItem) {
            hideBeautyBarLayout();
            mBeautyPlasticRecyclerAdapter.clearSelect();

            mRenderPool.runSync(new Runnable() {
                @Override
                public void run() {
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.MonsterFace);
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.StickerFace);
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.PlasticFace);
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.Reshape);
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.PlasticFace));
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.Reshape));
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.MonsterFace));
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.StickerFace));
                    Filter filter = new Filter(mFP.getContext(), TusdkFaceMonsterFilter.TYPE_NAME);
                    Config config = new Config();
                    config.setString(TusdkFaceMonsterFilter.CONFIG_TYPE, ((PropsItemMonster) propsItem).getMonsterCode());
                    filter.setConfig(config);
                    boolean ret = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.MonsterFace), filter);
                    mCurrentFilterMap.put(SelesParameters.FilterModel.MonsterFace, filter);
                }
            });
            mPropsItemPagerAdapter.notifyAllPageData();
        }

        /**
         * 当前道具是否正在被使用
         *
         * @param propsItem 道具
         * @return
         */
        @Override
        public boolean propsItemUsed(PropsItem propsItem) {
            if (mCurrentFilterMap.get(SelesParameters.FilterModel.MonsterFace) == null)
                return false;
            boolean res = mCurrentFilterMap.get(SelesParameters.FilterModel.MonsterFace).getConfig().getString(TusdkFaceMonsterFilter.CONFIG_TYPE).equals(((PropsItemMonster) propsItem).getMonsterCode());
            return res;
        }


    };

    /**
     * 设置贴纸适配器
     */
    public void init(final FragmentManager fm, final Lifecycle lifecycle) {

        // 添加贴纸道具分类数据
        mPropsItemCategories.addAll(PropsItemStickerCategory.allCategories());

        // 添加哈哈镜道具分类
        mPropsItemCategories.addAll(PropsItemMonsterCategory.allCategories());

        mPropsItemPagerAdapter = new PropsItemPagerAdapter(fm, lifecycle, new PropsItemPagerAdapter.DataSource() {
            @Override
            public Fragment frament(int pageIndex) {

                PropsItemCategory category = mPropsItemCategories.get(pageIndex);

                switch (category.getMediaEffectType()) {
                    case StickerFace: {
                        StickerPropsItemPageFragment fragment = new StickerPropsItemPageFragment(pageIndex, mPropsItemCategories.get(pageIndex).getItems());
                        fragment.setItemDelegate(mStickerPropsItemDelegate);
                        return fragment;
                    }
                    default: {
                        PropsItemMonsterPageFragment fragment = new PropsItemMonsterPageFragment(pageIndex, mPropsItemCategories.get(pageIndex).getItems());
                        fragment.setItemDelegate(mPropsItemDelegate);
                        return fragment;
                    }
                }

            }

            @Override
            public int pageCount() {
                return mPropsItemCategories.size();
            }
        });

        mPropsItemViewPager.setAdapter(mPropsItemPagerAdapter);

        mPropsItemTabPagerIndicator.setViewPager(mPropsItemViewPager, 0);
        mPropsItemTabPagerIndicator.setDefaultVisibleCounts(mPropsItemCategories.size());


        List<String> itemTitles = new ArrayList<>();
        for (PropsItemCategory category : mPropsItemCategories)
            itemTitles.add(category.getName());


        mPropsItemTabPagerIndicator.setTabItems(itemTitles);

//        initFilterGroupsViews(fm,lifecycle);
    }

    /*********************************** 美妆 ********************* */

    private RelativeLayout mCosmeticList;

    private LinearLayout mLipstick, mBlush, mEyebrow, mEyeshadow, mEyeliner, mEyelash, mFacial, mCosmeticClear;
    private RelativeLayout mLipstickPanel, mBlushPanel, mEyebrowPanel, mEyeshadowPanel, mEyelinerPanel, mEyelashPanel, mFacialPanel;

    private CosmeticPanelController mController;

    private CosmeticTypes.Types mCurrentType;

    private HashSet<CosmeticTypes.Types> types = new HashSet<>();

    private int lastPos = -1;

    private BasePanel.OnPanelClickListener panelClickListener = new BasePanel.OnPanelClickListener() {
        @Override
        public void onClear(CosmeticTypes.Types type) {
            int viewID = -1;
            switch (type) {
                case Lipstick:
                    viewID = R.id.lsq_lipstick_add;
                    break;
                case Blush:
                    viewID = R.id.lsq_blush_add;
                    break;
                case Eyebrow:
                    viewID = R.id.lsq_eyebrow_add;
                    break;
                case Eyeshadow:
                    viewID = R.id.lsq_eyeshadow_add;
                    break;
                case Eyeliner:
                    viewID = R.id.lsq_eyeliner_add;
                    break;
                case Eyelash:
                    viewID = R.id.lsq_eyelash_add;
                    break;
                case Facial:
                    viewID = R.id.lsq_facial_add;
                    break;
            }
            findViewById(viewID).setVisibility(View.GONE);
            mBeautyPlasticsConfigView.setVisibility(View.GONE);
            types.remove(type);
        }

        @Override
        public void onClose(CosmeticTypes.Types type) {
            switch (type) {
                case Lipstick:
                    mLipstickPanel.setVisibility(View.GONE);
                    break;
                case Blush:
                    mBlushPanel.setVisibility(View.GONE);
                    break;
                case Eyebrow:
                    mEyebrowPanel.setVisibility(View.GONE);
                    break;
                case Eyeshadow:
                    mEyeshadowPanel.setVisibility(View.GONE);
                    break;
                case Eyeliner:
                    mEyelinerPanel.setVisibility(View.GONE);
                    break;
                case Eyelash:
                    mEyelashPanel.setVisibility(View.GONE);
                    break;
                case Facial:
                    mFacialPanel.setVisibility(View.GONE);
                    break;
            }
            mCurrentType = null;
            mBeautyPlasticsConfigView.setVisibility(View.GONE);
            mPreButton.setVisibility(View.VISIBLE);
            mCosmeticScroll.scrollTo(lastPos, 0);
        }

        @Override
        public void onClick(CosmeticTypes.Types type) {
            int viewID = -1;
            switch (type) {
                case Lipstick:
                    viewID = R.id.lsq_lipstick_add;
                    break;
                case Blush:
                    viewID = R.id.lsq_blush_add;
                    break;
                case Eyebrow:
                    viewID = R.id.lsq_eyebrow_add;
                    break;
                case Eyeshadow:
                    viewID = R.id.lsq_eyeshadow_add;
                    break;
                case Eyeliner:
                    viewID = R.id.lsq_eyeliner_add;
                    break;
                case Eyelash:
                    viewID = R.id.lsq_eyelash_add;
                    break;
                case Facial:
                    viewID = R.id.lsq_facial_add;
                    break;
            }
            findViewById(viewID).setVisibility(View.VISIBLE);
            mBeautyPlasticsConfigView.setVisibility(View.VISIBLE);
            types.add(type);
        }
    };

    private View mPreButton;

    private OnClickListener mCosmeticClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            lastPos = mCosmeticScroll.getScrollX();
            if (v.getId() != R.id.lsq_cosmetic_item_clear)
                if (mPreButton != null) mPreButton.setVisibility(View.VISIBLE);
            switch (v.getId()) {
                case R.id.lsq_cosmetic_item_clear:
                    clearCosmetic();
                    break;
                case R.id.lsq_cosmetic_item_lipstick:
                    mCurrentType = CosmeticTypes.Types.Lipstick;
                    mLipstick.setVisibility(View.GONE);
                    mPreButton = mLipstick;
                    mLipstickPanel.setVisibility(mLipstickPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    mBlushPanel.setVisibility(View.GONE);
                    mEyebrowPanel.setVisibility(View.GONE);
                    mEyeshadowPanel.setVisibility(View.GONE);
                    mEyelinerPanel.setVisibility(View.GONE);
                    mEyelashPanel.setVisibility(View.GONE);
                    mFacialPanel.setVisibility(View.GONE);
                    if (mLipstickPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_lipstick_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("lipAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mLipstickPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.lsq_cosmetic_item_blush:
                    mCurrentType = CosmeticTypes.Types.Blush;
                    mBlush.setVisibility(View.GONE);
                    mPreButton = mBlush;
                    mLipstickPanel.setVisibility(View.GONE);
                    mBlushPanel.setVisibility(mBlushPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    mEyebrowPanel.setVisibility(View.GONE);
                    mEyeshadowPanel.setVisibility(View.GONE);
                    mEyelinerPanel.setVisibility(View.GONE);
                    mEyelashPanel.setVisibility(View.GONE);
                    mFacialPanel.setVisibility(View.GONE);
                    if (mBlushPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_blush_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("blushAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mBlushPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.lsq_cosmetic_item_eyebrow:
                    mCurrentType = CosmeticTypes.Types.Eyebrow;
                    mEyebrow.setVisibility(View.GONE);
                    mPreButton = mEyebrow;
                    mLipstickPanel.setVisibility(View.GONE);
                    mBlushPanel.setVisibility(View.GONE);
                    mEyebrowPanel.setVisibility(mEyebrowPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    mEyeshadowPanel.setVisibility(View.GONE);
                    mEyelinerPanel.setVisibility(View.GONE);
                    mEyelashPanel.setVisibility(View.GONE);
                    mFacialPanel.setVisibility(View.GONE);
                    if (mEyebrowPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_eyebrow_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyebrowAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mEyebrowPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.lsq_cosmetic_item_eyeshadow:
                    mCurrentType = CosmeticTypes.Types.Eyeshadow;
                    mEyeshadow.setVisibility(View.GONE);
                    mPreButton = mEyeshadow;
                    mLipstickPanel.setVisibility(View.GONE);
                    mBlushPanel.setVisibility(View.GONE);
                    mEyebrowPanel.setVisibility(View.GONE);
                    mEyeshadowPanel.setVisibility(mEyeshadowPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    mEyelinerPanel.setVisibility(View.GONE);
                    mEyelashPanel.setVisibility(View.GONE);
                    mFacialPanel.setVisibility(View.GONE);
                    if (mEyeshadowPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_eyeshadow_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyeshadowAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mEyeshadowPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.lsq_cosmetic_item_eyeliner:
                    mCurrentType = CosmeticTypes.Types.Eyeliner;
                    mEyeliner.setVisibility(View.GONE);
                    mPreButton = mEyeliner;
                    mLipstickPanel.setVisibility(View.GONE);
                    mBlushPanel.setVisibility(View.GONE);
                    mEyebrowPanel.setVisibility(View.GONE);
                    mEyeshadowPanel.setVisibility(View.GONE);
                    mEyelinerPanel.setVisibility(mEyelinerPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    mEyelashPanel.setVisibility(View.GONE);
                    mFacialPanel.setVisibility(View.GONE);
                    if (mEyelinerPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_eyeliner_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyelineAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mEyelinerPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.lsq_cosmetic_item_eyelash:
                    mCurrentType = CosmeticTypes.Types.Eyelash;
                    mEyelash.setVisibility(View.GONE);
                    mPreButton = mEyelash;
                    mLipstickPanel.setVisibility(View.GONE);
                    mBlushPanel.setVisibility(View.GONE);
                    mEyebrowPanel.setVisibility(View.GONE);
                    mEyeshadowPanel.setVisibility(View.GONE);
                    mEyelinerPanel.setVisibility(View.GONE);
                    mEyelashPanel.setVisibility(mEyelashPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    mFacialPanel.setVisibility(View.GONE);
                    if (mEyelashPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_eyelash_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyelashAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mEyelashPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);

                    }
                    break;

                case R.id.lsq_cosmetic_item_facial:
                    mCurrentType = CosmeticTypes.Types.Facial;
                    mFacial.setVisibility(View.GONE);
                    mPreButton = mFacial;
                    mLipstickPanel.setVisibility(View.GONE);
                    mBlushPanel.setVisibility(View.GONE);
                    mEyebrowPanel.setVisibility(View.GONE);
                    mEyeshadowPanel.setVisibility(View.GONE);
                    mEyelinerPanel.setVisibility(View.GONE);
                    mEyelashPanel.setVisibility(View.GONE);
                    mFacialPanel.setVisibility(mFacialPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    if (mFacialPanel.getVisibility() == View.VISIBLE) {
                        mBeautyPlasticsConfigView.setVisibility(findViewById(R.id.lsq_facial_add).getVisibility());
                        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("facialAlpha")));
                        findViewById(R.id.list_panel).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                mCosmeticScroll.scrollTo(mFacialPanel.getLeft(), 0);
                                findViewById(R.id.list_panel).removeOnLayoutChangeListener(this);
                            }
                        });
                    } else {
                        mBeautyPlasticsConfigView.setVisibility(View.GONE);

                    }
                    break;
            }
        }
    };

    private boolean isFirstShow = true;

    private void clearCosmetic() {
        if (types.size() == 0) return;
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
        adBuilder.setTitle(R.string.lsq_text_cosmetic_type);
        adBuilder.setMessage(R.string.lsq_clear_beauty_cosmetic_hit);
        adBuilder.setNegativeButton(R.string.lsq_audioRecording_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adBuilder.setPositiveButton(R.string.lsq_audioRecording_next, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mController.clearAllCosmetic();
                types.removeAll(Arrays.asList(CosmeticTypes.Types.values()));
            }
        });
        adBuilder.show();
    }

    private HorizontalScrollView mCosmeticScroll;

    private void initCosmeticView() {
        mController.setPanelClickListener(panelClickListener);

        mCosmeticList = findViewById(R.id.lsq_cosmetic_list);

        mCosmeticClear = findViewById(R.id.lsq_cosmetic_item_clear);
        mCosmeticClear.setOnClickListener(mCosmeticClick);

        mLipstick = findViewById(R.id.lsq_cosmetic_item_lipstick);
        mLipstick.setOnClickListener(mCosmeticClick);
        mLipstickPanel = findViewById(R.id.lsq_lipstick_panel);
        mLipstickPanel.addView(mController.getLipstickPanel().getPanel());

        mBlush = findViewById(R.id.lsq_cosmetic_item_blush);
        mBlush.setOnClickListener(mCosmeticClick);
        mBlushPanel = findViewById(R.id.lsq_blush_panel);
        mBlushPanel.addView(mController.getBlushPanel().getPanel());

        mEyebrow = findViewById(R.id.lsq_cosmetic_item_eyebrow);
        mEyebrow.setOnClickListener(mCosmeticClick);
        mEyebrowPanel = findViewById(R.id.lsq_eyebrow_panel);
        mEyebrowPanel.addView(mController.getEyebrowPanel().getPanel());

        mEyeshadow = findViewById(R.id.lsq_cosmetic_item_eyeshadow);
        mEyeshadow.setOnClickListener(mCosmeticClick);
        mEyeshadowPanel = findViewById(R.id.lsq_eyeshadow_panel);
        mEyeshadowPanel.addView(mController.getEyeshadowPanel().getPanel());

        mEyeliner = findViewById(R.id.lsq_cosmetic_item_eyeliner);
        mEyeliner.setOnClickListener(mCosmeticClick);
        mEyelinerPanel = findViewById(R.id.lsq_eyeliner_panel);
        mEyelinerPanel.addView(mController.getEyelinerPanel().getPanel());

        mEyelash = findViewById(R.id.lsq_cosmetic_item_eyelash);
        mEyelash.setOnClickListener(mCosmeticClick);
        mEyelashPanel = findViewById(R.id.lsq_eyelash_panel);
        mEyelashPanel.addView(mController.getEyelashPanel().getPanel());

        mFacial = findViewById(R.id.lsq_cosmetic_item_facial);
        mFacial.setOnClickListener(mCosmeticClick);
        mFacialPanel = findViewById(R.id.lsq_facial_panel);
        mFacialPanel.addView(mController.getFacialPanel().getPanel());


        mCosmeticScroll = findViewById(R.id.lsq_cosmetic_scroll_view);
    }

    /*********************************** 微整形 ********************/
    /**
     * 美颜微整形是否选中
     */
    private boolean isBeautyChecked = true;

    private boolean isCosmeticChecked = false;
    /**
     * 美颜适配器
     */
    private BeautyRecyclerAdapter mBeautyRecyclerAdapter;
    /**
     * 微整形适配器
     */
    private BeautyPlasticRecyclerAdapter mBeautyPlasticRecyclerAdapter;


    /**
     * 微整形调节栏
     */
    private ParamsConfigView mBeautyPlasticsConfigView;
    /**
     * 微整形默认值  Float 为进度值
     */
    private HashMap<String, Float> mDefaultBeautyPercentParams = new HashMap<String, Float>() {
        {
            put("eyeSize", 0.3f);
            put("chinSize", 0.5f);
            put("cheekNarrow", 0.0f);
            put("smallFace", 0.0f);
            put("noseSize", 0.2f);
            put("noseHeight", 0.0f);
            put("mouthWidth", 0.0f);
            put("lips", 0.0f);
            put("philterum", 0.0f);
            put("archEyebrow", 0.0f);
            put("browPosition", 0.0f);
            put("jawSize", 0.0f);
            put("cheekLowBoneNarrow", 0.0f);
            put("eyeAngle", 0.0f);
            put("eyeInnerConer", 0.0f);
            put("eyeOuterConer", 0.0f);
            put("eyeDis", 0.0f);
            put("eyeHeight", 0.0f);
            put("forehead", 0.0f);
            put("cheekBoneNarrow", 0.0f);

            put("eyelidAlpha", 0.0f);
            put("eyemazingAlpha", 0.0f);

            put("whitenTeethAlpha", 0.0f);
            put("eyeDetailAlpha", 0.0f);
            put("removePouchAlpha", 0.0f);
            put("removeWrinklesAlpha", 0.0f);

        }
    };

    private List<String> mReshapePlastics = new ArrayList() {
        {
            add("eyelidAlpha");
            add("eyemazingAlpha");

            add("whitenTeethAlpha");
            add("eyeDetailAlpha");
            add("removePouchAlpha");
            add("removeWrinklesAlpha");
        }
    };

    /**
     * 微整形参数
     */
    private List<String> mBeautyPlastics = new ArrayList() {
        {
            add("reset");
            add("eyeSize");
            add("chinSize");
            add("cheekNarrow");
            add("smallFace");
            add("noseSize");
            add("noseHeight");
            add("mouthWidth");
            add("lips");
            add("philterum");
            add("archEyebrow");
            add("browPosition");
            add("jawSize");
            add("cheekLowBoneNarrow");
            add("eyeAngle");
            add("eyeInnerConer");
            add("eyeOuterConer");
            add("eyeDis");
            add("eyeHeight");
            add("forehead");
            add("cheekBoneNarrow");

            add("eyelidAlpha");
            add("eyemazingAlpha");

            add("whitenTeethAlpha");
            add("eyeDetailAlpha");
            add("removePouchAlpha");
            add("removeWrinklesAlpha");
        }
    };

    /**
     * 美型调节栏
     */
    private ParamsConfigView.FilterConfigViewSeekBarDelegate mBeautyPlasticConfigViewSeekBarDelegate =
            new ParamsConfigView.FilterConfigViewSeekBarDelegate() {
                @Override
                public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {
//                    if (isBeautyChecked)
//                        submitSkinParamter(arg.getKey(), seekbar.getSeekbar().getProgress());
//                    else
//                        submitPlasticFaceParamter(arg.getKey(), seekbar.getSeekbar().getProgress());
                }
            };

    /**
     * 美颜Item点击事件
     */
    BeautyRecyclerAdapter.OnBeautyItemClickListener beautyItemClickListener =
            new BeautyRecyclerAdapter.OnBeautyItemClickListener() {
                @Override
                public void onChangeSkin(View v, String key, Constants.SkinMode skinMode) {
                    mBeautyPlasticsConfigView.setVisibility(VISIBLE);
                    if (skinMode != mCurrentSkinMode)
                        switchConfigSkin(skinMode);

                    SelesParameters.FilterArg filterArg = mSkinParameters.getFilterArg(key);
                    mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(filterArg));
                }

                @Override
                public void onClear() {
                    hideBeautyBarLayout();

                    mRenderPool.runSync(new Runnable() {
                        @Override
                        public void run() {
                            boolean ret = mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.SkinFace));
                        }
                    });
                    mCurrentSkinMode = null;
                    isBeautyClose = true;
                }
            };

    /**
     * 微整形Item点击事件
     */
    BeautyPlasticRecyclerAdapter.OnBeautyPlasticItemClickListener beautyPlasticItemClickListener = new BeautyPlasticRecyclerAdapter.OnBeautyPlasticItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            mBeautyPlasticsConfigView.setVisibility(VISIBLE);
            switchBeautyPlasticConfig(position);
        }

        @Override
        public void onClear() {

            hideBeautyBarLayout();

            AlertDialog.Builder adBuilder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
            adBuilder.setTitle(R.string.lsq_text_beauty_type);
            adBuilder.setMessage(R.string.lsq_clear_beauty_plastic_hit);
            adBuilder.setNegativeButton(R.string.lsq_audioRecording_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adBuilder.setPositiveButton(R.string.lsq_audioRecording_next, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (String key : mDefaultBeautyPercentParams.keySet()) {
                        TLog.e("key -- %s", mDefaultBeautyPercentParams.get(key));
                        submitPlasticFaceParamter(key, mDefaultBeautyPercentParams.get(key));
                    }
                    dialog.dismiss();
                }
            });
            adBuilder.show();
        }
    };

    /**
     * 隐藏美颜参数调节栏
     */
    private void hideBeautyBarLayout() {
        mBeautyPlasticsConfigView.setVisibility(GONE);

    }

    /**
     * 切换美颜、微整形Tab
     *
     * @param view
     */
    private void switchBeautyConfigTab(View view) {
        switch (view.getId()) {
            // 美颜
            case R.id.lsq_beauty_tab:
                isBeautyChecked = true;
                ((TextView) findViewById(R.id.lsq_beauty_tab)).setTextColor(getResources().getColor(R.color.lsq_color_white));
                ((TextView) findViewById(R.id.lsq_beauty_plastic_tab)).setTextColor(getResources().getColor(R.color.lsq_alpha_white_66));
                ((TextView) findViewById(R.id.lsq_cosmetic_tab)).setTextColor(getResources().getColor(R.color.lsq_alpha_white_66));
                findViewById(R.id.lsq_beauty_tab_line).setBackgroundResource(R.color.lsq_color_white);
                findViewById(R.id.lsq_beauty_plastic_tab_line).setBackgroundResource(R.color.lsq_alpha_white_00);
                findViewById(R.id.lsq_cosmetic_tab_line).setBackgroundResource(R.color.lsq_alpha_white_00);
                mCosmeticList.setVisibility(View.GONE);
                mBeautyRecyclerView.setVisibility(View.VISIBLE);
                mBeautyRecyclerView.setAdapter(mBeautyRecyclerAdapter);
                hideBeautyBarLayout();
                break;
            // 微整形
            case R.id.lsq_beauty_plastic_tab:
                isBeautyChecked = false;
                ((TextView) findViewById(R.id.lsq_beauty_tab)).setTextColor(getResources().getColor(R.color.lsq_alpha_white_66));
                ((TextView) findViewById(R.id.lsq_beauty_plastic_tab)).setTextColor(getResources().getColor(R.color.lsq_color_white));
                ((TextView) findViewById(R.id.lsq_cosmetic_tab)).setTextColor(getResources().getColor(R.color.lsq_alpha_white_66));

                findViewById(R.id.lsq_beauty_tab_line).setBackgroundResource(R.color.lsq_alpha_white_00);
                findViewById(R.id.lsq_beauty_plastic_tab_line).setBackgroundResource(R.color.lsq_color_white);
                findViewById(R.id.lsq_cosmetic_tab_line).setBackgroundResource(R.color.lsq_alpha_white_00);
                mCosmeticList.setVisibility(View.GONE);
                mBeautyRecyclerView.setVisibility(View.VISIBLE);
                mBeautyRecyclerView.setAdapter(mBeautyPlasticRecyclerAdapter);
                mBeautyRecyclerView.scrollToPosition(mBeautyPlasticRecyclerAdapter.getCurrentPos() - 1);
                int currentPos = mBeautyPlasticRecyclerAdapter.getCurrentPos();
                if (currentPos != -1) {
                    switchBeautyPlasticConfig(currentPos);
                } else {
                    hideBeautyBarLayout();
                }
                break;
            //美妆
            case R.id.lsq_cosmetic_tab:
                isBeautyChecked = false;
                isCosmeticChecked = true;
                ((TextView) findViewById(R.id.lsq_cosmetic_tab)).setTextColor(getResources().getColor(R.color.lsq_color_white));
                findViewById(R.id.lsq_cosmetic_tab_line).setBackgroundResource(R.color.lsq_color_white);

                ((TextView) findViewById(R.id.lsq_beauty_tab)).setTextColor(getResources().getColor(R.color.lsq_alpha_white_66));
                ((TextView) findViewById(R.id.lsq_beauty_plastic_tab)).setTextColor(getResources().getColor(R.color.lsq_alpha_white_66));
                findViewById(R.id.lsq_beauty_tab_line).setBackgroundResource(R.color.lsq_alpha_white_00);
                findViewById(R.id.lsq_beauty_plastic_tab_line).setBackgroundResource(R.color.lsq_alpha_white_00);

                mCosmeticList.setVisibility(View.VISIBLE);
                mBeautyRecyclerView.setVisibility(View.GONE);
                if (mCurrentType != null) {
                    mBeautyPlasticsConfigView.setVisibility(View.VISIBLE);
                    switch (mCurrentType) {
                        case Lipstick:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("lipAlpha")));
                            break;
                        case Blush:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("blushAlpha")));
                            break;
                        case Eyebrow:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyebrowAlpha")));
                            break;
                        case Eyeshadow:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyeshadowAlpha")));
                            break;
                        case Eyeliner:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyelineAlpha")));
                            break;
                        case Eyelash:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("eyelashAlpha")));
                            break;
                        case Facial:
                            mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(mController.getEffect().getFilterArg("facialAlpha")));
                            break;
                    }
                } else {
                    hideBeautyBarLayout();
                }

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        boolean ret = false;
                        if (mFP.getFilter(mFilterMap.get(SelesParameters.FilterModel.CosmeticFace)) == null) {
                            ret = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.CosmeticFace), mController.getCosmeticFilter());
                        }
                    }
                });


                break;
        }
    }

    /**
     * 设置美颜、微整形视图状态
     *
     * @param isVisible true显示false隐藏
     */
    private void setBeautyViewVisible(boolean isVisible) {

        if (isVisible) {
            setBeautyLayout(true);
            setTextButtonDrawableTop(mBeautyButton, R.drawable.video_nav_ic_beauty_selected);

            TextView lsq_beauty_tab = findViewById(R.id.lsq_beauty_tab);
            TextView lsq_beauty_shape_tab = findViewById(R.id.lsq_beauty_plastic_tab);
            TextView lsq_cosmetic_tab = findViewById(R.id.lsq_cosmetic_tab);

            lsq_beauty_tab.setTag(0);
            lsq_beauty_shape_tab.setTag(1);

            lsq_beauty_tab.setOnClickListener(onClickListener);
            lsq_beauty_shape_tab.setOnClickListener(onClickListener);
            lsq_cosmetic_tab.setOnClickListener(onClickListener);

            if (isCosmeticChecked) {
                switchBeautyConfigTab(lsq_cosmetic_tab);
            } else {
                switchBeautyConfigTab(isBeautyChecked ? lsq_beauty_tab : lsq_beauty_shape_tab);
            }
        } else {
            setBeautyLayout(false);
            setTextButtonDrawableTop(mBeautyButton, R.drawable.video_nav_ic_beauty);
        }
    }

    /**
     * 设置美颜视图
     *
     * @param isVisible 是否可见
     */
    private void setBeautyLayout(boolean isVisible) {
        mSmartBeautyTabLayout.setVisibility(isVisible ? VISIBLE : GONE);
    }

    private SelesParameters mSkinParameters;

    private Constants.SkinMode mCurrentSkinMode;

    /**
     * 切换美颜预设按键
     *
     * @param skinMode true 自然(精准)美颜 false 极致美颜
     */
    private void switchConfigSkin(Constants.SkinMode skinMode) {
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                Filter filter = mFP.getFilter(mFilterMap.get(SelesParameters.FilterModel.SkinFace));
                if (filter != null){
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.SkinFace));
                    filter.release();
                }
                SelesParameters selesParameters = new SelesParameters();
                selesParameters.appendFloatArg("whitening", 0.3f);
                selesParameters.appendFloatArg("smoothing", 0.8f);
                Filter skinFilter = null;
                Config config = null;
                boolean ret = false;
                switch (skinMode) {
                    case SkinNatural:
                        skinFilter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                        config = new Config();
                        config.setString(TusdkImageFilter.CONFIG_NAME, TusdkImageFilter.NAME_SkinNatural);
                        skinFilter.setConfig(config);
                        TusdkImageFilter.SkinNaturalPropertyBuilder naturalPropertyBuilder = new TusdkImageFilter.SkinNaturalPropertyBuilder();
                        naturalPropertyBuilder.smoothing = 0.8;
                        naturalPropertyBuilder.fair = 0.3;
                        naturalPropertyBuilder.ruddy = 0.4;
                        mPropertyMap.put(SelesParameters.FilterModel.SkinFace, naturalPropertyBuilder);
                        selesParameters.appendFloatArg("ruddy", 0.4f);
                        ret = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.SkinFace), skinFilter);
                        skinFilter.setProperty(TusdkImageFilter.PROP_PARAM, naturalPropertyBuilder.makeProperty());
                        break;
                    case SkinMoist:
                        skinFilter = new Filter(mFP.getContext(), TusdkImageFilter.TYPE_NAME);
                        config = new Config();
                        config.setString(TusdkImageFilter.CONFIG_NAME, TusdkImageFilter.NAME_SkinHazy);
                        skinFilter.setConfig(config);

                        TusdkImageFilter.SkinHazyPropertyBuilder hazyPropertyBuilder = new TusdkImageFilter.SkinHazyPropertyBuilder();
                        hazyPropertyBuilder.smoothing = 0.8;
                        hazyPropertyBuilder.fair = 0.3;
                        hazyPropertyBuilder.ruddy = 0.4;

                        mPropertyMap.put(SelesParameters.FilterModel.SkinFace, hazyPropertyBuilder);
                        selesParameters.appendFloatArg("ruddy", 0.4f);
                        ret = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.SkinFace), skinFilter);
                        skinFilter.setProperty(TusdkImageFilter.PROP_PARAM, hazyPropertyBuilder.makeProperty());
                        break;
                    case Beauty:
                        skinFilter = new Filter(mFP.getContext(), TusdkBeautFaceV2Filter.TYPE_NAME);

                        TusdkBeautFaceV2Filter.PropertyBuilder builder = new TusdkBeautFaceV2Filter.PropertyBuilder();
                        builder.smoothing = 0.8;
                        builder.whiten = 0.3;
                        builder.sharpen = 0.6f;
                        mPropertyMap.put(SelesParameters.FilterModel.SkinFace, builder);
                        selesParameters.appendFloatArg("sharpen", 0.6f);
                        ret = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.SkinFace), skinFilter);
                        skinFilter.setProperty(TusdkBeautFaceV2Filter.PROP_PARAM, builder.makeProperty());
                        break;
                }


                mCurrentFilterMap.put(SelesParameters.FilterModel.SkinFace, skinFilter);

                selesParameters.setListener(new SelesParameters.SelesParametersListener() {
                    @Override
                    public void onUpdateParameters(SelesParameters.FilterModel model, String code, SelesParameters.FilterArg arg) {
                        mRenderPool.runSync(new Runnable() {
                            @Override
                            public void run() {
                                boolean ret = false;
                                Object skinProperty = mPropertyMap.get(SelesParameters.FilterModel.SkinFace);
                                String key = arg.getKey();
                                double progress = arg.getPrecentValue();
                                switch (mCurrentSkinMode) {
                                    case SkinNatural:
                                        TusdkImageFilter.SkinNaturalPropertyBuilder naturalPropertyBuilder = (TusdkImageFilter.SkinNaturalPropertyBuilder) skinProperty;
                                        switch (key) {
                                            case "whitening":
                                                naturalPropertyBuilder.fair = progress;
                                                break;
                                            case "smoothing":
                                                naturalPropertyBuilder.smoothing = progress;
                                                break;
                                            case "ruddy":
                                                naturalPropertyBuilder.ruddy = progress;
                                                break;
                                        }
                                        ret = mCurrentFilterMap.get(SelesParameters.FilterModel.SkinFace).setProperty(TusdkImageFilter.PROP_PARAM, naturalPropertyBuilder.makeProperty());
                                        break;
                                    case SkinMoist:
                                        TusdkImageFilter.SkinHazyPropertyBuilder hazyPropertyBuilder = (TusdkImageFilter.SkinHazyPropertyBuilder) skinProperty;
                                        switch (key) {
                                            case "whitening":
                                                hazyPropertyBuilder.fair = progress;
                                                break;
                                            case "smoothing":
                                                hazyPropertyBuilder.smoothing = progress;
                                                break;
                                            case "ruddy":
                                                hazyPropertyBuilder.ruddy = progress;
                                                break;
                                        }
                                        ret = mCurrentFilterMap.get(SelesParameters.FilterModel.SkinFace).setProperty(TusdkImageFilter.PROP_PARAM, hazyPropertyBuilder.makeProperty());
                                        break;
                                    case Beauty:
                                        TusdkBeautFaceV2Filter.PropertyBuilder faceProperty = (TusdkBeautFaceV2Filter.PropertyBuilder) skinProperty;
                                        switch (key) {
                                            case "whitening":
                                                faceProperty.whiten = progress;
                                                break;
                                            case "smoothing":
                                                faceProperty.smoothing = progress;
                                                break;
                                            case "sharpen":
                                                faceProperty.sharpen = progress;
                                                break;
                                        }
                                        ret = mCurrentFilterMap.get(SelesParameters.FilterModel.SkinFace).setProperty(TusdkBeautFaceV2Filter.PROP_PARAM, faceProperty.makeProperty());
                                        break;
                                }
                            }
                        });
                    }
                });

                mSkinParameters = selesParameters;
                mCurrentSkinMode = skinMode;
            }
        });

        // 滤镜名显示
        showHitTitle(TuSdkContext.getString(getSkinModeTitle(skinMode)));

        isBeautyClose = false;
    }

    private String getSkinModeTitle(Constants.SkinMode skinMode) {
        switch (skinMode) {
            case SkinNatural:
                return "lsq_beauty_skin_precision";
            case SkinMoist:
                return "lsq_beauty_skin_extreme";
            case Beauty:
                return "lsq_beauty_skin_beauty";
        }
        return "";
    }

    /**
     * 应用美肤
     *
     * @param key
     * @param progress
     */
    private void submitSkinParamter(String key, float progress) {
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                Object skinProperty = mPropertyMap.get(SelesParameters.FilterModel.SkinFace);
                switch (mCurrentSkinMode) {
                    case SkinNatural:
                        TusdkImageFilter.SkinNaturalPropertyBuilder naturalPropertyBuilder = (TusdkImageFilter.SkinNaturalPropertyBuilder) skinProperty;
                        switch (key) {
                            case "whitening":
                                naturalPropertyBuilder.fair = progress;
                                break;
                            case "smoothing":
                                naturalPropertyBuilder.smoothing = progress;
                                break;
                            case "ruddy":
                                naturalPropertyBuilder.ruddy = progress;
                                break;
                        }
                        ret = mCurrentFilterMap.get(SelesParameters.FilterModel.SkinFace).setProperty(TusdkImageFilter.PROP_PARAM, naturalPropertyBuilder.makeProperty());
                        break;
                    case SkinMoist:
                        TusdkImageFilter.SkinHazyPropertyBuilder hazyPropertyBuilder = (TusdkImageFilter.SkinHazyPropertyBuilder) skinProperty;
                        switch (key) {
                            case "whitening":
                                hazyPropertyBuilder.fair = progress;
                                break;
                            case "smoothing":
                                hazyPropertyBuilder.smoothing = progress;
                                break;
                            case "ruddy":
                                hazyPropertyBuilder.ruddy = progress;
                                break;
                        }
                        ret = mCurrentFilterMap.get(SelesParameters.FilterModel.SkinFace).setProperty(TusdkImageFilter.PROP_PARAM, hazyPropertyBuilder.makeProperty());
                        break;
                    case Beauty:
                        TusdkBeautFaceV2Filter.PropertyBuilder faceProperty = (TusdkBeautFaceV2Filter.PropertyBuilder) skinProperty;
                        switch (key) {
                            case "whitening":
                                faceProperty.whiten = progress;
                                break;
                            case "smoothing":
                                faceProperty.smoothing = progress;
                                break;
                            case "sharpen":
                                faceProperty.sharpen = progress;
                                break;
                        }
                        ret = mCurrentFilterMap.get(SelesParameters.FilterModel.SkinFace).setProperty(TusdkBeautFaceV2Filter.PROP_PARAM, faceProperty.makeProperty());
                        break;
                }
            }
        });


    }

    private SelesParameters mPlasticParameter;

    /**
     * 切换微整形类型
     *
     * @param position
     */
    private void switchBeautyPlasticConfig(int position) {
        if (mCurrentFilterMap.get(SelesParameters.FilterModel.MonsterFace) != null) {
            mRenderPool.runSync(new Runnable() {
                @Override
                public void run() {
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.MonsterFace);
                    mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.MonsterFace));
                }
            });
            mPropsItemPagerAdapter.notifyAllPageData();
        }


        if (mFP.getFilter(mFilterMap.get(SelesParameters.FilterModel.PlasticFace)) == null) {
            initPlastic();
        }

        SelesParameters.FilterArg filterArg = mPlasticParameter.getFilterArg(mBeautyPlastics.get(position));
        mBeautyPlasticsConfigView.setFilterArgs(Arrays.asList(filterArg));


    }

    private void addReshape() {
        Filter reshapeFilter = new Filter(mFP.getContext(), TusdkReshapeFilter.TYPE_NAME);
        boolean reshapeRes = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.Reshape), reshapeFilter);
        mCurrentFilterMap.put(SelesParameters.FilterModel.Reshape, reshapeFilter);
    }

    private void removeReshape() {
        mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.Reshape));
        mCurrentFilterMap.remove(SelesParameters.FilterModel.Reshape);
    }

    private void initPlastic() {
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                if (mFP.getFilter(mFilterMap.get(SelesParameters.FilterModel.PlasticFace)) == null) {
                    Filter plasticFilter = new Filter(mFP.getContext(), TusdkFacePlasticFilter.TYPE_NAME);

                    TusdkFacePlasticFilter.PropertyBuilder plasticProperty = new TusdkFacePlasticFilter.PropertyBuilder();

                    SelesParameters parameters = new SelesParameters();
                    boolean plasticRes = mFP.addFilter(mFilterMap.get(SelesParameters.FilterModel.PlasticFace), plasticFilter);
                    mCurrentFilterMap.put(SelesParameters.FilterModel.PlasticFace, plasticFilter);
                    TusdkReshapeFilter.PropertyBuilder reshapeProperty = new TusdkReshapeFilter.PropertyBuilder();

                    for (String key : mDefaultBeautyPercentParams.keySet()) {
                        float value = mDefaultBeautyPercentParams.get(key);
                        if (mReshapePlastics.contains(key)) {
                            parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                            switch (key) {
                                case "eyelidAlpha":
                                    reshapeProperty.eyelidOpacity = value;
                                    break;
                                case "eyemazingAlpha":
                                    reshapeProperty.eyemazingOpacity = value;
                                    break;
                                case "whitenTeethAlpha":
                                    reshapeProperty.whitenTeethOpacity = value;
                                    break;
                                case "eyeDetailAlpha":
                                    reshapeProperty.eyeDetailOpacity = value;
                                    break;
                                case "removePouchAlpha":
                                    reshapeProperty.removePouchOpacity = value;
                                    break;
                                case "removeWrinklesAlpha":
                                    reshapeProperty.removeWrinklesOpacity = value;
                                    break;
                            }
                        } else {
                            switch (key) {
                                case "eyeSize":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.eyeEnlarge = value;
                                    break;
                                case "chinSize":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.cheekThin = value;
                                    break;
                                case "cheekNarrow":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.cheekNarrow = value;
                                    break;
                                case "smallFace":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.faceSmall = value;
                                    break;
                                case "noseSize":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.noseWidth = value;
                                    break;
                                case "noseHeight":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.noseHeight = value;
                                    break;
                                case "mouthWidth":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.mouthWidth = value;
                                    break;
                                case "lips":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.lipsThickness = value;
                                    break;
                                case "philterum":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.philterumThickness = value;
                                    break;
                                case "archEyebrow":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.browThickness = value;
                                    break;
                                case "browPosition":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.browHeight = value;
                                    break;
                                case "jawSize":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.chinThickness = value;
                                    break;
                                case "cheekLowBoneNarrow":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.cheekLowBoneNarrow = value;
                                    break;
                                case "eyeAngle":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.eyeAngle = value;
                                    break;
                                case "eyeInnerConer":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.eyeInnerConer = value;
                                    break;
                                case "eyeOuterConer":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.eyeOuterConer = value;
                                    break;
                                case "eyeDis":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.eyeDistance = value;
                                    break;
                                case "eyeHeight":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.eyeHeight = value;
                                    break;
                                case "forehead":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key), -1, 1);
                                    plasticProperty.foreheadHeight = value;
                                    break;
                                case "cheekBoneNarrow":
                                    parameters.appendFloatArg(key, mDefaultBeautyPercentParams.get(key));
                                    plasticProperty.cheekBoneNarrow = value;
                                    break;

                            }
                        }
                    }

                    plasticFilter.setProperty(TusdkFacePlasticFilter.PROP_PARAM, plasticProperty.makeProperty());


                    parameters.setListener(new SelesParameters.SelesParametersListener() {
                        @Override
                        public void onUpdateParameters(SelesParameters.FilterModel model, String code, SelesParameters.FilterArg arg) {
                            double value = arg.getValue();
                            String key = arg.getKey();
                            if (mReshapePlastics.contains(key)) {
                                submitPlastic(value, key, reshapeProperty);
                            } else {
                                submitPlastic(value, key, plasticProperty);

                            }
                        }
                    });
                    mPropertyMap.put(SelesParameters.FilterModel.PlasticFace, plasticProperty);
                    mPropertyMap.put(SelesParameters.FilterModel.Reshape, reshapeProperty);
                    mPlasticParameter = parameters;
                }
            }
        });
    }

    private void submitPlastic(double value, String key, TusdkFacePlasticFilter.PropertyBuilder plasticProperty) {
        switch (key) {
            case "eyeSize":
                plasticProperty.eyeEnlarge = value;
                break;
            case "chinSize":
                plasticProperty.cheekThin = value;
                break;
            case "cheekNarrow":
                plasticProperty.cheekNarrow = value;
                break;
            case "smallFace":
                plasticProperty.faceSmall = value;
                break;
            case "noseSize":
                plasticProperty.noseWidth = value;
                break;
            case "noseHeight":
                plasticProperty.noseHeight = value;
                break;
            case "mouthWidth":
                plasticProperty.mouthWidth = value;
                break;
            case "lips":
                plasticProperty.lipsThickness = value;
                break;
            case "philterum":
                plasticProperty.philterumThickness = value;
                break;
            case "archEyebrow":
                plasticProperty.browThickness = value;
                break;
            case "browPosition":
                plasticProperty.browHeight = value;
                break;
            case "jawSize":
                plasticProperty.chinThickness = value;
                break;
            case "cheekLowBoneNarrow":
                plasticProperty.cheekLowBoneNarrow = value;
                break;
            case "eyeAngle":
                plasticProperty.eyeAngle = value;
                break;
            case "eyeInnerConer":
                plasticProperty.eyeInnerConer = value;
                break;
            case "eyeOuterConer":
                plasticProperty.eyeOuterConer = value;
                break;
            case "eyeDis":
                plasticProperty.eyeDistance = value;
                break;
            case "eyeHeight":
                plasticProperty.eyeHeight = value;
                break;
            case "forehead":
                plasticProperty.foreheadHeight = value;
                break;
            case "cheekBoneNarrow":
                plasticProperty.cheekBoneNarrow = value;
                break;

        }
        mPlasticParameter.getFilterArg(key).setValue((float) value);
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                boolean ret = mCurrentFilterMap.get(SelesParameters.FilterModel.PlasticFace).setProperty(TusdkFacePlasticFilter.PROP_PARAM, plasticProperty.makeProperty());

            }
        });
    }

    private void submitPlastic(double value, String key, TusdkReshapeFilter.PropertyBuilder reshapeProperty) {


        switch (key) {
            case "eyelidAlpha":
                reshapeProperty.eyelidOpacity = value;
                break;
            case "eyemazingAlpha":
                reshapeProperty.eyemazingOpacity = value;
                break;
            case "whitenTeethAlpha":
                reshapeProperty.whitenTeethOpacity = value;
                break;
            case "eyeDetailAlpha":
                reshapeProperty.eyeDetailOpacity = value;
                break;
            case "removePouchAlpha":
                reshapeProperty.removePouchOpacity = value;
                break;
            case "removeWrinklesAlpha":
                reshapeProperty.removeWrinklesOpacity = value;
                break;
        }

        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                if (mCurrentFilterMap.get(SelesParameters.FilterModel.Reshape) == null) {
                    addReshape();
                }
                boolean res = true;
                if (checkEnableReshape()) {
                    res = mCurrentFilterMap.get(SelesParameters.FilterModel.Reshape).setProperty(TusdkReshapeFilter.PROP_PARAM, reshapeProperty.makeProperty());
                } else {
                    removeReshape();
                }
            }
        });
    }

    /**
     * 应用整形值
     *
     * @param key
     * @param progress
     */
    private void submitPlasticFaceParamter(String key, float progress) {
        submitPlastic(progress, key, (TusdkFacePlasticFilter.PropertyBuilder) mPropertyMap.get(SelesParameters.FilterModel.PlasticFace));
    }


    /******************************** 拍照 ************************/
    /**
     * 更新拍照预览界面
     *
     * @param isShow true显示false隐藏
     */
    private void updatePreviewImageLayoutStatus(boolean isShow) {
        ThreadHelper.post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.lsq_preview_image_layout).setVisibility(isShow ? VISIBLE : GONE);
            }
        });
    }

    /**
     * 显示拍照视图
     *
     * @param bitmap
     */
    public void presentPreviewLayout(TuSdkResult result) {
        if (result.image != null) {
            mCurrentResult = result;
            mCaptureBitmap = result.image;
            updatePreviewImageLayoutStatus(true);
            mPreViewImageView.setImageBitmap(result.image);
            // 暂停相机
            mCamera.pausePreview();
        }
    }

    /**
     * 保存拍照资源
     */
    public void saveResource() {
        updatePreviewImageLayoutStatus(false);
        File file = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            file = AlbumHelper.getAlbumFileAndroidQ();
        } else {
            file = AlbumHelper.getAlbumFile();
        }
        ImageSqlHelper.saveJpgToAblum(mContext, mCaptureBitmap, 80, file, mCurrentResult.metadata);
        refreshFile(file);
        destroyBitmap();
        post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, getStringFromResource("lsq_image_save_ok"), Toast.LENGTH_SHORT).show();
            }
        });

        mCamera.resumePreview();
    }

    /**
     * 刷新相册
     *
     * @param file
     */
    public void refreshFile(File file) {
        if (file == null) {
            TLog.e("refreshFile file == null");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }

    /**
     * 删除拍照资源
     */
    public void deleteResource() {
        updatePreviewImageLayoutStatus(false);
        destroyBitmap();
        mCamera.resumePreview();
    }

    /**
     * 销毁拍照图片
     */
    private void destroyBitmap() {
        if (mCaptureBitmap == null) return;

        if (!mCaptureBitmap.isRecycled())
            mCaptureBitmap.recycle();

        mCaptureBitmap = null;
    }

    /********************************** 点击事件 ************************/
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 关闭
                case R.id.lsq_closeButton:
                    if (getDelegate() != null) getDelegate().finishRecordActivity();
                    break;
                // 切换摄像头
                case R.id.lsq_switchButton:
                    mCamera.rotateCamera();
                    mLightingOpen.setTextColor(getResources().getColor(R.color.lsq_color_white));
                    mLightingClose.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                    if (mCamera.getFacing() == CameraConfigs.CameraFacing.Front) {
                        mCamera.cameraFocus().setFocus(new PointF(0.5f, 0.5f), null);
                    }
                    break;
                // 美颜按钮显示美颜布局
                case R.id.lsq_beautyButton:
                    setFilterContentVisible(false);
                    setBottomViewVisible(mSmartBeautyTabLayout.getVisibility() == VISIBLE);
                    setBeautyViewVisible(mSmartBeautyTabLayout.getVisibility() == GONE);
                    setStickerVisible(false);
                    setSpeedViewVisible(false);
                    getFocusTouchView().isShowFoucusView(false);
                    break;
                // 速度
                case R.id.lsq_speedButton:
                    setFilterContentVisible(false);
                    setBottomViewVisible(true);
                    setStickerVisible(false);
                    setBeautyViewVisible(false);
                    setSpeedViewVisible(mSpeedModeBar.getVisibility() == GONE);
                    break;
                // 更多设置
                case R.id.lsq_moreButton:
                    mMoreConfigLayout.setVisibility(mMoreConfigLayout.getVisibility() == VISIBLE ? GONE : VISIBLE);
                    setTextButtonDrawableTop(mMoreButton, mMoreConfigLayout.getVisibility() == VISIBLE ? R.drawable.video_nav_ic_more_selected : R.drawable.video_nav_ic_more);
                    break;
                // 自动对焦开启
                case R.id.lsq_focus_open:
                    mFocusOpen.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                    mFocusClose.setTextColor(getResources().getColor(R.color.lsq_color_white));

                    mCamera.cameraFocus().setDisableContinueFocus(false);
                    break;
                // 自动对焦关闭
                case R.id.lsq_focus_close:
                    mFocusOpen.setTextColor(getResources().getColor(R.color.lsq_color_white));
                    mFocusClose.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));

                    mCamera.cameraFocus().setDisableContinueFocus(true);
                    break;
                // 闪光灯开启
                case R.id.lsq_lighting_open:
                    updateFlashMode(CameraConfigs.CameraFlash.Torch);
                    break;
                // 闪光灯关闭
                case R.id.lsq_lighting_close:
                    updateFlashMode(CameraConfigs.CameraFlash.Off);
                    break;
                // 美颜
                case R.id.lsq_beauty_tab:
                    isCosmeticChecked = false;
                    switchBeautyConfigTab(v);
                    break;
                // 微整形
                case R.id.lsq_beauty_plastic_tab:
                    isCosmeticChecked = false;
                    switchBeautyConfigTab(v);
                    break;
                case R.id.lsq_cosmetic_tab:
                    isCosmeticChecked = true;
                    switchBeautyConfigTab(v);
                    break;
                // 滤镜
                case R.id.lsq_tab_filter_btn:
                    setBeautyViewVisible(false);
                    setBottomViewVisible(false);
                    setSpeedViewVisible(false);
                    setStickerVisible(false);
                    showFilterLayout();
                    getFocusTouchView().isShowFoucusView(false);
                    break;
                // 贴纸
                case R.id.lsq_stickerWrap:
                    setFilterContentVisible(false);
                    setBeautyViewVisible(false);
                    setSpeedViewVisible(false);
                    setBottomViewVisible(false);
                    showStickerLayout();
                    getFocusTouchView().isShowFoucusView(false);
                    break;
                // 比例
                case R.id.lsq_radio_1_1:
                    updateCameraRatio(RatioType.ratio_1_1);
                    break;
                case R.id.lsq_radio_3_4:
                    updateCameraRatio(RatioType.ratio_3_4);
                    break;
                case R.id.lsq_radio_full:
                    updateCameraRatio(RatioType.ratio_orgin);
                    break;
                // 视频回退
                case R.id.lsq_backWrap:
                    // 点击后退按钮删除上一条视频
                    if (getDelegate().getFragmentSize() > 0) {
                        getDelegate().popFragment();
                        mRecordProgress.removePreSegment();

                        if (interuptLayout.getChildCount() != 0) {
                            interuptLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    interuptLayout.removeViewAt(interuptLayout.getChildCount() - 1);

                                }
                            });
                        }
                        // 删除最后一段，重置录制状态
                        if (getDelegate().getFragmentSize() == 0) {
                            interuptLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    interuptLayout.removeAllViews();
                                }
                            });

                            updateRecordButtonResource(mRecordMode);
                            setViewHideOrVisible(true);
                            return;
                        }
                    }
                    // 刷新按钮状态
                    setViewHideOrVisible(true);
                    break;
                // 保存录制视频
                case R.id.lsq_confirmWrap:
//                    if (mCamera.getMovieDuration() < Constants.MIN_RECORDING_TIME) {
//                        String msg = getStringFromResource("min_recordTime") + Constants.MIN_RECORDING_TIME + "s";
//                        TuSdk.messageHub().showToast(mContext, msg);
//                        return;
//                    }
                    // 启动录制隐藏比例调节按钮
                    if(mDelegate.stopRecording()){
                        initRecordProgress();
                        setViewHideOrVisible(true);
                    }
                    break;
                // 取消拍摄
                case R.id.lsq_backButton:
                    deleteResource();
                    break;
                // 保存拍摄
                case R.id.lsq_saveImageButton:
                    saveResource();
                    break;
                // 取消贴纸
                case R.id.lsq_cancel_button:
                    mRenderPool.runSync(new Runnable() {
                        @Override
                        public void run() {
                            boolean ret = mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.StickerFace));
                            mFP.deleteFilter(mFilterMap.get(SelesParameters.FilterModel.MonsterFace));
                        }
                    });
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.StickerFace);
                    mCurrentFilterMap.remove(SelesParameters.FilterModel.MonsterFace);
                    mPropsItemPagerAdapter.notifyAllPageData();
                    break;
            }
        }
    };


    private OnClickListener mOnSimultaneouslyModeChanged = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mDelegate == null) return;
            if (!canChangeLayer) return;
            switch (v.getId()) {
                case R.id.lsq_top_bottom:
                    mCurrentDoubleViewMode = DoubleViewMode.TopBottom;
                    mTopBottomMode.setTextColor(getContext().getColor(R.color.lsq_widget_speedbar_button_bg));
                    mLeftRightMode.setTextColor(getContext().getColor(R.color.lsq_color_white));
                    mViewInViewMode.setTextColor(getContext().getColor(R.color.lsq_color_white));
                    break;
                case R.id.lsq_left_right:
                    mCurrentDoubleViewMode = DoubleViewMode.LeftRight;
                    mTopBottomMode.setTextColor(getContext().getColor(R.color.lsq_color_white));
                    mLeftRightMode.setTextColor(getContext().getColor(R.color.lsq_widget_speedbar_button_bg));
                    mViewInViewMode.setTextColor(getContext().getColor(R.color.lsq_color_white));
                    break;
                case R.id.lsq_view_in_view:
                    mCurrentDoubleViewMode = DoubleViewMode.ViewInView;
                    mTopBottomMode.setTextColor(getContext().getColor(R.color.lsq_color_white));
                    mLeftRightMode.setTextColor(getContext().getColor(R.color.lsq_color_white));
                    mViewInViewMode.setTextColor(getContext().getColor(R.color.lsq_widget_speedbar_button_bg));
                    break;
            }
            mDelegate.updateDoubleViewMode(mCurrentDoubleViewMode);
        }
    };

    /**
     * 改变闪关灯状态
     *
     * @param cameraFlash
     */
    public void updateFlashMode(CameraConfigs.CameraFlash cameraFlash) {
        if (mCamera.getFacing() == CameraConfigs.CameraFacing.Front) return;
        switch (cameraFlash) {
            case Off:
                mLightingOpen.setTextColor(getResources().getColor(R.color.lsq_color_white));
                mLightingClose.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));

                mCamera.cameraParams().setFlashMode(cameraFlash);
                break;
            case Torch:
                mLightingOpen.setTextColor(getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                mLightingClose.setTextColor(getResources().getColor(R.color.lsq_color_white));

                mCamera.cameraParams().setFlashMode(cameraFlash);
                break;
        }
    }

    /**
     * 更新相机比例
     *
     * @param type
     */
    public void updateCameraRatio(int type) {
        // 只要开始录制就不可切换
        if (mDelegate.getFragmentSize() > 0) return;
        switch (type) {
            case RatioType.ratio_1_1:
                mRadio1_1.setImageResource(R.drawable.lsq_video_popup_ic_scale_square_selected);
                mRadio3_4.setImageResource(R.drawable.lsq_video_popup_ic_scale_3_4);
                mRadioFull.setImageResource(R.drawable.lsq_video_popup_ic_scale_full);
                switchCameraRatio(new Point(1, 1));
                break;
            case RatioType.ratio_3_4:
                mRadio1_1.setImageResource(R.drawable.lsq_video_popup_ic_scale_square);
                mRadio3_4.setImageResource(R.drawable.lsq_video_popup_ic_scale_3_4_selected);
                mRadioFull.setImageResource(R.drawable.lsq_video_popup_ic_scale_full);
                switchCameraRatio(new Point(9,  16));
                break;
            case RatioType.ratio_orgin:
                mRadio1_1.setImageResource(R.drawable.lsq_video_popup_ic_scale_square);
                mRadio3_4.setImageResource(R.drawable.lsq_video_popup_ic_scale_3_4);
                mRadioFull.setImageResource(R.drawable.lsq_video_popup_ic_scale_full_selected);
                TuSdkSize orgin = mRegionHandle.getWrapSize();
                TuCameraAspectRatio ratio = TuCameraAspectRatio.of(orgin.width, orgin.height);
                switchCameraRatio(new Point(ratio.getX(), ratio.getY()));
                break;
        }
    }

    public void onDoubleView(){
        mRadio1_1.setImageResource(R.drawable.lsq_video_popup_ic_scale_square);
        mRadio3_4.setImageResource(R.drawable.lsq_video_popup_ic_scale_3_4);
        mRadioFull.setImageResource(R.drawable.lsq_video_popup_ic_scale_full_selected);
    }

    /**
     * 改变屏幕比例 录制状态不可改变
     *
     * @param type 参数类型 RatioType
     */
    private void switchCameraRatio(Point ratio) {
        if (mCamera == null) return;
        TLog.e("current ratio %s", ratio.toString());

        mRegionHandle.setOffsetTopPercent(getPreviewOffsetTopPercent(ratio.x, ratio.y));

        mRegionHandle.changeWithRatio(((float) ratio.x) / ratio.y, new RegionHandler.RegionChangerListener() {
            @Override
            public void onRegionChanged(RectF rectPercent) {
                mDelegate.changedRect(rectPercent);
            }
        });
//
        mDelegate.changedRatio(TuSdkSize.create(ratio.x, ratio.y));


        // 设置预览区域顶部偏移量 必须在 changeRegionRatio 之前设置
//        mCamera.getRegionHandler().setOffsetTopPercent(getPreviewOffsetTopPercent(type));
//        mCamera.changeRegionRatio(RatioType.ratio(type));
//        mCamera.setRegionRatio(RatioType.ratio(type));

        // 计算保存比例
//        mCamera.getVideoEncoderSetting().videoSize = TuSdkSize.create((int) (mCamera.getCameraPreviewSize().width * RatioType.ratio(type)), mCamera.getCameraPreviewSize().width);

    }

    /**
     * 获取当前 Ratio 预览画面顶部偏移百分比（默认：-1 居中显示 取值范围：0-1）
     *
     * @param ratioType
     * @return
     */
    protected float getPreviewOffsetTopPercent(int x, int y) {
        if (x == 1 && y == 1) return 0.1f;
        // 置顶
        return 0.f;
    }

    /************************ 录制模式切换 **************************/
    /**
     * 模式按键切换动画
     */
    private ValueAnimator valueAnimator;
    /**
     * 录制按键模式
     */
    private int mRecordMode = RecordType.SHORT_CLICK_RECORD;

    private float mPosX, mCurPosX;
    private static final int FLING_MIN_DISTANCE = 20;// 移动最小距离

    private OnTouchListener onModeBarTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPosX = event.getX();
                    mCurPosX = 0;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mCurPosX = event.getX();
                    // 滑动效果处理
                    if (mCurPosX - mPosX > 0
                            && (Math.abs(mCurPosX - mPosX) > FLING_MIN_DISTANCE)) {
                        //向左滑动
                        if (mRecordMode == RecordType.SHORT_CLICK_RECORD) {
                            switchCameraModeButton(RecordType.CAPTURE);
                        } else if (mRecordMode == RecordType.DOUBLE_VIEW_RECORD) {
                            switchCameraModeButton(RecordType.SHORT_CLICK_RECORD);
                        }
                        return false;
                    } else if (mCurPosX - mPosX < 0
                            && (Math.abs(mCurPosX - mPosX) > FLING_MIN_DISTANCE)) {
                        //向右滑动
                        if (mRecordMode == RecordType.CAPTURE) {
                            switchCameraModeButton(RecordType.SHORT_CLICK_RECORD);
                        } else if (mRecordMode == RecordType.SHORT_CLICK_RECORD) {
                            switchCameraModeButton(RecordType.DOUBLE_VIEW_RECORD);
                        }
                        return false;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    // 点击效果处理
                    if (Math.abs(mCurPosX - mPosX) < FLING_MIN_DISTANCE || mCurPosX == 0) {
                        switch (v.getId()) {
                            // 拍照模式
                            case R.id.lsq_shootButton:
                                switchCameraModeButton(RecordType.CAPTURE);
                                break;
                            // 点击录制模式
                            case R.id.lsq_clickButton:
                                switchCameraModeButton(RecordType.SHORT_CLICK_RECORD);
                                break;
                            case R.id.lsq_double_view_Button:
                                switchCameraModeButton(RecordType.DOUBLE_VIEW_RECORD);
                                break;
                        }
                        return false;
                    }
            }
            return false;
        }
    };

    /**
     * 切换摄像模式按键
     *
     * @param index
     */
    public void switchCameraModeButton(int index) {
        if (valueAnimator != null && valueAnimator.isRunning() || mRecordMode == index) return;

        // 设置文字颜色
        mShootButton.setTextColor(index == 0 ? getResources().getColor(R.color.lsq_color_white) : getResources().getColor(R.color.lsq_alpha_white_66));
        mClickButton.setTextColor(index == 2 ? getResources().getColor(R.color.lsq_color_white) : getResources().getColor(R.color.lsq_alpha_white_66));
        mDoubleViewButton.setTextColor(index == 5 ? getResources().getColor(R.color.lsq_color_white) : getResources().getColor(R.color.lsq_alpha_white_66));

        // 设置偏移位置
        final float[] Xs = getModeButtonWidth();

        float offSet = 0;
        if (mRecordMode == 0 && index == 2)
            offSet = -(Xs[1] - Xs[0]) / 2 - (Xs[2] - Xs[1]) / 2;
        else if (mRecordMode == 0 && index == 5)
            offSet = -(Xs[1] - Xs[0]) / 2 - (Xs[3] - Xs[2]) / 2 - (Xs[2] - Xs[1]);
        else if (mRecordMode == 2 && index == 0)
            offSet = (Xs[1] - Xs[0]) / 2 + (Xs[2] - Xs[1]) / 2;
        else if (mRecordMode == 2 && index == 5)
            offSet = -(Xs[2] - Xs[1]) / 2  - (Xs[3] - Xs[2]) / 2;
        else if (mRecordMode == 5 && index == 0)
            offSet = (Xs[1] - Xs[0]) / 2 + (Xs[2] - Xs[1]) + (Xs[3] - Xs[2]) / 2;
        else if (mRecordMode == 5 && index == 2)
            offSet = (Xs[2] - Xs[1]) / 2 + (Xs[3] - Xs[2]) / 2;

        // 切换动画
        valueAnimator = ValueAnimator.ofFloat(0, offSet);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float offSet = (float) animation.getAnimatedValue();
                mShootButton.setX(Xs[0] + offSet);
                mClickButton.setX(Xs[1] + offSet);
                mDoubleViewButton.setX(Xs[2] + offSet);
            }
        });
        valueAnimator.start();

        // 录制按键背景
        if (index == RecordType.CAPTURE) {
            mSpeedButton.setVisibility(GONE);
            mSpeedModeBar.setVisibility(GONE);
            mChangeAudioLayout.setVisibility(GONE);
            mSimultaneouslyLayer.setVisibility(GONE);

            findViewById(R.id.lsq_camera_radio_layer).setVisibility(View.VISIBLE);
        } else if (index == RecordType.SHORT_CLICK_RECORD) {
            mSpeedButton.setVisibility(VISIBLE);
            mSpeedModeBar.setVisibility(isSpeedChecked ? VISIBLE : GONE);
            mChangeAudioLayout.setVisibility(VISIBLE);
            mSimultaneouslyLayer.setVisibility(GONE);

            findViewById(R.id.lsq_camera_radio_layer).setVisibility(View.VISIBLE);
        } else if (index == RecordType.DOUBLE_VIEW_RECORD) {
            mSpeedButton.setVisibility(VISIBLE);
            mSpeedModeBar.setVisibility(isSpeedChecked ? VISIBLE : GONE);
            mChangeAudioLayout.setVisibility(VISIBLE);
            mSimultaneouslyLayer.setVisibility(VISIBLE);

            if (mDelegate != null)
                mDelegate.selectVideo();

            findViewById(R.id.lsq_camera_radio_layer).setVisibility(View.GONE);
        }
        if (index != RecordType.DOUBLE_VIEW_RECORD) {
            if (mDelegate != null) mDelegate.updateDoubleViewMode(DoubleViewMode.None);
            mSelectAudio.setVisibility(View.VISIBLE);
        } else if ( index == RecordType.DOUBLE_VIEW_RECORD){
            mSelectAudio.setVisibility(View.GONE);
        }
        updateRecordButtonResource(index);
        mRecordMode = index;
    }

    /**
     * 获取底部拍摄模式按键宽度
     */
    private float[] getModeButtonWidth() {
        float[] Xs = new float[4];
        Xs[0] = mShootButton.getX();
        Xs[1] = mClickButton.getX();
        Xs[2] = mDoubleViewButton.getX();
        Xs[3] = mDoubleViewButton.getX() + mDoubleViewButton.getWidth();
        return Xs;
    }

    /**
     * 切换速率
     *
     * @param selectedSpeedMode
     */
    private void selectSpeedMode(double selectedSpeedMode) {
        int childCount = mSpeedModeBar.getChildCount();

        for (int i = 0; i < childCount; i++) {
            Button btn = (Button) mSpeedModeBar.getChildAt(i);
            double speedMode = Double.parseDouble((String) btn.getTag());

            if (selectedSpeedMode == speedMode) {
                btn.setBackgroundResource(R.drawable.tusdk_view_widget_speed_button_bg);
            } else {
                btn.setBackgroundResource(0);
            }
        }

        getDelegate().changedSpeed(selectedSpeedMode);

//        // 切换相机速率
//        TuSdkRecorderVideoCamera.SpeedMode speedMode = TuSdkRecorderVideoCamera.SpeedMode.values()[selectedSpeedMode];
//        mCamera.setSpeedMode(speedMode);
    }

    /**
     * 设置显示隐藏控件（速度按键）
     *
     * @param isVisible 是否可见 true显示false隐藏
     */
    private void setSpeedViewVisible(boolean isVisible) {
        isSpeedChecked = isVisible;
        if (isVisible) {
            setTextButtonDrawableTop(mSpeedButton, R.drawable.video_nav_ic_speed_selected);
            mSpeedModeBar.setVisibility(VISIBLE);
        } else {
            setTextButtonDrawableTop(mSpeedButton, R.drawable.video_nav_ic_speed);
            mSpeedModeBar.setVisibility(GONE);
        }
    }

    /****************************** 视图控制 ****************************/

    /**
     * 设置按键图片
     *
     * @param textButton 按键
     * @param id         图片id
     */
    private void setTextButtonDrawableTop(TextView textButton, @DrawableRes int id) {
        Drawable top = getResources().getDrawable(id);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        textButton.setCompoundDrawables(null, top, null, null);
    }

    /**
     * 底部控件是否可见 滤镜、美颜、贴纸切换时
     *
     * @param isVisible 是否可见
     */
    private void setBottomViewVisible(boolean isVisible) {
        mBottomBarLayout.setVisibility(isVisible ? VISIBLE : GONE);
        mRecordButton.setVisibility(isVisible ? VISIBLE : GONE);
        mRecordModeBarLayout.setVisibility(isVisible && mDelegate.getFragmentSize() <= 0 ? VISIBLE : GONE);
        mRollBackButton.setVisibility(isVisible && mDelegate.getFragmentSize() > 0 ? VISIBLE : GONE);
    }

    /**
     * 设置显示隐藏控件（录制、非录制状态下）
     *
     * @param isVisible 是否可见
     */
    private void setViewHideOrVisible(boolean isVisible) {
        int visibleState = isVisible ? VISIBLE : GONE;

        mTopBar.setVisibility(visibleState);
        mSpeedModeBar.setVisibility(isVisible && isSpeedChecked ? visibleState : GONE);
        mBottomBarLayout.setVisibility(visibleState);
        mRecordModeBarLayout.setVisibility(visibleState);
        mConfirmButton.setVisibility(GONE);
        mRollBackButton.setVisibility(GONE);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);

        if (mDelegate.getFragmentSize() > 0) {
            layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            mConfirmButton.setVisibility(visibleState);
            mRollBackButton.setVisibility(visibleState);
            mRecordModeBarLayout.setVisibility(GONE);
        }
        mFilterButton.setLayoutParams(layoutParams);
    }

    /**
     * 改变录制按钮视图
     *
     * @param type
     */
    private void updateRecordButtonResource(int type) {
        switch (type) {
            case RecordType.CAPTURE:
                mRecordButton.setBackgroundResource(R.drawable.tusdk_view_widget_shoot);
                mRecordButton.setImageResource(0);
                break;
            case RecordType.SHORT_CLICK_RECORD:
                mRecordButton.setBackgroundResource(R.drawable.tusdk_view_widget_record_unpressed);
                mRecordButton.setImageResource(R.drawable.video_ic_recording);
                break;
            case RecordType.SHORT_CLICK_RECORDING:
                mRecordButton.setBackgroundResource(R.drawable.tusdk_view_widget_record_pressed);
                mRecordButton.setImageResource(R.drawable.video_ic_recording);
                break;
            case RecordType.DOUBLE_VIEW_RECORD:
                mRecordButton.setBackgroundResource(R.drawable.tusdk_view_widget_record_unpressed);
                mRecordButton.setImageResource(R.drawable.video_ic_recording);
                break;

        }
    }

    /**
     * 设置滤镜视图
     *
     * @param isVisible 是否可见
     */
    private void setFilterContentVisible(boolean isVisible) {
        mFilterContent.setVisibility(isVisible ? VISIBLE : INVISIBLE);
    }


    /********************************** 回调事件 ***********************/

    /**
     * 录制状态改变回调
     *
     * @param
     * @param recording 是否正在录制中
     */
    public void updateMovieRecordState(RecordState state, boolean recording) {

        if (state == RecordState.Recording) // 开始录制
        {
            updateRecordButtonResource(RecordType.SHORT_CLICK_RECORDING);
            setViewHideOrVisible(false);
            mMoreConfigLayout.setVisibility(GONE);
            setTextButtonDrawableTop(mMoreButton, false ? R.drawable.video_nav_ic_more_selected : R.drawable.video_nav_ic_more);
            mSelectAudio.setVisibility(View.GONE);
            mSimultaneouslyLayer.setVisibility(View.VISIBLE);
            canChangeLayer = false;

            switch (mCurrentDoubleViewMode){
                case None:
                    break;
                case ViewInView:
                    mTopBottomMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_gray));
                    mLeftRightMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_gray));
                    mViewInViewMode.setTextColor(getContext().getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                    break;
                case TopBottom:
                    mTopBottomMode.setTextColor(getContext().getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                    mLeftRightMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_gray));
                    mViewInViewMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_gray));
                    break;
                case LeftRight:
                    mTopBottomMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_gray));
                    mLeftRightMode.setTextColor(getContext().getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                    mViewInViewMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_gray));
                    break;
            }



        } else if (state == RecordState.Paused) // 已暂停录制
        {
            if (mRecordProgress.getProgress() != 0) {
                addInteruptPoint(TuSdkContext.getDisplaySize().width * mRecordProgress.getProgress());
            }
            mRecordProgress.pauseRecord();
            setViewHideOrVisible(true);
            updateRecordButtonResource(mRecordMode);
        } else if (state == RecordState.RecordCompleted) //录制完成弹出提示（续拍模式下录过程中超过最大时间时调用）
        {
            getDelegate().pauseRecording();
            String msg = getStringFromResource("lsq_record_completed");
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });


            if (mRecordProgress.getProgress() != 0) {
                addInteruptPoint(TuSdkContext.getDisplaySize().width * 0.999f);
            }
            updateRecordButtonResource(mRecordMode);
            setViewHideOrVisible(true);

        } else if (state == RecordState.Saving) // 正在保存视频
        {
            String msg = getStringFromResource("new_movie_saving");
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });

        } else if (state == RecordState.SaveCompleted) {
            String msg = getStringFromResource("lsq_video_save_ok");
            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    if (mRecordMode != RecordType.DOUBLE_VIEW_RECORD){
                        mSelectAudio.setVisibility(View.VISIBLE);
                    } else if (mRecordMode == RecordType.DOUBLE_VIEW_RECORD){
                        mSimultaneouslyLayer.setVisibility(View.VISIBLE);
                        canChangeLayer = true;
                        switch (mCurrentDoubleViewMode){
                            case None:
                                break;
                            case ViewInView:
                                mTopBottomMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_white));
                                mLeftRightMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_white));
                                mViewInViewMode.setTextColor(getContext().getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                                break;
                            case TopBottom:
                                mTopBottomMode.setTextColor(getContext().getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                                mLeftRightMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_white));
                                mViewInViewMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_white));
                                break;
                            case LeftRight:
                                mTopBottomMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_white));
                                mLeftRightMode.setTextColor(getContext().getResources().getColor(R.color.lsq_widget_speedbar_button_bg));
                                mViewInViewMode.setTextColor(getContext().getResources().getColor(R.color.lsq_color_white));
                                break;
                        }
                    }
                }
            });


            updateRecordButtonResource(mRecordMode);
            setViewHideOrVisible(true);
        } else if (state == RecordState.RecordTimeOut) {
            String msg = getStringFromResource("lsq_max_audio_record_time");

            post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });


            updateRecordButtonResource(RecordType.SHORT_CLICK_RECORD);
            setViewHideOrVisible(true);
        }
    }

    /**
     * 添加视频断点标记
     *
     * @param margingLeft
     */
    private void addInteruptPoint(float margingLeft) {
        // 添加断点标记
        View interuptBtn = new View(mContext);
        LayoutParams lp = new LayoutParams(2,
                LayoutParams.MATCH_PARENT);

        interuptBtn.setBackgroundColor(TuSdkContext.getColor("lsq_progress_interupt_color"));
        lp.setMargins((int) Math.ceil(margingLeft), 0, 0, 0);
        interuptBtn.setLayoutParams(lp);
        interuptLayout.addView(interuptBtn);
    }

    /**
     * 录制进度回调
     *
     * @param progress
     * @param durationTime
     */
    public void updateViewOnMovieRecordProgressChanged(float progress, float durationTime) {
        TLog.e("progress -- %s durationTime -- %s", progress, durationTime);
        mRecordProgress.setProgress(progress);
    }

    /**
     * 录制错误时更新视图显示
     *
     * @param error
     * @param isRecording
     */
    public void updateViewOnMovieRecordFailed(/*TuSdkRecorderVideoCamera.RecordError error,*/ boolean isRecording) {
//        if (error == TuSdkRecorderVideoCamera.RecordError.MoreMaxDuration) // 超过最大时间 （超过最大时间是再次调用startRecording时会调用）
//        {
//            String msg = getStringFromResource("max_recordTime") + Constants.MAX_RECORDING_TIME + "s";
//            TuSdk.messageHub().showToast(mContext, msg);
//
//        } else if (error == TuSdkRecorderVideoCamera.RecordError.SaveFailed) // 视频保存失败
//        {
//            String msg = getStringFromResource("new_movie_error_saving");
//            TuSdk.messageHub().showError(mContext, msg);
//        } else if (error == TuSdkRecorderVideoCamera.RecordError.InvalidRecordingTime) {
//            TuSdk.messageHub().showError(mContext, R.string.lsq_record_time_invalid);
//        }
//        setViewHideOrVisible(true);
    }

    /**
     * 录制完成时更新视图显示
     *
     * @param isRecording
     */
    public void updateViewOnMovieRecordComplete(boolean isRecording) {
        TuSdk.messageHub().dismissRightNow();
        String msg = getStringFromResource("new_movie_saved");
        TuSdk.messageHub().showSuccess(mContext, msg);

        // 录制完进度清零(正常录制模式)
        mRecordProgress.clearProgressList();
        setViewHideOrVisible(true);
    }

    /**
     * 获取字符串资源
     *
     * @param fieldName
     * @return
     */
    protected String getStringFromResource(String fieldName) {
        int stringID = this.getResources().getIdentifier(fieldName, "string",
                this.mContext.getPackageName());

        return getResources().getString(stringID);
    }


    public void onResume() {

    }

    public TuSdkSize getWrapSize() {
        return mRegionHandle.getWrapSize();
    }

    public boolean checkEnableMarkSence() {
        Filter reshapeFilter = mCurrentFilterMap.get(SelesParameters.FilterModel.Reshape);

        boolean makeSence = reshapeFilter != null;

        makeSence = makeSence || mController.checkMarkSence();

        return makeSence;
    }

    public void updateAudioNameState(int visibility){
        mSelectAudio.setVisibility(visibility);
    }

    public void setAudioName(String name) {
        mAudioName.setText(name);
    }

    public void updateMinPosition(float leftPercent){
        Button minTimeButton = (Button) findViewById(R.id.lsq_minTimeBtn);
        LayoutParams minTimeLayoutParams = (LayoutParams) minTimeButton.getLayoutParams();
        minTimeLayoutParams.leftMargin = (int) (TuSdkContext.getScreenSize().width * leftPercent)
                - TuSdkContext.dip2px(minTimeButton.getWidth());
        minTimeButton.setLayoutParams(minTimeLayoutParams);
    }

    private boolean checkEnableReshape() {
        TusdkReshapeFilter.PropertyBuilder builder = (TusdkReshapeFilter.PropertyBuilder) mPropertyMap.get(SelesParameters.FilterModel.Reshape);
        return builder.eyelidOpacity + builder.eyemazingOpacity + builder.whitenTeethOpacity + builder.removePouchOpacity + builder.removeWrinklesOpacity + builder.eyeDetailOpacity != 0;
    }
}
