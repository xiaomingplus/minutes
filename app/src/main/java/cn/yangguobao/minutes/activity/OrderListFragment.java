package cn.yangguobao.minutes.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.yangguobao.minutes.R;
import zrc.widget.SimpleFooter;
import zrc.widget.SimpleHeader;
import zrc.widget.ZrcListView;


public class OrderListFragment extends Fragment {

    private ZrcListView listView;
    private Handler handler;
    private ArrayList<Order> orderData;
    private int pageId = -1;
    private TheAdapter adapter;
    private  String userType = "consumer";
    private  String type = "1";
    private  String userId = "1";
    private  String token = "debug";
    private final Integer pageSize = 10;
    public OrderListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        SharedPreferences pref = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
        userId = pref.getString("userId", "");
        token = pref.getString("token", "");
        type = pref.getString("type","");

//        userId="1";
//        token = "debug";
       if(type == "1"){
           userType ="consumer";
       }else{
           userType = "server";
       }

        if(adapter == null)
        {

            listView = (ZrcListView) getView().findViewById(R.id.zListView);
            handler = new Handler();
            // 设置默认偏移量，主要用于实现透明标题栏功能。（可选）
//            float density = getResources().getDisplayMetrics().density;
//            listView.setFirstTopOffset((int) (50 * density));
            // 设置下拉刷新的样式（可选，但如果没有Header则无法下拉刷新）
            SimpleHeader header = new SimpleHeader(getActivity());
            header.setTextColor(0xff0066aa);
            header.setCircleColor(0xff33bbee);
            listView.setHeadable(header);
            // 设置加载更多的样式（可选）
            SimpleFooter footer = new SimpleFooter(getActivity());
            footer.setCircleColor(0xff33bbee);
            listView.setFootable(footer);
            // 设置列表项出现动画（可选）
            listView.setItemAnimForTopIn(R.anim.topitem_in);
            listView.setItemAnimForBottomIn(R.anim.bottomitem_in);
            // 下拉刷新事件回调（可选）
            listView.setOnRefreshStartListener(new ZrcListView.OnStartListener() {
                @Override
                public void onStart() {
                    refresh();
                }
            });
            // 加载更多事件回调（可选）
            listView.setOnLoadMoreStartListener(new ZrcListView.OnStartListener() {
                @Override
                public void onStart() {
                    loadMore();
                }
            });
            adapter = new TheAdapter();
            listView.setAdapter(adapter);
            listView.refresh();

            listView.setOnItemClickListener(new ZrcListView.OnItemClickListener() {
                @Override
                public void onItemClick(ZrcListView parent, View view, int position, long id) {
                    Order order = orderData.get(position);
                    DetailFragment fragment = new DetailFragment();
                    Bundle args = new Bundle();
                    args.putString("userId",userId);
                    args.putString("token", token);
                    args.putSerializable("order", order);
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container_body, fragment);
                    fragmentTransaction.addToBackStack(null);

                    fragmentTransaction.commit();

                    // set the toolbar title
                    ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("详情");

                    /*
                    Order order = orderData.get(position);
                    //进入下单人下单后的界面，传入订单对象
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("order", order);
                    intent.putExtras(bundle);
                    intent.putExtra("userId", userId);
                    intent.putExtra("token", token);
                    startActivity(intent);*/




                }
            });
        }
        listView.setAdapter(adapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_order_list, container, false);



        return rootView;

    }

    private void refresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pageId = 1;
                Http.get("order/list?pageSize=" + pageSize + "&userId=" + userId + "&token=" + token + "&page=" + pageId + "&type=" + userType, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        if (statusCode == 200) {
                            try {
                                if (response.getInt("code") == 200) {
                                    orderData = new ArrayList<Order>();
                                    Order[] initData = new Order[response.getJSONArray("data").length()];
                                    for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                        JSONObject item1 = (JSONObject) response.getJSONArray("data").get(i);
                                        if (type == "1") {
                                            initData[i] = new Order("跑腿人:" + Common.subTitle(item1.getString("toNickname")) + "(￥" + String.format("%.1f", item1.getDouble("price")) + ")", "" + Time.date(item1.getLong("createAt")) + "・" + item1.getString("status"), item1.getString("toNickname"), item1.getString("toTel"), item1.getDouble("toScore"), item1.getString("price"), item1.getString("originPlace"), item1.getString("destinationPlace"), item1.getString("distance"), Time.date(item1.getLong("createAt")), "" + (item1.getInt("takeTime") / 60), item1.getString("status"));

                                        } else {
                                            initData[i] = new Order("客户:" + Common.subTitle(item1.getString("fromNickname")) + "(￥" + String.format("%.1f", item1.getDouble("price")) + ")", "" + Time.date(item1.getLong("createAt")) + "・" + item1.getString("status"), item1.getString("fromNickname"), item1.getString("fromTel"), item1.getDouble("fromScore"), item1.getString("price"), item1.getString("originPlace"), item1.getString("destinationPlace"), item1.getString("distance"), Time.date(item1.getLong("createAt")), "" + (item1.getInt("takeTime") / 60), item1.getString("status"));
                                        }


                                    }
                                    for (Order item : initData) {
                                        orderData.add(item);
                                    }
                                    adapter.notifyDataSetChanged();
                                    listView.setRefreshSuccess("加载成功"); // 通知加载成功
                                    listView.startLoadMore(); // 开启LoadingMore功能

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        } else {

                            Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT);
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT);
                        Log.i("fail",responseString);
                    }
                });
            }
        }, 1 * 1000);
    }

    private void loadMore() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pageId++;
                Http.get("order/list?userId=1&token=debug&pageSize=2&page=" + pageId + "&type=" + userType, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        if (statusCode == 200) {
                            try {
                                if (response.getInt("code") == 200) {
                                    Order[] initData = new Order[response.getJSONArray("data").length()];
                                    for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                        JSONObject item1 = (JSONObject) response.getJSONArray("data").get(i);
                                        if(type=="1"){
                                            initData[i] = new Order("跑腿人:"+ Common.subTitle(item1.getString("toNickname"))   + "(￥" + String.format("%.1f", item1.getDouble("price")) + ")", "" + cn.yangguobao.minutes.activity.Time.date(item1.getLong("createAt")) + "・" + item1.getString("status"),item1.getString("toNickname"),item1.getString("toTel"),item1.getDouble("toScore"),item1.getString("price"),item1.getString("originPlace"),item1.getString("destinationPlace"),item1.getString("distance"), Time.date(item1.getLong("createAt")),""+(item1.getInt("takeTime")/60),item1.getString("status"));

                                        }else{
                                            initData[i] = new Order("客户:"+ Common.subTitle(item1.getString("fromNickname")) + "(￥" + String.format("%.1f", item1.getDouble("price")) + ")", "" + Time.date(item1.getLong("createAt")) + "・" + item1.getString("status"),item1.getString("fromNickname"),item1.getString("fromTel"),item1.getDouble("fromScore"),item1.getString("price"),item1.getString("originPlace"),item1.getString("destinationPlace"),item1.getString("distance"), Time.date(item1.getLong("createAt")),""+(item1.getInt("takeTime")/60),item1.getString("status"));
                                        }

                                    }
                                    if (initData.length > 0) {
                                        for (Order item : initData) {
                                            orderData.add(item);
                                        }
                                        adapter.notifyDataSetChanged();
                                        listView.setLoadMoreSuccess();
                                    } else {
                                        listView.stopLoadMore();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        } else {

                            Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT);
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                });


            }
        }, 2 * 1000);
    }
    private class TheAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return orderData == null ? 0 : orderData.size();
        }

        @Override
        public Object getItem(int position) {
            return orderData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View view;

            if (convertView == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.order_list, null);
            } else {
                view = (View) convertView;
            }
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView description = (TextView) view.findViewById(R.id.description);
            title.setText(orderData.get(position).getTitle());
            description.setText(orderData.get(position).getDescription());
            return view;
        }
    }
}
