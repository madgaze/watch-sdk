package com.madgaze.watchsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.madgaze.watch.MGServiceInterface;
import com.madgaze.watch.MGServiceListener;

import java.util.Arrays;

public abstract class WatchActivity extends AppCompatActivity {
    String TAG = WatchActivity.class.getSimpleName();
    String remotePackageName = "com.madgaze.watch";
    static String CONTROLLER_APP_NOT_UPDATED = "MAD Gaze Controller app not updated";
    static String INCOMPLETE_CALIBRATION = "Incomplete Calibration";
    static String CONTROLLER_ERROR = "MAD Gaze Controller app error";
    static String SOME_GESTURES_NOT_TRAINED = "Some gestures have not trained";
    MGServiceInterface mMGServiceInterface;
    boolean isDetectionOn = false;
    Context activityContext = this;

    public abstract void onWatchGestureReceived(WatchGesture gesture);
    public abstract void onWatchGestureError(WatchException error);
    public abstract void onWatchDetectionOn();
    public abstract void onWatchDetectionOff();
    public boolean isWatchGestureDetecting() {
        return isDetectionOn;
    }
    protected abstract WatchGesture[] getRequiredWatchGestures();

    public void startWatchGestureDetection() {
        if (!isDetectionOn) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(remotePackageName+".test", remotePackageName+".service.SignalDetectService"));
            boolean result = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            if (!result) {
                onWatchGestureError(new WatchException(CONTROLLER_APP_NOT_UPDATED));
            }
        }
    }

    public void stopWatchGestureDetection() {
        if (isDetectionOn) {
            if (mMGServiceInterface != null) {
                try {
                    mMGServiceInterface.unregistListener(new MyServiceListener(activityContext));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(mConnection);
                isDetectionOn = false;
                onWatchDetectionOff();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopWatchGestureDetection();
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
                    boolean isTrained = mMGServiceInterface.isTrained();
                    if (!isTrained) {
                        onWatchGestureError(new WatchException(INCOMPLETE_CALIBRATION));
                    }
                    if (!isGesturesTrained()) {
                        Log.d(TAG, "!isGesturesTrained");
                        onWatchGestureError(new WatchException(SOME_GESTURES_NOT_TRAINED));
                    }
                    mMGServiceInterface.registListener(new MyServiceListener(activityContext));
                    isDetectionOn = true;
                    onWatchDetectionOn();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMGServiceInterface != null) {
                try {
                    mMGServiceInterface.unregistListener(new MyServiceListener(activityContext));
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

            if (gestureId != -1 && Arrays.asList(getRequiredWatchGestures()).contains(wg)) {
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
