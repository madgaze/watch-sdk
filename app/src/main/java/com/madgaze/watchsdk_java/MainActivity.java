package com.madgaze.watchsdk_java;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.madgaze.watchsdk.MGWatch;
import com.madgaze.watchsdk.MobileActivity;
import com.madgaze.watchsdk.WatchException;
import com.madgaze.watchsdk.WatchGesture;

import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends MobileActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    public final WatchGesture[] REQUIRED_WATCH_GESTURES = {
            WatchGesture.FINGER_SNAP,
            WatchGesture.THUMBTAP_INDEX_MIDDLE,
            WatchGesture.FOREARM_LEFT,
            WatchGesture.FOREARM_LEFT_2,
            WatchGesture.FOREARM_RIGHT,
            WatchGesture.HANDBACK_UP,
            WatchGesture.THUMBTAP_INDEX,
            WatchGesture.MOVE_FOREARM_DOWN,
            WatchGesture.THUMBTAP_MIDDLE,
            WatchGesture.JOINTTAP_MIDDLE_MIDDLE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDefinedGestures();
        setListeners();
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
        Log.d(TAG, "onWatchDetectionOn: ");
        setStatusText("Listening");
    }

    @Override
    public void onWatchDetectionOff() {
        Log.d(TAG, "onWatchDetectionOff: ");
        setStatusText("Idle");
    }

    @Override
    public void onMGWatchServiceReady() {
        setStatusText("Service Connected");
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
        setStatusText("Watch Connected");
    }

    @Override
    public void onWatchDisconnected() {
        setStatusText("Watch Disconnected");
        showConnectDialog();
    }

    @Override
    protected WatchGesture[] getRequiredWatchGestures(){
        return REQUIRED_WATCH_GESTURES;
    }

    private void tryStartDetection(){

        if (!MGWatch.isWatchConnected(this)) {
            setStatusText("Connecting");
            showConnectDialog();
            return;
        }

        if (!MGWatch.isGesturesTrained(this)) {
            showTrainingDialog();
            return;
        }

        if (!MGWatch.isWatchGestureDetecting(this)) {
            MGWatch.startGestureDetection(this);
        }
    }


    private void setStatusText(String text){
        setText(R.id.status, "Status: " + text);
    }

    private void setResultText(final WatchGesture gesture){
        setText(R.id.result, gesture.toString());
    }

    public void setDefinedGestures(){
        setText(R.id.definedGestures, TextUtils.join(", ", getRequiredWatchGestures()));
    }

    public void showConnectDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Connect Required")
                .setMessage("Watch is not connected. Connect to MAD Gaze Watch now.")
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MGWatch.connect(MainActivity.this);
                    }
                })
                .setCancelable(false);
        dialog.show();
    }

    public void showTrainingDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Training Required")
                .setMessage("The required gestures for this application have not been trained. Do you want to train now?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MGWatch.trainRequiredGestures(MainActivity.this);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setStatusText("Training Required");
                        ((Button)findViewById(R.id.trainButton)).setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void setListeners(){
        ((Button)findViewById(R.id.trainButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                MGWatch.trainRequiredGestures(MainActivity.this);
            }
        });
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