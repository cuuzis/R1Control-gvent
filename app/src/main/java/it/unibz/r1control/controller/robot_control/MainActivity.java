package it.unibz.r1control.controller.robot_control;

import it.unibz.r1control.R;
import it.unibz.r1control.model.data.SensorValues;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private BluetoothConnection myConnection;
    private TouchSpeedController speedCtrl;

    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;

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
            speedCtrl = new TouchSpeedController(findViewById(R.id.root));
            myConnection = new BluetoothConnection(this, speedCtrl, mBluetoothAdapter);
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
            speedCtrl = new TouchSpeedController(findViewById(R.id.root));
            myConnection = new BluetoothConnection(this, speedCtrl, mBluetoothAdapter);
        }
        else {
            ((TextView) findViewById(R.id.btInfo) ).setText("not enabled");
        }
    }

    private void promptToEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }



    // from MAX
    //to get the current sensor values
    public SensorValues getSensorValues() {
        return sensorValues;
    }
}