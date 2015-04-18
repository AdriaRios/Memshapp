package org.adriarios.memshapp.customComponents;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian on 18/04/2015.
 */
public class ScrollViewCustom extends ScrollView {

    List<View> mInterceptScrollViews = new ArrayList<View>();

    public ScrollViewCustom(Context context) {
        super(context);
    }

    public ScrollViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewCustom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addInterceptScrollView(View view) {
        mInterceptScrollViews.add(view);
    }

    public void removeInterceptScrollView(View view) {
        mInterceptScrollViews.remove(view);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        // check if we have any views that should use their own scrolling
        if (mInterceptScrollViews.size() > 0) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Rect bounds = new Rect();

            for (View view : mInterceptScrollViews) {
                view.getHitRect(bounds);
                if (bounds.contains(x, y)) {
                    //were touching a view that should intercept scrolling
                    return false;
                }
            }
        }

        return super.onInterceptTouchEvent(event);
    }
}
