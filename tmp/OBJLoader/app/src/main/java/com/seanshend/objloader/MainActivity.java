package com.seanshend.objloader;

import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // Debug.startMethodTracing("calc");
        super.onCreate(savedInstanceState);
        setContentView(new MyRenderer(this));
    }

    /**
     * Remember to resume the glSurface
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Also pause the glSurface
     */
    @Override
    protected void onPause() {
        Debug.stopMethodTracing();
        super.onPause();
    }
}
