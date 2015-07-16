package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;

public class StartActivity extends Activity {

    private static final int OLD_USER = 0;
    private static final int NEW_USER = 1;
    //添加按钮计时
    private TimeCount time;
    private Button verifyButton;//验证按钮
    private Button loginButton;//登录按钮
    private EditText phoneNum;
    private EditText verifyCode;
    String inputPhone;
    String inputVerify;
    String isUser="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        //找到所输入的号码
        phoneNum=(EditText) findViewById(R.id.phoneNum);
        //找到验证码
        verifyCode=(EditText) findViewById(R.id.verifyInput);

        //设置按钮倒计时
        time = new TimeCount(60000, 1000);//构造CountDownTimer对象
        //找到按钮对象
        verifyButton = (Button) findViewById(R.id.verifyButton);
        loginButton=(Button) findViewById(R.id.loginButton);

        //设置按钮监听
        verifyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO 自动生成的方法存根

                //sendRequestWithHttpClient();
                //String temp=getUrlContent("https://route.showapi.com/6-1?num=18010671901&showapi_appid=just_test_app&showapi_timestamp=1436142304135&showapi_sign=72dfdc9ee6991e685b223733d83878fd",2000);
                //sendRequestWithHttpClient();
                //Log.d("fanhui",temp);
                // System.out.printf("%s",temp);
                inputPhone = phoneNum.getText().toString();
                if(inputPhone.length()!=11){
                    Toast.makeText(StartActivity.this, "请输入正确的号码！",
                            Toast.LENGTH_SHORT).show();}
                else{
                    time.start();//开始计时
                    getVerify();
                }
                //getUserInfo("1","debug");
                //getUserInfoTheee("https://route.showapi.com/6-1?num=18010671901&showapi_appid=just_test_app&showapi_timestamp=1436142304135&showapi_sign=72dfdc9ee6991e685b223733d83878fd");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //从控件中获取文本信息
                inputPhone = phoneNum.getText().toString();
                inputVerify=verifyCode.getText().toString();

                if(inputPhone.length()!=11){Toast.makeText(StartActivity.this, "请输入正确的号码！",
                        Toast.LENGTH_SHORT).show();}
                else{


                    postVerify();//应添加验证码验证


                }
                //getUserInfo("1","debug");
                //getUserInfoTheee("https://route.showapi.com/6-1?num=18010671901&showapi_appid=just_test_app&showapi_timestamp=1436142304135&showapi_sign=72dfdc9ee6991e685b223733d83878fd");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public void getVerify(){
        final ProgressDialog progressDialog = new ProgressDialog
                (StartActivity.this);
        progressDialog.setTitle("正在发送验证码");
        progressDialog.setMessage("请耐心等待...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        RequestParams params = new RequestParams();
        params.put("tel",inputPhone);
        Http.post("valid",params,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                super.onSuccess(statusCode, headers, response);
                Log.i("res",response.toString());
                if (statusCode == 200) {
                    try {
                        if (response.getInt("code") == 200) {
                            Toast.makeText(StartActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                            parseJSONWithJSONObject1(response.toString());
                        }

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }




            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("fail", responseString);
                progressDialog.dismiss();
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(StartActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }

        });





    }

    public void postVerify(){
        final ProgressDialog progressDialog = new ProgressDialog
                (StartActivity.this);
        progressDialog.setTitle("登录中");
        progressDialog.setMessage("请耐心等待...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        RequestParams params = new RequestParams();
        params.put("tel", inputPhone);
        params.put("code",inputVerify);
        Http.post("signin", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();

                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    try {
                        if (response.getInt("code") == 200) {
                            parseJSONWithJSONObject2(response.toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressDialog.dismiss();
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(StartActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                Log.i("fail", responseString);
            }

        });

    }
    //返回获取验证码后，给isUser赋值，并不继续作操作
    private void parseJSONWithJSONObject1(String jsonData) {

        //判断是否已注册
        try {
            JSONObject jb = new JSONObject(jsonData);
            //只进行isUser的赋值
            isUser = new JSONObject(jb.getString("data")).getString("isUser");

            Log.d("StartActivity", "判断用户是不是老用户" + isUser);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    //此函数为登录后，获取服务器信息
    private void parseJSONWithJSONObject2(String jsonData) {


        try {
            JSONObject jb = new JSONObject(jsonData);
            //String token = new JSONObject(jb.getString("data")).getString("token");
            //获取状态码
            String statusCode=jb.getString("code");

            //验证码验证成功的状态
            if(statusCode.equals("200")){
                //判断新老用户，直接进入主界面，传入用户实体
                //若本地存在用户信息，则读取；若无则访问接口
                JSONObject data=new JSONObject(jb.getString("data"));

                String userId=data.getString("userId");
                String token=data.getString("token");
                String nickname = data.getString("nickname");
Log.i("reg",userId);
                //开启广播监听
                Context context = getApplicationContext();

                XGPushManager.registerPush(context,userId,new XGIOperateCallback(){

                    @Override
                    public void onSuccess(Object o, int i) {
                        Log.i("res"+i,o.toString());
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        Log.i("ref"+i+s,o.toString());

                    }
                });

                //因为小米屏蔽广播，需要加以下语句开启
                Intent service = new Intent(context, XGPushService.class);
                context.startService(service);
                //在本地永久保存当前token
                SharedPreferences.Editor editor = getSharedPreferences("data",
                        MODE_PRIVATE).edit();
                editor.putString("userId",userId);
                editor.putString("token", token);
                editor.putString("orderId","0");
                editor.putString("localOrderId","0");
                editor.putString("localScore","0");
                editor.commit();

                if(isUser.equals("true"))
                {
                    //老用户直接进入选择页面
                    finish();
                    Intent intent = new Intent(StartActivity.this, ChooseStatus.class);
                    //测试地图语句
                    //Intent intent = new Intent(StartActivity.this, BaseMap.class);
                    SharedPreferences.Editor editorNickname = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editorNickname.putString("nickname",nickname);
                    editorNickname.commit();


                    //传入userId和token来在主界面中get用户最新信息
                    intent.putExtra("userId", userId);
                    intent.putExtra("token", token);
                    startActivity(intent);
                }else{
                    //若新用户，利用返回的uid和token注册
                    //打开注册activity
                    finish();
                    Intent intent = new Intent(StartActivity.this, Register.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("token", token);
                    startActivity(intent);
                }


            }else
            {

                String meg=jb.getString("message");
                Log.d("StartActivity", meg);
                //等待计时器重新计时，重新获取验证码

            }



        }catch (Exception e) {
            e.printStackTrace();
        }
    }
	/*
	private void sendRequestWithHttpClient(String apiUrl) {
		new Thread(new Runnable() {
		@Override
		public void run() {
			try {
			//使用get方法获取http entity文件，再调用函数解析json
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet("https://route.showapi.com/6-1?num=18010671901&showapi_appid=just_test_app&showapi_timestamp=1436142304135&showapi_sign=72dfdc9ee6991e685b223733d83878fd");
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
			// 请求和响应都成功了
			HttpEntity entity = httpResponse.getEntity();
			String response = EntityUtils.toString(entity,"utf-8");
			parseJSONWithJSONObject(response);
			}
			} catch (Exception e) {
			e.printStackTrace();
			}
			}
			}).start();
			}
			*/
	/*
protected String readString(InputStream in) throws Exception {
    byte[]data = new byte[1024];
    int length = 0;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    while((length=in.read(data))!=-1){
        bout.write(data,0,length);
    }
    return new String(bout.toByteArray(),"UTF-8");

}
*/

	/*
	//将开启http的线程进行了封装
	public static void getUserInfoTheee(String apiUrl) {

		getJsonThread getUserInfoThread =new getJsonThread(apiUrl);
		new Thread(getUserInfoThread).start();
			}

*/

    //按钮计时类
    class TimeCount extends CountDownTimer {


        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }
        @Override
        public void onFinish() {//计时完毕时触发
            verifyButton.setText("重新验证");
            verifyButton.setClickable(true);
        }
        @Override
        public void onTick(long millisUntilFinished){//计时过程显示

            verifyButton.setClickable(false);
            verifyButton.setText(millisUntilFinished /1000+"秒");

        }

    }



}
