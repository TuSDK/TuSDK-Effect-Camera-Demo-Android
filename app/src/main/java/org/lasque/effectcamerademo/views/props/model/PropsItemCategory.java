package org.lasque.effectcamerademo.views.props.model;

import org.lasque.tusdkpulse.core.seles.SelesParameters;

import java.util.ArrayList;
import java.util.List;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.effectcamerademo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 11:20 AM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
// 道具分类
public class PropsItemCategory <Item extends PropsItem>{

    /** 分类名称 */
    private String mName;

    /** 道具列表 */
    private List<Item> mItems;

    /** 道具分类对应的特效类型 */
    private SelesParameters.FilterModel mMediaEffectType;

    public PropsItemCategory(SelesParameters.FilterModel mediaEffectType,List<Item> items) {
        mItems = new ArrayList<>(items);
        this.mMediaEffectType =  mediaEffectType;
    }

    /**
     * 获取分离下的所有道具
     *
     * @return 道具列表
     */
    public List<Item> getItems() {
        return mItems;
    }

    /**
     * 设置分类名称
     *
     * @param name 分离名称
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * 获取分类名称
     *
     * @return
     */
    public String getName() {
        return this.mName;
    }

    /**
     * 道具分类对应的特效类型
     *
     * @return TuSdkMediaEffectData.TuSdkMediaEffectDataType
     */
    public SelesParameters.FilterModel getMediaEffectType() {
        return mMediaEffectType;
    }
}

