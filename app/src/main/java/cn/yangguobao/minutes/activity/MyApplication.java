package cn.yangguobao.minutes.activity;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

public  class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// ÔÚÊ¹ÓÃ SDK ¸÷×é¼äÖ®Ç°³õÊ¼»¯ context ÐÅÏ¢£¬´«Èë ApplicationContext
		SDKInitializer.initialize(this);
	
	}



 }
