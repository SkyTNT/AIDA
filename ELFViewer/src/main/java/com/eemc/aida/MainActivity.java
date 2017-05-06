package com.eemc.aida;

import android.content.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity
{
	private Vector<String> projectItems;
	private final static String TAG_PROJECTS = "project_items";

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loadProjectsData();
		refreshProjectCardViews();
		copyBin();

		ActionBar supportActionBar=getSupportActionBar();
		supportActionBar.setTitle(R.string.app_name);
		supportActionBar.setSubtitle("工程");

		FloatingActionButton buttonAddNew=(FloatingActionButton) findViewById(R.id.main_plus_button);
		buttonAddNew.setImageResource(R.drawable.ic_plus);
		buttonAddNew.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					FileChooser fc=new FileChooser(MainActivity.this, Environment.getExternalStorageDirectory().toString());
					fc.setOnFinishEvent(new FileChooser.FileChooserOnFinishEvent(){

							public void onFinish(File chose)
							{
								try
								{
									for (int i=0;i < projectItems.size() ;++i)
									{
										if (projectItems.get(i).equals(chose.getPath()))
										{
											AlertDialog d=new AlertDialog.Builder(MainActivity.this).setTitle("错误").setMessage("你已添加过了").create();
											d.show();
											return;
										}
									}
									FileInputStream fis=new FileInputStream(chose);
									byte b[]=new byte[4];
									fis.read(b);
									fis.close();
									if (b[0] == 0x7f && b[1] == 0x45 && b[2] == 0x4c && b[3] == 0x46)
									{
										projectItems.add(chose.getPath());
										refreshProjectCardViews();
									}
									else
									{
										android.support.v7.app.AlertDialog d=new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("错误").setMessage("该文件不是有效的elf文件").create();
										d.show();
									}
								}
								catch (Exception e)
								{
								}
							}
						});
					fc.start();
				}
			});
    }

	private void refreshProjectCardViews()
	{
		saveProjectsData();
		ListView listView = (ListView) findViewById(R.id.main_listview);
		listView.setAdapter(new ProjectCardViewAdapter());
	}

	private void loadProjectsData()
	{
		projectItems = new Vector<String>();
		SharedPreferences preferences=getSharedPreferences(TAG_PROJECTS, Context.MODE_MULTI_PROCESS);
		int num = preferences.getInt("projects_num", 0);
		for (int index=0;index < num;++index)
		{
			String projectItem = preferences.getString(new Integer(index).toString(), null);
			if (projectItem != null)
				projectItems.add(projectItem);
		}
	}

	private void copyBin()
	{
		try
		{
			byte[]b=new byte[240844];
			InputStream in=getAssets().open("disassembler");
			in.read(b);
			in.close();
			OutputStream out=openFileOutput("disassembler", MODE_WORLD_WRITEABLE | MODE_WORLD_READABLE);
			out.write(b);
			out.close();
			Runtime.getRuntime().exec("chmod 777 /data/data/com.eemc.aida/files/disassembler");
		}
		catch (Exception e)
		{
		}
	}

	private void saveProjectsData()
	{
		SharedPreferences.Editor preferencesEdtitor=getSharedPreferences(TAG_PROJECTS, Context.MODE_MULTI_PROCESS).edit();
		preferencesEdtitor.putInt("projects_num", projectItems.size());
		for (int index=0;index < projectItems.size();++index)
		{
			preferencesEdtitor.putString(new Integer(index).toString(), projectItems.get(index));
		}
		preferencesEdtitor.commit();
	}

	private class ProjectCardViewAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return projectItems.size();
		}

		@Override
		public Object getItem(int p1)
		{
			return p1;
		}

		@Override
		public long getItemId(int p1)
		{
			return p1;
		}

		@Override
		public View getView(int p1, View p2, ViewGroup p3)
		{
			final String path = projectItems.get(p1);
			CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.main_cardview, null);
			AppCompatTextView nameView=(AppCompatTextView)cardView.findViewById(R.id.main_cardview_name);
			nameView.setText(path.substring(path.lastIndexOf("/") + 1));
			AppCompatTextView pathView=(AppCompatTextView)cardView.findViewById(R.id.main_cardview_full_path);
			pathView.setText(path);
			cardView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						Intent intent=new Intent(MainActivity.this, ELFViewerActivity.class);
						intent.putExtra("path", path);
						startActivity(intent);
					}

				});
			cardView.setOnLongClickListener(new View.OnLongClickListener()
				{

					@Override
					public boolean onLongClick(View p1)
					{
						new AlertDialog.Builder(MainActivity.this).setIcon(R.drawable.ic_delete).setTitle(R.string.main_delete_item).setMessage(R.string.main_delete_item_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									projectItems.remove(path);
									refreshProjectCardViews();
									p1.dismiss();
								}


							}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									p1.dismiss();
								}


							}).show();
						return false;
					}


				});
			return cardView;
		}
	}
}
