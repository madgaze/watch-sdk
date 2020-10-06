// MGServiceListener.aidl
package com.madgaze.watch.connector;

// Declare any non-default types here with import statements

interface MGConnectorServiceListener {
    void actionPerformed(int key, int times);
    void onServiceFailure(int errorCode);
}