package adamzimny.mpc_hc_remote.activity;

import adamzimny.mpc_hc_remote.R;
import adamzimny.mpc_hc_remote.api.*;
import adamzimny.mpc_hc_remote.util.*;
import adamzimny.mpc_hc_remote.util.helper.ImageLoaderHelper;
import adamzimny.mpc_hc_remote.util.helper.ImdbHelper;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RemoteActivity extends AppCompatActivity implements MpcUpdateListener {

    Map<String, String> variables;
    String ip;
    int port;
    MediaPlayerClassicHomeCinema mpc;
    boolean isPlaying;
    boolean isMuted;

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
    RefreshWorker worker;
    PosterTask posterTask;

    @BindView(R.id.file_browser_recycler)
    RecyclerView fileBrowser;
    FastItemAdapter<FileItem> adapter;

    @BindView(R.id.swipe_layout)
    SwipeRevealLayout swipeLayout;

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
        worker = new RefreshWorker(mpc, this);
        worker.run();

        adapter = new FastItemAdapter<>();
        fileBrowser.setAdapter(adapter);
        fileBrowser.setLayoutManager(new LinearLayoutManager(this));


        adapter.withOnClickListener(new FastAdapter.OnClickListener<FileItem>() {
            @Override
            public boolean onClick(View v, IAdapter<FileItem> adapter, FileItem item, int position) {
                if (item.getInfo().isDirectory())
                    browse(item.getInfo());
                else
                    openFile(item.getInfo());
                return false;
            }

        });
        initFileBrowser();
    }


    private void initFileBrowser() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                        }
                    });
                    final List<FileInfo> files = mpc.browse();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (FileInfo file : files) {
                                Log.d("browser", "file " + file.getFileName());
                                adapter.add(new FileItem(file));

                            }
                            adapter.notifyDataSetChanged();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void browse(final FileInfo item) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                        }
                    });
                    final List<FileInfo> files = mpc.browse(item);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (FileInfo file : files) {
                                Log.d("browser", "file " + file.getFileName());

                                adapter.add(new FileItem(file));


                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void openFile(final FileInfo item) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.openFile(item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (swipeLayout.isOpened()) {
            swipeLayout.close(true);
            return;
        }


        disconnect();
        if (posterTask != null) {
            posterTask.cancel(true);
        }
    }

    public void initView() {

        ImageLoaderHelper.initialize(this);
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2, backgroundImage);
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2, poster);
        title.setText(Variables.file);
        title.setSelected(true);
        if ("Playing".equals(Variables.statestring)) {
            isPlaying = true;
            playButton.setImageResource(R.drawable.pause);
        } else {
            isPlaying = false;
            playButton.setImageResource(R.drawable.play);
        }
        isMuted = Variables.muted == 1;
        muteButton.setImageResource(isMuted ? R.drawable.mute : R.drawable.volume_up);
        volumeBar.setProgress(Variables.volumelevel);
        if (Variables.file != null && !Variables.file.isEmpty() && !Variables.file.equals("N/A")) {
            posterTask = new PosterTask();
            posterTask.execute(Variables.file);
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
                            Log.e("mpc excetion","message: " + e.getLocalizedMessage());
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
        Log.d("worker", "duration string is " + Variables.durationstring + " vs " + durationText.getText().toString());
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
                            Variables.position = seekBar.getProgress();
                            mpc.seek(tc[0]);
                        } catch (IOException e) {
                            Log.e("mpc exception", "message: " +e.getLocalizedMessage());
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
        worker.stop();
        Variables.reset();
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
                    Log.e("mpc excetion","message: " + e.getLocalizedMessage());
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
                    Log.e("mpc excetion","message: " + e.getLocalizedMessage());
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
                    Log.e("mpc excetion", "message: " +e.getLocalizedMessage());
                }
            }
        });
    }

    @OnClick(R.id.mute)
    public void mute() {
     /*   Log.d("muted", "Mute button pressed");

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
*/
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.toggleMute();
                } catch (IOException e) {
                    Log.e("mpc excetion","message: " + e.getLocalizedMessage());
                }
            }
        });
        //  volumeBar.setEnabled(!isMuted);
    }

    @OnClick(R.id.previous_file)
    public void previousFile() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.execute(Command.PREVIOUS_FILE);
                } catch (IOException e) {
                    Log.e("mpc excetion","message: " + e.getLocalizedMessage());
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
                    Log.e("mpc excetion","message: " + e.getLocalizedMessage());
                } catch (TimeCodeException e) {
                    e.printStackTrace();
                }
            }
        });
        //TODO warunek na 0 lub 100%
     /*   Variables.position -= 10 * 1000;
        positionText.setText(DateUtils.secondsToTime(Variables.position / 1000));
        progressBar.setProgress(Variables.position);*/
    }

    @OnClick(R.id.play)
    public void play() {
/*
        if (!isPlaying) {
            playButton.setImageResource(R.drawable.pause);
        } else {
            playButton.setImageResource(R.drawable.play);

        }
        isPlaying = !isPlaying;*/
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mpc.togglePlayPause();
                } catch (IOException e) {
                    Log.e("mpc excetion","message: " + e.getLocalizedMessage());
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
                    Log.e("mpc excetion","message: " +e.getLocalizedMessage());
                } catch (TimeCodeException e) {
                    e.printStackTrace();
                }
            }
        });
     /*   //TODO warunek na 0 lub 100%
        Variables.position += 10 * 1000;
        positionText.setText(DateUtils.secondsToTime(Variables.position / 1000));
        progressBar.setProgress(Variables.position);*/
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

    @Override
    public void onUpdate(Map<String, String> map) {
        Log.d("worker", "onUpdate" + map.get("durationstring"));
        Variables.readMap(map);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initView();
            }
        });
        initFileBrowser();
    }


    @Override
    public void onVolumeChanged(Map<String, String> variables) {
        final int volume = Integer.parseInt(variables.get("volumelevel"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                volumeBar.setProgress(volume);
            }
        });
        Variables.volumelevel = volume;
    }

    @Override
    public void onPositionChanged(final Map<String, String> variables) {
        final int pos = Integer.parseInt(variables.get("position"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                positionText.setText(variables.get("positionstring"));
                progressBar.setProgress(pos);
            }
        });
        Variables.position = pos;
        Variables.positionstring = variables.get("positionstring");
    }

    @Override
    public void onMute(Map<String, String> variables) {
        isMuted = Integer.parseInt(variables.get("muted")) == 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                volumeBar.setEnabled(!isMuted);
                if (isMuted) {
                    muteButton.setImageResource(R.drawable.mute);
                } else {
                    muteButton.setImageResource(R.drawable.volume_up);
                }
            }
        });

        Variables.muted = isMuted ? 1 : 0;

    }

    @Override
    public void onStateChanged(Map<String, String> variables) {
        String state = variables.get("statestring");
        if (state.equals("Playing")) {
            isPlaying = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playButton.setImageResource(R.drawable.pause);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isPlaying = false;
                    playButton.setImageResource(R.drawable.play);
                }
            });

        }
        Variables.statestring = state;
        Variables.state = Integer.parseInt(variables.get("state"));
    }

    @Override
    public void onFileClosed() {
        Log.d("worker", "onFileClosed");
        Variables.reset();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2, backgroundImage);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2, poster);
                title.setText("");
                positionText.setText("00:00:00");
                progressBar.setProgress(0);
                durationText.setText("00:00:00");
            }
        });
    }

    @Override
    public void onDurationChanged(final Map<String, String> variables) {
        final int dur = Integer.parseInt(variables.get("duration"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                durationText.setText(variables.get("durationstring"));
                progressBar.setMax(dur);
            }
        });
        Variables.duration = dur;
        Variables.positionstring = variables.get("durationstring");
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
                Variables.poster = "default";
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2, backgroundImage);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2,poster);
            } else {

                if(!result.equalsIgnoreCase(Variables.poster)) {
                    title.setText(Variables.title);
                    Variables.poster = result;
                    ImageLoader.getInstance().displayImage(result, backgroundImage);
                    ImageLoader.getInstance().displayImage(result, poster);
                }
            }
        }
    }
}
