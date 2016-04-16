package com.wizard.easybackup;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity 
{
	Intent i;
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void onButClick(View v)
	{
		int pos=0;
		if(((RadioButton)this.findViewById(R.id.contradio)).isChecked()) pos=1;
		if(((RadioButton)this.findViewById(R.id.smsradio)).isChecked()) pos=2;
		SharedPreferences pref=this.getSharedPreferences("cred", MainActivity.MODE_PRIVATE);
		if(pref.getString("storage", "local").equals("cloud"))
		{
			Toast.makeText(this,"Currently only Local support available",Toast.LENGTH_LONG).show();
			return;
		}
		if(pos==0)
			Toast.makeText(this,"Choose an option first",Toast.LENGTH_LONG).show();
		else if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			if(v==this.findViewById(R.id.backbut))
			{
					i = new Intent(this, BackupActivity.class);
					i.putExtra("opt", pos);
					this.startActivity(i);
			}
		
			else if(v==this.findViewById(R.id.resbut))
			{
			
					i = new Intent(this, RestoreActivityOld.class);
					i.putExtra("opt", pos);
					this.startActivity(i);
				
			}
		}
		else
		{
			Toast.makeText(this,"External (Local) Storage Not Mounted!",Toast.LENGTH_LONG).show();
		}
		
	}

public void getVCF()
{
	final String vfile = "ContactsRestore.vcf";
	ContentResolver cr = getContentResolver();
	Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
			null, null, null);
	phones.moveToFirst();
	String lookupKey = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
	Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
	AssetFileDescriptor fd;

	for(int i =0;i<phones.getCount();i++)
	{
	String value = String.valueOf(phones.getString(i));
	if(value != null) {
		try {
			fd = getApplicationContext().getContentResolver().openAssetFileDescriptor(uri, "r");
			FileInputStream fis = fd.createInputStream();
			byte[] buf = new byte[(int) fd.getDeclaredLength()];
			fis.read(buf);
			String VCard = new String(buf);
			String path = Environment.getExternalStorageDirectory().toString() + File.separator + "EasyBackup" + File.separator + vfile;
			FileOutputStream mFileOutputStream = new FileOutputStream(path, true);
			mFileOutputStream.write(VCard.toString().getBytes());
			phones.moveToNext();
			Log.d("Vcard", VCard);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	}

}
}
