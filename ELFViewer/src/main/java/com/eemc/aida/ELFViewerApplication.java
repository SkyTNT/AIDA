package com.eemc.aida;
import android.app.*;
import android.util.*;
import java.util.*;

public class ELFViewerApplication extends Application
{
	private CrashHandler crashHandler;
	@Override
	public void onCreate()
	{
		super.onCreate();
		if (crashHandler == null)
		{
			crashHandler = new CrashHandler();
		}
		crashHandler.init(this,Thread.getDefaultUncaughtExceptionHandler());
	}
	
	static
	{
        System.loadLibrary("elf-viewer");
    }
}
