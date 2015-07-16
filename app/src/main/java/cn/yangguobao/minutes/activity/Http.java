package cn.yangguobao.minutes.activity;

/**
 * Created by GuobaoYang on 15/7/11.
*/

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class Http {
    private static final String BASE_URL = "http://minutes.scuinfo.com/api/";
//    private static final String BASE_URL = "http://10.0.2.2:9871/api/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }


    private static String getAbsoluteUrl(String relativeUrl) {
        Log.i("url:",BASE_URL + relativeUrl);
        return BASE_URL + relativeUrl;
    }
}

