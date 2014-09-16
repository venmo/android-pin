package com.venmo.android.pin;

import android.view.View;
import android.widget.Toast;

import com.venmo.android.pin.view.PinputView;
import com.venmo.android.pin.view.PinputView.OnCommitListener;

class ConfirmPinViewController extends BaseViewController {
    private String mTruthString;

    ConfirmPinViewController(PinFragment f, View v, String truth) {
        super(f, v);
        mTruthString = truth;
    }

    @Override
    void initUI() {
        String confirm = String.format(mContext.getString(R.string.confirm_n_digit_pin),
                mPinputView.getPinLen());
        mHeaderText.setText(confirm);
    }

    @Override
    OnCommitListener provideListener() {
        return new OnCommitListener() {
            @Override
            public void onPinCommit(PinputView view, String submission) {
                if (submission.equals(mTruthString)) {
                    handleSave(submission);
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.pin_mismatch),
                            Toast.LENGTH_SHORT).show();
                    resetToCreate();
                    view.showErrorAndClear();
                }
            }
        };
    }

    private void handleSave(final String submission) {
        PinSaver saver = getConfig().getPinSaver();
        if (saver instanceof AsyncSaver) {
            getOutAndInAnim(mPinputView, mProgressBar).start();
            mHeaderText.setText(R.string.saving_pin);
            runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        getConfig().getPinSaver().save(submission);
                        postToMain(new Runnable() {
                            @Override
                            public void run() {
                                onSaveComplete();
                            }
                        });
                    } catch (Exception e) {
                        generalErrorAsync(mPinFragment.getString(R.string.async_save_error));
                    }
                }
            });
        } else {
            saver.save(submission);
            onSaveComplete();
        }
    }

    private void onSaveComplete() {
        mPinputView.getText().clear();
        mPinFragment.notifyCreated();
    }

    private void resetToCreate() {
        mPinFragment.setDisplayType(PinFragment.PinDisplayType.CREATE);
        mPinFragment.setViewController(new CreatePinViewController(mPinFragment, mRootView));
    }

}
