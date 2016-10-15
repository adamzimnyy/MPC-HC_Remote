package adamzimny.mpc_hc_remote.util;

import java.util.Map;

/**
 * Created by adamz on 15.10.2016.
 */
public interface MpcUpdateListener {

    void onUpdate(Map<String, String> variables);
    void onVolumeChanged(Map<String, String> variables);
    void onPositionChanged(Map<String, String> variables);
    void onMute(Map<String, String> variables);
    void onStateChanged(Map<String, String> variables);
    void onFileClosed();
    void onDurationChanged(Map<String, String> variables);
}
