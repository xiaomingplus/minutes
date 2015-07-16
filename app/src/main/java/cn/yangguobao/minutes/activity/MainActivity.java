package cn.yangguobao.minutes.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.yangguobao.minutes.R;


public class MainActivity extends ActionBarActivity implements FragmentDrawer.FragmentDrawerListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private String userId;
    private String token;
    private String type;
    private String nickname;
    private DrawerLayout mDrawerLayout;
    double currentLat,currentLon;//保存当前的经纬度，之后再进行墨卡托转换
    String currentX,currentY;
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        userId = pref.getString("userId", "");
        token = pref.getString("token", "");
        type = pref.getString("type","");
        nickname = pref.getString("nickname","");

        Log.i("userinfo",userId+"&"+type+nickname);



        if(userId=="" || token==""){
            finish();
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
        }else {

            mToolbar = (Toolbar) findViewById(R.id.toolbar);

            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            drawerFragment = (FragmentDrawer)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
            drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
            drawerFragment.setDrawerListener(this);

            // display the first navigation drawer view on app launch
            displayView(0);


            TextView nicknameTextview = (TextView) findViewById(R.id.profile_nickname);
            ImageView profile_pic = (ImageView) findViewById(R.id.profile_image);
            nicknameTextview.setText(nickname);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            Log.i("type3",type);

            //todo 下单上单
            if (type.equals("2")) {
                profile_pic.setImageResource(R.drawable.receiver);
            } else {
                profile_pic.setImageResource(R.drawable.consumer);

            }



            nicknameTextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = null;
                    fragment = new UserFragment();
                    String title = getString(R.string.user_profile);
                    if (fragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.commit();

                        // set the toolbar title
                        getSupportActionBar().setTitle(title);
                        mDrawerLayout.closeDrawer(findViewById(R.id.fragment_navigation_drawer));

                    }

                }
            });
            profile_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = null;
                    fragment = new UserFragment();
                    String title = getString(R.string.user_profile);
                    if (fragment != null) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.commit();

                        // set the toolbar title
                        getSupportActionBar().setTitle(title);
                        mDrawerLayout.closeDrawer(findViewById(R.id.fragment_navigation_drawer));

                    }

                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_online) {


            //点击之后，获取用户当前位置
            locationManager = (LocationManager) getSystemService(Context.	 LOCATION_SERVICE);
            // 获取所有可用的位置?供器
            List<String> providerList = locationManager.getProviders(true);
            if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
            } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            } else {
            }
            Location location = locationManager.getLastKnownLocation(provider);
            currentLat=location.getLatitude();
            currentLon=location.getLongitude();

            serverOnline();
            return true;




        }

        if(id == R.id.action_offline){
            serverOffline();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
            displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                if(type.equals("2")){

                    //todo 当前是否有订单

                    SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                    String nowOrderId = pref.getString("orderId", "");

                    Log.i("han",nowOrderId);

                    if(nowOrderId.equals("0")){

                        fragment = new ServerFragment();
                        title = getString(R.string.title_home);
                    }else{
                        Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                        startActivity(intent);
                    }



                }else{

                    fragment = new OrderFragment();
                    title = getString(R.string.title_home);


                }
                break;
            case 1:


                fragment = new WalletFragment();
                title = getString(R.string.title_wallet);
                break;
            case 2:
                fragment = new OrderListFragment();
                title = getString(R.string.title_order_list);
                break;

            case 3:

                RequestParams switchParams = new RequestParams();
                switchParams.put("userId", userId);
                switchParams.put("token",token );
                switchParams.put("type",((type=="1")?"2":"1"));
                Http.post("switch", switchParams, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.i("res", response.toString());
                        if (statusCode == 200) {
                            try {
                                if (response.getInt("code") == 200) {
                                    finish();


                                    if(type.equals("1")){
                                        finish();
                                        //在本地永久保存当前token

                                        Toast.makeText(MainActivity.this, "成功切换为接单人身份", Toast.LENGTH_SHORT).show();
                                        SharedPreferences.Editor editor = getSharedPreferences("data",
                                                MODE_PRIVATE).edit();
                                        editor.putString("type","2");
                                        editor.commit();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        intent.putExtra("userId", userId);
                                        intent.putExtra("token", token);
                                        intent.putExtra("type","2");
                                        startActivity(intent);
                                    }
                                    else if(type.equals("2")){
                                        finish();
                                        Toast.makeText(MainActivity.this, "成功切换为下单人身份", Toast.LENGTH_SHORT).show();
                                        //在本地永久保存当前token
                                        SharedPreferences.Editor editor = getSharedPreferences("data",
                                                MODE_PRIVATE).edit();
                                        editor.putString("type","1");
                                        editor.commit();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        intent.putExtra("userId", userId);
                                        intent.putExtra("token", token);
                                        intent.putExtra("type","1");
                                        startActivity(intent);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    }

                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        Log.i("fail", responseString);
                    }

                });




                break;

            case 4:

                fragment = new HelpFragment();
                title = getString(R.string.title_help);
                break;

            case 5:

                fragment = new AboutFragment();
                title = "关于";

                break;

            case 6:


                RequestParams params = new RequestParams();
                params.put("userId", userId);
                params.put("token",token );
                Http.post("logout", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.i("res", response.toString());
                        if (statusCode == 200) {
                            try {
                                if (response.getInt("code") == 200) {
                                    finish();
                                    SharedPreferences.Editor editor = getSharedPreferences("data",
                                            MODE_PRIVATE).edit();
                                    editor.clear();
                                    editor.commit();
                                    //开启广播监听
                                    Context context = getApplicationContext();

                                    XGPushManager.registerPush(context, "*", new XGIOperateCallback() {

                                        @Override
                                        public void onSuccess(Object o, int i) {
                                            Log.i("res" + i, o.toString());
                                        }

                                        @Override
                                        public void onFail(Object o, int i, String s) {
                                            Log.i("ref" + i + s, o.toString());

                                        }
                                    });

                                    //因为小米屏蔽广播，需要加以下语句开启
                                    Intent service = new Intent(context, XGPushService.class);
                                    context.startService(service);
                                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                    intent.putExtra("userId", userId);
                                    intent.putExtra("token", token);
                                    startActivity(intent);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    }

                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        Log.i("fail", responseString);
                    }

                });



                break;


            case 8:
                fragment = new ReceiveFragment();
                title = "接单";


                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }


    //访问上线接口 ！！完成！！！
    public void serverOnline(){
        final ProgressDialog progressDialog = new ProgressDialog
                (MainActivity.this);
        progressDialog.setTitle("上线中");
        progressDialog.setMessage("请耐心等待...");
        progressDialog.setCancelable(true);
        progressDialog.show();

//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x", currentX));
//                    params.add(new BasicNameValuePair("y", currentY));
                    MinutesUtil.changToMercator(currentLat,currentLon);
                    currentX=MinutesUtil.getCXS();
                    currentY=MinutesUtil.getCYS();
        RequestParams switchParams = new RequestParams();
        switchParams.put("userId", userId);
        switchParams.put("token",token );
        switchParams.put("x",currentX);
        switchParams.put("y",currentY);
        Http.post("enable", switchParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    progressDialog.dismiss();

                    try {
                        if (response.getInt("code") == 200) {
                            Toast.makeText(MainActivity.this, "上线成功，请耐心等待系统派单！", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(MainActivity.this,response.getString("message"), Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    progressDialog.dismiss();

                    Toast.makeText(MainActivity.this,"网络错误", Toast.LENGTH_SHORT).show();

                }


            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                Log.i("fail", responseString);
                progressDialog.dismiss();

            }

        });



//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//
//                    MinutesUtil.changToMercator(currentLat,currentLon);
//                    currentX=MinutesUtil.getCXS();
//                    currentY=MinutesUtil.getCYS();
//
//                    HttpClient httpClient = new DefaultHttpClient();
//                    HttpPost httpPost = new HttpPost("http://minutes.scuinfo.com/api/enable");
//                    List<NameValuePair> params = new ArrayList<NameValuePair>();
//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x", currentX));
//                    params.add(new BasicNameValuePair("y", currentY));
//                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
//                    httpPost.setEntity(entity);
//                    HttpResponse response=httpClient.execute(httpPost);
//
//                    //如果服务器处理成功，即code=200才继续执行
//                    if(response.getStatusLine().getStatusCode()==200){
//                        HttpEntity entity2 = response.getEntity();
//                        String response2 = EntityUtils.toString(entity2, "utf-8");
//
//
//                        //判断是否上线成功
//                        try {
//                            JSONObject jb = new JSONObject(response2);
//                            if(jb.getString("code").equals("200")){
//
//                                Toast.makeText(MainActivity.this, "你已经成功上线！", Toast.LENGTH_SHORT).show();
//                            }
//                            else{
//                                Toast.makeText(MainActivity.this, jb.getString("message"),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }catch (Exception e) {
//                            e.printStackTrace();
//                        }//处理json的catch结束
//
//
//                    }//判断服务器返回成功的If结束
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();


    }

    //访问下线接口 ！！！完成！！！
    public void serverOffline(){
        final ProgressDialog progressDialog = new ProgressDialog
                (MainActivity.this);
        progressDialog.setTitle("下线中");
        progressDialog.setMessage("请耐心等待...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        RequestParams switchParams = new RequestParams();
        switchParams.put("userId", userId);
        switchParams.put("token",token );
        Http.post("disable", switchParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    progressDialog.dismiss();

                    try {
                        if (response.getInt("code") == 200) {
                            Toast.makeText(MainActivity.this, "下线成功！", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(MainActivity.this,response.getString("message"), Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    progressDialog.dismiss();

                    Toast.makeText(MainActivity.this,"网络错误", Toast.LENGTH_SHORT).show();

                }


            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                Log.i("fail", responseString);
                progressDialog.dismiss();

            }

        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("back","ni");
        System.exit(0);//正常退出App

    }
}
