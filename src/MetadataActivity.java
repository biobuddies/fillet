package es.biobuddi.fillet;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Show typed text on a car stereo as AVRCP track metadata, after
 * https://gist.github.com/davidalbers/953eb949314fd6607616. Android picks the media session
 * whose app is playing audio, so loop silence while the session is active.
 */
public class MetadataActivity extends Activity {

    private AudioTrack silence;
    private MediaSession session;
    private EditText album, artist, device, track;

    private EditText addRow(LinearLayout layout, String hint) {
        var row = new EditText(this);
        row.setFilters(new InputFilter[] { new InputFilter.LengthFilter(37) }); // 2011 Odyssey
        row.setHint(hint);
        row.setSingleLine();
        layout.addView(row);
        return row;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        var layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        // Android 15 draws edge to edge, under the status bar, so pad by system bar insets
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            var bars = insets.getInsets(WindowInsets.Type.ime() | WindowInsets.Type.systemBars());
            view.setPadding(bars.left + 48, bars.top + 48, bars.right + 48, bars.bottom + 48); // px
            return WindowInsets.CONSUMED;
        });
        device = addRow(layout, "Device");
        album = addRow(layout, "Album");
        artist = addRow(layout, "Artist");
        track = addRow(layout, "Track");
        var button = new Button(this);
        button.setText("Set");
        button.setOnClickListener(view -> set());
        layout.addView(button);
        setContentView(layout);

        // A callback makes the session look like a media player
        session = new MediaSession(this, "Fillet");
        session.setCallback(new MediaSession.Callback() {});
        session.setPlaybackState(
            new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, 0, 1, SystemClock.elapsedRealtime())
                .build()
        );
        var attributes = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();
        var frames = 44100; // 1 s
        silence = new AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(
                new AudioFormat.Builder()
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(frames)
                    .build()
            )
            .setBufferSizeInBytes(frames * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build();
        silence.write(new short[frames], 0, frames);
        silence.setLoopPoints(0, frames, -1);
        getSystemService(AudioManager.class).requestAudioFocus(
            new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes)
                .build()
        );
    }

    @Override
    protected void onDestroy() {
        silence.release();
        session.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) set();
    }

    private void set() {
        var name = device.getText().toString();
        if (!name.isEmpty()) {
            if (
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 0);
                return;
            }
            if (!getSystemService(BluetoothManager.class).getAdapter().setName(name)) {
                Toast.makeText(this, "Device rename failed", Toast.LENGTH_SHORT).show();
            }
        }
        session.setMetadata(
            new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_ALBUM, album.getText().toString())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist.getText().toString())
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.getText().toString())
                .build()
        );
        session.setActive(true);
        silence.play();
    }
}
