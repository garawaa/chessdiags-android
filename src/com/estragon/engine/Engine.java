package com.estragon.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.uci.Uci;
import com.estragon.chessdiags2.Coup;


public class Engine {
	Process process;
	BufferedReader input;
	OutputStream output; 
	AnalyseListener listener;
	Timer t;
	Uci uci;
	public static final int ANALYSIS_TIME_MILLIS = 500;
	static Engine engine = null;

	/**
	 * Load the chess engine
	 */
	public static void loadEngine() {
		if (engine != null) return;
		new Thread(new Runnable() {
			public void run() {
				engine = new Engine();
				engine.load();
			}
		}).start();
	}
	
	public static Engine getEngine() throws EngineNotReadyException {
		if (engine == null) throw new EngineNotReadyException();
		return engine;
	}
	
	/**
	 * @param c
	 * @param engineName the name of the engine, use DEFAULT_ENGINE_NAME for the default one
	 */
	public Engine() {

	}

	/**
	 * Write a single line to the engine
	 * @param wat the line to write (without line delimiter)
	 * @throws IOException
	 */
	public void writeLine(String wat) {
		uci.message(wat);
		//this.output.write((wat+"\n").getBytes());
	}

	public void analyse() {
		analyse(ANALYSIS_TIME_MILLIS);
	}

	/**
	 * Let the engine analyse for exactly milliTime milliseconds
	 * @param milliTime
	 */
	public void analyse(long milliTime) {

		t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				writeLine("stop");
				if (uci.getEngine().getBestMove() == 0) {
					Log.e("Chessdiags","No move found ...");
				}
				else {
					Engine.this.broadCastBestMove(new Coup(63-Move.getFromIndex(uci.getEngine().getBestMove()),63-Move.getToIndex(uci.getEngine().getBestMove())));
					Log.i("Chessdiags","Best move : "+uci.getEngine().getBestMove());
					Log.i("Chessdiags","from : "+Move.getFromIndex(uci.getEngine().getBestMove()));
					Log.i("Chessdiags","to : "+Move.getToIndex(uci.getEngine().getBestMove()));
				}
			}
		}, milliTime);
		this.writeLine("go movetime "+milliTime);



	}

	/**
	 * Give the specified fen to the engine
	 * @param fen must be a complete fen (not only the position part)
	 */
	public void setPosition(String fen) {
		this.writeLine("ucinewgame");
		this.writeLine("position fen "+fen+" moves");
	}

	/**
	 * Set the analyse listener of this engine, the listener will receive each analyse the engine provides
	 */
	public void setAnalyseListener(AnalyseListener l) {
		listener = l;
	}

	/**
	 * Remove the listener of this engine
	 */
	public void removeAnalyseListener() {
		listener = null;
	}

	/**
	 * Inform the listener that the best move was found
	 * @param c the best move found
	 */
	public void broadCastBestMove(Coup c) {
		if (t != null) {
			t.cancel();
			t.purge();
		}
		if (listener == null) return;
		listener.bestMoveFound(c);
	}

	public static synchronized void waitForLoad() {

	}

	public static class EngineNotReadyException extends Exception {

	}

	/**
	 * Load the engine and start it
	 */
	public synchronized void load() {
		if (uci != null) return;
		long millis = System.currentTimeMillis();
		uci = new Uci();
		this.writeLine("uci");
		Log.i("Chessdiags","Engine loaded in "+(System.currentTimeMillis()-millis)+" ms");
		/*String location = this.context.getFilesDir().getAbsolutePath()+"/"+name;
		try {
			if (process != null) return; //if the engine is already started
			if(!new File(location).exists()) {

			//The engine is not present, we have to copy it from the apk
			InputStream i = context.getAssets().open(name);	
			FileOutputStream w = null;

			//Save it on the internal storage
			w = this.context.openFileOutput(name,Context.MODE_WORLD_READABLE);

			if (w == null) throw new FileNotFoundException("");

			//FileOutputStream w = new FileOutputStream(location);
			int nbLecture;	
			byte[] buffer =new byte[4096]; 
			while( (nbLecture = i.read(buffer)) != -1 ) 
				w.write(buffer, 0, nbLecture);
			i.close();
			w.close();
			}

			//We have to chmod it so that it can be launched
			//Process chmod = Runtime.getRuntime().exec("/system/bin/chmod 744 "+location);

			//Wait for the chmod to end, should not take long
			//chmod.waitFor();

			//The engine is now on /data/data/package/name
			process = Runtime.getRuntime().exec(location);

			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			output = process.getOutputStream();

			//Initialize the stream reader
			new Thread(new StreamReader(this)).start();

			//Let the engine know that we gonna work with uci
			output.write("uci\n".getBytes());
		}
		catch (Exception e) {
			Toast.makeText(this.context, name+" engine load failed "+e.toString() , Toast.LENGTH_LONG).show();
		}*/
	}
}
