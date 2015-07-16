
package cn.yangguobao.minutes.activity;

/**接单人界面，主要实现：
 * 1.上下线处理
 * 2.实时发送自己位置，在地图上显示轨迹
 * 3.系统匹配后，在界面显示 电话 和 信息 的接口
 * 4.开始订单、取消订单、完成订单
 * 5.进入评分
 * */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.yangguobao.minutes.R;

/**接单人界面，主要实现：
 * 1.上下线处理
 * 2.实时发送自己位置，在地图上显示轨迹
 * 3.系统匹配后，在界面显示 电话 和 信息 的接口
 * 4.完成订单
 * 5.进入评分
 * */
public class ServerActivity extends Activity implements OnGetGeoCoderResultListener {

    Button startButton,finishButton,cancelB2,messageToCus,callToCus;
    String userId,token,currentX,currentY,place;
    String orderStatus;//若获得推送，则将此值改为已匹配
    String orderId;//这应该从推送处获得
    double currentLat,currentLon;//保存当前的经纬度，之后再进行墨卡托转换
    MapView mMapView;
    BaiduMap mBaiduMap;
    BitmapDescriptor mCurrentMarker;
    //百度地图实时定位变量
    LocationClient mLocClient;
    MyLocationListenner myListener;//继承了本来的位置监听器，
    boolean isFirstLoc = true;// 是否首次定位
    //腿儿的点数组
    List<LatLng> points = new ArrayList<LatLng>();
    List<LatLng> temp = new ArrayList<LatLng>();
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        orderId=pref.getString("orderId",orderId);
        userId=pref.getString("userId",userId);
        token=pref.getString("token",token);

        Log.i("cyk",orderId+"xx"+userId+token);
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(ServerActivity.this);        //事件绑定
        finishButton=(Button)findViewById(R.id.orderFinish);
        finishButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.i("tex",arg0.toString());
                //点击按钮结束订单
                //finishOrder();
                //先进行地理搜索，再结束订单
                LatLng ptCenter = new LatLng(currentLat, currentLon);
                Log.i("2",currentX+currentY+"");
                // 反Geo搜索，在其监听器里实现开始订单的方法
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ptCenter));
            }
        });

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.serverMapView);
        mBaiduMap = mMapView.getMap();
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);

        myListener=new MyLocationListenner();
        if(myListener!=null)
            Log.d("SerVerActivity", "myListener对象创建！");
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定义深度
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);
        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型"bd09为墨卡托坐标"
        option.setScanSpan(2000);//设置发起定位请求的间隔时间为1000ms
        mLocClient.setLocOption(option);
        mLocClient.start();

//        sendServerLocation();
        //todo
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server, menu);
        return true;
    }







    //实时发送腿儿的位置信息  !!!完成！！！
    protected void sendServerLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(true)	{
                    try {
                        MinutesUtil.changToMercator(currentLat,currentLon);
                        currentX=MinutesUtil.getCXS();
                        currentY=MinutesUtil.getCYS();
                        //先post相关参数，再通过get保存？
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://minutes.scuinfo.com/api/location");

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("userId", userId));
                        params.add(new BasicNameValuePair("token", token));
                        params.add(new BasicNameValuePair("x", currentX));
                        params.add(new BasicNameValuePair("y", currentY));
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
                        httpPost.setEntity(entity);
                        HttpResponse response=httpClient.execute(httpPost);


                        //如果服务器处理成功，即code=200才继续执行
                        if(response.getStatusLine().getStatusCode()==200){
                            HttpEntity entity2 = response.getEntity();
                            String response2 = EntityUtils.toString(entity2,"utf-8");
                            //parseJSONWithJSONObject1(response2);
                            //地理位置发送成功与否，都不进行操作
                            //完成订单后，将orderId和localScore都复位0
                            Log.d("ServerActivity","地理位置发送成功");
                            SharedPreferences.Editor editor = getSharedPreferences("data",
                                    MODE_PRIVATE).edit();
                            editor.putString("orderId","0");
                            editor.putString("localScore","0");
                            editor.commit();
                        }
                        else{Toast.makeText(ServerActivity.this, "服务器出错",
                                Toast.LENGTH_SHORT).show();}
                        Thread.sleep(3000);//每隔3秒发送一次接单人位置信息

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }//结束while

            }
        }).start();

    }


    //发送订单结束完成信息  ！！！完成！！！
    protected void finishOrder() {
       // params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x", currentX));
//                    params.add(new BasicNameValuePair("y", currentY));
//                    params.add(new BasicNameValuePair("orderId", orderId));
//                    params.add(new BasicNameValuePair("place", place));
        RequestParams params = new RequestParams("userId",userId);
        params.put("token",token);
        params.put("x",currentX);
        params.put("y", currentY);
        params.put("orderId",orderId);
        params.put("place",place);

        Log.i("param",params.toString());
        Http.post("order/finish", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    try {
                        if (response.getInt("code") == 200) {



                            Toast.makeText(ServerActivity.this, "订单已完成，钱已打入你的账户！",
                                    Toast.LENGTH_SHORT).show();

                                                        SharedPreferences.Editor editor = getSharedPreferences("data",
                                    MODE_PRIVATE).edit();
                            editor.putString("orderId", "0");
                            editor.commit();

                            //todo				//订单结束，到评分接口
                            Intent intent=new Intent(ServerActivity.this,MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(ServerActivity.this, response.getString("message"),
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
                Toast.makeText(ServerActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }

        });


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//
//                    HttpClient httpClient = new DefaultHttpClient();
//                    HttpPost httpPost = new HttpPost("http://minutes.scuinfo.com/api/finish");
//
//                    List<NameValuePair> params = new ArrayList<NameValuePair>();
//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x", currentX));
//                    params.add(new BasicNameValuePair("y", currentY));
//                    params.add(new BasicNameValuePair("orderId", orderId));
//                    params.add(new BasicNameValuePair("place", place));
//                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
//                    httpPost.setEntity(entity);
//                    HttpResponse response=httpClient.execute(httpPost);
//
//                    //如果服务器处理成功，即code=200才继续执行
//                    if(response.getStatusLine().getStatusCode()==200){
//                        Log.d("ServerActivity","订单完成成功");
//                        HttpEntity entity2 = response.getEntity();
//                        String response2 = EntityUtils.toString(entity2,"utf-8");
//
//
//                        //创建方法对订单是否成功结束进行处理
//                        JSONObject jb = new JSONObject(response2);
//                        if(jb.getString("code").equals("200")){
//                            //将orderId设为0
//                            SharedPreferences.Editor editor = getSharedPreferences("data",
//                                    MODE_PRIVATE).edit();
//                            editor.putString("orderId", "0");
//                            editor.commit();
//
//                            //todo				//订单结束，到评分接口
//                            Intent intent=new Intent(ServerActivity.this,MainActivity.class);
//                            startActivity(intent);
//
//                        }
//                        else{//打印服务器返回错误
//                            Toast.makeText(ServerActivity.this, jb.getString("message"),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    else{Toast.makeText(ServerActivity.this, "服务器出错",
//                            Toast.LENGTH_SHORT).show();}
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).start();

    }



    //实时监听接单人当前位置内部类
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            //Log.d("CustomerActivity","进入并创建了locationListener!");
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);

            }
            /**绘制轨迹，可以进行算法的设置来优化显示界面，目前绘制效果不佳
             * 若腿儿网络不好，无法实时返回信息的时候，可利用百度地图的路线规划功能
             * 给其进行可能路线的轨迹绘制。之后到达正确路径节点后，再进行重新编排
             * */
            currentLat=location.getLatitude();
            currentLon=location.getLongitude();
            LatLng pt = new LatLng(currentLat,currentLon);
            points.add(pt);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);

            }

            if(points.size()<=5){
                temp.add(pt);
                if(points.size()==5){
                    points.removeAll(temp);
                }
            }
            //数组中大于5个点时才进行绘制



            if(points.size()>5){
                		Log.d("ServerActivity","数组中点数大于1");

                OverlayOptions ooPolyline = new PolylineOptions().width(15).color(0xAAFF0000).points(points).customTexture(mCurrentMarker);
                mBaiduMap.addOverlay(ooPolyline);
                //设置地图中心点

            }


        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult arg0) {
        // TODO 自动生成的方法存根

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

        Log.i("x","xx");
        // TODO 自动生成的方法存根
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            place="未知地点";
            MinutesUtil.changToMercator(currentLat, currentLon);
            currentX=MinutesUtil.getCXS();
            currentY=MinutesUtil.getCYS();
            //结束订单
            finishOrder();

            return;
        }else {

            place = result.getAddress();
            MinutesUtil.changToMercator(currentLat, currentLon);
            currentX = MinutesUtil.getCXS();
            currentY = MinutesUtil.getCYS();
            //结束订单
            finishOrder();
        }
    }

}


