package com.wally.view.demo;

import android.app.Application;

import com.wally.view.demo.model.Engine;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wally on 2017/9/22.
 */

public class CustomApplication extends Application {
    private static CustomApplication sInstance;
    private Engine mEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        mEngine = new Retrofit.Builder()
                .baseUrl("http://7xk9dj.com1.z0.glb.clouddn.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(Engine.class);
    }

    public static CustomApplication getInstance() {
        return sInstance;
    }

    public Engine getEngine() {
        return mEngine;
    }
}
