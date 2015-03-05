package com.protect_tw;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Alarm_message extends Activity implements LocationListener{
	SharedPreferences settings;
	String UserID_string = "";
	int UserID = 0;
	Button protect_sence1,
	       protect_sence2,
	       protect_sence3,
	       protect_sence4,
	       protect_sence5,
	       real_sence1,
	       real_sence2,
	       real_sence3,
	       real_sence4,
	       real_sence5,
	       back_button,
	       other_sence;	
	EditText edit_other_sence;
	private LocationManager locationManager;
	double lat, 
	       lon;
	Protect_TW_utils util = new Protect_TW_utils(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_message);
		
        // fix can't send request to server, so I add this.		
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        }  
		
        // init location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
											   0,   // 3 sec
											   0, 
											   this);
        // get UserID
		settings = getSharedPreferences("UserInfo", 0);
		UserID_string = settings.getString("UserID", "");
		UserID_string = UserID_string.replace("\"", "");
		UserID = Integer.parseInt(UserID_string);
		
		// Init UI
		protect_sence1 = (Button) findViewById(R.id.protect_sence1);
		protect_sence2 = (Button) findViewById(R.id.protect_sence2);
		protect_sence3 = (Button) findViewById(R.id.protect_sence3);
		protect_sence4 = (Button) findViewById(R.id.protect_sence4);
		protect_sence5 = (Button) findViewById(R.id.protect_sence5);
		real_sence1 = (Button) findViewById(R.id.real_sence1);
		real_sence2 = (Button) findViewById(R.id.real_sence2);
		real_sence3 = (Button) findViewById(R.id.real_sence3);
		real_sence4 = (Button) findViewById(R.id.real_sence4);
		real_sence5 = (Button) findViewById(R.id.real_sence5);
		other_sence = (Button) findViewById(R.id.other_sence);
		back_button = (Button) findViewById(R.id.back_button);
		edit_other_sence = (EditText) findViewById(R.id.edit_other_sence);
		
		protect_sence1.setOnClickListener(send);
		protect_sence2.setOnClickListener(send);
		protect_sence3.setOnClickListener(send);
		protect_sence4.setOnClickListener(send);
		protect_sence5.setOnClickListener(send);
		real_sence1.setOnClickListener(send);
		real_sence2.setOnClickListener(send);
		real_sence3.setOnClickListener(send);
		real_sence4.setOnClickListener(send);
		real_sence5.setOnClickListener(send);
		back_button.setOnClickListener(send);
		other_sence.setOnClickListener(send);	
	}
	
	private Button.OnClickListener send = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.protect_sence1:
					send_alarm_message("我這邊淹水了！", "1");
					break;
				case R.id.protect_sence2:
					send_alarm_message("我遇到喪屍了，快來救我！", "2");
					break;
				case R.id.protect_sence3:
					send_alarm_message("火山爆發了阿~！", "3");
					break;
				case R.id.protect_sence4:
					send_alarm_message("我遇見變態了，救命啊！", "4");
					break;
				case R.id.protect_sence5:
					send_alarm_message("海嘯侵襲，求救！", "5");
					break;
				case R.id.real_sence1:
					send_alarm_message("我這邊遇到車禍了！", "6");
					break;
				case R.id.real_sence2:
					send_alarm_message("發生爆炸了，快來救我！", "7");
					break;
				case R.id.real_sence3:
					send_alarm_message("火警發生，請快派人來！", "8");
					break;
				case R.id.real_sence4:
					send_alarm_message("有人員落海，請快派人來救！", "9");
					break;
				case R.id.real_sence5:
					send_alarm_message("我身體不適，請幫我叫救護車！", "10");
					break;
				case R.id.other_sence:
					send_alarm_message(edit_other_sence.getText().toString(), "11");
					break;		
				default:
					break;	
			}
			back_page();
		}				
	};
	
	private void back_page(){
		Intent i = new Intent();
        i.setClass(Alarm_message.this, Main_Activity.class);
        startActivity(i);
	}
	
	private void send_alarm_message(String message, String type){
    	String HttpString = util.Connect_URL + "add_alarm_info/" + UserID + "/" + lat + "/" + lon + "/" + message + "/" + type + "/" + UUID.randomUUID();
		String response = util.http_json_Result(HttpString, Alarm_message.this, 1);		
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if(location != null){
			if(		lat != 0 
				&& 	lon != 0){
				lat = location.getLatitude();
				lon = location.getLongitude();
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {

	}
}
