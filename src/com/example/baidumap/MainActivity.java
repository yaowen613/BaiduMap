package com.example.baidumap;

import java.lang.reflect.Method;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.baidumap.MyOrientationListener.OnOrientationListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

/**
 * 
 * MapView的基本用法
 * 
 * @author yaowen.wang
 */

public class MainActivity extends Activity {

	MapView mMapView = null;// 地图控件
	BaiduMap mBaiduMap;// 地图实例
	LocationClient mLocationClient;// 定位的客户端
	MyLocationListener mMyLocationListener;// 定位的监听器
	LocationMode mCurrentMode = LocationMode.NORMAL;// 当前定位的模式
	private volatile boolean isFristLocation = true;// 是否是第一次定位
	// 最新一次的经纬度
	private double mCurrentLantitude;
	private double mCurrentLongitude;
	// 当前的精度
	private float mCurrentAccracy;
	// 方向传感器的监听器
	private MyOrientationListener myOrientationListener;
	private int mXDirection;// 方向传感器X方向的值
	// 地图定位的模式
	private String[] mStyles = new String[] { "地图模式--正常", "地图模式--跟随", "地图模式--罗盘" };
	private int mCurrentStyle = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// 設置是否全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		// 第一次定位
		isFristLocation = true;
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		// 获得地图的实例
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		// 初始化定位
		initMyLocation();
		// 初始化传感器
		initOritationListener();

	}

	/**
	 * 初始化方向传感器
	 */
	private void initOritationListener() {
		// TODO Auto-generated method stub
		myOrientationListener = new MyOrientationListener(getApplicationContext());
		myOrientationListener.setOnOrientationListener(new OnOrientationListener() {
			@Override
			public void onOrientationChanged(float x) {
				mXDirection = (int) x;

				// 构造定位数据
				MyLocationData locData = new MyLocationData.Builder().accuracy(mCurrentAccracy)
						// 此处设置开发者获取到的方向信息，顺时针0-360
						.direction(mXDirection).latitude(mCurrentLantitude).longitude(mCurrentLongitude).build();
				// 设置定位数据
				mBaiduMap.setMyLocationData(locData);
				// 设置自定义图标
				BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
				MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
				mBaiduMap.setMyLocationConfigeration(config);

			}
		});
	}

	/**
	 * 初始化定位相关控件
	 */
	private void initMyLocation() {
		// TODO Auto-generated method stub
		// 定位初始化
		mLocationClient = new LocationClient(this);
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		// 设置定位的相关配置
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);
	}

	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation arg0) {
			// TODO Auto-generated method stub
			// map view 销毁后不在处理新接收的位置
			if (arg0 == null || mMapView == null)
				return;
			// 构造定位数据
			MyLocationData locData = new MyLocationData.Builder().accuracy(arg0.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(mXDirection).latitude(arg0.getLatitude()).longitude(arg0.getLongitude()).build();
			mCurrentAccracy = arg0.getRadius();
			// 设置定位数据
			mBaiduMap.setMyLocationData(locData);
			mCurrentLantitude = arg0.getLatitude();
			mCurrentLongitude = arg0.getLongitude();
			// 设置自定义图标
			BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
			MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
			mBaiduMap.setMyLocationConfigeration(config);
			// 第一次定位时，将地图位置移动到当前位置
			if (isFristLocation) {
				isFristLocation = false;
				LatLng ll = new LatLng(arg0.getLatitude(), arg0.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 默认点击menu菜单，菜单项不现实图标，反射强制其显示
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {

		if (featureId == Window.FEATURE_OPTIONS_PANEL && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}

		}
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.id_menu_map_common:
			// 普通地图
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			break;
		case R.id.id_menu_map_site:// 卫星地图
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.id_menu_map_traffic:
			// 开启交通图

			if (mBaiduMap.isTrafficEnabled()) {
				item.setTitle("开启实时交通");
				mBaiduMap.setTrafficEnabled(false);
			} else {
				item.setTitle("关闭实时交通");
				mBaiduMap.setTrafficEnabled(true);
			}
			break;
		case R.id.id_menu_map_myLoc:
			center2myLoc();
			break;
		case R.id.id_menu_map_style:
			mCurrentStyle = (++mCurrentStyle) % mStyles.length;
			item.setTitle(mStyles[mCurrentStyle]);
			// 设置自定义图标
			switch (mCurrentStyle) {
			case 0:
				mCurrentMode = LocationMode.NORMAL;
				break;
			case 1:
				mCurrentMode = LocationMode.FOLLOWING;
				break;
			case 2:
				mCurrentMode = LocationMode.COMPASS;
				break;
			}
			BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
			MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
			mBaiduMap.setMyLocationConfigeration(config);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 地图移动到我的位置,此处可以重新发定位请求，然后定位； 直接拿最近一次经纬度，如果长时间没有定位成功，可能会显示效果不好
	 */
	private void center2myLoc() {
		LatLng ll = new LatLng(mCurrentLantitude, mCurrentLongitude);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	@Override
	protected void onStart() {
		// 开启图层定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		// 开启方向传感器
		myOrientationListener.start();
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();

		// 关闭方向传感器
		myOrientationListener.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		mMapView = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

}
