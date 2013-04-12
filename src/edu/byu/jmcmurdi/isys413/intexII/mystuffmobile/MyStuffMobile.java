package edu.byu.jmcmurdi.isys413.intexII.mystuffmobile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ArrayAdapter;

public class MyStuffMobile extends Activity {
	ViewFlipper vf = null;
	HttpClient client = null;
	private ArrayList<String> captionList = new ArrayList<String>();
	ListView lv = null;
	private String custid = null;
	ImageView iv = null;
	private int vfloginview = 0;
	private int vflistview = 0;
	private boolean vfsentinal = false;

	ArrayAdapter<String> adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystuffmobile);
		vf = (ViewFlipper) findViewById(R.id.vf);
		client = new DefaultHttpClient();
		lv = (ListView) findViewById(R.id.lv);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	}
	
	
	
	public void btnNewPicClick(View view) {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(this)));
		startActivityForResult(intent, TAKE_PHOTO_CODE);
	}

	private static final int TAKE_PHOTO_CODE = 1;

	/**
	 * This method will either create or pull the temporary file /sdcard/image.tmp
	 * 
	 * @param context
	 * @return
	 */
	private File getTempFile(Context context) {
		// it will return /sdcard/image.tmp
		final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		return new File(path, "image.tmp");
	}

	/**
	 * This method takes place after the image has been taken and runs the rest of the program calling other methods This method only calls other methods if the resultcode equals okay and the photo was successfully taken
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_CODE) {
			File file = getTempFile(this);
			try {
				Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(file));
				// ImageView viewer = (ImageView) findViewById(R.id.pictureViewer);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				// viewer.setImageBitmap(captureBmp);
				InputStream in = new FileInputStream(file);
				int count;
				byte[] buffer = new byte[512];
				while ((count = in.read(buffer)) >= 0) {
					out.write(buffer, 0, count);
				}
				String imagedata = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);

				PicturePosting postingCamera = new PicturePosting(imagedata);
				postingCamera.execute(imagedata);

				if (postingCamera.get().equals("failed")) {
					Toast.makeText(this, "Posting failed please try again later", Toast.LENGTH_LONG).show();
				} else {

					//testnotification(captureBmp);

					//Toast.makeText(this, "Balance updated see notification for details", Toast.LENGTH_LONG).show();
					// Toast.makeText(this, balance, Toast.LENGTH_LONG).show();
					Log.v("Success", postingCamera.get());
					refreshlist();
				}

			} catch (Exception e) {
				Toast.makeText(this, "Something went seriously wrong jim...", Toast.LENGTH_LONG).show();

				/*
				 * Jump to a new activity by using this: Intent intent = new Intent(this, NextActivity.class); intent.putExtra("myKey","myvalue"); StartActivity(intent);
				 */

			}

		}
	}

	/**
	 * This class is an asynchronous implementation of httppost and execute
	 * 
	 * @author Jedi Master Justin Paul McMurdie
	 * 
	 */
	// Android 4.1.1 requires all http commands to run as asynchronous tasks
	private class PicturePosting extends AsyncTask<String, Void, String> {
		public PicturePosting(String imagein) {
			imageString = imagein;
		}

		private String imageString = null;
		//String status = null;

		/**
		 * The portion of the asynchronous task that runs in the background
		 */
		protected String doInBackground(String... image) {
			try {

				
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://10.0.2.2:2020/MystuffWeb/edu.byu.isys413.jmcmurdi.actions.Postimage.action");

				// setting up the nameVaule pairs
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("imagedata", imageString));
				nameValuePairs.add(new BasicNameValuePair("ismobile", "true"));
				nameValuePairs.add(new BasicNameValuePair("custid", custid));
				//showToast(custid);
				nameValuePairs.add(new BasicNameValuePair("caption", "Dcc-" + System.currentTimeMillis()/3.14159/1000000000));
				nameValuePairs.add(new BasicNameValuePair("picname", "temppicname"));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = null;

				response = httpclient.execute(httppost);

				HttpEntity e = response.getEntity();
				//showToast("E = " + e.toString());

				JSONObject respobj = new JSONObject(EntityUtils.toString(e));


				String S_response = respobj.toString();

				return S_response;
			} catch (Exception e) {
				Log.v("tag", "The post to the server failed");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String test = sw.toString(); // stack trace as a string
				Log.v("why", test);
			}
			return "failed";
		} /* do in background */

	}

	
	
	
	

	public void loginbtnclick(View view) {
		try {
			if(vfsentinal == false){
				vfloginview = vf.getCurrentView().getId();
			}
			System.out.println("LoginButtonClicked");
			EditText passwordtext = (EditText) findViewById(R.id.password);
			EditText usernametext = (EditText) findViewById(R.id.username);

			String username = usernametext.getText().toString();

			String password = passwordtext.getText().toString();

			LoginPosting lposting = new LoginPosting(username, password);

			// Log.v("username", username);
			// Log.v("username", password);
			String temp = null;
			lposting.execute();

			if (lposting.get().equals("failed")) {
				showToast("Something went seriously wrong with posting the datas");
				//showToast(lposting.get());
			} else {
				temp = lposting.get();
				//showToast(temp);
				
				JSONObject myjson = null;
				try {
					myjson = new JSONObject(temp);
					String String_shouldbe_array = myjson.getString("piclist");
					// /showToast(String_shouldbe_array);
					Log.v("piclist", String_shouldbe_array);
					// JSONArray myjsonarray = (JSONArray)myjson;
					JSONArray myjsonarray = new JSONArray(String_shouldbe_array);
					for (int i = 0; i < myjsonarray.length(); i++) {
						JSONObject tempJSONobj = myjsonarray.getJSONObject(i);
						// showToast(tempJSONobj.get("caption").toString());
						captionList.add(tempJSONobj.get("caption").toString());
					}
					adapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_list_item_multiple_choice,
							captionList);
					lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					lv.setAdapter(adapter);

				} catch (JSONException e) {
					showToast("Problem converting the string to a JSON object or converting the string into a jsonarray");
					e.printStackTrace();
				}
			}

		} catch (InterruptedException e) {
			showToast("Interrupted Exception");
			e.printStackTrace();
		} catch (ExecutionException e) {
			showToast("ExecutionException");
			e.printStackTrace();
		}

		vf.setDisplayedChild(1);
		if(vfsentinal == false){
			vflistview = vf.getCurrentView().getId();
		}
		vfsentinal = true;
		
		
	}

	
	public void PicViewBtnClick(View view) {
		Log.v("btn", "PicBtnClicked");
		// showToast("Selected item id " + lv.getSelectedItemId());
		//showToast(lv.getCheckedItemPosition() + "");
		SparseBooleanArray items = new SparseBooleanArray();
		items = lv.getCheckedItemPositions();
		ArrayList<String> checkeditems = new ArrayList<String>();
		for (int i = 0 ; i < items.size(); i++){
			int position = items.keyAt(i);
			if (items.valueAt(i)){
				checkeditems.add(adapter.getItem(position));
			}
			
		}
		if (checkeditems.size() > 1){
			showToast("You have " + checkeditems.size() + " items selected. Please disselect " + (checkeditems.size() - 1) + " item(s)");
		}
		else{
			//showToast("good boy");
			String captiontext = checkeditems.get(0).toString();
			PictureRequest pr = new PictureRequest(captiontext);
			
			pr.execute();
			String prTemp = null;
			
			//vf.setDisplayedChild(2);
			
			String line = null;
			
			try {
				line = pr.get();
				String linelength = line.length() + "";
				//`Log.v("Line", linelength);
				//Log.v("line", line);
				//showToast("zero");
				byte[] imageAsBytes = Base64.decode(line.getBytes(), Base64.DEFAULT);
				//showToast("one");
				Bitmap decodedByte = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
				//showToast("two");
				iv = (ImageView)findViewById(R.id.iv);
				iv.setImageBitmap(decodedByte);
				//showToast("three");
				vf.setDisplayedChild(2);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (ExecutionException e) {
				
				e.printStackTrace();
			}
			
		
			
		}
		
	}

	private class PictureRequest extends AsyncTask<String, Void, String> {

		String captiontext = null;

		public PictureRequest(String captiontext) {
			this.captiontext = captiontext;
			

		}

		/**
		 * The portion of the asynchronous task that runs in the background
		 */
		protected String doInBackground(String... image) {
			try {

				// showToast("made it baby");
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();

				HttpPost httppost = new HttpPost("http://10.0.2.2:2020/MystuffWeb/edu.byu.isys413.jmcmurdi.actions.GetPic.action");
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				
				nameValuePairs.add(new BasicNameValuePair("ismobile", "true"));
				nameValuePairs.add(new BasicNameValuePair("captiontext", captiontext));
				
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				HttpResponse response = null;
				response = httpclient.execute(httppost);
				HttpEntity e = response.getEntity();

				JSONObject respobj = new JSONObject(EntityUtils.toString(e));


				String status = respobj.getString("status");
				// showToast(status);
				String encodedpic = respobj.getString("ePic");
			

				String S_response = encodedpic;

				return S_response;
			} catch (Exception e) {
				Log.v("tag", "The post to the server failed");
				// showToast("failed to post");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String test = sw.toString(); // stack trace as a string
				Log.v("tag", test);
			}
			return "failed";
		} /* do in background */

	}
	
	@Override
	public void onBackPressed(){
		if(vf.getCurrentView().getId() == vflistview){
			btnLogoutClicked(iv);
			//showToast("whatup");
			
		}
		else if(vf.getCurrentView().getId() == vfloginview){
			super.onBackPressed();
			//showToast("whatup");
		}
		else{
			//showToast(vf.getId() + "");
			//Log.v("viewstuff", vf.getCurrentView().getId()+ "");
			
			vf.showPrevious();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_stuff_mobile, menu);
		return true;
	}
	

	private class LoginPosting extends AsyncTask<String, Void, String> {

		String password = null;
		String username = null;

		public LoginPosting(String username, String password) {
			this.username = username;
			this.password = password;
			// Log.v("username", username);
			// Log.v("username", password);

		}

		/**
		 * The portion of the asynchronous task that runs in the background
		 */
		protected String doInBackground(String... image) {
			try {

				// showToast("made it baby");
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();

				HttpPost httppost = new HttpPost(
						"http://10.0.2.2:2020/MystuffWeb/edu.byu.isys413.jmcmurdi.actions.Login.action");
				// showToast("made it 2 baby");
				// setting up the nameVaule pairs
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs
						.add(new BasicNameValuePair("username", username));
				nameValuePairs
						.add(new BasicNameValuePair("password", password));
				nameValuePairs.add(new BasicNameValuePair("ismobile", "true"));
				// nameValuePairs.add(new BasicNameValuePair("username",
				// customer_id));
				// nameValuePairs.add(new BasicNameValuePair("imagedata",
				// imageString));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				// showToast("made it 3 baby");
				HttpResponse response = null;
				// showToast("made it 4 baby");
				response = httpclient.execute(httppost);
				// showToast("made it 5 baby");
				HttpEntity e = response.getEntity();
				// showToast("made it 6 baby");

				JSONObject respobj = new JSONObject(EntityUtils.toString(e));

				// pulling the balance from the JSON object and posting it to
				// the class variable string object "balance"
				// balance = respobj.getString("balance");

				String status = respobj.getString("status");
				// showToast(status);
				String custid = respobj.getString("custid");
				// showToast(custid);
				setCustid(custid);

				// Log.v("myJSON", respobj.toString());

				// JSONArray jsonarray = respobj.getJSONArray("piclist");

				String S_response = respobj.toString();

				return S_response;
			} catch (Exception e) {
				Log.v("tag", "The post to the server failed");
				// showToast("failed to post");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String test = sw.toString(); // stack trace as a string
				Log.v("tag", test);
			}
			return "failed";
		} /* do in background */

	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * @return the custid
	 */
	public String getCustid() {
		return custid;
	}

	/**
	 * @param custid
	 *            the custid to set
	 */
	public void setCustid(String custid) {
		this.custid = custid;
	}
	public void btnLogoutClicked(View view){
		setCustid(null);
		EditText passwordtext = (EditText) findViewById(R.id.password);
		EditText usernametext = (EditText) findViewById(R.id.username);
		passwordtext.setText("");
		usernametext.setText("");
		adapter.clear();
		adapter.notifyDataSetChanged();
		lv.setAdapter(null);
		
		
		vf.setDisplayedChild(0);
		
	}
	public void refreshlist(){
		adapter.clear();
		adapter.notifyDataSetChanged();
		lv.setAdapter(null);
		loginbtnclick(lv);
	}

}
