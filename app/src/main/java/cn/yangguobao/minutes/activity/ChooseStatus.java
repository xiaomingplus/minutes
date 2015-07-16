package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.yangguobao.minutes.R;

public class ChooseStatus extends Activity {


    private ImageView customer;
    private ImageView serve;
    String type;
    String userId;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_status);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");

        customer = (ImageView) findViewById(R.id.user_consumers);
        serve = (ImageView) findViewById(R.id.user_receivers);

        customer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                type="1";
                enterNext();
            }
        });

        serve.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                type="2";
                enterNext();
            }
        });
    }

    protected void enterNext() {
        final ProgressDialog progressDialog = new ProgressDialog
                (ChooseStatus.this);
        progressDialog.setTitle("正在进入Minutes的世界");
        progressDialog.setMessage("请耐心等待...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("http://minutes.scuinfo.com/api/switch");

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("userId", userId));
                    params.add(new BasicNameValuePair("token", token));
                    params.add(new BasicNameValuePair("type", type));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
                    httpPost.setEntity(entity);
                    HttpResponse response=httpClient.execute(httpPost);

                    if(response.getStatusLine().getStatusCode()==200){
                        progressDialog.dismiss();
                        HttpEntity entity2 = response.getEntity();
                        String response2 = EntityUtils.toString(entity2, "utf-8");
                        parseJSONWithJSONObject(response2);


                    }
                    else{
                        progressDialog.dismiss();

                        Toast.makeText(ChooseStatus.this, "网络错误",
                            Toast.LENGTH_SHORT).show();}
                } catch (Exception e) {
                    progressDialog.dismiss();

                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void parseJSONWithJSONObject(String jsonData) {

        try {
            JSONObject jb = new JSONObject(jsonData);
            if(jb.getString("code").equals("200")){
                if(type.equals("1")){
                    finish();


                    //在本地永久保存当前token
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editor.putString("type","1");
                    editor.commit();

                    Intent intent = new Intent(ChooseStatus.this, MainActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("token", token);
                    startActivity(intent);
                }
                else if(type.equals("2")){
                    finish();
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            MODE_PRIVATE).edit();
                    editor.putString("type","2");
                    editor.commit();
                    Intent intent = new Intent(ChooseStatus.this, MainActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("token", token);
                    startActivity(intent);
                }
            }
            else{
                Toast.makeText(ChooseStatus.this, jb.getString("message"),
                        Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
