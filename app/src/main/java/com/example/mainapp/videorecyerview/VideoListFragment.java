package com.example.mainapp.videorecyerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mainapp.R;
import com.example.zqvideolibrary.ZQVideoPlayer;

public class VideoListFragment extends Fragment {
    public static Fragment newInstance() {
        VideoListFragment fragment = new VideoListFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_video_layout, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new VideoRecyclerAdapter());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        ZQVideoPlayer.releaseAllVideos();
    }

}
