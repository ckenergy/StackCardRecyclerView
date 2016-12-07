package com.ckenergy.stackcard.sample;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ckenergy.stackcard.stackcardlayoutmanager.StackCardLayoutManager;
import com.ckenergy.stackcard.stackcardlayoutmanager.StackCardPostLayout;

public class HorizontalActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);

        StackCardLayoutManager stackCardLayoutManager = new StackCardLayoutManager(StackCardLayoutManager.HORIZONTAL, false, new StackCardPostLayout());
        RecyclerViewAdapter1 adapter = new RecyclerViewAdapter1(20);

        initRecyclerView(recyclerView,stackCardLayoutManager, adapter);

        Log.d(getClass().getSimpleName(),stackCardLayoutManager.getStackOrder()+"");
    }
}
