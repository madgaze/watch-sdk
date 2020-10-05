package com.madgaze.watchsdk;

public enum WatchGesture {
    ARM_LEFT(1),
    ARM_RIGHT(2),
    HANDBACK_UP(3),
    HANDBACK_DOWN(4),
    HANDBACK_LEFT(5),
    HANDBACK_RIGHT(6),
    FINGER_INDEX_MIDDLE(7),
    FINGER_SNAP(8);

    private int signal;
    WatchGesture(int signal) {
        this.signal = signal;
    }
    protected int getSignal() {
        return signal;
    }
}
