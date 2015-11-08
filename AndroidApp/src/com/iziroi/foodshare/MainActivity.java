package com.iziroi.foodshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity {

	private GoogleMap map;
	private EditText txtSearch;
	private ImageButton btnVoice;
	private int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	private LinearLayout layoutSearch;
	private ArrayList<HashMap<String, String>> foodList = new ArrayList<HashMap<String,String>>();
	List<Bitmap> listBitmap = new ArrayList<Bitmap>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initUI();
		gmap();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void initUI() {
		layoutSearch = (LinearLayout) findViewById(R.id.layoutSearch);
		txtSearch = (EditText) findViewById(R.id.txtSearch);
		btnVoice = (ImageButton) findViewById(R.id.btnVoice);
		checkVoiceRecognition();
		txtSearch.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					moveLocation();
					return true;
				}
				return false;
			}
		});
	}

	public void checkVoiceRecognition() {
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			btnVoice.setEnabled(false);
			showToastMessage("Voice recognizer not present");
		}
	}

	private void gmap() {
		try {
			initilizeMap();
			GPSTracker mGPS = new GPSTracker(this);
			LatLng sydney;
			if (mGPS.canGetLocation()) {
				mGPS.getLocation();
				sydney = new LatLng(mGPS.getLatitude(), mGPS.getLongitude());
			} else
				sydney = new LatLng(21.023406605281156, 105.7676637545228);
			map.setMyLocationEnabled(true);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

			map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker arg0) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public View getInfoContents(Marker marker) {
					View v = getLayoutInflater().inflate(
							R.layout.custom_infowindow, null);
					ImageView imgView = (ImageView) v.findViewById(R.id.imgView);
					TextView lbLat = (TextView) v.findViewById(R.id.lbLat);
					TextView lbLong = (TextView) v.findViewById(R.id.lbLong);
					if(foodList.size() > 0)
					{
						for (int i = 0; i < foodList.size(); i++) {
							if(marker.getTitle().equals(foodList.get(i).get("id")))
							{
								lbLat.setText(foodList.get(i).get("title"));
								lbLong.setText(foodList.get(i).get("address"));
								imgView.setImageBitmap(listBitmap.get(i));
							}
						}
					}
					return v;
				}
			});

			/*
			 * map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			 * 
			 * @Override public void onMapClick(LatLng arg0) { map.clear();
			 * 
			 * MarkerOptions markerOptions = new MarkerOptions();
			 * 
			 * // Setting position on the MarkerOptions
			 * markerOptions.position(arg0);
			 * 
			 * // Animating to the currently touched position
			 * map.animateCamera(CameraUpdateFactory.newLatLng(arg0));
			 * 
			 * // Adding marker on the GoogleMap Marker marker =
			 * map.addMarker(markerOptions);
			 * 
			 * // Showing InfoWindow on the GoogleMap marker.showInfoWindow(); }
			 * });
			 */

			map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

				@Override
				public void onInfoWindowClick(Marker m) {
					Intent i = new Intent(getApplicationContext(), DetailActivity.class);
					i.putExtra("id", m.getTitle());
					startActivity(i);
				}
			});
		} catch (Exception e) {

		}
	}

	private void initilizeMap() {
		if (map == null) {
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			// check if map is created successfully or not
			if (map == null) {
				layoutSearch.setVisibility(View.GONE);
				showToastMessage("Sorry! unable to create maps");	
			}
			else{
				new ReadJSON().execute("http://192.168.56.1/foodshare/food.php");
			}
		}
	}

	private LatLng getLocation(String address) {
		Geocoder coder = new Geocoder(this);
		LatLng location = null;
		try {
			double longitude = 0, latitude = 0;
			ArrayList<Address> adresses = (ArrayList<Address>) coder
					.getFromLocationName(address, 50);
			for (Address add : adresses) {
				// if (statement) {//Controls to ensure it is right address such
				// as country etc.
				longitude = add.getLongitude();
				latitude = add.getLatitude();
				// }
			}
			location = new LatLng(latitude, longitude);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	public void VoiceCall(View v) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me".toString());

		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		int noOfMatches = 10;
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
			if (resultCode == RESULT_OK) {

				ArrayList<String> textMatchList = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (!textMatchList.isEmpty()) {
					if (textMatchList.get(0).contains("search")) {
						String searchQuery = textMatchList.get(0);
						searchQuery = searchQuery.replace("search", "");
						Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
						search.putExtra(SearchManager.QUERY, searchQuery);
						startActivity(search);
					} else {
						txtSearch.setText(textMatchList.get(0).toString());
						moveLocation();
					}

				}
			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
				showToastMessage("Audio Error");
			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
				showToastMessage("Client Error");
			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
				showToastMessage("Network Error");
			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
				showToastMessage("No Match");
			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
				showToastMessage("Server Error");
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

	void showToastMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void moveLocation() {
		LatLng location = getLocation(txtSearch.getText().toString().trim());
		showToastMessage(location.latitude + ", " + location.longitude);
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(location);
		map.animateCamera(CameraUpdateFactory.newLatLng(location));
	}
	
	private class ReadJSON extends AsyncTask<String, Void, Void>{
		private ProgressDialog progress;
		private JSONArray food;
		String jsonStr;
		
		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(MainActivity.this, "","Please wait...", true);
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
                    for (int i = 0; i < food.length(); i++) {
                        JSONObject c = food.getJSONObject(i);
                         
                        String id = c.getString("id");
                        String title = c.getString("title");
                        String address = c.getString("address");
                        String image_small = c.getString("image_small");
                        String _lat = c.getString("lat");
                        String _long = c.getString("long");
                        
                        // tmp hashmap for single contact
                        HashMap<String, String> hist = new HashMap<String, String>();
 
                        // adding each child node to HashMap key => value
                        hist.put("id", id);
                        hist.put("title", title);
                        hist.put("address", address);
                        hist.put("image_small", image_small);
                        hist.put("lat", _lat);
                        hist.put("long", _long);
                        listBitmap.add(_downloadImage(image_small));
                        foodList.add(hist);
                    }
                } catch (JSONException e) {
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
			for (int i = 0; i < foodList.size(); i++) {
				LatLng location = new LatLng(Double.parseDouble(foodList.get(i).get("lat")),
						Double.parseDouble(foodList.get(i).get("long")));
				MarkerOptions maker2 = new MarkerOptions().position(location).title(foodList.get(i).get("id"));
				maker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.gmap_marker));
				map.addMarker(maker2);	
			}
			
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
