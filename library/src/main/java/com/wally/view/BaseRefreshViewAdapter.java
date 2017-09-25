package com.wally.view;

import android.view.View;

/**
 * Created by wally on 2017/9/22.
 */

public abstract class BaseRefreshViewAdapter {

    public static final int IDLE = 0;
    public static final int PULL_DOWN = 1;
    public static final int READY_REFRESH = 2;
    public static final int REFRESHING = 3;
    /*
     *手指移动距离与下拉刷新控件paddingTop移动距离的比值，默认1.8f
    */
    private float mPullDistanceScale = 1.8f;

    /**
     * 下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值，默认0.4f
     */
    private float mSpringDistanceScale = 0.4f;

    private int mHeaderAnimDuration = 500;


    /*
    * 获取头部下拉刷新控件
    * @return
    */
    public abstract View getRefreshHeaderView();

    /**
     * 下拉刷新控件可见时，处理上下拉进度
     *
     * @param scale         下拉过程0 到 1，回弹过程1 到 0，没有加上弹簧距离移动时的比例
     * @param moveYDistance 整个下拉刷新控件paddingTop变化的值，如果有弹簧距离，会大于整个下拉刷新控件的高度
     */
    public abstract void handleScale(float scale, int moveYDistance);

    /**
     * 进入到未处理下拉刷新状态
     */
    public abstract void changeToIdle();

    /**
     * 进入下拉状态
     */
    public abstract void changeToPullDown();

    /**
     * 进入释放刷新状态
     */
    public abstract void changeToReleaseRefresh();

    /**
     * 进入正在刷新状态
     */
    public abstract void changeToRefreshing();

    /**
     * 结束下拉刷新
     */
    public abstract void onEndRefreshing();


    /*
    * 获取头加载更多控件
    * @return
    */
    public abstract View getLoadMoreFooterView();

    /**
     * 进入加载更多状态
     */
    public abstract void changeToLoadingMore();

    /**
     * 结束上拉加载更多
     */
    public abstract void onEndLoadingMore();

    /**
     * 加载更多失败
     */
    public abstract void onLoadingMoreError(WallyRefreshLayout.LoadingListener loadingListener);

    /**
     * 设置是否还有更多数据
     */
    public abstract void changeToNoMore();


    /**
     * 是处于能够进入刷新状态
     *
     * @return
     */
    public boolean isCanChangeToRefreshing() {
        return false;
    }


    /**
     * 下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值
     *
     * @return
     */
    public float getSpringDistanceScale() {
        return mSpringDistanceScale;
    }

    /**
     * 设置下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值，不能小于0，如果刷新控件比较高，建议将该值设置小一些
     *
     * @param springDistanceScale
     */
    public void setSpringDistanceScale(float springDistanceScale) {
        if (springDistanceScale < 0) {
            throw new RuntimeException("下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值springDistanceScale不能小于0");
        }
        mSpringDistanceScale = springDistanceScale;
    }

    /**
     * 手指移动距离与下拉刷新控件paddingTop移动距离的比值
     *
     * @return
     */
    public float getPaddingTopScale() {
        return mPullDistanceScale;
    }

    /**
     * 设置手指移动距离与下拉刷新控件paddingTop移动距离的比值
     *
     * @param pullDistanceScale
     */
    public void setPullDistanceScale(float pullDistanceScale) {
        mPullDistanceScale = pullDistanceScale;
    }


    /**
     * 获取顶部未满足下拉刷新条件时回弹到初始状态、满足刷新条件时回弹到正在刷新状态、刷新完毕后回弹到初始状态的动画时间，默认为500毫秒
     *
     * @return
     */
    public int getHeaderAnimDuration() {
        return mHeaderAnimDuration;
    }

    /**
     * 设置顶部未满足下拉刷新条件时回弹到初始状态、满足刷新条件时回弹到正在刷新状态、刷新完毕后回弹到初始状态的动画时间，默认为300毫秒
     *
     * @param animDuration
     */
    public void setHeaderAnimDuration(int animDuration) {
        mHeaderAnimDuration = animDuration;
    }


}
