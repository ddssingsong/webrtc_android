package cn.wildfirechat.avenginekit;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import org.webrtc.ThreadUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class e {
    private static final String a = "AVBluetoothManager";
    private static final int b = 4000;
    private static final int c = 2;
    private final Context d;
    private final cn.wildfirechat.avenginekit.c e;
    private final AudioManager f;
    private final Handler g;
    int h;
    private e.c i;
    private final BluetoothProfile.ServiceListener j;
    private BluetoothAdapter k;
    private BluetoothHeadset l;
    private BluetoothDevice m;
    private final BroadcastReceiver n;
    private final Runnable o = new d(this);

    static e a(Context var0, cn.wildfirechat.avenginekit.c var1) {
        Log.d("AVBluetoothManager", "create" + a.a());
        return new e(var0, var1);
    }

    protected e(Context var1, cn.wildfirechat.avenginekit.c var2) {
        Log.d("AVBluetoothManager", "ctor");
        ThreadUtils.checkIsOnMainThread();
        this.d = var1;
        this.e = var2;
        this.f = this.a(var1);
        this.i = e.c.a;
        this.j = new e.b((d)null);
        this.n = new e.a((d)null);
        this.g = new Handler(Looper.getMainLooper());
    }

    private void k() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "updateAudioDeviceState");
        this.e.d();
    }

    private void j() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "startTimer");
        this.g.postDelayed(this.o, 4000L);
    }

    private void h() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "cancelTimer");
        this.g.removeCallbacks(this.o);
    }

    private void g() {
        ThreadUtils.checkIsOnMainThread();
        if (this.i != e.c.a && this.l != null) {
            Log.d("AVBluetoothManager", "bluetoothTimeout: BT state=" + this.i + ", attempts: " + this.h + ", SCO is on: " + this.i());
            if (this.i == e.c.f) {
                boolean var1 = false;
                List var2;
                if ((var2 = this.l.getConnectedDevices()).size() > 0) {
                    this.m = (BluetoothDevice)var2.get(0);
                    if (this.l.isAudioConnected(this.m)) {
                        Log.d("AVBluetoothManager", "SCO connected with " + this.m.getName());
                        var1 = true;
                    } else {
                        Log.d("AVBluetoothManager", "SCO is not connected with " + this.m.getName());
                    }
                }

                if (var1) {
                    this.i = e.c.g;
                    this.h = 0;
                } else {
                    Log.w("AVBluetoothManager", "BT failed to connect after timeout");
                    this.e();
                }

                this.k();
                Log.d("AVBluetoothManager", "bluetoothTimeout done: BT state=" + this.i);
            }
        }
    }

    private boolean i() {
        return this.f.isBluetoothScoOn();
    }

    private String a(int var1) {
        switch(var1) {
            case 0:
                return "DISCONNECTED";
            case 1:
                return "CONNECTING";
            case 2:
                return "CONNECTED";
            case 3:
                return "DISCONNECTING";
            default:
                switch(var1) {
                    case 10:
                        return "OFF";
                    case 11:
                        return "TURNING_ON";
                    case 12:
                        return "ON";
                    case 13:
                        return "TURNING_OFF";
                    default:
                        return "INVALID";
                }
        }
    }

    public e.c a() {
        ThreadUtils.checkIsOnMainThread();
        return this.i;
    }

    public void b() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "start");
        if (!this.a(this.d, "android.permission.BLUETOOTH")) {
            Log.w("AVBluetoothManager", "Process (pid=" + Process.myPid() + ") lacks BLUETOOTH permission");
        } else if (this.i != e.c.a) {
            Log.w("AVBluetoothManager", "Invalid BT state");
        } else {
            this.l = null;
            this.m = null;
            this.h = 0;
            this.k = BluetoothAdapter.getDefaultAdapter();
            if (this.k == null) {
                Log.w("AVBluetoothManager", "Device does not support Bluetooth");
            } else if (!this.f.isBluetoothScoAvailableOffCall()) {
                Log.e("AVBluetoothManager", "Bluetooth SCO audio is not available off call");
            } else {
                this.a(this.k);
                if (!this.a(this.d, this.j, 1)) {
                    Log.e("AVBluetoothManager", "BluetoothAdapter.getProfileProxy(HEADSET) failed");
                } else {
                    IntentFilter var1;
                    IntentFilter var10003 = var1 = new IntentFilter;
                    var10003.<init>();
                    var10003.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
                    var10003.addAction("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
                    this.a(this.n, var1);
                    Log.d("AVBluetoothManager", "HEADSET profile state: " + this.a(this.k.getProfileConnectionState(1)));
                    Log.d("AVBluetoothManager", "Bluetooth proxy for headset profile has started");
                    this.i = e.c.c;
                    Log.d("AVBluetoothManager", "start done: BT state=" + this.i);
                }
            }
        }
    }

    public void d() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "stop: BT state=" + this.i);
        if (this.k != null) {
            this.e();
            if (this.i != e.c.a) {
                this.a(this.n);
                this.h();
                BluetoothHeadset var1;
                if ((var1 = this.l) != null) {
                    this.k.closeProfileProxy(1, var1);
                    this.l = null;
                }

                this.k = null;
                this.m = null;
                this.i = e.c.a;
                Log.d("AVBluetoothManager", "stop done: BT state=" + this.i);
            }
        }
    }

    public boolean c() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "startSco: BT state=" + this.i + ", attempts: " + this.h + ", SCO is on: " + this.i());
        if (this.h >= 2) {
            Log.e("AVBluetoothManager", "BT SCO connection fails - no more attempts");
            return false;
        } else if (this.i != e.c.d) {
            Log.e("AVBluetoothManager", "BT SCO connection fails - no headset available");
            return false;
        } else {
            Log.d("AVBluetoothManager", "Starting Bluetooth SCO and waits for ACTION_AUDIO_STATE_CHANGED...");
            this.i = e.c.f;
            this.f.startBluetoothSco();
            this.f.setBluetoothScoOn(true);
            ++this.h;
            this.j();
            Log.d("AVBluetoothManager", "startScoAudio done: BT state=" + this.i + ", SCO is on: " + this.i());
            return true;
        }
    }

    public void e() {
        ThreadUtils.checkIsOnMainThread();
        Log.d("AVBluetoothManager", "stopScoAudio: BT state=" + this.i + ", SCO is on: " + this.i());
        e.c var1;
        if ((var1 = this.i) == e.c.f || var1 == e.c.g) {
            this.h();
            this.f.stopBluetoothSco();
            this.f.setBluetoothScoOn(false);
            this.i = e.c.e;
            Log.d("AVBluetoothManager", "stopScoAudio done: BT state=" + this.i + ", SCO is on: " + this.i());
        }
    }

    public void f() {
        if (this.i != e.c.a && this.l != null) {
            Log.d("AVBluetoothManager", "updateDevice");
            List var1;
            if ((var1 = this.l.getConnectedDevices()).isEmpty()) {
                this.m = null;
                this.i = e.c.c;
                Log.d("AVBluetoothManager", "No connected bluetooth headset");
            } else {
                this.m = (BluetoothDevice)var1.get(0);
                this.i = e.c.d;
                Log.d("AVBluetoothManager", "Connected bluetooth headset: name=" + this.m.getName() + ", state=" + this.a(this.l.getConnectionState(this.m)) + ", SCO audio=" + this.l.isAudioConnected(this.m));
            }

            Log.d("AVBluetoothManager", "updateDevice done: BT state=" + this.i);
        }
    }

    protected AudioManager a(Context var1) {
        return (AudioManager)var1.getSystemService("audio");
    }

    protected void a(BroadcastReceiver var1, IntentFilter var2) {
        this.d.registerReceiver(var1, var2);
    }

    protected void a(BroadcastReceiver var1) {
        this.d.unregisterReceiver(var1);
    }

    protected boolean a(Context var1, BluetoothProfile.ServiceListener var2, int var3) {
        return this.k.getProfileProxy(var1, var2, var3);
    }

    protected boolean a(Context var1, String var2) {
        return this.d.checkPermission(var2, Process.myPid(), Process.myUid()) == 0;
    }

    @SuppressLint({"HardwareIds"})
    protected void a(BluetoothAdapter var1) {
        Log.d("AVBluetoothManager", "BluetoothAdapter: enabled=" + var1.isEnabled() + ", state=" + this.a(var1.getState()) + ", name=" + var1.getName() + ", address=" + var1.getAddress());
        Set var2;
        if (!(var2 = var1.getBondedDevices()).isEmpty()) {
            Log.d("AVBluetoothManager", "paired devices:");
            Iterator var3 = var2.iterator();

            while(var3.hasNext()) {
                BluetoothDevice var4 = (BluetoothDevice)var3.next();
                Log.d("AVBluetoothManager", " name=" + var4.getName() + ", address=" + var4.getAddress());
            }
        }

    }

    private class a extends BroadcastReceiver {
        private a() {
        }

        public void onReceive(Context var1, Intent var2) {
            if (e.this.i != e.c.a) {
                int var10000;
                String var3;
                int var4;
                e var5;
                if ((var3 = var2.getAction()).equals("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")) {
                    var10000 = var4 = var2.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
                    Log.d("AVBluetoothManager", "BluetoothHeadsetBroadcastReceiver.onReceive: a=ACTION_CONNECTION_STATE_CHANGED, s=" + e.this.a(var4) + ", sb=" + this.isInitialStickyBroadcast() + ", BT state: " + e.this.i);
                    if (var10000 == 2) {
                        var5 = e.this;
                        var5.h = 0;
                        var5.k();
                    } else if (var4 != 1 && var4 != 3 && var4 == 0) {
                        e.this.e();
                        e.this.k();
                    }
                } else if (var3.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                    var10000 = var4 = var2.getIntExtra("android.bluetooth.profile.extra.STATE", 10);
                    Log.d("AVBluetoothManager", "BluetoothHeadsetBroadcastReceiver.onReceive: a=ACTION_AUDIO_STATE_CHANGED, s=" + e.this.a(var4) + ", sb=" + this.isInitialStickyBroadcast() + ", BT state: " + e.this.i);
                    if (var10000 == 12) {
                        e.this.h();
                        if (e.this.i == e.c.f) {
                            Log.d("AVBluetoothManager", "+++ Bluetooth audio SCO is now connected");
                            e.this.i = e.c.g;
                            var5 = e.this;
                            var5.h = 0;
                            var5.k();
                        } else {
                            Log.w("AVBluetoothManager", "Unexpected state BluetoothHeadset.STATE_AUDIO_CONNECTED");
                        }
                    } else if (var4 == 11) {
                        Log.d("AVBluetoothManager", "+++ Bluetooth audio SCO is now connecting...");
                    } else if (var4 == 10) {
                        Log.d("AVBluetoothManager", "+++ Bluetooth audio SCO is now disconnected");
                        if (this.isInitialStickyBroadcast()) {
                            Log.d("AVBluetoothManager", "Ignore STATE_AUDIO_DISCONNECTED initial sticky broadcast.");
                            return;
                        }

                        e.this.k();
                    }
                }

                Log.d("AVBluetoothManager", "onReceive done: BT state=" + e.this.i);
            }
        }
    }

    private class b implements BluetoothProfile.ServiceListener {
        private b() {
        }

        public void onServiceConnected(int var1, BluetoothProfile var2) {
            if (var1 == 1 && e.this.i != e.c.a) {
                Log.d("AVBluetoothManager", "BluetoothServiceListener.onServiceConnected: BT state=" + e.this.i);
                e.this.l = (BluetoothHeadset)var2;
                e.this.k();
                Log.d("AVBluetoothManager", "onServiceConnected done: BT state=" + e.this.i);
            }
        }

        public void onServiceDisconnected(int var1) {
            if (var1 == 1 && e.this.i != e.c.a) {
                Log.d("AVBluetoothManager", "BluetoothServiceListener.onServiceDisconnected: BT state=" + e.this.i);
                e.this.e();
                e.this.l = null;
                e.this.m = null;
                e.this.i = e.c.c;
                e.this.k();
                Log.d("AVBluetoothManager", "onServiceDisconnected done: BT state=" + e.this.i);
            }
        }
    }

    public static enum c {
        a,
        b,
        c,
        d,
        e,
        f,
        g;

        private c() {
        }
    }
}
