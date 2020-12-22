package com.dds.core.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.core.util.ActivityStackManager;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 添加Activity到堆栈
        ActivityStackManager.getInstance().onCreated(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        ActivityStackManager.getInstance().onDestroyed(this);
        super.onDestroy();
    }
}
