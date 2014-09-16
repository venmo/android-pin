package com.venmo.android.pin;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.venmo.android.pin.util.PinHelper;

import junit.framework.Assert;

public class PinHelperTest extends AndroidTestCase {

    private SharedPreferences prefs;

    @Override
    public void setUp() {
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public void tearDown() {
        prefs.edit().clear().commit();
    }

    public void testValidatingOnEmptyPinFails() {
        try {
            PinHelper.doesMatchDefaultPin(mContext, "1111");
            Assert.fail("Should not be able to validate pin if one has never been saved");
        } catch (NullPointerException npe) {
            //expect NPE
        }
    }

    public void testPinValidation() {
        PinHelper.saveDefaultPin(mContext, "1111");
        assertFalse(PinHelper.doesMatchDefaultPin(mContext, "2222"));
        assertTrue(PinHelper.doesMatchDefaultPin(mContext, "1111"));

        PinHelper.saveDefaultPin(mContext, "2222");
        assertTrue(PinHelper.doesMatchDefaultPin(mContext, "2222"));
        assertFalse(PinHelper.doesMatchDefaultPin(mContext, "1111"));
    }

}
