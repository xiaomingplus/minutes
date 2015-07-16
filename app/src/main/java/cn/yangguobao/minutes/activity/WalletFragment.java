package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;

public class WalletFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match


    double money;
    String postMoney;
    TextView showMoney;
    EditText editMoney;
    Button postButtonMoney;
    private String userId="1";
    private String token = "debug";


    public WalletFragment() {
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
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }


    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        SharedPreferences pref = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
        userId = pref.getString("userId", "");
        token = pref.getString("token", "");

        showMoney = (TextView) getView().findViewById(R.id.wallet_money);
        editMoney = (EditText) getView().findViewById(R.id.edit_money);
        postButtonMoney = (Button) getView().findViewById(R.id.post_button_money);
        postButtonMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMoney = editMoney.getText().toString();
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getActivity());
                dialog.setTitle("提醒");
                dialog.setMessage("请确认充值金额为:"+postMoney+"元");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RequestParams params = new RequestParams();
                        params.add("userId", userId);
                        params.add("token", token);
                        params.add("money", postMoney);

                        Http.post("pay", params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);

                                Log.i("json", response.toString());
                                if (statusCode == 200) {
                                    try {
                                        if (response.getInt("code") == 200) {
                                            Toast.makeText(getActivity(), "充值成功！", Toast.LENGTH_SHORT).show();
                                            money = money + Double.valueOf(postMoney);
                                            showMoney.setText(String.format("%.1f",money));
                                            editMoney.setText("");
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
                        editMoney.setText("");
                    }
                });
                dialog.show();

            }
        });


        Http.get("money?userId="+userId+"&token="+token+"", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        Log.i("json", response.toString());
                        if (statusCode == 200) {
                            try {
                                if (response.getInt("code") == 200) {
                                    JSONObject jb = new JSONObject(response.getString("data"));
                                    money = jb.getDouble("money");
                                    showMoney.setText(String.format("%.1f",money));


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

        );

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
