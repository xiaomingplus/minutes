package cn.yangguobao.minutes.activity;
/**
 * Created by GuobaoYang on 15/7/11.
 */
public class Common {


    public static String subTitle (String str){

        if(str.length()>4){
            return str.substring(0,4);
        }else{
            return str;

        }

    }
}
