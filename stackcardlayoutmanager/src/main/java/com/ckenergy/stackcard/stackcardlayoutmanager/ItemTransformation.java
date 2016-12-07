package com.ckenergy.stackcard.stackcardlayoutmanager;

public class ItemTransformation {

    final float mScaleX;
    final float mScaleY;
    final int mTranslationX;
    final int mTranslationY;
    final float mAlpha;


    public ItemTransformation(final float scaleX, final float scaleY, final int translationX, final int translationY, final float alpha) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mTranslationX = translationX;
        mTranslationY = translationY;
        mAlpha = alpha;
    }
}