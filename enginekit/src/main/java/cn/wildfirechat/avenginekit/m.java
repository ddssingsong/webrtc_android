package cn.wildfirechat.avenginekit;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import org.webrtc.ThreadUtils;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class m implements SensorEventListener {
    private static final String a = "AVProximitySensor";
    private final ThreadUtils.ThreadChecker b = new ThreadUtils.ThreadChecker();
    private final Runnable c;
    private final SensorManager d;
    private Sensor e = null;
    private boolean f = false;

    static m a(Context var0, Runnable var1) {
        return new m(var0, var1);
    }

    private m(Context var1, Runnable var2) {
        Log.d("AVProximitySensor", "AVProximitySensor" + a.a());
        this.c = var2;
        this.d = (SensorManager)var1.getSystemService("sensor");
    }

    private boolean d() {
        if (this.e != null) {
            return true;
        } else {
            this.e = this.d.getDefaultSensor(8);
            if (this.e == null) {
                return false;
            } else {
                this.e();
                return true;
            }
        }
    }

    private void e() {
        if (this.e != null) {
            StringBuilder var1;
            StringBuilder var10000 = var1 = new StringBuilder;
            var10000.<init>("Proximity sensor: ");
            var10000.append("name=").append(this.e.getName());
            var10000.append(", vendor: ").append(this.e.getVendor());
            var10000.append(", power: ").append(this.e.getPower());
            var10000.append(", resolution: ").append(this.e.getResolution());
            var10000.append(", max range: ").append(this.e.getMaximumRange());
            var10000.append(", min delay: ").append(this.e.getMinDelay());
            if (Build.VERSION.SDK_INT >= 20) {
                var1.append(", type: ").append(this.e.getStringType());
            }

            if (Build.VERSION.SDK_INT >= 21) {
                var1.append(", max delay: ").append(this.e.getMaxDelay());
                var1.append(", reporting mode: ").append(this.e.getReportingMode());
                var1.append(", isWakeUpSensor: ").append(this.e.isWakeUpSensor());
            }

            Log.d("AVProximitySensor", var1.toString());
        }
    }

    public boolean b() {
        this.b.checkIsOnValidThread();
        Log.d("AVProximitySensor", "start" + a.a());
        if (!this.d()) {
            return false;
        } else {
            this.d.registerListener(this, this.e, 3);
            return true;
        }
    }

    public void c() {
        this.b.checkIsOnValidThread();
        Log.d("AVProximitySensor", "stop" + a.a());
        Sensor var1;
        if ((var1 = this.e) != null) {
            this.d.unregisterListener(this, var1);
        }
    }

    public boolean a() {
        this.b.checkIsOnValidThread();
        return this.f;
    }

    public final void onAccuracyChanged(Sensor var1, int var2) {
        this.b.checkIsOnValidThread();
        a.a(var1.getType() == 8);
        if (var2 == 0) {
            Log.e("AVProximitySensor", "The values returned by this sensor cannot be trusted");
        }

    }

    public final void onSensorChanged(SensorEvent var1) {
        this.b.checkIsOnValidThread();
        a.a(var1.sensor.getType() == 8);
        if (var1.values[0] < this.e.getMaximumRange()) {
            Log.d("AVProximitySensor", "Proximity sensor => NEAR state");
            this.f = true;
        } else {
            Log.d("AVProximitySensor", "Proximity sensor => FAR state");
            this.f = false;
        }

        Runnable var3;
        if ((var3 = this.c) != null) {
            var3.run();
        }

        Log.d("AVProximitySensor", "onSensorChanged" + a.a() + ": accuracy=" + var1.accuracy + ", timestamp=" + var1.timestamp + ", distance=" + var1.values[0]);
    }
}

