package com.brouken.fixer;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Utils.setNoSafeVolume(this);

        //Utils.changeIME(this);
    }
}
