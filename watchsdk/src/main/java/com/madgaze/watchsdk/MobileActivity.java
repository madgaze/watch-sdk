package com.madgaze.watchsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.madgaze.watch.connector.MGConnectorServiceInterface;
import com.madgaze.watch.connector.MGConnectorServiceListener;

import java.util.Arrays;
import java.util.Map;

public abstract class MobileActivity extends AppCompatActivity {
    String TAG = MobileActivity.class.getSimpleName();
    String remotePackageName = "com.madgaze.watch.connector";
    static String CONTROLLER_APP_NOT_UPDATED = "MG Watch app not updated";
    static String NOT_CONNECTED_TO_WATCH = "Not connected to watch";
    static String CONTROLLER_ERROR = "MG Watch app error";
    MGConnectorServiceInterface mMGConnectorServiceInterface;
    private boolean isDetectionOn = false;
    Context activityContext = this;

    public abstract void onWatchGestureReceived(WatchGesture gesture, int times);
    public abstract void onWatchGestureError(WatchException error);
    public abstract void onWatchDetectionOn();
    public abstract void onWatchDetectionOff();
    public abstract void onWatchServiceConnected();
    public abstract void onWatchServiceDisconnected();
    public abstract void onWatchConnected();
    public abstract void onWatchDisconnected();

    public Map<String, int[]> registerGestures(int[] gestures) {
        Map<String, int[]> signalMap = null;
        try {
            signalMap = mMGConnectorServiceInterface.registerGestures(gestures);
            Log.d(TAG, "onServiceConnected: signalMap: "+ Arrays.toString(signalMap.get("needTrain")));

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return signalMap;
    }

    public boolean isWatchConnected() {
        try {
            return mMGConnectorServiceInterface.isConnected();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isWatchGestureDetecting() {
        try {
            if (mMGConnectorServiceInterface == null || !mMGConnectorServiceInterface.isConnected()) {
                isDetectionOn = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isDetectionOn;
    }

    public void startWatchGestureDetection() {
        try {
            Log.d(TAG, "startWatchGestureDetection: "+mMGConnectorServiceInterface.isConnected());
            if (mMGConnectorServiceInterface != null && mMGConnectorServiceInterface.isConnected()) {
                if (!isDetectionOn) {
                    isDetectionOn = true;
                    onWatchDetectionOn();
                }
            } else {
                isDetectionOn = false;
                onWatchDetectionOff();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopWatchGestureDetection() {
        if (isDetectionOn) {
            isDetectionOn = false;
            onWatchDetectionOff();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(remotePackageName, remotePackageName+".service.SDKService"));
        boolean result = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (!result) {
            onWatchGestureError(new WatchException(CONTROLLER_APP_NOT_UPDATED));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWatchGestureDetection();
        if (mMGConnectorServiceInterface != null) {
            try {
                mMGConnectorServiceInterface.unregistListener(new MyConnectorServiceListener(activityContext));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(mConnection);
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMGConnectorServiceInterface = MGConnectorServiceInterface.Stub.asInterface(service);
            Log.d(TAG, "onServiceConnected: ");
            try {
                if (mMGConnectorServiceInterface != null) {
                    mMGConnectorServiceInterface.registListener(new MyConnectorServiceListener(activityContext));
//                    boolean isConnected = mMGConnectorServiceInterface.isConnected();
//                    Log.d(TAG, "onServiceConnected: isConnected: "+isConnected);
//                    if (!isConnected) {
//                        onWatchGestureError(new WatchException(NOT_CONNECTED_TO_WATCH));
//                    }
                    onWatchServiceConnected();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMGConnectorServiceInterface != null) {
                try {
                    mMGConnectorServiceInterface.unregistListener(new MyConnectorServiceListener(activityContext));
                    onWatchServiceDisconnected();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mMGConnectorServiceInterface = null;
            }
        }
    };

    class MyConnectorServiceListener extends MGConnectorServiceListener.Stub {
        Context mContext;

        public MyConnectorServiceListener(Context context) {
            this.mContext = context;
        }

        @Override
        public void actionPerformed(int key, int times) {
            if (key != 1 || times != -1) {
                WatchGesture gesture = findNameBySignal(key);
                if (gesture != null) {
                    ((MobileActivity)mContext).onWatchGestureReceived(gesture, times);
                }
            }
        }

        @Override
        public void onServiceFailure(int errorCode) {
            ((MobileActivity)mContext).onWatchGestureError(new WatchException(CONTROLLER_ERROR));
        }

        @Override
        public void onWatchConnected() {
            ((MobileActivity)mContext).onWatchConnected();
        }

        @Override
        public void onWatchDisconnected() {
            ((MobileActivity)mContext).onWatchDisconnected();
        }

        WatchGesture findNameBySignal(int signal) {
            for (WatchGesture g: WatchGesture.values()) {
                if (g.getSignal() == signal) {
                    return g;
                }
            }
            return null;
        }
    }
}
