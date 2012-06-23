package com.estragon.chessdiags2;

import ressources.Ressources;
import android.app.Application;
import android.content.res.Configuration;

public class Appli extends Application {
	private static Appli appli;

	public static Appli getInstance() {
		return appli;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		appli = this;   
		//Chargement des ressources (en async) 
		Ressources.charger();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}



}
