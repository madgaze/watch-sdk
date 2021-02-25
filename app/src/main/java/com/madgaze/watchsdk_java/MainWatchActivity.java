package com.madgaze.watchsdk_java;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.madgaze.watchsdk.MGWatch;
import com.madgaze.watchsdk.WatchActivity;
import com.madgaze.watchsdk.WatchException;
import com.madgaze.watchsdk.WatchGesture;

import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainWatchActivity extends WatchActivity {
    private final String TAG = MainWatchActivity.class.getSimpleName();

    public final WatchGesture[] REQUIRED_WATCH_GESTURES = {
            WatchGesture.FINGER_SNAP,
            WatchGesture.THUMBTAP_INDEX_MIDDLE,
            WatchGesture.FOREARM_LEFT,
            WatchGesture.FOREARM_LEFT_2,
            WatchGesture.FOREARM_RIGHT,
            WatchGesture.HANDBACK_UP,
            WatchGesture.THUMBTAP_INDEX,
            WatchGesture.MOVE_FOREARM_DOWN,
            WatchGesture.THUMBTAP_MIDDLE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_main);
    }

    @Override
    public void onPause(){
        super.onPause();

        if (MGWatch.isWatchGestureDetecting(this))
            MGWatch.stopWatchGestureDetection(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (MGWatch.isServiceReady(this))
            tryStartDetection();
    }

    @Override
    public void onWatchGestureReceived(WatchGesture gesture) {
        Log.d(TAG, "onWatchGestureReceived: "+gesture.name());
        setResultText(gesture);
    }

    @Override
    public void onWatchGestureError(WatchException error) {
        Log.d(TAG, "onWatchGestureError: "+error.getMessage());
        setStatusText(error.getMessage());
    }

    @Override
    public void onWatchDetectionOn() {
        setStatusText("Listening");
    }

    @Override
    public void onWatchDetectionOff() {
        setStatusText("Idle");
    }

    @Override
    public void onServiceReady() {
        setStatusText("Service Connected");
        tryStartDetection();
    }

    @Override
    protected WatchGesture[] getRequiredWatchGestures() {
        return REQUIRED_WATCH_GESTURES;
    }

    private void tryStartDetection(){
        if (!MGWatch.isGesturesTrained(this)) {
            setStatusText("Training Required");
            return;
        }

        if (!MGWatch.isWatchGestureDetecting(this)) {
            MGWatch.startWatchGestureDetection(this);
        }
    }

    private void setStatusText(String text){
        setText(R.id.status, "Status: " + text);
    }

    private void setResultText(final WatchGesture gesture){
        setText(R.id.result, gesture.toString());
    }

    public void setText(final int resId, final String text){
        if (Looper.myLooper() == Looper.getMainLooper()) {
            TextView textView = ((TextView) findViewById(resId));
            if (textView != null)
                textView.setText(text);
        } else runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = ((TextView)findViewById(resId));
                if (textView != null)
                    textView.setText(text);
            }
        });
    }

}