package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;

public class UserFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    String tel,nickname,gender,postNickName,postGender,type,userId,token;
    double toScore,fromScore;

    EditText nickNamekeyValue;
    TextView phoneNumberKeyValue;
    Button finish;
    RadioButton radioButtonBoy,radioButtongirl;
    RadioGroup group;
    RatingBar ratingBar;
    TextView nicknameTextview ;


    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }


    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        SharedPreferences pref = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
        userId = pref.getString("userId", "");
        token = pref.getString("token", "");
        type = pref.getString("type","");
        nickNamekeyValue =(EditText) getView().findViewById(R.id.nickNameKeyValue);
        nicknameTextview= (TextView) getView().findViewById(R.id.profile_nickname);
        phoneNumberKeyValue = (TextView) getView().findViewById(R.id.phoneNumberKeyValue);
        group = (RadioGroup) getView().findViewById(R.id.genderButton);
        radioButtonBoy = (RadioButton) getView().findViewById(R.id.radioButtonBoy);
        radioButtongirl = (RadioButton) getView().findViewById(R.id.radioButtonGirl);
        ratingBar = (RatingBar) getView().findViewById(R.id.scoreKeyValue);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#f39c12"), PorterDuff.Mode.SRC_ATOP);
        radioButtonBoy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                postGender = "1";
            }
        });
        radioButtongirl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                postGender = "2";
            }
        });

        finish = (Button) getView().findViewById(R.id.finishButton);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNickName = nickNamekeyValue.getText().toString();

                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getActivity());
                dialog.setTitle("提示");
                dialog.setMessage("确定修改个人信息吗");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RequestParams params = new RequestParams();
                        params.add("userId", userId);
                        params.add("token", token);
                        params.add("nickname", postNickName);
                        params.add("gender", postGender);

                        Http.post("complete", params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);

                                Log.i("json", response.toString());
                                if (statusCode == 200) {
                                    try {
                                        if (response.getInt("code") == 200) {
                                            Toast.makeText(getActivity(), "修改成功！", Toast.LENGTH_SHORT).show();
                                            nickNamekeyValue.setText(postNickName);
                                        } else {
                                            Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                super.onFailure(statusCode, headers, responseString, throwable);
                            }
                        });
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();}

        });  //Button结束


        Http.get("userinfo?userId="+userId+"&token="+token+"", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        Log.i("json", response.toString());
                        if (statusCode == 200) {
                            try {
                                if (response.getInt("code") == 200) {
                                    JSONObject jb = new JSONObject(response.getString("data"));
                                    nickname = jb.getString("nickname");
                                    gender = jb.getString("gender");
                                    tel = jb.getString("tel");
                                    fromScore = jb.getDouble("fromScore");
                                    toScore = jb.getDouble("toScore");

                                    nickNamekeyValue.setText(nickname);
                                    phoneNumberKeyValue.setText(tel);
                                    if(type.equals("1")){
                                        ratingBar.setRating((float)fromScore);
                                    }
                                    if(type.equals("2")){
                                        ratingBar.setRating((float)toScore);
                                    }else{}

                                    if (gender.equals("1") || gender.equals("0")) {
                                        radioButtonBoy.setChecked(true);
                                    }
                                    if (gender.equals("2")) {
                                        radioButtongirl.setChecked(true);
                                    } else {
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {

                            Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                }

        ); //get结束


    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
