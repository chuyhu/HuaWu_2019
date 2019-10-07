package com.example.mainapp.firstpage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.arcsoft.face.FaceEngine;
import com.example.mainapp.R;
import com.example.mainapp.cameramoudle.CameraMainActivity;
import com.example.mainapp.cameramoudle.common.Constants;
import com.example.mainapp.cameramoudle.util.ConfigUtil;
import com.example.mainapp.videorecyerview.VideoListFragment;
import com.example.qqnaviviewlibrary.QQNaviView;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FirstPageActivity extends AppCompatActivity {



    //关于人脸拍照方面
    private Toast toast = null;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };


    // 底部导航栏
    private QQNaviView mFamilyView;
    private QQNaviView mVideoView;
    private QQNaviView mNoteView;
    private QQNaviView mSelfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化布局
        setContentView(R.layout.activity_first_page);

        mFamilyView = (QQNaviView) findViewById(R.id.fr_family);
        mVideoView = (QQNaviView) findViewById(R.id.fr_video);
        mNoteView = (QQNaviView) findViewById(R.id.fr_note);
        mSelfView = (QQNaviView) findViewById(R.id.fr_self);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        resetIcon();
        mVideoView.setBigIcon(R.drawable.video);
        mVideoView.setSmallIcon(R.drawable.video1);
        mFamilyView.lookRight();
        ft.replace(R.id.container,new VideoListFragment()).commit();
//        showToast("video");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.lookLeft();
            }
        }, 50);

        // 设置人脸识别角度
        ConfigUtil.setFtOrient(FirstPageActivity.this, FaceEngine.ASF_OP_0_HIGHER_EXT);

    }


    public void onClick(View view){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        resetIcon();
        switch (view.getId()){
            case R.id.fr_family:
                mFamilyView.setBigIcon(R.drawable.familycare);
                mFamilyView.setSmallIcon(R.drawable.familycare1);
                mVideoView.lookLeft();
                ft.replace(R.id.container,new Family()).commit();
                showToast("family");
                break;
            case R.id.fr_video:
                mVideoView.setBigIcon(R.drawable.video);
                mVideoView.setSmallIcon(R.drawable.video1);
                mFamilyView.lookRight();
                ft.replace(R.id.container,new VideoListFragment()).commit();
                showToast("video");
                break;
            case R.id.fr_note:
                mNoteView.setBigIcon(R.drawable.note);
                mNoteView.setSmallIcon(R.drawable.note1);
                mNoteView.lookRight();
                mVideoView.lookRight();
                ft.replace(R.id.container,new Note()).commit();
                showToast("note");
                break;
            case R.id.fr_self:
                mSelfView.setBigIcon(R.drawable.video);
                mSelfView.setSmallIcon(R.drawable.video1);
                mNoteView.lookRight();
                ft.replace(R.id.container,new Self()).commit();
                showToast("self");
                break;
        }
    }

    private void resetIcon() {
        mFamilyView.setBigIcon(R.drawable.familycare);
        mFamilyView.setSmallIcon(R.drawable.familycare1);

        mVideoView.setBigIcon(R.drawable.video);
        mVideoView.setSmallIcon(R.drawable.video1);

        mNoteView.setBigIcon(R.drawable.note);
        mNoteView.setSmallIcon(R.drawable.note1);

        mSelfView.setBigIcon(R.drawable.note);
        mSelfView.setSmallIcon(R.drawable.note1);
    }

    private void showToast(CharSequence msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //    人脸部分

    /**
     * 激活引擎
     *
     * @param view
     */

    public void activeEngine(final View view) {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(FirstPageActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (view != null) {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        /**
         * 打开相机，显示年龄性别
         *
         * @param view
         */
        startActivity(new Intent(this, CameraMainActivity.class));

    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine(null);
            }
        }
    }




}
