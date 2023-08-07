package com.dds.temple0.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dds.temple0.R;
import com.dds.temple0.socket.SocketManager;


public class SettingFragment extends Fragment {

    private SettingViewModel notificationsViewModel;
    private Button button;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(SettingViewModel.class);
        View root = inflater.inflate(R.layout.temp0_fragment_setting, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        button = root.findViewById(R.id.exit);
        button.setOnClickListener(v -> SocketManager.getInstance().logout());
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }


}