package cn.wildfirechat.avenginekit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.webrtc.ThreadUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class c {
    private static final String a = "AVAudioManager";
    private static final String b = "auto";
    private static final String c = "true";
    private static final String d = "false";
    private final Context e;
    private AudioManager f;
    private cn.wildfirechat.avenginekit.c.b g;
    private cn.wildfirechat.avenginekit.c.c h;
    private int i = -2;
    private boolean j = false;
    private boolean k = false;
    private boolean l = false;
    private cn.wildfirechat.avenginekit.c.a m;
    private cn.wildfirechat.avenginekit.c.a n;
    private cn.wildfirechat.avenginekit.c.a o;
    private final String p;
    private m q = null;
    private final e r;
    private Set<cn.wildfirechat.avenginekit.c.a> s = new HashSet();
    private BroadcastReceiver t;
    private OnAudioFocusChangeListener u;

    private void g() {
        if (this.p.equals("auto")) {
            if (this.s.size() == 2 && this.s.contains(cn.wildfirechat.avenginekit.c.a.c) && this.s.contains(cn.wildfirechat.avenginekit.c.a.a)) {
                if (this.q.a()) {
                    this.c(cn.wildfirechat.avenginekit.c.a.c);
                } else {
                    this.c(cn.wildfirechat.avenginekit.c.a.a);
                }
            }

        }
    }

    static cn.wildfirechat.avenginekit.c a(Context var0) {
        return new cn.wildfirechat.avenginekit.c(var0);
    }

    private c(Context var1) {
        Log.d("AVAudioManager", "ctor");
        ThreadUtils.checkIsOnMainThread();
        this.e = var1;
        this.f = (AudioManager)var1.getSystemService("audio");
        this.r = cn.wildfirechat.avenginekit.e.a(var1, this);
        this.t = new cn.wildfirechat.avenginekit.c.d((cn.wildfirechat.avenginekit.a)null);
        this.h = cn.wildfirechat.avenginekit.c.c.a;
        this.p = PreferenceManager.getDefaultSharedPreferences(var1).getString("speakerphone_preference", "auto");
        Log.d("AVAudioManager", "useSpeakerphone: " + this.p);
        if (this.p.equals("false")) {
            this.m = cn.wildfirechat.avenginekit.c.a.c;
        } else {
            this.m = cn.wildfirechat.avenginekit.c.a.a;
        }

        this.q = cn.wildfirechat.avenginekit.m.a(var1, this::g);
        Log.d("AVAudioManager", "defaultAudioDevice: " + this.m);
        a.a("AVAudioManager");
    }

    private void c(cn.wildfirechat.avenginekit.c.a var1) {
        Log.d("AVAudioManager", "setAudioDeviceInternal(device=" + var1 + ")");
        a.a(this.s.contains(var1));
        switch(cn.wildfirechat.avenginekit.b.a[var1.ordinal()]) {
            case 1:
                this.b(true);
                break;
            case 2:
            case 3:
            case 4:
                this.b(false);
                break;
            default:
                Log.e("AVAudioManager", "Invalid audio device selection");
        }

        this.n = var1;
    }

    private void a(BroadcastReceiver var1, IntentFilter var2) {
        this.e.registerReceiver(var1, var2);
    }

    private void a(BroadcastReceiver var1) {
        this.e.unregisterReceiver(var1);
    }

    private void b(boolean var1) {
        if (this.f.isSpeakerphoneOn() != var1) {
            this.f.setSpeakerphoneOn(var1);
        }
    }

    private void a(boolean var1) {
        if (this.f.isMicrophoneMute() != var1) {
            this.f.setMicrophoneMute(var1);
        }
    }

    private boolean e() {
        return this.e.getPackageManager().hasSystemFeature("android.hardware.telephony");
    }

    @Deprecated
    private boolean f() {
        if (VERSION.SDK_INT < 23) {
            return this.f.isWiredHeadsetOn();
        } else {
            AudioDeviceInfo[] var4;
            int var1 = (var4 = this.f.getDevices(3)).length;

            for(int var2 = 0; var2 < var1; ++var2) {
                int var3;
                if ((var3 = var4[var2].getType()) == 3) {
                    Log.d("AVAudioManager", "hasWiredHeadset: found wired headset");
                    return true;
                }

                if (var3 == 11) {
                    Log.d("AVAudioManager", "hasWiredHeadset: found USB audio device");
                    return true;
                }
            }

            return false;
        }
    }

    public void a(cn.wildfirechat.avenginekit.c.b var1) {
        Log.d("AVAudioManager", "start");
        ThreadUtils.checkIsOnMainThread();
        if (this.h == cn.wildfirechat.avenginekit.c.c.c) {
            Log.e("AVAudioManager", "AudioManager is already active");
        } else {
            Log.d("AVAudioManager", "AudioManager starts...");
            this.g = var1;
            this.h = cn.wildfirechat.avenginekit.c.c.c;
            this.i = this.f.getMode();
            this.j = this.f.isSpeakerphoneOn();
            this.k = this.f.isMicrophoneMute();
            this.l = this.f();
            this.u = new cn.wildfirechat.avenginekit.a(this);
            if (this.f.requestAudioFocus(this.u, 0, 2) == 1) {
                Log.d("AVAudioManager", "Audio focus request granted for VOICE_CALL streams");
            } else {
                Log.e("AVAudioManager", "Audio focus request failed");
            }

            this.f.setMode(0);
            this.f.setSpeakerphoneOn(true);
            this.a(false);
            this.n = this.o = cn.wildfirechat.avenginekit.c.a.e;
            this.s.clear();
            this.r.b();
            this.d();
            this.a(this.t, new IntentFilter("android.intent.action.HEADSET_PLUG"));
            Log.d("AVAudioManager", "AudioManager started");
            this.q.b();
        }
    }

    public void c() {
        Log.d("AVAudioManager", "stop");
        ThreadUtils.checkIsOnMainThread();
        if (this.h != cn.wildfirechat.avenginekit.c.c.c) {
            Log.e("AVAudioManager", "Trying to stop AudioManager in incorrect state: " + this.h);
        } else {
            this.h = cn.wildfirechat.avenginekit.c.c.a;
            this.a(this.t);
            this.r.d();
            this.b(this.j);
            this.a(this.k);
            this.f.setMode(this.i);
            this.f.abandonAudioFocus(this.u);
            this.u = null;
            Log.d("AVAudioManager", "Abandoned audio focus for VOICE_CALL streams");
            m var1;
            if ((var1 = this.q) != null) {
                var1.c();
                this.q = null;
            }

            this.g = null;
            Log.d("AVAudioManager", "AudioManager stopped");
        }
    }

    public void b(cn.wildfirechat.avenginekit.c.a var1) {
        label17: {
            ThreadUtils.checkIsOnMainThread();
            int var2;
            if ((var2 = cn.wildfirechat.avenginekit.b.a[var1.ordinal()]) != 1) {
                if (var2 != 2) {
                    Log.e("AVAudioManager", "Invalid default audio device selection");
                    break label17;
                }

                if (!this.e()) {
                    this.m = cn.wildfirechat.avenginekit.c.a.a;
                    break label17;
                }
            }

            this.m = var1;
        }

        Log.d("AVAudioManager", "setDefaultAudioDevice(device=" + this.m + ")");
        this.d();
    }

    public void a(cn.wildfirechat.avenginekit.c.a var1) {
        ThreadUtils.checkIsOnMainThread();
        if (!this.s.contains(var1)) {
            Log.e("AVAudioManager", "Can not select " + var1 + " from available " + this.s);
        }

        this.o = var1;
        this.d();
    }

    public Set<cn.wildfirechat.avenginekit.c.a> a() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableSet(new HashSet(this.s));
    }

    public cn.wildfirechat.avenginekit.c.a b() {
        ThreadUtils.checkIsOnMainThread();
        return this.n;
    }

    public void d() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVAudioManager", "--- updateAudioDeviceState: wired headset=" + this.l + ", BT state=" + this.r.a());
        Log.d("AVAudioManager", "Device status: available=" + this.s + ", selected=" + this.n + ", user selected=" + this.o);
        if (this.r.a() == cn.wildfirechat.avenginekit.e.c.d || this.r.a() == cn.wildfirechat.avenginekit.e.c.c || this.r.a() == cn.wildfirechat.avenginekit.e.c.e) {
            this.r.f();
        }

        HashSet var1;
        var1 = new HashSet.<init>();
        if (this.r.a() == cn.wildfirechat.avenginekit.e.c.g || this.r.a() == cn.wildfirechat.avenginekit.e.c.f || this.r.a() == cn.wildfirechat.avenginekit.e.c.d) {
            var1.add(cn.wildfirechat.avenginekit.c.a.d);
        }

        if (this.l) {
            var1.add(cn.wildfirechat.avenginekit.c.a.b);
        } else {
            var1.add(cn.wildfirechat.avenginekit.c.a.a);
            if (this.e()) {
                var1.add(cn.wildfirechat.avenginekit.c.a.c);
            }
        }

        HashSet var10002 = var1;
        boolean var4 = this.s.equals(var1) ^ true;
        this.s = var10002;
        if (this.r.a() == cn.wildfirechat.avenginekit.e.c.c && this.o == cn.wildfirechat.avenginekit.c.a.d) {
            this.o = cn.wildfirechat.avenginekit.c.a.e;
        }

        if (this.l && this.o == cn.wildfirechat.avenginekit.c.a.a) {
            this.o = cn.wildfirechat.avenginekit.c.a.b;
        }

        if (!this.l && this.o == cn.wildfirechat.avenginekit.c.a.b) {
            this.o = cn.wildfirechat.avenginekit.c.a.a;
        }

        cn.wildfirechat.avenginekit.c.a var2;
        boolean var5;
        if (this.r.a() != cn.wildfirechat.avenginekit.e.c.d || (var2 = this.o) != cn.wildfirechat.avenginekit.c.a.e && var2 != cn.wildfirechat.avenginekit.c.a.d) {
            var5 = false;
        } else {
            var5 = true;
        }

        cn.wildfirechat.avenginekit.c.a var3;
        boolean var7;
        if ((this.r.a() == cn.wildfirechat.avenginekit.e.c.g || this.r.a() == cn.wildfirechat.avenginekit.e.c.f) && (var3 = this.o) != cn.wildfirechat.avenginekit.c.a.e && var3 != cn.wildfirechat.avenginekit.c.a.d) {
            var7 = true;
        } else {
            var7 = false;
        }

        if (this.r.a() == cn.wildfirechat.avenginekit.e.c.d || this.r.a() == cn.wildfirechat.avenginekit.e.c.f || this.r.a() == cn.wildfirechat.avenginekit.e.c.g) {
            Log.d("AVAudioManager", "Need BT audio: start=" + var5 + ", stop=" + var7 + ", BT state=" + this.r.a());
        }

        if (var7) {
            this.r.e();
            this.r.f();
        }

        if (var5 && !var7 && !this.r.c()) {
            this.s.remove(cn.wildfirechat.avenginekit.c.a.d);
            var4 = true;
        }

        if (this.r.a() == cn.wildfirechat.avenginekit.e.c.g) {
            var2 = cn.wildfirechat.avenginekit.c.a.d;
        } else if (this.l) {
            var2 = cn.wildfirechat.avenginekit.c.a.b;
        } else {
            var2 = this.m;
        }

        if (var2 != this.n || var4) {
            this.c(var2);
            Log.d("AVAudioManager", "New device status: available=" + this.s + ", selected=" + var2);
            cn.wildfirechat.avenginekit.c.b var6;
            if ((var6 = this.g) != null) {
                var6.a(this.n, this.s);
            }
        }

        Log.d("AVAudioManager", "--- updateAudioDeviceState done");
    }

    private class d extends BroadcastReceiver {
        private static final int a = 0;
        private static final int b = 1;
        private static final int c = 0;
        private static final int d = 1;

        private d() {
        }

        public void onReceive(Context var1, Intent var2) {
            int var7;
            int var10000 = var7 = var2.getIntExtra("state", 0);
            int var3 = var2.getIntExtra("microphone", 0);
            String var4 = var2.getStringExtra("name");
            String var5 = "AVAudioManager";
            StringBuilder var9 = (new StringBuilder()).append("WiredHeadsetReceiver.onReceive").append(a.a()).append(": a=").append(var2.getAction()).append(", s=");
            String var6;
            if (var10000 == 0) {
                var6 = "unplugged";
            } else {
                var6 = "plugged";
            }

            var9 = var9.append(var6).append(", m=");
            String var11;
            if (var3 == 1) {
                var11 = "mic";
            } else {
                var11 = "no mic";
            }

            var10000 = var7;
            Log.d(var5, var9.append(var11).append(", n=").append(var4).append(", sb=").append(this.isInitialStickyBroadcast()).toString());
            cn.wildfirechat.avenginekit.c var8 = c.this;
            boolean var10;
            if (var10000 == 1) {
                var10 = true;
            } else {
                var10 = false;
            }

            var8.l = var10;
            c.this.d();
        }
    }

    public interface b {
        void a(cn.wildfirechat.avenginekit.c.a var1, Set<cn.wildfirechat.avenginekit.c.a> var2);
    }

    public static enum c {
        a,
        b,
        c;

        private c() {
        }
    }

    public static enum a {
        a,
        b,
        c,
        d,
        e;

        private a() {
        }
    }
}
