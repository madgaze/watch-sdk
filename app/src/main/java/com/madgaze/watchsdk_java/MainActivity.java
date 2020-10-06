package com.madgaze.watchsdk_java;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.madgaze.watchsdk.MobileActivity;
import com.madgaze.watchsdk.WatchActivity;
import com.madgaze.watchsdk.WatchException;
import com.madgaze.watchsdk.WatchGesture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends MobileActivity {
    private String TAG = MainActivity.class.getSimpleName();

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
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart before isWatchGestureDetecting: "+isWatchGestureDetecting());
        if (!isWatchGestureDetecting()) {
            startWatchGestureDetection();
        }
        Log.d(TAG, "onStart after isWatchGestureDetecting: "+isWatchGestureDetecting());
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWatchGestureReceived(WatchGesture gesture, int times) {
        Log.d(TAG, "onWatchGestureReceived: "+gesture.name()+" x"+times);
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
}