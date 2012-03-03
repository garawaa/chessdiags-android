package widgets;

import java.util.ArrayList;

import ressources.Ressources;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import chesspresso.Chess;
import chesspresso.position.Position;
import core.Partie;

public class Board extends View implements OnTouchListener {

	Partie partie = null;
	ArrayList<BoardListener> listeners = new ArrayList<BoardListener>();
	static Paint paint = null;
	int highLight = -1;

	public Board(Context context,Partie p) {
		super(context);
		init();
		chargerPartie(p);
	}

	public Board(Context c) {
		super(c);
		init();
	}

	public Board(Context c,AttributeSet set) {
		super(c,set);
		init();
	}
	public Board(Context c,AttributeSet set,int style) {
		super(c,set,style);
		init();
	}

	public void init() {
		this.setOnTouchListener(this);
		if (paint != null) return;
		paint = new Paint();
		paint.setTextSize(38);
		paint.setColor(Color.BLACK);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setFakeBoldText(true);
	} 

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int dim = Math.min(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(dim,dim);
	}

	public void chargerPartie(Partie p) {
		partie = p;

		this.invalidate();
	}

	public void addListener(BoardListener l) {
		listeners.add(l);
	}

	public void removeListener(BoardListener l) {
		listeners.remove(l);
	}

	public void broadcastCaseClickee(int index) {
		for (BoardListener l : listeners) {
			l.caseClickee(index);
		}
	}


	public interface BoardListener {
		public void caseClickee(int indexCase);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		float dim = Math.min(getWidth(), getHeight());
		float taille = 8;
		float tailleCase = dim / taille;

		canvas.drawBitmap(Ressources.echiquier, null, new RectF(0,0,dim,dim), paint);

		if (partie == null) return;


		//Dessin des pièces
		Position pos = partie.getPositionPieces();
		int[] conversion = new int[] {0,2,3,4,5,1,6};
		for (int i = 0; i < 64; i++ ) {
			int piece = conversion[pos.getPiece(Partie.conversionCase(i))];
			if (piece == 0) continue;
			int couleur = pos.getColor(Partie.conversionCase(i));
			dessinCase(canvas,piece + couleur * 6,getPosition(i)[0],getPosition(i)[1],tailleCase,i==highLight);
		} 
	}

	public float[] getPosition(int i) {
		return getPosition(i,false);
	}

	public float[] getPosition(int i,boolean center) {
		int taille = 8;
		float tailleCase = Math.min(getWidth(), getHeight()) / 8.0f;
		int x = (i%taille);
		int y = ((int) (i/taille));
		if (isInverse()) y = 7 - y;
		if (center) {
			return new float[] {x * tailleCase + tailleCase / 2,y * tailleCase + tailleCase /2};
		}
		else return new float[] {x * tailleCase,y * tailleCase};
	}

	public void majBoard() {
		this.postInvalidate();
	}

	public void highLight(int numCase) {
		this.highLight = numCase;
	}

	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		int caseX = (int) (x / v.getWidth() * 8);
		int caseY = (int) (y / v.getHeight() * 8);
		if (isInverse()) caseY = 7 - caseY;
		int numCase = caseX + caseY * 8;
		broadcastCaseClickee(numCase);
		return false;
	}
	
	public static void dessinCase(Canvas canvas, int piece, float offsetx, float offsety, float tailleCase, boolean choisie) {
		if (choisie) {
			canvas.save();
			canvas.clipRect(new RectF(offsetx,offsety,offsetx+tailleCase,offsety+tailleCase));
			canvas.drawColor(Color.GREEN);
			canvas.restore();
		}
	
		if (choisie) paint.setAlpha(150);
		canvas.drawBitmap(Ressources.pieces[piece],null, new RectF(offsetx,offsety,offsetx+tailleCase,offsety+tailleCase), paint);
		paint.setAlpha(255);
	}

	public static void dessinCase(Canvas canvas, int piece, float offsetx, float offsety, float tailleCase) {
		dessinCase(canvas,piece,offsetx,offsety,tailleCase,false);
	}

	public boolean isInverse() {
		return partie.getCouleurDefendue() != Chess.WHITE;
	}
	
	
	
}
