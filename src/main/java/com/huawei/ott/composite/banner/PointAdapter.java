package com.huawei.ott.composite.banner;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huawei.ott.gadget.extview.ImageViewExt;
import com.huawei.ott.sdk.log.DebugLog;

import java.util.ArrayList;
import java.util.List;

public class PointAdapter extends BaseAdapter
{
    private static final String TAG = "PointAdapter";
    private List<ImageView> points = new ArrayList<ImageView>();
    private Context fContext;
    private int num;
    private int pointWidth;

    public PointAdapter(Context context)
    {
        fContext = context;
        pointWidth = fContext.getResources().getDimensionPixelSize(R.dimen.point_width);
    }

    public void setNum(int num)
    {
        this.num = num;
        createImageViews();
        notifyDataSetChanged();
    }

    private void createImageViews()
    {
        ImageViewExt imageViewExt;
        for (int i = 0; i < num; i++)
        {
            imageViewExt = new ImageViewExt(fContext);
            imageViewExt.setLayoutParams(new AbsListView.LayoutParams(pointWidth, pointWidth));
            imageViewExt.setImageResource(R.drawable.defaultpoint_and);

            points.add(imageViewExt);
        }
    }


    @Override
    public long getItemId(int p)
    {
        return p;
    }

    @Override
    public Object getItem(int p)
    {
        if (p < points.size())
        {
            return points.get(p);
        }
        return p;
    }

    @Override
    public View getView(int p, View convertView, ViewGroup parent)
    {
        return points.get(p);
    }


    @Override
    public int getCount()
    {
        return this.num;
    }

    public void setFocusPoint(int index)
    {
        DebugLog.info(TAG, "createImageViews points size = " + num);
        for (int i = 0; i < num; i++)
        {
            if (index == i)
            {
                points.get(i).setImageResource(R.drawable.redpoint);
            }
            else
            {
                points.get(i).setImageResource(R.drawable.defaultpoint_and);
            }
        }
    }
}
