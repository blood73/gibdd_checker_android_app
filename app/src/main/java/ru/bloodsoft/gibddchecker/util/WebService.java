package ru.bloodsoft.gibddchecker.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class WebService {

    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE = "Cookie";

    static CookieManager msCookieManager = new CookieManager();
    private static int responseCode;

    // HTTP GET request
   public static InputStream sendGetStream(String url) throws Exception {

       Integer connectionTimeout = new SettingsStorage().getConnectionTimeout();

       URL obj = new URL(url);

       HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

       con.setReadTimeout(connectionTimeout);
       con.setConnectTimeout(connectionTimeout);
       con.setRequestProperty("Connection", "close");

       // optional default is GET
       con.setRequestMethod("GET");

       //add request header
       con.setRequestProperty("User-Agent", "Mozilla");

       if (msCookieManager.getCookieStore().getCookies().size() > 0) {
       //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
               con.setRequestProperty(COOKIE,
                               TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
       }

       Map<String, List<String>> headerFields = con.getHeaderFields();
       List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
       if (cookiesHeader != null) {
           for (String cookie : cookiesHeader) {
               msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
           }
       }

       return con.getInputStream();

       }

    public static String sendGibddHttpsPost(String requestURL, String urlParameters) {
        URL url;
        String response = "";
        Integer connectionTimeout = 30000;

        try {
            url = new URL(requestURL);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Connection", "close");
            conn.setReadTimeout(connectionTimeout);
            conn.setConnectTimeout(connectionTimeout);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "  application/x-www-form-urlencoded; charset=UTF-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                            conn.setRequestProperty(COOKIE,
                                            TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
                }

            conn.setFixedLengthStreamingMode(urlParameters.getBytes().length);
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(urlParameters);
            out.close();

            setResponseCode(conn.getResponseCode());

            if (getResponseCode() == HttpsURLConnection.HTTP_OK) {

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                conn.disconnect();
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }

    private static void setResponseCode(int responseCode) {
        WebService.responseCode = responseCode;
    }

    private static int getResponseCode() {
        return responseCode;
    }
}
