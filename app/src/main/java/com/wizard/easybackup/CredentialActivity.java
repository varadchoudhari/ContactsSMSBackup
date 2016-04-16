package com.wizard.easybackup;


import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class CredentialActivity extends Activity
{
	SharedPreferences pref;
	String uname,pass,store;
	EditText ufld,pfld;
	CheckBox cb;
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credential);
		pref=this.getSharedPreferences("cred", CredentialActivity.MODE_PRIVATE);
		uname=pref.getString("username", "");
		pass=pref.getString("password","");
		store=pref.getString("storage","local");
		cb=(CheckBox)this.findViewById(R.id.cloudtoggle);
		if(store.equals("local"))
		{
			cb.setChecked(false);
		}
		else if(store.equals("cloud"))
		{
			cb.setChecked(true);
		}
		ufld=(EditText)this.findViewById(R.id.unamefld);
		ufld.setText(uname);
		pfld=(EditText)this.findViewById(R.id.passfld1);
		pfld.setText(pass);
	}

	public void onButClick(View v)
	{
		if(v==this.findViewById(R.id.credcanbut))
		{
			this.finish();
		}
		else if(v==this.findViewById(R.id.credokbut))
		{
			  SharedPreferences.Editor e=pref.edit();
			  uname=ufld.getText().toString();
			  pass=pfld.getText().toString();
			  if(cb.isChecked())
			  {
				  store="cloud";
			  }
			  else if(!cb.isChecked())
			  {
				  store="local";
			  }
			  e.putString("storage", store);
			  e.putString("username", uname);
    		  e.putString("password",pass);
    		  e.commit();
    		  this.finish();
		}
	}
	public boolean onCreateOptionsMenu(Menu menu)
	{
		//getMenuInflater().inflate(R.menu.credential, menu);
		return true;
	}

}
