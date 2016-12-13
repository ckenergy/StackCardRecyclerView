package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

/**
 * Created by chengkai on 2016/11/28.
 */
public class ItemTouchHelperCallBack extends ItemTouchHelper.Callback {

    private onSwipListener mOnSwipListener;

    private boolean isStartSwip = false;

    private Rect beforeCurrentRect;
    private Rect beforeBelowRect;

    private View belowView;

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        Log.d(getClass().getSimpleName(),"onMoved");
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        Log.d(getClass().getSimpleName(),"getMovementFlags");
        if (!(recyclerView.getLayoutManager() instanceof StackCardLayoutManager)) {
            return makeMovementFlags(0, 0);
        }
        final int dragFlags;
        final int swipeFlags;
        StackCardLayoutManager layoutManager = (StackCardLayoutManager) recyclerView.getLayoutManager();
        dragFlags = 0;
        if (layoutManager.getOrientation() == StackCardLayoutManager.HORIZONTAL) {
            swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        }else {
            swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        }

        if (!isStartSwip) {
            beforeCurrentRect = ViewCompat.getClipBounds(viewHolder.itemView);
            isStartSwip = true;
            int current = viewHolder.getAdapterPosition();
            Log.d(getClass().getSimpleName(),"current:"+current);
            Rect rect = new Rect(0, 0, viewHolder.itemView.getWidth(), viewHolder.itemView.getHeight());
            ViewCompat.setClipBounds(viewHolder.itemView, rect);
            int belowPosition;
            if (layoutManager.getStackOrder() == StackCardLayoutManager.IN_STACK_ORDER) {
                belowPosition = current - 1;
            }else {
                belowPosition = current + 1;
            }
            belowView = layoutManager.findViewByPosition(belowPosition);
//            recyclerView.getAdapter().
            Log.d(getClass().getSimpleName(),"belowView:"+belowView);
            if(belowView != null) {
                beforeBelowRect = ViewCompat.getClipBounds(belowView);
                Rect belowRect;
                if (layoutManager.getOrientation() == StackCardLayoutManager.HORIZONTAL) {
                    float scale = viewHolder.itemView.getScaleY() / belowView.getScaleY();
                    int width = Math.round(beforeCurrentRect.width()*scale)+beforeBelowRect.width();
                    if (layoutManager.getStackOrder() * layoutManager.getNumberOrder() < 0) {
                        belowRect = new Rect(belowView.getWidth()-width, 0, belowView.getWidth(), beforeBelowRect.height());
                    }else {
                        belowRect = new Rect(0, 0, width, beforeBelowRect.height());
                    }
                }else {
                    float scale = viewHolder.itemView.getScaleX()/belowView.getScaleX();
                    int height = Math.round(beforeCurrentRect.height()*scale)+beforeBelowRect.height();
                    if (layoutManager.getStackOrder() * layoutManager.getNumberOrder() < 0) {
                        belowRect = new Rect(0, belowView.getHeight()-height, belowView.getWidth(), belowView.getHeight());
                    }else {
                        belowRect = new Rect(0, 0, beforeBelowRect.width(), height);
                    }
                }
                ViewCompat.setClipBounds(belowView, belowRect);
            }

        }
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Log.d(getClass().getSimpleName(),"clearView:"+viewHolder.getAdapterPosition());
        if (isStartSwip) {
            isStartSwip = false;
            if (viewHolder.getAdapterPosition() > 0 ) {
                if (belowView != null) {
                    ViewCompat.setClipBounds(belowView, beforeBelowRect);
                }
                ViewCompat.setClipBounds(viewHolder.itemView, beforeCurrentRect);
            }
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        Log.d(getClass().getSimpleName(),"onMove");
        return true;
    }

    public onSwipListener getOnSwipListener() {
        return mOnSwipListener;
    }

    public void setOnSwipListener(onSwipListener onSwipListener) {
        this.mOnSwipListener = onSwipListener;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(getClass().getSimpleName(),"onSwiped,direction:"+direction);
        int position = viewHolder.getAdapterPosition();
        if (mOnSwipListener != null) {
            mOnSwipListener.onSwip(viewHolder,position);
        }
    }

    public interface onSwipListener {
        void onSwip(RecyclerView.ViewHolder viewHolder, int position);
    }
}
