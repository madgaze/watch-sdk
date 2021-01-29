// MGServiceInterface.aidl
package com.madgaze.watch.connector;

import com.madgaze.watch.connector.MGConnectorServiceListener;

// Declare any non-default types here with import statements

interface MGConnectorServiceInterface {
    boolean isConnected();
    byte[] registerGestures(in int[] gestureIds);
    void goToConnectPage();
    void goToTrainingPage(in byte[] signals);
    void registListener(MGConnectorServiceListener listener);
    void unregistListener(MGConnectorServiceListener listener);
}
