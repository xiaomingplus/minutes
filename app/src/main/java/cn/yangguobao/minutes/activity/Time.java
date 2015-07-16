

package cn.yangguobao.minutes.activity;

import java.text.SimpleDateFormat;

/**
 * Created by GuobaoYang on 15/7/11.
 */
public class Time {


    public static String date(Long timestamp){
        Long time= new Long(timestamp*1000);
       SimpleDateFormat date =  new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return date.format(time);
    }

}
