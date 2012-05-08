package com.github.ignition.samples.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;

import com.github.ignition.core.widgets.ScrollingTextView;

public class ScrollingTextViewActivity extends Activity {

    private ScrollingTextView scrollingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrolling_textview_sample);

        scrollingText = (ScrollingTextView) findViewById(android.R.id.text1);

        InputStream is = getResources().openRawResource(R.raw.scrolling_text);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            line = br.readLine();
            while (line != null) {
                sb.append(line + "\n");
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        scrollingText.setText(sb.toString());
    }

}
