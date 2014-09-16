package com.venmo.android.pin;

import android.content.Context;

import com.venmo.android.pin.util.PinHelper;

/**
 * Default {@link Validator} implementation that should use a {@code SystemPreference} lookup to
 * confirm or reject a string input
 */
class DefaultValidator implements Validator {
    private Context context;

    public DefaultValidator(Context c) {
        context = c;
    }

    @Override
    public boolean isValid(String input) {
        return PinHelper.doesMatchDefaultPin(context, input);
    }
}