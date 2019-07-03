package in.creationdevs.acc_gyro;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements SensorEventListener {
    private float lastX, lastY, lastZ,lastX_gyro,lastY_gyro,lastZ_gyro;
    int counter_gyro;
    float rollingreadings_gyro_x[] = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    float rollingreadings_gyro_y[] = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    float rollingreadings_gyro_z[] = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    int counter_acc;
    float rollingreadings_acc_x[] = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    float rollingreadings_acc_y[] = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    float rollingreadings_acc_z[] = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    float rollingaverage_gyro_x = 0;
    float rollingaverage_gyro_y = 0;
    float rollingaverage_gyro_z = 0;
    float rollingaverage_acc_x = 0;
    float rollingaverage_acc_y = 0;
    float rollingaverage_acc_z = 0;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float deltaX_gyro = 0;
    private float deltaY_gyro = 0;
    private float deltaZ_gyro = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;

    public TextView currentX, currentY, currentZ, maxX, maxY, maxZ,currentX_gyro,currentY_gyro,currentZ_gyro;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // fai! we dont have an accelerometer!
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // success! we have an accelerometer

            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // fai! we dont have an accelerometer!
        }
    }

    public void initializeViews() {
        currentX = findViewById(R.id.textview_currentX);
        currentY = findViewById(R.id.textview_currentY);
        currentZ = findViewById(R.id.textview_currentZ);
        currentX_gyro = findViewById(R.id.textview_currentX_gyro);
        currentY_gyro = findViewById(R.id.textview_currentY_gyro);
        currentZ_gyro = findViewById(R.id.textview_currentZ_gyro);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // clean current values
            // display the current x,y,z accelerometer values
            // display the max x,y,z accelerometer values
            // get the change of the x,y,z values of the accelerometer
            deltaX = event.values[0];
            deltaY = event.values[1];
            deltaZ = event.values[2];
            counter_acc = (counter_acc+1)%10;
            rollingreadings_acc_x[counter_acc] = deltaX;
            rollingreadings_acc_y[counter_acc] = deltaY;
            rollingreadings_acc_z[counter_acc] = deltaZ;

          /*  if(deltaX < 0.1)
                deltaX = 0;
            if(deltaY < 0.1)
                deltaY = 0;
            if(deltaZ < 0.1)
                deltaZ = 0;
           */
            displayCurrentValues();

            // if the change is below 2, it is just plain noise

        }
        else if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // clean current values
            // display the current x,y,z gyroscope values
            // display the max x,y,z gyroscope values
            deltaX_gyro = event.values[0];
            deltaY_gyro = event.values[1];
            deltaZ_gyro = event.values[2];
            counter_gyro = (counter_gyro+1)%10;
            rollingreadings_gyro_x[counter_gyro] = deltaX_gyro;
            rollingreadings_gyro_y[counter_gyro] = deltaY_gyro;
            rollingreadings_gyro_z[counter_gyro] = deltaZ_gyro;
         /*   if(deltaX_gyro < 0.1)
                deltaX_gyro = 0;
            if(deltaY_gyro < 0.1)
                deltaY_gyro = 0;
            if(deltaZ_gyro < 0.1)
                deltaZ_gyro = 0;
            */
            displayCurrentValuesGyro();

            // if the change is below 2, it is just plain noise

        }




    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");

    }

    public void displayCleanValuesGyro(){
        currentZ_gyro.setText("0.0");
        currentY_gyro.setText("0.0");
        currentX_gyro.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }
    public void displayCurrentValuesGyro() {
        currentX_gyro.setText(Float.toString(deltaX_gyro));
        currentY_gyro.setText(Float.toString(deltaY_gyro));
        currentZ_gyro.setText(Float.toString(deltaZ_gyro));
    }
    // display the max x,y,z accelerometer values

    public void potholeFound(View view){
        rollingaverage_gyro_x = 0;
        rollingaverage_gyro_y = 0;
        rollingaverage_gyro_z = 0;
        rollingaverage_acc_x = 0;
        rollingaverage_acc_y = 0;
        rollingaverage_acc_z = 0;
        for(int i=0;i<10;i++) {
            rollingaverage_gyro_x = (float) (rollingaverage_gyro_x + Math.pow((double)rollingreadings_gyro_x[i],2.0));
            rollingaverage_gyro_y = (float)(rollingaverage_gyro_y + Math.pow((double)rollingreadings_gyro_y[i],2.0));
            rollingaverage_gyro_z = (float)(rollingaverage_gyro_z + Math.pow((double)rollingreadings_gyro_z[i],2.0));
            rollingaverage_acc_x = (float)(rollingaverage_acc_x + Math.pow((double)rollingreadings_acc_x[i],2.0));
            rollingaverage_acc_y = (float)(rollingaverage_acc_y + Math.pow((double)rollingreadings_acc_y[i],2.0));
            rollingaverage_acc_z = (float)(rollingaverage_acc_z + Math.pow(rollingreadings_acc_z[i],2.0));
        }
        rollingaverage_gyro_x = (float)Math.sqrt((double)rollingaverage_gyro_x/10f);
        rollingaverage_gyro_y = (float)Math.sqrt((double)rollingaverage_gyro_y/10f);
        rollingaverage_gyro_z = (float)Math.sqrt((double)rollingaverage_gyro_z/10f);
        rollingaverage_acc_x = (float)Math.sqrt((double)rollingaverage_acc_x/10f);
        rollingaverage_acc_y = (float)Math.sqrt((double)rollingaverage_acc_y/10f);
        rollingaverage_acc_z = (float)Math.sqrt((double)rollingaverage_acc_z/10f);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://creationdevs.in//pothole/index.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this,"Pothole Registered",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener(){
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error on URL", Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put("acc_x",Float.toString(rollingaverage_gyro_x));
                map.put("acc_y",Float.toString(rollingaverage_gyro_y));
                map.put("acc_z",Float.toString(rollingaverage_gyro_z));
                map.put("gyro_x",Float.toString(rollingaverage_acc_x));
                map.put("gyro_y",Float.toString(rollingaverage_acc_y));
                map.put("gyro_z",Float.toString(rollingaverage_acc_z));
                map.put("type","1");
                return map;
            }
        };
        queue.add(stringRequest);
    }

    public void notPothole(View view){
        rollingaverage_gyro_x = 0;
        rollingaverage_gyro_y = 0;
        rollingaverage_gyro_z = 0;
        rollingaverage_acc_x = 0;
        rollingaverage_acc_y = 0;
        rollingaverage_acc_z = 0;
        for(int i=0;i<10;i++) {
            rollingaverage_gyro_x = (float) (rollingaverage_gyro_x + Math.pow((double)rollingreadings_gyro_x[i],2.0));
            rollingaverage_gyro_y = (float)(rollingaverage_gyro_y + Math.pow((double)rollingreadings_gyro_y[i],2.0));
            rollingaverage_gyro_z = (float)(rollingaverage_gyro_z + Math.pow((double)rollingreadings_gyro_z[i],2.0));
            rollingaverage_acc_x = (float)(rollingaverage_acc_x + Math.pow((double)rollingreadings_acc_x[i],2.0));
            rollingaverage_acc_y = (float)(rollingaverage_acc_y + Math.pow((double)rollingreadings_acc_y[i],2.0));
            rollingaverage_acc_z = (float)(rollingaverage_acc_z + Math.pow(rollingreadings_acc_z[i],2.0));
        }
        rollingaverage_gyro_x = (float)Math.sqrt((double)rollingaverage_gyro_x/10f);
        rollingaverage_gyro_y = (float)Math.sqrt((double)rollingaverage_gyro_y/10f);
        rollingaverage_gyro_z = (float)Math.sqrt((double)rollingaverage_gyro_z/10f);
        rollingaverage_acc_x = ((float)Math.sqrt((double)rollingaverage_acc_x/10f));
        rollingaverage_acc_y = (float)Math.sqrt((double)rollingaverage_acc_y/10f);
        rollingaverage_acc_z = (float)Math.sqrt((double)rollingaverage_acc_z/10f);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://creationdevs.in//pothole/index.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this,"'NotPothole' Registered",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener(){
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error on URL", Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put("acc_x",Float.toString(rollingaverage_gyro_x));
                map.put("acc_y",Float.toString(rollingaverage_gyro_y));
                map.put("acc_z",Float.toString(rollingaverage_gyro_z));
                map.put("gyro_x",Float.toString(rollingaverage_acc_x));
                map.put("gyro_y",Float.toString(rollingaverage_acc_y));
                map.put("gyro_z",Float.toString(rollingaverage_acc_z));
                map.put("type","0");
                return map;
            }
        };
        queue.add(stringRequest);
    }

}

