package com.dds.core;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dds.LauncherActivity;
import com.dds.core.socket.IUserState;
import com.dds.core.socket.SocketManager;
import com.dds.webrtc.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements IUserState {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_user, R.id.navigation_room, R.id.navigation_setting)
                .build();

        // 設置ActionBar跟随联动
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        // 设置Nav跟随联动
        NavigationUI.setupWithNavController(navView, navController);


        // 设置登录状态回调
        SocketManager.getInstance().addUserStateCallback(this);

    }

    @Override
    public void userLogin() {

    }

    @Override
    public void userLogout() {
        if (!this.isFinishing()) {
            Intent intent = new Intent(this, LauncherActivity.class);
            startActivity(intent);
            this.finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
