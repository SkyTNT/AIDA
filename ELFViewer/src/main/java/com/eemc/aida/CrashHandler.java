package com.eemc.aida;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.lang.Thread.*;
import android.util.*;

public class CrashHandler implements UncaughtExceptionHandler
{
	private Context ctx;
	private UncaughtExceptionHandler mDefaultHandler;
	
	public void init(Context ctx,UncaughtExceptionHandler defaultHandlet)
	{
		this.ctx = ctx;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(final Thread p1, final Throwable p2)
	{
		new Thread() {      
            @Override      
            public void run()
			{      
				Intent intent = new Intent(ctx, CrashActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("info", "Thread:\t" + p1.getName() + "(id:" + p1.getId() + ")\nError:\n" + Log.getStackTraceString(p2));
				ctx.startActivity(intent); 
				mDefaultHandler.uncaughtException(p1,p2);
            }      
        }.start();
	}

}
