package com.huawei.ott.composite.banner;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huawei.ott.composite.banner.util.BitmapCache;
import com.huawei.ott.gadget.banner.VODBanner;
import com.huawei.ott.sdk.image.ImageLoaderTask;
import com.huawei.ott.sdk.image.ImageLoaderUtil;
import com.huawei.ott.sdk.log.DebugLog;
import com.huawei.ott.sdk.ottutil.android.DeviceInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;

import java.util.ArrayList;
import java.util.List;

public class AdsBannerAdapter extends PagerAdapter
{
    private static final String TAG = AdsBannerAdapter.class.getSimpleName();
    private Context fContext;
    private ViewHolder holder;
    private PosterViewPager posterGalleryPage;
    private List<VODBanner> vodBeanList = new ArrayList<>();
    private AdsBanner.CallBack callBack;
    private DisplayImageOptions options = ImageLoaderUtil.getDefaultDisplayImageOptions()
            .cacheInMemory(true).showImageForEmptyUri(R.drawable.default_image_240)
            .showImageOnLoading(R.drawable.default_image_240).build();

    private DisplayImageOptions phoneOptions = ImageLoaderUtil.getDefaultDisplayImageOptions()
            .cacheInMemory(true).showImageForEmptyUri(R.drawable.banner_default_image_168_and)
            .showImageOnLoading(R.drawable.banner_default_image_168_and).build();


    private SparseArray<List<String>> hcsSlaveAddressMap = new SparseArray<>();


    public AdsBannerAdapter(Context aContext, PosterViewPager posterGalleryPager)
    {
        super();
        this.fContext = aContext;
        this.posterGalleryPage = posterGalleryPager;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        holder = new ViewHolder();
        DebugLog.info(TAG, "Position is " + position);
        View convertView = LayoutInflater.from(this.fContext).inflate(R.layout.adsbanner_item,
                container, false);
        holder.imageView = (ImageView) convertView.findViewById(R.id.item_banner_image);

        if (this.vodBeanList.size() > 0)
        {
            final int index = position % this.vodBeanList.size();
            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    DebugLog.info(TAG, "instantiateItem , Onclick id is " + v.toString());
                    if (null != callBack)
                    {
                        callBack.onItemClick(index);
                    }
                }
            });

            String posterUrl = this.vodBeanList.get(index).getVODPicture();
            if (DeviceInfo.isPad())
            {
                new PosterLoadTask(position, posterUrl).displayImageWithUISize(posterUrl, this
                        .hcsSlaveAddressMap.get(index), holder.imageView, options);
            }
            else
            {
                Bitmap cacheBitmap = BitmapCache.getInstance().getCacheGradientBitmap(posterUrl);
                if (cacheBitmap != null)
                {
                    holder.imageView.setImageBitmap(cacheBitmap);
                }
                else
                {
                    new PosterLoadTask(position, posterUrl).displayImageWithUISize(posterUrl,
                            this.hcsSlaveAddressMap.get(index), holder.imageView, phoneOptions);
                }
            }
        }

        this.posterGalleryPage.setObjectForPosition(convertView, position);
        DebugLog.info(TAG, "container size  is " + container.getChildCount());
        container.addView(convertView);
        return convertView;
    }


    @Override
    public int getCount()
    {
        return Short.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        DebugLog.info(TAG, "position is " + position);
        container.removeView((View) object);
        posterGalleryPage.clearViewMap(position);
    }

    public void setDataRes(List<VODBanner> beanList)
    {
        DebugLog.info(TAG, "setDataRes , vodBeanList size : " + beanList.size());
        this.vodBeanList = beanList;
    }

    public List<VODBanner> getVodBeanList()
    {
        return vodBeanList;
    }

    public void setCallBack(AdsBanner.CallBack callBack)
    {
        this.callBack = callBack;
    }

    private static class ViewHolder
    {
        ImageView imageView;
    }

    private class PosterLoadTask extends ImageLoaderTask
    {
        private int p;
        private String posterUrl;

        public PosterLoadTask(int position, String imageUri)
        {
            p = position;
            posterUrl = imageUri;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
        {
            super.onLoadingComplete(imageUri, view, loadedImage);
            if (!DeviceInfo.isPad() && loadedImage != null)
            {
                DebugLog.info(TAG, "Position is " + p);
                posterGalleryPage.getBlurAndGradientBitmap(p, posterUrl, loadedImage);
            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason)
        {
            super.onLoadingFailed(imageUri, view, failReason);
        }
    }
}