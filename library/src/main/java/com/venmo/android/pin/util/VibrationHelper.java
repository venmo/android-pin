package com.venmo.android.pin.util;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Vibrator;

public class VibrationHelper {

    public static void vibrate(Context context, int duration) {
        if (hasVibrationPermission(context)) {
            ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE))
                    .vibrate(duration);
        }
    }

    private static boolean hasVibrationPermission(Context context) {
        int result = context.checkCallingOrSelfPermission(permission.VIBRATE);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

}
