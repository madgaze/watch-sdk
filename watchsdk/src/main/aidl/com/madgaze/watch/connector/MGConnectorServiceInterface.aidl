// MGServiceInterface.aidl
package com.madgaze.watch.connector;

import com.madgaze.watch.connector.MGConnectorServiceListener;

// Declare any non-default types here with import statements

interface MGConnectorServiceInterface {
    boolean isConnected();
    void registListener(MGConnectorServiceListener listener);
    void unregistListener(MGConnectorServiceListener listener);
}
