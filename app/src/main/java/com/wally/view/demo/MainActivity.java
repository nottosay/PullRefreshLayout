package com.wally.view.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.wally.view.DefaultRefreshViewAdapter;
import com.wally.view.WallyRefreshLayout;
import com.wally.view.demo.model.Engine;
import com.wally.view.demo.model.RefreshModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements WallyRefreshLayout.LoadingListener {

    private WallyRefreshLayout mRefreshLayout;
    protected Engine mEngine;
    private int mMorePageNumber = 0;

    private MyAdapter mAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEngine = CustomApplication.getInstance().getEngine();
        mAdapter = new MyAdapter(this);
        mRefreshLayout = (WallyRefreshLayout) findViewById(R.id.refresh_layout);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        mRefreshLayout.setLoadingListener(this);
        mRefreshLayout.setRefreshViewAdapter(new DefaultRefreshViewAdapter(this));

        mEngine.loadInitDatas().enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Call<List<RefreshModel>> call, Response<List<RefreshModel>> response) {
                mAdapter.setDatas(response.body());
            }

            @Override
            public void onFailure(Call<List<RefreshModel>> call, Throwable t) {
            }
        });
    }

    @Override
    public void onRefresh() {
        mMorePageNumber = 0;
        mEngine.loadNewData(1).enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Call<List<RefreshModel>> call, Response<List<RefreshModel>> response) {
                mRefreshLayout.endRefreshing();
                mAdapter.setDatas(response.body());
            }

            @Override
            public void onFailure(Call<List<RefreshModel>> call, Throwable t) {
                mRefreshLayout.endRefreshing();
            }
        });
    }

    @Override
    public void onLoadMore() {
        mMorePageNumber++;
        if (mMorePageNumber > 4) {
            mRefreshLayout.endLoadingMore();
            mRefreshLayout.setNoMore(true);
            return;
        }

        mEngine.loadMoreData(mMorePageNumber).enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Call<List<RefreshModel>> call, Response<List<RefreshModel>> response) {
                mRefreshLayout.endLoadingMore();
                mAdapter.addDatas(response.body());
            }

            @Override
            public void onFailure(Call<List<RefreshModel>> call, Throwable t) {
                mRefreshLayout.endLoadingMore();
            }
        });
    }
}
