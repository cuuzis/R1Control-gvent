package it.unibz.r1control.controller.robot_control;

import android.view.MotionEvent;
import android.view.View;

import java.io.Console;

import it.unibz.r1control.model.data.MotorSpeed;
import it.unibz.r1control.model.data.SensorValues;

public class TouchSpeedController implements View.OnTouchListener {

    private static final byte STAY = (byte)128;

    // Bitsets encoding wheels whose speed was set
    private static final int LEFT_SPEED_SET  = 0b10;
    private static final int RIGHT_SPEED_SET = 0b01;
    private static final int BOTH_SPEEDS_SET = 0b11;

    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    public static final int MAX_PROXIMITY_INFRARED = 250; //value is inverse to distance
    public static final int MAX_PROXIMITY_S1 = 35;
    public static final int MAX_PROXIMITY_S2 = 30;
    public static final int MAX_PROXIMITY_S3 = 30;
    public static final int MAX_PROXIMITY_S4 = 35;
    public static final int MAX_PROXIMITY_S5 = 40;
    public static final int MAX_PROXIMITY_S6 = 40;
    public static final int MAX_PROXIMITY_S7 = 30;
    public static final int MAX_PROXIMITY_S8 = 30;

    private MainActivity mainActivity;
    private View touchableArea;
    private MotorSpeed requestedSpeed;

    private float center;
    private float height;

    public TouchSpeedController(MainActivity mainActivity, View touchableArea) {
        this.mainActivity = mainActivity;
        this.touchableArea = touchableArea;
        requestedSpeed = new MotorSpeed();
    }


    public void start() {
        touchableArea.setOnTouchListener(this);
    }


    public MotorSpeed getRequestedSpeed() {
        return requestedSpeed;
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (center == 0)
            center = v.getWidth() >>> 2;
        if (height == 0)
            height = v.getHeight();
        requestedSpeed.reset();
        if (center > 0 && height > 0 && e.getAction() != MotionEvent.ACTION_UP) {
            int speedsBitSet = 0;
            int count = e.getPointerCount();
            boolean pointerUp = e.getActionMasked() == MotionEvent.ACTION_POINTER_UP;
            for (int i = 0; i < count && speedsBitSet != BOTH_SPEEDS_SET; i++) {
                byte speed = pointerUp && i == e.getActionIndex()
                    ? MotorSpeed.STAY
                    : (byte)(0xFF * (1 - e.getY(i) / height));
                if (e.getX(i) - center < 0) {
                    speedsBitSet |= LEFT_SPEED_SET;
                    if (noCollision(speed, LEFT)) {
                        requestedSpeed.setLeftSpeed(speed);
                    }
                } else {
                    speedsBitSet |= RIGHT_SPEED_SET;
                    if (noCollision(speed, RIGHT)) {
                        requestedSpeed.setRightSpeed(speed);
                    }
                }
            }
        }
        return true;
    }


    // collision detection
    public boolean noCollision(byte speed, int motor)
    {
        // Debug
        if (motor == LEFT) {
        System.out.println("Left speed:" + speed);
        } else {
            System.out.println("Right speed:" + speed);
        }
        System.out.println();


        SensorValues sensorValues = mainActivity.getSensorValues();

        //front left
        int ultra1 = sensorValues.getUsData(2);
        int ultra2 = sensorValues.getUsData(3);
        int infra1 = sensorValues.getIrData(0);

        //front right
        int ultra3 = sensorValues.getUsData(4);
        int ultra4 = sensorValues.getUsData(5);
        int infra2 = sensorValues.getIrData(1);

        //back left
        int ultra5 = sensorValues.getUsData(1);
        int ultra6 = sensorValues.getUsData(0);

        //back right
        int ultra7 = sensorValues.getUsData(7);
        int ultra8 = sensorValues.getUsData(6);

        //checking front sensors when moving forward
        if (  (short)speed > -120  &&  (short)speed < 0  ) {
            if (       ultra1 <= MAX_PROXIMITY_S1
                    || ultra2 <= MAX_PROXIMITY_S2
                    || ultra3 <= MAX_PROXIMITY_S3
                    || ultra4 <= MAX_PROXIMITY_S4
                    ||(infra1 >= MAX_PROXIMITY_INFRARED && motor == RIGHT)
                    ||(infra2 >= MAX_PROXIMITY_INFRARED && motor == LEFT)) {
                return false;
            }
        }
        //checking back sensors when moving backward
        else if (  (short)speed > 0  &&  (short)speed < 120  ) {
            if (      (ultra5 <= MAX_PROXIMITY_S5 && motor == LEFT)
                    || ultra6 <= MAX_PROXIMITY_S6
                    || ultra7 <= MAX_PROXIMITY_S7
                    ||(ultra8 <= MAX_PROXIMITY_S8 && motor == RIGHT)) {
                return false;
            }
        }
        return true;
    }
}
