package adamzimny.mpc_hc_remote.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by AKiniyalocts on 2/23/15.
 *
 */
public class IntentHelper {
    public final static int FILE_PICK = 1001;
    public final static int GPS_SETTINGS = 1002;
    public static final int PERMISSION_GPS = 1003;

    public static void chooseFileIntent(Activity activity){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, FILE_PICK);
    }

    public static void startActivityIntent(Activity source, Class<? extends Activity> nextActivity){
        Intent  i = new Intent(source,nextActivity);
        source.startActivity(i);
    }

    public static void startActivityIntent(Activity source, Class<? extends Activity> nextActivity, Bundle bundle){
        Intent  i = new Intent(source,nextActivity);
        i.putExtras(bundle);
        source.startActivity(i);
    }
}