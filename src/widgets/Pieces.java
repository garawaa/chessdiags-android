package widgets;

import java.util.ArrayList;

import ressources.Ressources;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import core.Partie;
import core.Piece;

public class Pieces extends View {

	Partie partie = null;
	ArrayList<PiecesListener> listeners = new ArrayList<PiecesListener>();
	static Paint paint = null;
	int highLight = -1;

	public Pieces(Context context,Partie p) {
		super(context);
		init();
		chargerPartie(p);
	}

	public Pieces(Context c) {
		super(c);
		init();
	}

	public Pieces(Context c,AttributeSet set) {
		super(c,set);
		init();
	}
	public Pieces(Context c,AttributeSet set,int style) {
		super(c,set,style);
		init();
	}

	public void init() {
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
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
	}

	public void chargerPartie(Partie p) {
		partie = p;

		this.invalidate();
	}

	public void addListener(PiecesListener l) {
		listeners.add(l);
	}

	public void removeListener(PiecesListener l) {
		listeners.remove(l);
	}


	public void broadcastPieceChoisie(int type) {
		for (PiecesListener l : listeners) {
			l.pieceChoisie(type);
		}
	}

	public interface PiecesListener {
		public void pieceChoisie(int type);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		float width = getWidth();
		float height = getHeight();
		float taille = 7;
		float tailleCase = width / taille;
		
		canvas.drawBitmap(Ressources.echiquier, new Rect(0,0,(int) (Ressources.echiquier.getWidth() / 8 * 7),(int) (Ressources.echiquier.getHeight() / 8 * 2)), new RectF(0,0,width,tailleCase * 2), paint);

		for (int i = 1; i < 7; i++) {
			Board.dessinCase(canvas, i, (i-1) * tailleCase, 0, tailleCase,i == highLight);
			Board.dessinCase(canvas, i+6, (i-1) * tailleCase, tailleCase, tailleCase,(i + 6) == highLight);
		}

	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		float width = getWidth();
		float tailleCase = width / 7;
		float height = tailleCase * 2;
		
		int type = 0;
		type = 1 + (int) (x / tailleCase);
		type = type % 7;
		if (y > height / 2 && type != 0) type += 6;

		highLight(type);
		this.broadcastPieceChoisie(type);

		return super.onTouchEvent(event);
	}
	
	public void highLight(int numCase) {
		this.highLight = numCase;
		invalidate();
	}
}
