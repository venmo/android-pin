package com.venmo.android.pin;

/**
 * Marker interface similar to {@link com.venmo.android.pin.AsyncValidator} for asynchronous
 * saving. Any implementations of this class are promised to be run on a background thread.
 */
public interface AsyncSaver extends PinSaver {
}
