package com.eemc.aida.views;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.eemc.aida.elf.*;
import com.eemc.aida.*;

public class HexView extends RelativeLayout
{
	dump dumper;
	int w,h;
	int line;
	View bar;
	boolean chose=false;
	int choseblockaddr,choseblocksize;
	
	public TextView[] addrs=new TextView[20];
	public TextView[] tvs=new TextView[160];
	
	public HexView(Context con,int width,int height,dump d){
		super(con);
		dumper=d;
		w=width;
		h=height;
		
		bar=new View(con);
		bar.setBackgroundColor(Color.GRAY);
		bar.setX(w-w/16);
		
		for(int i=0;i<20;i++){
			addrs[i]=new TextView(con);
			addrs[i].setX(0);
			addrs[i].setY(i*(height/20));
			addrs[i].setTextColor(0xffaeda00);
			addrs[i].setGravity(Gravity.RIGHT|Gravity.CENTER);
			addView(addrs[i],w/2-20,h/20);
		}
		
		int w2=width/2;
		for(int ix=0;ix<8;ix++){
			for(int iy=0;iy<20;iy++){
				tvs[ix+iy*8]=new TextView(con);
				TextView tv=tvs[ix+iy*8];
				tv.setX(width/2+ix*(w2/8));
				tv.setY(iy*(height/20));
				tv.setGravity(Gravity.CENTER);
				tv.setTextColor(0xff80a000);
				addView(tv,w2/8,h/20);
			}
		}
		addView(bar,w/16,h/20);
		
		scrollToLine(0);
	}
	
	public void scrollToLine(int l){
		line=l;
		
		for(int i=0;i<20;i++){
			
			int addr=(i+line)*8;
			String sn="";
			for(section sec:dumper.elf.sections){
				if(sec.offset<=addr&&sec.offset+sec.size>addr){
					sn=sec.name+":";
					break;
				}
			}
			
			addrs[i].setText(sn+Utils.i2hex(addr));
		}
		
		for(int ix=0;ix<8;ix++){
			for(int iy=0;iy<20;iy++){
				TextView tv=tvs[ix+iy*8];
				tv.setText(Utils.b2hex(dumper.bs[ix+(iy+line)*8]));
				if(chose){
					for(int i=choseblockaddr;i<choseblockaddr+choseblocksize;i++){
						if(i==ix+(iy+line)*8){
							tv.setBackgroundColor(0xffdedeff);
							break;
						}else{
							tv.setBackgroundColor(0x00000000);
						}
					}
				}
			}
		}
	}
	
	public void setChoose(int addr,int size){
		chose=true;
		choseblockaddr=addr;
		choseblocksize=size;
	}
	
	float clickX,clickY,bardy;
	public int memLine;
	boolean barclicked=false;
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float ex=event.getX();
		float ey=event.getY();
		float dy=ey-clickY;
		int ac=event.getAction();
		
		if(ac==MotionEvent.ACTION_DOWN){
			clickX=ex;
			clickY=ey;
			if(ex>bar.getX()&&ex<bar.getX()+bar.getWidth()&&ey>bar.getY()&&ey<bar.getY()+bar.getHeight()){
				barclicked=true;
				bardy=bar.getY()-ey;
				bar.setBackgroundColor(0xff5555ff);
			}
		}else if(ac==MotionEvent.ACTION_MOVE){
			if(barclicked){
				int thisline=(int)(ey+bardy)*((dumper.bs.length/8)/(h-h/20));
				if(ey+bardy>=0&&ey+bardy<=h-h/20&&Math.abs(thisline-line)>=1){
					bar.setY(ey+bardy);
					scrollToLine(thisline);
				}
				}else{
				int thisline=(int)-dy/40+memLine;
				if(thisline>=0&&dumper.bs.length/8-20>=thisline){
					if(Math.abs(thisline-line)>=1)scrollToLine(thisline);
				}//bar.setY((h-h/20)*(thisline/(dumper.bs.length/8)));
				}
		}else if(ac==MotionEvent.ACTION_UP){
			clickX=0;
			clickY=0;
			memLine+=(int)-dy/40;
			if(memLine<0){memLine=0;}
			if(barclicked){
				bar.setBackgroundColor(Color.GRAY);
				memLine=(int)(ey+bardy)*((dumper.bs.length/8)/(h-h/20));
				barclicked=false;
				bardy=0;
			}
		}
		
		return true;
	}
}
