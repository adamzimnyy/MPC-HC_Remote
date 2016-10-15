package adamzimny.mpc_hc_remote.util.helper;

import adamzimny.mpc_hc_remote.util.StringUtil;
import adamzimny.mpc_hc_remote.util.Variables;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adamz on 02.10.2016.
 */
public class ImdbHelper {

    public static String findPoster(String filename) {
        String title = StringUtil.getTitleFromFileName(filename) + " imdb";
        if (isTvSeries(filename)) {
            title = title + " TV Series ";
        }
        String imdbLink = getImdbFromGoogle(title + " site:imdb.com");
        Variables.imdb = imdbLink;
        String poster = getPosterFromImdb(imdbLink);
        Variables.title = getTitleFromImdb(imdbLink);
        if (poster.contains("@")) {
            int lastPos = poster.lastIndexOf("@");
            String before = poster.substring(0, lastPos);
            return before + "@.jpg";
        }
        return poster;
    }

    public static boolean isTvSeries(String filename) {
        if (filename == null) return false;
        boolean result = false;
        Log.d("filename", filename);
        Pattern p = Pattern.compile("[sS][0-9]{2}[eE][0-9]{2}");
        Matcher m = p.matcher(filename);
        if (m.find()) {
            Log.d("filename", "Found " + m.group());
            result = true;
        }
        p = Pattern.compile("[0-9]+x[0-9]{2}");
        m = p.matcher(filename);

        if (m.find()) {
            Log.d("filename", "Found " + m.group());
            result = true;
        }
        p = Pattern.compile("\\.[0-9]{3}\\.");
        m = p.matcher(filename);

        if (m.find()) {
            Log.d("filename", "Found " + m.group());
            result = true;
        }
        return result;
    }

    private static String getPosterFromImdb(String imdbLink) {
        if (imdbLink.isEmpty()) return "";
        Log.d("replace", "imdb link is " + imdbLink);
        try {
            Document doc = Jsoup
                    .connect(imdbLink)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();

            Element image = doc.select("link[rel=image_src]").first();
            return image.attr("href");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getImdbFromGoogle(String query) {

        String request = "https://www.bing.com/search?q=" + query.replace(" ", "%20") + "&num=20";
        Log.d("poster", "initial query is " + request);
        try {

            // need http protocol, set this as a Google bot agent :)
            Document doc = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(10000).get();

            // get all links
            Elements links = doc.select("a[href*=imdb.com/title/]");
            for (Element link : links) {

                String temp = link.attr("href");
                if (temp.contains("title/tt")) {
                    Log.d("poster", "returning " + temp.replace("/url?q=", "").substring(0, 35) + " from " + query);
                    return temp.replace("/url?q=", "").substring(0, 35);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String getTitleFromImdb(String imdbLink) {
        if (imdbLink.isEmpty()) return "";
        Log.d("replace", "imdb link is " + imdbLink);
        try {
            Document doc = Jsoup
                    .connect(imdbLink)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();

            Element title = doc.select("meta[name=title]").first();
            return title.attr("content").replace(" - IMDb", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
