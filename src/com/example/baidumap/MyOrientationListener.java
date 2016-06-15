package com.example.baidumap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MyOrientationListener implements SensorEventListener {

	private Context context;
	private SensorManager sensorManager;
	private Sensor sensor;

	private float lastX;

	private OnOrientationListener onOrientationListener;

	public MyOrientationListener(Context context) {
		this.context = context;
	}

	// 寮�濮�
	public void start() {
		// 鑾峰緱浼犳劅鍣ㄧ鐞嗗櫒
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			// 鑾峰緱鏂瑰悜浼犳劅鍣�
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		// 娉ㄥ唽
		if (sensor != null) {// SensorManager.SENSOR_DELAY_UI
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		}

	}

	// 鍋滄妫�娴�
	public void stop() {
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// 鎺ュ彈鏂瑰悜鎰熷簲鍣ㄧ殑绫诲瀷
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			// 杩欓噷鎴戜滑鍙互寰楀埌鏁版嵁锛岀劧鍚庢牴鎹渶瑕佹潵澶勭悊
			float x = event.values[SensorManager.DATA_X];

			if (Math.abs(x - lastX) > 1.0) {
				onOrientationListener.onOrientationChanged(x);
			}
			// Log.e("DATA_X", x+"");
			lastX = x;

		}
	}

	public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
		this.onOrientationListener = onOrientationListener;
	}

	public interface OnOrientationListener {
		void onOrientationChanged(float x);
	}

}
