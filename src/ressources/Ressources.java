package ressources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.R;

public class Ressources {

	private static final int[] idPieces = new int[] {R.drawable.p0,R.drawable.p1,R.drawable.p2,R.drawable.p3,R.drawable.p4,R.drawable.p5,R.drawable.p6
		,R.drawable.p7,R.drawable.p8,R.drawable.p9,R.drawable.p10,R.drawable.p11,R.drawable.p12};
	public static final Bitmap[] pieces = new Bitmap[13];
	public static Bitmap echiquier = null;
	
	public synchronized static void charger() {
		new Thread(new Runnable() {
			public void run() {
				long millis = System.currentTimeMillis();
				for (int i = 0; i < pieces.length; i++) {
					pieces[i] = BitmapFactory.decodeResource(Appli.getInstance().getResources(), idPieces[i]);
				}
				echiquier = BitmapFactory.decodeResource(Appli.getInstance().getResources(), R.drawable.echiquier);
				Log.i("Chessdiags","Ressources loaded in "+(System.currentTimeMillis()-millis)+" ms");
			}
		}).start();
	}
	
	public synchronized static void waitForLoad() {
		
	}
}
