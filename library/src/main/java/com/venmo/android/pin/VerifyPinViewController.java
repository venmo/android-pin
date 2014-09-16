package com.venmo.android.pin;

import android.view.View;

import com.venmo.android.pin.view.PinputView;
import com.venmo.android.pin.view.PinputView.OnCommitListener;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

class VerifyPinViewController extends BaseViewController {
    private static final String KEY_INCORRECT_PIN_ATTEMPTS = "com.venmo.pin.incorrect_pin_attempts";

    VerifyPinViewController(PinFragment f, View v) {
        super(f, v);
    }

    @Override
    void initUI() {
        String verify = String.format(
                mContext.getString(R.string.verify_n_digit_pin), mPinputView.getPinLen());
        mHeaderText.setText(verify);
    }

    @Override
    OnCommitListener provideListener() {
        return new OnCommitListener() {
            @Override
            public void onPinCommit(PinputView view, final String submission) {
                validate(submission);
            }
        };
    }

    protected void validate(final String submission) {
        final Validator validator = getConfig().getValidator();
        if (validator instanceof AsyncValidator) {
            getOutAndInAnim(mPinputView, mProgressBar).start();
            runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        final boolean valid = validator.isValid(submission);
                        postToMain(new Runnable() {
                            @Override
                            public void run() {
                                handleAsyncValidation(valid);
                            }
                        });
                    } catch (Exception e) {
                        generalErrorAsync(mPinFragment.getString(R.string.async_save_error));
                    }
                }
            });
        } else {
            handleValidation(validator.isValid(submission));
        }
    }

    private void handleValidation(boolean isValid) {
        if (isValid) {
            resetIncorrectPinCount();
            mPinFragment.notifyValid();
        } else {
            incrementFailedAttempts();
            mPinputView.showErrorAndClear();
        }
    }

    private void handleAsyncValidation(boolean valid) {
        handleValidation(valid);
        if (!valid) {
            resetPinputView();
        }
    }

    private void resetIncorrectPinCount() {
        getDefaultSharedPreferences(mContext).edit()
                .putInt(KEY_INCORRECT_PIN_ATTEMPTS, 0)
                .commit();
    }

    private void incrementFailedAttempts() {
        int failedAttempts = getIncorrectPinAttempts() + 1;
        int maxTries = getConfig().maxTries();
        boolean attemptsDepleted = maxTries > 0 && failedAttempts >= maxTries;
        getDefaultSharedPreferences(mContext).edit()
                .putInt(KEY_INCORRECT_PIN_ATTEMPTS, attemptsDepleted ? 0 : failedAttempts)
                .commit();
        TryDepletionListener depletionListener = getConfig().tryDepletionListener();
        if (attemptsDepleted && depletionListener != null) {
            depletionListener.onTriesDepleted();
        }
    }

    private int getIncorrectPinAttempts() {
        return getDefaultSharedPreferences(mContext).getInt(KEY_INCORRECT_PIN_ATTEMPTS, 0);
    }

}
