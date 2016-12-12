package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Implementation of {@link StackCardLayoutManager.IPostLayout} that makes interesting scaling of items. <br />
 */
public class StackCardPostLayout implements StackCardLayoutManager.IPostLayout {

    private boolean isLessType = false;

    private int smallDistance;
    private int mediumDistance;
    private int bigDistance;


    public int getCenterViewOffset(@NonNull StackCardLayoutManager layoutManager, int length) {
        int mediumCount = isLessType ? 1 : 2;

        /**
         * need show 3.5 SmallDistance item and (isLessType ? 1 : 2) MediumDistance item
         */
        float centerStartRatio = mediumCount / getMediumDistanceRatio() + 3.5f / getSmallDistanceRatio();
        int centerViewStart = (int) (length*centerStartRatio);
        if (layoutManager.getStackOrder() * layoutManager.getNumberOrder() < 0) {
            centerViewStart = length-layoutManager.getScrollItemSize()-centerViewStart;
        }

        smallDistance = Math.round(length/getSmallDistanceRatio()/layoutManager.getBaseScale());
        mediumDistance = Math.round(length/getMediumDistanceRatio()/layoutManager.getBaseScale());
        bigDistance = Math.round(length/getBigDistanceRatio()/layoutManager.getBaseScale());
        return centerViewStart;
    }


    /**
     *
     * make sure the childview is smaller than parent
     *
     * @param layoutManager         this
     * @param orientation           layoutManager orientation {@link android.support.v7.widget.RecyclerView#getLayoutDirection()}
     * @return
     */
    @Override
    public float getBaseScale(@NonNull StackCardLayoutManager layoutManager, int orientation) {
        Log.d(getClass().getSimpleName(),"getBaseScale");
        int parentHeight = layoutManager.getHeightNoPadding();
        int parentWidth = layoutManager.getWidthNoPadding();
        float baseScale = 1;
        int mDecoratedChildWidth = layoutManager.getDecoratedChildWidth();
        int mDecoratedChildHeight = layoutManager.getDecoratedChildHeight();

        /**
         * it is auto scale child view when it is bigger than parent
         */
        if(orientation == StackCardLayoutManager.VERTICAL && parentHeight < parentWidth) {
            if (parentHeight < mDecoratedChildWidth) {
                baseScale = 1.0f*parentHeight/mDecoratedChildWidth;
            }
            layoutManager.setMaxVisibleItems(2);
            isLessType = true;
        }else if(orientation == StackCardLayoutManager.HORIZONTAL) {
            if (parentHeight < mDecoratedChildHeight) {
                baseScale = 1.0f*parentHeight/mDecoratedChildHeight;
            }
            if (parentWidth < parentHeight) {
                layoutManager.setMaxVisibleItems(2);
                isLessType = true;
            }
        }
        return baseScale;
    }

    @Override
    public int getCenterViewStartOffset(@NonNull StackCardLayoutManager layoutManager, int orientation) {
        Log.d(getClass().getSimpleName(),"getCenterViewStartOffset");
        int length ;
        if (orientation == StackCardLayoutManager.VERTICAL) {
            length = layoutManager.getHeightNoPadding();
        }else {
            length = layoutManager.getWidthNoPadding();
        }
        return getCenterViewOffset(layoutManager, length);
    }

    @Override
    public ItemTransformation transformChild(@NonNull final StackCardLayoutManager layoutManager, final float itemPositionToCenterDiff, final int orientation) {
        Log.d(getClass().getSimpleName(),"transformChild");
        int base=40;
        float itemDiff = Math.abs(itemPositionToCenterDiff-2.5f);
        final float scale = Math.min((base-itemDiff)/base, 1);
//        Log.d("StackCardZoomPostLayoutListener","itemPositionToCenterDiff:"+itemPositionToCenterDiff);
        int translateY;
        int translateX;
        int height;
        int width;
        width = layoutManager.getWidthNoPadding();
        height = layoutManager.getHeightNoPadding();
//        Log.d("transformChild","height:"+height);
        int changePosition = isLessType ? 1 : 2;
        float ratio;
        int clipLength;

        /**
         * use the x instead of itemPositionToCenterDiff ,y instead of ratio
         * isLessType == false  changePosition = 2 the x range(-6,2)
         *  1.when (x <= -2) i need it closeest each other so derivatives y' = 1/25 , y = (x+a)/25
         * when it close to -2 the y need close to -2/6 ,because when (x > -2 && x < 0) the function is y = x/6,
         * the x close to -2 the y is close to -2/6, so it must successive , finial in (x <= -2) the function is y = (x+2)/25-2/6
         *
         *  2.when (x > -2 && x < 0) i need it closer each other so derivatives y' = 1/6 , y = (x+a)/6
         * when it close to 0 the y need close to 0,  the function is y = x/6
         *
         *  3.when ( x > 0) i need it closer each other so derivatives y' = 1/2 , y = (x+a)/2
         *  the function is y = x/2
         *
         *  isLessType == true  changePosition = 1 the x range(-5,1)
         *  there is different at moreType the MediumDistanceRatio only one item
         *
         */
        /*if(itemPositionToCenterDiff > 0) {
            clipLength = bigDistance;
        }else if (itemPositionToCenterDiff > -1&& itemPositionToCenterDiff < 0) {
            clipLength = (int) ((itemPositionToCenterDiff+1)*height);
        }else if (itemPositionToCenterDiff < -1 && itemPositionToCenterDiff > -2) {
            clipLength = mediumDistance;
        }else if (itemPositionToCenterDiff < -2 && itemPositionToCenterDiff >-3){
            clipLength = (int) ((itemPositionToCenterDiff+3)*height);
        }else if (itemPositionToCenterDiff < -3) {
            clipLength = smallDistance;
        }*/
        if(itemPositionToCenterDiff <= -changePosition-1) {
            clipLength = smallDistance;
        }else if(itemPositionToCenterDiff <= -1 && itemPositionToCenterDiff > -changePosition-1) {
            clipLength = mediumDistance;
        }else {
            clipLength = bigDistance;
        }
        if (itemPositionToCenterDiff <= -changePosition) {
            ratio = ((itemPositionToCenterDiff+changePosition) / getSmallDistanceRatio()
                    -changePosition/getMediumDistanceRatio());
        }else if (itemPositionToCenterDiff <= 0) {
            ratio = itemPositionToCenterDiff / getMediumDistanceRatio();
        }else {
            ratio = itemPositionToCenterDiff / getBigDistanceRatio();
        }
        if (StackCardLayoutManager.VERTICAL == orientation) {
            translateY = Math.round(height * ratio);
            translateX = 0;
        } else {
            translateX = Math.round(width * ratio);
            translateY = 0;
        }
        float alpha = 1;
        if(itemPositionToCenterDiff > -6 && itemPositionToCenterDiff < -2) {
            alpha = itemPositionToCenterDiff/4f+3f/2;
        }else if(itemPositionToCenterDiff <= -6){
            alpha = 0;
        }

        clipLength = Math.round(clipLength/scale);
        /*if (isLessType && itemPositionToCenterDiff >= 1) {
            clipLength = -1;
        }*/
        Log.d("StackCardPostLayout", "itemPositionToCenterDiff:"+itemPositionToCenterDiff+",alpha:"+alpha);
        return new ItemTransformation(scale, scale, translateX, translateY, clipLength, alpha);
    }

    public float getSmallDistanceRatio() {
        if (isLessType) {
            return 25f;
        }else {
            return 35f;
        }
    }

    public float getMediumDistanceRatio() {
        if (isLessType) {
            return 6f;
        }else {
            return 12f;
        }
    }

    public float getBigDistanceRatio() {
        if (isLessType) {
            return 2f;
        }else {
            return 2.5f;
        }
    }
}