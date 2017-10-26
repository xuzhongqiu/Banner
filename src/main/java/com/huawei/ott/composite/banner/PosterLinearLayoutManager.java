package com.huawei.ott.composite.banner;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

/**
 */
public class PosterLinearLayoutManager extends LinearLayoutManager
{
    private float MILLISECONDS_PER_INCH = 0.03f;
    private View previousHighLightedView;

    public PosterLinearLayoutManager(Context context)
    {
        super(context);
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int
            position)
    {
        super.smoothScrollToPosition(recyclerView, state, position);

        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView
                .getContext())
        {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition)
            {
                return PosterLinearLayoutManager.this.computeScrollVectorForPosition
                        (targetPosition);
            }

            //This returns the milliseconds it takes to
            //scroll one pixel.
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics)
            {
                return MILLISECONDS_PER_INCH / displayMetrics.density;
            }

        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public void scrollToPosition(final int position)
    {
        super.scrollToPosition(position);


        if (null != previousHighLightedView)
        {
            previousHighLightedView.findViewById(R.id.poster_selected_image).setVisibility(View
                    .INVISIBLE);
        }

        View childAt = findViewByPosition(position);

        if (childAt == null)
        {
            return;
        }
        childAt.findViewById(R.id.poster_selected_image).setVisibility(View.VISIBLE);

        previousHighLightedView = childAt;
    }
}
