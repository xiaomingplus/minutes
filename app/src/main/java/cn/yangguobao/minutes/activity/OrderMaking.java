package cn.yangguobao.minutes.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;

public class OrderMaking extends Activity {


    private ImageView dial,sms;
    private TextView telText,nicknameTexk;
    private String tel,nickname,userId,orderId,token,toUserId,score;
    private Button button;
//    int firstStart=0;//确定是刚进入活动，订单的第一次开始，为0


    //设置handle对象接受消息以开启线程

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_making);
//        接收订单信息对象

        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        orderId=pref.getString("orderId","0");
        userId=pref.getString("userId","0");
        token=pref.getString("token","0");
        toUserId=pref.getString("toUserId", "0");

        tel = pref.getString("toUserTel","0");
        nickname = pref.getString("toUserNickname","0");
        score = pref.getString("toUserToScore","0");


        telText = (TextView) findViewById(R.id.new_order_tel_to);
        nicknameTexk = (TextView) findViewById(R.id.orderDetailToUserNickName);
        button = (Button) findViewById(R.id.cancelOrderNowTo);
//
////
//        t1=(TextView) findViewById(R.id.orderDetailToUserNickName);//昵称
//        t2=(TextView) findViewById(R.id.new_order_tel_to);//电话
////
////
////       t3=(R) findViewById(R.id.orderDetailRatingBarTo);//分数  //todo
     dial = (ImageView) findViewById(R.id.to_dial_to);
        sms = (ImageView) findViewById(R.id.to_msg_to);
////
////
        telText.setText(tel);
        nicknameTexk.setText(nickname);


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                requestCancel();
            }
        });
        dial.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "" + tel));
                startActivity(intent);

            }
        });
        sms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Uri uri = Uri.parse("smsto:" +tel);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(sendIntent);
            }
        });



    }

//    protected void getServerLocation() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                while(true)	{
//                    try {
//                        HttpClient httpClient = new DefaultHttpClient();
//                        HttpGet httpGet = new HttpGet("http://minutes.scuinfo.com/api/location?userId="+userId+"&token="+token+"&toUserId="+toUserId);
//                        HttpResponse response = httpClient.execute(httpGet);
//
//
//                        //如果服务器处理成功，即code=200才继续执行
//                        if(response.getStatusLine().getStatusCode()==200){
//                            HttpEntity entity2 = response.getEntity();
//                            String response2 = EntityUtils.toString(entity2, "utf-8");
//                            parseJSONWithJSONObject1(response2);
//
//                        }
//                        else{
//                            Toast.makeText(OrderMaking.this, "服务器出错",
//                                    Toast.LENGTH_SHORT).show();}
//                        Thread.sleep(4000);//每隔4秒刷新一次接单人位置信息
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }//结束while
//
//            }
//        }).start();
//
//    }

//    private void parseJSONWithJSONObject1(String jsonData) {
//
//        try {
//            JSONObject jb = new JSONObject(jsonData);
//            if(jb.getString("code").equals("200")){
//                JSONObject data = new JSONObject(jb.getString("data"));
//                //将服务器上墨卡托坐标转换成经纬度
//                String mercatorX=data.getString("x");
//                String mercatorY=data.getString("y");
//                MinutesUtil.MercatorToBD(Double.valueOf(mercatorX), Double.valueOf(mercatorY));
//
//                //获取当前腿儿的经纬度
//                lat=MinutesUtil.CbdX;//纬度
//                lon=MinutesUtil.CbdY;//经度
//
//			/*
//			//添加新的覆盖物
//			LatLng llS = new LatLng(lat, lon);
//			OverlayOptions ooS = new MarkerOptions().position(llS).icon(mCurrentMarker)
//					.zIndex(16).draggable(false);
//			dMarker = (Marker) (mBaiduMap.addOverlay(ooS));
//			*/
//			/*真正实现的方法不是添加覆盖物，而是将此位置加入数组中，
//			调用openGl进行线段的绘制
//			*/
//                LatLng pt = new LatLng(lat, lon);
//                points.add(pt);
//
//                if(points.size()<=5){
//                    temp.add(pt);
//                    if(points.size()==5){
//                        points.removeAll(temp);
//                    }
//                }
//                //数组中大于5个点时才进行绘制
//                if(points.size()>5){
//
//                    OverlayOptions ooPolyline = new PolylineOptions().width(5).color(0xAAFF0000).points(points).customTexture(mCurrentMarker);
//                    mBaiduMap.addOverlay(ooPolyline);
//                }
//            }
//            else{
//                Toast.makeText(OrderMaking.this, jb.getString("message"),
//                        Toast.LENGTH_SHORT).show();
//
//
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
    //post取消订单请求，只执行一次
    protected void requestCancel() {


        RequestParams params = new RequestParams("userId",userId);
        params.put("token",token);
        params.put("toUserId",toUserId);

        params.put("orderId",orderId);

        Log.i("param", params.toString());
        Http.post("order/from/cancel", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    try {
                        if (response.getInt("code") == 200) {
                            Toast.makeText(OrderMaking.this, "订单已取消！",
                                    Toast.LENGTH_SHORT).show();
                            parseJSONWithJSONObject2(response.toString());

                        }else{
                            Toast.makeText(OrderMaking.this, response.getString("message"),
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
                Toast.makeText(OrderMaking.this, "网络错误", Toast.LENGTH_SHORT).show();
            }

        });





//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    //先post相关参数，再通过get保存？
//                    HttpClient httpClient = new DefaultHttpClient();
//                    HttpPost httpPost = new HttpPost("http://minutes.scuinfo.com/api/from/cancel");
//
//                    List<NameValuePair> params = new ArrayList<NameValuePair>();
//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("orderId", orderId));
//                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
//                    httpPost.setEntity(entity);
//                    HttpResponse response=httpClient.execute(httpPost);
//
//                    //如果服务器处理成功，即code=200才继续执行
//                    if(response.getStatusLine().getStatusCode()==200){
//                        HttpEntity entity2 = response.getEntity();
//                        String response2 = EntityUtils.toString(entity2,"utf-8");
//                        parseJSONWithJSONObject2(response2);
//                    }
//                    else{Toast.makeText(OrderMaking.this, "服务器出错",
//                            Toast.LENGTH_SHORT).show();}
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }

    private void parseJSONWithJSONObject2(String jsonData) {


        try {
            JSONObject jb = new JSONObject(jsonData);
            if(jb.getString("code").equals("200")){
                //取消订单成功，返回下单界面
                Intent intent=new Intent(OrderMaking.this,MainActivity.class);
                startActivity(intent);
                finish();

            }
            else{
                Toast.makeText(OrderMaking.this, jb.getString("message"),
                        Toast.LENGTH_SHORT).show();

            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

//
//    //获取订单当前信息
//    protected void getOrderStatus() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                while(true)	{
//                    try {
//
//                        HttpClient httpClient = new DefaultHttpClient();
//                        HttpGet httpGet = new HttpGet("http://minutes.scuinfo.com/api/detail?userId="+userId+"&token="+token+"&orderId="+orderId);
//                        HttpResponse response = httpClient.execute(httpGet);
//
//                        //如果服务器处理成功，即code=200才继续执行
//                        if(response.getStatusLine().getStatusCode()==200){
//                            HttpEntity entity2 = response.getEntity();
//                            String response2 = EntityUtils.toString(entity2,"utf-8");
//                            parseJSONWithJSONObject3(response2);
//
//                        }
//                        else{Toast.makeText(OrderMaking.this, "服务器出错",
//                                Toast.LENGTH_SHORT).show();}
//                        Thread.sleep(2000);//每隔2秒刷新一次订单信息
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }//结束while
//
//            }
//        }).start();
//
//    }

//    private void parseJSONWithJSONObject3(String jsonData) {
//
//        try {
//            JSONObject jb = new JSONObject(jsonData);
//            if(jb.getString("code").equals("200")){
//                JSONObject data = new JSONObject(jb.getString("data"));
//                String orderStatus=data.getString("status");
//
//                if(orderStatus.equals("已完成")){
//                    //如果订单完成，则进入评分界面
//                    //todo
//                    		Intent intent=new Intent(OrderMaking.this,MainActivity.class);
////                    //评分界面中需要以下三个参数
////                    intent.putExtra("userid", userId);
////                    intent.putExtra("token", token);
////                    intent.putExtra("orderId", orderId);
//                    startActivity(intent);
//                }
//
//                else if(orderStatus.equals("已开始服务")&&firstStart==0){
//                    //如果订单开始
//                    firstStart=1;
//                    String info= "订单已开始！";
//                    Toast.makeText(OrderMaking.this, info,
//                            Toast.LENGTH_SHORT).show();
//                    //设置一个消息，在主线程中启动该线程
//                    //getServerLocation();//开始接受腿儿的位置并绘制
//                    Message message = new Message();
//                    message.what = START_SLOC;
//                    handler.sendMessage(message); // 将Message对象发送出去
//                }
//
//                else if(orderStatus.equals("用户已取消")){
//                    //如果用户已取消
//                    //在上面的方法已实现跳转，此处不写了
//                }
//                else if(orderStatus.equals("已匹配")){
//                    //如果订单已匹配
//                    //进入此页面的时候，订单应该是已匹配的状态的，应显示让用户等待的界面
//                    String info= "已匹配接单人，请等待订单开始！";
//                    Toast.makeText(OrderMaking.this, info,
//                            Toast.LENGTH_SHORT).show();
//
//
//                }
//                else if(orderStatus.equals("接单人已取消")){
//                    //如果接单人已取消
//                    String info= "接单人已取消订单，跳转到下单界面！";
//                    Toast.makeText(OrderMaking.this, info,
//                            Toast.LENGTH_SHORT).show();
////todo
//                    	Intent intent=new Intent(OrderMaking.this,MainActivity.class);
//                    intent.putExtra("userid", userId);
//                    intent.putExtra("token", token);
//                    startActivity(intent);
//
//                }
//            }
//            else{
//                Toast.makeText(OrderMaking.this, jb.getString("message"),
//                        Toast.LENGTH_SHORT).show();
//
//
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }




}
