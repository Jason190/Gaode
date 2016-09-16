package com.example.jason.gaode;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main2Activity extends Activity  implements LocationSource,
        AMapLocationListener {
    private MapView mapView=null;
    private AMap amap=null;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private LatLng lastLocation=null;
    private SQLiteDatabase db=null;
    private SimpleDateFormat sDateFormat    =   new    SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
    private int topSpeed=100;//km/h

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mapView=(MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        init();
        initDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDB();
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
    double lasttime=0;
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener!=null&&aMapLocation!=null){

            if (aMapLocation!=null&&aMapLocation.getErrorCode()==0){
                mListener.onLocationChanged(aMapLocation);

                if (lastLocation==null){
                    lastLocation=new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    lasttime= aMapLocation.getTime();
                    amap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(lastLocation,18f,0f,0f)));
                }
                if (lastLocation.latitude!=aMapLocation.getLatitude()||lastLocation.longitude!=aMapLocation.getLongitude()){
                    double distance=coorNageCalcDistance(lastLocation,new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
                    double duringTime=aMapLocation.getTime()-lasttime;
                    double speed=(distance/(duringTime/1000)/1000*3600);
                    double ss=speed/topSpeed*255;
                    int speedColor=(int)ss;
                    if (speed>topSpeed)
                    {
                        speedColor=255;
                    }

                    amap.addPolyline(new PolylineOptions().add(lastLocation,new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude())).width(15).color(Color.rgb(255-speedColor,speedColor,0)));
                    ContentValues cv=new ContentValues();
                    editPositionList(cv,null,123,"jason",Double.NaN,Double.NaN,aMapLocation.getLatitude(),aMapLocation.getLongitude(),sDateFormat.format(new Date()),null );
                    TextView tv=(TextView)findViewById(R.id.texter);
                    tv.setText(String.format("%3.2f",speed) +",颜色"+speedColor+",type"+aMapLocation.getLocationType()+",卫星"+aMapLocation.getSatellites()+",精度"+aMapLocation.getAccuracy()+",速度"+aMapLocation.getSpeed()+",地址"+aMapLocation.getAddress()+",街道"+aMapLocation.getStreet());
                    try{
                    db.insert("test1",null,cv);}
                    catch (Exception e){}
                }
                lastLocation=new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                lasttime= aMapLocation.getTime();
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



    public dian gaussProjCal(double longitude, double latitude)
    {
        dian point=new dian();
        int ProjNo = 0; int ZoneWide; ////带宽
        double longitude1, latitude1, longitude0, latitude0, X0, Y0, xval, yval;
        double a, f, e2, ee, NN, T, C, A, M, iPI;
        iPI = 0.0174532925199433; ////3.1415926535898/180.0;
        ZoneWide = 6; ////6度带宽
        a = 6378245.0; f = 1.0 / 298.3; //54年北京坐标系参数
        ////a=6378140.0; f=1/298.257; //80年西安坐标系参数
        ProjNo = (int)(longitude / ZoneWide);
        longitude0 = ProjNo * ZoneWide + ZoneWide / 2;
        longitude0 = longitude0 * iPI;
        latitude0 = 0;
        longitude1 = longitude * iPI; //经度转换为弧度
        latitude1 = latitude * iPI; //纬度转换为弧度
        e2 = 2 * f - f * f;
        ee = e2 * (1.0 - e2);
        NN = a / Math.sqrt (1.0 - e2 * Math.sin(latitude1) * Math.sin(latitude1));
        T = Math.tan(latitude1) * Math.tan(latitude1);
        C = ee * Math.cos(latitude1) * Math.cos(latitude1);
        A = (longitude1 - longitude0) * Math.cos(latitude1);


        M = a * ((1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * latitude1 - (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2
                * e2 / 1024) * Math.sin(2 * latitude1)
                + (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * Math.sin(4 * latitude1) - (35 * e2 * e2 * e2 / 3072) * Math.sin(6 * latitude1));
        xval = NN * (A + (1 - T + C) * A * A * A / 6 + (5 - 18 * T + T * T + 72 * C - 58 * ee) * A * A * A * A * A / 120);
        yval = M + NN * Math.tan(latitude1) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24
                + (61 - 58 * T + T * T + 600 * C - 330 * ee) * A * A * A * A * A * A / 720);
        X0 = 1000000L * (ProjNo + 1) + 500000L;
        Y0 = 0;
        xval = xval + X0; yval = yval + Y0;
        point.setX(yval);//北方向
        point.setY(xval);
        return point;
        //X = xval;
        //Y = yval;
    }
    //单位是度
    public double coorNageCalcDistance(LatLng point1,LatLng point2){
        dian pointA=gaussProjCal(point1.longitude,point1.latitude);
        dian pointB=gaussProjCal(point2.longitude,point2.latitude);

        return zuoBiaoFanSuan(pointA,pointB);
    }
    //直角坐标系下计算
    private double zuoBiaoFanSuan(dian point1,dian point2){
        double dx=point1.getX()-point2.getX();
        double dy=point1.getY()-point2.getY();
        double ddx=Math.pow(dx,2);
        double ddy=Math.pow(dy,2);
        double distance=Math.sqrt(ddx+ddy);
        return distance;
    }
    private void initDB(){
        String path= Environment.getExternalStorageDirectory().getPath();

//        db=SQLiteDatabase.openOrCreateDatabase(path+ File.separator+"database.db",null);
        DBhelper dBhelper=new DBhelper(this,path+File.separator+"database.db",null,1);
        db= dBhelper.getWritableDatabase();
    }

    private ContentValues editPositionList(ContentValues cv, Integer id, int holderid, String holderName, double latGPS, double lonGPS, double latAmap, double
                                           lonAmap, String date, String time){
        cv.put("ID",id);
        cv.put("holderID",holderid);
        cv.put("holderName",holderName);
        cv.put("latGPS",latGPS);
        cv.put("lonGPS",lonGPS);
        cv.put("latAmap",latAmap);
        cv.put("lonAmap",lonAmap);
        cv.put( "date",date );
        cv.put("time",time);
        return cv;
    }

}
