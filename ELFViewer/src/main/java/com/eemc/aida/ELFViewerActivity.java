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
	private HexView vhex;
	private Dumper dumper;
	private ListView listView;
	private Vector<Symbol> showingSymbols=new Vector<Symbol>();
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
		dumper = new Dumper(path);
		setTitle(path.substring(path.lastIndexOf("/") + 1));
		showingSymbols.addAll(loadedSymbols);
		listView = (ListView) findViewById(R.id.viewer_list_view);
		search(null);
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setSubtitle(path.substring(path.lastIndexOf("/") + 1));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
		if (item.getItemId() == android.R.id.home)
		{
			onBackPressed();
		}
		else if (item.getItemId() == R.id.action_find)
		{
			final EditText editText=new EditText(ELFViewerActivity.this);
			editText.setHint(R.string.abc_search_hint);
			new AlertDialog.Builder(ELFViewerActivity.this).
				setTitle(R.string.search_menu_title).setView(editText).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						search(editText.getText().toString());
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						p1.dismiss();
					}


				}).show();
		}
		return super.onOptionsItemSelected(item);
	}

	private void search(String keyWord)
	{
		showingSymbols.clear();

		if (keyWord == null || keyWord.isEmpty())
		{
			showingSymbols.addAll(loadedSymbols);
		}
		else
		{
			for (int i=0;i < loadedSymbols.size() ;++i)
			{
				if (loadedSymbols.get(i).demangledName.toLowerCase().contains(keyWord.toString().toLowerCase()))
				{
					showingSymbols.add(loadedSymbols.get(i));
				}
			}
		}

		listView.setAdapter(new SymbolAdapter());
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(new ListView.OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					final Symbol sym=showingSymbols.get(p3);
					if (sym.type == 2)
					{
						final int addr = sym.value;

						new AlertDialog.Builder(ELFViewerActivity.this).setTitle(R.string.viewer_choose_disassmebler).setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									p1.dismiss();
								}


							}).setItems(R.array.viewer_disassmebler, new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									switch (p2)
									{
										case 0:
											{
												String result=Objdump.dump(ELFViewerActivity.this, false, addr, sym.size + addr, path);
												ASMCodeActivity.startThisActivity(ELFViewerActivity.this, result, sym);
												break;
											}
										case 1:
											{
												byte[] basecode=Utils.cp(dumper.bs, sym.value - 1, sym.size);
												String codes=new String();
												for (int i=0;i < sym.size / 2;++i)
												{
													byte[]co = Utils.cp(basecode, i * 2, 2);
													codes += BIN2ASM.cast(ELFViewerActivity.this, 0, Utils.b2i(co)) + "\n";
												}
												ASMCodeActivity.startThisActivity(ELFViewerActivity.this, codes, sym);
												break;
											}
									}
								}


							}).show();

						/*
						 vhex.setChoose(addr, sym.size);
						 vhex.scrollToLine(addr / 8);
						 vhex.memLine = addr / 8;
						 */
					}
				}


			});
	}

	private class SymbolAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return showingSymbols.size();
		}

		@Override
		public Object getItem(int p1)
		{
			return showingSymbols.get(p1);
		}

		@Override
		public long getItemId(int p1)
		{
			return p1;
		}

		@Override
		public View getView(int p1, View p2, ViewGroup p3)
		{
			LinearLayout ml=new LinearLayout(ELFViewerActivity.this);
			Symbol sym=showingSymbols.get(p1);
			LinearLayout ml2=new LinearLayout(ELFViewerActivity.this);
			ml2.setOrientation(1);
			TextView index=new TextView(ELFViewerActivity.this);
			index.setText(p1 + "");
			index.setTextSize(10);
			index.setTextColor(Color.GRAY);
			index.setGravity(Gravity.CENTER);
			TextView name=new TextView(ELFViewerActivity.this);
			name.setText(sym.demangledName);
			name.setTextSize(20);
			TextView info=new TextView(ELFViewerActivity.this);
			info.setText("bind:" + Tables.symbol_bind.get(sym.bind) + "  类型:" + Tables.symbol_type.get(sym.type) + "  值:" + Utils.i2hex(sym.value) + "  大小:" + sym.size);
			info.setTextSize(10);
			info.setTextColor(Color.GRAY);
			ImageView iv=new ImageView(ELFViewerActivity.this);
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

	@Override
	public void onBackPressed()
	{
		new AlertDialog.Builder(ELFViewerActivity.this).setTitle(R.string.abc_action_bar_home_description).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
					ELFViewerActivity.this.finish();
				}


			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
				}


			}).show();
	}
}
