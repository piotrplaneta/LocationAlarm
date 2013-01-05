package edu.uj.pmd.locationalarm.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import edu.uj.pmd.locationalarm.R;

/**
 * User: piotrplaneta
 * Date: 22.11.2012
 * Time: 10:18
 */
public class AlarmActivity extends Activity implements GestureDetector.OnGestureListener {
    private PowerManager.WakeLock wakeLock;
    private boolean alarmShown = false;
    private GestureDetector gestureDetector;
    private int screenHeight;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupView();
        setScreenHeight();
        showAlarm();
        this.gestureDetector = new GestureDetector(this, this);
    }

    private void setScreenHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenHeight = size.y;
    }

    private void setupView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_alarm);
    }

    private void showAlarm() {
        if(!alarmShown) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "alarm");
            this.wakeLock.acquire();
            vibrate();
            Intent intent = getIntent();
            if(intent.getExtras().getBoolean("noSignal")) {
                TextView textView = (TextView) findViewById(R.id.noSignalText);
                textView.setText(getText(R.string.no_signal));
            } else if(intent.getExtras().getBoolean("lowBattery")) {
                TextView textView = (TextView) findViewById(R.id.noSignalText);
                textView.setText(getText(R.string.low_battery));
            } else {
                alarmShown = true;
                TextView textView = (TextView) findViewById(R.id.noSignalText);
                textView.setText(getText(R.string.alarm_text));
            }
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        long[] pattern = { 0, 500, 200 };

        v.vibrate(pattern, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start.getRawY() - finish.getRawY() > (screenHeight/2.5)) {
            ImageView imageView = (ImageView) this.findViewById(R.id.gestureImage);
            imageView.setImageResource(R.drawable.up_pressed);
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start.getRawY() - finish.getRawY() > (screenHeight/2.5)) {
            this.finish();
        }
        return true;
    }

    @Override
    protected void onPause() {
        if(alarmShown) {
            this.finish();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.cancel();
        if(this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
    }
}