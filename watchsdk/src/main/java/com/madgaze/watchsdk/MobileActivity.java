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
    static String MGWATCH_SERVICE_DISCONNECTED = "MG Watch service disconnected";
    MGConnectorServiceInterface mMGConnectorServiceInterface;
    private boolean isDetectionOn = false;
    public boolean isServiceReady = false;
    private byte[] needTrainSignals;
    Context activityContext = this;
    MyConnectorServiceListener mMyConnectorServiceListener;

    public abstract void onWatchGestureReceived(WatchGesture gesture);
    public abstract void onWatchGestureError(WatchException error);
    public abstract void onWatchDetectionOn();
    public abstract void onWatchDetectionOff();
    public abstract void onMGWatchServiceReady();
    public abstract void onWatchConnected();
    public abstract void onWatchDisconnected();
    protected abstract WatchGesture[] getRequiredWatchGestures();

    @Override
    public void onResume(){
        super.onResume();
        if (mMGConnectorServiceInterface != null) {
            try {
                mMGConnectorServiceInterface.registListener(mMyConnectorServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (isWatchGestureDetecting())
            stopGestureDetection();

        if (mMGConnectorServiceInterface != null) {
            try {
                mMGConnectorServiceInterface.unregistListener(mMyConnectorServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isGesturesTrained() {
        try {
            WatchGesture[] objectArray = getRequiredWatchGestures();
            int[] gesturesIds = new int[objectArray.length];
            for (int i = 0; i < objectArray.length; i++)
                gesturesIds[i] = objectArray[i].getGestureId();
            needTrainSignals = mMGConnectorServiceInterface.registerGestures(gesturesIds);
            Log.d(TAG, "isGesturesTrained: needTrainSignals: "+Arrays.toString(needTrainSignals));
            return needTrainSignals.length == 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void goToConnectPage() {
        try {
            mMGConnectorServiceInterface.goToConnectPage();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void goToTrainingPage() {
        try {
            mMGConnectorServiceInterface.goToTrainingPage(needTrainSignals);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

    public void startGestureDetection() {
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

    public void stopGestureDetection() {
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
        unbindService(mConnection);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMGConnectorServiceInterface = MGConnectorServiceInterface.Stub.asInterface(service);
            Log.d(TAG, "onServiceConnected: ");
            try {
                if (mMGConnectorServiceInterface != null) {
                    mMyConnectorServiceListener = new MyConnectorServiceListener(activityContext);
                    mMGConnectorServiceInterface.registListener(mMyConnectorServiceListener);
//                    boolean isConnected = mMGConnectorServiceInterface.isConnected();
//                    Log.d(TAG, "onServiceConnected: isConnected: "+isConnected);
//                    if (!isConnected) {
//                        onWatchGestureError(new WatchException(NOT_CONNECTED_TO_WATCH));
//                    }
                    isServiceReady = true;
                    onMGWatchServiceReady();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMGConnectorServiceInterface != null) {
                try {
                    isServiceReady = false;
                    onWatchGestureError(new WatchException(MGWATCH_SERVICE_DISCONNECTED));
                    mMGConnectorServiceInterface.unregistListener(mMyConnectorServiceListener);
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
        public void actionPerformed(int gestureId) {
            WatchGesture wg = null;
            for(WatchGesture e : WatchGesture.values()){
                if(gestureId == e.getGestureId()) {
                    wg = e;
                    break;
                }
            }

            if (isDetectionOn && gestureId != -1 && Arrays.asList(getRequiredWatchGestures()).contains(wg)) {
                WatchGesture gesture = findNameByGestureId(gestureId);
                if (gesture != null) {
                    ((MobileActivity)mContext).onWatchGestureReceived(gesture);
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

        WatchGesture findNameByGestureId(int gestureId) {
            for (WatchGesture g: WatchGesture.values()) {
                if (g.getGestureId() == gestureId) {
                    return g;
                }
            }
            return null;
        }
    }
}
