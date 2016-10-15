package adamzimny.mpc_hc_remote.activity;

import adamzimny.mpc_hc_remote.R;
import adamzimny.mpc_hc_remote.util.Variables;
import adamzimny.mpc_hc_remote.api.Command;
import adamzimny.mpc_hc_remote.api.MediaPlayerClassicHomeCinema;
import adamzimny.mpc_hc_remote.api.TimeCode;
import adamzimny.mpc_hc_remote.api.TimeCodeException;
import adamzimny.mpc_hc_remote.util.helper.DateUtils;
import adamzimny.mpc_hc_remote.util.helper.ImageLoaderHelper;
import adamzimny.mpc_hc_remote.util.helper.ImdbHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

public class RemoteActivity extends AppCompatActivity {

    Map<String, String> variables;
    String ip;
    int port;
    MediaPlayerClassicHomeCinema mpc;
    boolean isPlaying;
    boolean isMuted;

    boolean settingsVisible = true;
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.position_text)
    TextView positionText;

    @BindView(R.id.duration_text)
    TextView durationText;

    @BindView(R.id.play)
    ImageButton playButton;

    @BindView(R.id.mute)
    ImageButton muteButton;

    @BindView(R.id.progress_bar)
    SeekBar progressBar;

    @BindView(R.id.bg_image)
    ImageView backgroundImage;

    @BindView(R.id.poster_small)
    CircularImageView poster;

    @BindView(R.id.volume_bar)
    SeekBar volumeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_remote);
        ButterKnife.bind(this);
        Log.d("title", title == null ? "null" : "not null");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mpc = new MediaPlayerClassicHomeCinema(Variables.ip, Variables.port);
        initView();
    }

    public void initView() {

        ImageLoaderHelper.initialize(this);
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.connect_bg, backgroundImage);
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.connect_bg, poster);
        title.setText(Variables.file);
        title.setSelected(true);
        if (Variables.statestring.equals("Playing")) {
            isPlaying = true;
            playButton.setImageResource(R.drawable.pause);
            progressBar.setProgress(Variables.position * 100 / Variables.duration);
        } else {
            isPlaying = false;
            playButton.setImageResource(R.drawable.play);
        }
        isMuted = Variables.muted == 1;
        Log.d("muted", " Variables.muted = " + Variables.muted);
        Log.d("muted", " isMuted = " + isMuted);
        if (!isMuted) {
            Log.d("muted", " setting icon to volume_up");

            muteButton.setImageResource(R.drawable.volume_up);
        } else {
            Log.d("muted", "  setting icon to mute");
            muteButton.setImageResource(R.drawable.mute);
        }
        volumeBar.setProgress(Variables.volumelevel);

        if (Variables.file != null && !Variables.file.isEmpty() && !Variables.file.equals("N/A")) {
            PosterTask task = new PosterTask();
            task.execute(Variables.file);
        }

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                final TimeCode[] tc = new TimeCode[1];
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            mpc.setVolume(seekBar.getProgress());
                            Variables.volumelevel = seekBar.getProgress();
                        } catch (IOException e) {
                            Log.e("mpc excetion", e.getLocalizedMessage());
                        }
                    }
                });

            }
        });
        title.setText(Variables.file);
        volumeBar.setEnabled(!isMuted);
        progressBar.setMax(Variables.duration);
        positionText.setText(Variables.positionstring);
        durationText.setText(Variables.durationstring);
        progressBar.setProgress(Variables.position);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                final TimeCode[] tc = new TimeCode[1];
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tc[0] = new TimeCode(seekBar.getProgress() / 1000);
                            //  TimeCode tc = new TimeCode(100);
                            Variables.position = seekBar.getProgress();
                            mpc.seek(tc[0]);
                        } catch (IOException e) {
                            Log.e("mpc exception", "shit");
                        } catch (TimeCodeException e) {
                            e.printStackTrace();
                        }
                    }
                });

                positionText.setText(DateUtils.secondsToTime(seekBar.getProgress() / 1000));
            }

        });
    }

    @OnClick(R.id.disconnect)
    public void disconnect() {
        finish();
    }

    @OnClick(R.id.fullscreen)
    public void fullscreen() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.toggleFullscreen();
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
    }

    @OnClick(R.id.sub_delay_down)
    public void subDelayDown() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.execute(Command.SUBTITLE_DELAY_MINUS);
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
    }

    @OnClick(R.id.sub_delay_up)
    public void subDelayUp() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.execute(Command.SUBTITLE_DELAY_PLUS);
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
    }

    @OnClick(R.id.mute)
    public void mute() {
        Log.d("muted", "Mute button pressed");

        if (!isMuted) {
            Log.d("muted", " setting icon to volume_up");

            muteButton.setImageResource(R.drawable.mute);
        } else {
            Log.d("muted", "  setting icon to mute");
            muteButton.setImageResource(R.drawable.volume_up);
        }
        isMuted = !isMuted;
        Variables.muted = isMuted ? 1 : 0;
        Log.d("muted", "new values, isMuted = " + isMuted + ", Variables.muted = " + Variables.muted);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.toggleMute();
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
        volumeBar.setEnabled(!isMuted);
    }

    @OnClick(R.id.previous_file)
    public void previousFile() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.execute(Command.PREVIOUS_FILE);
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
    }

    @OnClick(R.id.jump_back)
    public void jumpBack() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                  //  mpc.seek(new TimeCode(Variables.position / 1000 - 10));
                    mpc.jump(-10);
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                } catch (TimeCodeException e) {
                    e.printStackTrace();
                }
            }
        });
        //TODO warunek na 0 lub 100%
        Variables.position -= 10 * 1000;
        positionText.setText(DateUtils.secondsToTime(Variables.position / 1000));
        progressBar.setProgress(Variables.position);
    }

    @OnClick(R.id.play)
    public void play() {

        if (!isPlaying) {
            playButton.setImageResource(R.drawable.pause);
        } else {
            playButton.setImageResource(R.drawable.play);

        }
        isPlaying = !isPlaying;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.togglePlayPause();
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
    }

    @OnClick(R.id.jump_forward)
    public void jumpForward() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                  //  mpc.seek(new TimeCode(Variables.position / 1000 + 10));
                    mpc.jump(10);
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                } catch (TimeCodeException e) {
                    e.printStackTrace();
                }
            }
        });
        //TODO warunek na 0 lub 100%
        Variables.position += 10 * 1000;
        positionText.setText(DateUtils.secondsToTime(Variables.position / 1000));
        progressBar.setProgress(Variables.position);
    }

    @OnClick(R.id.next_file)
    public void nextFile() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.execute(Command.NEXT_FILE);
                } catch (IOException e) {
                    Log.e("mpc excetion", e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class PosterTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {

            return ImdbHelper.findPoster(Variables.file);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            ImageLoaderHelper.initialize(RemoteActivity.this);
            if (result.isEmpty()) {
                title.setText(Variables.file);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.default_poster, backgroundImage);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.default_poster, poster);
            } else {
                title.setText(Variables.title);
                ImageLoader.getInstance().displayImage(result, backgroundImage);
                ImageLoader.getInstance().displayImage(result, poster);

            }
        }
    }
}
