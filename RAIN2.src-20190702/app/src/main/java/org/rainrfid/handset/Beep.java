package org.rainrfid.handset;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import org.rainrfid.handset.R;

public class Beep {

    private SoundPool soundPool;
    private int sound_ok;
    private int sound_error;
    private int sound_done;

    private int sound_current;

    public Beep(Context context) {
        if (soundPool == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            }
            this.sound_ok = soundPool.load(context, R.raw.success, 0);
            this.sound_error = soundPool.load(context, R.raw.error, 0);
            this.sound_done = soundPool.load(context, R.raw.ding, 0);
        }
    }

    public void playInv() {
        if (sound_current > 0) {
            soundPool.stop(sound_current);
        }
        sound_current = soundPool.play(sound_done, 1f, 1f, 0, 0, 1f);
    }

    public void playOk() {
        if (sound_current > 0) {
            soundPool.stop(sound_current);
        }
        sound_current = soundPool.play(sound_ok, 1f, 1f, 0, 0, 1f);
    }

    public void playError() {
        if (sound_current > 0) {
            soundPool.stop(sound_current);
        }
        sound_current = soundPool.play(sound_error, 1f, 1f, 0, 0, 1f);
    }
}
