package com.ckenergy.stackcard.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.vertical).setOnClickListener(this);
        findViewById(R.id.horizontal).setOnClickListener(this);
        findViewById(R.id.both).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vertical:
                startActivity(new Intent(this,VerticalActivity.class));
                break;
            case R.id.horizontal:
                startActivity(new Intent(this,HorizontalActivity.class));
                break;
            case R.id.both:
                startActivity(new Intent(this,BothActivity.class));
                break;
        }
    }
}
