package com.estragon.chessdiags2;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import chesspresso.Chess;

import com.billing.Billing;
import com.billing.BillingService.BillingServiceListener;
import com.estragon.sql.DAO;

import core.Source;
import donnees.ListeSources;

public class ChessPreferences extends PreferenceActivity implements OnPreferenceClickListener, BillingServiceListener {

	Preference addSource;
	PreferenceCategory sources;
	PreferenceCategory manageSources;
	private Method mStartIntentSender;
	private Object[] mStartIntentSenderArgs = new Object[5];

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();


	}

	private void verifListe() {
		majListe();
	}

	public void majListe() {
		sources.removeAll();
		for (final Source source : ListeSources.getListe()) {
			if (source.getId() == 1) continue;
			final Preference pref = new Preference(this);
			pref.setTitle(source.getName());
			pref.setSummary(source.getUrl());
			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub
					//source
					LayoutInflater inflater=LayoutInflater.from(pref.getContext());
					final View addView=inflater.inflate(R.layout.dialogsource, null);
					final AlertDialog dialog = new AlertDialog.Builder(pref.getContext())
					.setTitle(source.getName())
					.setView(addView)
					.setPositiveButton(getString(R.string.update),
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String name = ((EditText) addView.findViewById(R.id.name)).getText().toString();
							String url = ((EditText) addView.findViewById(R.id.url)).getText().toString();
							source.setName(name);
							source.setUrl(url);
							if (DAO.createOrUpdateSource(source))	{
								majListe();
							}
							else Toast.makeText(ChessPreferences.this, getString(R.string.invalidurl), 1).show();
						}
					})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// ignore, just dismiss
						}
					})
					.show();

					EditText name = (EditText) addView.findViewById(R.id.name);
					name.setText(source.getName());
					EditText url = (EditText) addView.findViewById(R.id.url);
					url.setText(source.getUrl());
					url.setEnabled(false);
					Button supprimer = (Button) addView.findViewById(R.id.boutonSupprimer);
					supprimer.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							DAO.deleteSource(source.getId());
							startActivity(ChessPreferences.this.getIntent());
							finish();
						}
					});
					return true;
				}
			});
			sources.addPreference(pref);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		verifListe();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.settings);
		addPreferencesFromResource(R.layout.preferences);
		sources = (PreferenceCategory) this.findPreference("sources");
		manageSources = (PreferenceCategory) this.findPreference("manageSources");

		majListe();

		addSource = new CustomPreference(this,null);
		addSource.setTitle(R.string.addsource);
		addSource.setOnPreferenceClickListener(this);
		Preference inApp = new Preference(this);
		inApp.setTitle(R.string.wantmore);
		inApp.setSummary(R.string.browserepositories);

		inApp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				WebView view = new WebView(ChessPreferences.this);
				view.loadUrl("http://chessdiags.com/API/sourceList.php");
				AlertDialog dialog = new AlertDialog.Builder(ChessPreferences.this).setView(view).setPositiveButton(R.string.ok, null).show();
				//final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://64cases.com/API/sourceList.php"));
				//startActivity(intent);
				return false;
			}
		});
		Preference yourOwn = new Preference(this);
		yourOwn.setTitle(R.string.createyourownsource);
		yourOwn.setSummary(getString(R.string.apidetails)+" (http://chessdiags.com/API/howto.php)");
		yourOwn.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				WebView view = new WebView(ChessPreferences.this);
				view.loadUrl("http://chessdiags.com/API/howto.php");
				AlertDialog dialog = new AlertDialog.Builder(ChessPreferences.this).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				}).show();
				return false;
			}
		});

		manageSources.addPreference(addSource);
		manageSources.addPreference(inApp);
		manageSources.addPreference(yourOwn);


		Preference about = findPreference("about");
		about.setSummary("Version "+ChessDiags.getVersionName());
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				WebView view = new WebView(ChessPreferences.this);
				WebSettings webSettings = view.getSettings();
				webSettings.setSavePassword(false);
				webSettings.setSaveFormData(false);
				webSettings.setJavaScriptEnabled(true);
				webSettings.setSupportZoom(false);
				view.loadUrl("file:///android_res/raw/about.html");
				AlertDialog dialog = new AlertDialog.Builder(ChessPreferences.this).setView(view).setPositiveButton(R.string.ok, null).show();
				return true;
			}
		});

		Preference donate = findPreference("donate");
		donate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				Toast.makeText(ChessPreferences.this, R.string.loading, Toast.LENGTH_LONG).show();
				ouvrirPageDon();
				return true;
			}
		});
	}

	public void ouvrirPageDon() {
		try {
			Billing.connexion(this,this);
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.inappnotsupported, 100).show();
		}
	}

	private void initCompatibilityLayer() {
		try {
			mStartIntentSender = ChessPreferences.class.getMethod("startIntentSender",
					new Class[] {
					IntentSender.class, Intent.class, int.class, int.class, int.class
			});
		} catch (SecurityException e) {
			mStartIntentSender = null;
		} catch (NoSuchMethodException e) {
			mStartIntentSender = null;
		}
	}


	void startBuyPageActivity(PendingIntent pendingIntent, Intent intent) {
		initCompatibilityLayer();
		if (mStartIntentSender != null) {
			// This is on Android 2.0 and beyond.  The in-app checkout page activity
			// will be on the activity stack of the application.
			try {
				// This implements the method call:
				// mActivity.startIntentSender(pendingIntent.getIntentSender(),
				//     intent, 0, 0, 0);
				mStartIntentSenderArgs[0] = pendingIntent.getIntentSender();
				mStartIntentSenderArgs[1] = intent;
				mStartIntentSenderArgs[2] = Integer.valueOf(0);
				mStartIntentSenderArgs[3] = Integer.valueOf(0);
				mStartIntentSenderArgs[4] = Integer.valueOf(0);
				mStartIntentSender.invoke(this, mStartIntentSenderArgs);
			} catch (Exception e) {

			}
		} else {
			// This is on Android 1.6. The in-app checkout page activity will be on its
			// own separate activity stack instead of on the activity stack of
			// the application.
			try {
				pendingIntent.send(this, 0 /* code */, intent);
			} catch (CanceledException e) {

			}
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		return false;
	}

	public class CustomPreference extends DialogPreference {

		public CustomPreference(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void showDialog(Bundle state) {
			// TODO Auto-generated method stub
			LayoutInflater inflater=LayoutInflater.from(getContext());
			final View addView=inflater.inflate(R.layout.dialogsource, null);
			new AlertDialog.Builder(getContext())
			.setTitle(R.string.newsource)
			.setView(addView)
			.setPositiveButton(R.string.add,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
					ajouterSource(addView);
					verifListe();
				}
			})
			.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
					// ignore, just dismiss
				}
			})
			.show();
			View zoneBoutons = (View) addView.findViewById(R.id.boutonSupprimer);
			zoneBoutons.setVisibility(View.GONE);
		}



		public void ajouterSource(View v) {
			//TODO
			EditText name = (EditText) v.findViewById(R.id.name);
			EditText url = (EditText) v.findViewById(R.id.url);
			if (DAO.addSource(name.getText().toString(), url.getText().toString()))	;
			else Toast.makeText(getContext(), R.string.invalidurloralreadyadded, 1).show();
		}



	}

	@Override
	public void connecte() {
		// TODO Auto-generated method stub
		try {
			PendingIntent intent = Billing.acheter();
			if (intent == null) throw new Exception();
			startBuyPageActivity(intent, new Intent());
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.inappnotsupported, 100).show();
		}

	}

	@Override
	public void deConnecte() {
		// TODO Auto-generated method stub
		
	}



}
