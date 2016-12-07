package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class StackCardChildSelectionListener {

    @NonNull
    private final RecyclerView mRecyclerView;
    @NonNull
    private final StackCardLayoutManager mStackCardLayoutManager;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
            final int position = holder.getAdapterPosition();

            if (position == mStackCardLayoutManager.getCenterItemPosition()) {
                onCenterItemClicked(mRecyclerView, mStackCardLayoutManager, v);
            } else {
                onBackItemClicked(mRecyclerView, mStackCardLayoutManager, v);
            }
        }
    };

    protected StackCardChildSelectionListener(@NonNull final RecyclerView recyclerView, @NonNull final StackCardLayoutManager stackCardLayoutManager) {
        mRecyclerView = recyclerView;
        mStackCardLayoutManager = stackCardLayoutManager;

        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(final View view) {
                view.setOnClickListener(mOnClickListener);
            }

            @Override
            public void onChildViewDetachedFromWindow(final View view) {
                view.setOnClickListener(null);
            }
        });
    }

    protected abstract void onCenterItemClicked(@NonNull final RecyclerView recyclerView, @NonNull final StackCardLayoutManager stackCardLayoutManager, @NonNull final View v);

    protected abstract void onBackItemClicked(@NonNull final RecyclerView recyclerView, @NonNull final StackCardLayoutManager stackCardLayoutManager, @NonNull final View v);
}