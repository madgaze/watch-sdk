package com.madgaze.watchsdk_java;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.madgaze.watchsdk.MGWatch;
import com.madgaze.watchsdk.WatchActivity;
import com.madgaze.watchsdk.WatchException;
import com.madgaze.watchsdk.WatchGesture;

import androidx.appcompat.widget.Toolbar;

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
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!MGWatch.isWatchGestureDetecting(this)) {
            MGWatch.startWatchGestureDetection(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWatchGestureReceived(WatchGesture gesture) {
        Log.d(TAG, "onWatchGestureReceived: "+gesture.name());
    }

    @Override
    public void onWatchGestureError(WatchException error) {
        Log.d(TAG, "onWatchGestureError: "+error.getMessage());
    }

    @Override
    public void onWatchDetectionOn() {
        Log.d(TAG, "onWatchDetectionOn: ");
    }

    @Override
    public void onWatchDetectionOff() {
        Log.d(TAG, "onWatchDetectionOff: ");
    }

    @Override
    protected WatchGesture[] getRequiredWatchGestures() {
        return REQUIRED_WATCH_GESTURES;
    }

}