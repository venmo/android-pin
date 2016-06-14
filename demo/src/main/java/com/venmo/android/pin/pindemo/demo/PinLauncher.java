package com.venmo.android.pin.pindemo.demo;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class PinLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_launcher);

        final Intent launchIntent = new Intent();

        findViewById(R.id.sdk_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchIntent.setClass(PinLauncher.this, PinDemoActivity.class);

                startActivity(launchIntent);
            }
        });

        findViewById(R.id.support_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchIntent.setClass(PinLauncher.this, PinSupportDemoActivity.class);

                startActivity(launchIntent);
            }
        });
    }

}
