package cn.yangguobao.minutes.activity;

import java.io.Serializable;

/**
 * Created by GuobaoYang on 15/7/11.
 */
public class Order implements Serializable {
    private String title;
    private String description;
    private String nickname;
    private String status;
    private String tel;
    private float ratting;
    private String price;
    private String originPlace;
    private String destinationPlace;
    private String distance;
    private String createAt;
    private String takeTime;
    public Order(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Order(String title, String description, String nickname,String tel,double ratting,String price,String originPlace,String destinationPlace,String distance,String createAt,String takeTime,String status) {
        this.title = title;
        this.description = description;
        this.nickname = nickname;
        this.tel=tel;
        this.ratting = (float)ratting ;
        this.price = price;
        this.originPlace = originPlace;
        this.destinationPlace = destinationPlace;
        this.distance = distance;
        this.createAt = createAt;
        this.takeTime = takeTime;
        this.status = status;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }

    public String getNickname(){
        return nickname;
    }
    public String getTel(){
        return tel;
    }

    public Float getRatting(){
        return ratting;
    }
    public String getPrice(){
        return price;
    }
    public String getOriginPlace(){
        return originPlace;
    }
    public String getDestinationPlace(){
        return destinationPlace;
    }

    public String getDistance(){
        return distance;
    }

    public String getCreateAt(){
        return createAt;
    }

    public String getTakeTime(){
        return takeTime;
    }
    public  String getStatus(){
        return status;
    }



}
