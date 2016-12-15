package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.view.View;

/**
 * Custom implementation of {@link android.support.v7.widget.RecyclerView.SmoothScroller} that can work only with {@link StackCardLayoutManager}.
 *
 * @see StackCardLayoutManager
 */
public abstract class StackCardSmoothScroller extends LinearSmoothScroller {

    protected StackCardSmoothScroller(final Context context) {
        super(context);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public int calculateDyToMakeVisible(final View view, final int snapPreference) {
        final StackCardLayoutManager layoutManager = (StackCardLayoutManager) getLayoutManager();
        if (null == layoutManager || !layoutManager.canScrollVertically()) {
            return 0;
        }
        int dy = layoutManager.getOffsetForCurrentView(view);
        return dy;
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public int calculateDxToMakeVisible(final View view, final int snapPreference) {
        final StackCardLayoutManager layoutManager = (StackCardLayoutManager) getLayoutManager();
        if (null == layoutManager || !layoutManager.canScrollHorizontally()) {
            return 0;
        }
        int dx = layoutManager.getOffsetForCurrentView(view);
        return dx;
    }

}