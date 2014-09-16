package com.venmo.android.pin.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class PinHelper {
    private static final String KEY_PINPUT_PIN_HASH = "com.venmo.pin.pinputview_pin";
    private static final String KEY_PR_SALT = "com.venmo.pin.pr_salt";

    // default pin encryption settings
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ROUNDS = 100;
    private static final int KEY_LEN = 256;
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";

    private static byte[] generateSalt() {
        byte[] salt = new byte[24];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private static byte[] hash(char[] pin, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, ROUNDS, KEY_LEN);
        Arrays.fill(pin, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean validate(char[] actual, byte[] expected, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] pwdHash = hash(actual, salt);
        Arrays.fill(actual, Character.MIN_VALUE);
        if (pwdHash.length != expected.length) return false;
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != expected[i]) return false;
        }
        return true;
    }

    public static boolean hasDefaultPinSaved(Context c) {
        return getDefaultSharedPreferences(c).getString(KEY_PINPUT_PIN_HASH, null) != null;
    }

    public static void resetDefaultSavedPin(Context c) {
        getDefaultSharedPreferences(c).edit()
                .clear()
                .commit();
    }

    public static boolean doesMatchDefaultPin(Context c, String pin) {
        try {
            SharedPreferences def = getDefaultSharedPreferences(c);
            return validate(pin.toCharArray(),
                    getPinHashFromPreferences(def),
                    getSaltFromPreferences(def));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("error validating pin", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("error validating pin", e);
        }
    }

    public static void saveDefaultPin(Context context, String pin) {
        try {
            final byte[] salt = generateSalt();
            final byte[] hash = hash(pin.toCharArray(), salt);

            // save salt & pin after successful hashing
            saveToPreferences(getDefaultSharedPreferences(context), salt, hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("error saving pin: ", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("error saving pin: ", e);
        }
    }

    private static void saveToPreferences(SharedPreferences prefs, byte[] salt, byte[] hash) {
        prefs.edit()
                .putString(KEY_PR_SALT, encode(salt))
                .putString(KEY_PINPUT_PIN_HASH, encode(hash))
                .commit();
    }

    private static byte[] getSaltFromPreferences(SharedPreferences prefs) {
        return decode(getStringFromPrefsOrThow(prefs, KEY_PR_SALT));
    }

    private static byte[] getPinHashFromPreferences(SharedPreferences prefs) {
        return decode(getStringFromPrefsOrThow(prefs, KEY_PINPUT_PIN_HASH));
    }

    private static String getStringFromPrefsOrThow(SharedPreferences prefs, String key) {
        String val = prefs.getString(key, null);
        if (val == null) {
            throw new NullPointerException("Trying to retrieve pin value before it's been set");
        }
        return val;
    }

    private static String encode(byte[] src) {
        return Base64.encodeToString(src, Base64.DEFAULT);
    }

    private static byte[] decode(String src) {
        return Base64.decode(src, Base64.DEFAULT);
    }

}
