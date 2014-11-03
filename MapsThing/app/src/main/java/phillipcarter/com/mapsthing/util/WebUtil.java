package phillipcarter.com.mapsthing.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil {
    /**
     * Returns String contents of data downloaded from a URL.
     */
    public static String downloadUrl(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            is = conn.getInputStream();

            return readIt(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Reads an input stream and converts it to a string.
     */
    public static String readIt(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bfr = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bfr.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }
}
