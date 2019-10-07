package com.example.mainapp.lbs.PanormaDemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.model.BaiduPanoData;
import com.baidu.lbsapi.panoramaview.OnTabMarkListener;
import com.baidu.lbsapi.panoramaview.PanoramaRequest;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.lbsapi.panoramaview.TextMarker;
import com.baidu.lbsapi.tools.Point;
import com.baidu.pano.platform.plugin.indooralbum.IndoorAlbumCallback;
import com.baidu.pano.platform.plugin.indooralbum.IndoorAlbumPlugin;
import com.example.mainapp.R;

public class PanoramaDemoActivityMain extends AppCompatActivity {

    PanoramaView panoramaView;
    private double mLatitude,mLongitude;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DemoApplication app = (DemoApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new DemoApplication.MyGeneralListener());
        }
        setContentView(R.layout.activity_panorama_demo_main);

        //初始化控件
        panoramaView = (PanoramaView)findViewById(R.id.panorama);

        Intent intent = getIntent();
        mLatitude = intent.getDoubleExtra("latitude",0);
        mLongitude = intent.getDoubleExtra("longitude",0);
        uid = intent.getStringExtra("uid");

        //设置全景图片的显示级别
        //较低清晰度 ImageDefinationLow
        //中等清晰度 ImageDefinationMiddle
        //较高清晰度 ImageDefinationHigh
        panoramaView. setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh);
        panoramaView.setPanoramaPitch(0);      //设置全景图的俯仰角
        //获取当前全景图的俯仰角
        //更新俯仰角的取值范围：室外景[-15, 90], 室内景[-25, 90],
        //90为垂直朝上方向，0为水平方向
        panoramaView.getPanoramaPitch();
        panoramaView.setPanoramaHeading(0);        //设置全景图的偏航角
        panoramaView.getPanoramaHeading();    //获取当前全景图的偏航角
        panoramaView.setPanoramaLevel(1);        //设置全景图的缩放级别   level分为1-5级
        panoramaView.setShowTopoLink(true);        //是否显示邻接街景箭头（有邻接全景的时候）
        panoramaView.setArrowTextureByUrl(uid);        //根据url设置箭头的纹理(2.0.0新增)

        panoramaView.setPanoramaViewListener(new PanoramaViewListener() {
            @Override
            public void onDescriptionLoadEnd(String s) {

            }

            @Override
            public void onLoadPanoramaBegin() {

            }

            @Override
            public void onLoadPanoramaEnd(String s) {

            }

            @Override
            public void onLoadPanoramaError(String s) {

            }

            @Override
            public void onMessage(String s, int i) {

            }

            @Override
            public void onCustomMarkerClick(String s) {

            }

            @Override
            public void onMoveStart() {

            }

            @Override
            public void onMoveEnd() {

            }
        });
        panoramaView.setPanorama(mLongitude, mLatitude);
        PanoramaRequest request = PanoramaRequest.getInstance(PanoramaDemoActivityMain.this);
        BaiduPanoData locationPanoramaData = request.getPanoramaInfoByLatLon(mLongitude,mLatitude);

        //默认相册
        IndoorAlbumPlugin.getInstance().init();
        IndoorAlbumCallback.EntryInfo info = new IndoorAlbumCallback.EntryInfo();
        info.setEnterPid(locationPanoramaData.getPid());
        IndoorAlbumPlugin.getInstance().loadAlbumView(panoramaView,info);
        panoramaView.setPanoramaByUid(uid,PanoramaView.PANOTYPE_INTERIOR);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.item_add:
                TextMarker marker = new TextMarker();
                marker.setMarkerPosition(new Point(mLongitude,mLatitude));
                marker.setMarkerHeight(20f);
                marker.setText("Hello marker!");
                marker.setFontSize(12);
                marker.setBgColor(0XFFFFFFF);
                marker.setPadding(10,20,15,25);
                marker.setOnTabMarkListener(new OnTabMarkListener() {
                    @Override
                    public void onTab() {
                        Toast.makeText(PanoramaDemoActivityMain.this,"标注已被点击",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        panoramaView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        panoramaView.onResume();
    }
    @Override
    protected void onDestroy() {
        panoramaView.destroy();
        super.onDestroy();
    }

}
