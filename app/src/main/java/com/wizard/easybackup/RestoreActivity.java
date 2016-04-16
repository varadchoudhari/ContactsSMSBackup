package com.wizard.easybackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RestoreActivity extends Activity implements OnItemClickListener,
		FilenameFilter {

	File appdir, datadir;
	Calendar cal;
	FileOutputStream log;
	ListView lv;
	ArrayAdapter<String> ad;
	String logmsg;
	int opt;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore);
		appdir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/" + this.getString(R.string.app_name));
		lv = (ListView) this.findViewById(R.id.filelist);
		lv.setOnItemClickListener(this);
		opt = this.getIntent().getIntExtra("opt", 0);
		if (opt == 1)
			datadir = new File(appdir + "/Contacts");
		else if (opt == 2)
			datadir = new File(appdir + "/Sms");

		if (datadir.isDirectory()) {
			ad = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, datadir.list(this));
			lv.setAdapter(ad);
		} else {
			Toast.makeText(this, "Backup Folder not Found!", Toast.LENGTH_LONG)
					.show();
			this.finish();
		}
	}

	public void onItemClick(AdapterView<?> ad, View v, int pos, long id) {
		String filename = ((TextView) v).getText().toString();
		if (opt == 1)
			resContact(filename);
		if (opt == 2) {
			resSms(filename);
		}

	}

	public void resContact(String filename) {
		try {
			File f = new File(datadir + "/" + filename);
			FileInputStream istrm = new FileInputStream(f);
			if ((int) f.length() <= 0) {
				Toast.makeText(this, "file empty/does not exit",
						Toast.LENGTH_LONG).show();
				return;
			}
			parseContacts(istrm, filename);
		} catch (XmlPullParserException e) {
			Toast.makeText(this, "XmlPullParserException!", Toast.LENGTH_LONG)
					.show();
			this.finish();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Error:Could not find backup file!",
					Toast.LENGTH_LONG).show();
			this.finish();
		} catch (IOException e) {
			Toast.makeText(this, "Error:Could not read from backup file!",
					Toast.LENGTH_LONG).show();
			this.finish();
		}
		this.finish();
	}

	private void parseContacts(FileInputStream strm, String filename)
			throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder op;
		String name, str1 = "", str2 = "";
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(strm, null);
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			// name = "";
			// str1 = "";
			// str2 = "";
			op = null;
			switch (event) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if (name.equals("contactfile"))
					break;
				if (name.equals("contact"))
					break;
				if (name.equals("accname")) {
					str1 = parser.nextText();
					if (str1.equals("null")) {
						Log.i("", "MYTAG here");
						str1 = null;
					}
					break;
				}
				if (name.equals("acctype")) {
					str2 = parser.nextText();
					if (str2.equals("null"))
						str2 = null;
					op = ContentProviderOperation
							.newInsert(ContactsContract.RawContacts.CONTENT_URI)
							.withValue(
									ContactsContract.RawContacts.ACCOUNT_NAME,
									str1)
							.withValue(
									ContactsContract.RawContacts.ACCOUNT_TYPE,
									str2);
					Log.i("", "MYTAG str1=" + str1 + " str2=" + str2);
					ops.add(op.build());
					break;
				}
				if (name.equals("name")) {
					op = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.Data.DATA1,
									parser.nextText());
					ops.add(op.build());
					break;
				}
				if (name.equals("phone")) {
					op = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.Data.DATA1,
									parser.nextText());
					ops.add(op.build());
					break;
				}
				if (name.equals("note")) {
					op = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.Data.DATA1,
									parser.nextText());
					ops.add(op.build());
					break;
				}
				if (name.equals("email")) {
					op = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.Data.DATA1,
									parser.nextText());
					ops.add(op.build());
					break;
				}
				if (name.equals("org")) {
					op = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.Data.DATA1,
									parser.nextText());
					ops.add(op.build());
					break;
				}
				if (name.equals("address")) {
					op = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.Data.DATA1,
									parser.nextText());
					ops.add(op.build());
					break;
				}

			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (name.equals("contact")) {
					try {
						Log.i("", "MYTAG ops=" + ops.toString());
						this.getContentResolver().applyBatch(
								ContactsContract.AUTHORITY, ops);

						ops.clear();
					} catch (RemoteException e) {
						Toast.makeText(this, "RemoteException",
								Toast.LENGTH_SHORT).show();
						return;// this.finish();
					} catch (OperationApplicationException e) {
						Toast.makeText(this, "OperationApplicationException",
								Toast.LENGTH_SHORT).show();
						return;// this.finish();
					}
					break;
				}
			}
			event = parser.next();
		}
		log = new FileOutputStream(appdir + "/" + "log.txt", true);
		cal = Calendar.getInstance();
		logmsg = cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1)
				+ "." + cal.get(Calendar.DAY_OF_MONTH) + "/"
				+ cal.get(Calendar.HOUR_OF_DAY) + "."
				+ cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND);
		logmsg = logmsg + "____" + this.getString(R.string.Restore) + "____"
				+ this.getString(R.string.Contacts) + "____" + filename;
		log.write((logmsg + "\n\n").getBytes());
		log.close();
		Toast.makeText(this, "Contacts restored successfully!",
				Toast.LENGTH_LONG).show();
	}

	public void resSms(String filename) {
		try {
			File f = new File(datadir + "/" + filename);

			FileInputStream istrm = new FileInputStream(f);
			if ((int) f.length() <= 0) {
				Toast.makeText(this, "file empty/does not exit",
						Toast.LENGTH_LONG).show();
				return;// this.finish();
			}
			parseSms(istrm, filename);
		} catch (XmlPullParserException e) {
			Toast.makeText(this, "XmlPullParserException", Toast.LENGTH_LONG)
					.show();
			this.finish();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Error:Could not find backup file!",
					Toast.LENGTH_LONG).show();
			this.finish();
		} catch (IOException e) {
			Toast.makeText(this, "Error:Could not read from backup file!",
					Toast.LENGTH_LONG).show();
			this.finish();
		}
		this.finish();
	}

	private void parseSms(FileInputStream strm, String filename)
			throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		ContentValues cv = null;
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(strm, null);
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			String name = "";
			switch (event) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if (name.equals("smsfile"))
					break;
				if (name.equals("sms")) {
					cv = new ContentValues();
				} else if (cv != null) {
					cv.put(name, parser.nextText());
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (name.equals("sms") && cv != null) {
					String sel = "address =? and body=? and date=?";
					String[] selarr = { cv.getAsString("address"),
							cv.getAsString("body"), cv.getAsString("date") };
					Cursor curs = getContentResolver().query(
							android.net.Uri.parse("content://sms"),
							new String[] { "_id" }, sel, selarr, null);
					if (curs.getCount() == 0)
						this.getContentResolver().insert(
								android.net.Uri.parse("content://sms"), cv);
				}
			}
			event = parser.next();
		}
		log = new FileOutputStream(appdir + "/" + "log.txt", true);
		cal = Calendar.getInstance();
		logmsg = cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1)
				+ "." + cal.get(Calendar.DAY_OF_MONTH) + "/"
				+ cal.get(Calendar.HOUR_OF_DAY) + "."
				+ cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND);
		logmsg = logmsg + "____" + this.getString(R.string.Restore) + "____"
				+ this.getString(R.string.Sms) + "____" + filename;
		log.write((logmsg + "\n\n").getBytes());
		log.close();
		Toast.makeText(this, "Sms restored successfully!", Toast.LENGTH_LONG)
				.show();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.restore, menu);
		return true;
	}

	public boolean accept(File dir, String filename) {
		if (filename.endsWith(".xml"))
			return true;
		else
			return false;
	}

}
