package com.venmo.android.pin.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.venmo.android.pin.R;

import java.util.List;

public class PinKeyboardView extends KeyboardView {

    public static final int KEYCODE_DELETE = -5;
    private Drawable mKeyBackgroundDrawable;
    private boolean mShowUnderline;
    private int mUnderlinePadding;
    private Paint mPaint;
    private Paint mUnderlinePaint;

    @SuppressWarnings("unused")
    public PinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    @SuppressWarnings("unused")
    public PinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // @formatter:off
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PinKeyboardView, defStyle, 0);
        // @formatter:on
        Resources res = getResources();
        mKeyBackgroundDrawable = a.getDrawable(R.styleable.PinKeyboardView_pinkeyboardview_keyBackground);
        mShowUnderline = a.getBoolean(R.styleable.PinKeyboardView_pinkeyboardview_showUnderline, false);
        mUnderlinePadding =
                a.getDimensionPixelSize(R.styleable.PinKeyboardView_pinkeyboardview_underlinePadding,
                        res.getDimensionPixelSize(R.dimen.keyboard_underline_padding));
        int textSize = a.getDimensionPixelSize(R.styleable.PinKeyboardView_pinkeyboardview_textSize,
                res.getDimensionPixelSize(R.dimen.pin_keyboard_default_text_size));
        int textColor = a.getColor(R.styleable.PinKeyboardView_pinkeyboardview_textColor, Color.BLACK);
        int underlineColor = a.getColor(R.styleable.PinKeyboardView_pinkeyboardview_keyUnderlineColor,
                getResources().getColor(R.color.pin_light_gray_50));
        a.recycle();

        mPaint = new Paint();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        }
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);

        mUnderlinePaint = new Paint();

        mUnderlinePaint.setColor(underlineColor);
        float stroke = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                getResources().getDisplayMetrics());
        mUnderlinePaint.setStrokeWidth(stroke);
        setPreviewEnabled(false);
        setKeyboard(new Keyboard(getContext(), R.xml.keyboard_number_pad));
        Drawable back = getResources().getDrawable(R.drawable.key_back);
        back.setColorFilter(textColor, Mode.SRC_ATOP);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Drawable keyBackground = mKeyBackgroundDrawable;
        List<Key> keys = getKeyboard().getKeys();

        for (Key key : keys) {
            if (keyBackground != null && (key.icon != null || key.label != null)) {
                int[] state = key.getCurrentDrawableState();
                keyBackground.setState(state);
                keyBackground.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                keyBackground.draw(canvas);
            }
            if (key.label != null) {
                String label = key.label.toString();
                float desiredW = mPaint.measureText(label);
                float desiredH = mPaint.measureText(label);
                float x = key.x + (key.width / 2);
                float y = key.y + (key.height / 2) + (desiredH / 2);
                canvas.drawText(label, x, y, mPaint);
                if (mShowUnderline) {
                    canvas.drawLine(
                            key.x + mUnderlinePadding,
                            key.y + key.height - mUnderlinePadding,
                            key.x + key.width - mUnderlinePadding,
                            key.y + key.height - mUnderlinePadding,
                            mUnderlinePaint);
                }
            } else if (key.icon != null) {
                final int startX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
                final int startY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
                key.icon.setBounds(
                        startX,
                        startY,
                        startX + key.icon.getIntrinsicWidth(),
                        startY + key.icon.getIntrinsicHeight());
                key.icon.draw(canvas);
            }
        }
    }

    public static abstract class PinPadActionListener implements OnKeyboardActionListener {
        @Override
        public abstract void onKey(int primaryCode, int[] keyCodes);

        @Override
        public void onPress(int primaryCode) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeUp() {
        }
    }
}
