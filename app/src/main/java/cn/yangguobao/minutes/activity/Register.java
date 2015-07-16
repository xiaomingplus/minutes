package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;


public class Register extends Activity {

    private EditText nickName;
    private RadioButton male;
    private RadioButton female;
    private Button submit;
    String gender;
    String postedNickName;
    String userId;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");

        nickName = (EditText) findViewById(R.id.nickName);
        male = (RadioButton) findViewById(R.id.registerGenderBoy);
        female = (RadioButton) findViewById(R.id.registerGenderGirl);


        submit = (Button) findViewById(R.id.submitRegister);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                postedNickName = nickName.getText().toString();
                postPersonInfo();
            }
        });


        male.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                gender = "1";
            }
        });
        female.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                gender = "2";
            }
        });
    }

    public void postPersonInfo() {


        RequestParams params = new RequestParams("userId",userId);
        params.put("token",token);
        params.put("nickname",postedNickName);
        params.put("gender", gender);
        Http.post("complete", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {
                    try {
                        if (response.getInt("code") == 200) {
                            parseJSONWithJSONObject(response.toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("fail", responseString);

                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(Register.this, "网络错误", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void parseJSONWithJSONObject(String jsonData) {

        //ÅÐ¶ÏÊÇ·ñ×¢²á³É¹¦
        try {
            JSONObject jb = new JSONObject(jsonData);
            if (jb.getString("code").equals("200")) {
                finish();
                Intent intent = new Intent(Register.this, ChooseStatus.class);
                intent.putExtra("userId", userId);
                intent.putExtra("token", token);
                startActivity(intent);
            } else {
                Toast.makeText(Register.this, jb.getString("message"),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }


}

