package com.eemc.aida;

import android.support.v7.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import java.io.*;
import java.util.*;
import android.support.v7.widget.*;
import android.content.*;
import android.app.*;

public class FileChooserActivity extends AppCompatActivity
{
	private File currentPath;
	private Vector<File> filesInCurrentPath;

	public final static String TAG_FILE_PATH = "file_path";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_chooser);

		setResult(RESULT_CANCELED);
		
		String pathString = null;
		try
		{
			pathString = getIntent().getExtras().getString(TAG_FILE_PATH);
		}
		catch (Throwable t)
		{}
		if (pathString == null)
			pathString = Environment.getExternalStorageDirectory().toString();
		currentPath = new File(pathString);

		openDirectory(currentPath);
	}

	private void select(File file)
	{
		if (file.isDirectory())
			openDirectory(file);
		else
			selectFile(file);
	}

	private void openDirectory(File directory)
	{
		currentPath = directory;
		filesInCurrentPath = new Vector<File>();

		File[] unmanagedFilesInCurrentDirectory = currentPath.listFiles();
		if (unmanagedFilesInCurrentDirectory != null)
		{
			for (File fileItem : unmanagedFilesInCurrentDirectory)
			{
				if (fileItem.isDirectory())
					filesInCurrentPath.add(fileItem);
			}
			for (File fileItem : unmanagedFilesInCurrentDirectory)
			{
				if (!fileItem.isDirectory())
					filesInCurrentPath.add(fileItem);
			}
		}

		ListView fileListView = (ListView) findViewById(R.id.file_chooser_list_view);
		fileListView.setAdapter(new FileAdapter());

		getSupportActionBar().setSubtitle(currentPath.getPath());
	}

	private void selectFile(File file)
	{
		Intent data = new Intent();
		Bundle extras = new Bundle();
		extras.putString(TAG_FILE_PATH,file.getPath());
		data.putExtras(extras);
		setResult(RESULT_OK,data);
		finish();
	}

	private class FileAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			if (currentPath.getPath().lastIndexOf("/") != -1)
				return filesInCurrentPath.size() + 1;
			if (filesInCurrentPath.size() == 0)
				return 1;
			return filesInCurrentPath.size();
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
			CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.file_chooser_card_view, null);

			if (currentPath.getPath().lastIndexOf("/") != -1)
			{
				if (p1 == 0)
				{
					AppCompatImageView fileImage = (AppCompatImageView) cardView.findViewById(R.id.file_chooser_card_view_image_view);
					fileImage.setImageResource(R.drawable.ic_folder_outline);

					AppCompatTextView textFileName = (AppCompatTextView) cardView.findViewById(R.id.file_chooser_card_view_text_name);
					textFileName.setText("...");

					cardView.setOnClickListener(new View.OnClickListener()
						{

							@Override
							public void onClick(View p1)
							{
								if (currentPath.getPath().lastIndexOf("/") != -1)
								{
									File lastFile = new File(currentPath.getPath().substring(0, currentPath.getPath().lastIndexOf("/")));
									openDirectory(lastFile);
								}
							}


						});
				}
				else
				{
					final File currentCardViewFile = filesInCurrentPath.get(--p1);
					AppCompatImageView fileImage = (AppCompatImageView) cardView.findViewById(R.id.file_chooser_card_view_image_view);
					if (currentCardViewFile.isDirectory())
						fileImage.setImageResource(R.drawable.ic_folder);
					else
						fileImage.setImageResource(R.drawable.ic_file);

					AppCompatTextView textFileName = (AppCompatTextView) cardView.findViewById(R.id.file_chooser_card_view_text_name);
					textFileName.setText(currentCardViewFile.getName());

					cardView.setOnClickListener(new View.OnClickListener()
						{

							@Override
							public void onClick(View p1)
							{
								select(currentCardViewFile);
							}


						});
				}
			}
			else
			{
				if (filesInCurrentPath.size() > 0)
				{
					final File currentCardViewFile = filesInCurrentPath.get(p1);
					AppCompatImageView fileImage = (AppCompatImageView) cardView.findViewById(R.id.file_chooser_card_view_image_view);
					if (currentCardViewFile.isDirectory())
						fileImage.setImageResource(R.drawable.ic_folder);
					else
						fileImage.setImageResource(R.drawable.ic_file);

					AppCompatTextView textFileName = (AppCompatTextView) cardView.findViewById(R.id.file_chooser_card_view_text_name);
					textFileName.setText(currentCardViewFile.getName());

					cardView.setOnClickListener(new View.OnClickListener()
						{

							@Override
							public void onClick(View p1)
							{
								select(currentCardViewFile);
							}


						});
				}
				else
				{
					AppCompatImageView fileImage = (AppCompatImageView) cardView.findViewById(R.id.file_chooser_card_view_image_view);
					fileImage.setImageResource(R.drawable.ic_folder_outline);

					AppCompatTextView textFileName = (AppCompatTextView) cardView.findViewById(R.id.file_chooser_card_view_text_name);
					textFileName.setText(R.string.file_selector_back_to_external_storage);

					cardView.setOnClickListener(new View.OnClickListener()
						{

							@Override
							public void onClick(View p1)
							{
								openDirectory(Environment.getExternalStorageDirectory());
							}


						});
				}
			}
			return cardView;
		}

	}
	
	public static void startThisActivity(Activity context,File path,int requestCode)
	{
		startThisActivity(context,path.getPath(),requestCode);
	}
	
	public static void startThisActivity(Activity context,String path,int requestCode)
	{
		Intent intent = new Intent(context,FileChooserActivity.class);
		Bundle extras = new Bundle();
		extras.putString(TAG_FILE_PATH,path);
		intent.putExtras(extras);
		context.startActivityForResult(intent,0);
	}
}
