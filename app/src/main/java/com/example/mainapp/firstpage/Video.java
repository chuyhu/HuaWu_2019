package com.example.mainapp.firstpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mainapp.R;
import com.example.mainapp.videorecyerview.VideoListFragment;
import com.example.zqvideolibrary.ZQVideoPlayer;

public class Video extends Fragment {

    public Video() {
        // Required empty public constructor
    }

    // 关于视频recyclerview
    private ViewPager viewPager;
    private Fragment[] mFragmentArrays = new Fragment[1];
    private String[] mTabTitles = new String[1];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        // 关于视频recyclerview
        viewPager = view.findViewById(R.id.view_pager);
        // 关于视频方面初始化
        initView();
        // Inflate the layout for this fragment
        return view;
    }

    //关于视频recylerview
    private void initView() {
        mFragmentArrays[0] = VideoListFragment.newInstance();
        PagerAdapter adapter = new VideoViewPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    final class VideoViewPagerAdapter extends FragmentPagerAdapter {
        public VideoViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentArrays[position];
        }

        @Override
        public int getCount() {
            return mFragmentArrays.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ZQVideoPlayer.releaseAllVideos();
    }
}

