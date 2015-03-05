package com.protect_tw;

import static com.protect_tw.Notification_Utilities.SENDER_ID;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login_page extends Activity {
	EditText UserName,
			 Password;
	Button Login,
		   Back_button;
	Protect_TW_utils util = new Protect_TW_utils(); 
	private String username_string = "",
			       password_string = "";
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	String regID = "",
		   TAG = "** pushAndroidActivity **",
		   reg_id = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_page);
		
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
        
		// Init notification
		checkNotNull(SENDER_ID, "SENDER_ID");
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		regID = GCMRegistrar.getRegistrationId(this);
		Log.i(TAG, "registration id =====  "+regID);
		if (regID.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
			regID = GCMRegistrar.getRegistrationId(this);
		} else {
			Log.i(TAG, "Already registered");
		}		
		
		// get UI item
        UserName = (EditText)findViewById(R.id.username);
        Password = (EditText)findViewById(R.id.passwd);
        Login = (Button)findViewById(R.id.login_button);
        Back_button = (Button)findViewById(R.id.back_button_backup);
        
        Login.setOnClickListener(login_function);
        Back_button.setOnClickListener(back);
	}
	
	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(
				getString(R.string.error_config, name));
		}		
	} 
	
	// detect back key event
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
        	back_page();
        }        
        return super.onKeyDown(keyCode, event);
    }   
    
    public void back_page(){
		Intent i = new Intent();
        i.setClass(this, Main_Activity.class);
        startActivity(i);	
    }
    
    private Button.OnClickListener back = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			back_page();
		}    	
    };
    
    private Button.OnClickListener login_function = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			// prepare http string
			String HttpString = util.Connect_URL + "Login/";
			
			// get username and passwd
			username_string = UserName.getText().toString();
			password_string = Password.getText().toString();
			
			if( password_string.length() == 0){
				Password.setError(Html.fromHtml("<font color='red'>密碼不可為空白</font>"));
				return;
			}else if(username_string.length() == 0){
				UserName.setError(Html.fromHtml("<font color='red'>帳號不可為空白</font>"));
				return;
			}else{
				settings = getSharedPreferences("UserInfo", 0);
				editor = settings.edit();
				HttpString += password_string + "/";
				HttpString += username_string + "/";				
				HttpString += UUID.randomUUID();
			}
			
			String response = util.http_json_Result(HttpString, Login_page.this, 1);
			if(		response == null
				||	response.equals("null")){
				Login.setClickable(true);
			}else{
				if(		response.contains("查無此帳號")
					||	response.contains("密碼錯誤")){						
					Toast.makeText(Login_page.this, response, Toast.LENGTH_SHORT).show(); 					
				}else{				
					response = response.replace("\"", "");
					// save user information 
					editor.putString("Username", username_string);
					editor.putString("Password", password_string);
					editor.putString("UserID", response);
					editor.commit();
					// SendDemoNotification
					HttpString = util.Connect_URL + "NotificationRegister/" + response + "/" + "android/" + regID;
					if(null != util.get_network_info(Login_page.this)){
						util.http_json_Result(HttpString, Login_page.this, 1);
					}
					
					// Back
					Intent i = new Intent();
			        i.setClass(Login_page.this, Main_Activity.class);
			        startActivity(i);	
				}
				Login.setClickable(true);
			}
		}    
    };
}
