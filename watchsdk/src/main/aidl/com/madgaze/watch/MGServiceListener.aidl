// MGServiceListener.aidl
package com.madgaze.watch;

// Declare any non-default types here with import statements

interface MGServiceListener {
    void actionPerformed(int gestureId);
    void onServiceFailure(int errorCode);
}