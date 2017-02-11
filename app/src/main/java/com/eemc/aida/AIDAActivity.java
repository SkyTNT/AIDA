package com.eemc.aida;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.eemc.aida.elf.*;
import com.eemc.aida.views.*;
import com.gc.materialdesign.views.*;
import com.gc.materialdesign.widgets.*;
import com.gc.materialdesign.widgets.Dialog;
import java.util.*;
import android.text.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.support.v7.internal.view.menu.*;

public class AIDAActivity extends Activity
{
	String path;
	RelativeLayout mainlayout;
	LinearLayout ll;
	ProgressBarCircularIndeterminate pb;
	ButtonFlat showmenu;
	PopupMenu menu;
	ListView symlist;
	EditText symsearch;
	HexView vhex;
	SymbolAdapter symad;
	Activity self=this;
	int width,height,sbheight;
	dump dumper;
	Vector<symbol>syms=new Vector<symbol>();
	int symnum;
	HashMap<Integer,RelativeLayout>vmap=new HashMap<Integer,RelativeLayout>();
	HashMap<Integer,ButtonFlat>bfmap=new HashMap<Integer,ButtonFlat>();
	MyHandler mhandler=new MyHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		path=this.getIntent().getExtras().getString("path");
		setTitle(path);
		dumper=new dump(path);
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
		Toolbar tb=new Toolbar(this);
		//tb.setLogo(R.drawable.ic_launcher);
		tb.setTitle("AIDA");
		tb.setSubtitle(path.substring(path.lastIndexOf("/")+1));
		tb.setTitleTextColor(Color.WHITE);
		tb.setBackgroundColor(0xff1e88e5);
		mainlayout.addView(tb,width,height/10);
		
		pb=new ProgressBarCircularIndeterminate(this);
		pb.setX(width-(2*height)/15-20);
		pb.setY(height/20-height/30);
		pb.setBackgroundColor(0xff5555ff);
		mainlayout.addView(pb,height/15,height/15);
		
		showmenu=new ButtonFlat(this);
		showmenu.setX(width-height/15-10);
		showmenu.setY(height/20-height/30);
		showmenu.setBackgroundColor(0xff1e88e5);
		showmenu.setBackgroundResource(R.drawable.menu);
		mainlayout.addView(showmenu,height/15,height/15);
		menu=new PopupMenu(self,showmenu);
		showmenu.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					Menu m=menu.getMenu();
					m.clear();
					m.add(0,0,0,"搜索");
					m.add(0,1,0,"跳转");
					menu.show();
				}
			});
		menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
				@Override
				public boolean onMenuItemClick(MenuItem p1)
				{
					switch(p1.getItemId()){
						case 0:
							AlertDialog.Builder d=new AlertDialog.Builder(self);
							final EditText kw=new EditText(self);
							kw.setHint("关键字");
							d.setTitle("搜索").setView(kw).setCancelable(false).setNegativeButton("确定", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										setCardView(0);
										symad.showing.clear();
										for(int i=0;i<symnum;i++){
											if(syms.get(i).demangledname.contains(kw.getText().toString())){
												symad.showing.add(i);
											}
										}
										symad.notifyDataSetChanged();
									}
								});
							d.create().show();
							break;
						default:break;
					}
					return false;
				}
			});
		
		HorizontalScrollView hsv=new HorizontalScrollView(this);
		hsv.setY(height/10);
		hsv.setBackgroundColor(0xff1e88e5);
		ll=new LinearLayout(this);
		hsv.addView(ll);
		mainlayout.addView(hsv,width,height/15);
		initCards();
		initSyms();
	}
	
	void initCards(){
		RelativeLayout rsyms=new RelativeLayout(this);
		symlist=new ListView(this);
		symlist.setFastScrollEnabled(true);
		symlist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					symbol sym=syms.get(symad.showing.get(p3));
					if(sym.type==2){
						int addr=sym.value;
						int s=sym.size;
						if(s==0){
							s=1;
						}
					vhex.setChoose(addr,s);
					vhex.scrollToLine(addr/8);
					vhex.memLine=addr/8;
					byte[] basecode=Utils.cp(dumper.bs,sym.value-1,sym.size);
					String codes="";
					for(int i=0;i<sym.size/2;i++){
						byte[]co=Utils.cp(basecode,i*2,2);
						codes+=Utils.disassemble(0,Utils.b2i(co))+"\n";
					}
					AlertDialog.Builder d=new AlertDialog.Builder(self);
					d.setMessage("全名:\n"+sym.name+"汇编:\n"+codes);
					d.setPositiveButton("转到16进制视图",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1,int p2){
									setCardView(1);
									// TODO: Implement this method
								}
							});
					d.show();
					}
				}
			});
		symad=new SymbolAdapter(this);
		symlist.setAdapter(symad);
		rsyms.addView(symlist,width,height-height/10-height/15);
		symsearch=new EditText(this);
		symsearch.setHint("搜索");
		symsearch.setY(-height/15);
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
					for(int i=0;i<symnum;i++){
						if(syms.get(i).demangledname.contains(p1.toString())){
							symad.showing.add(i);
						}
					}
					symad.notifyDataSetChanged();
					symlist.invalidate();
				}
			});
		rsyms.addView(symsearch,width,height/15);
		addCardView(0,"符号表",rsyms);
		
		
		RelativeLayout rHex=new RelativeLayout(this);
		vhex=new HexView(this,width,height-height/10-height/15,dumper);
		rHex.addView(vhex,width,height-height/10-height/15);
		addCardView(1,"HEX视图",rHex);
		
		RelativeLayout rAIDA=new RelativeLayout(this);
		AIDAView vAIDA=new AIDAView(this);
		rAIDA.addView(vAIDA,width,height-height/10-height/15);
		addCardView(2,"AIDA视图",rAIDA);
		
		
		setCardView(0);
	}
	
	void initSyms(){
		for(section sec:dumper.elf.sections){
			if(sec.type==2||sec.type==11){
				symnum+=dumper.getSymNum(sec);
			}
		}
		
		Thread loadsyms=new Thread(new Runnable(){
				@Override
				public void run()
				{
					for(section sec:dumper.elf.sections){
						if(sec.type==2||sec.type==11){
							for(int i=0;i<dumper.getSymNum(sec);++i){
								Message msg=new Message();
								msg.what=0;
								msg.arg1=i;
								msg.obj=dumper.getSym(sec,i);
								mhandler.sendMessage(msg);
							}
						}
					}
					Message msg=new Message();
					msg.what=1;
					mhandler.sendMessage(msg);
				}
			});
			loadsyms.start();
	}
	
	void addCardView(int id,String name,RelativeLayout v){
		ButtonFlat bf=new ButtonFlat(this);
		bf.setText(name);
		bf.setRippleSpeed(30);
		bf.setBackgroundColor(0xff1e88e5);
		bf.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					int index=0;
					for(int i=0;i<bfmap.size();i++){
						if(bfmap.get(i)==p1){index=i;}
					}
					setCardView(index);
				}
			});
		ll.addView(bf,width/3,height/15);
		v.setX(-width);
		v.setY(height/10+height/15);
		mainlayout.addView(v,width,height-height/10-height/15);
		vmap.put(id,v);
		bfmap.put(id,bf);
	}
	
	void setCardView(int id){
		for(int i=0;i<bfmap.size();i++){
			bfmap.get(i).setBackgroundColor(0xff1e88e5);
		}
		bfmap.get(id).setBackgroundColor(0xff5555ff);
		for(int i=0;i<vmap.size();i++){
			vmap.get(i).setX(-width);
		}
		vmap.get(id).setX(0);
	}
	
	class SymbolAdapter extends BaseAdapter
	{
		Context con;
		Vector<Integer>showing;
		
		SymbolAdapter(Context con){
			this.con=con;
			showing=new Vector<Integer>();
		}

		@Override
		public int getCount()
		{
			return showing.size();
		}

		@Override
		public Object getItem(int p1)
		{
			return syms.get(showing.get(p1));
		}

		@Override
		public long getItemId(int p1)
		{
			return showing.get(p1);
		}
		
		void addSym(symbol sym){
			syms.add(sym);
			symlist.invalidate();
		}
		
		@Override
		public View getView(int p1, View p2, ViewGroup p3)
		{
			LinearLayout ml=new LinearLayout(con);
				symbol sym=syms.get(showing.get(p1));
				LinearLayout ml2=new LinearLayout(con);
				ml2.setOrientation(1);
				TextView index=new TextView(con);
				index.setText(showing.get(p1)+"");
				index.setTextSize(10);
				index.setTextColor(Color.GRAY);
				index.setGravity(Gravity.CENTER);
				TextView name=new TextView(con);
				name.setText(sym.demangledname);
				name.setTextSize(20);
				TextView info=new TextView(con);
				info.setText("bind:"+Tables.symbol_bind.get(sym.bind)+"  类型:"+Tables.symbol_type.get(sym.type)+"  值:"+Utils.i2hex(sym.value)+"  大小:"+sym.size);
				info.setTextSize(10);
				info.setTextColor(Color.GRAY);
				ImageView iv=new ImageView(con);
				if(sym.type==1){
					iv.setImageResource(R.drawable.obj);
				}
				if(sym.type==2){
					iv.setImageResource(R.drawable.func);
				}
				ml.addView(index,50,50);
				ml.addView(iv,50,50);
				ml.addView(ml2);
				ml2.addView(name);
				ml2.addView(info);
			return ml;
		}
	}
	
	class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			int what=msg.what;
			if(what==0){
				symad.addSym((symbol)(msg.obj));
				symad.showing.add(msg.arg1);
				bfmap.get(0).setText("符号表\n("+msg.arg1+"/"+symnum+")");
			}
			if(what==1){
				pb.setY(-height/15);
				bfmap.get(0).setText("符号表");
				symlist.getLayoutParams().height=height-height/10-(height*2)/15;
				symlist.setY(height/15);
				symsearch.setY(0);
				SnackBar m=new SnackBar(self,"Symbol加载完成");
				m.show();
			}
		}
	}
}
