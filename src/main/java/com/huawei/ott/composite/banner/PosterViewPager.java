package com.huawei.ott.composite.banner;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.huawei.ott.composite.banner.util.BitmapCache;
import com.huawei.ott.composite.banner.util.FastBlur;
import com.huawei.ott.gadget.banner.VODBanner;
import com.huawei.ott.gadget.extview.ImageViewExt;
import com.huawei.ott.gadget.extview.utils.Drawables;
import com.huawei.ott.gadget.util.BitmapUtil;
import com.huawei.ott.gadget.util.SmoothScrollerUtil;
import com.huawei.ott.sdk.log.DebugLog;
import com.huawei.ott.sdk.ottutil.android.DensityUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PosterViewPager extends ViewPager
{
    /**
     * class tag
     */
    private static String TAG = PosterViewPager.class.getSimpleName();
    /**
     * banner scroller duration
     */
    private static final int SCROLLER_DURATION = 500;
    /**
     * position offset rate
     */
    private static final float POSITION_OFFSET_RATE = 1.5f;
    /**
     * current position ImageView
     */
    private ImageView currentImageView;
    /**
     * current position original bitmap
     */
    private Bitmap originalBitmap;
    /**
     * HashMap use to save viewPage views
     */
    private HashMap<Integer, Object> fObjs = new LinkedHashMap<>();
    /**
     * bg change Listener
     */
    private IBgChangeListener fOnBgChangeListener;
    /**
     * last blur level
     */
    private int lastLevel;
    /**
     * thread pool ExecutorService
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();
    public static final int BLUR_LEVEL_COUNT = 10;

    /**
     * IBgChangeListener use to change the bg
     */
    public interface IBgChangeListener
    {
        /**
         * use to change the bg
         *
         * @param blurBitmap the blur bitmap
         */
        void onBitmapChange(Bitmap blurBitmap);
    }

    /**
     * set onBgChangeListener
     *
     * @param onBgChangeListener
     */
    public void setOnBgChangeListener(IBgChangeListener onBgChangeListener)
    {
        this.fOnBgChangeListener = onBgChangeListener;
    }


    /**
     * the class
     * init defaultBitmap
     *
     * @param context context
     * @param attrs   attrs
     */
    public PosterViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        SmoothScrollerUtil smoothScrollerUtil = new SmoothScrollerUtil();
        smoothScrollerUtil.setSmoothScroller(context, this, SCROLLER_DURATION);
    }


    /**
     * listener the viewpager onPageScrolled
     * reset some params and complete the current ImageView transition in x
     *
     * @param position             the cur position
     * @param positionOffset       the scroll positionOffset
     * @param positionOffsetPixels the scroll positionOffsetPixels
     */

    @Override
    protected void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        originalBitmap = null;
        currentImageView = null;
        pHandler.removeMessages(MSG_VERTICAL_SCROLL);
        View leftView = findViewByObject(position);
        if (leftView != null)
        {
            leftView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            ImageView leftImageView = (ImageView) leftView.findViewById(R.id.item_banner_image);
            leftImageView.setTranslationX(positionOffsetPixels / POSITION_OFFSET_RATE);
            leftView.bringToFront();
        }
    }

    /**
     * onBgChangeCallBack
     *
     * @param imageUri the picture uri
     */
    public void onBgChangeCallBack(String imageUri)
    {
        Bitmap bitmap = BitmapCache.getInstance().getCacheBlurBitmap(imageUri);
        if (fOnBgChangeListener != null)
        {
            fOnBgChangeListener.onBitmapChange(bitmap);
        }
    }


    /**
     * start a runnable to get blurBitmap and GradientBitmap
     *
     * @param pos      the cut position
     * @param imageUri the picture uri
     * @param bitmap   the original bitmap
     */
    protected void getBlurAndGradientBitmap(int pos, String imageUri, Bitmap bitmap)
    {
        executorService.submit(new UpdateBgRunnable(pos, imageUri, bitmap));
    }

    /**
     * init the handler
     */
    PosterHandler pHandler = new PosterHandler();
    /**
     * the msg horizontal scroll
     */
    private static final int MSG_HORIZONTAL_SCROLL = 0;
    /**
     * the msg vertical scroll
     */
    private static final int MSG_VERTICAL_SCROLL = 1;

    /**
     * the handler to refresh the imageview
     * MSG_VERTICAL_SCROLL : parent view is scrollview,by scrollview scroll to up ,current
     * imageview show blur bitmap
     * <p>
     * MSG_HORIZONTAL_SCROLL:viewpager show CacheGradientBitmap
     */
    private class PosterHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSG_VERTICAL_SCROLL:
                    Bitmap temp = (Bitmap) msg.obj;
                    int level = msg.arg1;
                    if (currentImageView != null)
                    {
                        DebugLog.debug(TAG, "handleMessage-->setalpha" + level);
                        currentImageView.setImageBitmap(temp);
                        currentImageView.setAlpha(1 - (level * 0.1f));
                    }
                    break;
                case MSG_HORIZONTAL_SCROLL:
                    int pos = msg.arg1;
                    String posterUrl = msg.obj.toString();
                    View view = findViewByObject(pos);
                    if (view != null)
                    {
                        ImageView currentImageView = (ImageView) view.findViewById(R.id
                                .item_banner_image);
                        Bitmap cacheBitmap = BitmapCache.getInstance().getCacheGradientBitmap
                                (posterUrl);
                        if (cacheBitmap != null)
                        {
                            currentImageView.setImageBitmap(cacheBitmap);
                        }
                    }
                    if (pos == getCurrentItem())
                    {
                        onBgChangeCallBack(posterUrl);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class UpdateBgRunnable implements Runnable
    {
        private int pos;
        private String imageUri;

        private Bitmap bitmap;

        UpdateBgRunnable(int pos, String imageUri, Bitmap bitmap)
        {
            this.pos = pos;
            this.imageUri = imageUri;
            this.bitmap = bitmap;
        }

        @Override
        public void run()
        {
            if (bitmap == null)
            {
                return;
            }
            Bitmap blurBitmap = getBlurBitmap(bitmap);
            int screenWidth = DensityUtil.getScreenWidth(getContext());
            Bitmap gradtBitmap = BitmapUtil.gradientBitmap(bitmap, screenWidth, getResources()
                    .getDimensionPixelSize(R.dimen.h_poster));
            BitmapCache.getInstance().saveGradientBitmap(gradtBitmap, imageUri);
            BitmapCache.getInstance().saveBlurBitmap(blurBitmap, imageUri);
            Message msg = pHandler.obtainMessage();
            msg.what = MSG_HORIZONTAL_SCROLL;
            msg.arg1 = this.pos;
            msg.obj = imageUri;
            pHandler.sendMessage(msg);
        }
    }

    private Bitmap getBlurBitmap(Bitmap sourceBitmap)
    {
        return FastBlur.blur(sourceBitmap, 10, 8);
    }

    private class UpdateSERunnable implements Runnable
    {
        private int level;

        UpdateSERunnable(int level)
        {
            this.level = level;
        }

        @Override
        public void run()
        {
            DebugLog.info(TAG, "UpdateSERunnable level is " + level);
            Bitmap newBitmap = FastBlur.blur(originalBitmap, this.level, 8);
            Message msg = pHandler.obtainMessage();
            msg.what = MSG_VERTICAL_SCROLL;
            msg.arg1 = this.level;
            msg.obj = newBitmap;
            pHandler.sendMessage(msg);
        }
    }

    protected void fuzzyBanner(int value)
    {
        if (currentImageView == null)
        {
            currentImageView = getCurrentImageView();
        }
        if (currentImageView == null)
        {
            DebugLog.debug(TAG, "currentImageView == null");
            return;
        }
        if (null == originalBitmap)
        {
            DebugLog.debug(TAG, "null == originalBitmap");
            originalBitmap = getCurrentBitmap();
            lastLevel = 0;
            pHandler.removeMessages(MSG_VERTICAL_SCROLL);
        }
        final int itemFuzzyBanner = getResources().getDimensionPixelSize(R.dimen.h_poster) /
                BLUR_LEVEL_COUNT;
        if (value > itemFuzzyBanner)
        {
            setClickable(false);
            int level = (int) (17 * (value / Double.valueOf(getHeight())));
            if (lastLevel != level && lastLevel < BLUR_LEVEL_COUNT)
            {
                DebugLog.info(TAG, "inner level is " + level);
                executorService.submit(new UpdateSERunnable(level));
            }
            lastLevel = level;
        }
        else
        {
            DebugLog.info(TAG, "fuzzyBanner level is 0");
            pHandler.removeMessages(MSG_VERTICAL_SCROLL);
            if (originalBitmap != null)
            {
                currentImageView.setImageBitmap(originalBitmap);
            }
            else
            {
                currentImageView.setImageDrawable(Drawables.getInstance().getDrawable
                        (getResources(), R.drawable.banner_default_image_168_and));
            }
            currentImageView.setAlpha(1f);
            lastLevel = 0;
            setClickable(true);
        }
    }

    /**
     * build current bitmap
     *
     * @return current bitmap
     */
    private Bitmap getCurrentBitmap()
    {
        AdsBannerAdapter pagerAdapter = (AdsBannerAdapter) getAdapter();
        if (pagerAdapter == null)
        {
            return null;
        }
        int size = pagerAdapter.getVodBeanList().size();
        int index = getCurrentItem() % size;
        VODBanner vodBanner = pagerAdapter.getVodBeanList().get(index);
        Bitmap bitmap = null;
        if (vodBanner != null)
        {
            bitmap = BitmapCache.getInstance().getCacheGradientBitmap(vodBanner.getVODPicture());
        }
        return bitmap;
    }

    /**
     * get current imageView
     *
     * @return
     */
    private ImageViewExt getCurrentImageView()
    {
        int index = getCurrentItem();
        DebugLog.debug(TAG, "index:" + index);
        View view = findViewByObject(index);
        if (view != null)
        {
            return (ImageViewExt) view.findViewById(R.id.item_banner_image);
        }
        return null;
    }

    private View findViewByObject(int position)
    {
        Object o = fObjs.get(Integer.valueOf(position));
        if (o == null)
        {
            return null;
        }
        PagerAdapter a = getAdapter();
        View v;
        for (int i = 0; i < getChildCount(); i++)
        {
            v = getChildAt(i);
            if (a.isViewFromObject(v, o))
            {
                return v;
            }
        }
        return null;
    }

    protected void setObjectForPosition(Object obj, int position)
    {
        //fObjs.clear();
        fObjs.put(Integer.valueOf(position), obj);
    }

    public void clearViewMap(int p)
    {
        fObjs.remove(p);
    }

}