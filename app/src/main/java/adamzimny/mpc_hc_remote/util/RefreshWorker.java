package adamzimny.mpc_hc_remote.util;

import adamzimny.mpc_hc_remote.api.MediaPlayerClassicHomeCinema;
import android.os.AsyncTask;
import android.util.Log;

import java.awt.font.NumericShaper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by adamz on 11.10.2016.
 */
public class RefreshWorker {
    MediaPlayerClassicHomeCinema mpc;
    Timer timer;
    MpcUpdateListener listener;

    public RefreshWorker(MediaPlayerClassicHomeCinema mpc, MpcUpdateListener listener) {
        this.mpc = mpc;
        this.listener = listener;
    }

    public void run() {

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            boolean isFileOpen;

            @Override
            public void run() {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, String> variables = mpc.getVariables();
                            //if opened a different file
                            if (!variables.get("file").equals(Variables.file)) {
                                listener.onUpdate(variables);
                                return;
                            }
                            if (Integer.parseInt(variables.get("state")) != Variables.state) {
                                if (Integer.parseInt(variables.get("state")) == -1)
                                    listener.onFileClosed();
                                else
                                    listener.onStateChanged(variables);
                                return;
                            }
                            if (Integer.parseInt(variables.get("volumelevel")) != Variables.volumelevel) {
                                listener.onVolumeChanged(variables);
                            }
                            if (Integer.parseInt(variables.get("position")) != Variables.position) {
                                listener.onPositionChanged(variables);

                            }
                            if (Integer.parseInt(variables.get("duration")) != Variables.duration) {
                                listener.onDurationChanged(variables);

                            }
                            if (Integer.parseInt(variables.get("muted")) != Variables.muted) {
                                listener.onMute(variables);
                            }
                            isFileOpen = true;
                        } catch (IOException e) {
                            if (isFileOpen) listener.onFileClosed();
                            isFileOpen = false;
                        } catch (NumberFormatException ne) {
                            ne.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 200);
    }

    public void stop() {
        timer.cancel();
    }
}
