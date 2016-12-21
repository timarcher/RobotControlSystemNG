package com.timarcher.robotcontrolsystemng.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Menu;

import com.timarcher.robotcontrolsystemng.R;
import com.timarcher.robotcontrolsystemng.activities.diagnostics.SlidingMenuFragment;
import com.timarcher.robotcontrolsystemng.activities.diagnostics.SonarFragment;

public class DiagnosticsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostics);

        SlidingPaneLayout mPane = (SlidingPaneLayout) findViewById(R.id.diagnostics_activity_main_pane);

        //mPane.openPane();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.diagnostics_activity_sliding_menu_pane, new SlidingMenuFragment(), "diagnostics_activity_sliding_menu_pane").commit();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.diagnostics_activity_content_pane, new SonarFragment(), "diagnostics_activity_content_pane").commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
