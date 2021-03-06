package adamzimny.mpc_hc_remote.api;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Client for the web interface exposed by MPC-HC.
 */
public class MediaPlayerClassicHomeCinema {
    /**
     * A pair of {@link String}s.
     */
    public static class KeyValuePair {
        private final String key;
        private final String value;

        /**
         * Create a new instance.
         *
         * @param key   The key
         * @param value The value
         */
        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }


    private String baseURI;
    private String commandEndPoint;
    private String infoEndPoint;
    private String variableEndPoint;

    private OkHttpClient okHttpClient;

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("« (MPC-HC v\\d+.\\d+.\\d+.\\d+).*");
    private static final String BROWSER_ROOT_URI_PATH = "/browser.html";

    private static final String VARIABLE_IDS[] = new String[]{
            "file", "filepatharg", "filepath", "filedirarg", "filedir", "state",
            "statestring", "position", "positionstring", "duration",
            "durationstring", "volumelevel", "muted", "playbackrate",
            "reloadtime"
    };

    /**
     * Construct a new instance of the media player.
     *
     * @param hostname The hostname or IP address of the machine that MPC-HC is running on
     * @param port     The port that MPC-HC is listening on
     */
    public MediaPlayerClassicHomeCinema(String hostname, int port) {
        baseURI = "http://" + hostname + ":" + Integer.toString(port);
        commandEndPoint = baseURI + "/command.html";
        infoEndPoint = baseURI + "/info.html";
        variableEndPoint = baseURI + "/variables.html";
        okHttpClient = new OkHttpClient();

        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(500, TimeUnit.MILLISECONDS);
        b.writeTimeout(500, TimeUnit.MILLISECONDS);
        b.connectTimeout(500, TimeUnit.MILLISECONDS);
        okHttpClient = b.build();
    }

    /**
     * Get a list of {@link FileInfo} objects for the system root.
     * This is a listing of all attached drives, e.g. A:\, C:\, D:\.
     *
     * @return The file listing for the system root
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public List<FileInfo> browse() throws IOException {
        return browse(BROWSER_ROOT_URI_PATH);
    }

    /**
     * Get a list of {@link FileInfo} objects for the given path.
     *
     * @param file The FileInfo object from the FileTable
     * @return A list of {@link FileInfo} objects for the given path
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public List<FileInfo> browse(FileInfo file) throws IOException {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Argument must be a directory");
        }
        return browse(file.getHref());
    }

    /**
     * Get a list of {@link FileInfo} objects for the given path.
     *
     * @param href The relative path to the file, as returned by
     *             {@link FileInfo#getHref()}
     * @return A list of {@link FileInfo} objects for the given path
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    List<FileInfo> browse(String href) throws IOException {
        Response response = get(baseURI + href);
        Document document = Jsoup.parse(response.body().string());

        Element table = document.getElementsByClass("browser-table").get(1);
        return FileInfo.fromHTMLTableElement(table);
    }

    /**
     * Open the file pointed to by the href with MPC-HC.
     *
     * @param file The {@link FileInfo} to open
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void openFile(FileInfo file) throws IOException {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Argument must be a file");
        }
        get(baseURI + file.getHref());
    }

    /**
     * Send the command to MPC-HC.
     *
     * @param command The command to execute
     * @param args    Any additional key value pairs to add to the form
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void execute(final Command command,
                        KeyValuePair... args) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("wm_command", Integer.toString(command.getValue()));
        for (KeyValuePair arg : args) {
            formBuilder.add(arg.getKey(), arg.getValue());
        }

        Request request = new Request.Builder()
                .url(commandEndPoint)
                .post(formBuilder.build())
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code: " + response);
        }
    }

    /**
     * Get the info string from MPC-HC.
     *
     * @return The info string
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public String getInfo() throws IOException {
        Response response = get(infoEndPoint);
        Document document = Jsoup.parse(response.body().string());
        Element element = document.getElementById("mpchc_np");
        return element.text();
    }

    /**
     * Get a map of all web interface exposed MPC-HC variables.
     *
     * @return The variables
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public Map<String, String> getVariables() throws IOException {
        Response response = get(variableEndPoint);

        final String html = response.body().string();
        Document document = Jsoup.parse(html);
        Map<String, String> variables = new HashMap<String, String>();
        for (String variableID : VARIABLE_IDS) {
            String value = document.getElementById(variableID).text();
            variables.put(variableID, value);
        }

        return variables;
    }

    /**
     * Perform an HTTP GET request for a URL.
     *
     * @param url The URL to GET
     * @return The response
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    private Response get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code: " + response);
        }

        return response;
    }


    //
    // Convenience methods
    //


    /**
     * Toggle the mute state of the player.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void toggleMute() throws IOException {
        execute(Command.VOLUME_MUTE);
    }

    /**
     * Returns true if the player is muted.
     *
     * @return True if the player is muted
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public boolean isMuted() throws IOException {
        return getVariables().get("muted").equals("1");
    }

    /**
     * Set mute on the player.
     *
     * @param mute If {@code true}, mute the player. If {@code false}, unmute.
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void setMute(boolean mute) throws IOException {
        boolean isMuted = isMuted();
        if ((isMuted && !mute) || (!isMuted && mute)) {
            toggleMute();
        }
    }

    /**
     * Returns true if the player is playing.
     *
     * @return True if the player is playing
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public boolean isPlaying() throws IOException {
        return getVariables().get("statestring").equals("Playing");
    }

    /**
     * Returns true if the player is paused.
     *
     * @return True if the player is paused
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public boolean isPaused() throws IOException {
        return getVariables().get("statestring").equals("Paused");
    }

    /**
     * Returns true if the player is stopped.
     *
     * @return True if the player is stopped
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public boolean isStopped() throws IOException {
        return getVariables().get("statestring").equals("Stopped");
    }

    /**
     * Returns true if no file is open.
     *
     * @return True if no file is open
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public boolean isClosed() throws IOException {
        return getVariables().get("state").equals("-1");
    }

    /**
     * Play the current video.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void play() throws IOException {
        execute(Command.PLAY);
    }

    /**
     * Pause the current video.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void pause() throws IOException {
        execute(Command.PAUSE);
    }

    /**
     * Toggle between play and pause.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void togglePlayPause() throws IOException {
        execute(Command.PLAY_PAUSE);
    }

    /**
     * Stop the current video.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void stop() throws IOException {
        execute(Command.STOP);
    }

    /**
     * Close the currently playing file. This does <b>not</b> end the process.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void close() throws IOException {
        execute(Command.CLOSE);
    }

    /**
     * Get the current volume level. Note that this can be non-zero, but
     * {@link #isMuted()} may still return true. Muting is independent of
     * the current volume level.
     *
     * @return The current volume level
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public int getVolume() throws IOException {
        return Integer.valueOf(getVariables().get("volumelevel"));
    }

    /**
     * Set the volume (0 to 100). Values outside of this range are clamped.
     *
     * @param volume The volume to set
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void setVolume(int volume) throws IOException {
        if (volume < 0) {
            volume = 0;
        } else if (volume > 100) {
            volume = 100;
        }

        execute(Command.SET_VOLUME,
                new KeyValuePair("volume", Integer.toString(volume)));
    }

    /**
     * Seek to the given time code. If the give time is larger than the duration
     * of the video, seek to the end.
     *
     * @param where Where to seek
     * @throws IOException       If the HTTP call receives an unexpected response code
     * @throws TimeCodeException This should never happen
     */
    public void seek(TimeCode where) throws IOException, TimeCodeException {
        if (where.getTotalSeconds() > getDuration().getTotalSeconds()) {
            where = getDuration();
        }
        execute(Command.SEEK,
                new KeyValuePair("position", where.toString()));
    }

    /**
     * Seek to the start of the video.
     *
     * @throws IOException       If the HTTP call receives an unexpected response code
     * @throws TimeCodeException This should never happen
     */
    public void seekToStart() throws IOException, TimeCodeException {
        seek(TimeCode.START);
    }

    /**
     * Seek from the current position. A positive value will seek forward, and a
     * negative value will seek backward.
     *
     * @param seconds How far and in what direction to seek
     * @throws IOException       If the HTTP call receives an unexpected response code
     * @throws TimeCodeException This should never happen
     */
    public void jump(int seconds) throws IOException, TimeCodeException {
        TimeCode destination;
        if (seconds >= 0) {
            destination = TimeCode.plus(getPosition(), new TimeCode(seconds));
        } else {
            try {
                destination = TimeCode.minus(getPosition(), new TimeCode(Math.abs(seconds)));
            } catch (TimeCodeException e) {
                // Invalid attempt to seek to before the video started (negative time code)
                destination = TimeCode.START;
            }
        }

        seek(destination);
    }

    /**
     * Toggle fullscreen.
     *
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public void toggleFullscreen() throws IOException {
        execute(Command.FULLSCREEN);
    }

    /**
     * Get the current position in the video.
     *
     * @return The current position
     * @throws IOException       If the HTTP call receives an unexpected response code
     * @throws TimeCodeException This should never happen
     */
    public TimeCode getPosition() throws IOException, TimeCodeException {
        return new TimeCode(getVariables().get("positionstring"));
    }

    /**
     * Get the duration of the current video.
     *
     * @return The duration of the current video
     * @throws IOException       If the HTTP call receives an unexpected response code
     * @throws TimeCodeException This should never happen
     */
    public TimeCode getDuration() throws IOException, TimeCodeException {
        return new TimeCode(getVariables().get("durationstring"));
    }

    /**
     * Get the version string (MPC-HC vX.X.X.X).
     *
     * @return The version string
     * @throws IOException If the HTTP call receives an unexpected response code
     */
    public String getVersion() throws IOException {
        Matcher matcher = VERSION_PATTERN.matcher(getInfo());
        return matcher.group(1);
    }
}
