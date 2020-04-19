package com.dds.core.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dds.webrtc.R;

import java.util.ArrayList;
import java.util.List;


public class UserListFragment extends Fragment {

    private UserListViewModel homeViewModel;
    private RecyclerView list;
    private List<UserBean> datas;
    private UserAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

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
        refreshLayout = root.findViewById(R.id.swipe);
    }


    private void initData() {
        adapter = new UserAdapter();
        datas = new ArrayList<>();
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));

        homeViewModel.getUserList().observe(getViewLifecycleOwner(), userBeans -> {
            datas.clear();
            datas.addAll(userBeans);
            adapter.notifyDataSetChanged();
        });

        refreshLayout.setOnRefreshListener(() -> {
            homeViewModel.loadUsers();
        });
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private class UserAdapter extends RecyclerView.Adapter<Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_users, null);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            UserBean userBean = datas.get(position);
            holder.text.setText(userBean.getNickName());
        }


        @Override
        public int getItemCount() {
            return datas.size();
        }


    }

    private class Holder extends RecyclerView.ViewHolder {

        private final TextView text;

        Holder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_user_name);
        }
    }

}