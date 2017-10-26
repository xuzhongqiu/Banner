package com.huawei.ott.composite.banner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


public class AdsPreviewLayout extends LinearLayout
{
    private Context fContext;


    public AdsPreviewLayout(Context context)
    {
        this(context, null);
    }

    public AdsPreviewLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        fContext = context;
        init();
    }

    private void init()
    {
        LayoutInflater.from(fContext).inflate(R.layout.ads_preview_layout, this, true);
    }
}
