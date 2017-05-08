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
	private final static int REQUEST_CODE_CHOOSE_FILE = 0;
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loadProjectsData();
		refreshProjectCardViews();
		
		ActionBar supportActionBar=getSupportActionBar();
		supportActionBar.setTitle(R.string.app_name);
		supportActionBar.setSubtitle(R.string.main_subtitle_projects);

		FloatingActionButton buttonAddNew=(FloatingActionButton) findViewById(R.id.main_plus_button);
		buttonAddNew.setImageResource(R.drawable.ic_plus);
		buttonAddNew.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					startActivityForResult(new Intent(MainActivity.this, FileChooserActivity.class), REQUEST_CODE_CHOOSE_FILE);
				}
			});
			
		FloatingActionButton buttonGCCTools=(FloatingActionButton) findViewById(R.id.main_more_button);
		buttonGCCTools.setImageResource(R.drawable.ic_puzzle);
		buttonGCCTools.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					new AlertDialog.Builder(MainActivity.this).setTitle(R.string.main_gcc_tools).setPositiveButton(android.R.string.cancel,new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								p1.dismiss();
							}
							
						
					}).setItems(R.array.main_gcc_tool_items,new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								switch(p2)
								{
								case 0:
									RuntimeActivity.startThisActivity(MainActivity.this,"");
									break;
								case 1:
									break;
								case 2:
									break;
								case 3:
									break;
								}
							}
							
						
					}).show();
				}
			});
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_CHOOSE_FILE && resultCode == FileChooserActivity.RESULT_OK)
		{
			String path = data.getExtras().getString(FileChooserActivity.TAG_FILE_PATH);

			for (int i=0;i < projectItems.size() ;++i)
			{
				if (projectItems.get(i).equals(path))
				{
					new AlertDialog.Builder(MainActivity.this).setTitle(R.string.main_repeated_file).setMessage(R.string.main_repeated_file_message).setIcon(R.drawable.ic_file_multiple).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								p1.dismiss();
							}


						}).show();
					return;
				}
			}
			byte byteFileHeader[]=new byte[4];
			try
			{
				FileInputStream fileInputStream=new FileInputStream(path);
				fileInputStream.read(byteFileHeader);
				fileInputStream.close();
			}
			catch (IOException ioException)
			{}

			if (byteFileHeader[0] == 0x7f && byteFileHeader[1] == 0x45 && byteFileHeader[2] == 0x4c && byteFileHeader[3] == 0x46)
			{
				projectItems.add(path);
				refreshProjectCardViews();
			}
			else
			{
				new AlertDialog.Builder(MainActivity.this).setTitle(R.string.main_is_not_elf_file).setMessage(R.string.main_is_not_elf_file_message).setIcon(R.drawable.ic_file_document).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							p1.dismiss();
						}


					}).show();
			}
		}
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
						File file = new File(path);
						if (file.exists())
						{
							LoadingActivity.startThisActivity(MainActivity.this,path);
						}
						else
						{
							new AlertDialog.Builder(MainActivity.this).setTitle(R.string.main_missing_file).setMessage(R.string.main_missing_file_message).setIcon(R.drawable.ic_file_hidden).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										p1.dismiss();
									}


								}).setNegativeButton(R.string.main_delete_this_project, new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										projectItems.remove(path);
										refreshProjectCardViews();
										p1.dismiss();
									}


								}).show();
						}
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
