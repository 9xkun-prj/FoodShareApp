package com.iziroi.foodshare;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class BookmarksFragment extends Fragment {
	private GridView gridView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bookmarks, container, false);
		gridView = (GridView) v.findViewById(R.id.listBookmark);
		new LoadList().execute();
		return v;
	}
	
	private class LoadList extends AsyncTask<Void, Void, Void>{
		private ProgressDialog progress;
		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(getActivity(), "","Please wait...", true);
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(progress.isShowing())
				progress.dismiss();
			super.onPostExecute(result);
		}
	}

}
