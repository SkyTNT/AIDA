package com.eemc.aida;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.widget.*;
import android.support.v7.app.*;

public class CrashActivity extends AppCompatActivity
{
	LinearLayout mainl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mainl=new LinearLayout(this);
		mainl.setBackgroundColor(0xff1e88e5);
		mainl.setOrientation(1);
		setContentView(mainl);
		TextView ic=new TextView(this);
		ic.setText(" :(");
		ic.setTextColor(Color.WHITE);
		ic.setTextSize(50);
		mainl.addView(ic);
		ScrollView sv=new ScrollView(this);
		TextView info=new TextView(this);
		info.setTextColor(Color.WHITE);
		info.setText("你的AIDA似乎出了些问题\n"+getIntent().getExtras().getString("info"));
		sv.addView(info);
		mainl.addView(sv);
	}

	@Override
	public void onBackPressed()
	{
		System.exit(0);
		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE); 
		manager.killBackgroundProcesses("com.eemc.aida");
	}
}
