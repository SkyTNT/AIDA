package com.eemc.aida.elf;
import android.content.*;
import java.io.*;

public class Objdump extends ExecutableFileRunner
{
	public static void prepare(Context context)
	{
		copyBinFile(context,"binary/i686/objdump");
		copyBinFile(context,"binary/arm/objdump");
	}

	public static String dump(Context context,boolean isI686,int start_addr,int stop_addr,String filePath)
	{
		String exe_path = "/data/data/"+context.getPackageName()+"/files/binary/"+(isI686?"i686/":"arm/")+"objdump";
		String start_str = "--start-address=0x"+Integer.toHexString(start_addr);
		String stop_str = "--stop-address=0x"+Integer.toHexString(stop_addr);
		String instruction = exe_path + " -S " + start_str + " " + stop_str + " " + filePath;
		try
		{
			Runtime.getRuntime().exec(new String("chmod 751 "+exe_path).split(" "));
			StringBuilder strBuilder=new StringBuilder();
			byte[] b=new byte[1];
			InputStream r=Runtime.getRuntime().exec(instruction.split(" ")).getInputStream();
			while (r.read(b) != -1)
			{
				strBuilder.append(new String(b));
			}
			
			
			String result = new String(strBuilder);
			String[] results = result.split("\n\n");
			for(String result_item : results)
			{
				if(result_item.contains(Integer.toHexString(start_addr)))
					return result_item;
			}
			return result;
		}
		catch (Throwable e)
		{
			return e.toString();
		}
		//return null;
	}
}
