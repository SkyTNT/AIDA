package com.eemc.aida;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.gc.materialdesign.views.*;
import com.gc.materialdesign.widgets.*;
import java.io.*;
import org.json.*;

import android.support.v7.widget.Toolbar;
import android.widget.ScrollView;
import com.gc.materialdesign.widgets.Dialog;

public class MainActivity extends Activity
{
	RelativeLayout mainlayout;
	LinearLayout plist;
	JSONObject projects;
	Activity self=this;
	int width,height,sbheight;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		mainlayout=new RelativeLayout(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager wm =(WindowManager)getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			sbheight=getResources().getDimensionPixelSize(resourceId);
			height-=sbheight;
		}
		setContentView(mainlayout);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
           	getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			mainlayout.setFitsSystemWindows(true);
			ViewGroup contentLayout = (ViewGroup)findViewById(android.R.id.content);
			View statusBarView = new View(this);
			contentLayout.addView(statusBarView,width,sbheight);
			statusBarView.setBackgroundColor(0xff1e88e5);
        }
		
		try
		{
			initFiles();
		}
		catch (Exception e)
		{
			try
			{
				projects = new JSONObject("{\"num\":0}");
			}
			catch (JSONException e2)
			{}
			Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
		}
		Toolbar tb=new Toolbar(this);
		//tb.setNavigationIcon(R.drawable.ic_launcher);
		tb.setTitle("AIDA");
		tb.setSubtitle("工程");
		tb.setTitleTextColor(Color.WHITE);
		tb.setBackgroundColor(0xff1e88e5);
		mainlayout.addView(tb,width,height/10);
		
		ScrollView plv=new ScrollView(this);
		plv.setY(height/10);
		mainlayout.addView(plv,width,height-height/10);
		plist=new LinearLayout(this);
		plist.setOrientation(1);
		plv.addView(plist);
		try
		{
			for (int i=0;i<projects.getInt("num");i++)
			{
				addProjectButton(projects.getString(i+""));
			}
		}
		catch (Exception e)
		{
			
		}

		
		final ButtonFloat newpj=new ButtonFloat(this);
		newpj.setText("+");
		newpj.setRippleSpeed(30);
		newpj.setTextColor(Color.WHITE);
		newpj.setX(width-height/10-10);
		newpj.setY(height-height/10-10);
		newpj.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					final FileChooser fc=new FileChooser(self,"/sdcard");
					fc.setOnFiniEve(new Runnable(){
							@Override
							public void run()
							{
								try
								{
									for (int i=0;i<projects.getInt("num");i++)
									{
										if(projects.getString(i+"").equals(fc.chose.getPath())){
											Dialog d=new Dialog(self,"错误","你已添加过了");
											d.show();
											return;
										}
									}
									FileInputStream fis=new FileInputStream(fc.chose);
									byte b[]=new byte[4];
									fis.read(b);
									fis.close();
									if(b[0]==0x7f&&b[1]==0x45&&b[2]==0x4c&&b[3]==0x46){
									addProjectButton(fc.chose.getPath());
									projects.put("num",plist.getChildCount());
									projects.put(""+(plist.getChildCount()-1),fc.chose.getPath());
									}else{
										Dialog d=new Dialog(self,"错误","该文件不是有效的elf文件");
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
		mainlayout.addView(newpj,height/10,height/10);
    }
	
	void addProjectButton(final String path){
		final ButtonRectangle pj=new ButtonRectangle(self);
		pj.setText(path.substring(path.lastIndexOf("/")+1));
		pj.setRippleSpeed(30);
		pj.setBackgroundColor(Color.WHITE);
		pj.setTextColor(Color.BLACK);
		pj.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					Intent intent=new Intent(self,AIDAActivity.class);
					intent.putExtra("path",path);
					startActivity(intent);
				}
			});
		plist.addView(pj,width,height/6);
	}
	
	void initFiles() throws Exception{
		copyBin();
		File m=new File(Utils.mainPath);
		if(!m.exists()){
			m.mkdir();
		}
		File pjs=new File(Utils.mainPath+"/projects");
		if(!pjs.exists()){
			pjs.createNewFile();
		}
		String str=new String(Utils.readFile(Utils.mainPath+"/projects"));
		
		projects=new JSONObject(str);
	}
	
	void copyBin(){
		try
		{
			byte[]b=new byte[240844];
			InputStream in=getAssets().open("disassembler");
			in.read(b);
			in.close();
			OutputStream out=openFileOutput("disassembler",MODE_WORLD_WRITEABLE|MODE_WORLD_READABLE);
			out.write(b);
			out.close();
			Runtime.getRuntime().exec("chmod 777 /data/data/com.eemc.aida/files/disassembler");
		}
		catch (Exception e)
		{
			Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
		}
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Utils.saveFile(Utils.mainPath+"/projects",projects.toString().getBytes());
	}
}
