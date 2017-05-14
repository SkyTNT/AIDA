package com.eemc.aida;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;
import com.eemc.aida.elf.*;
import java.util.regex.*;

public class ASMCodeActivity extends AppCompatActivity
{
	public static final String TAG_ASM_CODE = "asm_code";
	public static final String TAG_SYMBOL_NAME = "symbol_name";
	static String regnames[]=  { "a1", "a2", "a3", "a4","r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15","sl",  "fp",  "ip",  "sp",  "lr",  "pc", "WR", "SB", "SL",  "FP",  "IP",  "SP",  "LR",  "PC"};
	SpannableString ss;
	AppCompatTextView tv;

	Handler myhandler=new Handler(){@Override
		public void handleMessage(Message msg)
		{
			tv.setText(ss);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asm_code);

		tv= (AppCompatTextView)findViewById(R.id.asm_code_text_view_codes);
		tv.setTextColor(Color.BLUE);
		Thread initHL=new Thread(new Runnable(){
				@Override
				public void run()
				{
					ss=new SpannableString(getIntent().getExtras().getString(TAG_ASM_CODE));
					myhandler.sendEmptyMessage(0);
					for(String regname:regnames){
						setTextHighlight(ss,"[(, )\\{\\[]"+regname,Color.GREEN,true);
						setTextHighlight(ss,regname+"[,\\}\\]\\!]",Color.GREEN,true);
					}

					setTextHighlight(ss,"<.*>",0xffff00ff,true);
					setTextHighlight(ss,"0x[0123456789abcdef]*",0xff80a000,true);
					setTextHighlight(ss,"#[0123456789+-]*",0xff80a000,true);
					setTextHighlight(ss,"\\{",0xff9999ff,true);
					setTextHighlight(ss,"\\}",0xff9999ff,true);
					setTextHighlight(ss,"\\(",0xff00ffff,true);
					setTextHighlight(ss,"\\)",0xff00ffff,true);
					setTextHighlight(ss,"\\[",0xff9999ff,true);
					setTextHighlight(ss,"\\]",0xff9999ff,true);
					setTextHighlight(ss,"\\:",0xffaeda00,true);	
					setTextHighlight(ss,"\\+",0xffaeda00,true);	
					setTextHighlight(ss,"\\-",0xffaeda00,true);	
					setTextHighlight(ss,"\\!",0xffaeda00,true);	
					setTextHighlight(ss,"\\&",0xffaeda00,true);	
					setTextHighlight(ss,"\\*",0xffaeda00,true);	
					myhandler.sendEmptyMessage(0);
				}
			});
		initHL.start();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setSubtitle(getIntent().getExtras().getString(TAG_SYMBOL_NAME));
	}
	
	public void setTextHighlight(SpannableString ss, String matchStr,int color,boolean isbold) {
		int spanStart = 0;
		int spanEnd = 0;
		if (ss.toString()!= null && matchStr != null) {
			Pattern p = Pattern.compile(matchStr);
			Matcher m = p.matcher(ss);
			while (m.find()) {
				spanStart = m.start();
				spanEnd = m.end();
				ss.setSpan(new ForegroundColorSpan(color), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				if(isbold){
					ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),spanStart,spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
			finish();
		return super.onOptionsItemSelected(item);
	}

	public static void startThisActivity(Context context, String asmCodes, Symbol symbol)
	{
		Intent intent = new Intent(context, ASMCodeActivity.class);
		Bundle extras = new Bundle();
		extras.putString(TAG_ASM_CODE, asmCodes);
		extras.putString(TAG_SYMBOL_NAME, symbol.name);
		intent.putExtras(extras);
		context.startActivity(intent);
	}
}
