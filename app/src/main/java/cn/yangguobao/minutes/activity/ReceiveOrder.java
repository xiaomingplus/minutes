package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.yangguobao.minutes.R;

public class ReceiveOrder extends Activity implements OnGetGeoCoderResultListener {

    String orderId="0",userId,token,localScore="0";
    String tel;//get接单人信息


    Button startOrder,cancelOrder;
    ImageView callToCus,sendToCus;
    TextView cusName,cusPhone,cusScore;
    double currentLat,currentLon;//保存当前的经纬度，之后再进行墨卡托转换
    String currentX,currentY;
    private LocationManager locationManager;
    private String provider;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    String placePost;//给接口发送的地点信息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_order);

        //开启广播监听
        Context context = getApplicationContext();
        XGPushManager.registerPush(context);
        //因为小米屏蔽广播，需要加以下语句开启
        Intent service = new Intent(context, XGPushService.class);
        context.startService(service);

        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        orderId=pref.getString("orderId",orderId);
        userId=pref.getString("userId",userId);
        token=pref.getString("token",token);
        localScore=pref.getString("localScore",localScore);//最开始需要初始化一下


        if(orderId!="0"){

            Http.get("order/detail?orderId=" + orderId + "&userId=" + userId + "&token=" + token , new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    if (statusCode == 200) {
                        Log.i("orderInfo",response.toString());

                        try {
                            if (response.getInt("code") == 200) {


                                JSONObject data = response.getJSONObject("data");
                                tel = data.getString("fromTel");

                                android.widget.RatingBar ratingBar = (android.widget.RatingBar) findViewById(R.id.orderDetailRatingBar);
                                LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
                                stars.getDrawable(2).setColorFilter(Color.parseColor("#f39c12"), PorterDuff.Mode.SRC_ATOP);
                                ratingBar.setRating((Float.valueOf(data.getString("fromScore"))));

                                cusName =(TextView) findViewById(R.id.orderDetailUserNickName);

                                cusPhone=(TextView) findViewById(R.id.new_order_tel);

                                cusName.setText(data.getString("fromNickname"));
                                cusPhone.setText(data.getString("fromTel"));



                                //当orderId不为0时，就判断接收到了订单，才开始
                                getCusInfo();
                                // 初始化搜索模块，注册事件监听
                                mSearch = GeoCoder.newInstance();
                                mSearch.setOnGetGeoCodeResultListener(ReceiveOrder.this);


                                startOrder=(Button)findViewById(R.id.startOrderNow);
                                cancelOrder=(Button)findViewById(R.id.cancelOrderNow);
                                sendToCus=(ImageView) findViewById(R.id.to_msg);
                                callToCus=(ImageView) findViewById(R.id.to_dial);

                                startOrder.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {

                                        final ProgressDialog progressDialog = new ProgressDialog
                                                (ReceiveOrder.this);
                                        progressDialog.setTitle("正在开始...");
                                        progressDialog.setMessage("请耐心等待...");
                                        progressDialog.setCancelable(true);
                                        progressDialog.show();

                                        //点击之后，获取用户当前位置
                                        locationManager = (LocationManager) getSystemService(Context.	 LOCATION_SERVICE);
                                        // 获取所有可用的位置?供器
                                        List<String> providerList = locationManager.getProviders(true);
                                        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                                            provider = LocationManager.GPS_PROVIDER;
                                        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                                            provider = LocationManager.NETWORK_PROVIDER;
                                        } else {

                                            return;
                                        }
                                        Location location = locationManager.getLastKnownLocation(provider);
                                        currentLat=location.getLatitude();
                                        currentLon=location.getLongitude();
                                        LatLng ptCenter = new LatLng(currentLat, currentLon);
                                        progressDialog.dismiss();

                                        // 反Geo搜索，在其监听器里实现开始订单的方法
                                        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                                                .location(ptCenter));

                                    }
                                });
                                cancelOrder.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {

                                        requestCancel();
                                    }
                                });
                                callToCus.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:"+""+tel));
                                        startActivity(intent);

                                    }
                                });
                                sendToCus.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        Uri uri = Uri.parse("smsto:" +tel);
                                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(sendIntent);
                                    }
                                });


                            }else{

                                Toast.makeText(ReceiveOrder.this, response.getString("message"), Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                        Toast.makeText(ReceiveOrder.this, "网络错误2", Toast.LENGTH_SHORT);
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Toast.makeText(ReceiveOrder.this, "网络错误", Toast.LENGTH_SHORT);
                    Log.i("fail",responseString);
                }
            });



        }//结束if
    }

    //发送订单开始信息  ！！！完成！！！
    protected void startOrder() {

//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x", currentX));
//                    params.add(new BasicNameValuePair("y", currentY));
//                    params.add(new BasicNameValuePair("orderId", orderId));
//                    params.add(new BasicNameValuePair("place", placePost));

        RequestParams params = new RequestParams("userId",userId);
        params.put("token",token);
        params.put("x",currentX);
        params.put("y", currentY);
        params.put("orderId",orderId);
        params.put("place",placePost);

        Log.i("param",params.toString());
        Http.post("order/start", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    try {
                        if (response.getInt("code") == 200) {
                            Toast.makeText(ReceiveOrder.this, "订单成功开始！",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ReceiveOrder.this, ServerActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(ReceiveOrder.this, response.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("fail", responseString);

                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(ReceiveOrder.this, "网络错误", Toast.LENGTH_SHORT).show();
            }

        });

    }

    //post取消订单请求，只执行一次 ！！！完成！！！
    protected void requestCancel() {


        RequestParams params = new RequestParams("userId",userId);
        params.put("token",token);
        params.put("orderId",orderId);

        Log.i("param",params.toString());
        Http.post("order/to/cancel", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {

                    parseJSONWithJSONObject2(response.toString());

                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("fail", responseString);

                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(ReceiveOrder.this, "网络错误", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void parseJSONWithJSONObject2(String jsonData) {


        try {
            JSONObject jb = new JSONObject(jsonData);
            if(jb.getString("code").equals("200")){
                //取消订单成功
                //作取消订单相关处理
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString("orderId", "0");
                editor.putString("localOrderId","0");
                editor.commit();

                Toast.makeText(ReceiveOrder.this, "取消成功",
                        Toast.LENGTH_SHORT).show();


//todo                这里要让它重启自己的活动
                Intent intent = new Intent(ReceiveOrder.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
            else{
                Toast.makeText(ReceiveOrder.this, jb.getString("message"),
                        Toast.LENGTH_SHORT).show();

            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    //获取(订单）下单人的相关信息
    protected void getCusInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)	{

                    try {
                        //使用get方法获取http entity文件，再调用函数解析json

                        Log.i("url","http://minutes.scuinfo.com/api/order/detail?userId="+userId+"&token="+token+"&orderId="+orderId);

                        HttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet("http://minutes.scuinfo.com/api/order/detail?userId="+userId+"&token="+token+"&orderId="+orderId);
                        HttpResponse httpResponse = httpClient.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            // 请求和响应都成功了
                            HttpEntity entity = httpResponse.getEntity();
                            String response = EntityUtils.toString(entity,"utf-8");

                            JSONObject jb = new JSONObject(response);
                            if(jb.getString("code").equals("200")){
                                JSONObject data = new JSONObject(jb.getString("data"));
                                /////////   				//cusScoreS=data.getString("fromScore"); 没有！！
                                if(data.getString("status")=="用户已取消"){

                                    SharedPreferences.Editor editor = getSharedPreferences("data",
                                            MODE_PRIVATE).edit();
                                    editor.putString("orderId", "0");
                                    editor.putString("localOrderId", "0");
                                    editor.commit();
                                    Intent intent = new Intent(ReceiveOrder.this, LoadActivity.class);
                                    startActivity(intent);
                                    finish();

                                    //todo 跳转
                                    Thread.interrupted();

                                }
                            }else{
                                Toast.makeText(ReceiveOrder.this, jb.getString("message"), Toast.LENGTH_LONG)
                                        .show();
                            }

                        }
                        Thread.sleep(3000);//每隔三秒获取订单最新状态
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }//结束while
            }
        }).start();

    }


    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

            return;
        }
    }
    //反地理编码监听器实现方法
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            placePost="未知地点";
            MinutesUtil.changToMercator(currentLat,currentLon);
            currentX=MinutesUtil.getCXS();
            currentY=MinutesUtil.getCYS();
            //点击按钮开始订单
            startOrder();

            return;
        }else {

            placePost = result.getAddress();
            MinutesUtil.changToMercator(currentLat, currentLon);
            currentX = MinutesUtil.getCXS();
            currentY = MinutesUtil.getCYS();
            //点击按钮开始订单
            startOrder();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    ////生命周期？？？？？


    @Override
    protected void onDestroy() {

        XGPushManager.unregisterPush(this);
        mSearch.destroy();
        super.onDestroy();
    }


}

