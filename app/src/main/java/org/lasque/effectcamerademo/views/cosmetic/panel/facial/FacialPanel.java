package org.lasque.effectcamerademo.views.cosmetic.panel.facial;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.lasque.tusdkpulse.modules.view.widget.sticker.StickerLocalPackage;
import org.lasque.effectcamerademo.R;
import org.lasque.effectcamerademo.views.cosmetic.CosmeticPanelController;
import org.lasque.effectcamerademo.views.cosmetic.CosmeticTypes;
import org.lasque.effectcamerademo.views.cosmetic.OnItemClickListener;
import org.lasque.effectcamerademo.views.cosmetic.panel.BasePanel;

/**
 * TuSDK
 * org.lasque.effectcamerademo.views.cosmetic.panel.facial
 * droid-sdk-video-refresh
 *
 * @author H.ys
 * @Date 2020/12/16  14:33
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class FacialPanel extends BasePanel {

    private CosmeticTypes.FacialType mCurrentType;
    private FacialAdapter mAdapter;

    public FacialPanel(CosmeticPanelController controller){
        super(controller, CosmeticTypes.Types.Facial);
    }

    @Override
    protected View createView() {
        View panel = LayoutInflater.from(mController.getContext()).inflate(R.layout.cosmetic_facial_panel, null,false);
        final ImageView putAway = panel.findViewById(R.id.lsq_facial_put_away);
        putAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPanelClickListener != null) onPanelClickListener.onClose(mType);
            }
        });
        final ImageView clearLips = panel.findViewById(R.id.lsq_facial_null);
        clearLips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        mAdapter = new FacialAdapter(CosmeticPanelController.mFacialTypes,mController.getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<CosmeticTypes.FacialType, FacialAdapter.FacialViewHolder>() {
            @Override
            public void onItemClick(int pos, FacialAdapter.FacialViewHolder holder, CosmeticTypes.FacialType item) {
                mCurrentType = item;
                mController.getProperty().facialId = StickerLocalPackage.shared().getStickerGroup(mCurrentType.mGroupId).stickers.get(0).stickerId;
                mController.getProperty().facialEnable = 1;
                mController.getProperty().facialOpacity = mController.getEffect().getFilterArg("facialAlpha").getPrecentValue();
                mController.updateProperty();
                mAdapter.setCurrentPos(pos);
                if (onPanelClickListener!= null)onPanelClickListener.onClick(mType);
            }
        });

        RecyclerView itemList = panel.findViewById(R.id.lsq_facial_item_list);
        LinearLayoutManager manager = new LinearLayoutManager(mController.getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        itemList.setLayoutManager(manager);
        itemList.setAdapter(mAdapter);
        itemList.setNestedScrollingEnabled(false);
        return panel;
    }

    @Override
    public void clear() {
        mCurrentType = null;
        mController.getProperty().facialEnable = 0;
        mController.updateProperty();
        mAdapter.setCurrentPos(-1);
        if (onPanelClickListener != null) onPanelClickListener.onClear(mType);
    }
}
