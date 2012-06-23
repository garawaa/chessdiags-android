package com.estragon.chessdiags2.intent;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.estragon.chessdiags2.R;
import com.estragon.sql.DAO;

public class AddSourceActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Uri uri = getIntent().getData();
		String name = uri.getQueryParameter("name");
		String url = uri.getQueryParameter("url");
		if (DAO.addSource(name,url)) {
			//ChessDiags.shouldMaj(true);
			Toast.makeText(this,getString(R.string.sourcesuccesfullyadded,name), Toast.LENGTH_SHORT).show();
		}
		else Toast.makeText(this, getString(R.string.invalidurloralreadyadded,name), Toast.LENGTH_SHORT).show();
		finish();
	}


}
