package com.protect_tw;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class Main_Activity extends FragmentActivity implements LocationListener{
	private TabHost mTabHost;
    private TabManager mTabManager;   
    int tab_page = 0;
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private list_Thread list_Thread;
	private Handler list_Handler = new Handler();
	Protect_TW_utils util = new Protect_TW_utils(); 
	String UserID = "";
	double lat, 
    	   lon;
	private LocationManager locationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        settings = getSharedPreferences("UserInfo", 0);
        UserID = settings.getString("UserID", "");
        if(UserID.length() == 0){
			Intent i = new Intent();
	        i.setClass(this, Login_page.class);
	        startActivity(i);
        }else{
            // init location
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
            									   0,   // 3 sec
            									   0, 
            									   this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
    											   0,   // 3 sec
    											   0, 
    											   this);
			mTabHost = (TabHost)findViewById(android.R.id.tabhost);
	        mTabHost.setup();
	        
	        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);            
	        
	        mTabManager.addTab(
	                mTabHost.newTabSpec("group_tab").setIndicator("群組", this.getResources().getDrawable(R.drawable.group_click_tab)),
	                Group_tab.class, null);
	        mTabManager.addTab(
	            mTabHost.newTabSpec("infomation_tab").setIndicator("資訊", this.getResources().getDrawable(R.drawable.infomation_click_tab)),
	            Infomation_tab.class, null);
	        mTabManager.addTab(
	            mTabHost.newTabSpec("message_tab").setIndicator("訊息", this.getResources().getDrawable(R.drawable.message_click_tab)),
	            Message_tab.class, null);
	        mTabManager.addTab(
	                mTabHost.newTabSpec("setting_tab").setIndicator("設定", this.getResources().getDrawable(R.drawable.setting_tabhost_button_action)),
	                Setting_tab.class, null);
	        mTabHost.setCurrentTab(tab_page);//設定一開始就跳到第一個分頁    
	        DisplayMetrics dm = new DisplayMetrics();   
	        getWindowManager().getDefaultDisplay().getMetrics(dm); //先取得螢幕解析度  
	        int screenWidth = dm.widthPixels;   //取得螢幕的寬           
	           
	        TabWidget tabWidget = mTabHost.getTabWidget();   //取得tab的物件
	        int count = tabWidget.getChildCount();   //取得tab的分頁有幾個
	        tabWidget.setStripEnabled(false);
	        for (int i = 0; i < count; i++) {   
	            tabWidget.getChildTabViewAt(i).setMinimumWidth((screenWidth)/4);//設定每一個分頁最小的寬度           
	            tabWidget.getChildTabViewAt(i).setBackgroundResource(R.drawable.button_tab_backgroud);
	            TextView tv = (TextView) tabWidget.getChildAt(i).findViewById(android.R.id.title);
	            tv.setTextColor(R.drawable.tab_click_text);
	        }
	    }
    }
	
	public class list_Thread extends Thread{
    	public void run(){
    		new control_view_change().execute();
    		list_Handler.postDelayed(list_Thread, 20000);    		
    	}
	}
	
	private class control_view_change extends AsyncTask<Object, Void, String>{
		@Override
		protected String doInBackground(Object... params) {
			if(		lat != 0
				&&	lon != 0){
				String HttpString = "";
				HttpString = util.Connect_URL + "User_set_location/" + UserID + "/" + lat + "/" + lon + "/" + UUID.randomUUID();
				if(null != util.get_network_info(Main_Activity.this)){
					Log.e("HttpString", "HttpString:" + HttpString);
					util.http_json_Result(HttpString, Main_Activity.this, 0);
				}					
			}
			return null;
		}
		
		protected void onPostExecute(String resoult){
			
		}
	}
	
	public void clk_start(){
		Log.i("clk_Thread", "clk_start");
		clk_stop();
		list_Thread = new list_Thread();
    	list_Handler.post(list_Thread);
	}
	
	public void clk_stop(){
		Log.i("clk_Thread", "clk_stop");
		if(list_Handler != null){
			list_Handler.removeCallbacks(list_Thread);
		}
	}	
	
	public void onResume() {
		super.onResume();
		Log.e("onRestart", "restart");
		clk_start();
	}

	@Override
	public void onLocationChanged(Location location) {
		if(		location != null
			&& 	location.getLatitude() != 0
			&& 	location.getLongitude() != 0){
			lat = location.getLatitude();
			lon = location.getLongitude();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}
}
