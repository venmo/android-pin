package com.venmo.android.pin.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.venmo.android.pin.R;
import com.venmo.android.pin.util.VibrationHelper;


public class PinputView extends TextView {
    private static final String TAG = PinputView.class.getSimpleName();
    private static final String KEY_SAVED_INSTANCE_STATE = "com.venmo.pin.pinputview.state";
    private static final String KEY_SAVED_STATE_PIN = "com.venmo.pin.pinputview.savedPin";
    public static final int VIBRATE_LENGTH_DEFAULT = 300;

    private int mCharPadding;
    private Pair<Drawable, Drawable>[] mDrawables;
    private int mPinLen;
    private OnCommitListener mListener;
    private final int mAnimDuration = 150;
    private Animator mErrorAnimator;
    private boolean mVibrateOnError = true;
    private int mErrorVibrationLen = VIBRATE_LENGTH_DEFAULT;

    public interface OnCommitListener {
        public void onPinCommit(PinputView view, String submission);
    }

    public PinputView(Context context) {
        super(context);
        init(null, 0);
    }

    public PinputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PinputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // @formatter:off
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PinputView, defStyle, 0);
        // @formatter:on

        setFocusableInTouchMode(false);
        setKeyListener(DigitsKeyListener.getInstance(false, false));

        mPinLen = a.getInt(R.styleable.PinputView_pinputview_len, 4);
        mCharPadding = (int) a.getDimension(R.styleable.PinputView_pinputview_characterPadding,
                getResources().getDimension(R.dimen.pinputview_default_char_padding));
        int foregroundColor = a.getColor(R.styleable.PinputView_pinputview_foregroundColor, Color.BLUE);
        int backgroundColor = a.getColor(R.styleable.PinputView_pinputview_backgroundColor, Color.GRAY);

        a.recycle();

        initDrawables(foregroundColor, backgroundColor);
        initFilters();
        initializeAnimator();
    }

    private void initializeAnimator() {
        post(new Runnable() {
            @Override
            public void run() {
                float x = getX();
                mErrorAnimator =
                        ObjectAnimator.ofFloat(PinputView.this, "x", x, x + getWidth() / 20);
                mErrorAnimator.setInterpolator(new CycleInterpolator(3));
                mErrorAnimator.setDuration(mErrorVibrationLen);
                mErrorAnimator.addListener(new AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        vibrateOnError();
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        getText().clear();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
        });
    }

    private void initDrawables(int foregroundColor, int backgroundColor) {
        int size = (int) getTextSize();

        int left = getPaddingLeft();
        int top = getPaddingTop() + mCharPadding;
        int right = left + size;
        int bottom = top + size;
        mDrawables = new Pair[mPinLen];

        for (int i = 0; i < mPinLen; i++) {
            int charOff = i * size + ((i + 1) * mCharPadding);
            Rect bounds = new Rect(left + charOff, top, right + charOff, bottom);

            mDrawables[i] = Pair.create(
                    makeCharShape(size, foregroundColor, bounds),
                    makeCharShape(size, backgroundColor, bounds));
        }
    }

    private void initFilters() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before == 0 && start < mPinLen) {
                    animateDrawableIn(mDrawables[start].first, mDrawables[start].second);
                }
            }

            @Override
            public void afterTextChanged(final Editable text) {
                if (text.length() == mPinLen && mListener != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onPinCommit(PinputView.this, text.toString());
                        }
                    }, mAnimDuration);
                }
            }
        });
        InputFilter lenFilter = new LengthFilter(mPinLen);
        setFilters(new InputFilter[]{lenFilter});
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float height = getTextSize() + getPaddingTop() + getPaddingBottom();
        int hPadding = mCharPadding * 2;
        float width = mPinLen * getTextSize() + getPaddingLeft() + getPaddingRight();
        int wPadding = mCharPadding * (mPinLen + 1);
        setMeasuredDimension((int) width + wPadding, (int) height + hPadding);
    }

    public void showErrorAndClear() {
        mErrorAnimator.start();
    }

    public void setVibrateOnError(boolean vibrate) {
        setVibrateOnError(vibrate, PinputView.VIBRATE_LENGTH_DEFAULT);
    }

    public void setVibrateOnError(boolean vibrate, int millis) {
        mVibrateOnError = vibrate;
        mErrorVibrationLen = millis;
    }

    public void setListener(OnCommitListener listener) {
        mListener = listener;
    }

    public void setErrorAnimator(Animator errorAnimator) {
        mErrorAnimator = errorAnimator;
        mErrorAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                vibrateOnError();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                getText().clear();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    /**
     * @return the pre-defined length of this PIN
     * TODO: allow arbitrary length PIN and return -1
     */
    public int getPinLen() {
        return mPinLen;
    }

    //TODO: maybe change this to setPin(Drawable) and use clone to make as many as we need
    protected Drawable makeCharShape(float size, int color, Rect bounds) {
        Shape shape = new OvalShape();
        shape.resize(size, size);
        ShapeDrawable drawable = new ShapeDrawable(shape);
        drawable.getPaint().setColor(color);
        drawable.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
        drawable.setBounds(bounds);
        return drawable;
    }

    protected void animateDrawableIn(Drawable foreground, Drawable background) {
        final ShapeDrawable front = (ShapeDrawable) foreground;
        final ShapeDrawable back = (ShapeDrawable) background;
        float to = ((ShapeDrawable) background).getShape().getWidth();
        ValueAnimator animator = ValueAnimator.ofFloat(0, to);
        animator.setDuration(mAnimDuration);
        animator.setInterpolator(new OvershootInterpolator(3f));
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newSize =
                        animation.getAnimatedFraction() * back.getShape().getWidth();
                int alpha = Math.min((int) (255 * animation.getAnimatedFraction()), 255);
                int offset = (int) (back.getShape().getWidth() - newSize) / 2;
                int left = back.getBounds().left + offset;
                int top = back.getBounds().top + offset;
                int right = (int) (left + newSize);
                int bottom = (int) (top + newSize);

                front.getShape().resize(newSize, newSize);
                front.setBounds(left, top, right, bottom);
                front.setAlpha(alpha);
                invalidate(
                        back.getBounds().left - mCharPadding,
                        back.getBounds().top - mCharPadding,
                        right + mCharPadding,
                        bottom + mCharPadding);
            }
        });

        animator.start();
    }

    private void vibrateOnError() {
        if (mVibrateOnError) {
            VibrationHelper.vibrate(getContext(), mErrorVibrationLen);
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            setText(bundle.getCharSequence(KEY_SAVED_STATE_PIN));
            state = bundle.getParcelable(KEY_SAVED_INSTANCE_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SAVED_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putCharSequence(KEY_SAVED_STATE_PIN, getText().toString());
        return bundle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mPinLen; i++) {
            mDrawables[i].second.draw(canvas);
            if (i < getText().length()) {
                mDrawables[i].first.draw(canvas);
            }
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }
}
