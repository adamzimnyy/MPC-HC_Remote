package adamzimny.mpc_hc_remote;

import android.content.res.Configuration;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by adamz on 18.08.2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
