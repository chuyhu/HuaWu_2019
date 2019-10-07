package com.example.mainapp.lbs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.example.mainapp.R;
import com.example.mainapp.lbs.PanormaDemo.PanoramaDemoActivityMain;
import com.example.mainapp.lbs.search.PoiOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LbsActivity extends AppCompatActivity implements View.OnClickListener {

    public LocationClient mLocationClient;   //百度地图监听器
    private TextView positionText;
    private MapView mapView;    //初始化百度地图
    private BaiduMap baiduMap;    //初始化百度地图
    private boolean isFirstLocation = true;   //是否首次定位
    private static String PATH = "custom_map_config.json";   //个性化地图路径
    private TextView mGexinghua;   //个性化设置
    private Boolean GXH = true;
    private TextView btSerach;  //设置搜索功能
    private EditText txtAddCity, txtAddr;   //设置搜索框
    private PoiSearch mPoiSearch;  //用于信息检索
    BitmapDescriptor othersCurrentMarker;    //其他人的位置信息
    int tag = 1;   //请求标识
    long serviceId = 0;  //轨迹服务Id
    String entityName = "myTrace";   //设备标识
    boolean isNeedObjectStorage = false;   //是否需要对象存储服务

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();

        //设置locationClientOption
        mLocationClient.setLocOption(option);
        SDKInitializer.initialize(getApplicationContext());

        //设置个性化地图
        setMapCustonFile(this, PATH);

        setContentView(R.layout.activity_lbs);

        //初始化控件
        positionText = (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();

        //权限申请
        if (ContextCompat.checkSelfPermission(LbsActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(LbsActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(LbsActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(LbsActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        judgePermission();    //获取动态权限

        //初始化控件
        mapView = (MapView) findViewById(R.id.bmapView);  //地图控件
        mGexinghua = (TextView) findViewById(R.id.gexinghua);   //个性化
        txtAddCity = (EditText) findViewById(R.id.txtAddCity);  //城市输入框
        txtAddr = (EditText) findViewById(R.id.txtAddr);   //关键字输入框
        btSerach = (TextView) findViewById(R.id.btOk);   //搜索

        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        mGexinghua.setOnClickListener(this);   //个性化设置

        mPoiSearch = PoiSearch.newInstance();   //创建POI检索实例

        btSerach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPoiSearch.searchInCity((new PoiCitySearchOption()
                        .city(txtAddCity.getText().toString())
                        .keyword(txtAddr.getText().toString())));
                Toast.makeText(LbsActivity.this, txtAddCity.getText().toString() + txtAddr.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        mPoiSearch.setOnGetPoiSearchResultListener(listener);  //设置检索监听器

        //删除百度地图logo
        View child = mapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        //添加他人的位置
        Resources r1 = this.getResources();
        Bitmap bmp1 = BitmapFactory.decodeResource(r1, R.drawable.touxiang);
        addOtherLocation(38.02, 112.455, bmp1);

        //初始化轨迹服务
        Trace mTrace = new Trace(serviceId,entityName,isNeedObjectStorage);
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
        int gatherInterval = 5;  //定义周期单位是 秒
        int packInterval = 10;  //定义打包回传周期  单位为  秒
        mTraceClient.setInterval(gatherInterval,packInterval);

        //初始化监听器
        OnTraceListener mTraceListener = new OnTraceListener() {
            @Override
            public void onBindServiceCallback(int i, String s) {
            }
            //开启服务回调
            @Override
            public void onStartTraceCallback(int i, String s) {
            }
            //停止服务回调
            @Override
            public void onStopTraceCallback(int i, String s) {
            }
            //开启采集回调
            @Override
            public void onStartGatherCallback(int i, String s) {
            }
            //停止采集回调
            @Override
            public void onStopGatherCallback(int i, String s) {
            }
            //推送回调
            @Override
            public void onPushCallback(byte b, PushMessage pushMessage) {
            }
            @Override
            public void onInitBOSCallback(int i, String s) {
            }
        };
        mTraceClient.startTrace(mTrace,mTraceListener);      //开启服务
        mTraceClient.startGather(mTraceListener);       //开启采集
        mTraceClient.stopTrace(mTrace,mTraceListener);   //停止服务
        mTraceClient.stopGather(mTraceListener);  //停止采集

        HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(tag,serviceId,entityName);
        //设置轨迹查询起止时间
        // 开始时间(单位：秒)
        long startTime = System.currentTimeMillis() / 1000 - 12 * 60 * 60;
        // 结束时间(单位：秒)
        long endTime = System.currentTimeMillis() / 1000;
        // 设置开始时间
        historyTrackRequest.setStartTime(startTime);
        // 设置结束时间
        historyTrackRequest.setEndTime(endTime);
        // 初始化轨迹监听器
        OnTrackListener mTrackListener = new OnTrackListener() {
            // 历史轨迹回调
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {}
        };
        mTraceClient.queryHistoryTrack(historyTrackRequest, mTrackListener);        // 查询历史轨迹

    }


    //内部类，继承了PoiOverlay
    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            return true;
        }
    }

    //设置个性化地图config文件路径
    public static void setMapCustonFile(Context context, String PATH) {

        FileOutputStream out = null;
        InputStream inputStream = null;
        String moduleName = null;
        try {
            inputStream = context.getAssets()
                    .open(PATH);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            moduleName = context.getFilesDir().getAbsolutePath();
            File f = new File(moduleName + "/" + PATH);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            out = new FileOutputStream(f);
            out.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MapView.setCustomMapStylePath(moduleName + "/" + PATH);
    }

    //设置个性化
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gexinghua:
                if (GXH == true) {
                    mGexinghua.setTextColor(this.getResources().getColor(R.color.colorAccent));
                    //mGexinghua.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ic_launcher_background));
                    //设置开启个性化地图
                    MapView.setMapCustomEnable(true);
                    mGexinghua.setText("关闭个性化地图");
                    GXH = false;
                } else if (GXH == false) {
                    mGexinghua.setTextColor(this.getResources().getColor(R.color.colorAccent));
                    //mGexinghua.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ic_launcher_background));
                    //设置关闭个性化地图
                    MapView.setMapCustomEnable(false);
                    mGexinghua.setText("开启个性化地图");
                    GXH = true;
                }
                break;
        }
    }

    //获取自己的位置信息并封装到LatLng对象中
    private void navigateTo(BDLocation location) {
        if (isFirstLocation) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocation = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    //添加别人的位置信息
    public void addOtherLocation(double latitute, double longtitute, Bitmap touxiang) {
        Resources r = this.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(r, R.mipmap.icon_gcoding);  //红点，到时候需要换
        //构建Market图标
        othersCurrentMarker = BitmapDescriptorFactory
                .fromBitmap(mergeBitmap(bmp, touxiang));
        //定义Market坐标点
        LatLng point = new LatLng(latitute, longtitute);

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(othersCurrentMarker);
        //在地图上添加Market，并显示
        baiduMap.addOverlay(option);
    }

    //将两张图片合并为一张图片 用作头像
    private Bitmap mergeBitmap(Bitmap firstBitmap, Bitmap secondBitmap) {
        int Width = firstBitmap.getWidth();
        int Height = firstBitmap.getHeight();
        secondBitmap = zoomImage(secondBitmap, Width, Height);
        Bitmap bitmap = Bitmap.createBitmap(Width, Height * 2,
                firstBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(secondBitmap, new Matrix(), null);
        canvas.drawBitmap(firstBitmap, 0, Height, null);
        return bitmap;
    }

    //获取图片的信息
    private Bitmap zoomImage(Bitmap bgimage, int newWidth, int newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        mPoiSearch.destroy();

        baiduMap.setMyLocationEnabled(false);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mLocationClient = new LocationClient(getApplicationContext());
        MyLocationListener myLocationListener = new MyLocationListener();

        //声明LocationClient类实例并配置定位参数
        LocationClientOption option = new LocationClientOption();
        BDLocation bdLocation = new BDLocation();

        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);   //基础模式
        option.setIsNeedAltitude(true);

        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        option.setOpenAutoNotifyMode(1000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);

        //注册监听函数
        mLocationClient.registerLocationListener(myLocationListener);

        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(option);

        mLocationClient.onReceiveLocation(bdLocation);  //获取实时定位信息
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    public class MyLocationListener extends BDAbstractLocationListener
            implements com.example.mainapp.lbs.MyLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {

            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation
                    || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);

                BDLocation location = new BDLocation();

                double latitude = location.getLatitude();    //获取纬度信息
                double longitude = location.getLongitude();    //获取经度信息
                float radius = location.getRadius();    //获取定位精度，默认值为0.0f
                String coorType = location.getCoorType();//获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
                int errorCode = location.getLocType();   //获取定位类型、定位错误返回码
                String locationDescribe = location.getLocationDescribe();    //获取位置描述信息

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
                    currentPosition.append("经线：").append(bdLocation.getLongitude()).append("\n");
                    currentPosition.append("定位方式：");
                    if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                        currentPosition.append("GPS");
                    } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                        currentPosition.append("网络");
                    }
                    positionText.setText(currentPosition);
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }


    //搜索功能检索监听器
    OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            // 没有找到检索结果
            if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(LbsActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            baiduMap.clear();   //清除百度地图
            PoiOverlay poiOverlay = new MyPoiOverlay(baiduMap);  //创建PoiOverlay对象
            baiduMap.setOnMarkerClickListener(poiOverlay);   //全景监听对象
            poiOverlay.setData(poiResult);  //设置Poi检索数据
            //将poiOverlay添加至地图并缩放至合适级别
            poiOverlay.addToMap();
            poiOverlay.zoomToSpan();
        }
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiResult) {
            if(poiResult.error != SearchResult.ERRORNO.NO_ERROR){
                Toast.makeText(getApplicationContext(),"Sorry,未找到结果",Toast.LENGTH_SHORT).show();
            } else{
                LatLng latLng = poiResult.getLocation();
                double longitude = latLng.longitude;
                double latitude = latLng.latitude;
                Intent intent = new Intent(getApplicationContext(), PanoramaDemoActivityMain.class);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude",longitude);
                intent.putExtra("uid",poiResult.getUid());
                startActivity(intent);
            }
        }
        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }
        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    };
    //6.0之后要动态获取权限，重要！！！
    protected void judgePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

            // sd卡权限
            String[] SdCardPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, SdCardPermission, 100);
            }

            //手机状态权限
            String[] readPhoneStatePermission = {Manifest.permission.READ_PHONE_STATE};
            if (ContextCompat.checkSelfPermission(this, readPhoneStatePermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, readPhoneStatePermission, 200);
            }

            //定位权限
            String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ContextCompat.checkSelfPermission(this, locationPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, locationPermission, 300);
            }

            String[] ACCESS_COARSE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
            if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, ACCESS_COARSE_LOCATION, 400);
            }


            String[] READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, READ_EXTERNAL_STORAGE, 500);
            }

            String[] WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_STORAGE, 600);
            }

        } else {
            //doSdCardResult();
        }
        //LocationClient.reStart();
    }
}
