package com.ckenergy.stackcard.sample;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.ckenergy.stackcard.stackcardlayoutmanager.StackCardLayoutManager;
import com.ckenergy.stackcard.stackcardlayoutmanager.StackCardPostLayout;

public class BothActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_both);

        RecyclerView vRecyclerView = (RecyclerView) findViewById(R.id.list1);

        StackCardLayoutManager vStackCardLayoutManager = new StackCardLayoutManager(StackCardLayoutManager.VERTICAL,true, new StackCardPostLayout());
        initRecyclerView(vRecyclerView, vStackCardLayoutManager, new RecyclerViewAdapter(20));

        final RecyclerView hRecyclerView = (RecyclerView) findViewById(R.id.list2);
        StackCardLayoutManager hStackCardLayoutManager = new StackCardLayoutManager(StackCardLayoutManager.HORIZONTAL, false, new StackCardPostLayout());

        initRecyclerView(hRecyclerView, hStackCardLayoutManager, new RecyclerViewAdapter(20));

    }

}
