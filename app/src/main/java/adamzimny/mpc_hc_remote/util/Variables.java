package adamzimny.mpc_hc_remote.util;

import android.util.Log;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by adamz on 01.10.2016.
 */
public class Variables {
    public static String ip;
    public static int port;
    public static String file;
    public static String filepatharg;
    public static String filepath;
    public static String filedirarg;
    public static String filedir;
    public static int state;
    public static String statestring;
    public static int position;
    public static String positionstring;
    public static int duration;
    public static String durationstring;
    public static int volumelevel;
    public static int muted;
    public static int playbackrate;
    public static int reloadtime;
    public static String imdb;
    public static String title;
    public static String poster;

    public static void readMap(Map<String, String> map) {
        Log.d("updater","readMap "+new JSONObject(map).toString());
        duration = Integer.parseInt(map.get("duration"));
        file = map.get("file");
        durationstring = map.get("durationstring");
        filepatharg = map.get("filepatharg");
        filepath = map.get("filepath");
        filedirarg = map.get("filedirarg");
        filedir = map.get("filedir");
        state = Integer.parseInt(map.get("state"));
        statestring = map.get("statestring");
        position = Integer.parseInt(map.get("position"));
        positionstring = map.get("positionstring");
        duration = Integer.parseInt(map.get("duration"));
        durationstring = map.get("durationstring");
        volumelevel = Integer.parseInt(map.get("volumelevel"));
        muted = Integer.parseInt(map.get("muted"));
        playbackrate = Integer.parseInt(map.get("playbackrate"));
        reloadtime = Integer.parseInt(map.get("reloadtime"));

    }


    public static void reset() {
        Log.d("updater","RESET!!!!!!!!");
        duration = 0;
        file = null;
        durationstring = null;
        filepatharg = null;
        filepath = null;
        filedirarg =null;
        filedir = null;
        state = 0;
        statestring = null;
        position =0;
        positionstring =null;
        duration =0;
        durationstring = null;
        volumelevel =0;
        muted =0;
        playbackrate =0;
        reloadtime =0;
        poster = "default";
    }
}
