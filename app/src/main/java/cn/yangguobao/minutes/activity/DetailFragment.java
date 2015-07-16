package cn.yangguobao.minutes.activity;

/**
 * Created by GuobaoYang on 15/7/14.
 */
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import cn.yangguobao.minutes.R;



public class DetailFragment extends Fragment {

    private Order orderDetail;

    public DetailFragment() {
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
        orderDetail = (Order) getArguments().getSerializable("order");
        RatingBar ratingBar = (RatingBar) getView().findViewById(R.id.orderDetailRatingBar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#f39c12"), PorterDuff.Mode.SRC_ATOP);
        ratingBar.setRating(orderDetail.getRatting());
        TextView nickname = (TextView) getView().findViewById(R.id.orderDetailUserNickName);
        nickname.setText(orderDetail.getNickname());
        TextView originPlace = (TextView) getView().findViewById(R.id.orderDetailOriginValue);
        originPlace.setText(orderDetail.getOriginPlace());

        TextView tel = (TextView) getView().findViewById(R.id.orderDetailTelValue);
        tel.setText(orderDetail.getTel());
        TextView destinationPlace = (TextView) getView().findViewById(R.id.orderDetailDestinationValue);
        destinationPlace.setText(orderDetail.getDestinationPlace());
        TextView createAt = (TextView) getView().findViewById(R.id.orderDetailStartTimeValue);
        createAt.setText(orderDetail.getCreateAt());
        TextView takeTime = (TextView) getView().findViewById(R.id.orderDetailTakeTimeValue);
        takeTime.setText(orderDetail.getTakeTime()+"分");
        TextView distance = (TextView) getView().findViewById(R.id.orderDetailDistanceValue);
        distance.setText(orderDetail.getDistance()+"米");

        TextView price = (TextView) getView().findViewById(R.id.orderDetailCostValue);
        price.setText(orderDetail.getPrice()+"元");

        TextView status = (TextView) getView().findViewById(R.id.orderDetailStatusValue);
        status.setText(orderDetail.getStatus());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;

    }

}

