package cn.yangguobao.minutes.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;

public class LoadActivity extends ActionBarActivity {
    private String userId = "";
    private String token = "";
    String orderId="0",localScore="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        //开启广播监听
        Context context = getApplicationContext();
        XGPushManager.registerPush(context);
        //因为小米屏蔽广播，需要加以下语句开启
        Intent service = new Intent(context, XGPushService.class);
        context.startService(service);

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                userId = pref.getString("userId", "");
             token = pref.getString("token", "");
        if(userId!="" && token!=""){
            Http.get("userinfo?userId=" + userId + "&token=" + token, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.i("res", response.toString());
                    if (statusCode == 200) {
                        try {
                            if (response.getInt("code") == 200) {

                                XGPushClickedResult click = XGPushManager.onActivityStarted(LoadActivity.this);
                                if (click != null) {

                                    String customContent = click.getCustomContent();
                                    if (customContent != null && customContent.length() != 0) {
                                        try {
                                            JSONObject json = new JSONObject(customContent);

                                            localScore = json.getString("score");//下单人均分
                                            orderId=json.getString("id");
                                            SharedPreferences.Editor editor = getSharedPreferences("data",
                                                    MODE_PRIVATE).edit();
                                            editor.putString("orderId",orderId);
                                            editor.putString("localOrderId", orderId);
                                            editor.putString("localScore", localScore);
                                            editor.commit();
                                            Log.d("xingeKey", orderId);


                                            Intent intent = new Intent(LoadActivity.this, ReceiveOrder.class);
                                            startActivity(intent);

                                            //todo
                                        }catch(JSONException e){
                                            e.printStackTrace();}

                                    }
                                }else{
                                    //进入主页面
                                    Log.i("here","正常启动");

                                    Intent intent = new Intent(LoadActivity.this, MainActivity.class);

                                    startActivity(intent);
                                    finish();
                                }





                            }else{
                                Intent intent = new Intent(LoadActivity.this, StartActivity.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("token", token);
                                startActivity(intent);
                                finish();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }


                }

//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Log.i("fail", responseString);
//                    super.onFailure(statusCode, headers, responseString, throwable);
//                    Toast.makeText(LoadActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(LoadActivity.this, StartActivity.class);
//                    intent.putExtra("userId", userId);
//                    intent.putExtra("token", token);
//                    startActivity(intent);
//                }

               public void onFailure(Throwable e,JSONObject j){

                }

            });




        }else{

            //进入登录页

            Intent intent = new Intent(LoadActivity.this, StartActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("token", token);
            startActivity(intent);
            finish();

        }


        //todo 是否有存帐号密码
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
