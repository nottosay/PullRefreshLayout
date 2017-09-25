package com.wally.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

import com.wally.view.utils.RefreshScrollingUtil;

import java.lang.reflect.Field;


/**
 * 下拉刷新和上拉加载更多布局
 * Created by wally on 2017/9/22.
 */
public class WallyRefreshLayout extends ViewGroup {

    private BaseRefreshViewAdapter mRefreshViewAdapter;


    /**
     * 下拉刷新控件
     */
    private View mRefreshHeaderView;

    /**
     * 上拉加载更多控件
     */
    private View mLoadMoreFooterView;

    /**
     * 下拉刷新控件的高度
     */
    private int mRefreshHeaderViewHeight;


    /**
     * 加载更多控件的高度
     */
    private int mLoadMoreViewHeight;
    /**
     * 当前刷新状态
     */
    private int mCurrentRefreshStatus = BaseRefreshViewAdapter.IDLE;


    private LoadingListener mLoadingListener;

    private RefreshScaleListener mRefreshScaleListener;

    /**
     * 整个头部控件最小的paddingTop
     */
    private int mCurrentHeaderViewTop;

    /**
     * 整个头部控件最大的paddingTop
     */
    private int mMaxHeaderViewPaddingTop;


    /**
     * 整个loadMore控件最小的paddingTop
     */
    private int mCurrentLoadMoreViewTop;

    /**
     * 是否处于正在加载更多状态
     */
    private boolean mIsLoadingMore = false;

    private AbsListView mAbsListView;
    private ScrollView mScrollView;
    private RecyclerView mRecyclerView;
    private View mNormalView;
    private WebView mWebView;

    private View mContentView;

    private float mInterceptTouchDownX = -1;
    private float mInterceptTouchDownY = -1;
    /**
     * 记录开始下拉刷新时的downY
     */
    private int mRefreshDownY = -1;

    /**
     * 是否已经设置内容控件滚动监听器
     */
    private boolean mIsInitedContentViewScrollListener = false;
    /**
     * 触发上拉加载更多时是否显示加载更多控件
     */
    private boolean mIsShowLoadingMoreView = true;

    /**
     * 下拉刷新是否可用
     */
    private boolean mPullDownRefreshEnable = true;

    private Handler mHandler;


    private int mTouchSlop;
    private boolean noMore;


    public WallyRefreshLayout(Context context) {
        this(context, null);
    }

    public WallyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mHandler = new Handler(Looper.getMainLooper());
    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() != 1) {
            throw new RuntimeException(WallyRefreshLayout.class.getSimpleName() + "必须有且只能有一个子控件");
        }

        mContentView = getChildAt(0);
        if (mContentView instanceof AbsListView) {
            mAbsListView = (AbsListView) mContentView;
        } else if (mContentView instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) mContentView;
        } else if (mContentView instanceof ScrollView) {
            mScrollView = (ScrollView) mContentView;
        } else if (mContentView instanceof WebView) {
            mWebView = (WebView) mContentView;
        } else {
            mNormalView = mContentView;
            // 设置为可点击，否则在空白区域无法拖动
            mNormalView.setClickable(true);
        }
    }


    public void setRefreshViewAdapter(BaseRefreshViewAdapter refreshViewAdapter) {
        mRefreshViewAdapter = refreshViewAdapter;
        initRefreshView();
    }

    /**
     * 初始化下拉刷新控件
     */
    private void initRefreshView() {
        mRefreshHeaderView = mRefreshViewAdapter.getRefreshHeaderView();
        if (mRefreshHeaderView != null) {
            addView(mRefreshHeaderView);
        }

        mLoadMoreFooterView = mRefreshViewAdapter.getLoadMoreFooterView();
        if (mLoadMoreFooterView != null) {
            addView(mLoadMoreFooterView);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // 被添加到窗口后再设置监听器，这样开发者就不必烦恼先初始化RefreshLayout还是先设置自定义滚动监听器
        if (!mIsInitedContentViewScrollListener && mLoadMoreFooterView != null) {
            setRecyclerViewOnScrollListener();
            setAbsListViewOnScrollListener();
            mIsInitedContentViewScrollListener = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mCurrentHeaderViewTop + mRefreshHeaderViewHeight - mCurrentLoadMoreViewTop, MeasureSpec.EXACTLY);
        mContentView.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshHeaderView.measure(widthMeasureSpec, 0);
        if (mRefreshHeaderViewHeight == 0) {
            mRefreshHeaderViewHeight = mRefreshHeaderView.getMeasuredHeight();
            mCurrentHeaderViewTop = -mRefreshHeaderViewHeight;
            mMaxHeaderViewPaddingTop = (int) (mRefreshHeaderViewHeight * mRefreshViewAdapter.getSpringDistanceScale());
        }
        mLoadMoreFooterView.measure(widthMeasureSpec, 0);
        mLoadMoreViewHeight = mLoadMoreFooterView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();

        if (mRefreshHeaderView != null) {
            mRefreshHeaderView.layout(left, top + mCurrentHeaderViewTop, left + width - right, top + mCurrentHeaderViewTop + mRefreshHeaderViewHeight);
        }

        if (mLoadMoreFooterView != null) {
            mLoadMoreFooterView.layout(left, top + height - bottom - mCurrentLoadMoreViewTop, left + width - right, top + height - bottom - mCurrentLoadMoreViewTop + mLoadMoreViewHeight);
        }


        mContentView.layout(left, top + mCurrentHeaderViewTop + mRefreshHeaderViewHeight, left + width - right, top + height - bottom - mCurrentLoadMoreViewTop);
    }

    private void setRecyclerViewOnScrollListener() {
        if (mRecyclerView != null) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if ((newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) && shouldHandleRecyclerViewLoadingMore(mRecyclerView)) {
                        beginLoadingMore();
                    }
                }
            });
        }
    }

    private void setAbsListViewOnScrollListener() {
        if (mAbsListView != null) {
            try {
                // 通过反射获取开发者自定义的滚动监听器，并将其替换成自己的滚动监听器，触发滚动时也要通知开发者自定义的滚动监听器（非侵入式，不让开发者继承特定的控件）
                // mAbsListView.getClass().getDeclaredField("mOnScrollListener")获取不到mOnScrollListener，必须通过AbsListView.class.getDeclaredField("mOnScrollListener")获取
                Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
                field.setAccessible(true);
                // 开发者自定义的滚动监听器
                final AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener) field.get(mAbsListView);
                mAbsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                        if ((scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) && shouldHandleAbsListViewLoadingMore(mAbsListView)) {
                            if (noMore) {
                                showNoMoreView();
                            } else {
                                beginLoadingMore();
                            }
                        } else {
                            hideNoMoreView();
                        }

                        if (onScrollListener != null) {
                            onScrollListener.onScrollStateChanged(absListView, scrollState);
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {
                        if (onScrollListener != null) {
                            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean shouldHandleAbsListViewLoadingMore(AbsListView absListView) {
        if (mIsLoadingMore || mCurrentRefreshStatus == BaseRefreshViewAdapter.REFRESHING || mLoadMoreFooterView == null || mLoadingListener == null || absListView == null || absListView.getAdapter() == null || absListView.getAdapter().getCount() == 0) {
            return false;
        }

        return RefreshScrollingUtil.isAbsListViewToBottom(absListView);
    }

    public boolean shouldHandleRecyclerViewLoadingMore(RecyclerView recyclerView) {
        if (mIsLoadingMore || mCurrentRefreshStatus == BaseRefreshViewAdapter.REFRESHING || mLoadMoreFooterView == null || mLoadingListener == null || recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) {
            return false;
        }
        return RefreshScrollingUtil.isRecyclerViewToBottom(recyclerView);
    }

    /**
     * 是否满足处理刷新的条件
     */
    private boolean shouldHandleLoadingMore() {
        if (mIsLoadingMore || mCurrentRefreshStatus == BaseRefreshViewAdapter.REFRESHING || mLoadMoreFooterView == null || mLoadingListener == null) {
            return false;
        }

        // 内容是普通控件，满足
        if (mNormalView != null) {
            return true;
        }

        if (RefreshScrollingUtil.isWebViewToBottom(mWebView)) {
            return true;
        }

        if (RefreshScrollingUtil.isScrollViewToBottom(mScrollView)) {
            return true;
        }

        if (mAbsListView != null) {
            return shouldHandleAbsListViewLoadingMore(mAbsListView);
        }

        if (mRecyclerView != null) {
            return shouldHandleRecyclerViewLoadingMore(mRecyclerView);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInterceptTouchDownX = event.getRawX();
                mInterceptTouchDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsLoadingMore && (mCurrentRefreshStatus != BaseRefreshViewAdapter.REFRESHING)) {
                    if (mInterceptTouchDownX == -1) {
                        mInterceptTouchDownX = (int) event.getRawX();
                    }
                    if (mInterceptTouchDownY == -1) {
                        mInterceptTouchDownY = (int) event.getRawY();
                    }

                    int interceptTouchMoveDistanceY = (int) (event.getRawY() - mInterceptTouchDownY);
                    // 可以没有上拉加载更多，但是必须有下拉刷新，否则就不拦截事件
                    if (Math.abs(event.getRawX() - mInterceptTouchDownX) < Math.abs(interceptTouchMoveDistanceY) && mRefreshHeaderView != null) {
                        if ((interceptTouchMoveDistanceY > mTouchSlop && shouldHandleRefresh()) || (interceptTouchMoveDistanceY < -mTouchSlop && shouldHandleLoadingMore())) {

                            // ACTION_DOWN时没有消耗掉事件，子控件会处于按下状态，这里设置ACTION_CANCEL，使子控件取消按下状态
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            super.onInterceptTouchEvent(event);
                            return true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 重置
                mInterceptTouchDownX = -1;
                mInterceptTouchDownY = -1;
                break;
        }

        return super.onInterceptTouchEvent(event);
    }

    /**
     * 是否满足处理刷新的条件
     */
    private boolean shouldHandleRefresh() {
        if (!mPullDownRefreshEnable || mIsLoadingMore || mCurrentRefreshStatus == BaseRefreshViewAdapter.REFRESHING || mRefreshHeaderView == null || mLoadingListener == null) {
            return false;
        }

        return isContentViewToTop();
    }

    private boolean isContentViewToTop() {
        // 内容是普通控件，满足
        if (mNormalView != null) {
            return true;
        }

        if (RefreshScrollingUtil.isScrollViewOrWebViewToTop(mWebView)) {
            return true;
        }

        if (RefreshScrollingUtil.isScrollViewOrWebViewToTop(mScrollView)) {
            return true;
        }

        if (RefreshScrollingUtil.isAbsListViewToTop(mAbsListView)) {
            return true;
        }

        if (RefreshScrollingUtil.isRecyclerViewToTop(mRecyclerView)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mRefreshHeaderView) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mRefreshDownY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (handleActionMove(event)) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (handleActionUpOrCancel(event)) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }


    /**
     * 处理手指滑动事件
     *
     * @return true表示自己消耗掉该事件，false表示不消耗该事件
     */
    private boolean handleActionMove(MotionEvent event) {
        if (mCurrentRefreshStatus == BaseRefreshViewAdapter.REFRESHING || mIsLoadingMore) {
            return false;
        }

        if (mRefreshDownY == -1) {
            mRefreshDownY = (int) event.getY();
        }

        int refreshDiffY = (int) event.getY() - mRefreshDownY;
        refreshDiffY = (int) (refreshDiffY / mRefreshViewAdapter.getPaddingTopScale());

        // 如果是向下拉，并且当前可见的第一个条目的索引等于0，才处理整个头部控件的padding
        if (refreshDiffY > 0 && shouldHandleRefresh()) {
            int paddingTop = -mRefreshHeaderViewHeight + refreshDiffY;
            if (paddingTop > 0 && mCurrentRefreshStatus != BaseRefreshViewAdapter.READY_REFRESH) {
                // 下拉刷新控件完全显示，并且当前状态没有处于释放开始刷新状态
                mCurrentRefreshStatus = BaseRefreshViewAdapter.READY_REFRESH;
                handleRefreshStatusChanged();

                mRefreshViewAdapter.handleScale(1.0f, refreshDiffY);

                if (mRefreshScaleListener != null) {
                    mRefreshScaleListener.onRefreshScaleChanged(1.0f, refreshDiffY);
                }
            } else if (paddingTop < 0) {
                // 下拉刷新控件没有完全显示，并且当前状态没有处于下拉刷新状态
                if (mCurrentRefreshStatus != BaseRefreshViewAdapter.PULL_DOWN) {
                    boolean isPreRefreshStatusNotIdle = mCurrentRefreshStatus != BaseRefreshViewAdapter.IDLE;
                    mCurrentRefreshStatus = BaseRefreshViewAdapter.PULL_DOWN;
                    if (isPreRefreshStatusNotIdle) {
                        handleRefreshStatusChanged();
                    }
                }
                float scale = 1 - paddingTop * 1.0f / -mRefreshHeaderViewHeight;
                /**
                 * 往下滑
                 * paddingTop    mCurrentHeaderViewTop 到 0
                 * scale         0 到 1
                 * 往上滑
                 * paddingTop    0 到 mCurrentHeaderViewTop
                 * scale         1 到 0
                 */
                mRefreshViewAdapter.handleScale(scale, refreshDiffY);

                if (mRefreshScaleListener != null) {
                    mRefreshScaleListener.onRefreshScaleChanged(scale, refreshDiffY);
                }
            }
            paddingTop = Math.min(paddingTop, mMaxHeaderViewPaddingTop);
            mCurrentHeaderViewTop = paddingTop;

            requestLayout();

            if (mRefreshViewAdapter.isCanChangeToRefreshing()) {
                mRefreshDownY = -1;

                beginRefreshing();
            }

            return true;
        }

        return false;
    }

    /**
     * 处理手指抬起事件
     *
     * @return true表示自己消耗掉该事件，false表示不消耗该事件
     */
    private boolean handleActionUpOrCancel(MotionEvent event) {
        boolean isReturnTrue = false;

        // 如果当前头部刷新控件没有完全隐藏，则需要返回true，自己消耗ACTION_UP事件
        if (mCurrentHeaderViewTop != -mRefreshHeaderViewHeight) {
            isReturnTrue = true;
        }

        if (mCurrentRefreshStatus == BaseRefreshViewAdapter.PULL_DOWN || mCurrentRefreshStatus == BaseRefreshViewAdapter.IDLE) {
            // 处于下拉刷新状态，松手时隐藏下拉刷新控件
            if (mCurrentHeaderViewTop < 0 && mCurrentHeaderViewTop > -mRefreshHeaderViewHeight) {
                hiddenRefreshHeaderView();
            }
            mCurrentRefreshStatus = BaseRefreshViewAdapter.IDLE;
            handleRefreshStatusChanged();
        } else if (mCurrentRefreshStatus == BaseRefreshViewAdapter.READY_REFRESH) {
            // 处于松开进入刷新状态，松手时完全显示下拉刷新控件，进入正在刷新状态
            beginRefreshing();
        }

        if (mRefreshDownY == -1) {
            mRefreshDownY = (int) event.getY();
        }
        int diffY = (int) event.getY() - mRefreshDownY;
        if (shouldHandleLoadingMore() && diffY <= 0) {
            // 处理上拉加载更多，需要返回true，自己消耗ACTION_UP事件
            isReturnTrue = true;
            if (noMore) {
                showNoMoreView();
            } else {
                beginLoadingMore();
            }
        }

        mRefreshDownY = -1;
        return isReturnTrue;
    }

    /**
     * 处理下拉刷新控件状态变化
     */
    private void handleRefreshStatusChanged() {
        switch (mCurrentRefreshStatus) {
            case BaseRefreshViewAdapter.IDLE:
                mRefreshViewAdapter.changeToIdle();
                break;
            case BaseRefreshViewAdapter.PULL_DOWN:
                mRefreshViewAdapter.changeToPullDown();
                break;
            case BaseRefreshViewAdapter.READY_REFRESH:
                mRefreshViewAdapter.changeToReleaseRefresh();
                break;
            case BaseRefreshViewAdapter.REFRESHING:
                mRefreshViewAdapter.changeToRefreshing();
                break;
            default:
                break;
        }
    }

    /**
     * 切换到正在刷新状态，会调用delegate的onBGARefreshLayoutBeginRefreshing方法
     */
    public void beginRefreshing() {
        if (mCurrentRefreshStatus != BaseRefreshViewAdapter.REFRESHING && mLoadingListener != null) {
            mCurrentRefreshStatus = BaseRefreshViewAdapter.REFRESHING;
            changeRefreshHeaderViewToZero();
            handleRefreshStatusChanged();
            mLoadingListener.onRefresh();
        }
    }

    /**
     * 结束下拉刷新
     */
    public void endRefreshing() {
        noMore = false;
        if (mCurrentRefreshStatus == BaseRefreshViewAdapter.REFRESHING) {
            mCurrentRefreshStatus = BaseRefreshViewAdapter.IDLE;
            hiddenRefreshHeaderView();
        }
    }

    /**
     * 隐藏下拉刷新控件，带动画
     */
    private void hiddenRefreshHeaderView() {
        ValueAnimator animator = ValueAnimator.ofInt(mCurrentHeaderViewTop, -mRefreshHeaderViewHeight);
        animator.setDuration(mRefreshViewAdapter.getHeaderAnimDuration());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int paddingTop = (int) animation.getAnimatedValue();
                mCurrentHeaderViewTop = paddingTop;
                requestLayout();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                handleRefreshStatusChanged();
                mRefreshViewAdapter.onEndRefreshing();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    /**
     * 设置下拉刷新控件的paddingTop到0，带动画
     */
    private void changeRefreshHeaderViewToZero() {
        ValueAnimator animator = ValueAnimator.ofInt(mRefreshHeaderView.getPaddingTop(), 0);
        animator.setDuration(mRefreshViewAdapter.getHeaderAnimDuration());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int paddingTop = (int) animation.getAnimatedValue();
                mCurrentHeaderViewTop = paddingTop;
                requestLayout();
            }
        });
        animator.start();
    }

    /**
     * 开始上拉加载更多，会触发delegate的onBGARefreshLayoutBeginRefreshing方法
     */
    public void beginLoadingMore() {
        if (!mIsLoadingMore && mLoadMoreFooterView != null && mLoadingListener != null) {
            mIsLoadingMore = true;
            mLoadingListener.onLoadMore();

            if (mIsShowLoadingMoreView) {
                showLoadingMoreView();
            }
        }
    }

    /**
     * 显示上拉加载更多控件
     */
    private void showLoadingMoreView() {
        mRefreshViewAdapter.changeToLoadingMore();
        mCurrentLoadMoreViewTop = mLoadMoreViewHeight;

        RefreshScrollingUtil.scrollToBottom(mScrollView);
        RefreshScrollingUtil.scrollToBottom(mRecyclerView);
        RefreshScrollingUtil.scrollToBottom(mAbsListView);
    }


    /**
     * 显示上拉加载更多控件
     */
    private void showNoMoreView() {
        mRefreshViewAdapter.changeToNoMore();
        mCurrentLoadMoreViewTop = mLoadMoreViewHeight;

        RefreshScrollingUtil.scrollToBottom(mScrollView);
        RefreshScrollingUtil.scrollToBottom(mRecyclerView);
        RefreshScrollingUtil.scrollToBottom(mAbsListView);
    }

    /**
     * 隐藏上拉加载更多控件
     */
    private void hideNoMoreView() {
        mCurrentLoadMoreViewTop = 0;
        requestLayout();
    }

    /**
     * 结束上拉加载更多
     */
    public void endLoadingMore() {
        if (mIsLoadingMore) {
            if (mIsShowLoadingMoreView) {
                // 避免WiFi环境下请求数据太快，加载更多控件一闪而过
                mHandler.postDelayed(mDelayHiddenLoadingMoreViewTask, 300);
            } else {
                mIsLoadingMore = false;
            }
        }
    }

    private Runnable mDelayHiddenLoadingMoreViewTask = new Runnable() {
        @Override
        public void run() {
            mIsLoadingMore = false;
            mRefreshViewAdapter.onEndLoadingMore();
            mCurrentLoadMoreViewTop = 0;
            requestLayout();
        }
    };

    /**
     * 上拉加载更多时是否显示加载更多控件
     *
     * @param isShowLoadingMoreView
     */
    public void setShowLoadingMoreView(boolean isShowLoadingMoreView) {
        mIsShowLoadingMoreView = isShowLoadingMoreView;
    }

    /**
     * 设置下拉刷新是否可用
     *
     * @param pullDownRefreshEnable
     */
    public void setPullDownRefreshEnable(boolean pullDownRefreshEnable) {
        mPullDownRefreshEnable = pullDownRefreshEnable;
    }


    /**
     * 是否处于正在加载更多状态
     */
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    /**
     * 设置是否还有更多数据
     *
     * @param noMore
     */
    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
    }


    /**
     * 设置下拉刷新上拉加载更多代理
     *
     * @param loadingListener
     */
    public void setLoadingListener(LoadingListener loadingListener) {
        mLoadingListener = loadingListener;
    }


    public void setRefreshScaleListener(RefreshScaleListener mRefreshScaleListener) {
        this.mRefreshScaleListener = mRefreshScaleListener;
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }


    public interface RefreshScaleListener {
        /**
         * 下拉刷新控件可见时，处理上下拉进度
         *
         * @param scale         下拉过程0 到 1，回弹过程1 到 0，没有加上弹簧距离移动时的比例
         * @param moveYDistance 整个下拉刷新控件paddingTop变化的值，如果有弹簧距离，会大于整个下拉刷新控件的高度
         */
        void onRefreshScaleChanged(float scale, int moveYDistance);
    }
}