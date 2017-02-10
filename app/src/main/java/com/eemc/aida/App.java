package com.eemc.aida;
import android.app.*;

public class App extends Application
{
	static int muid=android.os.Process.myUid();
	@Override
	public void onCreate()
	{
		super.onCreate();
		CrashHandler ch=new CrashHandler();
		ch.init(this);
	}
	static
	{
        System.loadLibrary("aida");
    }
}
