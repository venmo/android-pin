package com.venmo.android.pin;

import android.content.Context;

/**
 * Created by rmercille on 6/13/16.
 */
public interface PinFragmentImplement {
    Context getContext();

    String getString(int resId);

    void setConfig(PinFragmentConfiguration config);

    PinFragmentConfiguration getConfig();

    void onPinCreationEntered(String pinEntry);

    void setViewController(BaseViewController controller);

    void setDisplayType(PinDisplayType type);

    void notifyValid();

    void notifyCreated();
}
