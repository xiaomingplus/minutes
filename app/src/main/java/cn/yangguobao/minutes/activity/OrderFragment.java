package cn.yangguobao.minutes.activity;

        import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import cn.yangguobao.minutes.R;

public class OrderFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    //百度地图实时定位变量
    LocationClient mLocClient;
    MyLocationListenner myListener;//继承了本来的位置监听器，

    MapView mMapView;
    BaiduMap mBaiduMap;
    BitmapDescriptor mCurrentMarker;

    double startX;
    double startY;
    String userId;
    String token;
    //地理编码搜索转换
    Button searchIntent;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    //覆盖物对象
    //BitmapDescriptor serverObject = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
    boolean isFirstLoc = true;// 是否首次定位
    //自动完成文本对象
    AutoCompleteTextView keyWorldsView=null;
    //poi查询和在线建议查询
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private ArrayAdapter<String> sugAdapter = null;//建议适配器，用来显示在线建议结果
    //可退拽的目的地覆盖物maker
    private Marker dMarker=null;

    //下单处理，将出发地坐标发出去即可
    Button makeOrder;

    public OrderFragment() {
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
        return inflater.inflate(R.layout.fragment_order, container, false);
    }


    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        //获取上个活动传来的userId和token
/*
        Intent intent = getIntent();
	     userId = intent.getStringExtra("userId");
	     token = intent.getStringExtra("token");
	*/
//        userId = "1";
//        token = "debug";

        // 地图初始化
        SharedPreferences pref = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
        userId=pref.getString("userId", userId);
        token=pref.getString("token", token);

        mMapView = (MapView) getView().findViewById(R.id.cusView111);
        mBaiduMap = mMapView.getMap();

        if(mBaiduMap!=null)

            //设置自己所在位置当前覆盖物的图片
            mCurrentMarker = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.NORMAL, true, null));


        myListener=new MyLocationListenner();
        if(myListener!=null)
            Log.d("CustomerActivity", "myListener对象创建！");
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定义深度
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);
        // 定位初始化
        mLocClient = new LocationClient(getActivity().getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型"bd09为墨卡托坐标"
        option.setScanSpan(2000);//设置发起定位请求的间隔时间为1000ms
        mLocClient.setLocOption(option);
        mLocClient.start();
        Log.d("CustomerActivity","locationClient返回的状态码"+Integer.toString(mLocClient.requestLocation()));

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng arg0) {
                mBaiduMap.clear();
                OverlayOptions ooS = new MarkerOptions().position(arg0).icon(mCurrentMarker)
                        .zIndex(16).draggable(true);
                dMarker = (Marker) (mBaiduMap.addOverlay(ooS));
                //实时将坐标返回给全局变量
                startX=dMarker.getPosition().latitude;//纬度
                startY=dMarker.getPosition().longitude;//经度
            }

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                // TODO 自动生成的方法存根
                return false;
            }

        });
        //设置目的地覆盖物监听
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(
                        getActivity(),
                        "拖拽结束，新位置：" + marker.getPosition().latitude + ", "
                                + marker.getPosition().longitude,
                        Toast.LENGTH_LONG).show();
            }

            public void onMarkerDragStart(Marker marker) {
            }
        });
        // 初始化地理搜索模块，注册事件监听，实现其地理编码和反地理编码方法
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

            //实现OnGetGeoCoderResultListener的方法
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getActivity(), "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                mBaiduMap.clear();
						/*
						mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.icon_geo)));
						*/

                LatLng llS = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
                OverlayOptions ooS = new MarkerOptions().position(llS).icon(mCurrentMarker)
                        .zIndex(16).draggable(true);
                dMarker = (Marker) (mBaiduMap.addOverlay(ooS));
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                        .getLocation()));
                //实时将出发地坐标返回到全局变量中
                startX=result.getLocation().latitude;
                startY=result.getLocation().longitude;
                String strInfo = String.format("纬度：%f 经度：%f",
                        result.getLocation().latitude, result.getLocation().longitude);
                Log.d("CustomerActivity",strInfo);
                //Toast.makeText(CustomerActivity.this, strInfo, Toast.LENGTH_LONG).show();


            }

            //反地理编码方法
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
						/*
						if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
							//Toast.makeText(CustomerActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
							return;
						}
						mBaiduMap.clear();
						mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.icon_geo)));
						mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
								.getLocation()));
						Toast.makeText(CustomerActivity.this, result.getAddress(),
								Toast.LENGTH_LONG).show();
				*/
            }
        });
        //获取搜索按钮对象，并添加事件监听
        searchIntent=(Button) getView().findViewById(R.id.button999);
        searchIntent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                keyWorldsView = (AutoCompleteTextView) getView().findViewById(R.id.editText11);
                // 实际地理位置搜索
                mSearch.geocode(new GeoCodeOption().city(
                        "成都").address(
                        keyWorldsView.getText().toString()));
						/*并不需要兴趣点查询
						mPoiSearch.searchInCity((new PoiCitySearchOption())
								.city("成都")
								.keyword(keyWorldsView.getText().toString())
								.pageNum(1));
								*/
            }
        });
        makeOrder=(Button) getView().findViewById(R.id.makeOrder);
        makeOrder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //点击下单后，将下单信息发送给服务器
                sendToServe();

                //开启新的活动，暂定为用户等待接单界面

            }

        });



        // 初始化搜索模块，注册搜索事件监听

        //实现poi查询监听器
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener(){

            @Override
            public void onGetPoiDetailResult(PoiDetailResult result) {
                if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getActivity(), result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                            .show();
                }

            }

            @Override
            public void onGetPoiResult(PoiResult result) {
                if (result == null
                        || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Toast.makeText(getActivity(), "未找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                //如果查询结果无误，则进行处理
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
							/*
							mBaiduMap.clear();
							PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
							mBaiduMap.setOnMarkerClickListener(overlay);
							overlay.setData(result);
							overlay.addToMap();
							overlay.zoomToSpan();
							*/
                    Toast.makeText(getActivity(), "查询成功，之后添加覆盖物即可", Toast.LENGTH_LONG)
                            .show();
                    return;
                }


            }

        });

        //实现在线建议查询监听器
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener(){
            @Override
            public void onGetSuggestionResult(SuggestionResult res) {
                if (res == null || res.getAllSuggestions() == null) {
                    return;
                }
                sugAdapter.clear();
                for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                    if (info.key != null)
                        sugAdapter.add(info.key);
                }
                sugAdapter.notifyDataSetChanged();
            }
        });

        keyWorldsView = (AutoCompleteTextView) getView().findViewById(R.id.editText11);
        sugAdapter = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_list);
        keyWorldsView.setAdapter(sugAdapter);
        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                //此处发出了在线搜索请求，上面的监听器将会收到并处理
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(cs.toString()).city("成都"));
            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    //下单处理
    protected void sendToServe() {
//        final ProgressDialog progressDialog = new ProgressDialog
//                (getActivity());
//        progressDialog.setTitle("正在查询附近的跑腿人");
//        progressDialog.setMessage("请耐心等待...");
//        progressDialog.setCancelable(true);
//        progressDialog.show();
//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x",MinutesUtil.CXS));
//                    params.add(new BasicNameValuePair("y", MinutesUtil.CYS));
                   MinutesUtil.changToMercator(startX, startY);
        RequestParams params = new RequestParams("userId",userId);
        params.put("token",token);
        params.put("x",MinutesUtil.CXS);
        params.put("y", MinutesUtil.CYS);

        Log.i("param", params.toString());
        Http.post("order", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("res", response.toString());
                if (statusCode == 200) {

//                    progressDialog.dismiss();
                    try {
                        if (response.getInt("code") == 200) {
                            Toast.makeText(getActivity(), "已成功匹配到跑腿人！",
                                    Toast.LENGTH_SHORT).show();


    JSONObject data = response.getJSONObject("data");
Log.i("info",data.toString());
                            SharedPreferences.Editor xeditor = getActivity().getSharedPreferences("data",
                                    getActivity().MODE_PRIVATE).edit();


                            Log.i("userinfo",data.getString("toUserNickname")+data.getString("toUserTel")+data.getString("toUserToScore"));
                            xeditor.putString("toUserNickname", data.getString("toUserNickname"));
                            xeditor.putString("toUserTel",data.getString("toUserTel"));
                            xeditor.putString("toUserToScore",data.getString("toUserToScore"));
                            xeditor.putString("orderId",data.getString("orderId"));
                            xeditor.commit();


                            Intent intent = new Intent(getActivity(),OrderMaking.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), response.getString("message"),
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
                Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
            }

        });



//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//
//                    MinutesUtil.changToMercator(startX, startY);
//                    //	MinutesUtil.MercatorToBD(Double.valueOf(MinutesUtil.CXS), Double.valueOf(MinutesUtil.CYS));
//                    //先post相关参数，再通过get保存？
//                    HttpClient httpClient = new DefaultHttpClient();
//                    HttpPost httpPost = new HttpPost("http://minutes.scuinfo.com/api/order");
//
//                    List<NameValuePair> params = new ArrayList<NameValuePair>();
//                    params.add(new BasicNameValuePair("userId", userId));
//                    params.add(new BasicNameValuePair("token", token));
//                    params.add(new BasicNameValuePair("x",MinutesUtil.CXS));
//                    params.add(new BasicNameValuePair("y", MinutesUtil.CYS));
//
//                    Log.i("目标坐标：",MinutesUtil.CXS+","+MinutesUtil.CYS);
//
//                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
//                    httpPost.setEntity(entity);
//                    HttpResponse response=httpClient.execute(httpPost);
//
//                    //如果服务器处理成功，即code=200才继续执行
//                    if(response.getStatusLine().getStatusCode()==200){
//
//                        progressDialog.dismiss();
//                        HttpEntity entity2 = response.getEntity();
//                        String response2 = EntityUtils.toString(entity2, "utf-8");
//                        parseJSONWithJSONObject(response2);
//
//                        Log.d("CustomerActivity", "服务器返回成功");
//                        Log.d("CustomerActivity",MinutesUtil.CXS);
//                    }
//                    else{
//                        progressDialog.dismiss();
//
//                        Toast.makeText(getActivity(), "服务器下单出错", Toast.LENGTH_SHORT).show();}
//                } catch (Exception e) {
//                    progressDialog.dismiss();
//
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }

    private void parseJSONWithJSONObject(String jsonData) {


        try {
            JSONObject jb = new JSONObject(jsonData);
            Log.d("CustomerActivity","succcccccc");
            if(jb.getString("code").equals("200")){

                JSONObject data=new JSONObject(jb.getString("data"));

                //若处理成功，则进入下个活动
                OrderInfo oi=new OrderInfo();
                oi.setOrderId(data.getString("orderId"));
                oi.setToUserId(data.getString("toUserId"));
                oi.setToUserNickname(data.getString("toUserNickname"));
                oi.setToUserTel(data.getString("toUserTel"));
                oi.setToUserGender(data.getString("toUserGender"));
                oi.setToUserToScore(data.getString("toUserToScore"));

                Log.d("CustomerActivity",data.getString("orderId"));
                Log.d("CustomerActivity",data.getString("toUserNickname"));
                Log.d("CustomerActivity",data.getString("toUserTel"));

                //进入下单人下单后的界面，传入订单对象
                Intent intent = new Intent(getActivity(), MainActivity.class);
                //todo
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderInfoObject", oi);
                intent.putExtras(bundle);
                intent.putExtra("userId", userId);
                intent.putExtra("token", token);
                startActivity(intent);
            }
            else {//打印出服务器返回的错误
                Toast.makeText(getActivity(), jb.getString("message"),Toast.LENGTH_SHORT).show();
                Log.d("CustomerActivity",jb.getString("message"));
            }


        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    //实时监听下单人当前位置函数
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


            //Log.d("CustomerActivity",Double.toString(location.getLatitude()));
            //Log.d("CustomerActivity",Double.toString(location.getLongitude()));

            //设置地图中心点
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);


            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
