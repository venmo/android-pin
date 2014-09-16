package com.venmo.android.pin;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.venmo.android.pin.util.VibrationHelper;
import com.venmo.android.pin.view.PinKeyboardView;
import com.venmo.android.pin.view.PinputView;
import com.venmo.android.pin.view.PinputView.OnCommitListener;

import java.util.concurrent.ExecutorService;

import static android.view.MotionEvent.ACTION_DOWN;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

abstract class BaseViewController {

    private ExecutorService mExecutor;
    protected PinFragment mPinFragment;
    protected Context mContext;
    protected PinputView mPinputView;
    protected PinKeyboardView mKeyboardView;
    protected TextView mHeaderText;
    protected ProgressBar mProgressBar;
    protected View mRootView;

    /*package*/ BaseViewController(PinFragment f, View v) {
        mPinFragment = f;
        mContext = f.getActivity();
        mRootView = v;
        init();
    }

    private void init() {
        mPinputView = (PinputView) mRootView.findViewById(R.id.pin_pinputview);
        mKeyboardView = (PinKeyboardView) mRootView.findViewById(R.id.pin_keyboard);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.pin_progress_spinner);
        mHeaderText = (TextView) mRootView.findViewById(R.id.pin_header_label);
        initKeyboard();
        initUI();
        mPinputView.setListener(provideListener());
    }

    final void refresh(View rootView) {
        mRootView = rootView;
        init();
    }

    abstract void initUI();
    abstract OnCommitListener provideListener();

    private void initKeyboard() {
        mKeyboardView.setOnKeyboardActionListener(new PinKeyboardView.PinPadActionListener() {
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                Editable e = mPinputView.getText();
                if (primaryCode == PinKeyboardView.KEYCODE_DELETE) {
                    int len = e.length();
                    if (len == 0) {
                        return;
                    }
                    e.delete(len - 1, e.length());
                } else {
                    mPinputView.getText().append((char) primaryCode);
                }
            }
        });
        mKeyboardView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == ACTION_DOWN && getConfig().shouldVibrateOnKey()) {
                    VibrationHelper.vibrate(mContext, getConfig().vibrateDuration());
                }
                return false;
            }
        });
    }

    protected PinFragmentConfiguration getConfig() {
        return mPinFragment.getConfig();
    }

    protected Animator getOutAnim(View v) {
        float start = v.getX();
        float end = -v.getWidth();
        ObjectAnimator out = ObjectAnimator.ofFloat(v, "x", start, end);
        out.setDuration(150);
        out.setInterpolator(new AccelerateInterpolator());
        return out;
    }

    protected Animator getInAnim(View v) {
        float start = mRootView.getWidth();
        float end = (start / 2) - (v.getWidth() / 2);
        final ObjectAnimator in = ObjectAnimator.ofFloat(v, "x", start, end);
        in.setDuration(150);
        in.setInterpolator(new DecelerateInterpolator());
        return in;
    }

    protected Animator getOutAndInAnim(final PinputView out, final View in) {
        Animator a = getOutAnim(out);
        a.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                out.getText().clear();
                in.setVisibility(View.VISIBLE);
                getInAnim(in).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        return a;
    }

    void resetPinputView(){
        mProgressBar.setVisibility(View.INVISIBLE);
        float centerPosition = (mRootView.getWidth() / 2) - (mPinputView.getWidth() / 2);
        mPinputView.setX(centerPosition);
        mPinputView.getText().clear();
    }

    protected void runAsync(Runnable runnable) {
        getExecutor().submit(runnable);
    }

    protected void postToMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    void generalErrorAsync(final String s){
        postToMain(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mPinFragment.getActivity(), s, Toast.LENGTH_SHORT).show();
                resetPinputView();
            }
        });
    }

    private ExecutorService getExecutor() {
        if (mExecutor == null) {
            mExecutor = newSingleThreadExecutor();
        }
        return mExecutor;
    }
}
