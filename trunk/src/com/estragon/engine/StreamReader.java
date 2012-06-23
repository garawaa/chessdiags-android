package com.estragon.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.estragon.chessdiags2.Coup;

public class StreamReader implements Runnable {
	Pattern bestMove = Pattern.compile("bestmove ([a-h]{1}[1-8]{1}[a-h]{1}[1-8]{1})");
	Engine e;
	public StreamReader(Engine e) {
		this.e = e;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String ligne = null;
		try {
			while ((ligne = e.input.readLine()) != null) {
				Matcher match = bestMove.matcher(ligne);
				if (match.find()) {
					Log.e("Read : ","Engine => move found : "+match.group(1));
					Log.e("Read : ","Engine : "+parseMove(match.group(1)));
					e.broadCastBestMove(parseMove(match.group(1)));
				}
				Log.e("Lu : ","Engine : "+ligne);
			}
		}
		catch (Exception e) {
			Log.e("Chessdiags","broken pipe",e);
		}
	}

	private static int letterToInt(char letter) {
		return (int) letter - (int) 'a';
	}
	
	/**
	 * Parses a h7g2 like String to a Coup
	 * @return
	 */
	public static Coup parseMove(String move) {
		if (move.length() != 4) return null;
		int start = letterToInt(move.charAt(0)) + (8 - Integer.parseInt(move.charAt(1)+"")) * 8;
		int finish = letterToInt(move.charAt(2)) + (8 - Integer.parseInt(move.charAt(3)+"")) * 8;
		return new Coup(start,finish);
	}
}
