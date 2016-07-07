package it.unibz.r1control.controller.robot_control;

import it.unibz.r1control.R;
import it.unibz.r1control.model.data.MagnetometerData;
import it.unibz.r1control.model.data.MotorControlData;
import it.unibz.r1control.model.data.SensorValues;
import it.unibz.r1control.model.data.TemperatureData;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private BluetoothConnection myConnection;
    private TouchSpeedController speedCtrl;

    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private final int TIME_INTERFACE_REFRESH = 1000; //miliseconds

    private  SensorValues sensorValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            System.out.println("Device does not support Bluetooth");
            ((TextView) findViewById(R.id.btInfo) ).setText("not supported");
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            promptToEnableBluetooth();
        }
        else {
            connectToRobot();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myConnection.closeBluetoothConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_transparency:
                //showTranspSeekBar();
                return true;
            case R.id.action_enable_BT:
                promptToEnableBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            connectToRobot();
        }
        else {
            ((TextView) findViewById(R.id.btInfo) ).setText("not enabled");
        }
    }

    private void promptToEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void connectToRobot() {
        speedCtrl = new TouchSpeedController(findViewById(R.id.root));
        myConnection = new BluetoothConnection(this, speedCtrl, mBluetoothAdapter);
        //setting values on intarface after initialiazing
        new setSensorValues().execute();
    }



    // from MAX
    //to get the current sensor values
    public SensorValues getSensorValues() {
        return sensorValues;
    }

    //method that creates a threat to update the values on the interface, since it will be a non-blocking it has to return to the UI threat also
    public void OnNewSensorData(final SensorValues data) {
            runOnUiThread(new Runnable() {
                public void run() {
                    // Ultrasonic Sensors
                    short value0 = (short)Math.abs((int)data.getUsData(0));
                    short value1 = (short)Math.abs((int)data.getUsData(1));
                    short value2 = (short)Math.abs((int)data.getUsData(2));
                    short value3 = (short)Math.abs((int)data.getUsData(3));
                    short value4 = (short)Math.abs((int)data.getUsData(4));
                    short value5 = (short)Math.abs((int)data.getUsData(5));
                    short value6 = (short)Math.abs((int)data.getUsData(6));
                    short value7 = (short)Math.abs((int)data.getUsData(7));

                    View view = findViewById(R.id.root);
                    ((TextView) view.findViewById(R.id.sensor1)).setText(String.valueOf(value2));
                    ((TextView) view.findViewById(R.id.sensor2)).setText(String.valueOf(value3));
                    ((TextView) view.findViewById(R.id.sensor3)).setText(String.valueOf(value4));
                    ((TextView) view.findViewById(R.id.sensor4)).setText(String.valueOf(value5));
                    ((TextView) view.findViewById(R.id.sensor5)).setText(String.valueOf(value1));
                    ((TextView) view.findViewById(R.id.sensor6)).setText(String.valueOf(value0));
                    ((TextView) view.findViewById(R.id.sensor7)).setText(String.valueOf(value7));
                    ((TextView) view.findViewById(R.id.sensor8)).setText(String.valueOf(value6));

                    // InfraRed Sensors
                    //generalFragment.setInfraRedSensors(data.getIrData(0), data.getIrData(1));
                    String value = "1: "+data.getIrData(0) +",2:"+data.getIrData(1);
                    ((TextView) view.findViewById(R.id.irValue1)).setText(value);

                    // Magnetometer
                    //generalFragment.setMagnometerData(data.getMgData());  //Magnometer
                    MagnetometerData magnometerData = data.getMgData();
                    value = "Bearing: "+ magnometerData.getBearing();
                    value = value +" Pitch: "+ magnometerData.getPitch();
                    value = value +" Roll: "+ magnometerData.getRoll();
                    ((TextView) view.findViewById(R.id.bearingValue)).setText(String.valueOf(value));

                    // Motor
                    //generalFragment.setMotorControlData(data.getMcData()); //MotorControlData
                    MotorControlData motorControlData = data.getMcData();
                    value = "Left current: "+  motorControlData.getLeftCurrent();
                    value = value +" Right current: "+ motorControlData.getRightCurrent();
                    value = value +" Voltage: "+ motorControlData.getVoltage();
                    ((TextView) view.findViewById(R.id.leftCurrValue)).setText(String.valueOf(value));

                    // Temperature
                    //generalFragment.setTemperature( data.getTmpData()); //get temperature
                    TemperatureData temperatureData = data.getTmpData();
                    value = temperatureData.getTemperatureData(0)+" C";
                    value = value+" "+temperatureData.getTemperatureData(1)+" C";
                    ((TextView) view.findViewById(R.id.temperatureValue1)).setText(String.valueOf(value));


                    //isRunOnUiThreadFinished = true;
                }

                protected void onPostExecute() {
                    System.out.println("Finished executing");

                }

            });


    }

    //Async trask in charge of setting the values on the interface with a sleep of 1 second =TIME_INTERFACE_REFRESH
    private class setSensorValues extends AsyncTask<Void , Void, Void> {

        protected Void doInBackground(Void ... params) {

            while (true)
            {
                try {
                    Thread.sleep(TIME_INTERFACE_REFRESH);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                //befire sendin values checking if is connected to the bluethooth
                if(myConnection.isConnected() && myConnection.isValueSetted())
                {

                    sensorValues = myConnection.getCurrentValues();
                    OnNewSensorData(sensorValues);

                }

            }

        }

    }
}