package it.unibz.r1control.controller.robot_control;

import android.view.MotionEvent;
import android.view.View;

import java.io.Console;

import it.unibz.r1control.model.data.MotorSpeed;
import it.unibz.r1control.model.data.SensorValues;

/**
 * Implements a Speed controller by listening to touch inputs on a given View. This View is split in
 * a left and a right part to set the speed of the left and right wheel, respectively. In each
 * region, we check the y-coordinate in relation to the view's full height. Touching at the bottom
 * will tell the corresponding wheel to turn backward at full speed, while touching at the top will
 * tell it to turn forward at full speed. Similarly, touching at the center will tell the wheel to
 * stop.
 *
 * Created by Matthias on 10.12.2015.
 */
public class TouchSpeedController implements View.OnTouchListener {

    private static final byte STAY = (byte)128;

    // Bitsets encoding wheels whose speed was set
    private static final int LEFT_SPEED_SET  = 0b10;
    private static final int RIGHT_SPEED_SET = 0b01;
    private static final int BOTH_SPEEDS_SET = 0b11;

    private static final int MAX_PROXIMITY_ULTRASONIC = 30;
    private static final int MAX_PROXIMITY_INFRARED = 250; //value is inverse to distance

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
                    if (noCollision(speed)) {
                        requestedSpeed.setLeftSpeed(speed);
                    }
                } else {
                    speedsBitSet |= RIGHT_SPEED_SET;
                    if (noCollision(speed)) {
                        requestedSpeed.setRightSpeed(speed);
                    }
                }
            }
        }
        return true;
    }


    // collision detection
    public boolean noCollision(byte speed)
    {

        System.out.println("Speed byte:" + speed);
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

        //checking front sensors if moving forward
        if (  (short)speed > -120  &&  (short)speed < 0  ) {
            if (ultra1 <= MAX_PROXIMITY_ULTRASONIC
                    || ultra2 <= MAX_PROXIMITY_ULTRASONIC
                    || ultra3 <= MAX_PROXIMITY_ULTRASONIC
                    || ultra4 <= MAX_PROXIMITY_ULTRASONIC
                    || infra1 >= MAX_PROXIMITY_INFRARED
                    || infra2 >= MAX_PROXIMITY_INFRARED) {
                return false;
            }
        }
        //checking back sensors if moving backward
        if (  (short)speed > 0  &&  (short)speed < 120  ) {
            if (       ultra5 <= MAX_PROXIMITY_ULTRASONIC
                    || ultra6 <= MAX_PROXIMITY_ULTRASONIC
                    || ultra7 <= MAX_PROXIMITY_ULTRASONIC
                    || ultra8 <= MAX_PROXIMITY_ULTRASONIC) {
                return false;
            }
        }
        return true;


    }
}
