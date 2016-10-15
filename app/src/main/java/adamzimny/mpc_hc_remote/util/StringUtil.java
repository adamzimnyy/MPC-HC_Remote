package adamzimny.mpc_hc_remote.util;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adamz on 01.10.2016.
 */
public class StringUtil {

    public static final String[] releases = new String[]{
            "rartv", "yify", "ettv", "eztv", "x264", "BRRip", "1080p", "DTS-JYK", "KORSUB", "XviD",
            "STUTTERSHIT", "HDRip", "720p", "AAC", "H264", "ETRG", "SPARKS", "HDTV",
            "WEBRip", "LOL", "PROPER", "REPACK", "EXTENDED", "BluRay", "KILLERS", "IMMERSE", "AVS",
            "ASAP", "WEB-DL", "DD5.1", "rarbg", "extended", "extended.cut", "extended.edition", "utlimate.edition",
            "JYK", "6CH", "Shaanig", "yukinomati", "hevc", "AC3-EVO", "ethd", "skgtv", "x265"
    };

    public static final String[] extensions = new String[]{"mp3", "mp4", "wma", "avi", "mkv", "flv", "gifv", "mov", "wmv", "vob", "ogg", "webm", "mpg", "mpeg", "rmvb", "flv", "3gp"};

    public static String getTitleFromFileName(String filename) {
        filename = filename.toLowerCase();
        for (String s : releases) {
            if (filename.contains(s.toLowerCase())) {
                filename = filename.replace(s.toLowerCase(), "");
            }
        }

        for (String s : extensions) {
            if (filename.contains(s.toLowerCase())) {
                filename = filename.replace(s.toLowerCase(), "");
            }
        }

        filename = filename.replaceAll("[sS][0-9]{2}[eE][0-9]{2}", "");
        String[] parts = filename.split("[\\.\\-\\_\\[\\]\\(\\)]");
        StringBuilder sb = new StringBuilder();
        for (String s : parts) {
            if (!s.trim().isEmpty()) {
                try {
                    int i = Integer.parseInt(s);
                    if (i < 1950 || i > 2050) {
                        continue;
                    }
                    if (i > 100 && i < 999) {
                        //TODO probably is a tv show
                    }
                } catch (NumberFormatException e) {
                }


                sb.append(s).append(" ");
            }
        }
        Log.d("poster", "Title from filename " + filename + " is " + sb.toString());
        return sb.toString();
    }
}
