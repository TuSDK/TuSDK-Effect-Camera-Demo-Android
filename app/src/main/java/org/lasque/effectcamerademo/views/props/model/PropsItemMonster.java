package org.lasque.effectcamerademo.views.props.model;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.effectcamerademo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 5:58 PM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
// 哈哈镜道具
public class PropsItemMonster extends PropsItem {

    /** 哈哈镜类型 */
    private String mMonsterFaceType;
    /** 缩略图名称 */
    private String mThumbName;


    public PropsItemMonster(String monsterFaceType) {
        this.mMonsterFaceType = monsterFaceType;
    }

    /**
     * 设置缩略图名称
     *
     * @param thumbName
     */
    public void setThumbName(String thumbName) {
        this.mThumbName = thumbName;
    }

    /**
     * 获取缩略图名称
     *
     * @return
     */
    public String getThumbName() {
        return mThumbName;
    }

    public String getMonsterCode(){
        return mMonsterFaceType;
    }
}

