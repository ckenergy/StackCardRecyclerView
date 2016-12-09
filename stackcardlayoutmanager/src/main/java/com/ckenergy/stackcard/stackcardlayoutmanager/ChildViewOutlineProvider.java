package com.ckenergy.stackcard.stackcardlayoutmanager;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by chengkai on 2016/12/9.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ChildViewOutlineProvider extends ViewOutlineProvider {
    @Override
    public void getOutline(View view, Outline outline) {
        Log.d(getClass().getSimpleName(),"width:"+view.getWidth()+",height:"+view.getHeight());
        outline.setRect(view.getLeft(),view.getTop(),view.getLeft()+view.getWidth()/2,view.getTop()+view.getHeight()/2);
    }
}
