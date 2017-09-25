package com.wally.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by wally on 2017/9/22.
 */

public class DefaultRefreshViewAdapter extends BaseRefreshViewAdapter {

    protected Context mContext;


    private TextView mHeaderStatusTv;
    private ImageView mHeaderArrowIv;
    private ProgressBar mHeaderProgressBar;
    private RotateAnimation mUpAnim;
    private RotateAnimation mDownAnim;

    private String mPullDownRefreshText = "下拉刷新";
    private String mReleaseRefreshText = "释放更新";
    private String mRefreshingText = "加载中...";
    private View mRefreshHeaderView;


    /*
     * 上拉加载更多控件
    */
    protected View mLoadMoreFooterView;
    private TextView mTvFooterStatus;
    private ProgressBar mFooterProgressBar;


    public DefaultRefreshViewAdapter(Context mContext) {
        this.mContext = mContext;
        initAnimation();
    }

    private void initAnimation() {
        mUpAnim = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mUpAnim.setDuration(150);
        mUpAnim.setFillAfter(true);

        mDownAnim = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDownAnim.setFillAfter(true);
    }

    @Override
    public View getRefreshHeaderView() {
        if (mRefreshHeaderView == null) {
            mRefreshHeaderView = View.inflate(mContext, R.layout.layout_default_refresh_header, null);
            mRefreshHeaderView.setBackgroundColor(Color.TRANSPARENT);
            mHeaderStatusTv = (TextView) mRefreshHeaderView.findViewById(R.id.tv_normal_refresh_header_status);
            mHeaderArrowIv = (ImageView) mRefreshHeaderView.findViewById(R.id.iv_normal_refresh_header_arrow);
            mHeaderProgressBar = (ProgressBar) mRefreshHeaderView.findViewById(R.id.proBar_normal_refresh_header);
            mHeaderStatusTv.setText(mPullDownRefreshText);
        }
        return mRefreshHeaderView;
    }

    @Override
    public void handleScale(float scale, int moveYDistance) {

    }

    @Override
    public void changeToIdle() {

    }

    @Override
    public void changeToPullDown() {
        mHeaderStatusTv.setText(mPullDownRefreshText);
        mHeaderProgressBar.setVisibility(View.INVISIBLE);
        mHeaderArrowIv.setVisibility(View.VISIBLE);
        mDownAnim.setDuration(150);
        mHeaderArrowIv.startAnimation(mDownAnim);
    }

    @Override
    public void changeToReleaseRefresh() {
        mHeaderStatusTv.setText(mReleaseRefreshText);
        mHeaderProgressBar.setVisibility(View.INVISIBLE);
        mHeaderArrowIv.setVisibility(View.VISIBLE);
        mHeaderArrowIv.startAnimation(mUpAnim);
    }

    @Override
    public void changeToRefreshing() {
        mHeaderStatusTv.setText(mRefreshingText);
        // 必须把动画清空才能隐藏成功
        mHeaderArrowIv.clearAnimation();
        mHeaderArrowIv.setVisibility(View.INVISIBLE);
        mHeaderProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEndRefreshing() {
        mHeaderStatusTv.setText(mPullDownRefreshText);
        mHeaderProgressBar.setVisibility(View.INVISIBLE);
        mHeaderArrowIv.setVisibility(View.VISIBLE);
        mDownAnim.setDuration(0);
        mHeaderArrowIv.startAnimation(mDownAnim);
    }


    @Override
    public View getLoadMoreFooterView() {
        if (mLoadMoreFooterView == null) {
            mLoadMoreFooterView = View.inflate(mContext, R.layout.layout_default_refresh_footer, null);
            mFooterProgressBar = (ProgressBar) mLoadMoreFooterView.findViewById(R.id.proBar_normal_refresh_footer);
            mTvFooterStatus = (TextView) mLoadMoreFooterView.findViewById(R.id.tv_normal_refresh_footer_status);
            mTvFooterStatus.setText("加载中...");
        }
        return mLoadMoreFooterView;
    }

    @Override
    public void changeToLoadingMore() {
        mLoadMoreFooterView.setOnClickListener(null);
        mTvFooterStatus.setText("加载中...");
        mFooterProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEndLoadingMore() {
        mLoadMoreFooterView.setOnClickListener(null);
    }

    @Override
    public void onLoadingMoreError(final WallyRefreshLayout.LoadingListener loadingListener) {
        mLoadMoreFooterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingListener.onLoadMore();
            }
        });
        mTvFooterStatus.setText("加载失败，请重试");
        mFooterProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void changeToNoMore() {
        mLoadMoreFooterView.setOnClickListener(null);
        mTvFooterStatus.setText("没有更多数据了");
        mFooterProgressBar.setVisibility(View.GONE);
    }
}
