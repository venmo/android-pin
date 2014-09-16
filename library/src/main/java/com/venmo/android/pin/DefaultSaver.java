package com.venmo.android.pin;

import android.content.Context;
import android.widget.Toast;

import com.venmo.android.pin.util.PinHelper;

public class DefaultSaver implements PinSaver {
    private Context context;

    public DefaultSaver(Context context) {
        this.context = context;
    }

    @Override
    public void save(String pin) {
        Toast.makeText(context, context.getString(R.string.pin_created),
                Toast.LENGTH_SHORT).show();
        PinHelper.saveDefaultPin(context, pin);

    }
}
