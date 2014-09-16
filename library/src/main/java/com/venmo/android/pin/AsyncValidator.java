package com.venmo.android.pin;

/**
 * Marker interface to implement if a PIN validation should be run asynchronously (i.e. by making a
 * network request). {@link PinFragment} will background the logic in your {@code AsyncValidator} if
 * you implement it.
 */
public interface AsyncValidator extends Validator {}
