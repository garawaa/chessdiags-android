package com.estragon.chessdiags2;

import android.content.Intent;
import android.os.Bundle;

import com.estragon.sql.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class ORMActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getHelper();
		startActivity(new Intent(this,TestActivity.class));
	}

	
}
