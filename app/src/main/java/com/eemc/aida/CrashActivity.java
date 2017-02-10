package com.eemc.aida;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.graphics.*;

public class CrashActivity extends Activity
{
	RelativeLayout mainl;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mainl=new RelativeLayout(this);
		mainl.setBackgroundColor(Color.BLUE);
		setContentView(mainl);
		TextView info=new TextView(this);
		info.setTextColor(Color.WHITE);
		info.setText(getIntent().getExtras().getString("info"));
		mainl.addView(info);
	}

	@Override
	public void onBackPressed()
	{
		// TODO: Implement this method
		super.onBackPressed();
		android.os.Process.killProcess(App.muid);
		System.exit(0);
	}
}
