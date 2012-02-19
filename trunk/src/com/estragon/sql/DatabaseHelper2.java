package com.estragon.sql;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.R;
import com.estragon.chessdiags2.TestActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import core.Problem;
import core.Source;

public class DatabaseHelper2 extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "chessdiags";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 23;

	// the DAO object we use to access the SimpleData table
	private Dao<SimpleData, Integer> simpleDao = null;
	private Dao<Problem, Integer> problemDao = null;
	private Dao<Source, Integer> sourceDao = null;
	private RuntimeExceptionDao<SimpleData, Integer> simpleRuntimeDao = null;
	
	static DatabaseHelper2 helper = null;

	public DatabaseHelper2(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION,R.raw.ormlite_config);
	}

	public static synchronized DatabaseHelper2 getHelper() {
		if (helper == null) {
			helper = OpenHelperManager.getHelper(Appli.getInstance(), DatabaseHelper2.class);
		}
		return helper;
	}
	
	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, SimpleData.class);
			TableUtils.createTable(connectionSource, Problem.class);
			TableUtils.createTable(connectionSource, Source.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

		RuntimeExceptionDao<Source, Integer> sourceDao = getRuntimeExceptionDao(Source.class);
		sourceDao.create(new Source(1,Appli.getInstance().getString(R.string.yourcreations),null,false));
		sourceDao.create(new Source(2,Appli.getInstance().getString(R.string.beginnerrepo),"http://chessdiags.com/API/maj.php?id=1",false));
		sourceDao.create(new Source(3,Appli.getInstance().getString(R.string.wtharvey),"http://chessdiags.com/API/maj.php?id=2",false));
		sourceDao.create(new Source(4,Appli.getInstance().getString(R.string.publicrepo),"http://chessdiags.com/API/maj.php?id=3",true));
		
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, SimpleData.class, true);
			TableUtils.dropTable(connectionSource, Problem.class, true);
			TableUtils.dropTable(connectionSource, Source.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<SimpleData, Integer> getDao() throws SQLException {
		if (simpleDao == null) {
			simpleDao = getDao(SimpleData.class);
		}
		return simpleDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Problem, Integer> getProblemDao() throws SQLException {
		if (problemDao == null) {
			problemDao = getDao(Problem.class);
		}
		return problemDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Source, Integer> getSourceDao() throws SQLException {
		if (sourceDao == null) {
			sourceDao = getDao(Source.class);
		}
		return sourceDao;
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<SimpleData, Integer> getSimpleDataDao() {
		if (simpleRuntimeDao == null) {
			simpleRuntimeDao = getRuntimeExceptionDao(SimpleData.class);
		}
		return simpleRuntimeDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		simpleRuntimeDao = null;
	}
}
