package com.wizard.easybackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends Activity
{
	File f;
	FileInputStream strm;
	byte[] buf;
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		try 
		{
			f=new File(Environment.getExternalStorageDirectory().getPath()+"/"+this.getString(R.string.app_name)+"/log.txt");
			if(!f.isFile())
			{
				Toast.makeText(this,"Error:Not a regular file",Toast.LENGTH_LONG).show();
				this.finish();
			}
			strm=new FileInputStream(f);
			buf=new byte[(int) f.length()];
			strm.read(buf);
			TextView tv=(TextView)this.findViewById(R.id.logtext);
			tv.setText(new String(buf));
		}
		catch (FileNotFoundException e)
		{
			Toast.makeText(this,"Log File not found!",Toast.LENGTH_LONG).show();
			this.finish();
		}
		catch (IOException e)
		{
			Toast.makeText(this,"Error Reading Log File!",Toast.LENGTH_LONG).show();
			this.finish();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		//getMenuInflater().inflate(R.menu.log, menu);
		return true;
	}

}
