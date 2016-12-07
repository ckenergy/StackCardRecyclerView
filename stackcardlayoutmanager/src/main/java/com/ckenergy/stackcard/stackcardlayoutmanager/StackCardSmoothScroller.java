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

    //该方法控制速度。
    //if returned value is 2 ms, it means scrolling 1000 pixels with LinearInterpolation should take 2 seconds.
    /*@Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                *//*
                     控制单位速度,  毫秒/像素, 滑动1像素需要多少毫秒.

                     默认为 (25F/densityDpi) 毫秒/像素

                     mdpi上, 1英寸有160个像素点, 25/160,
                     xxhdpi,1英寸有480个像素点, 25/480,
                  *//*

        //return 10F / displayMetrics.densityDpi;//可以减少时间，默认25F
        float pixel = super.calculateSpeedPerPixel(displayMetrics);
        Log.d("StackCardSmoothScroller","pixel:"+pixel);
        return pixel;
    }

    //该方法计算滑动所需时间。在此处间接控制速度。
    //Calculates the time it should take to scroll the given distance (in pixels)
    @Override
    protected int calculateTimeForScrolling(int dx) {
               *//*
                   控制距离, 然后根据上面那个方(calculateSpeedPerPixel())提供的速度算出时间,

                   默认一次 滚动 TARGET_SEEK_SCROLL_DISTANCE_PX = 10000个像素,

                   在此处可以减少该值来达到减少滚动时间的目的.
                *//*

        //间接计算时提高速度，也可以直接在calculateSpeedPerPixel提高
        if (dx > 2000) {
            dx = 2000;
        }

        int time = super.calculateTimeForScrolling(dx);
        Log.d("StackCardSmoothScroller","time:"+time);//打印时间看下

        return time;
    }*/
}