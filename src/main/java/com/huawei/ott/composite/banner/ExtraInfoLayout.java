package com.huawei.ott.composite.banner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.huawei.ott.gadget.banner.VODBanner;
import com.huawei.ott.gadget.extview.LinearLayoutExt;
import com.huawei.ott.gadget.extview.TextViewExt;
import com.huawei.ott.gadget.extview.utils.Drawables;
import com.huawei.ott.sdk.log.DebugLog;
import com.huawei.ott.sdk.ottutil.android.DensityUtil;
import com.huawei.ott.sdk.ottutil.android.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ExtraInfoLayout extends RelativeLayout
{
    private static final String TAG = ExtraInfoLayout.class.getSimpleName();
    private int pointWidth;

    private int pointSpacing;
    private int size = 0;
    private Context fContext;
    private TextViewExt textViewIntro;
    private TextViewExt textViewIntroNext;
    private TextViewExt textViewName;
    private TextViewExt textViewNameNext;
    private LinearLayoutExt mPointLayout;
    private GridView mPointGridView;
    private PointAdapter mPointAdapter;
    private List<VODBanner> infos;

    public ExtraInfoLayout(Context context)
    {
        this(context, null);
    }

    public ExtraInfoLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.ads_extra_info_layout, this, true);
        fContext = context;
        infos = new ArrayList<>();
        init();
        initGridView();
    }

    private void initGridView()
    {
        mPointGridView = (GridView) findViewById(R.id.point_gridview);
        mPointLayout = (LinearLayoutExt) findViewById(R.id.point_layout);
        if (DeviceInfo.isPad())
        {
            mPointLayout.setVisibility(GONE);
            mPointGridView.setVisibility(GONE);
        }
        initPointWidthAndSpace();
        mPointGridView.setAdapter(getPointAdapter());
        mPointGridView.setColumnWidth(pointWidth);
    }

    private void init()
    {
        textViewIntro = (TextViewExt) findViewById(R.id.banner_vod_introduce);
        textViewName = (TextViewExt) findViewById(R.id.banner_vod_name);
        if (!DeviceInfo.isPad())
        {
            textViewNameNext = (TextViewExt) findViewById(R.id.banner_vod_name_next);
            textViewIntroNext = (TextViewExt) findViewById(R.id.banner_vod_introduce_next);
        }
    }

    private void initPointWidthAndSpace()
    {
        pointWidth = DensityUtil.dip2px(fContext, 10);
        pointSpacing = DensityUtil.dip2px(fContext, 0);
    }


    private PointAdapter getPointAdapter()
    {
        if (mPointAdapter == null)
        {
            mPointAdapter = new PointAdapter(fContext);
        }
        return mPointAdapter;
    }

    public void setPosterList(List<VODBanner> bannerVODList)
    {
        DebugLog.info(TAG, "setPosterSize , size = " + size);
        if (this.size != bannerVODList.size())
        {
            this.size = bannerVODList.size();
            infos = bannerVODList;
            getPointAdapter().setNum(size);
            mPointGridView.setNumColumns(size);
            ViewGroup.LayoutParams layoutParams = mPointGridView.getLayoutParams();
            layoutParams.width = size * (pointSpacing + pointWidth) - pointSpacing;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            mPointGridView.setLayoutParams(layoutParams);
            mPointLayout.setVisibility(View.VISIBLE);
            getPointAdapter().setFocusPoint(0);

            setFocus(0);
        }
    }

    private void showVRImage(TextViewExt textView, String content, int res, int maxLines)
    {
        textView.setMaxLines(maxLines);
        int maxWidth = getResources().getDimensionPixelOffset(R.dimen.name_max_width);
        int imageWidth = getResources().getDimensionPixelOffset(R.dimen.vr_logo_width) +
                getResources().getDimensionPixelOffset(R.dimen.vr_logo_name_left);
        TextPaint paint = textView.getPaint();

        int paddingLeft = textView.getPaddingLeft();
        int paddingRight= textView.getPaddingRight();

        int availableTextWidth = (maxWidth - paddingLeft - paddingRight) * maxLines-imageWidth;
        String ellipsizeStr = (String) TextUtils.ellipsize(content,  paint, availableTextWidth, TextUtils.TruncateAt.END);

        Drawable drawable = Drawables.getInstance().getDrawable(getResources(), res);
        drawable.setBounds(getResources().getDimensionPixelOffset(R.dimen.vr_logo_name_left),
                getResources().getDimensionPixelOffset(R.dimen.vr_logo_name_top), getResources()
                        .getDimensionPixelOffset(R.dimen.vr_logo_name_right), getResources()
                        .getDimensionPixelOffset(R.dimen.vr_logo_name_bottom));
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        String imgTag = "img";
        int start = ellipsizeStr.length();
        int end = start + imgTag.length();
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(ellipsizeStr + imgTag);
        ssBuilder.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ssBuilder);
    }

    public void setFocus(final int index)
    {
        getPointAdapter().setFocusPoint(index);
        VODBanner vodBanner = infos.get(index);
        if (null != vodBanner)
        {
            String name = "";
            if (!TextUtils.isEmpty(vodBanner.getVODName()))
            {
                name = vodBanner.getVODName().toUpperCase(Locale.getDefault());;
            }
            textViewIntro.setText(vodBanner.getVodIntroduce());
            textViewName.setText(name);
            if (vodBanner.isVR())
            {
                showVRImage(textViewName, name, R.drawable.vr_vrlogo_and, 2);
            }
        }
    }


    public void onScrollComing(int movingDirection,int nextPageIndex, float positionOffset)
    {
        VODBanner vodBanner = infos.get(nextPageIndex);
        String introduce = vodBanner.getVodIntroduce();
        String name = "";
        if (!TextUtils.isEmpty(vodBanner.getVODName()))
        {
            name = vodBanner.getVODName().toUpperCase(Locale.getDefault());
        }
        textViewNameNext.setText(name);
        textViewIntroNext.setText(introduce);

        if (vodBanner.isVR())
        {
            showVRImage(textViewNameNext, name, R.drawable.vr_vrlogo_and, 2);
        }
        if (1 == movingDirection)
        {
            textViewNameNext.setAlpha(positionOffset);
            textViewName.setAlpha(1 - positionOffset);

            textViewIntro.setAlpha(1 - positionOffset);
            textViewIntroNext.setAlpha(positionOffset);
        }
        else
        {
            textViewNameNext.setAlpha(1 - positionOffset);
            textViewName.setAlpha(positionOffset);

            textViewIntro.setAlpha(positionOffset);
            textViewIntroNext.setAlpha(1 - positionOffset);
        }
    }
}
