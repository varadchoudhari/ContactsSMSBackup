package com.wizard.easybackup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.app.Activity;
import android.database.Cursor;
import android.view.Menu;
import android.widget.Toast;

public class BackupActivity extends Activity 
{
	File appdir,cntctdir,smsdir;
	FileOutputStream ostrm,log;
	Calendar cal;
	String fname,logmsg;
	Cursor dataC,rawC;
	int count;
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		int opt=this.getIntent().getIntExtra("opt", 0);
		appdir = new File(Environment.getExternalStorageDirectory().getPath()+"/"+this.getString(R.string.app_name));
		if(!appdir.isDirectory()) 	appdir.mkdir();		   
		if(opt==1)
			backContacts();
		if(opt==2)
			backSms();
		this.finish();
	}
	public void backContacts()
	{
		cntctdir=new File(appdir+"/Contacts");
		if(!cntctdir.isDirectory())
					cntctdir.mkdir();		 
		String[] rawProj,dataProj,dataArgs;
		String rawSort,rawSelect,dataSort,dataSelect;
		rawProj=new String[]{ContactsContract.RawContacts._ID,ContactsContract.RawContacts.ACCOUNT_NAME,ContactsContract.RawContacts.ACCOUNT_TYPE};
		rawSort=ContactsContract.RawContacts._ID+" ASC";
		rawSelect=ContactsContract.RawContacts.DELETED+"!=1";
		rawC=this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, rawProj, rawSelect, null, rawSort);
		if((count=rawC.getCount())==0)
		{
			Toast.makeText(this,"No Data found to be backed up!",Toast.LENGTH_LONG).show();
			return;//this.finish();
		}
		try 
		{
			fname=getFname();
			String tag1,tag2,type;
			ostrm=new FileOutputStream(cntctdir.getAbsolutePath()+"/"+fname+".xml");
			ostrm.write(getString(R.string.xmlhead).getBytes());
			ostrm.write("<contactfile>\n".getBytes());
			while(rawC.moveToNext())
			{
				ostrm.write("<contact>\n".getBytes());
				ostrm.write(("<accname>"+rawC.getString(1)+"</accname>"+"<acctype>"+rawC.getString(2)+"</acctype>\n").getBytes());
				dataProj=new String[]{ContactsContract.Data._ID,ContactsContract.Data.MIMETYPE,ContactsContract.Data.DATA1};
				dataSelect=ContactsContract.Data.RAW_CONTACT_ID+"=? and "+ContactsContract.Data.DATA1+"!=?";
				dataArgs=new String[]{String.valueOf(rawC.getInt(0)),"null"};
				dataSort=ContactsContract.Data._ID+" ASC";
				dataC=this.getContentResolver().query(ContactsContract.Data.CONTENT_URI, dataProj, dataSelect, dataArgs, dataSort);
				while(dataC.moveToNext())
				{
					type=dataC.getString(1);
					tag1="<other>";tag2="</other>";
					if(type.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
					{
						tag1="<phone>";tag2="</phone>";
					}
					else if(type.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
					{
						tag1="<name>";tag2="</name>";
					}
					else if(type.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE))
					{
						tag1="<email>";tag2="</email>";
					}
					else if(type.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE))
					{
						tag1="<note>";tag2="</note>";
					}
					else if(type.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE))
					{
						tag1="<org>";tag2="</org>";
					}
					else if(type.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE))
					{
						tag1="<address>";tag2="</address>";
					}
					ostrm.write((tag1+dataC.getString(2)+tag2+"\n").getBytes());
				}
				ostrm.write("</contact>\n".getBytes());
			}
			ostrm.write("</contactfile>".getBytes()); ostrm.close();
			log=new FileOutputStream(appdir+"/"+"log.txt",true);
			logmsg=getFname()+"	____"+this.getString(R.string.Backup)+"____"+this.getString(R.string.Contacts)+"____"+count;
			log.write((logmsg+"\n\n").getBytes());
			log.close();
			Toast.makeText(this,"Contacts Backed up Successfully!",Toast.LENGTH_LONG).show();
		} 
		catch (FileNotFoundException e)
		{
			Toast.makeText(this,"Error:Could not create backup file!",Toast.LENGTH_LONG).show();
			this.finish();
		}
		catch (IOException e)
		{
			Toast.makeText(this,"Error:error while writing to file!",Toast.LENGTH_LONG).show();
			this.finish();
		}
	}
	public void backSms()
	{
		smsdir=new File(appdir+"/Sms");
			if(!smsdir.isDirectory())	smsdir.mkdir();		
		    fname=getFname();
			try
			{
				dataC=getContentResolver().query(android.net.Uri.parse("content://sms"), null, null, null, null);	
				if(dataC.getCount()==0)
				{
					Toast.makeText(this,"No Data found to be backed up!",Toast.LENGTH_LONG).show();
					return;//this.finish();
				}
				ostrm=new FileOutputStream(smsdir.getAbsolutePath()+"/"+fname+".xml");
				ostrm.write(getString(R.string.xmlhead).getBytes());
				ostrm.write("<smsfile>\n".getBytes());
				count=dataC.getColumnCount();
				while(dataC.moveToNext())
				{
					ostrm.write("<sms>\n".getBytes());
					for(int i=2;i<count;i++)
					{
						String str,col;
						col=dataC.getColumnName(i);
						ostrm.write(("<"+col+">").getBytes());
						try
						{
							str=dataC.getString(i);
							str=str.replace("&","&amp;");		str=str.replace("\"","&quot;");	str=str.replace("'","&apos;");		str=str.replace("<","&lt;");	str=str.replace(">","&gt;");			
							ostrm.write(str.getBytes());
							
						} 
						catch(NullPointerException e)	
						{
							ostrm.write("null".getBytes());
						}
						ostrm.write(("</"+col+">\n").getBytes());
					}
					ostrm.write("</sms>\n".getBytes());
				}
				ostrm.write("</smsfile>".getBytes()); ostrm.close();
				log=new FileOutputStream(appdir+"/"+"log.txt",true);
				logmsg=getFname()+"	____"+this.getString(R.string.Backup)+"____"+this.getString(R.string.Sms)+"____"+dataC.getCount();
				log.write((logmsg+"\n\n").getBytes());
				log.close();
				
				Toast.makeText(this,"Sms Backed up Successfully!",Toast.LENGTH_LONG).show();
			} 
			catch (FileNotFoundException e)
			{
				Toast.makeText(this,"Error:Could not create backup file!",Toast.LENGTH_LONG).show();
				this.finish();
			}
			catch (IOException e)
			{
				Toast.makeText(this,"Error:error while writing to file!",Toast.LENGTH_LONG).show();
				this.finish();
			}
				    
		    	
	}
	private String getFname()
	{
		 cal=Calendar.getInstance();
		 return cal.get(Calendar.YEAR)+"."+(cal.get(Calendar.MONTH)+1)+"."+cal.get(Calendar.DAY_OF_MONTH)+"_"+cal.get(Calendar.HOUR_OF_DAY)+"."+cal.get(Calendar.MINUTE)+"."+cal.get(Calendar.SECOND);
	}
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		//getMenuInflater().inflate(R.menu.backup, menu);
		return true;
	}
	
}
