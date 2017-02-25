package com.mukesh.kanchan.steppermotorcontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import net.calit2.mooc.iot_db410c.db410c_gpiolib.GpioProcessor;


public class RotateMotorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate_motor);

        Intent intent = getIntent();
        final String angleTxt = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String message = ("Rotated: ").concat(angleTxt);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_rotate_motor);
        layout.addView(textView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                GpioProcessor gpioProcessor = new GpioProcessor();

                //stepper motor control GPIO pins
                GpioProcessor.Gpio IN1 = gpioProcessor.getPin23();
                GpioProcessor.Gpio IN2 = gpioProcessor.getPin24();
                GpioProcessor.Gpio IN3 = gpioProcessor.getPin25();
                GpioProcessor.Gpio IN4 = gpioProcessor.getPin26();

                IN1.out();
                IN2.out();
                IN3.out();
                IN4.out();

                //to control speed of motor
                long delayTime = (long)0.001; //1ms

                //Stepper sequence
                int [][]SS = {{0,0,0,1}, {0,0,1,1},
                              {0,0,1,0}, {0,1,1,0},
                              {0,1,0,0}, {1,1,0,0},
                              {1,0,0,0}, {1,0,0,1}};

                // Forward/Reverse (0 = Forward, 1 = Reverse)
                int FR = 0;

                // Step Angle calculation
                // stride angle = 5.625
                // steps = Number of steps in One Revolution  * Gear ratio   .
                // steps= (360°/5.625°)*64 = 64 * 64 =4096
                double SA = 11.38; // 1.8 degrees per step

                int angle = Integer.valueOf(angleTxt);
                // check if (-) degrees for reverse direction
                if (angle < 0) {
                    FR = 1;
                    angle = Math.abs(angle);
                } else FR = 0;

                angle = (int)(angle*SA); // calculate number of steps

                // run stepper sequence
                int step = 0; // temp variable to repeat the loop of stepper sequence
                for (int i=0; i<angle; i++) {
                    step = i % 8;
                    if (FR ==1) step = 7 - step; //run sequence backwards for reverse direction
                    IN1.setValue(SS[step][0]);
                    try {
                        Thread.sleep(delayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IN2.setValue(SS[step][1]);
                    try {
                        Thread.sleep(delayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IN3.setValue(SS[step][2]);
                    try {
                        Thread.sleep(delayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IN4.setValue(SS[step][3]);
                    try {
                        Thread.sleep(delayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
}
