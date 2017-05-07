package com.eemc.aida;
import android.support.v7.app.*;
import android.os.*;
import android.content.*;
import java.io.*;
import android.support.design.widget.*;
import android.view.View.*;
import android.view.*;
import android.support.v7.widget.*;

public class RuntimeActivity extends AppCompatActivity
{
	private String executable_file_path;
	public final static String TAG_EXECUTABLE_FILE_PATH = "executable_file_path";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		executable_file_path = getIntent().getExtras().getString(TAG_EXECUTABLE_FILE_PATH);
		setContentView(R.layout.executable_runtime);

		File executable_file = new File("/data/data/" + getPackageName() + "/files/" + executable_file_path);
		executable_file.mkdirs();
		try
		{
			executable_file.createNewFile();
			InputStream fileInputStream = getAssets().open(executable_file_path);
			FileOutputStream fileOutPut = new FileOutputStream(executable_file);
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

		getSupportActionBar().setSubtitle(executable_file.getName());
		executable_file_path = executable_file.getPath();

		FloatingActionButton buttonNewInstruction = (FloatingActionButton)findViewById(R.id.executable_runtime_add_new_instruction);
		buttonNewInstruction.setImageResource(R.drawable.ic_pencil);
		buttonNewInstruction.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					new AlertDialog.Builder(RuntimeActivity.this).setMessage(R.string.executable_runtime_title).setView(new AppCompatEditText(RuntimeActivity.this)).show();
				}

			});
	}



	public static void startThisActivity(Context context, String path)
	{
		Intent intent = new Intent(context, RuntimeActivity.class);
		Bundle extras = new Bundle();
		extras.putString(TAG_EXECUTABLE_FILE_PATH, path);
		intent.putExtras(extras);
		context.startActivity(intent);
	}

	public static void startThisActivity(Context context, File path)
	{
		startThisActivity(context, path.getPath());
	}
}
