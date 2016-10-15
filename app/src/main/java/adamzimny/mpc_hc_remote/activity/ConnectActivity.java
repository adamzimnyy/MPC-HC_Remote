package adamzimny.mpc_hc_remote.activity;

import adamzimny.mpc_hc_remote.util.helper.IntentHelper;
import adamzimny.mpc_hc_remote.util.helper.ImageLoaderHelper;
import adamzimny.mpc_hc_remote.util.StringUtil;
import adamzimny.mpc_hc_remote.util.Variables;
import adamzimny.mpc_hc_remote.api.MediaPlayerClassicHomeCinema;
import adamzimny.mpc_hc_remote.util.helper.ImdbHelper;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adamzimny.mpc_hc_remote.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * A login screen that offers login via email/password.
 */
public class ConnectActivity extends AppCompatActivity {


    private ConnectTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView IPView;
    private EditText portView;
    private View mProgressView;

    @BindView(R.id.connect_bg)
    ImageView bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);

        //    test();

        ImageLoaderHelper.initialize(this);
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.bg2, bg);
        // Set up the login form.
        IPView = (AutoCompleteTextView) findViewById(R.id.ip);
        Log.d("replace", "S03E15".replace("\\^[0-9]{2}E[0-9]{2}", ""));
        portView = (EditText) findViewById(R.id.port);
        portView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    connect();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.connect_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    private void test() {
        final String[] inputs = new String[]{
                "jane.the.virgin.118.hdtv-lol",
                "Marvels.Luke.Cage.S01E04.WEBRip.x264-SKGTV[rarbg]",
                "Jane.The.Virgin.S01E06.HDTV.x264-KILLERS",
                "Quantico.S02E01.HDTV.x264-LOL[ettv]",
                "Marvels.Agents.of.S.H.I.E.L.D.S04E01.HDTV.x264-KILLERS[ettv]",
                "containment.111.hdtv-lol[ettv]",
                "Mr.Robot.S01E06.720p.HDTV.x264-IMMERSE[rarbg]",
                "Mr.Robot.S02E02.720p.HDTV.x264-AVS[rarbg]",
                "Mechanic.Resurrection.2016.720p.HDRip.KORSUB.XviD.MP3-STUTTERSHIT",
                "Mechanic.Resurrection.2016.720p.HDRip.KORSUB.XviD.MP3-STUTTERSHIT",
                "The.Way.Way.Back.2013.1080p.BluRay.x264.YIFY",
                "Moonrise Kingdom (2012)",
                "Ender's Game (2013) [1080p]",
                "Batman.v.Superman.Dawn.of.Justice.2016.Ultimate.Edition.1080p.WEB-DL.DD5.1.H264-RARBG",
                "X-Men.Days.of.Future.Past.2014.THE.ROGUE.CUT.720p.BluRay.H264.AAC-RARBG",
                "Me.Before.You.2016.1080p.BluRay.H264.AAC-RARBG"
        };
        AsyncTask.execute(new Runnable() {
                              @Override
                              public void run() {
                                  for (String s : inputs) {
                                      Log.d("poster", "Found poster is " + ImdbHelper.findPoster(StringUtil.getTitleFromFileName(s)));
                                  }
                              }
                          }
        );

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void connect() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        IPView.setError(null);
        portView.setError(null);

        // Store values at the time of the login attempt.
        String email = IPView.getText().toString().trim();
        String port = portView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(port) && !isPortValid(port)) {
            portView.setError("This is not a valid port");
            focusView = portView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            IPView.setError(getString(R.string.error_field_required));
            focusView = IPView;
            cancel = true;
        } else if (!isPIValid(email)) {
            IPView.setError("This is not a valid IP address");
            focusView = IPView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new ConnectTask(email, Integer.parseInt(port));
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPIValid(String ip) {
        Pattern pattern;
        Matcher matcher;
        String IPADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        pattern = Pattern.compile(IPADDRESS_PATTERN);
        matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private boolean isPortValid(String password) {
        try {
            int port = Integer.parseInt(password);
            if (port > 65536 || port <= 0) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE
            );
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private final String ip;
        private final int port;

        ConnectTask(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                MediaPlayerClassicHomeCinema mpc = new MediaPlayerClassicHomeCinema(ip, port);
                mpc.getInfo();
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Variables.ip = ip;
                Variables.port = port;
                IntentHelper.startActivityIntent(ConnectActivity.this, RemoteActivity.class);
            } else {
                portView.setError("Cannot connect.");
                portView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

