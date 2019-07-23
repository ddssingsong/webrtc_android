package cn.wildfirechat.avenginekit;

import android.media.AudioManager;
import android.util.Log;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class a implements AudioManager.OnAudioFocusChangeListener {
    a(c var1) {
        this.a = var1;
    }

    public void onAudioFocusChange(int var1) {
        String var2;
        switch(var1) {
            case -3:
                var2 = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                break;
            case -2:
                var2 = "AUDIOFOCUS_LOSS_TRANSIENT";
                break;
            case -1:
                var2 = "AUDIOFOCUS_LOSS";
                break;
            case 0:
            default:
                var2 = "AUDIOFOCUS_INVALID";
                break;
            case 1:
                var2 = "AUDIOFOCUS_GAIN";
                break;
            case 2:
                var2 = "AUDIOFOCUS_GAIN_TRANSIENT";
                break;
            case 3:
                var2 = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                break;
            case 4:
                var2 = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
        }

        Log.d("AVAudioManager", "onAudioFocusChange: " + var2);
    }
}
