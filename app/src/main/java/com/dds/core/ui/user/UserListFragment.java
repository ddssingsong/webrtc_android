package com.dds.core.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dds.App;
import com.dds.core.voip.CallSingleActivity;
import com.dds.core.voip.VoipEvent;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;

import java.util.ArrayList;
import java.util.List;


public class UserListFragment extends Fragment {

    private UserListViewModel homeViewModel;
    private RecyclerView list;
    private List<UserBean> datas;
    private UserAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private TextView no_data;

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
        no_data = root.findViewById(R.id.no_data);
    }


    private void initData() {
        adapter = new UserAdapter();
        datas = new ArrayList<>();
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(true);

        homeViewModel.getUserList().observe(getViewLifecycleOwner(), userBeans -> {
            if (userBeans.size() == 0) {
                no_data.setVisibility(View.VISIBLE);
            } else {
                no_data.setVisibility(View.GONE);
            }
            datas.clear();
            datas.addAll(userBeans);
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
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


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        refreshLayout.setRefreshing(false);
    }

    private class UserAdapter extends RecyclerView.Adapter<Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_users, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            UserBean userBean = datas.get(position);
            holder.text.setText(userBean.getNickName());
            if (App.getInstance().getUsername().equals(userBean.getUserId())) {
                holder.item_call_audio.setVisibility(View.GONE);
                holder.item_call_video.setVisibility(View.GONE);
            } else {
                holder.item_call_audio.setVisibility(View.VISIBLE);
                holder.item_call_video.setVisibility(View.VISIBLE);
            }
            holder.item_call_video.setOnClickListener(view -> {
                CallSingleActivity.openActivity(getContext(), userBean.getUserId(), true, userBean.getNickName(), false, false);

            });
            holder.item_call_audio.setOnClickListener(view -> {
                SkyEngineKit.init(new VoipEvent());
                CallSingleActivity.openActivity(getContext(), userBean.getUserId(), true, userBean.getNickName(), true, false);
            });
        }


        @Override
        public int getItemCount() {
            return datas.size();
        }


    }

    private class Holder extends RecyclerView.ViewHolder {

        private final TextView text;
        private Button item_call_audio;
        private Button item_call_video;

        Holder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_user_name);
            item_call_audio = itemView.findViewById(R.id.item_call_audio);
            item_call_video = itemView.findViewById(R.id.item_call_video);
        }
    }

}