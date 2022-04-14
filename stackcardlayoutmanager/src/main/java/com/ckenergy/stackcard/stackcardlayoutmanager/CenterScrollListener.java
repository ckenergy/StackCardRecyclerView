package com.ckenergy.stackcard.stackcardlayoutmanager;


import androidx.recyclerview.widget.RecyclerView;

/**
 * Class for centering items after scroll event.<br />
 * This class will listen to current scroll state and if item is not centered after scroll it will automatically scroll it to center.
 */
public class CenterScrollListener extends RecyclerView.OnScrollListener {

    private boolean mAutoSet = true;

    private int arrow;

    @Override
    public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        super.onScrollStateChanged(recyclerView, newState);
//        Log.d(getClass().getSimpleName(),"newState:"+newState);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof StackCardLayoutManager)) {
            mAutoSet = true;
            return;
        }

        final StackCardLayoutManager lm = (StackCardLayoutManager) layoutManager;
        if (!mAutoSet) {
            if (RecyclerView.SCROLL_STATE_IDLE == newState || RecyclerView.SCROLL_STATE_SETTLING == newState) {
                final int scrollNeeded = lm.getOffsetCenterView();
                int itemSize= lm.getScrollItemSize();
                int centerPosition = lm.getCenterItemPosition();
//                Log.d(this.getClass().getSimpleName(),"scrollNeeded:"+scrollNeeded+",centerPosition:"+centerPosition);
//                Log.d(getClass().getSimpleName(),"newState:"+newState);
                int totalOffset = centerPosition*itemSize-scrollNeeded;
//                Log.d(getClass().getSimpleName(),"totalOffset:"+totalOffset);

                int distance = scrollNeeded;
                if (totalOffset > (lm.getItemCount()-1)*itemSize) {
                    distance = (lm.getItemCount()-1)*itemSize-totalOffset;
                }else if (totalOffset < 0) {
                    distance = -totalOffset;
                }else {
                    if (Math.abs(scrollNeeded) > itemSize/10) {// move itemsize 1/20 than move to next
                        if (lm.getNumberOrder()*arrow*scrollNeeded < 0) {
                            distance = (int) (scrollNeeded-Math.signum(scrollNeeded)*itemSize);
                        }
                    }
                }
//                Log.d(this.getClass().getSimpleName(),"distance:"+distance);
                if (StackCardLayoutManager.HORIZONTAL == lm.getOrientation()) {
                    recyclerView.smoothScrollBy(distance*lm.getNumberOrder(), 0);
                } else {
                    recyclerView.smoothScrollBy(0, distance*lm.getNumberOrder());
                }
                mAutoSet = true;
            }
        }
        if (RecyclerView.SCROLL_STATE_DRAGGING == newState || RecyclerView.SCROLL_STATE_SETTLING == newState) {
            mAutoSet = false;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//        Log.d(this.getClass().getSimpleName(),"dx:"+dx+",dy:"+dy);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof StackCardLayoutManager)) {
            mAutoSet = true;
            return;
        }

        final StackCardLayoutManager lm = (StackCardLayoutManager) layoutManager;
        if (!mAutoSet) {
            if (StackCardLayoutManager.HORIZONTAL == lm.getOrientation()) {
                arrow = (int) Math.signum(dx);
            }else {
                arrow = (int) Math.signum(dy);
            }
        }
    }
}