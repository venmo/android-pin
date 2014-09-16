package com.venmo.android.pin;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.venmo.android.pin.view.PinputView;

import java.util.concurrent.ExecutorService;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

class VerifyPinViewController extends BaseViewController {
    private static final String KEY_INCORRECT_PIN_ATTEMPTS = "com.venmo.pin.incorrect_pin_attempts";
    private ExecutorService mExecutor;

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
    PinputView.Listener provideListener() {
        return new PinputView.Listener() {
            @Override
            public void onPinCommit(PinputView view, final String submission) {
                final Validator validator = getConfig().validator();
                if (validator instanceof AsyncValidator) {
                    getOutAndInAnim(mPinputView, mProgressBar).start();
                    getExecutor().submit(new Runnable() {
                        @Override
                        public void run() {
                            final boolean valid = validator.isValid(submission);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    handleAsyncValidation(valid);
                                }
                            });
                        }
                    });
                } else {
                    handleValidation(validator.isValid(submission));
                }
            }
        };
    }

    private ExecutorService getExecutor() {
        return mExecutor == null ? newSingleThreadExecutor() : mExecutor;
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
            mProgressBar.setVisibility(View.INVISIBLE);
            float centerPosition = (mRootView.getWidth() / 2) - (mPinputView.getWidth() / 2);
            mPinputView.setX(centerPosition);
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
