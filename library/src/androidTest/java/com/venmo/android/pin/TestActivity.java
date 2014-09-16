package com.venmo.android.pin;

import android.app.Activity;
import android.os.Bundle;

import com.venmo.android.pin.PinFragment.Listener;
import com.venmo.android.pin.PinFragment.PinDisplayType;
import com.venmo.android.pin.test.R;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.venmo.android.pin.PinFragmentConfiguration.UNLIMITED_TRIES;

public class TestActivity extends Activity implements Listener {

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
            PinFragment pf = type == PinDisplayType.CREATE ?
                    PinFragment.newInstanceForCreation(config) :
                    PinFragment.newInstanceForVerification(config);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, pf, PinFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onValidated() {validated.set(true);}

    @Override
    public void onPinCreated() {created.set(true);}
}
