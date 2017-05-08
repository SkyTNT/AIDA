package com.eemc.aida;
import android.support.v7.app.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.content.*;
import com.eemc.aida.elf.*;

public class ASMCodeActivity extends AppCompatActivity
{
	public static final String TAG_ASM_CODE = "asm_code";
	public static final String TAG_SYMBOL_NAME = "symbol_name";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asm_code);
		
		AppCompatTextView textViewCodes = (AppCompatTextView)findViewById(R.id.asm_code_text_view_codes);
		textViewCodes.setText(getIntent().getExtras().getString(TAG_ASM_CODE));
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setSubtitle(getIntent().getExtras().getString(TAG_SYMBOL_NAME));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId()==android.R.id.home)
			finish();
		return super.onOptionsItemSelected(item);
	}
	
	public static void startThisActivity(Context context,String asmCodes,Symbol symbol)
	{
		Intent intent = new Intent(context,ASMCodeActivity.class);
		Bundle extras = new Bundle();
		extras.putString(TAG_ASM_CODE,asmCodes);
		extras.putString(TAG_SYMBOL_NAME,symbol.name);
		intent.putExtras(extras);
		context.startActivity(intent);
	}
}
