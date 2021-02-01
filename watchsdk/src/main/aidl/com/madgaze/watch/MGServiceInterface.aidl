// MGServiceInterface.aidl
package com.madgaze.watch;

import com.madgaze.watch.MGServiceListener;

// Declare any non-default types here with import statements

interface MGServiceInterface {
    int isTaskRunning();
    boolean isTrained();
    byte[] registerGestures(in int[] gestureIds);
    void registListener(MGServiceListener listener);
    void unregistListener(MGServiceListener listener);
}
