package crc.lysis.Pedometer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;









import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import static android.hardware.SensorManager.SENSOR_ACCELEROMETER;
import static android.hardware.SensorManager.SENSOR_ORIENTATION;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static android.hardware.SensorManager.SENSOR_DELAY_UI;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PedometersActivity extends Activity implements SensorListener{
	
	private SensorManager sensormgr,orsensormgr;
	private Button StartBT,AutoDetect,leave,exercise,upload,menu,back;
	private Button save;
	private TextView Text1,Text3;
	private boolean StartFlag=false;
	final List<Float> x_list = new ArrayList<Float>();
	final List<Float> y_list = new ArrayList<Float>();
	final List<Float> z_list = new ArrayList<Float>();
	final List<Float> temp_x_list = new ArrayList<Float>();
	final List<Float> temp_y_list = new ArrayList<Float>();
	final List<Float> temp_z_list = new ArrayList<Float>();
	final List<Float> o1_list = new ArrayList<Float>();
	final List<Float> o2_list = new ArrayList<Float>();
	final List<Float> o3_list = new ArrayList<Float>();
	private float o1_temp = 0;
	private float o2_temp = 0;
	private float o3_temp = 0;
	final List<Double> t1_list = new ArrayList<Double>();
	final List<Double> t2_list = new ArrayList<Double>();
	final List<Double> t3_list = new ArrayList<Double>();
	final List<Double> w1_list = new ArrayList<Double>();
	final List<Double> w2_list = new ArrayList<Double>();
	final List<Double> w3_list = new ArrayList<Double>();
	final List<Double> tempcount_list = new ArrayList<Double>();
	final List<Double> tempcount_list_plus = new ArrayList<Double>();
	
	double temp_count_a=0.0;                              //計算力量扣掉重力後之加總
	
	int ClickStartBT_time=0;
	double[] list_varience = new double[4];
	
	int Stepcount=0;
	int Thre_step=0;
	int Count_return=0;
	boolean flag_step=true;
	
	String Stepcount_string ="a";
	
	private Message m3;
	
	private MyHandler1 handler1;
	
	
	boolean detect_on=false;
	final List<Double> detect_list = new ArrayList<Double>();
	final List<Integer> detect_list_step = new ArrayList<Integer>();
	int[] detect_level = new int[20];
	int step_freq_detect = 0;
	
	SharedPreferences SPreferences;
	
	private PowerManager.WakeLock wakeLock;
	private boolean ifLocked = false;
	float screen_now_Bright=0;
	
	int count_peak = 0;
	double Front_peak = 9.8 ;
	boolean plus_minus = true;
	double now_power = 0.0;
	int step_freq = 0;
	int new_threshod = 20;
	double law_new_threshold = 12;
	double front_step_peak = 0;
	
	Date front_date;
	Date now_date;
	float calories;
	Date exercise_bigin;
	Date exercise_stop;
	
	private int check=1,count=0;
	private String newName="";
	private EditText account_e,ed_text1,ed_text3,ed_text4,ed_text5,ed_text6;
	
	int temp_pre2, count_step2;
	float temp_cal;
	
    int weight = 68;
    int hight_cm = 170;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
        StartBT = (Button) findViewById(R.id.button1);
        Text1 = (TextView)findViewById(R.id.text1);
        Text3 = (TextView)findViewById(R.id.text3);
  //    account_e = (EditText)findViewById(R.id.account_r);
        menu = (Button) findViewById(R.id.button6);
  //     menu.setOnClickListener(click);
        
        front_date = new Date();
        
        SPreferences =getSharedPreferences("MyCustomSharedPreferences", 0);
        
        int temp_pre = SPreferences.getInt("thres_value", 18);
        int count_step = SPreferences.getInt("Step_count", 0);
        float calories_temp = SPreferences.getFloat("calories", 0);
        long frontdate_temp = SPreferences.getLong("front_Date", front_date.getTime());
        weight = SPreferences.getInt("weight", 60);
        hight_cm = SPreferences.getInt("hight_cm", 170);
        
//        String Account_name = SPreferences.getString("account", "");
        
 //       account_e.setText(Account_name);
        
        front_date = new Date(frontdate_temp);
        calories = calories_temp;
        new_threshod = temp_pre;
        law_new_threshold = ((new_threshod-10) * 2 / 5) + 10 ;
        Stepcount        = count_step;
        
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "BackLight");
        
		
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		screen_now_Bright = lp.screenBrightness;
		
        setT();
        
    	StartBT.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {
    			StartFlag = true;
    			StartBT.setEnabled(false);
    			AutoDetect.setEnabled(false);
    			leave.setEnabled(false);
    			exercise.setEnabled(false);
    //			save.setEnabled(false);
    			menu.setEnabled(false);
    //			account_e.setEnabled(false);
    //			account_e.setInputType(InputType.TYPE_NULL);
    			
    			WindowManager.LayoutParams lp2 = getWindow().getAttributes();
    			lp2.screenBrightness = 0.05f;
    			getWindow().setAttributes(lp2);
    		}
        });
    	
    	
    	handler1 = new MyHandler1();
    	
    	AutoDetect = (Button) findViewById(R.id.button9);
    	
    	AutoDetect.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {
    			
    			
    			new AlertDialog.Builder(PedometersActivity.this)
    			.setTitle("通知")
    			.setMessage("自動偵測?")
    			.setPositiveButton
    			(
    			  "確定",
    			  new DialogInterface.OnClickListener() 
    			  {		
    				@Override
    				public void onClick(DialogInterface dialog, int which) 
    				{
    	    			int temp_best_thres = 6;
    	    			double temp_best_count = 100;
    	    			if(detect_on){
    	    				
    	    				StartFlag = false;
    	    				
    	    				for(int i=0;i<20;i++){
    	    					int temp_level = i+12;
    	    					double temp_law_level = ((temp_level-10) * 2 / 5) + 10 ;
    	    					int temp_count = 0;
    	    					int temp_step_freq=0;
    	    					double temp_fronf_step_peak=0;
    	    					for(int j=0;j<detect_list.size();j++){
    	    						
    	    						////////////////
    	    						if(detect_list_step.get(j) > temp_step_freq+3){    	    							
    	    							if(detect_list.get(j) > temp_level){    	    								
    	    								temp_step_freq = detect_list_step.get(j);
    	    								temp_fronf_step_peak = detect_list.get(j);
    	    								temp_count++;
    	    							}
    	    							else if(detect_list_step.get(j) < temp_step_freq + 15){    	    								
    	    								if(detect_list.get(j) > temp_law_level){   	    									
    	    									if(temp_fronf_step_peak>temp_level ){									
    	    										temp_step_freq = detect_list_step.get(j);
    	    										temp_fronf_step_peak = detect_list.get(j);
    	    										temp_count++;							
    	    									}
    	    								}
    	    							}
    	    						
    	    						}
    	    						////////////////
    	    					}
    	    					
    	    					detect_level[i] = temp_count;
    	    					Log.d("DebugCCC", "detect_level[i]" +":::" + i + ":::::"+detect_level[i]);
    	    					
    	    				}
    	    				
    	    				for(int i=0;i<20;i++){
    	    					double temp_minus=0;
    	    					if(detect_level[i]>100){
    	    						temp_minus = detect_level[i] - 100;
    	    					}
    	    					else{
    	    						temp_minus = 100 - detect_level[i] ;
    	    					}
    	    					if(temp_minus<temp_best_count){
    	    						temp_best_thres = i;
    	    						temp_best_count = temp_minus;
    	    					}
    	    				}
    	    						
    	    				new_threshod = temp_best_thres+12;
    	    				String s_t_l = String.valueOf(new_threshod);
    	    				Text3.setText(s_t_l);
    	    				detect_on = false;
    	        			StartBT.setEnabled(true);
    	        			leave.setEnabled(true);
    	        			exercise.setEnabled(true);
    	        			menu.setEnabled(true);
   // 	        			account_e.setEnabled(true);
   // 	        			account_e.setInputType(InputType.TYPE_CLASS_TEXT);
    	    				//Stepcount = 0;
    	    				detect_list.clear();
    	    				step_freq_detect =0;
    	    			}
    	    			else{
    	    				detect_on = true;
    	    				StartFlag = true;
    	        			StartBT.setEnabled(false);
    	        			leave.setEnabled(false);
    	        			exercise.setEnabled(false);  
    	        			menu.setEnabled(false);
  //  	        			account_e.setEnabled(false);
  //  	        			account_e.setInputType(InputType.TYPE_NULL);
    	    			}                                                        
    	    			
    	    			 SharedPreferences.Editor editor = SPreferences.edit();
    	    				
    	    			 editor.putInt("thres_value",new_threshod );
    	    			 editor.putInt("Step_count", Stepcount );
    	    			 editor.commit();
    					// TODO Auto-generated method stub	
    				}                                                                 
    			  })
    			  .setNegativeButton("取消",
    			  new DialogInterface.OnClickListener() 
    			  {		
    				@Override
    				public void onClick(DialogInterface dialog, int which) 
    				{
    					// TODO Auto-generated method stub	
    				}
    			  }).show();
    			
    			
    		} 
        });
    	
    	
    	exercise= (Button) findViewById(R.id.button4);
       	exercise.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {
    		
    			exercise_bigin = new Date();
    			exercise_log("Start_activity");
    			
    			new AlertDialog.Builder(PedometersActivity.this)
    			.setTitle("通知")
    			.setMessage("運動中，停留在此畫面")
    			.setPositiveButton
    			(
    			  "結束運動",
    			  new DialogInterface.OnClickListener() 
    			  {		
    				@Override
    				public void onClick(DialogInterface dialog, int which) 
    				{
    					exercise_stop = new Date();
    					double time_d=(exercise_stop.getTime()-exercise_bigin.getTime())/(1000);
    					double temp_cal =0;
    					temp_cal =   (0.08*time_d*weight/60);
    					calories = calories +(float)temp_cal;
    			    	SPreferences =getSharedPreferences("MyCustomSharedPreferences", 0);
    					SharedPreferences.Editor editor = SPreferences.edit();
    					editor.putFloat("calories",calories );
    					editor.commit();
    					exercise_log("Finish_activity");
    				}
    			  }).show();	
    		}
        });    
    	
       	leave  = (Button) findViewById(R.id.button3);
       	leave.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {

    			finish();
    			
    		}
        }); 
       	
       	menu.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {

    			menu();
    			
    		}
        });
       	
  /*     	save = (Button) findViewById(R.id.button5);
       	save.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {

    			save_data();
    			
    		}
        });                    */
       	
    }
    
    public void menu(){
    	setContentView(R.layout.main2);
    	account_e = (EditText)findViewById(R.id.account_r);
        String Account_name = SPreferences.getString("account", "");     
        account_e.setText(Account_name);
        upload = (Button) findViewById(R.id.button16);
        upload.setOnClickListener(click);
        back = (Button) findViewById(R.id.button13);
        ed_text1 = (EditText)findViewById(R.id.ed_text1);
        ed_text3 = (EditText)findViewById(R.id.ed_text3);
        ed_text4 = (EditText)findViewById(R.id.ed_text4);
        ed_text5 = (EditText)findViewById(R.id.ed_text5);
        ed_text6 = (EditText)findViewById(R.id.ed_text6);
        ed_text1.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed_text3.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed_text4.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed_text5.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed_text6.setInputType(InputType.TYPE_CLASS_NUMBER);
        
        
        SPreferences =getSharedPreferences("MyCustomSharedPreferences", 0);
        String Account_name2 = SPreferences.getString("account", "");
        weight = SPreferences.getInt("weight", 60);
        hight_cm = SPreferences.getInt("hight_cm", 170);
        
        account_e.setText(Account_name2);
        
   	    Stepcount_string = Integer.toString(Stepcount);
     	ed_text1.setText(Stepcount_string);
		String s_t_l = String.valueOf(new_threshod);
		ed_text3.setText(s_t_l);
		String s_t_l2 = String.valueOf(calories);
		ed_text4.setText(s_t_l2);
		s_t_l2 = String.valueOf(weight);
		ed_text6.setText(s_t_l2);
		s_t_l2 = String.valueOf(hight_cm);
		ed_text5.setText(s_t_l2);
		
       	back.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View arg0) {
	
    	        temp_pre2 = Integer.valueOf(ed_text3.getText().toString());
    	        count_step2 =Integer.valueOf(ed_text1.getText().toString());
    	        temp_cal = Float.valueOf(ed_text4.getText().toString());
    	        weight = Integer.valueOf(ed_text6.getText().toString());
    	        hight_cm = Integer.valueOf(ed_text5.getText().toString());
    	        
   			    SharedPreferences.Editor editor = SPreferences.edit();
				
   			    editor.putInt("thres_value",temp_pre2 );
   			    editor.putInt("weight",weight );
   			    editor.putInt("hight_cm",hight_cm );
   			    editor.commit();
    	        
    	        setContentView(R.layout.main);
    	        
    	        StartBT = (Button) findViewById(R.id.button1);
    	        Text1 = (TextView)findViewById(R.id.text1);
    	        Text3 = (TextView)findViewById(R.id.text3);
    	        menu = (Button) findViewById(R.id.button6);

    	        calories = temp_cal;
    	        new_threshod = temp_pre2;
    	        law_new_threshold = ((new_threshod-10) * 2 / 5) + 10 ;
    	        Stepcount        = count_step2;
    	        
   // 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
   // 			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "BackLight");
    	        
    			
    			WindowManager.LayoutParams lp = getWindow().getAttributes();
    			screen_now_Bright = lp.screenBrightness;
    			
    	        setT();
    	        
    	    	StartBT.setOnClickListener(new Button.OnClickListener() {
    	    		public void onClick(View arg0) {
    	    			StartFlag = true;
    	    			StartBT.setEnabled(false);
    	    			AutoDetect.setEnabled(false);
    	    			leave.setEnabled(false);
    	    			exercise.setEnabled(false);
    	    			menu.setEnabled(false);
    	    			
    	    			WindowManager.LayoutParams lp2 = getWindow().getAttributes();
    	    			lp2.screenBrightness = 0.05f;
    	    			getWindow().setAttributes(lp2);
    	    		}
    	        });
    	    	
    	    	
    	    	handler1 = new MyHandler1();
    	    	
    	    	AutoDetect = (Button) findViewById(R.id.button9);
    	    	
    	    	AutoDetect.setOnClickListener(new Button.OnClickListener() {
    	    		public void onClick(View arg0) {
    	    			
    	    			
    	    			new AlertDialog.Builder(PedometersActivity.this)
    	    			.setTitle("通知")
    	    			.setMessage("自動偵測?")
    	    			.setPositiveButton
    	    			(
    	    			  "確定",
    	    			  new DialogInterface.OnClickListener() 
    	    			  {		
    	    				@Override
    	    				public void onClick(DialogInterface dialog, int which) 
    	    				{
    	    	    			int temp_best_thres = 6;
    	    	    			double temp_best_count = 100;
    	    	    			if(detect_on){
    	    	    				
    	    	    				StartFlag = false;
    	    	    				
    	    	    				for(int i=0;i<20;i++){
    	    	    					int temp_level = i+12;
    	    	    					double temp_law_level = ((temp_level-10) * 2 / 5) + 10 ;
    	    	    					int temp_count = 0;
    	    	    					int temp_step_freq=0;
    	    	    					double temp_fronf_step_peak=0;
    	    	    					for(int j=0;j<detect_list.size();j++){
    	    	    						
    	    	    						////////////////
    	    	    						if(detect_list_step.get(j) > temp_step_freq+3){    	    							
    	    	    							if(detect_list.get(j) > temp_level){    	    								
    	    	    								temp_step_freq = detect_list_step.get(j);
    	    	    								temp_fronf_step_peak = detect_list.get(j);
    	    	    								temp_count++;
    	    	    							}
    	    	    							else if(detect_list_step.get(j) < temp_step_freq + 15){    	    								
    	    	    								if(detect_list.get(j) > temp_law_level){   	    									
    	    	    									if(temp_fronf_step_peak>temp_level ){									
    	    	    										temp_step_freq = detect_list_step.get(j);
    	    	    										temp_fronf_step_peak = detect_list.get(j);
    	    	    										temp_count++;							
    	    	    									}
    	    	    								}
    	    	    							}
    	    	    						
    	    	    						}
    	    	    						////////////////
    	    	    					}
    	    	    					
    	    	    					detect_level[i] = temp_count;
    	    	    					Log.d("DebugCCC", "detect_level[i]" +":::" + i + ":::::"+detect_level[i]);
    	    	    					
    	    	    				}
    	    	    				
    	    	    				for(int i=0;i<20;i++){
    	    	    					double temp_minus=0;
    	    	    					if(detect_level[i]>100){
    	    	    						temp_minus = detect_level[i] - 100;
    	    	    					}
    	    	    					else{
    	    	    						temp_minus = 100 - detect_level[i] ;
    	    	    					}
    	    	    					if(temp_minus<temp_best_count){
    	    	    						temp_best_thres = i;
    	    	    						temp_best_count = temp_minus;
    	    	    					}
    	    	    				}
    	    	    						
    	    	    				new_threshod = temp_best_thres+12;
    	    	    				String s_t_l = String.valueOf(new_threshod);
    	    	    				Text3.setText(s_t_l);
    	    	    				detect_on = false;
    	    	        			StartBT.setEnabled(true);
    	    	        			leave.setEnabled(true);
    	    	        			exercise.setEnabled(true);
    	    	        			menu.setEnabled(true);
    	    	    				//Stepcount = 0;
    	    	    				detect_list.clear();
    	    	    				step_freq_detect =0;
    	    	    			}
    	    	    			else{
    	    	    				detect_on = true;
    	    	    				StartFlag = true;
    	    	        			StartBT.setEnabled(false);
    	    	        			leave.setEnabled(false);
    	    	        			exercise.setEnabled(false);  
    	    	        			menu.setEnabled(false);
    	    	    			}                                                        
    	    	    			
    	    	    			 SharedPreferences.Editor editor = SPreferences.edit();
    	    	    				
    	    	    			 editor.putInt("thres_value",new_threshod );
    	    	    			 editor.putInt("Step_count", Stepcount );
    	    	    			 editor.commit();
    	    					// TODO Auto-generated method stub	
    	    				}                                                                 
    	    			  })
    	    			  .setNegativeButton("取消",
    	    			  new DialogInterface.OnClickListener() 
    	    			  {		
    	    				@Override
    	    				public void onClick(DialogInterface dialog, int which) 
    	    				{
    	    					// TODO Auto-generated method stub	
    	    				}
    	    			  }).show();
    	    			
    	    			
    	    		} 
    	        });
    	    	
    	    	
    	    	exercise= (Button) findViewById(R.id.button4);
    	       	exercise.setOnClickListener(new Button.OnClickListener() {
    	    		public void onClick(View arg0) {
    	    		
    	    			exercise_bigin = new Date();
    	    			exercise_log("Start_activity");
    	    			
    	    			new AlertDialog.Builder(PedometersActivity.this)
    	    			.setTitle("通知")
    	    			.setMessage("運動中，停留在此畫面")
    	    			.setPositiveButton
    	    			(
    	    			  "結束運動",
    	    			  new DialogInterface.OnClickListener() 
    	    			  {		
    	    				@Override
    	    				public void onClick(DialogInterface dialog, int which) 
    	    				{
    	    					exercise_stop = new Date();
    	    					double time_d=(exercise_stop.getTime()-exercise_bigin.getTime())/(1000);
    	    					double temp_cal =0;
    	    					temp_cal =   (0.08*time_d*weight/60);
    	    					calories = calories +(float)temp_cal;
    	    			    	SPreferences =getSharedPreferences("MyCustomSharedPreferences", 0);
    	    					SharedPreferences.Editor editor = SPreferences.edit();
    	    					editor.putFloat("calories",calories );
    	    					editor.commit();
    	    					exercise_log("Finish_activity");
    	    				}
    	    			  }).show();	
    	    		}
    	        });    
    	    	
    	       	leave  = (Button) findViewById(R.id.button3);
    	       	leave.setOnClickListener(new Button.OnClickListener() {
    	    		public void onClick(View arg0) {

    	    			finish();
    	    			
    	    		}
    	        });
    	       	menu.setOnClickListener(new Button.OnClickListener() {
    	    		public void onClick(View arg0) {

    	    			menu();
    	    			
    	    		}
    	        });
    		}
        });
    }
    
    public void back(){

    }
    
    
	public void onResume() {
		super.onResume();
		sensormgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensormgr.registerListener(this, SENSOR_ACCELEROMETER, SENSOR_DELAY_UI);
		orsensormgr= (SensorManager) getSystemService(SENSOR_SERVICE);
		orsensormgr.registerListener(this, SENSOR_ORIENTATION, SENSOR_DELAY_UI);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mBroadcastReceiver, filter);    //mBroadcastReceiver存取電池計量
		
		acquireWakeLock();
		
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);
		
		releaseWakeLock() ;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int level = 0;
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				level = intent.getIntExtra("level", 0);
			}
			if (level == 1) {
//			
				Toast.makeText(PedometersActivity.this, "Power shortage, it's going to leave this app...", Toast.LENGTH_LONG);
			}
		}
	};
	


	
	public void onAccuracyChanged(int sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
	public void onSensorChanged(final int sensor, final float[] values) {
		// TODO Auto-generated method stub

		if(StartFlag){
				
				if (sensor == SensorManager.SENSOR_ACCELEROMETER){
					
					if(x_list.size()<5){
					
					x_list.add(values[0]);
					y_list.add(values[1]);
					z_list.add(values[2]);
					
					o1_list.add(o1_temp);
					o2_list.add(o2_temp);
					o3_list.add(o3_temp);
					
					}
	//				ClickStartBT_time++;
					

					Log.d("DebugCCC", "Getdata");
					
					
					
					////////////////////////////////////
					if(detect_on){
						step_freq_detect++;			
					}		
					

					if((x_list.size()>=5)){
						
						for(int i=0;i<x_list.size();i++){
						
//						Log.e("DebugCCC", " Get Data:  " + x_list.get(i) +" , " + y_list.get(i) +" , " + z_list.get(i) +" , ");
						
						count_ya_s(i);
										
						step_freq++;
						
						if(plus_minus){
							
							if((now_power - Front_peak )>0){
								Front_peak = now_power;
							}
							else if((now_power - Front_peak )<0){
								plus_minus = false;
								
								Log.d("DebugCCC", " Get Data:  " +"                             :::::::::          " + now_power);
								/////////////////////////////////////////
								
								if(detect_on){
									detect_list.add( Front_peak );
									detect_list_step.add( step_freq_detect );
								}
								
								if(step_freq > 3){
									
									if(Front_peak > new_threshod){
										
										step_freq =0;
										front_step_peak = Front_peak;
										if(Thre_step>=3){
										Stepcount++;
								        m3 =handler1.obtainMessage(0);    //傳送1給 handler
								        handler1.sendMessage(m3);
											if(Stepcount % 10 ==0){
												m3 =handler1.obtainMessage(1);    //傳送1給 handler
												handler1.sendMessage(m3);
											}
										}
										else{
											Thre_step++;
										}

									}
									else if(step_freq < 15){
										if(Front_peak > law_new_threshold){
											if(front_step_peak>new_threshod){									
												step_freq =0;
												front_step_peak = Front_peak;
												if(Thre_step>=3){
												Stepcount++;							
										        m3 =handler1.obtainMessage(0);    //傳送1給 handler
										        handler1.sendMessage(m3);
													if(Stepcount % 10 ==0){
														m3 =handler1.obtainMessage(1);    //傳送1給 handler
														handler1.sendMessage(m3);
													}
												}
												else{
													Thre_step++;
												}
											}
										}
									}
								}
								
								Front_peak = now_power;
								//////////////////////////////////////////
							}
							
						}
						else{
							if((now_power - Front_peak )<0){
								Front_peak = now_power;
							}
							else if((now_power - Front_peak )>0){
								plus_minus = true;
								Front_peak = now_power;
							}
						}
						
						if(step_freq>250){
							Thre_step =0;
						}
						
						
						}
						
						x_list.clear();
						y_list.clear();
						z_list.clear();
						
						o1_list.clear();
						o2_list.clear();
						o3_list.clear();	
					}
					////////////////////////////////////
				}
				
				if(sensor == SensorManager.SENSOR_ORIENTATION){
					
					 o1_temp = values[0];
					 o2_temp = values[1];
					 o3_temp = values[2];
					
				}
		

		
		
//		if(step_freq>100000){
//			step_freq=0;
//		}
					
		
/*		if((x_list.size()>=5)&&(o1_list.size()>=5)){
			
			//計算變異數
			
			
//			list_varience[0] = varience(x_list);
//			list_varience[1] = varience(y_list);
//			list_varience[2] = varience(z_list);
			
			count_ya();
			
//			save_data();
			
//		    Log.d("DebugCCC", " Get Data:  " +list_varience[0]+"   "+list_varience[1]+"  "+list_varience[2] );
			
			
			
	//		for(int i=0;i<x_list.size();i++){
				
	//			temp_x_list.add(x_list.get(i));
	//			temp_y_list.add(y_list.get(i));
	//			temp_z_list.add(z_list.get(i));
				
	//		}
			
			x_list.clear();
			y_list.clear();
			z_list.clear();
			
			o1_list.clear();
			o2_list.clear();
			o3_list.clear();
			
			Log.d("DebugCCC", "clear" );
			
			if(detect_on){
				detect_list.add(temp_count_a);
			}
			
	//		if(list_varience[0] +list_varience[1] +list_varience[2] > 10 ){
			
	//		if(flag_step){
			if(temp_count_a > threshhold_level){
				if(Thre_step>=3){
				
				Stepcount++;
				if(Stepcount % 10 ==0){
			        m3 =handler1.obtainMessage(1);    //傳送1給 handler
			        handler1.sendMessage(m3);
				}
				flag_step=false;
				}
				else{
				Thre_step++;
				}
			}
			else{
				Count_return++;
				if(Count_return>50){
					Thre_step=0;
					Count_return=0;
				}
			}
	//		}
	//		else{
	//			flag_step=true;
	//		}
	//		}
			
			
	        m3 =handler1.obtainMessage(0);    //傳送1給 handler
	        handler1.sendMessage(m3);
	//		Text1.setText( Stepcount);	
	    }                                                              */
		

		}
	
		
		}
		
		
	
    class MyHandler1 extends Handler{         //handler
    	@Override
        public void handleMessage(Message msg) 
        {
   	    switch (msg.what) 
	    {
	        // 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
   	    
	        case 0:
	        	setT();
	        	save_pre();
   	        	break;
	        case 1:
	        	cal_count();
	        	time_log();
	        	break;
            }
    	
	    super.handleMessage(msg);
        }
    }
	
    public void save_pre(){
		
		 SharedPreferences.Editor editor = SPreferences.edit();
		
//		 editor.putInt("thres_value",threshhold_level );
		 editor.putInt("Step_count", Stepcount );
		 editor.commit();
    }
    
    public void get_peak(){
    	
    	
    	
    }
    
    public void count_ya_s(int j){
    	double i = 0.0;
    	double x,y,z;
    	i = x_list.get(j) * Math.sin(o3_list.get(j)/180*Math.PI);
 //   	w1_list.add(i);
    	x=i;
    	i = y_list.get(j) * Math.sin(o2_list.get(j)/180*Math.PI);
//    	w2_list.add(i);
    	y=i;
    	i = -(z_list.get(j) * Math.cos(o2_list.get(j)/180*Math.PI)*Math.cos(o3_list.get(j)/180*Math.PI));
//    	w3_list.add(i);
    	z=i;

    	now_power = x+y+z;
    	
    	Log.e("DebugCCC", " Get Data:" + now_power);
    	
    }
    
    public void count_ya(){
    	double i = 0.0;
    	temp_count_a=0.0;
    	double temp_count = 0.0;
    	
		for (int j = 0; j < x_list.size(); j++)
		{
			i = x_list.get(j) * Math.sin(o3_list.get(j)/180*Math.PI);
			t1_list.add(i);
			w1_list.add(i);
			i = y_list.get(j) * Math.sin(o2_list.get(j)/180*Math.PI);
			t2_list.add(i);
			w2_list.add(i);
			i = -(z_list.get(j) * Math.cos(o2_list.get(j)/180*Math.PI)*Math.cos(o3_list.get(j)/180*Math.PI));
			t3_list.add(i);
			w3_list.add(i);
			Log.e("DebugCCC", " Get Data:  " + t1_list.get(j) + "  , " + t2_list.get(j) + "  , " + t3_list.get(j) + "   ");
			
			temp_count = t1_list.get(j) + t2_list.get(j) + t3_list.get(j);
			
			if(temp_count >10.6){
			temp_count_a += (temp_count-10.6);
			}
			else if(temp_count < 9){
			temp_count_a +=	(9-temp_count);
			}
			
		}
		
		t1_list.clear();
		t2_list.clear();
		t3_list.clear();
		
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Button.OnClickListener click= new Button.OnClickListener() {

 		@Override
 		public void onClick(View v) {
 			

 			count = 0;
 			String strCaptureFilePath2 = "/sdcard/pedo";
 			String actionUrl="http://140.116.247.48/uploadtimelog/phoneupload2.php";
 			
    	//		String userDate=Date.getText().toString();
 			
 			String ServerURL="http://140.116.247.48/uploadtimelog/phoneupload2.php";
     		
 			//建立HTTP Post連線
 			HttpPost httpRequest=new HttpPost(ServerURL);
 		
 			//Post運作傳送變數必須用NameValuePair[]陣列儲存
 			List <NameValuePair> params=new ArrayList <NameValuePair>();
 		
 			
 			
 			try{
 				
 				String oldPath = "/sdcard/pedo/time.txt";
 				String newPath ="/sdcard/pedo/"+account_e.getText().toString();
 				File oldFile = new File( oldPath );
 				oldFile.renameTo(new File(newPath));
 				
 				params.add(new BasicNameValuePair("strid",account_e.getText().toString()));
 				
 				//發出HTTP request
 				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
 			
 				//取得HTTP response
 				HttpResponse httpResponse=new DefaultHttpClient().execute(httpRequest);
 			
 				//如果狀態碼為200  ok
 				if(httpResponse.getStatusLine().getStatusCode()==200){
 					//取出連線成功的回應字串
 					String strResult=EntityUtils.toString(httpResponse.getEntity());

 					Log.d("abc",strResult);
 					
 					//items=new ArrayList<String>();
 					//paths=new ArrayList<String>();
 					
 				File f=new File(strCaptureFilePath2);
 					File[] files=f.listFiles();
             	
 					if(files.length!=0){
             	            	
 						//將所有檔案加入ArrayList
 		 			for(int i=0;i<files.length;i++){
 							File file=files[i];
 							//items.add(file.getName());
 							//paths.add(file.getPath());
             		
 							File uploadfile=new File(file.getPath());
             			
 							newName=uploadfile.getName();
             			
 							//呼叫upload方法把檔案傳到伺服器
 							upload(uploadfile.getPath());
 					
 							
 							
 							Log.d("abc",Integer.toString(check));
 							
 							if(check==0){
 								count+=1;
 							}
 						}
             	
 						if(count!=0){
 			//				showDialog("特徵檔案上傳完成！");
 							new AlertDialog.Builder(PedometersActivity.this)
 							.setTitle("通知")
 							.setMessage("上傳成功")
 							.setPositiveButton
 							(
 							  "確定",
 							  new DialogInterface.OnClickListener() 
 							  {		
 								@Override
 								public void onClick(DialogInterface dialog, int which) 
 								{
 									// TODO Auto-generated method stub	
 								}
 							  }).show();
 							Log.d("abc","1");
 						}else{
 			//				showDialog("上傳不完全，請稍候再試。");
 							new AlertDialog.Builder(PedometersActivity.this)
 							.setTitle("警告")
 							.setMessage("上傳失敗，請檢察網路狀況")
 							.setPositiveButton
 							(
 							  "確定",
 							  new DialogInterface.OnClickListener() 
 							  {		
 								@Override
 								public void onClick(DialogInterface dialog, int which) 
 								{
 									// TODO Auto-generated method stub	
 								}
 							  }).show();
 							Log.d("abc","2");
 						}
 					}else{
 			//			showDialog("沒有檔案可以上傳！");
						new AlertDialog.Builder(PedometersActivity.this)
							.setTitle("警告")
							.setMessage("檔案錯誤")
							.setPositiveButton
							(
							  "確定",
							  new DialogInterface.OnClickListener() 
							  {		
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									// TODO Auto-generated method stub	
								}
							  }).show();
 						Log.d("abc","3");
 					}                                           
 				}else{
 			//		showDialog(httpResponse.getStatusLine().toString());
					new AlertDialog.Builder(PedometersActivity.this)
					.setTitle("警告")
					.setMessage("伺服器出錯")
					.setPositiveButton
					(
					  "確定",
					  new DialogInterface.OnClickListener() 
					  {		
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							// TODO Auto-generated method stub	
						}
					  }).show();
 					Log.d("abc","4");
 				}
 				
 				File newFile = new File( newPath );
 				newFile.renameTo(new File(oldPath));					
 				
 			}
 			catch (ClientProtocolException e){
 		//		showDialog(e.getMessage().toString());
 				Log.d("abc","5");
 				e.printStackTrace();
 			}catch (IOException e){
 		//		showDialog(e.getMessage().toString());
 				Log.d("abc","6");
 				e.printStackTrace();
 			}catch (Exception e){
 		//		showDialog(e.getMessage().toString());
 				Log.d("abc","7");
 				e.printStackTrace();
 			}
 			
 			
 			
			 SharedPreferences.Editor editor = SPreferences.edit();
				
			 editor.putString("account",account_e.getText().toString() );
			 editor.commit();
 			
 		}
 			
 		//	/sdcard/Myphoto/IMAG004
 			
     };
    
    private int upload(String uploadFile)
    {
      String end = "\r\n";
      String twoHyphens = "--";
      String boundary = "*****";
      String actionUrl="http://140.116.247.48/uploadtimelog/phoneupload2.php";
      try
      {
        URL url =new URL(actionUrl);
        
        
//		HttpPost httpRequest=new HttpPost(actionUrl);
		//Post運作傳送變數必須用NameValuePair[]陣列儲存
//		List <NameValuePair> params=new ArrayList <NameValuePair>();
//		params.add(new BasicNameValuePair("strid","John"));
		//發出HTTP request
//		httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		
		
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        /* 允許Input、Output，不使用Cache */
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        /* 設定傳送的method=POST */
        con.setRequestMethod("POST");
        /* setRequestProperty */
        
       
        
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        /* 設定DataOutputStream */
        DataOutputStream ds = 
          new DataOutputStream(con.getOutputStream());
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"thefile\";filename=\"" +
                      newName +"\"" + end);
        ds.writeBytes(end);   

        /* 取得檔案的FileInputStream */
        FileInputStream fStream = new FileInputStream(uploadFile);
        /* 設定每次寫入1024bytes */
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        /* 從檔案讀取資料至緩衝區 */
        while((length = fStream.read(buffer)) != -1)
        {
          /* 將資料寫入DataOutputStream中 */
          ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        /* close streams */
        fStream.close();
        ds.flush();
        
        /* 取得Response內容 */
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) != -1 )
        {
          b.append( (char)ch );
        }
        
        check=Integer.parseInt(b.toString().trim());
        
        /* 關閉DataOutputStream */
        ds.close();
      }
      catch(Exception e)
      {
   //     showDialog(""+e);
      }
      return check;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
    
    public void save_data(){
    	
    	double temp_plus_a=0;
    	double temp_plus=0;
    	
   // 	if(w1_list.size()>100){
    	
    	Date newDate = new Date();
    	double temp=0.0;
    	
    	for(int i=0;i<w1_list.size()-5;i++){
    		
    		temp_plus =0;
    		
    		for(int j=0;j<5;j++){

    			temp_plus_a =0;
    			temp_plus_a = temp_plus_a + w1_list.get(i+j)+ w2_list.get(i+j)+ w3_list.get(i+j);
    			if(temp_plus_a >10.6){
    				temp_plus+=(temp_plus_a-10.6);
    				}
    			else if(temp_plus_a < 9){	
    				temp_plus+=(9-temp_plus_a);	
    				}
    			
    		}

    		tempcount_list_plus.add(temp_plus);
    	}
    	
    	
    	tempcount_list.add(temp_count_a);
    	
    	String d_url = "/sdcard/pedo/"+newDate.getYear()+"-"+newDate.getMonth()+"-"+newDate.getDate()+"-"+"-"+newDate.getHours()+"-"+newDate.getMinutes()+"-"+newDate.getSeconds()+".txt";
    	try{
    		File filedir = new File("/sdcard/pedo");
    		filedir.mkdir();
    		File file = new File(d_url);
    		file.createNewFile();
            FileWriter fw = new FileWriter(d_url, false);
            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
            for(int j = 0; j < w1_list.size(); j++){
            temp = w1_list.get(j) + w2_list.get(j) + w3_list.get(j);
            if(j<5){
            bw.write(w1_list.get(j) + "\t" + w2_list.get(j) + "\t" + w3_list.get(j)+"\t"+temp+"\t"+tempcount_list_plus.get(5*(j/5)));
            bw.newLine();
            }
            else if(j< w1_list.size()-5){
            bw.write(w1_list.get(j) + "\t" + w2_list.get(j) + "\t" + w3_list.get(j)+"\t"+temp+"\t"+tempcount_list_plus.get(5*(j/5))+"\t"+tempcount_list_plus.get(j-5));
            bw.newLine();	
            }
            else{
                bw.write(w1_list.get(j) + "\t" + w2_list.get(j) + "\t" + w3_list.get(j)+"\t"+temp+"\t"+"0"+"\t"+tempcount_list_plus.get(j-5));
                bw.newLine();	
            }
            }
            bw.close();
        }catch(IOException e){
           e.printStackTrace();
        }
    	
    	
    	Log.e("DebugS", " Save Data:  ");
		w1_list.clear();
		w2_list.clear();
		w3_list.clear();
		
		tempcount_list.clear();
		tempcount_list_plus.clear();
 //   	}
    	
 //   	else{
    		
 //   	tempcount_list.add(temp_count_a);
    	
//    	}
    	
    }
    

    //卡路里計算
    public void cal_count(){
    	
    	now_date = new Date();
    	if(front_date.getDate()!=now_date.getDate()){                           //經過晚上12點做卡路里歸零
    		front_date = now_date;
    		front_date.setHours(0);
    		front_date.setMinutes(0);
    		front_date.setSeconds(0);
    		calories = 0;
    	}
    	
    	double time_d=(now_date.getTime()-front_date.getTime())/(1000);
        double temp_cal=0;
        double hight = hight_cm / 100;
    	if(time_d > 100){
    		temp_cal = weight * time_d / 3600 ;
    	}
    	else if(time_d > 10){
    		temp_cal = 0.2 * hight * weight * time_d /800;
    	}
    	else if(time_d > 6.67){
    		temp_cal = 0.25 * hight * weight * time_d /800;
    	}
    	else if(time_d >5){
    		temp_cal = 0.333 * hight * weight * time_d /800;
    	}
    	else if(time_d>4){
    		temp_cal = 0.5 * hight * weight * time_d /800;
    	}
    	else if(time_d>3.33){
    		temp_cal = 0.833 * hight * weight * time_d /800;
    	}
    	else if(time_d>2.5){
    		temp_cal = 1 * hight * weight * time_d /800;
    	}
    	else{
    		temp_cal = 1.2 * hight * weight * time_d /800;
    	}
    	
    	calories = calories + (float)temp_cal;
    	front_date = now_date;
    	SPreferences =getSharedPreferences("MyCustomSharedPreferences", 0);
		SharedPreferences.Editor editor = SPreferences.edit();
			
		editor.putFloat("calories",calories );
		editor.putLong("front_Date", front_date.getTime() );
		editor.commit();
    	
    }
    
    
    public void time_log(){
    	Date newDate = new Date();
    	String step_record = Integer.toString(Stepcount) +" : " + +newDate.getYear()+"-"+newDate.getMonth()+"-"+newDate.getDate()+"-"+"-"+newDate.getHours()+"-"+newDate.getMinutes()+"-"+newDate.getSeconds()+" : "+Float.toString(calories);
    	time_append(step_record );
    }
    
    public void exercise_log(String SaorSt){
    	Date newDate = new Date();
    	String step_record = SaorSt +" : " + +newDate.getYear()+"-"+newDate.getMonth()+"-"+newDate.getDate()+"-"+"-"+newDate.getHours()+"-"+newDate.getMinutes()+"-"+newDate.getSeconds();
    	time_append(step_record );
    }
    
    public void time_append(String time){
    	String p_url = "/sdcard/pedo/time.txt";
		FileWriter fWriter;
		PrintWriter pWriter;
    	try{
    		File filedir = new File("/sdcard/pedo");
    		filedir.mkdir();
    		File file = new File(p_url);

            fWriter = new FileWriter(file, true);
            pWriter = new PrintWriter(fWriter);
            pWriter.println(time);
   // 		file.createNewFile();
            pWriter.close();
            fWriter.close();
            Log.d("DebugaaC", time);
    	}
        catch(IOException e){
        e.printStackTrace();
     }
 }
    
    
    public void setT(){
    	 Stepcount_string = Integer.toString(Stepcount);
    	Text1.setText(Stepcount_string);
		String s_t_l = String.valueOf(new_threshod);
		Text3.setText(s_t_l);
    }
	
	public double getMean(List<Float> data) {
		double i = 0.0;
		for (int j = 0; j < data.size(); j++)
		{
			i += data.get(j);
		}
		return i / data.size();
	}
	
	public double varience(List<Float> data){
		
		double featureSet = this.getMean(data);

		double i = 0.0;
		for(int j= 0;j <data.size();j++){
			i += (data.get(j)-featureSet) * (data.get(j)-featureSet);
		}
		
		
		return i;
	}
	
	private void acquireWakeLock() {

		if(!ifLocked ){
		ifLocked = true;
		wakeLock.acquire();
		}
		}


	private void releaseWakeLock() {
		if(ifLocked){
		wakeLock.release();
		ifLocked = false;
		}

	}
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
  
        	
			new AlertDialog.Builder(PedometersActivity.this)
			.setTitle("通知")
			.setMessage("確定停止計步器?")
			.setPositiveButton
			(
			  "確定",
			  new DialogInterface.OnClickListener() 
			  {		
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					StartFlag = false;
	    			StartBT.setEnabled(true);
	//    			StopBT.setEnabled(true);
	    			AutoDetect.setEnabled(true);
	//    			save_step.setEnabled(true);
	    			leave.setEnabled(true);
	    			exercise.setEnabled(true);
	//    			save.setEnabled(true);
	    			menu.setEnabled(true);
	    			
	    			WindowManager.LayoutParams lp3 = getWindow().getAttributes();
	    			lp3.screenBrightness = screen_now_Bright;
	    			getWindow().setAttributes(lp3);
	    			
					// TODO Auto-generated method stub	
				}
			  })
			  .setNegativeButton("取消",
			  new DialogInterface.OnClickListener() 
			  {		
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					// TODO Auto-generated method stub	
				}
			  }).show();
        	
        	
        }
        return false;
    }
}