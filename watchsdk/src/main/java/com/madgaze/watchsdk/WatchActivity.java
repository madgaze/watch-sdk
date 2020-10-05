package com.madgaze.watchsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;

import com.madgaze.watch.MGServiceInterface;
import com.madgaze.watch.MGServiceListener;

public abstract class WatchActivity extends AppCompatActivity {
    String TAG = WatchActivity.class.getSimpleName();
    String remotePackageName = "com.madgaze.watch";
    static String CONTROLLER_APP_NOT_UPDATED = "MAD Gaze Controller app not updated";
    static String INCOMPLETE_CALIBRATION = "Incomplete Calibration";
    static String CONTROLLER_ERROR = "MAD Gaze Controller app error";
    MGServiceInterface mMGServiceInterface;
    boolean isDetectionOn = false;
    Context activityContext = this;

    public abstract void onWatchGestureReceived(WatchGesture gesture, int times);
    public abstract void onWatchGestureError(WatchException error);
    public abstract void onWatchDetectionOn();
    public abstract void onWatchDetectionOff();
    public boolean isWatchGestureDetecting() {
        return isDetectionOn;
    }

    public void startWatchGestureDetection() {
        if (!isDetectionOn) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(remotePackageName, remotePackageName+".service.SignalDetectService"));
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
        public void actionPerformed(int key, int times) {
            if (key != 1 || times != -1) {
                WatchGesture gesture = findNameBySignal(key);
                if (gesture != null) {
                    ((WatchActivity)mContext).onWatchGestureReceived(gesture, times);
                }
            }
        }

        @Override
        public void onServiceFailure(int errorCode) {
            ((WatchActivity)mContext).onWatchGestureError(new WatchException(CONTROLLER_ERROR));
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
