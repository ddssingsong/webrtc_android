package com.dds.java.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dds.webrtc.R;


public class UserListFragment extends Fragment {

    private UserListViewModel homeViewModel;
    private RecyclerView list;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(UserListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        initData();
        return root;
    }

    private void initView(View root) {
        list = root.findViewById(R.id.list);
    }


    private void initData() {

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}