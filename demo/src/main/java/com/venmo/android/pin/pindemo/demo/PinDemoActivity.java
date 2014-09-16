package com.venmo.android.pin.pindemo.demo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.venmo.android.pin.AsyncSaver;
import com.venmo.android.pin.AsyncValidator;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.util.PinHelper;

import java.io.IOError;
import java.io.IOException;

public class PinDemoActivity extends Activity implements PinFragment.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_demo);
        Fragment toShow = PinHelper.hasDefaultPinSaved(this) ?
                PinFragment.newInstanceForVerification() :
                PinFragment.newInstanceForCreation();

        getFragmentManager().beginTransaction()
                .replace(R.id.root, toShow, toShow.getClass().getSimpleName())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pin_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            PinHelper.resetDefaultSavedPin(this);
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidated() {
        Toast.makeText(this, "Validated PIN!", Toast.LENGTH_SHORT).show();
        recreate();
    }

    @Override
    public void onPinCreated() {
        Toast.makeText(this, "Created PIN!", Toast.LENGTH_SHORT).show();
        recreate();
    }

}
