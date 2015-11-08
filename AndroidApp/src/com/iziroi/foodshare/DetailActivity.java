package com.iziroi.foodshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity {
	ImageView imgView;
	TextView lbTitle, lbAddress, lbContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initUI();
		Intent i = getIntent();
		new ReadJSON().execute("http://192.168.56.1/foodshare/food_detail.php?id="+i.getStringExtra("id"));
	}
	
	private void initUI() {
		imgView = (ImageView) findViewById(R.id.imgView);
		lbTitle = (TextView) findViewById(R.id.lbTitle);
		lbAddress = (TextView) findViewById(R.id.lbAddress);
		lbContent = (TextView) findViewById(R.id.lbContent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ReadJSON extends AsyncTask<String, Void, Void>{
		private ProgressDialog progress;
		private JSONArray food;
		String jsonStr;
		HashMap<String, String> hash = new HashMap<String, String>();
		Bitmap bitmap;
		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(DetailActivity.this, "","Please wait...", true);
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(String... params) {
			ServiceHandler sh = new ServiceHandler();
            jsonStr = sh.makeServiceCall(params[0], ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                     
                    // Getting JSON Array node
                    food = jsonObj.getJSONArray("food");
 
                    // looping through All Contacts
                    
                        JSONObject c = food.getJSONObject(0);
                         
                        String id = c.getString("id");
                        String title = c.getString("title");
                        String address = c.getString("address");
                        String image = c.getString("image");
                        String description = c.getString("description");
 
                        // adding each child node to HashMap key => value
                        hash.put("id", id);
                        hash.put("title", title);
                        hash.put("address", address);
                        hash.put("image", image);
                        hash.put("description", description);
                        bitmap = _downloadImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(progress.isShowing())
				progress.dismiss();
			imgView.setImageBitmap(bitmap);
			lbTitle.setText(hash.get("title"));
			lbAddress.setText(hash.get("address"));
			lbContent.setText(hash.get("description"));
		}
	}
	private Bitmap _downloadImage(String uri) {
	    Bitmap bm = null;
	   
	    //open connection to uri & pass to bitmap
	    InputStream in = null;
	    try {
	      in = _openConnection(uri);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	   
	    //decode inputstream to bitmap
	    bm = BitmapFactory.decodeStream(in);
	    return bm;
	  }

	
	private InputStream _openConnection(String uri) throws Exception {
	    InputStream in = null;
	   
	    try {
	      URL url = new URL(uri);
	      URLConnection conn = url.openConnection();
	     
	      //httpConnection
	      if(!(conn instanceof HttpURLConnection)) {
	        throw new Exception("Not HTTP connection");
	      }
	     
	      //continue process
	      HttpURLConnection htConn = (HttpURLConnection) conn;
	      htConn.setAllowUserInteraction(false);
	      htConn.setRequestMethod("GET");
	      htConn.connect();
	     
	      //file is exist
	      int responseCode = -1;
	      responseCode = htConn.getResponseCode();
	      if(responseCode == HttpURLConnection.HTTP_OK){
	        in = htConn.getInputStream();
	      }
	    } catch (MalformedURLException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	   
	    return in;
	 }
}
