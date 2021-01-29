package com.madgaze.watchsdk;

public enum WatchGesture {
    ARM_LEFT(1),
    ARM_RIGHT(2),
    HANDBACK_UP(3),
    HANDBACK_DOWN(4),
    HANDBACK_LEFT(5),
    HANDBACK_RIGHT(6),
    FINGER_INDEX_MIDDLE(7),
    FINGER_SNAP(8),
    ARM_LEFT_2(9),
    ARM_RIGHT_2(10),
    HANDBACK_UP_2(11),
    HANDBACK_DOWN_2(12),
    HANDBACK_LEFT_2(13),
    HANDBACK_RIGHT_2(14),
    FINGER_INDEX_MIDDLE_2(15),
    FINGER_SNAP_2(16),
    FINGER_THUMB_LEFT(17),
    FINGER_THUMB_RIGHT(18),
    FINGER_INDEX_LEFT(19),
    FINGER_INDEX_RIGHT(20),
    FINGER_MIDDLE_LEFT(21),
    FINGER_MIDDLE_RIGHT(22),
    FINGER_RING_LEFT(23),
    FINGER_RING_RIGHT(24),
    FINGER_LITTLE(25),
    FINGER_INDEX(26),
    FINGER_MIDDLE(27),
    FINGER_INDEX_2(28),
    FINGER_MIDDLE_2(29),
    MOVE_ARM_UP(30),
    MOVE_ARM_DOWN(31),
    MOVE_ARM_LEFT(32),
    MOVE_ARM_RIGHT(33);

    private final int gestureId;
    WatchGesture(int gestureId) {
        this.gestureId = gestureId;
    }
    protected int getGestureId() {
        return gestureId;
    }
}
