/*
Copyright (c) 2012 Sony Ericsson Mobile Communications AB
Copyright (c) 2012 Sony Mobile Communications AB.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of the Sony Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package smartwatch;

import ressources.Ressources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextPaint;
import chesspresso.position.FEN;
import chesspresso.position.Position;

import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.R;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

import core.Problem;
import donnees.ListeProblemes;
import donnees.ListeSources;

/**
 * Control extension for 8 Game for SmartWatch
 */
class ChessdiagsControlSmartWatch extends ControlExtension {

	private static final int SHOW_SOLVED_IMAGE_TIME = 1500;

	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;

	private static final int NUMBER_TILE_TEXT_SIZE = 24;

	private static final int RANDOM_START_MOVES = 100;

	private int mNumberOfMoves;

	private int mEmptyTileIndex;

	private TextPaint mNumberTextPaint;

	private Bitmap mCurrentImage = null;

	/** Bitmap used when showing finished image game */
	private Bitmap mFullImage = null;

	private int mTilePressIndex = -1;

	private boolean mLongPressed = false;

	private GameType mGameType = GameType.NUMBERS;

	private Handler mHandler = null;

	private GameState mGameState = GameState.PLAYING;

	/** Lower left button rectangle */
	private static final Rect sActionButton1Rect = new Rect(0, 88, 40, 128);

	/** Lower middle button rectangle */
	private static final Rect sActionButton2Rect = new Rect(44, 88, 84, 128);

	/** Lower right button rectangle */
	private static final Rect sActionButton3Rect = new Rect(88, 88, 128, 128);

	private Rect mPressedButtonRect = null;

	private int mPressedActionImageId;

	private int mPressedActionDrawableId;

	private int mWidth;

	private int mHeight;
	
	private Problem currentProblem = null;

	private enum GameType {
		IMAGE, NUMBERS
	}

	private enum GameState {
		PLAYING, FINISHED_SHOW_IMAGE, FINISHED_SHOW_MENU, ACTION_MENU,
	}

	/**
	 * Create eight puzzle control.
	 *
	 * @param hostAppPackageName Package name of host application.
	 * @param context The context.
	 * @param handler The handler to use
	 */
	ChessdiagsControlSmartWatch(final String hostAppPackageName, final Context context,
			Handler handler) {
		super(context, hostAppPackageName);
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}
		mHandler = handler;
		mNumberTextPaint = new TextPaint();
		mNumberTextPaint.setTextSize(NUMBER_TILE_TEXT_SIZE);
		mNumberTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mNumberTextPaint.setColor(Color.WHITE);
		mNumberTextPaint.setAntiAlias(true);

		mWidth = getSupportedControlWidth(context);
		mHeight = getSupportedControlHeight(context);
	}

	/**
	 * Get supported control width.
	 *
	 * @param context The context.
	 * @return the width.
	 */
	public static int getSupportedControlWidth(Context context) {
		return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
	}

	/**
	 * Get supported control height.
	 *
	 * @param context The context.
	 * @return the height.
	 */
	public static int getSupportedControlHeight(Context context) {
		return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
	}

	@Override
	public void onDestroy() {
		Dbg.d("NavigationControlLiveViewTouch onDestroy");

		if (mContext != null) {
			// mHandler.removeCallbacks(mDrawResult);
			// mHandler.removeCallbacks(mDrawActionScreen);
		}
	};

	@Override
	public void onStart() {
		Dbg.d("onStart");
		refresh();
	}
	
	@Override
	public void onResume() {
		refresh();
	}

	Bitmap getChessboard(int dimx, int dimy) {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		Bitmap bmp = Bitmap.createBitmap(dimx, dimy, conf); // this creates a MUTABLE bitmap
		Canvas canvas = new Canvas(bmp);
		canvas.drawBitmap(Ressources.echiquier, null, new RectF(0, 0, dimx, dimy), null);
		
		float tailleCase = dimx/8;

		
		//Dessin des pièces
		Position pos = new Position(FEN.START_POSITION);
		try {
			if (currentProblem == null) currentProblem = ListeProblemes.getListe().getRandomProblem();
			pos = new Position(currentProblem.getPosition());
		}
		catch (Exception e) {
			//No problem in the database, print a message
		}
		int[] conversion = new int[] {0,2,3,4,5,1,6};
		for (int i = 0; i < 64; i++ ) {
			int piece = conversion[pos.getPiece(conversionCase(i))];
			if (piece == 0) continue;
			int couleur = pos.getColor(conversionCase(i));
			dessinCase(mContext,canvas,piece + couleur * 6,getPosition(i,tailleCase)[0],getPosition(i,tailleCase)[1],tailleCase,false);
		} 

		return bmp;
	}
	
	public static int conversionCase(int indexCase) {
		int oldY = (int) (indexCase / 8);
		int newY = 7 - oldY;
		int oldX = indexCase % 8;
		return newY * 8 + oldX;
	}
	
	public float[] getPosition(int i,float tailleCase) {
		return getPosition(i,false,tailleCase);
	}

	public float[] getPosition(int i,boolean center,float tailleCase) {
		int taille = 8;
		int x = (i%taille);
		int y = ((int) (i/taille));
		if (isInverse()) {
			y = 7 - y;
			x = 7 - x;
		}
		if (center) {
			return new float[] {x * tailleCase + tailleCase / 2,y * tailleCase + tailleCase /2};
		}
		else return new float[] {x * tailleCase,y * tailleCase};
	}
	
	boolean isInverse() {
		return false;
	}

	public static void dessinCase(Context context, Canvas canvas, int piece, float offsetx, float offsety, float tailleCase, boolean choisie) {
		if (choisie) {
			canvas.save();
			canvas.clipRect(new RectF(offsetx,offsety,offsetx+tailleCase,offsety+tailleCase));
			canvas.drawColor(Color.GREEN);
			canvas.restore();
		}

		//if (choisie) paint.setAlpha(150);
		canvas.drawBitmap(Ressources.pieces[piece],null, new RectF(offsetx,offsety,offsetx+tailleCase,offsety+tailleCase), null);
		//paint.setAlpha(255);
	}

	@Override
	public void onTouch(ControlTouchEvent event) {
		// TODO Auto-generated method stub
		//0 = down
		//1 = continue
		//2 = up
		if (event.getAction() == 0) {
			currentProblem = ListeProblemes.getListe().getRandomProblem();
			refresh();
		} 
		super.onTouch(event);
	}

	void refresh() {
		int dimx = getSupportedControlWidth(mContext);
		int dimy = getSupportedControlHeight(mContext);
		showBitmap(getChessboard(dimx,dimy));
	}




}
