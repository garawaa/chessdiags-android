package util;

import java.net.URL;

public class Utils {

	public static boolean isUrlValid(String url) {
		try {
			URL url2 = new URL(url);
			if ((!url2.getProtocol().equals("http") && !url2.getProtocol().equals("https")) || url2.getHost().equals("")) throw new Exception();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
