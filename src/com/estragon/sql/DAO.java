package com.estragon.sql;

import java.util.Date;

import util.Utils;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import core.History;
import core.Problem;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class DAO {

	public static ListeProblemes loadProblemes() {
		try {
			QueryBuilder<Problem, Integer> queryBuilder =
					DatabaseHelper.getHelper().getProblemDao().queryBuilder();
			queryBuilder.orderBy("source", true);
			queryBuilder.orderBy("id", true);
			return new ListeProblemes(queryBuilder.query());
		}
		catch (Exception e) {
			return new ListeProblemes();
		}
		/*
		ListeProblemes problemes = new ListeProblemes();
		SQLiteDatabase db = DatabaseHelper.getHelper().getWritableDatabase();
		Cursor c = db.rawQuery("SELECT secondid,source,position,difficulte,trouve,nom,description,moves FROM diagrammes ORDER BY source,secondid",null);
		while (c.moveToNext()) {
			int id = c.getInt(0);
			int source = c.getInt(1);
			String position = c.getString(2);
			int difficulty = c.getInt(3);
			int resolu = c.getInt(4);
			String nom = c.getString(5);
			String description = c.getString(6);
			int nbMoves = c.getInt(7);
			Problem problem = new Problem(id,source,position,difficulty,resolu==1,nom,description,nbMoves);
			problem.setSauvegarde(true);

			problemes.add(problem);
		}
		c.close();

		return problemes;
		 */
	}

	public static ListeSources loadSources() {
		try {
			QueryBuilder<Source, Integer> queryBuilder =
					DatabaseHelper.getHelper().getSourceDao().queryBuilder();
			queryBuilder.orderBy("id", true);
			return new ListeSources(queryBuilder.query());
		}
		catch (Exception e) {
			Log.e("lol","",e);
			return new ListeSources();
		}
		/*
		ListeSources sources = new ListeSources();
		SQLiteDatabase db = DatabaseHelper.getHelper().getWritableDatabase();
		Cursor c = db.rawQuery("SELECT id,name,url,state,uploadSupported FROM sources ORDER BY id",null);
		while (c.moveToNext()) {
			int id = c.getInt(0);
			String name = c.getString(1);
			String url = c.getString(2);
			boolean state = (c.getInt(3) == 1);
			boolean uploadSupported = (c.getInt(4) == 1);
			sources.add(new Source(id,name,url,state,uploadSupported));
		}
		c.close();

		return sources;
		 */
	}

	public static boolean addSource(String name,String url) {
		if (!Utils.isUrlValid(url)) return false;
		Source source = new Source(name,url,true);
		return createOrUpdateSource(source);
	}
	
	public static boolean createOrUpdateSource(Source source) {
		try {
			getSourceDao().createOrUpdate(source);
			ListeSources.getListe().add(source);
			return true;
		}
		catch (Exception e) {
			Log.e("Chessdiags","",e);
			return false;
		}
	}

	public static boolean deleteSource(int id) {
		try {
			getSourceDao().deleteById(id);
			ListeSources.getListe().remove(ListeSources.getListe().getSourceById(id));
			DeleteBuilder<Problem,Integer> delete = getProblemDao().deleteBuilder();
			delete.where().eq("source",id);
			getProblemDao().delete(delete.prepare());
			ListeProblemes.charger();
			
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static void deleteProblem(int source,int id) {
		Problem problem = ListeProblemes.getListe().getProblem(id, source);
		ListeProblemes.getListe().remove(problem);
		try {
			getProblemDao().delete(problem);
		}
		catch (Exception e) {

		}
	}

	public static void diagrammeResolu(Problem problem) {
		problem.setResolu(true);
		try {
			getProblemDao().update(problem);
		}
		catch (Exception e) {

		}
	}

	public static void setUploadSupported(int id, boolean upload) {
		Source source = ListeSources.getListe().getSourceById(id);
		source.setUploadSupported(upload);
		try {
			getSourceDao().update(source);
		}
		catch (Exception e) {
			
		}
	}

	public static Dao<Source,Integer> getSourceDao() {
		try {
			return DatabaseHelper.getHelper().getSourceDao();
		}
		catch (Exception e) {
			return null;
		}
	}

	public static Dao<Problem,Integer> getProblemDao() {
		try {
			return DatabaseHelper.getHelper().getProblemDao();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Dao<History,Integer> getHistoryDao() {
		try {
			return DatabaseHelper.getHelper().getHistoryDao();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static void addHistory(Problem problem) {
		try {
			History history = new History();
			history.setProblemInternalId(problem.getInternalId());
			history.setDate(new Date());
			Dao<History,Integer> dao = DatabaseHelper.getHelper().getHistoryDao();
			dao.createOrUpdate(history);
		}
		catch (Exception e) {
			Log.e("Chessdiags", "", e);
		}
	}

}
