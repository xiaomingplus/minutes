package cn.yangguobao.minutes.activity;

import android.util.Log;

import java.text.DecimalFormat;

public class MinutesUtil {
    
	public static  String CXS;
	public static String CYS;
	
	public static double CbdX;
	public static double CbdY;
	
	public static void changToMercator(double startX,double startY)
	{
		
		double changedX;
		double changedY;
		
		changedX=startY *20037508.34 / 180;
		changedY=Math.log(Math.tan((90 + startX) * Math.PI / 360)) / (Math.PI / 180);
		changedY=changedY*20037508.34 / 180;
		
	      DecimalFormat  df  = new DecimalFormat("#######.000000000");
	      CXS=df.format(changedX);
	      CYS=Double.toString(changedY);
		Log.d("CustomerActivity","x"+Double.toString(startX));
		Log.d("CustomerActivity","y"+Double.toString(startY));
		
		Log.d("CustomerActivity","x"+CXS);
		Log.d("CustomerActivity","y"+CYS);
		/*
		double lat=Double.valueOf(startX);//γ��
		double lon=Double.valueOf(startY);
		double CentralMeridian=0;
		double RefLat = 0;
		double N0 = 6378137.0 / Math.sqrt( 1-Math.pow(0.081819190843,2)*Math.pow(Math.sin(RefLat*Math.PI/180),2) ); 
        double q1 = Math.log( Math.tan( (180.0/4.0+lat/2.0)*Math.PI/180.0 ) ); 
        double q2 = 0.081819190843/2 * Math.log( (1+0.081819190843*Math.sin(lat*Math.PI/180.0) ) / (1-0.081819190843*Math.sin(lat*Math.PI/180.0) ) ); 
        double q = q1 - q2 ; 
        double x = N0 * Math.cos(RefLat*Math.PI/180.0) * ((lon-CentralMeridian)/57.29577951) ; 
        //double x=N0 *lon/57.29577951; 
        double y = N0 * Math.cos(RefLat*Math.PI/180.0) * q ;
        DecimalFormat  df  = new DecimalFormat("######0.000000000"); 
        Log.d("CustomerActivity","x��"+df.format(x));
        Log.d("CustomerActivity","y��"+df.format(y));
        CXS=df.format(x);
        CYS=df.format(y);
        */
        
        
      
	}
	
	public static void MercatorToBD(double mercatorX,double mercatorY)
	{
		CbdX=mercatorY/ 20037508.34 * 180;
		CbdX=180 / Math.PI * (2 * Math.atan(Math.exp(CbdX * Math.PI / 180)) - Math.PI / 2);
		CbdY= mercatorX/ 20037508.34 * 180;
		
		Log.d("CustomerActivity","x"+Double.toString(CbdX));
		Log.d("CustomerActivity","y"+Double.toString(CbdY));
	}
	public static String getCXS(){
		return CXS;
	}
	public static String getCYS(){
		return CYS;
	}
}
