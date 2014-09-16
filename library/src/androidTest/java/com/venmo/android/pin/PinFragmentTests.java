package com.venmo.android.pin;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Condition;
import com.robotium.solo.Solo;
import com.venmo.android.pin.PinFragment.PinDisplayType;
import com.venmo.android.pin.util.PinHelper;
import com.venmo.android.pin.view.PinKeyboardView;
import com.venmo.android.pin.view.PinputView;

import junit.framework.Assert;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class PinFragmentTests extends ActivityInstrumentationTestCase2<TestActivity> {

    private static final int WAIT_TIMEOUT_MILLIS = 2000;

    private Solo mSolo;

    public PinFragmentTests() {
        super(TestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mSolo = new Solo(getInstrumentation());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getDefaultSharedPreferences(getInstrumentation().getContext()).edit().clear().commit();
    }

    public void testKeyBoardInput() {
        getActivityHelper(PinDisplayType.VERIFY);

        clickOnKey(Key.ONE);
        assertPinText("1");

        clickOnKey(Key.TWO);
        assertPinText("12");

        clickOnKey(Key.THREE);
        assertPinText("123");

        pressDelete();
        assertPinText("12");
        pressDelete();
        pressDelete();
        assertPinText("");

        pressDelete();
        assertPinText(""); // still empty

        clickOnKey(Key.FOUR);
        assertPinText("4");
        pressDelete();

        clickOnKey(Key.FIVE);
        assertPinText("5");
        pressDelete();

        clickOnKey(Key.SIX);
        assertPinText("6");
        pressDelete();

        clickOnKey(Key.SEVEN);
        assertPinText("7");
        pressDelete();

        clickOnKey(Key.EIGHT);
        assertPinText("8");
        pressDelete();

        clickOnKey(Key.NINE);
        assertPinText("9");
        pressDelete();

        clickOnKey(Key.ZERO);
        assertPinText("0");
    }

    public void testPinCreate() {
        final TestActivity activity = getActivityHelper(PinDisplayType.CREATE);
        clickOnKey(Key.ONE, Key.ONE, Key.ONE, Key.ONE);
        confirmCreationWithPin(activity, Key.ONE, Key.ONE, Key.ONE, Key.ONE);
        assertPinSaved("1111");
    }

    public void testInvalidConfirmation() {
        TestActivity activity = getActivityHelper(PinDisplayType.CREATE);
        clickOnKey(Key.ONE, Key.ONE, Key.ONE, Key.ONE);

        int len = getPinputView().getPinLen();

        String confirmText = String.format(mSolo.getString(R.string.confirm_n_digit_pin), len);
        mSolo.waitForText(confirmText);

        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO);

        String createText = String.format(mSolo.getString(R.string.create_n_digit_pin), len);
        mSolo.waitForText(createText);
        try {
            assertPinSaved("1111");
            Assert.fail("Pin should not have been set");
        } catch (NullPointerException npe) {
            // expect NPE on checking pin
        }
    }

    public void testPinVerification() {
        final TestActivity activity = getActivityHelper(PinDisplayType.VERIFY);
        PinHelper.saveDefaultPin(activity, "1111");

        clickOnKey(Key.ONE, Key.ONE, Key.ONE, Key.ONE);

        assertTrue(validatedPin(activity));
    }

    public void testPinInvalid() {
        final TestActivity activity = getActivityHelper(PinDisplayType.VERIFY);
        PinHelper.saveDefaultPin(activity, "1111");

        clickOnKey(Key.TWO, Key.ONE, Key.ONE, Key.ONE);

        assertTrue(mSolo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return getPinputView().getText().toString().equals("");
            }
        }, 2000));
    }

    public void testCreateWithRotation() {
        final TestActivity activity = getActivityHelper(PinDisplayType.CREATE);
        mSolo.setActivityOrientation(Solo.PORTRAIT);
        getInstrumentation().waitForIdleSync();
        clickOnKey(Key.ONE, Key.ONE);

        mSolo.setActivityOrientation(Solo.LANDSCAPE);
        getInstrumentation().waitForIdleSync();
        assertPinText("11");
        clickOnKey(Key.ONE, Key.ONE);

        confirmCreationWithPin(activity, Key.ONE, Key.ONE, Key.ONE, Key.ONE);
        assertPinSaved("1111");
    }

    public void testVerifyRotation() {
        final TestActivity activity = getActivityHelper(PinDisplayType.VERIFY);
        mSolo.setActivityOrientation(Solo.PORTRAIT);
        getInstrumentation().waitForIdleSync();
        PinHelper.saveDefaultPin(activity, "1111");

        clickOnKey(Key.ONE, Key.ONE);

        mSolo.setActivityOrientation(Solo.LANDSCAPE);
        getInstrumentation().waitForIdleSync();
        assertPinText("11");

        clickOnKey(Key.ONE, Key.ONE);
        validatedPin(activity);
    }

    public void testMaxTries() {
        final TestActivity activity = getActivityHelper(PinDisplayType.VERIFY, 3);
        PinHelper.saveDefaultPin(activity, "1111");
        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO); // incorrect
        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO); // incorrect
        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO); // incorrect

        assertTrue(reachedMaxTries(activity));
    }

    public void testIncorrectTriesReset() {
        final TestActivity activity = getActivityHelper(PinDisplayType.VERIFY, 3);
        PinHelper.saveDefaultPin(activity, "1111");
        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO); // incorrect
        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO); // incorrect
        clickOnKey(Key.ONE, Key.ONE, Key.ONE, Key.ONE); // correct

        clickOnKey(Key.TWO, Key.TWO, Key.TWO, Key.TWO); // incorrect (third time)
        assertFalse(reachedMaxTries(activity));
    }

    private PinKeyboardView getKeyboard() {
        return (PinKeyboardView) mSolo.getView(R.id.pin_keyboard);
    }

    private PinputView getPinputView() {
        return (PinputView) mSolo.getView(R.id.pin_pinputview);
    }

    private void assertPinText(String pin) {
        assertEquals(pin, getPinputView().getText().toString());
    }

    private void assertPinSaved(String pin) {
        assertTrue(PinHelper.doesMatchDefaultPin(mSolo.getCurrentActivity(), pin));
    }

    private boolean reachedMaxTries(final TestActivity activity) {
        return mSolo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return activity.reachedMaxTries.get();
            }
        }, WAIT_TIMEOUT_MILLIS);
    }

    private boolean validatedPin(final TestActivity activity) {
        return mSolo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return activity.validated.get();
            }
        }, WAIT_TIMEOUT_MILLIS);
    }

    private void confirmCreationWithPin(final TestActivity activity, Key... keys) {
        int len = getPinputView().getPinLen();
        String confirmText = String.format(mSolo.getString(R.string.confirm_n_digit_pin), len);
        assertTrue(mSolo.waitForText(confirmText));

        clickOnKey(keys);
        assertTrue(mSolo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return activity.created.get();
            }
        }, WAIT_TIMEOUT_MILLIS));
    }

    private void pressDelete() {
        clickOnKey(Key.BACK);
    }

    private void clickOnKey(Key... mapKey) {
        for (Key k : mapKey) {
            int[] loc = new int[2];
            Keyboard.Key key = getKeyboard().getKeyboard().getKeys().get(k.index);
            getKeyboard().getLocationOnScreen(loc);
            int x = loc[0] + (key.width / 2) + (key.width * (k.index % 3));
            int y = loc[1] + (key.height / 2) + (key.height * (k.index / 3));
            mSolo.clickOnScreen(x, y);
        }
    }

    private TestActivity getActivityHelper(PinDisplayType type) {
        return getActivityHelper(type, PinFragmentConfiguration.UNLIMITED_TRIES);
    }

    private TestActivity getActivityHelper(PinDisplayType type, int maxTries) {
        Intent i = new Intent();
        i.putExtra(TestActivity.KEY_DISPLAY_TYPE, type);
        i.putExtra(TestActivity.KEY_MAX_TRIES, maxTries);
        setActivityIntent(i);
        return getActivity();
    }

    private enum Key {
        ONE(0), TWO(1), THREE(2), FOUR(3), FIVE(4), SIX(5), SEVEN(6), EIGHT(7), NINE(8), ZERO(10),
        BACK(11);
        private int index;

        private Key(int index) {
            this.index = index;
        }
    }
}