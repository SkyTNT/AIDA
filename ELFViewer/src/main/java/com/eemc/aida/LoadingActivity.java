package com.eemc.aida;
import android.support.v7.app.*;
import android.os.*;
import android.support.v7.widget.*;
import com.eemc.aida.elf.*;
import java.util.*;
import android.content.*;
import java.io.*;

public class LoadingActivity extends AppCompatActivity
{
	private LoadingActivityUIHandler mUIHandler;
	private AppCompatTextView countTextView;
	private Dumper dumper;
	private String filePath;
	private int symbolNum;

	public static final String TAG_FILE_PATH = "file_path";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_elf);

		mUIHandler = new LoadingActivityUIHandler();
		filePath = getIntent().getExtras().getString(TAG_FILE_PATH);
		dumper = new Dumper(filePath);
		countTextView = (AppCompatTextView) findViewById(R.id.loading_elf_text_view_message_count);
		ELFViewerActivity.loadedSymbols = new Vector<Symbol>();

		new Thread()
		{
			@Override
			public void run()
			{
				for (Section sec:dumper.elf.sections)
				{
					if (sec.type == 2 || sec.type == 11)
					{
						symbolNum += dumper.getSymNum(sec);
					}
				}

				for (Section sec:dumper.elf.sections)
				{
					if (sec.type == 2 || sec.type == 11)
					{
						for (int i=0;i < dumper.getSymNum(sec);++i)
						{
							Message msg=new Message();
							msg.what = 0;
							Symbol symbol = dumper.getSym(sec, i);
							ELFViewerActivity.loadedSymbols.add(symbol);
							msg.obj = ELFViewerActivity.loadedSymbols.size();
							mUIHandler.sendMessage(msg);
						}
					}
				}

				Objdump.prepare(LoadingActivity.this);
				BIN2ASM.prepare(LoadingActivity.this);


				Message msg=new Message();
				msg.what = 1;
				mUIHandler.sendMessage(msg);
			}
		}.start();
	}

	private class LoadingActivityUIHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (msg.what == 0)
				countTextView.setText(new Integer((int)msg.obj).toString() + " / " + new Integer(symbolNum).toString());
			else if (msg.what == 1)
			{
				ELFViewerActivity.startThisActivity(LoadingActivity.this, filePath);
				finish();
			}
		}
	}

	public static void startThisActivity(Context context, String path)
	{
		Intent intent = new Intent(context, LoadingActivity.class);
		Bundle extras = new Bundle();
		extras.putString(TAG_FILE_PATH, path);
		intent.putExtras(extras);
		context.startActivity(intent);
	}

	public static void startThisActivity(Context context, File path)
	{
		startThisActivity(context, path.getPath());
	}

	@Override
	public void onBackPressed()
	{

	}
}
