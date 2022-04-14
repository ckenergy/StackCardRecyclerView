package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.annotation.TargetApi;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link RecyclerView.LayoutManager} that layout items like Stack Card.
 * Generally there is one center item and bellow this item there are maximum {@link StackCardLayoutManager#getMaxVisibleItems()} items on each side of the center
 * item. By default {@link StackCardLayoutManager#getMaxVisibleItems()} is {@link StackCardLayoutManager#MAX_VISIBLE_ITEMS}.<br />
 * <br />
 * This LayoutManager supports only fixedSized adapter items.<br />
 * <br />
 * This LayoutManager supports {@link StackCardLayoutManager#HORIZONTAL} and {@link StackCardLayoutManager#VERTICAL} orientations. <br />
 * <br />
 * This LayoutManager supports circle layout. By default it if disabled. We don't recommend to use circle layout with adapter items count less then 3. <br />
 * <br />
 * Please be sure that layout_width of adapter item is a constant value and not {@link ViewGroup.LayoutParams#MATCH_PARENT}
 * for {@link #HORIZONTAL} orientation.
 * So like layout_height is not {@link ViewGroup.LayoutParams#MATCH_PARENT} for {@link StackCardLayoutManager#VERTICAL}<br />
 * <br />
 */
@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
public class StackCardLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "StackCardLayoutManager";

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

//    public static final int

    public static final int INVALID_POSITION = -1;
    public static final int MAX_VISIBLE_ITEMS = 3;

    /**
     * small number in the bottom,
     */
    public static final int IN_STACK_ORDER = 1;
    /**
     * big number in the bottom,
     */
    public static final int OUT_STACK_ORDER = -1;

    /**
     * left to right, top to bottom
     */
    public static final int NEGATIVE_ORDER = -1;
    /**
     * right to left, bottom to top
     */
    public static final int POSITIVE_ORDER = 1;

    private int mStackOrder;

    private int mNumberOrder;

    private boolean mIsRemoveLayout;

    private int mRemoveBelowPosition;

    private List<RemoveBean> mRemoveList;

    private RecyclerView mRecyclerView;


//    private int mRemoveBelowPosition;

//    public static final float SMALL_DISTANCE_RATIO = 35f;
//    public static final float MEDIUM_DISTANCE_RATIO = 12f;
//    public static final float BIG_DISTANCE_RATIO = 2.5f;

//    public static final float SMALL_DISTANCE_RATIO = 25f;
//    public static final float MEDIUM_DISTANCE_RATIO = 6f;
//    public static final float BIG_DISTANCE_RATIO = 2f;

    private static final boolean CIRCLE_LAYOUT = false;

//    private int layoutCountType;

    private Integer mDecoratedChildWidth;
    private Integer mDecoratedChildHeight;

    private final int mOrientation;
    private final boolean mCircleLayout;

    private int mPendingScrollPosition;

    private final LayoutHelper mLayoutHelper = new LayoutHelper(MAX_VISIBLE_ITEMS);

    private IPostLayout mViewPostLayout;

    private final List<OnCenterItemSelectionListener> mOnCenterItemSelectionListeners = new ArrayList<>();
    private int mCenterItemPosition = INVALID_POSITION;
    private int mItemsCount;

    private StackCardSavedState mPendingStackCardSavedState;

    private float mBaseScale;

    private int mCenterViewStart;

    private int mOverScrollMode;

    /**
     * implements {@link IPostLayout} to how layout item
     * @param orientation should be {@link #VERTICAL} or {@link #HORIZONTAL}
     */
    public StackCardLayoutManager(final int orientation, @NonNull IPostLayout iPostLayout) {
        this(orientation, CIRCLE_LAYOUT, iPostLayout);
    }

    /**
     * If circleLayout is true then all items will be in cycle. Scroll will be infinite on both sides.
     *
     * @param orientation  should be {@link #VERTICAL} or {@link #HORIZONTAL}
     * @param circleLayout true for enabling circleLayout
     */
    public StackCardLayoutManager(final int orientation, final boolean circleLayout, @NonNull IPostLayout iPostLayout) {
        this(orientation, circleLayout, IN_STACK_ORDER, iPostLayout);
    }

    public StackCardLayoutManager(final int orientation, final boolean circleLayout,int stackOrder, @NonNull IPostLayout iPostLayout) {
        this(orientation, circleLayout, stackOrder, POSITIVE_ORDER, iPostLayout);
    }

    public StackCardLayoutManager(final int orientation, final boolean circleLayout,int stackOrder, int numberOrder, @NonNull IPostLayout iPostLayout) {
        if (HORIZONTAL != orientation && VERTICAL != orientation) {
            throw new IllegalArgumentException("orientation should be HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
        mCircleLayout = circleLayout;
        mStackOrder = (int) Math.signum(stackOrder);
        mNumberOrder = (int) Math.signum(numberOrder);
        mPendingScrollPosition = INVALID_POSITION;
        this.mViewPostLayout = iPostLayout;
    }

    public float getBaseScale() {
        return mBaseScale;
    }

    /**
     * set the layout stack Order
     *
     * @return type of {@link StackCardLayoutManager#IN_STACK_ORDER} or {@link StackCardLayoutManager#IN_STACK_ORDER}
     */
    public int getStackOrder() {
        return mStackOrder;
    }

    /**
     * set the layout stack Order
     *
     * @param stackOrder type of {@link StackCardLayoutManager#IN_STACK_ORDER} or {@link StackCardLayoutManager#IN_STACK_ORDER}
     */
    public void setStackOrder(int stackOrder) {
        if (this.mStackOrder == stackOrder) {
            return;
        }
        this.mStackOrder = (int) Math.signum(stackOrder);
        requestLayout();
    }

    /**
     * get the layout number order
     *
     * @return type of {@link StackCardLayoutManager#NEGATIVE_ORDER} or {@link StackCardLayoutManager#POSITIVE_ORDER}
     */
    public int getNumberOrder() {
        return mNumberOrder;
    }

    /**
     * set the layout number order
     *
     * @param numberOrder type of {@link StackCardLayoutManager#NEGATIVE_ORDER} or {@link StackCardLayoutManager#POSITIVE_ORDER}
     */
    public void setNumberOrder(int numberOrder) {
        if (this.mNumberOrder == numberOrder) {
            return;
        }
        this.mNumberOrder = (int) Math.signum(numberOrder);
        requestLayout();
    }

    /**
     * Setup {@link IPostLayout} for this LayoutManager.
     * Its methods will be called for each visible view item after general LayoutManager layout finishes. <br />
     * <br />
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.
     *
     * @param postLayout listener for item layout changes. Can be null.
     */
    public void setPostLayoutListener(@Nullable final IPostLayout postLayout) {
        if (postLayout == null) {
            return;
        }
        mViewPostLayout = postLayout;
        requestLayout();
    }

    /**
     * Setup maximum visible (layout) items on each side of the center item.
     * Basically during scrolling there can be more visible items (+1 item on each side), but in idle state this is the only reached maximum.
     *
     * @param maxVisibleItems should be great then 0, if bot an {@link IllegalAccessException} will be thrown
     */
    @CallSuper
    public void setMaxVisibleItems(final int maxVisibleItems) {
        if (0 >= maxVisibleItems) {
            throw new IllegalArgumentException("maxVisibleItems can't be less then 1");
        }
        if (mLayoutHelper.mMaxVisibleItems == maxVisibleItems) {
            return;
        }
        mLayoutHelper.mMaxVisibleItems = maxVisibleItems;
        requestLayout();
    }

    /**
     * @return current setup for maximum visible items.
     * @see #setMaxVisibleItems(int)
     */
    public int getMaxVisibleItems() {
        return mLayoutHelper.mMaxVisibleItems;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * @return current layout orientation
     * @see #VERTICAL
     * @see #HORIZONTAL
     */
    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public boolean canScrollHorizontally() {
        return 0 != getChildCount() && HORIZONTAL == mOrientation;
    }

    @Override
    public boolean canScrollVertically() {
        return 0 != getChildCount() && VERTICAL == mOrientation;
    }

    @Override
    public void onAttachedToWindow(final RecyclerView recyclerView) {
        super.onAttachedToWindow(recyclerView);
        mRecyclerView = recyclerView;
        mOverScrollMode = recyclerView.getOverScrollMode();
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);//remove the can't scroll effect
//        Log.d(TAG,"sdk:"+Build.VERSION.SDK_INT);
//        mRecyclerView.getItemAnimator().setMoveDuration(1000);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        mRecyclerView.setOverScrollMode(mOverScrollMode);
        mRecyclerView = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setOutLine(RecyclerView recyclerView) {
        ChildViewOutlineProvider outlineProvider = new ChildViewOutlineProvider();
        int count = recyclerView.getChildCount();
        for(int i = 0; i<count;i++) {
            View view = recyclerView.getChildAt(i);
            if(view != null) {
                view.setOutlineProvider(outlineProvider);
            }
        }
    }

    /**
     * @return current layout center item
     */
    public int getCenterItemPosition() {
        return mCenterItemPosition;
    }

    /**
     * @param onCenterItemSelectionListener listener that will trigger when ItemSelectionChanges. can't be null
     */
    public void addOnItemSelectionListener(@NonNull final OnCenterItemSelectionListener onCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.add(onCenterItemSelectionListener);
    }

    /**
     * @param onCenterItemSelectionListener listener that was previously added by {@link #addOnItemSelectionListener(OnCenterItemSelectionListener)}
     */
    public void removeOnItemSelectionListener(@NonNull final OnCenterItemSelectionListener onCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.remove(onCenterItemSelectionListener);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void scrollToPosition(final int position) {
        if (0 > position) {
            throw new IllegalArgumentException("position can't be less then 0. position is : " + position);
        }
        mPendingScrollPosition = position;
//        Log.d(getClass().getSimpleName(),"position:"+position);
        requestLayout();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void smoothScrollToPosition(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.State state, final int position) {

        final StackCardSmoothScroller mySmoothScroller =
                new StackCardSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public PointF computeScrollVectorForPosition(final int targetPosition) {
                        if (0 > targetPosition) {
                            throw new IllegalArgumentException("position can't be less then 0. position is : " + position);
                        }
                        if (targetPosition >= state.getItemCount()) {
                            throw new IllegalArgumentException("position can't be great then adapter items count. position is : " + position);
                        }
                        return StackCardLayoutManager.this.computeScrollVectorForPosition(targetPosition);
                    }
                };

        mySmoothScroller.setTargetPosition(position);
        startSmoothScroll(mySmoothScroller);
    }

    protected PointF computeScrollVectorForPosition(final int targetPosition) {
        if (0 == getChildCount()) {
            return null;
        }

//        Log.d(TAG, "computeScrollVectorForPosition,centerPosition:"+targetPosition);

        final float currentScrollPosition = makeScrollPositionInRange0ToCount(getCurrentScrollPosition(), mItemsCount);
        int direction = targetPosition < currentScrollPosition ? -1 : 1;
        direction = direction*getNumberOrder();
        if (HORIZONTAL == mOrientation) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (HORIZONTAL == mOrientation) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    @Override
    public int scrollHorizontallyBy(final int dx, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (VERTICAL == mOrientation) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    /**
     * This method is called from {@link #scrollHorizontallyBy(int, RecyclerView.Recycler, RecyclerView.State)} and
     * {@link #scrollVerticallyBy(int, RecyclerView.Recycler, RecyclerView.State)} to calculate needed scroll that is allowed. <br />
     * <br />
     * This method may do relayout work.
     *
     * @param diff     distance that we want to scroll by
     * @param recycler Recycler to use for fetching potentially cached views for a position
     * @param state    Transient state of RecyclerView
     * @return distance that we actually scrolled by
     */
    @CallSuper
    protected int scrollBy(final int diff, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == getChildCount() || 0 == diff) {
            return 0;
        }
        final int resultScroll;
        if (mCircleLayout) {
            resultScroll = diff;

            mLayoutHelper.mScrollOffset += resultScroll;

            final int maxOffset = getScrollItemSize() * mItemsCount;
            while (0 > mLayoutHelper.mScrollOffset) {
                mLayoutHelper.mScrollOffset += maxOffset;
            }
            while (mLayoutHelper.mScrollOffset > maxOffset) {
                mLayoutHelper.mScrollOffset -= maxOffset;
            }

            mLayoutHelper.mScrollOffset -= resultScroll;
        } else {

            float currentScrollPosition = getCurrentScrollPosition();
            float scale = 1;
            if (getNumberOrder() == POSITIVE_ORDER) {
                if (diff < 0 && currentScrollPosition <= 0 ) {
                    if (getStackOrder() == OUT_STACK_ORDER) {
                        scale = (float) (Math.pow(1.1f,currentScrollPosition))*2;
                    }else {
                        scale = (float) (Math.pow(1.1f,currentScrollPosition))/2;
                    }
                }else if (diff > 0 && currentScrollPosition >= mItemsCount-1){
                    if (getStackOrder() == OUT_STACK_ORDER) {
                        scale = (float) (Math.pow(1.1f,mItemsCount-1-currentScrollPosition))/2;
                    }else {
                        scale = (float) (Math.pow(1.1f,(mItemsCount-1-currentScrollPosition)))*2;
                    }
                }
            }else {
                if (diff > 0 && currentScrollPosition <= 0 ) {
                    if (getStackOrder() == OUT_STACK_ORDER) {
                        scale = (float) (Math.pow(1.1f, currentScrollPosition))*2;
                    }else {
                        scale = (float) (Math.pow(1.1f, currentScrollPosition))/2;
                    }
                }else if (diff < 0 && currentScrollPosition >= mItemsCount-1){
                    if (getStackOrder() == OUT_STACK_ORDER) {
                        scale = (float) (Math.pow(1.1f,mItemsCount-1-currentScrollPosition))/2;
                    }else {
                        scale = (float) (Math.pow(1.1f,(mItemsCount-1-currentScrollPosition)))*2;
                    }
                }
            }

//            Log.d(getClass().getSimpleName(),"scale:"+scale+",currentScrollPosition:"+currentScrollPosition);
            resultScroll = (int) (diff*scale);
        }
//        Log.d(getClass().getSimpleName(),"resultScroll:"+resultScroll+",diff:"+diff+",mScrollOffset:"+mLayoutHelper.mScrollOffset);
        mLayoutHelper.mScrollOffset += resultScroll*getNumberOrder();
        fillData(recycler, state, false);
        return resultScroll;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        mDecoratedChildHeight = null;
        mDecoratedChildWidth = null;
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onAdapterChanged(final RecyclerView.Adapter oldAdapter, final RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);

        removeAllViews();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    @CallSuper
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            removeAndRecycleAllViews(recycler);
            selectItemCenterPosition(INVALID_POSITION);
            return;
        }

        boolean childMeasuringNeeded = false;
        if (null == mDecoratedChildWidth) {
            final View view = recycler.getViewForPosition(0);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(view);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(view);

            mBaseScale = 1;
            if (mViewPostLayout != null) {
                mBaseScale = mViewPostLayout.getBaseScale(this, getOrientation());
                mCenterViewStart = mViewPostLayout.getCenterViewStartOffset(this, getOrientation());
            }

            removeAndRecycleView(view, recycler);

            if (INVALID_POSITION == mPendingScrollPosition && null == mPendingStackCardSavedState) {
                mPendingScrollPosition = mCenterItemPosition;
            }

            childMeasuringNeeded = true;
        }

        if (INVALID_POSITION != mPendingScrollPosition) {
            final int itemsCount = state.getItemCount();
            mPendingScrollPosition = 0 == itemsCount ? INVALID_POSITION : Math.max(0, Math.min(itemsCount - 1, mPendingScrollPosition));
        }
        if (INVALID_POSITION != mPendingScrollPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingScrollPosition, state);
            mPendingScrollPosition = INVALID_POSITION;
            mPendingStackCardSavedState = null;
        } else if (null != mPendingStackCardSavedState) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingStackCardSavedState.mCenterItemPosition, state);
            mPendingStackCardSavedState = null;
        } else if (state.didStructureChange() && INVALID_POSITION != mCenterItemPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mCenterItemPosition, state);
        }

        fillData(recycler, state, childMeasuringNeeded);
    }

    private int calculateScrollForSelectingPosition(final int itemPosition, final RecyclerView.State state) {
        final int fixedItemPosition = itemPosition < state.getItemCount() ? itemPosition : state.getItemCount() - 1;
        return VERTICAL == mOrientation ? fixedItemPosition * mDecoratedChildHeight : fixedItemPosition * mDecoratedChildWidth;
    }

    private void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state, final boolean childMeasuringNeeded) {
//        Log.d(TAG,"fillData,"+"currentScrollPosition:"+currentScrollPosition);
        mItemsCount = state.getItemCount();
        final float currentScrollPosition = getCurrentScrollPosition();
//        Log.d(TAG,"mItemsCount:"+mItemsCount+",currentScrollPosition:"+currentScrollPosition);
        generateLayoutOrder(currentScrollPosition, state);
        removeAndRecycleUnusedViews(mLayoutHelper, recycler);

        detectOnItemSelectionChanged(currentScrollPosition, state);

        final int width = getWidthNoPadding();
        final int height = getHeightNoPadding();
        if (VERTICAL == mOrientation) {
            fillDataVertical(recycler, width, height, childMeasuringNeeded);
        } else {
            fillDataHorizontal(recycler, width, height, childMeasuringNeeded);
        }

        recycler.clear();

    }

    private void detectOnItemSelectionChanged(final float currentScrollPosition, final RecyclerView.State state) {
        final float absCurrentScrollPosition;
        if (mCircleLayout) {
            absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, state.getItemCount());
        }else {
            absCurrentScrollPosition = currentScrollPosition;
        }
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCenterItemPosition != centerItem) {
            mCenterItemPosition = centerItem;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    selectItemCenterPosition(centerItem);
                }
            });
        }
    }

    private void selectItemCenterPosition(final int centerItem) {
        for (final OnCenterItemSelectionListener onCenterItemSelectionListener : mOnCenterItemSelectionListeners) {
            onCenterItemSelectionListener.onCenterItemChanged(centerItem);
        }
    }

    private void fillDataVertical(final RecyclerView.Recycler recycler, final int width, final int height, final boolean childMeasuringNeeded) {
        final int start = (width - mDecoratedChildWidth) / 2;
        final int end = start + mDecoratedChildWidth;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int top = mCenterViewStart;
            final int bottom = top + mDecoratedChildHeight;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i, childMeasuringNeeded);
        }
    }

    private void fillDataHorizontal(final RecyclerView.Recycler recycler, final int width, final int height, final boolean childMeasuringNeeded) {
        final int top = (height - mDecoratedChildHeight) / 2;
        final int bottom = top + mDecoratedChildHeight;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int start = mCenterViewStart;
            final int end = start + mDecoratedChildWidth;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i, childMeasuringNeeded);
        }
    }

    private void removeAndRecycleUnusedViews(final LayoutHelper layoutHelper, final RecyclerView.Recycler recycler) {
        final List<View> viewsToRemove = new ArrayList<>();
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                viewsToRemove.add(child);
                continue;
            }
            final RecyclerView.LayoutParams recyclerViewLp = (RecyclerView.LayoutParams) lp;
            final int adapterPosition = recyclerViewLp.getViewAdapterPosition();
            if (recyclerViewLp.isItemRemoved() || !layoutHelper.hasAdapterPosition(adapterPosition)) {
                viewsToRemove.add(child);
            }
        }

        for (final View view : viewsToRemove) {
            removeAndRecycleView(view, recycler);
        }
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount);
        mIsRemoveLayout = true;
        if (mRemoveList == null) {
            mRemoveList = new ArrayList<>();
        }
        if (getStackOrder() == IN_STACK_ORDER) {
            mRemoveBelowPosition = positionStart - 1;
        }else {
            mRemoveBelowPosition = positionStart;
        }
//        Log.d(TAG,"onItemsRemoved:"+ mRemoveBelowPosition);
    }

    private void fillChildItem(final int start, final int top, final int end, final int bottom, @NonNull final LayoutOrder layoutOrder,
                               @NonNull final RecyclerView.Recycler recycler, final int i, final boolean childMeasuringNeeded) {
        final View view = bindChild(layoutOrder.mItemAdapterPosition, recycler, childMeasuringNeeded);
//        Log.d(TAG,"mItemAdapterPosition:"+layoutOrder.mItemAdapterPosition+",itemCount:"+mItemsCount);

        ViewCompat.setElevation(view, i);

        ItemTransformation transformation = null;
        if (null != mViewPostLayout) {
            transformation = mViewPostLayout.transformChild(this, layoutOrder.mItemPositionDiff, mOrientation);
        }
        if (null == transformation) {
            view.layout(start, top, end, bottom);
        } else {
            float scaleX = transformation.mScaleX* mBaseScale;
            float scaleY = transformation.mScaleY* mBaseScale;

            int viewWidth = view.getMeasuredWidth();
            int viewHeight = view.getMeasuredHeight();
            if (viewWidth <= 0 || viewHeight <= 0) {
                viewWidth = getDecoratedChildWidth();
                viewHeight = getDecoratedChildHeight();
            }
            if (getOrientation() == VERTICAL) {
                if (getStackOrder()*getNumberOrder() < 0) {
                    ViewCompat.setPivotX(view,view.getMeasuredWidth()/2);
                    ViewCompat.setPivotY(view,view.getMeasuredHeight());
                }else {
                    ViewCompat.setPivotX(view, view.getMeasuredWidth()/2);
                    ViewCompat.setPivotY(view, 0);
                }
            }else {
                if (getStackOrder() * getNumberOrder() < 0) {
                    ViewCompat.setPivotX(view,view.getMeasuredWidth());
                    ViewCompat.setPivotY(view,view.getMeasuredHeight()/2);
                }else {
                    ViewCompat.setPivotX(view,0);
                    ViewCompat.setPivotY(view,view.getMeasuredHeight()/2);
                }
            }
            ViewCompat.setScaleX(view, scaleX);
            ViewCompat.setScaleY(view, scaleY);

//            Log.d(TAG, "center:"+getCenterItemPosition()+",itemcount:"+mItemsCount);

            /**
             * below the {@link Build.VERSION_CODES.LOLLIPOP} view.setClipBounds(rect) is not work
             * so we needn't Calculation the rect
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Rect rect = getRect(viewWidth,viewHeight,layoutOrder.mItemAdapterPosition,view,transformation.mClipLength);
                if (mIsRemoveLayout) {
                    fixRemoved(viewWidth,viewHeight,layoutOrder,view,transformation,rect);
                }else {
                    ViewCompat.setClipBounds(view, rect);
                }
            }

            int lastPosition = getCenterItemPosition()-getStackOrder()*(getLayoutCount()-(getLayoutCount()/2-1));
            final float alpha = transformation.mAlpha;
            /**
             * when remove item, the last will add, if you set view alpha when the view is not init finish, the alpha is not work,
             * so need delay it.
             */
            boolean needFixAlpha = mIsRemoveLayout && alpha<1 && ((getStackOrder() == OUT_STACK_ORDER) || (getStackOrder() == IN_STACK_ORDER
                    && getCenterItemPosition() == mItemsCount-1)) && layoutOrder.mItemAdapterPosition == lastPosition;
            if (needFixAlpha) {
                view.setVisibility(View.INVISIBLE);
                long delay = mRecyclerView.getItemAnimator().getMoveDuration()+200;
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.VISIBLE);
                        ViewCompat.setAlpha(view, alpha);
                    }
                }, delay);
            }else{
                ViewCompat.setAlpha(view, alpha);
            }

            int translationX = transformation.mTranslationX*getStackOrder()*getNumberOrder();
            int translationY = transformation.mTranslationY*getStackOrder()*getNumberOrder();
            view.layout((start + translationX), (top + translationY), (end + translationX), (bottom + translationY));

        }
    }

    private Rect getRect(int viewWidth, int viewHeight, int adapterPosition, View view, int clipLength) {
        Rect rect = ViewCompat.getClipBounds(view);
        if (rect == null) {
            rect = new Rect();
        }
        if (getOrientation() == VERTICAL) {
            int height = clipLength;
            if (getStackOrder()*getNumberOrder() < 0) {
                rect.set(0, viewHeight-height, viewWidth, viewHeight);
            }else {
                rect.set(0,0,viewWidth,height);
            }
        }else {
            int width = clipLength;
            if (getStackOrder() * getNumberOrder() < 0) {
                rect.set(viewWidth-width,0,viewWidth,viewHeight);
            }else {
                rect.set(0,0,width,viewHeight);
            }
        }
        if (clipLength < 0) {
            rect.set(0, 0, viewWidth, viewHeight);
        }
        if (!mCircleLayout) {
            /**
             * in the top don't need clip rect
             */
            boolean needFixClip = ((getStackOrder() == IN_STACK_ORDER && adapterPosition == getItemCount()-1) ||
                    (getStackOrder() == OUT_STACK_ORDER && adapterPosition == 0));
            if (needFixClip) {
                rect.set(0, 0, viewWidth, viewHeight);
            }
        }
        return rect;
    }

    private void fixRemoved(int viewWidth, int viewHeight, @NonNull LayoutOrder layoutOrder,final View view,
                            ItemTransformation transformation, Rect rect) {
        RemoveBean bean = null;
        boolean needfixRemove = (getStackOrder() == IN_STACK_ORDER && mRemoveBelowPosition <= layoutOrder.mItemAdapterPosition)
                || (getStackOrder() == OUT_STACK_ORDER && (mRemoveBelowPosition == layoutOrder.mItemAdapterPosition
                || mRemoveBelowPosition == mItemsCount));
        if (needfixRemove) {
            Rect beforeRect = ViewCompat.getClipBounds(view);
//            Log.d(TAG,"position:"+layoutOrder.mItemAdapterPosition+",beforeRect:"+beforeRect);
            if (getOrientation() == VERTICAL) {
                int height = Math.round(beforeRect.height()/transformation.mScaleY);
                if (getNumberOrder()*getStackOrder() > 0) {
                    beforeRect.set(0 ,0 ,viewWidth, height);
                }else {
                    beforeRect.set(0, viewHeight-height, viewWidth,viewHeight);
                }
            }else {
                int width = Math.round(beforeRect.width()/transformation.mScaleX);
                if (getNumberOrder()*getStackOrder() > 0) {
                    beforeRect.set(0 ,0 ,width, viewHeight);
                }else {
                    beforeRect.set(viewWidth-width, 0, viewWidth,viewHeight);
                }
            }
            bean = new RemoveBean(layoutOrder.mItemAdapterPosition, rect);
            ViewCompat.setClipBounds(view, beforeRect);
        }else {
            ViewCompat.setClipBounds(view, rect);
//            Log.d(TAG,"position:"+layoutOrder.mItemAdapterPosition+",rect:"+rect);
        }
        if (bean != null) {
            mRemoveList.add(bean);
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);

        if (mIsRemoveLayout) {
            mIsRemoveLayout = false;
            long delay = mRecyclerView.getItemAnimator().getMoveDuration();
//            Log.d(TAG,"onLayoutCompleted,delay:"+delay);
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRemoveList != null && mRemoveList.size() > 0) {
                        for (RemoveBean bean : mRemoveList) {
//                            Log.d(TAG,"onLayoutCompleted,position:"+bean.position+",rect:"+bean.clipRect);
                            View view = findViewByPosition(bean.position);
                            ViewCompat.setClipBounds(view, bean.clipRect);
                        }
                        mRemoveList.clear();
                    }
                }
            }, delay);
        }
//        Log.d(TAG,"onLayoutCompleted");
    }

    /**
     * get the item count while need layout
     * @return
     */
    public int getLayoutCount() {
        return Math.min(mLayoutHelper.mMaxVisibleItems * 2 + 3, mItemsCount);
    }

    /**
     * @return current scroll position of center item. this value can be in any range if it is cycle layout.
     * if this is not, that then it is in [0, {@link #mItemsCount - 1}]
     */
    private float getCurrentScrollPosition() {
        final int fullScrollSize = getMaxScrollOffset();
        if (0 == fullScrollSize) {
            return 0;
        }
        return 1.0f * mLayoutHelper.mScrollOffset / getScrollItemSize();
    }

    /**
     * @return maximum scroll value to fill up all items in layout. Generally this is only needed for non cycle layouts.
     */
    private int getMaxScrollOffset() {
        return getScrollItemSize() * (mItemsCount - 1);
    }

    /**
     * Because we can support old Android versions, we should layout our children in specific order to make our center view in the top of layout
     * (this item should layout last). So this method will calculate layout order and fill up {@link #mLayoutHelper} object.
     * This object will be filled by only needed to layout items. Non visible items will not be there.
     *
     * @param currentScrollPosition current scroll position this is a value that indicates position of center item
     *                              (if this value is int, then center item is really in the center of the layout, else it is near state).
     *                              Be aware that this value can be in any range is it is cycle layout
     * @param state                 Transient state of RecyclerView
     * @see #getCurrentScrollPosition()
     */
    protected void generateLayoutOrder(final float currentScrollPosition, @NonNull final RecyclerView.State state) {
        mItemsCount = state.getItemCount();
        float absCurrentScrollPosition;
        if (mCircleLayout) {
            absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, mItemsCount);
        }else {

            /**when out of range(0,mItemsCount-1) make it slow*/
            /*if (currentScrollPosition < 0 ) {
                //the min is never small than -1.2  function y= 1.2*(1.1^x)-1.2 when x < 0
                absCurrentScrollPosition = (float) (1.2f*Math.pow(1.1f,currentScrollPosition)-1.2);
            }else if (currentScrollPosition > mItemsCount-1){
                //the max is never big than mItemsCount+1.7 function y= -2.7*((1.1^(mItemsCount-1-x)-1) when x > mItemsCount-1
                absCurrentScrollPosition = mItemsCount-1+(float) (-2.7f*Math.pow(1.1f,mItemsCount-1-currentScrollPosition)+2.7);
            }else {
            }*/
            absCurrentScrollPosition = currentScrollPosition;
        }
//        Log.d(TAG,"generateLayoutOrder,"+"absCurrentScrollPosition:"+absCurrentScrollPosition+",currentScrollPosition:"+currentScrollPosition);
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCircleLayout && 1 < mItemsCount) {
            final int layoutCount = getLayoutCount();// + 3 = 1 (center item) + 2 (addition bellow maxVisibleItems)

            mLayoutHelper.initLayoutOrder(layoutCount);

            final int countLayoutCenter = layoutCount / 2 + getStackOrder()*2;
            if (getStackOrder() == OUT_STACK_ORDER) {
                for (int i = 1; i <= layoutCount; ++i) {
                    final int position = Math.round( centerItem + i-1 - countLayoutCenter + mItemsCount) % mItemsCount;
                    mLayoutHelper.setLayoutOrder(layoutCount-i, position, absCurrentScrollPosition-((centerItem+i - 1)-countLayoutCenter));
                }
            }else {
                for (int i = 1; i <= layoutCount; ++i) {
                    final int position = Math.round( centerItem + i-1 - countLayoutCenter + mItemsCount) % mItemsCount;
                    mLayoutHelper.setLayoutOrder(i-1, position, (centerItem + (i-1-absCurrentScrollPosition) -countLayoutCenter));
                }
            }

        } else {

            if (getStackOrder() == OUT_STACK_ORDER) {
                final int firstVisible= Math.max(centerItem -(mLayoutHelper.mMaxVisibleItems - 1), 0);
                final int lastVisible = Math.min(centerItem +(mLayoutHelper.mMaxVisibleItems + 3), mItemsCount - 1);
                final int layoutCount = Math.abs(lastVisible - firstVisible) + 1;

                mLayoutHelper.initLayoutOrder(layoutCount);

                for (int i = firstVisible; i <= lastVisible; ++i) {
                    /**mMaxVisibleItems =3 the (itemPositionDiff,itemPositionDiff) is range (-2,6) */
                    mLayoutHelper.setLayoutOrder(lastVisible - i, i, (absCurrentScrollPosition-i));
                }
            }else {
                final int firstVisible = Math.max(centerItem - (mLayoutHelper.mMaxVisibleItems + 3), 0);
                final int lastVisible = Math.min(centerItem + (mLayoutHelper.mMaxVisibleItems - 1), mItemsCount - 1);
                final int layoutCount = Math.abs(lastVisible - firstVisible) + 1;

                mLayoutHelper.initLayoutOrder(layoutCount);

                for (int i = firstVisible; i <= lastVisible; ++i) {
                    /**mMaxVisibleItems =3 the (itemPositionDiff,itemPositionDiff) is range (-6,2) */
                    mLayoutHelper.setLayoutOrder(i - firstVisible, i, (i - absCurrentScrollPosition));
                }
            }

        }
    }

    public int getWidthNoPadding() {
        return getWidth() - getPaddingStart() - getPaddingEnd();
    }

    public int getHeightNoPadding() {
        return getHeight() - getPaddingEnd() - getPaddingStart();
    }

    private View bindChild(final int position, @NonNull final RecyclerView.Recycler recycler, final boolean childMeasuringNeeded) {
//        Log.d(TAG,"bindChild");
        final View view = findViewForPosition(recycler, position);

        if (null == view.getParent()) {
            addView(view);
            measureChildWithMargins(view, 0, 0);
        } else {
            detachView(view);
            attachView(view);
            if (childMeasuringNeeded) {
                measureChildWithMargins(view, 0, 0);
            }
        }
        return view;
    }

    private View findViewForPosition(final RecyclerView.Recycler recycler, final int position) {
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                continue;
            }
            final RecyclerView.LayoutParams recyclerLp = (RecyclerView.LayoutParams) lp;
            final int adapterPosition = recyclerLp.getViewAdapterPosition();
            if (adapterPosition == position) {
                if (recyclerLp.isItemChanged()) {
                    recycler.bindViewToPosition(child, position);
                    measureChildWithMargins(child, 0, 0);
                }
                return child;
            }
        }
        return recycler.getViewForPosition(position);
    }

    /**
     * is the first child width
     * @return  mDecoratedChildWidth
     */
    public Integer getDecoratedChildWidth() {
        return mDecoratedChildWidth;
    }

    /**
     * is the first child width
     * @param mDecoratedChildWidth
     */
    public void setDecoratedChildWidth(Integer mDecoratedChildWidth) {
        this.mDecoratedChildWidth = mDecoratedChildWidth;
    }

    /**
     * is the first child height
     * @return  mDecoratedChildHeight
     */
    public Integer getDecoratedChildHeight() {
        return mDecoratedChildHeight;
    }

    /**
     * is the first child height
     * @param mDecoratedChildHeight
     */
    public void setDecoratedChildHeight(Integer mDecoratedChildHeight) {
        this.mDecoratedChildHeight = mDecoratedChildHeight;
    }

    /**
     * @return full item size
     */
    protected int getScrollItemSize() {
        if (VERTICAL == mOrientation) {
            return mDecoratedChildHeight;
        } else {
            return mDecoratedChildWidth;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (null != mPendingStackCardSavedState) {
            return new StackCardSavedState(mPendingStackCardSavedState);
        }
        final StackCardSavedState savedState = new StackCardSavedState(super.onSaveInstanceState());
        savedState.mCenterItemPosition = mCenterItemPosition;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof StackCardSavedState) {
            mPendingStackCardSavedState = (StackCardSavedState) state;
            super.onRestoreInstanceState(mPendingStackCardSavedState.mSuperState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * @return Scroll offset from nearest item from center
     */
    int getOffsetCenterView() {
        return Math.round(getCurrentScrollPosition()) * getScrollItemSize() - mLayoutHelper.mScrollOffset;
    }

    /**
     * the {@link RecyclerView#smoothScrollToPosition(int)} will call
     * {@link StackCardLayoutManager#smoothScrollToPosition(RecyclerView, RecyclerView.State, int)} ,and in this method will startScroll
     * {@link android.support.v7.widget.RecyclerView.LayoutManager#startSmoothScroll(RecyclerView.SmoothScroller)} ,then will calculate distance in
     * {@link StackCardSmoothScroller#calculateDxToMakeVisible(View, int)} or {@link StackCardSmoothScroller#calculateDyToMakeVisible(View, int)}
     * ,finally will call this
     * @param view the position of child view
     * @return the distance of current view for position view
     */
    int getOffsetForCurrentView(@NonNull final View view) {
        final int position = getPosition(view);
//        Log.d(TAG,"getOffsetForCurrentView,position:"+position);
        final int fullCircles = mLayoutHelper.mScrollOffset / (mItemsCount * getScrollItemSize());
        int fullOffset = fullCircles * mItemsCount * getScrollItemSize();
        if (0 > mLayoutHelper.mScrollOffset) {
            fullOffset -= 1;
        }
//        Log.d(TAG,"getOffsetForCurrentView,fullOffset:"+fullOffset);
        int offset;
        if (0 == fullOffset || 0 < Math.signum(fullOffset)) {
            offset =  mLayoutHelper.mScrollOffset - position * getScrollItemSize() - fullOffset;
        } else {
            offset =  mLayoutHelper.mScrollOffset + position * getScrollItemSize() - fullOffset;
        }
//        Log.d(TAG,"getOffsetForCurrentView,offset:"+offset);
        return offset*getNumberOrder();
    }

    /**
     * Helper method that make scroll in range of [0, count). Generally this method is needed only for cycle layout.
     *
     * @param currentScrollPosition any scroll position range.
     * @param count                 adapter items count
     * @return good scroll position in range of [0, count)
     */
    private static float makeScrollPositionInRange0ToCount(final float currentScrollPosition, final int count) {
        float absCurrentScrollPosition = currentScrollPosition;
        while (0 > absCurrentScrollPosition) {
            absCurrentScrollPosition += count;
        }
        while (Math.round(absCurrentScrollPosition) >= count) {
            absCurrentScrollPosition -= count;
        }
        return absCurrentScrollPosition;
    }

    private static class RemoveBean {
        int position;
        Rect clipRect;

        public RemoveBean(int position, Rect clipRect) {
            this.position = position;
            this.clipRect = clipRect;
        }
    }

    /**
     * This interface is layout view ,if you want layout view in your way you should implements the interface <br />
     * <br />
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.
     */
    public interface IPostLayout {

        /**
         * get the centerView offset parent top, it call on {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)} method
         * when {@link #mDecoratedChildWidth} == null , in the {@link #onMeasure(RecyclerView.Recycler, RecyclerView.State, int, int)}
         * method the {@link #mDecoratedChildWidth} set null, so the method is difference from {@link #transformChild(StackCardLayoutManager, float, int)},
         * when init finish this never call again, so didn't put some params need always change when scrooling, you should put it in
         *{@link #transformChild(StackCardLayoutManager, float, int)}
         *
         * @param layoutManager         this
         * @param orientation           layoutManager orientation {@link RecyclerView#getLayoutDirection()}
         * @return                      the distance centerView from parent top
         */
        float getBaseScale(@NonNull StackCardLayoutManager layoutManager, int orientation);

        /**
         * get the offset between centerView and parent top, it call on {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)} method
         * when {@link #mDecoratedChildWidth} == null , in the {@link #onMeasure(RecyclerView.Recycler, RecyclerView.State, int, int)}
         * method the {@link #mDecoratedChildWidth} set null, so the method is difference from {@link #transformChild(StackCardLayoutManager, float, int)},
         * when init finish this never call again, so didn't put some params need always change when scrooling, you should put it in
         *{@link #transformChild(StackCardLayoutManager, float, int)}
         *
         * @param layoutManager         this
         * @param orientation           layoutManager orientation {@link RecyclerView#getLayoutDirection()}
         * @return                      the distance centerView from parent top
         */
        int getCenterViewStartOffset(@NonNull StackCardLayoutManager layoutManager, int orientation);

        /**
         * Called after child layout finished. Generally you can do any translation and scaling work here.
         * it call on {@link #fillChildItem(int, int, int, int, LayoutOrder, RecyclerView.Recycler, int, boolean)} method
         * the method is difference from {@link #getBaseScale(StackCardLayoutManager, int)} and
         * {@link #getCenterViewStartOffset(StackCardLayoutManager, int)}, it always call when you are scrolling
         * init finish this never call again, so didn't put some params need always change when scrooling, you should put it in
         * {@link #transformChild(StackCardLayoutManager, float, int)}
         *
         * @param layoutManager            this
         * @param itemPositionToCenterDiff view center line difference to layout center. if > 0 then this item is bellow layout center line, else if not
         * @param orientation              layoutManager orientation {@link RecyclerView#getLayoutDirection()}
         */
        ItemTransformation transformChild(@NonNull final StackCardLayoutManager layoutManager, final float itemPositionToCenterDiff, final int orientation);
    }

    public interface OnCenterItemSelectionListener {

        /**
         * Listener that will be called on every change of center item.
         * This listener will be triggered on <b>every</b> layout operation if item was changed.
         * Do not do any expensive operations in this method since this will effect scroll experience.
         *
         * @param adapterPosition current layout center item
         */
        void onCenterItemChanged(final int adapterPosition);
    }

    /**
     * Helper class that holds currently visible items.
     * Generally this class fills this list. <br />
     * <br />
     * This class holds all scroll and maxVisible items state.
     *
     * @see #getMaxVisibleItems()
     */
    private static class LayoutHelper {

        private int mMaxVisibleItems;

        private int mScrollOffset;

        private LayoutOrder[] mLayoutOrder;

        private final List<WeakReference<LayoutOrder>> mReusedItems = new ArrayList<>();

        LayoutHelper(final int maxVisibleItems) {
            mMaxVisibleItems = maxVisibleItems;
        }

        /**
         * Called before any fill calls. Needed to recycle old items and init new array list. Generally this list is an array an it is reused.
         *
         * @param layoutCount items count that will be layout
         */
        void initLayoutOrder(final int layoutCount) {
            if (null == mLayoutOrder || mLayoutOrder.length != layoutCount) {
                if (null != mLayoutOrder) {
                    recycleItems(mLayoutOrder);
                }
                mLayoutOrder = new LayoutOrder[layoutCount];
                fillLayoutOrder();
            }
        }

        /**
         * Called during layout generation process of filling this list. Should be called only after {@link #initLayoutOrder(int)} method call.
         *
         * @param arrayPosition       position in layout order
         * @param itemAdapterPosition adapter position of item for future data filling logic
         * @param itemPositionDiff    difference of current item scroll position and center item position.
         *                            if this is a center item and it is in real center of layout, then this will be 0.
         *                            if current layout is not in the center, then this value will never be int.
         *                            if this item center is bellow layout center line then this value is greater then 0,
         *                            else less then 0.
         */
        void setLayoutOrder(final int arrayPosition, final int itemAdapterPosition, final float itemPositionDiff) {
            final LayoutOrder item = mLayoutOrder[arrayPosition];
            item.mItemAdapterPosition = itemAdapterPosition;
            item.mItemPositionDiff = itemPositionDiff;
        }

        /**
         * Checks is this screen Layout has this adapterPosition view in layout
         *
         * @param adapterPosition adapter position of item for future data filling logic
         * @return true is adapterItem is in layout
         */
        boolean hasAdapterPosition(final int adapterPosition) {
            if (null != mLayoutOrder) {
                for (final LayoutOrder layoutOrder : mLayoutOrder) {
                    if (layoutOrder.mItemAdapterPosition == adapterPosition) {
                        return true;
                    }
                }
            }
            return false;
        }

        @SuppressWarnings("VariableArgumentMethod")
        private void recycleItems(@NonNull final LayoutOrder... layoutOrders) {
            for (final LayoutOrder layoutOrder : layoutOrders) {
                //noinspection ObjectAllocationInLoop
                mReusedItems.add(new WeakReference<>(layoutOrder));
            }
        }

        private void fillLayoutOrder() {
            for (int i = 0; i < mLayoutOrder.length; ++i) {
                if (null == mLayoutOrder[i]) {
                    mLayoutOrder[i] = createLayoutOrder();
                }
            }
        }

        private LayoutOrder createLayoutOrder() {
            final Iterator<WeakReference<LayoutOrder>> iterator = mReusedItems.iterator();
            while (iterator.hasNext()) {
                final WeakReference<LayoutOrder> layoutOrderWeakReference = iterator.next();
                final LayoutOrder layoutOrder = layoutOrderWeakReference.get();
                iterator.remove();
                if (null != layoutOrder) {
                    return layoutOrder;
                }
            }
            return new LayoutOrder();
        }
    }

    /**
     * Class that holds item data.
     * This class is filled during {@link #generateLayoutOrder(float, RecyclerView.State)} and used during {@link #fillData(RecyclerView.Recycler, RecyclerView.State, boolean)}
     */
    private static class LayoutOrder {

        /**
         * Item adapter position
         */
        private int mItemAdapterPosition;
        /**
         * Item center difference to layout center. If center of item is bellow layout center, then this value is greater then 0, else it is less.
         */
        private float mItemPositionDiff;
    }

    protected static class StackCardSavedState implements Parcelable {

        private final Parcelable mSuperState;
        private int mCenterItemPosition;

        protected StackCardSavedState(@Nullable final Parcelable superState) {
            mSuperState = superState;
        }

        private StackCardSavedState(@NonNull final Parcel in) {
            mSuperState = in.readParcelable(Parcelable.class.getClassLoader());
            mCenterItemPosition = in.readInt();
        }

        protected StackCardSavedState(@NonNull final StackCardSavedState other) {
            mSuperState = other.mSuperState;
            mCenterItemPosition = other.mCenterItemPosition;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(mSuperState, flags);
            dest.writeInt(mCenterItemPosition);
        }

        public static final Creator<StackCardSavedState> CREATOR
                = new Creator<StackCardSavedState>() {
            @Override
            public StackCardSavedState createFromParcel(final Parcel source) {
                return new StackCardSavedState(source);
            }

            @Override
            public StackCardSavedState[] newArray(final int size) {
                return new StackCardSavedState[size];
            }
        };
    }
}