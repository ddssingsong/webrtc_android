package com.dds.temple0.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dds.temple0.R;
import com.dds.temple0.socket.SocketManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class UserHomeActivity extends AppCompatActivity implements SocketManager.IUserStateEvent {
    private static final String TAG = "UserHomeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp0_activity_user_home);
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
        SocketManager.getInstance().addUserStateCallback(this, this);


    }

    @Override
    protected void onDestroy() {
        SocketManager.getInstance().removeUserStateCallback(this);
        super.onDestroy();
    }

    @Override
    public void userLogin(String userId) {
        // nop
    }

    @Override
    public void userLogout(String info) {
        this.finishAfterTransition();

    }

}
