package com.ckenergy.stackcard.stackcardlayoutmanager;

public class ItemTransformation {

    final float mScaleX;
    final float mScaleY;
    final int mTranslationX;
    final int mTranslationY;
    final float mAlpha;
    final int mClipLength;


    public ItemTransformation(final float scaleX, final float scaleY, final int translationX, final int translationY,final int clipLength, final float alpha) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mTranslationX = translationX;
        mTranslationY = translationY;
        mClipLength = clipLength;
        mAlpha = alpha;
    }
}