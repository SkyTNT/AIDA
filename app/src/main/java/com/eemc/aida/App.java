package com.eemc.aida;
import android.app.*;

public class App extends Application
{
	CrashHandler ch;
	static int muid=android.os.Process.myUid();
	@Override
	public void onCreate()
	{
		super.onCreate();
		if(ch!=null){
		ch=new CrashHandler();
		}
		ch.init(this);
	}
	static
	{
        System.loadLibrary("aida");
    }
}
