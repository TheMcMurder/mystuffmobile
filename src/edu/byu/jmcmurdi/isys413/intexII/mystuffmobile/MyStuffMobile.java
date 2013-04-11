package edu.byu.jmcmurdi.isys413.intexII.mystuffmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MyStuffMobile extends Activity {
	ViewFlipper vf = null;
	HttpClient client = null;
	private ArrayList<String> captionList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mystuffmobile);
		vf = (ViewFlipper) findViewById(R.id.vf);
		client = new DefaultHttpClient();

	}

	public void loginbtnclick(View view) {
		try {
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
				showToast(lposting.get());
			}else{
				temp = lposting.get();
				//showToast(temp);
				JSONObject myjson = null;
				try {
					myjson = new JSONObject(temp);
					String String_shouldbe_array = myjson.getString("piclist");
					showToast(String_shouldbe_array);
					Log.v("piclist",String_shouldbe_array);
					//JSONArray myjsonarray = (JSONArray)myjson;
					JSONArray myjsonarray = new JSONArray(String_shouldbe_array);
					for (int i = 0; i < myjsonarray.length(); i ++){
						JSONObject tempJSONobj = myjsonarray.getJSONObject(i);
						showToast(tempJSONobj.get("caption").toString());
						captionList.add(tempJSONobj.get("caption").toString());
					}
					
				
				} catch (JSONException e) {
					showToast("Problem converting the string to a JSON object or converting the string into a jsonarray");
					e.printStackTrace();
				}
			}
				
		} catch (InterruptedException e) {
			showToast("INterrupted Exception");
			e.printStackTrace();
		} catch (ExecutionException e) {
			showToast("ExecutionException");
			e.printStackTrace();
		}

		// vf.setDisplayedChild(1);
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
			Log.v("username", username);
			Log.v("username", password);

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
				//showToast(status);
				String custid = respobj.getString("custid");
				//showToast(custid);

				//Log.v("myJSON", respobj.toString());

				//JSONArray jsonarray = respobj.getJSONArray("piclist");

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
}
