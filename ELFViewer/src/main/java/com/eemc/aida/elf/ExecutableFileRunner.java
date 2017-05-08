package com.eemc.aida.elf;
import java.io.*;
import android.content.*;

public class ExecutableFileRunner
{
	static void copyBinFile(Context context, String fileName)
	{
		try
		{
			InputStream fileInputStream = context.getAssets().open(fileName);
			String fileFullName = "/data/data/" + context.getPackageName() + "/files/"+fileName;
			File file = new File(fileFullName.substring(0,fileFullName.lastIndexOf("/")));
			file.mkdirs();
			File file_exe = new File(fileFullName);
			file_exe.createNewFile();
			FileOutputStream fileOutPut = new FileOutputStream(file_exe);
			int byteReaded = -1;
			byte[] buffer = new byte[1024];
			while ((byteReaded = fileInputStream.read(buffer)) != -1)
			{
				fileOutPut.write(buffer, 0, byteReaded);
			}
			fileInputStream.close();
			fileOutPut.close();
		}
		catch (IOException e)
		{}
	}
}
