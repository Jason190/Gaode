package com.example.jason.gaode;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;

public class Main2Activity extends Activity  implements LocationSource,
        AMapLocationListener {
    private MapView mapView=null;
    private AMap amap=null;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private LatLng lastLocation=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mapView=(MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        if (amap==null){
            amap=mapView.getMap();
            setUpMap();

        }
    }
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.DKGRAY);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 207, 226, 243));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        amap.setMyLocationStyle(myLocationStyle);
        amap.setLocationSource(this);// 设置定位监听
        amap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        amap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener!=null&&aMapLocation!=null){
            if (aMapLocation!=null&&aMapLocation.getErrorCode()==0){
                mListener.onLocationChanged(aMapLocation);

                if (lastLocation==null){
                    lastLocation=new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    amap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(lastLocation,18f,0f,0f)));
                }
                if (lastLocation.latitude!=aMapLocation.getLatitude()||lastLocation.longitude!=aMapLocation.getLongitude()){
                    amap.addPolyline(new PolylineOptions().add(lastLocation,new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude())).width(15).color(Color.CYAN));
                }
                lastLocation=new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());

            }
            else {
                String errText="定位失败"+aMapLocation.getErrorCode()+":"+aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
        else {
            try {

            }
            catch (Exception e){

            }
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}
