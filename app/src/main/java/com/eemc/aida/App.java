package com.eemc.aida;
import android.app.*;
import android.util.*;
import java.util.*;

public class App extends Application
{
	CrashHandler ch;
	@Override
	public void onCreate()
	{
		super.onCreate();
		if(ch==null){
		ch=new CrashHandler();
		}
		ch.init(this);
	}
	static
	{
        System.loadLibrary("aida");
    }
}
