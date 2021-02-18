package com.madgaze.watchsdk_java;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.madgaze.watchsdk.MobileActivity;
import com.madgaze.watchsdk.WatchException;
import com.madgaze.watchsdk.WatchGesture;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends MobileActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    public final WatchGesture[] REQUIRED_WATCH_GESTURES = {
            WatchGesture.FINGER_SNAP,
            WatchGesture.FINGER_INDEX_MIDDLE,
            WatchGesture.ARM_LEFT,
            WatchGesture.ARM_LEFT_2,
            WatchGesture.ARM_RIGHT,
            WatchGesture.HANDBACK_UP,
            WatchGesture.FINGER_INDEX,
            WatchGesture.MOVE_ARM_DOWN,
            WatchGesture.FINGER_MIDDLE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
//        if (id == R.id.action_connect) {
//            goToConnectPage();
//            return true;
//        } else if (id == R.id.action_train) {
//            goToTrainingPage(new byte[] { 11 });
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
    public void onMGWatchServiceReady() {
        tryStartDetection();
    }

    @Override
    public void onPause(){
        super.onPause();

        if (MGWatch.isWatchGestureDetecting(this))
            MGWatch.stopGestureDetection(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (MGWatch.isMGWatchServiceReady(this))
            tryStartDetection();
    }

    @Override
    public void onWatchConnected() {
        Log.d(TAG, "onWatchConnected: ");
    }

    @Override
    public void onWatchDisconnected() {
        Log.d(TAG, "onWatchDisconnected: ");
    }

    @Override
    protected WatchGesture[] getRequiredWatchGestures(){
        return REQUIRED_WATCH_GESTURES;
    }

    private void tryStartDetection(){
        Log.i(TAG, "tryStartDetection:  ");

        if (!MGWatch.isWatchConnected(this)) {
            Log.d(TAG, "tryStartDetection: running connect()");
            MGWatch.connect(this);
            return;
        }

        if (!MGWatch.isGesturesTrained(this)) {
            Log.d(TAG, "tryStartDetection: running trainRequiredGestures()");
            MGWatch.trainRequiredGestures(this);
            return;
        }

        if (!MGWatch.isWatchGestureDetecting(this)) {
            Log.d(TAG, "tryStartDetection: running startGestureDetection()");
            MGWatch.startGestureDetection(this);
        }
    }

}