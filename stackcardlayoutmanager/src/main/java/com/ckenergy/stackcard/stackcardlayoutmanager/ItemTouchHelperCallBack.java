package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by chengkai on 2016/11/28.
 */
public class ItemTouchHelperCallBack extends ItemTouchHelper.Callback {

    private onSwipListener mOnSwipListener;

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags;
        final int swipeFlags;
        if (recyclerView.getLayoutManager() instanceof StackCardLayoutManager){
            StackCardLayoutManager lm = (StackCardLayoutManager) recyclerView.getLayoutManager();
            dragFlags = 0;
            if (lm.getOrientation() == StackCardLayoutManager.HORIZONTAL) {
                swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }else {
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }
        }else {
            dragFlags = 0;
            swipeFlags = 0;
        }
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    public onSwipListener getOnSwipListener() {
        return mOnSwipListener;
    }

    public void setOnSwipListener(onSwipListener onSwipListener) {
        this.mOnSwipListener = onSwipListener;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//        Log.d("onSwiped","direction:"+direction);
        int position = viewHolder.getAdapterPosition();
        if (mOnSwipListener != null) {
            mOnSwipListener.onSwip(viewHolder,position);
        }
    }

    public interface onSwipListener {
        void onSwip(RecyclerView.ViewHolder viewHolder, int position);
    }
}
