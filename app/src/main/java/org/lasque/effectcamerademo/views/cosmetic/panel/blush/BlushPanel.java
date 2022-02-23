package org.lasque.effectcamerademo.views.cosmetic.panel.blush;

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

import static org.lasque.effectcamerademo.views.cosmetic.CosmeticTypes.Types.Blush;

/**
 * TuSDK
 * org.lasque.effectcamerademo.views.cosmetic.panel.blush
 * droid-sdk-video-refresh
 *
 * @author H.ys
 * @Date 2020/10/20  16:05
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class BlushPanel extends BasePanel {

    private CosmeticTypes.BlushType mCurrentType;
    private BlushAdapter mAdapter;

    public BlushPanel(CosmeticPanelController controller) {
        super(controller, Blush);
    }

    @Override
    protected View createView() {
        final View panel = LayoutInflater.from(mController.getContext()).inflate(R.layout.cosmetic_blush_panel, null,false);
        ImageView putAway = panel.findViewById(R.id.lsq_blush_put_away);
        putAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPanelClickListener != null) onPanelClickListener.onClose(mType);
            }
        });
        ImageView clear = panel.findViewById(R.id.lsq_blush_null);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
        mAdapter = new BlushAdapter(CosmeticPanelController.mBlushTypes, mController.getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<CosmeticTypes.BlushType, BlushAdapter.BlushViewHolder>() {
            @Override
            public void onItemClick(int pos, BlushAdapter.BlushViewHolder holder, CosmeticTypes.BlushType item) {
                mCurrentType = item;
                mController.getBeautyManager().setBlushStickerId(StickerLocalPackage.shared().getStickerGroup(item.mGroupId).stickers.get(0).stickerId);
                mController.getBeautyManager().setBlushOpacity(mController.getEffect().getFilterArg("blushAlpha").getPrecentValue());
                mAdapter.setCurrentPos(pos);
                if (onPanelClickListener != null) onPanelClickListener.onClick(mType);
            }
        });
        RecyclerView itemList = panel.findViewById(R.id.lsq_blush_item_list);
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
        mAdapter.setCurrentPos(-1);
        mController.getBeautyManager().setBlushEnable(false);
        if (onPanelClickListener != null) onPanelClickListener.onClear(mType);
    }
}
