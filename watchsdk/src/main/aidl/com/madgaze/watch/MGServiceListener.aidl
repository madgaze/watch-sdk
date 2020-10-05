// MGServiceListener.aidl
package com.madgaze.watch;

// Declare any non-default types here with import statements

interface MGServiceListener {
    void actionPerformed(int key, int times);
    void onServiceFailure(int errorCode);
}