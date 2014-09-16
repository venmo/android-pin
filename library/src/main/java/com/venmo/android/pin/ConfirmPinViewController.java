package com.venmo.android.pin;

import android.view.View;
import android.widget.Toast;

import com.venmo.android.pin.util.PinHelper;
import com.venmo.android.pin.view.PinputView;


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
    PinputView.Listener provideListener() {
        return new PinputView.Listener() {
            @Override
            public void onPinCommit(PinputView view, String submission) {
                if (submission.equals(mTruthString)) {
                    mPinFragment.notifyCreated(submission);
                    Toast.makeText(mContext, mContext.getString(R.string.pin_created),
                            Toast.LENGTH_SHORT).show();
                    PinHelper.saveDefaultPin(mContext, submission);
                    mPinputView.getText().clear();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.pin_mismatch),
                            Toast.LENGTH_SHORT).show();
                    resetToCreate();
                    view.showErrorAndClear();
                }
            }
        };
    }

    private void resetToCreate() {
        mPinFragment.setDisplayType(PinFragment.PinDisplayType.CREATE);
        mPinFragment.setViewController(new CreatePinViewController(mPinFragment, mRootView));
    }

}
