package com.eemc.aida;

import android.content.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import android.view.*;
import android.util.*;
import android.support.v7.app.AlertDialog;

public final class Utils
{
	public static int endian=1;
	public static String b2hex(byte[] bytes)
	{
		String result="";
		for (byte b:bytes)
		{
			if (endian == 1)
			{
				result = b2hex(b) + result;
			}
			else if (endian == 2)
			{
				result += b2hex(b);
			}
		}
		return result;
	}

	public static String b2hex(byte b)
	{
		String result="";
		if (b >= 0)
		{
			result = Integer.toHexString(b);
		}
		else
		{
			result = Integer.toHexString(2 * 128 + b);
		}
		if (result.length() < 2)
		{
			result = "0" + result;
		}
		return result;
	}


	public static String i2hex(int value)
	{
		String base=Integer.toHexString(value);
		String result=base;
		for (int i=0;i < (8 - base.length());i++)
		{
			result = "0" + result;
		}
		result = "0x" + result;
		return result;
	}

	public static byte[] readFile(String fileName)
	{
		try
		{
			File file = new File(fileName);
			FileInputStream fis=new FileInputStream(file);
			byte b[]=new byte[(int)file.length()];
			fis.read(b);
			fis.close();
			return b;
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return null;
	}

	public static byte[] cp(byte[] res, int start, int count)
	{
		if (res == null)
		{
			return null;
		}
		byte[] result = new byte[count];
		for (int i=0;i < count;i++)
		{
			result[i] = res[start + i];
		}
		return result;
	}

	public static int b2i(byte[] src)
	{
		return Integer.parseInt(b2hex(src), 16);
	}   
	static public int cb2i(byte[] res, int start, int count)
	{
		return b2i(cp(res, start, count));
	}

	public static long b2l(byte[] src)
	{
		return Long.parseLong(b2hex(src), 16);
	}   
	static public long cb2l(byte[] res, int start, int count)
	{
		return b2l(cp(res, start, count));
	}

	public static native String demangle(String name);
	
	static
	{
		System.loadLibrary("elf-viewer");
	}
}
