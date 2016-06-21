package com.venmo.android.pin;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.venmo.android.pin.test.R;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.venmo.android.pin.PinFragmentConfiguration.UNLIMITED_TRIES;

public class TestSupportActivity extends FragmentActivity implements PinListener {

    static final String KEY_DISPLAY_TYPE = "com.venmo.test.pin_display_type";
    static final String KEY_MAX_TRIES = "com.venmo.test.pin_max_tries";
    AtomicBoolean created = new AtomicBoolean(false);
    AtomicBoolean validated = new AtomicBoolean(false);
    AtomicBoolean reachedMaxTries = new AtomicBoolean(false);

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.layout_pin_test);
        PinDisplayType type = (PinDisplayType) getIntent().getSerializableExtra(KEY_DISPLAY_TYPE);
        int maxTries = getIntent().getIntExtra(KEY_MAX_TRIES, UNLIMITED_TRIES);
        if (type != null) {
            PinFragmentConfiguration config = new PinFragmentConfiguration(this)
                    .maxTries(maxTries, new TryDepletionListener() {
                        @Override
                        public void onTriesDepleted() {
                            reachedMaxTries.set(true);
                        }
                    });
            PinSupportFragment pf = type == PinDisplayType.CREATE ?
                    PinSupportFragment.newInstanceForCreation(config) :
                    PinSupportFragment.newInstanceForVerification(config);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, pf, PinFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onValidated() {
        validated.set(true);
    }

    @Override
    public void onPinCreated() {
        created.set(true);
    }
}
