package com.eemc.aida;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.eemc.aida.elf.Tables;
import com.eemc.aida.elf.*;
import com.eemc.aida.views.*;
import java.util.*;
import android.support.v7.widget.Toolbar;
import java.io.*;

public class ELFViewerActivity extends AppCompatActivity
{
	private String path;
	private RelativeLayout mainlayout;
	private LinearLayout ll;
	private AppCompatImageButton showmenu;
	private ListView symlist;
	private EditText symsearch;
	private HexView vhex;
	private SymbolAdapter symad;
	private int width,height,sbheight;
	private Dump dumper;
	private HashMap<Integer,RelativeLayout>vmap=new HashMap<Integer,RelativeLayout>();
	private HashMap<Integer,FloatingActionButton>bfmap=new HashMap<Integer,FloatingActionButton>();
	
	public static final String TAG_FILE_PATH = "file_path";
	public static Vector<Symbol> loadedSymbols=new Vector<Symbol>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer);
		
		path = this.getIntent().getExtras().getString(TAG_FILE_PATH);
		dumper = new Dump(path);
		setTitle(path.substring(path.lastIndexOf("/") + 1));
		
		mainlayout = new RelativeLayout(this);
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
		{
			sbheight = getResources().getDimensionPixelSize(resourceId);
			height -= sbheight;
		}
		
		
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setSubtitle(path.substring(path.lastIndexOf("/") + 1));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		showmenu = new AppCompatImageButton(this);
		showmenu.setX(width - height / 15 - 10);
		showmenu.setY(height / 20 - height / 30);
		showmenu.setImageResource(R.drawable.ic_menu);
		mainlayout.addView(showmenu, height / 15, height / 15);
		
		/*menu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener(){
				@Override
				public boolean onMenuItemClick(MenuItem p1)
				{
					switch (p1.getItemId())
					{
						case 0:
							
							break;
						case 1:
							AlertDialog.Builder d1=new AlertDialog.Builder(ELFViewerActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
							final EditText addr=new EditText(ELFViewerActivity.this);
							addr.setHint("地址(16进制)");
							addr.addTextChangedListener(new TextWatcher(){
									String sold="";
									@Override
									public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
									{
										sold = p1.toString();
									}

									@Override
									public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
									{
										char[]replace=p1.toString().substring(p2, p2 + p4).toCharArray();
										for (char r:replace)
										{
											if (r != 0 && (r < '0' || r > 'f'))
											{
												addr.setText(sold);
												addr.setSelection(p2);
											}
										}
									}

									@Override
									public void afterTextChanged(Editable p1)
									{

									}
								});
							d1.setTitle("跳转").setView(addr).setCancelable(false).setNegativeButton("确定", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										int address=Integer.parseInt(addr.getText().toString(), 16);
										vhex.setChoose(address, 1);
										vhex.scrollToLine(address / 8);
										vhex.memLine = address / 8;
										setCardView(1);
									}
								});
							d1.create().show();
							break;
						default:break;
					}
					return false;
				}
			});
*/
		HorizontalScrollView hsv=new HorizontalScrollView(this);
		hsv.setY(height / 10);
		hsv.setBackgroundColor(0xff1e88e5);
		ll = new LinearLayout(this);
		hsv.addView(ll);
		mainlayout.addView(hsv, width, height / 15);
		initCards();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_viewer, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == android.R.id.home)
			finish();
		else if(item.getItemId() == R.id.action_find)
		{
			final EditText editText=new EditText(ELFViewerActivity.this);
			new AlertDialog.Builder(ELFViewerActivity.this).
			setTitle(R.string.search_menu_title).setView(editText).setCancelable(false).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						setCardView(0);
						symad.showing.clear();

						for (int i=0;i < loadedSymbols.size() ;++i)
						{
							if (loadedSymbols.get(i).demangledName.contains(editText.getText().toString()))
							{
								symad.showing.add(i);
							}
						}

						symad.notifyDataSetChanged();
					}
				}).show();
		}
		return super.onOptionsItemSelected(item);
	}

	private void initCards()
	{
		RelativeLayout rsyms=new RelativeLayout(this);
		symlist = new ListView(this);
		symlist.setFastScrollEnabled(true);
		symlist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					Symbol sym=loadedSymbols.get(symad.showing.get(p3));
					if (sym.type == 2)
					{
						int addr=sym.value;
						int s=sym.size;
						if (s == 0)
						{
							s = 1;
						}
						vhex.setChoose(addr, s);
						vhex.scrollToLine(addr / 8);
						vhex.memLine = addr / 8;
						byte[] basecode=Utils.cp(dumper.bs, sym.value - 1, sym.size);
						String codes="";
						for (int i=0;i < sym.size / 2;i++)
						{
							byte[]co=Utils.cp(basecode, i * 2, 2);
							codes += Utils.disassemble(0, Utils.b2i(co)) + "\n";
						}
						AlertDialog.Builder d=new AlertDialog.Builder(ELFViewerActivity.this);
						d.setMessage("全名:\n" + sym.name + "汇编:\n" + codes);
						d.setPositiveButton("转到16进制视图", new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									setCardView(1);
								}
							});
						d.show();
					}
				}
			});
		symad = new SymbolAdapter(this);
		symlist.setAdapter(symad);
		rsyms.addView(symlist, width, height - height / 10 - height / 15);
		symsearch = new EditText(this);
		symsearch.setHint(R.string.search_menu_title);
		symsearch.setY(-height / 15);
		symsearch.addTextChangedListener(new TextWatcher(){

				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
				{}
				@Override
				public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
				{}
				@Override
				public void afterTextChanged(Editable p1)
				{
					symad.showing.clear();
					for (int i=0;i < loadedSymbols.size();++i)
					{
						if (loadedSymbols.get(i).demangledName.contains(p1.toString()))
						{
							symad.showing.add(i);
						}
					}
					symad.notifyDataSetChanged();
					symlist.invalidate();
				}
			});
		rsyms.addView(symsearch, width, height / 15);
		addCardView(0, "符号表", rsyms);


		RelativeLayout rHex=new RelativeLayout(this);
		vhex = new HexView(this, width, height - height / 10 - height / 15, dumper);
		rHex.addView(vhex, width, height - height / 10 - height / 15);
		addCardView(1, "HEX视图", rHex);

		RelativeLayout rAIDA=new RelativeLayout(this);
		AIDAView vAIDA=new AIDAView(this);
		rAIDA.addView(vAIDA, width, height - height / 10 - height / 15);
		addCardView(2, "AIDA视图", rAIDA);

	

		setCardView(0);
	}

	private void addCardView(int id, String name, RelativeLayout v)
	{
		FloatingActionButton bf=new FloatingActionButton(this);
		bf.setBackgroundColor(0xff1e88e5);
		bf.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					int index=0;
					for (int i=0;i < bfmap.size();++i)
					{
						if (bfmap.get(i) == p1)
						{index = i;}
					}
					setCardView(index);
				}
			});
		ll.addView(bf, width / 3, height / 15);
		v.setX(-width);
		v.setY(height / 10 + height / 15);
		mainlayout.addView(v, width, height - height / 10 - height / 15);
		vmap.put(id, v);
		bfmap.put(id, bf);
	}

	private void setCardView(int id)
	{
		for (int i=0;i < bfmap.size();++i)
		{
			bfmap.get(i).setBackgroundColor(0xff1e88e5);
		}
		bfmap.get(id).setBackgroundColor(0xff5555ff);
		for (int i=0;i < vmap.size();i++)
		{
			vmap.get(i).setX(-width);
		}
		vmap.get(id).setX(0);
	}

	private class SymbolAdapter extends BaseAdapter
	{
		Context con;
		Vector<Integer>showing;

		SymbolAdapter(Context con)
		{
			this.con = con;
			showing = new Vector<Integer>();
		}

		@Override
		public int getCount()
		{
			return showing.size();
		}

		@Override
		public Object getItem(int p1)
		{
			return loadedSymbols.get(showing.get(p1));
		}

		@Override
		public long getItemId(int p1)
		{
			return showing.get(p1);
		}

		@Override
		public View getView(int p1, View p2, ViewGroup p3)
		{
			LinearLayout ml=new LinearLayout(con);
			Symbol sym=loadedSymbols.get(showing.get(p1));
			LinearLayout ml2=new LinearLayout(con);
			ml2.setOrientation(1);
			TextView index=new TextView(con);
			index.setText(showing.get(p1) + "");
			index.setTextSize(10);
			index.setTextColor(Color.GRAY);
			index.setGravity(Gravity.CENTER);
			TextView name=new TextView(con);
			name.setText(sym.demangledName);
			name.setTextSize(20);
			TextView info=new TextView(con);
			info.setText("bind:" + Tables.symbol_bind.get(sym.bind) + "  类型:" + Tables.symbol_type.get(sym.type) + "  值:" + Utils.i2hex(sym.value) + "  大小:" + sym.size);
			info.setTextSize(10);
			info.setTextColor(Color.GRAY);
			ImageView iv=new ImageView(con);
			if (sym.type == 1)
			{
				iv.setImageResource(R.drawable.ic_cube_outline);
			}
			if (sym.type == 2)
			{
				iv.setImageResource(R.drawable.ic_cube);
			}
			ml.addView(index, 50, 50);
			ml.addView(iv, 50, 50);
			ml.addView(ml2);
			ml2.addView(name);
			ml2.addView(info);
			return ml;
		}
	}
	
	
	public static void startThisActivity(Context context, String path)
	{
		Intent intent = new Intent(context, ELFViewerActivity.class);
		Bundle extras = new Bundle();
		extras.putString(TAG_FILE_PATH, path);
		intent.putExtras(extras);
		context.startActivity(intent);
	}
	
	public static void startThisActivity(Context context, File path)
	{
		startThisActivity(context, path.getPath());
	}
}
