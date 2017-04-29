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

	public static boolean saveFile(String fileName, byte[] arys)
	{
		File file = new File(fileName);
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(file);
			fos.write(arys);
			fos.flush();
			return true;
		}
		catch (Exception e)
		{
			System.out.println("save file error:" + e.toString());
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (Exception e)
				{
					System.out.println("close file error:" + e.toString());
				}
			}
		}
		return false;
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

	static public String disassemble(int mode, long given)
	{
		StringBuilder sb=new StringBuilder();
		try
		{
			byte[] b=new byte[1];
			InputStream r=Runtime.getRuntime().exec(new String[]{"/data/data/com.eemc.aida/files/disassembler",mode + "","" + given}).getInputStream();
			while (r.read(b) != -1)
			{
				sb.append(new String(b));
			}
		}
		catch (Exception e)
		{
			return "反编译失败" + e.toString();
		}
		return sb.toString();
	}

	public static native String demangle(String name);
}

class FileChooser
{
	String path;
	Context con;
	Runnable onfini;
	public File chose;
	FileChooser(Context con, String path)
	{
		this.con = con;
		this.path = path;
	}
	void start()
	{
		final FileAdapter fa=new FileAdapter(con, path);
		new AlertDialog.Builder(con, R.style.Theme_AppCompat_Light_Dialog_Alert).setTitle("选择文件").setAdapter(fa, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					if (p2 != 0)
					{
						if (fa.fl[p2 - 1].isDirectory())
						{
							path = fa.fl[p2 - 1].getPath();
							start();
						}
						else
						{
							if (onfini != null)
							{
								chose = fa.fl[p2 - 1];
								onfini.run();
							}
						}
					}
					else
					{
						path = path.substring(0, path.lastIndexOf("/"));
						if (path.equals(""))
						{
							path = "/";
						}
						start();
					}
				}
			}).create().show();
	}
	void setOnFiniEve(Runnable r)
	{
		this.onfini = r;
	}


	class FileAdapter extends BaseAdapter
	{
		Context con;
		public File[]fl;

		FileAdapter(Context con, String path)
		{
			this.con = con;
			Vector<File> vf=new Vector<File>();
			for (File f:new File(path).listFiles())
			{
				if (f.canRead())
				{
					vf.add(f);
				}
			}
			fl = new File[vf.size()];
			for (int i=0;i < vf.size();i++)
			{
				fl[i] = vf.get(i);
			}
		}

		@Override
		public int getCount()
		{
			return fl.length + 1;
		}

		@Override
		public Object getItem(int p1)
		{
			return fl[p1 - 1];
		}

		@Override
		public long getItemId(int p1)
		{

			return p1 - 1;
		}

		@Override
		public View getView(int p1, View p2, ViewGroup p3)
		{
			LinearLayout ml=new LinearLayout(con);
			ImageView iv=new ImageView(con);
			TextView tv=new TextView(con);
			if (p1 != 0)
			{
				if (fl[p1 - 1].isFile())
				{
					iv.setImageResource(R.drawable.ic_file);
				}
				else
				{
					iv.setImageResource(R.drawable.ic_folder);
				}
				tv.setText(fl[p1 - 1].getName());
			}
			else
			{
				iv.setImageResource(R.drawable.ic_folder_outline);
				tv.setText("..");
			}
			ml.addView(iv, 60, 60);
			ml.addView(tv);
			return ml;
		}
	}
}
