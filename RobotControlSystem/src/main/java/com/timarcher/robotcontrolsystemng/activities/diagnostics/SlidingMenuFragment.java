package com.timarcher.robotcontrolsystemng.activities.diagnostics;

import com.timarcher.robotcontrolsystemng.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SlidingMenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
 
        View v = inflater.inflate(R.layout.fragment_diagnostics_menu, container, false);
 
        //v.setBackgroundColor(Color.RED);
        //TextView tv = (TextView)v.findViewById(R.id.textView);
        //tv.setText("Pane One");
 
        return v;
    }
}
