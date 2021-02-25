package com.madgaze.watchsdk;

import android.app.Activity;

import com.madgaze.watchsdk.MobileActivity;
import com.madgaze.watchsdk.WatchActivity;

public class MGWatch {
    public static boolean isMGWatchServiceReady(MobileActivity ctx){
        return ctx.isServiceReady;
    }
    public static boolean isServiceReady(WatchActivity ctx){
        return ctx.isServiceReady;
    }
    public static boolean isWatchGestureDetecting(MobileActivity ctx){
        return ctx.isWatchGestureDetecting();
    }
    public static void stopGestureDetection(MobileActivity ctx){
        ctx.stopGestureDetection();
    }
    public static void startGestureDetection(MobileActivity ctx){
        ctx.startGestureDetection();
    }
    public static void connect(MobileActivity ctx){
        ctx.goToConnectPage();
    }
    public static boolean isWatchConnected(MobileActivity ctx){
        return ctx.isWatchConnected();
    }
    public static boolean isGesturesTrained(MobileActivity ctx){
        return ctx.isGesturesTrained();
    }
    public static boolean isGesturesTrained(WatchActivity ctx){
        return ctx.isGesturesTrained();
    }
    public static void trainRequiredGestures(MobileActivity ctx) {
        ctx.goToTrainingPage();
    }

    public static boolean isWatchGestureDetecting(WatchActivity ctx){
        return ctx.isWatchGestureDetecting();
    }
    public static void startWatchGestureDetection(WatchActivity ctx) {
        ctx.startWatchGestureDetection();
    }
    public static void stopWatchGestureDetection(WatchActivity ctx) {
        ctx.stopWatchGestureDetection();
    }
}