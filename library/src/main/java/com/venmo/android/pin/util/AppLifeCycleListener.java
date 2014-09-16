package com.venmo.android.pin.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

/**
 * Adapter interface to notify an {@code AppLifeCycleListener} client when an {@code Application}
 * is either foregrounded or backgrounded by providing hooks into those circumstances. This is
 * useful if you are trying to show the {@code PinFragment} upon foregrounding of your app. All
 * Activity LifeCycle hooks are no-op. If those hooks are needed in conjunction with the
 * foreground/background hooks, consider subclassing or using a decorator pattern.
 * <p>
 * Works in accordance to how Activities interact. This assumption should not change in future
 * iterations of the Android framework, but documented here just in case [<a
 * href="http://developer.android.com/guide/components/activities.html#CoordinatingActivities">link</a>].
 * For two activities A and B, we expect the following lifecycle methods in order when transitions
 * to B:
 * <ul>
 * <li>{@code A.onPause();}</li>
 * <li>{@code B.onCreate();}</li>
 * <li>{@code B.onStart();}</li>
 * <li>{@code B.onResume();}</li>
 * <li>{@code A.onStop();}</li>
 * </ul>
 * <p>
 * Thus after this series of executions, that is after {@code A.onStop();}, the number of started
 * Activities from our app is equal to the number of stopped activities iff we're moving to another
 * process's Activity (B).
 * <p>
 * Note that if the system can no longer accommodate an Activity's memory usage, the system will
 * destroy the entire process and not just an arbitrary Activity. In this situation, our counts
 * will be reset and work as normal.
 * <p>
 * Only for use on API 14+
 */
@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
public abstract class AppLifeCycleListener implements ActivityLifecycleCallbacks {
    private boolean hasStarted = false;
    private int startedCount = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        startedCount++;
        if (!hasStarted) {
            hasStarted = true;
            onAppForegrounded(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        startedCount--;
        if (startedCount == 0) {
            hasStarted = false;
            onAppBackgrounded(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    protected abstract void onAppForegrounded(Activity openedActivity);
    protected abstract void onAppBackgrounded(Activity closedActivity);
}
