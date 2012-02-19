package com.estragon.sql;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DatabaseHelper extends SQLiteOpenHelper {  
	private static final String DATABASE_NAME = "ChessDiags";   
	private static final int DATABASE_VERSION = 16;  
	static DatabaseHelper helper;

	public synchronized static DatabaseHelper getHelper() {
		if (helper == null) helper = new DatabaseHelper(Appli.getInstance());
		return helper;
	}


	private DatabaseHelper(Context context) {  
		super(context, DATABASE_NAME, null, DATABASE_VERSION);  
	}  


	@Override  
	public void onCreate(SQLiteDatabase db) {  
		if (1==1) return;
		//Delete old tables
		db.execSQL("DROP TABLE IF EXISTS diagrammes");
		db.execSQL("DROP TABLE IF EXISTS sources");

		//Create sources table
		db.execSQL("CREATE TABLE sources (id integer primary key autoincrement,name varchar(30), url varchar(50) unique, state integer DEFAULT 1, uploadSupported integer DEFAULT 1)");
		//Add the custom problem source
		db.execSQL("INSERT INTO sources (id,name,url) VALUES(1,'"+Appli.getInstance().getString(R.string.yourcreations)+"',NULL)"); 
		//Add the 64 cases beginner source
		db.execSQL("INSERT INTO sources (id,name,url,uploadSupported) VALUES(2,'"+Appli.getInstance().getString(R.string.beginnerrepo)+"','http://64cases.com/API/maj.php?id=1',0)");
		//Add the 64 cases wtharvey
		db.execSQL("INSERT INTO sources (id,name,url,uploadSupported) VALUES(3,'"+Appli.getInstance().getString(R.string.wtharvey)+"','http://64cases.com/API/maj.php?id=2',0)");
		//Add the 64 cases source
		db.execSQL("INSERT INTO sources (id,name,url) VALUES(4,'"+Appli.getInstance().getString(R.string.publicrepo)+"','http://64cases.com/API/maj.php?id=3')");

		//Create diagrammes table
		db.execSQL("CREATE TABLE diagrammes (secondid int,source int,position varchar(65),difficulte int,trouve int,nom text, description text, moves int DEFAULT 2, PRIMARY KEY (secondid,source))"); 
	}  

	@Override  
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
		Log.w("Content provider database",  
				"Upgrading database from version " + oldVersion + " to "  
						+ newVersion + ", which will destroy all old data");  
		onCreate(db);  

	}  

}  