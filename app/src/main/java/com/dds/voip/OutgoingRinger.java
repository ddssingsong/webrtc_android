package com.dds.voip;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dds.webrtc.R;

import java.io.IOException;

/**
 * Created by dds on 2019/8/21.
 * android_shuai@163.com
 */
public class OutgoingRinger {
    private static final String TAG = OutgoingRinger.class.getSimpleName();

    public enum Type {
        RINGING,
        BUSY
    }

    private final Context context;

    private MediaPlayer mediaPlayer;

    public OutgoingRinger(@NonNull Context context) {
        this.context = context;
    }

    public void start(Type type) {
        int soundId;
        if (type == Type.RINGING) soundId = R.raw.wr_ringback;
        else if (type == Type.BUSY) soundId = R.raw.wr_ringback;
        else throw new IllegalArgumentException("Not a valid sound type");

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        mediaPlayer.setLooping(true);

        String packageName = context.getPackageName();
        Uri dataUri = Uri.parse("android.resource://" + packageName + "/" + soundId);

        try {
            mediaPlayer.setDataSource(context, dataUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            Log.w(TAG, e);
        }
    }

    public void stop() {
        if (mediaPlayer == null) return;
        mediaPlayer.release();
        mediaPlayer = null;
    }
}

