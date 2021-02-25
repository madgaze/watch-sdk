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

import com.madgaze.watch.MGServiceInterface;
import com.madgaze.watch.MGServiceListener;

import java.util.Arrays;

public abstract class WatchActivity extends AppCompatActivity {
    String TAG = WatchActivity.class.getSimpleName();
    String remotePackageName = "com.madgaze.watch";
    static String CONTROLLER_APP_NOT_UPDATED = "CONTROLLER_APP_NOT_UPDATED";
    static String CONTROLLER_ERROR = "CONTROLLER_ERROR";
    static String UNABLE_TO_START_INCOMPLETED_TRAINING = "UNABLE_TO_START_INCOMPLETED_TRAINING";
    static String SERVICE_DISCONNECTED = "SERVICE_DISCONNECTED";
    MGServiceInterface mMGServiceInterface;
    boolean isDetectionOn = false;
    public boolean isServiceReady = false;
    Context activityContext = this;
    MyServiceListener mMyServiceListener;

    public abstract void onWatchGestureReceived(WatchGesture gesture);
    public abstract void onWatchGestureError(WatchException error);
    public abstract void onWatchDetectionOn();
    public abstract void onWatchDetectionOff();
    public abstract void onServiceReady();
    public boolean isWatchGestureDetecting() {
        return isDetectionOn;
    }
    protected abstract WatchGesture[] getRequiredWatchGestures();

    public void startWatchGestureDetection() {
        if (mMGServiceInterface != null && isGesturesTrained()) {
            isDetectionOn = true;
            onWatchDetectionOn();
        } else {
            isDetectionOn = false;
            onWatchGestureError(new WatchException(UNABLE_TO_START_INCOMPLETED_TRAINING));
        }
    }

    public void stopWatchGestureDetection() {
        isDetectionOn = false;
        onWatchDetectionOff();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(remotePackageName /* + ".test" */, remotePackageName + ".service.SignalDetectService"));
        boolean result = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (!result) {
            onWatchGestureError(new WatchException(CONTROLLER_APP_NOT_UPDATED));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mMGServiceInterface != null) {
            try {
                mMGServiceInterface.registListener(mMyServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mMGServiceInterface != null) {
            try {
                mMGServiceInterface.unregistListener(mMyServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public boolean isGesturesTrained() {
        try {
            WatchGesture[] objectArray = getRequiredWatchGestures();
            int[] gesturesIds = new int[objectArray.length];
            for (int i = 0; i < objectArray.length; i++)
                gesturesIds[i] = objectArray[i].getGestureId();
            byte[] needTrainSignals = mMGServiceInterface.registerGestures(gesturesIds);
            Log.d(TAG, "isGesturesTrained: "+ Arrays.toString(needTrainSignals));
            return needTrainSignals.length == 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMGServiceInterface = MGServiceInterface.Stub.asInterface(service);
            try {
                if (mMGServiceInterface != null) {
                    mMyServiceListener = new MyServiceListener(activityContext);
                    mMGServiceInterface.registListener(mMyServiceListener);
                    isServiceReady = true;
                    onServiceReady();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMGServiceInterface != null) {
                try {
                    isServiceReady = false;
                    onWatchGestureError(new WatchException(SERVICE_DISCONNECTED));
                    mMGServiceInterface.unregistListener(mMyServiceListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mMGServiceInterface = null;
            }
        }
    };

    class MyServiceListener extends MGServiceListener.Stub {
        Context mContext;

        public MyServiceListener(Context context) {
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
                    ((WatchActivity)mContext).onWatchGestureReceived(gesture);
                }
            }
        }

        @Override
        public void onServiceFailure(int errorCode) {
            ((WatchActivity)mContext).onWatchGestureError(new WatchException(CONTROLLER_ERROR));
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
