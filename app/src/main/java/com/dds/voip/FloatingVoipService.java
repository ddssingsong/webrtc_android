package com.dds.voip;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FloatingVoipService extends Service {
    public FloatingVoipService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
