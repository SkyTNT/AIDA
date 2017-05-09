package com.eemc.aida.elf;
import android.content.*;
import java.io.*;

public class BIN2ASM extends ExecutableFileRunner
{
	public static void prepare(Context context)
	{
		copyBinFile(context,"disassembler");
	}

	public static String dump(Context context,int mode,long given)
	{
		String exe_path = "/data/data/"+context.getPackageName()+"/files/disassembler";
		StringBuilder sb=new StringBuilder();
		try
		{
			Runtime.getRuntime().exec(new String("chmod 751 "+exe_path).split(" "));
			InputStream r=Runtime.getRuntime().exec(new String[]{exe_path,mode + "","" + given}).getInputStream();
			byte[] buffer=new byte[r.available()];
			r.read(buffer);
			sb.append(buffer);
		}
		catch (Exception e)
		{
			return "BIN2ASM:ERROR:" + e.toString();
		}
		return sb.toString();
	}
}
