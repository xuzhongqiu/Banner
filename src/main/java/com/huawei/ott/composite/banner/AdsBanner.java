package com.huawei.ott.composite.banner;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.huawei.ott.gadget.banner.VODBanner;
import com.huawei.ott.sdk.log.DebugLog;
import com.huawei.ott.sdk.ottutil.android.DealSecondClickUtil;
import com.huawei.ott.sdk.ottutil.android.DeviceInfo;

import java.util.List;

public class AdsBanner extends FrameLayout
{
    private static final String TAG = AdsBanner.class.getSimpleName();
    /**
     * context
     */
    private Context fContext;

    /**
     * custome viewpager for sliding poster
     */
    private PosterViewPager fViewPager;
    /**
     * show the small poster
     */
    private RecyclerView smallGalleryView;
    /**
     * banner gallery adapter
     */
    private AdsBannerAdapter mGalleryAdapter;
    /**
     * small poster gallery adapter
     */
    private PreviewAdapter smallGalleryAdapter;
    /**
     * linearLayoutManager for small gallery
     */
    private PosterLinearLayoutManager linearLayoutManager;
    /**
     * callback
     */
    private CallBack callBack;
    /**
     * the banner stop state
     */
    private boolean isStopped = true;
    /**
     * poster size
     */
    private int size = 0;

    /**
     * banner auto switch interval
     */
    private static final int AUTO_PLAY_INTERVAL = 3 * 1000;

    public interface CallBack
    {
        void onItemClick(int position);

        void onItemSelected(int position);

        void onItemScrollComing(int movingDirection, int position, float positionOffset);
    }

    public AdsBanner(Context context)
    {
        this(context, null);
        this.fContext = context;
    }

    public AdsBanner(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public AdsBanner(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.fContext = context;
        setBackgroundColor(getResources().getColor(R.color.C15));
        LayoutInflater.from(context).inflate(R.layout.ad_banner, this, true);
        initViewPager();

        if (DeviceInfo.isPad())
        {
            initSmallGalleryView();
        }
    }


    public PosterViewPager getfViewPager()
    {
        return fViewPager;
    }

    public void setCallBack(CallBack callBack)
    {
        this.callBack = callBack;
        getPagerAdapter().setCallBack(this.callBack);
    }

    /**
     * set banner vague when scorll y greater than threshold
     *
     * @param value
     */
    public void setVague(int value)
    {
        final int itemFuzzyBanner = getResources().getDimensionPixelSize(R.dimen.h_poster) / 10;
        if (value > itemFuzzyBanner)
        {
            stopAutoScroll();
        }
        else
        {
            startAutoScroll();
        }
        fViewPager.fuzzyBanner(value);
    }

    /**
     * first remove last runnable
     * then poster a delayed runnable
     */


    /**
     * the runnable to switch the poster
     */
    private final Runnable mRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            int position = fViewPager.getCurrentItem();
            position++;
            fViewPager.setCurrentItem(position, true);
            postDelayed(mRunnable, AUTO_PLAY_INTERVAL);
        }
    };

    /**
     * set the poster bg changed listener
     *
     * @param onColorChangeListener
     */
    public void setOnBgChangeListener(PosterViewPager.IBgChangeListener onColorChangeListener)
    {
        fViewPager.setOnBgChangeListener(onColorChangeListener);
    }


    float xStart, yStart, xEnd, yEnd;
    boolean isMove;
    private static final int MOVE_OFFSET = 10;

    private OnTouchListener onViewPagerTouchListener = new OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            if (size == 1)
            {
                return true;
            }
            else
            {
                if (DeviceInfo.isPad())
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            xStart = event.getX();
                            yStart = event.getY();
                            isMove = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            xEnd = event.getX();
                            yEnd = event.getY();
                            float offsetXonTouchEvent = Math.abs(xEnd - xStart);
                            float offsetYonTouchEvent = Math.abs(yEnd - yStart);
                            if (offsetYonTouchEvent < offsetXonTouchEvent && MOVE_OFFSET <
                                    offsetXonTouchEvent)

                            {
                                isMove = true;
                                stopAutoScroll();
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            xEnd = event.getX();
                            yEnd = event.getY();
                            if (isMove)
                            {
                                startAutoScroll();
                            }
                            break;
                        default:
                            break;
                    }
                }
                return AdsBanner.super.onTouchEvent(event);
            }
        }
    };

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager
            .OnPageChangeListener()
    {
        int lastPage = -1;
        int movingDirection = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            if (size > 1 && !DeviceInfo.isPad())
            {
                if (null != callBack && 0 != positionOffset)
                {
                    int nextIndex;
                    if (position == lastPage)
                    {
                        nextIndex = (position + 1) % size;
                        movingDirection = 1;
                    }
                    else
                    {
                        nextIndex = (position) % size;
                        movingDirection = -1;
                    }
                    callBack.onItemScrollComing(movingDirection, nextIndex, positionOffset);
                }
            }
        }

        @Override
        public void onPageSelected(int position)
        {
            if (size > 1)
            {
                int index = position % size;
                if (DeviceInfo.isPad() && null != linearLayoutManager)
                {
                    linearLayoutManager.scrollToPosition(index);
                }
                if (!DeviceInfo.isPad())
                {
                    AdsBannerAdapter adsBannerAdapter = getPagerAdapter();
                    VODBanner vodBanner = adsBannerAdapter.getVodBeanList().get(index);
                    String imageUri = "";
                    if (vodBanner != null)
                    {
                        imageUri = vodBanner.getVODPicture();
                    }
                    fViewPager.onBgChangeCallBack(imageUri);

                }
                if (null != callBack)
                {
                    callBack.onItemSelected(index);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
            if (ViewPager.SCROLL_STATE_IDLE == state)
            {
                lastPage = fViewPager.getCurrentItem();
                DebugLog.info(TAG, "onPageScrollStateChanged lastPage " + lastPage);
            }
        }
    };

    /**
     * init viewpager data
     */
    private void initViewPager()
    {
        fViewPager = (PosterViewPager) findViewById(R.id.poster_gallery);
        fViewPager.setOnTouchListener(onViewPagerTouchListener);
        fViewPager.setAdapter(getPagerAdapter());
        fViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    /**
     * init the small poster gallery view
     */
    private void initSmallGalleryView()
    {
        AdsPreviewLayout adsPreviewLayout = new AdsPreviewLayout(fContext);

        addView(adsPreviewLayout);

        smallGalleryView = (RecyclerView) adsPreviewLayout.findViewById(R.id.small_poster_gallery);
        linearLayoutManager = new PosterLinearLayoutManager(fContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        smallGalleryView.setLayoutManager(linearLayoutManager);

        getSmallGalleryAdapter().setOnSmallItemClickListener(new PreviewAdapter
                .OnSmallPosterItemClickListener()
        {
            @Override
            public void onSmallPosterItemClick(int position)
            {
                if (getSmallGalleryAdapter().getItemCount() > 1)
                {
                    stopAutoScroll();
                    fViewPager.setCurrentItem(position, false);
                    startAutoScroll();
                }
            }
        });

        smallGalleryView.setAdapter(getSmallGalleryAdapter());
    }

    /**
     * the gallery start
     */
    public void startAutoScroll()
    {
        if (this.size > 1 && isStopped)
        {
            DebugLog.info(TAG, "startAutoScroll");
            removeCallbacks(mRunnable);
            postDelayed(mRunnable, AUTO_PLAY_INTERVAL);
        }
        isStopped = false;
    }

    /**
     * the gallery stop
     */
    public void stopAutoScroll()
    {
        DebugLog.info(TAG, "stopAutoScroll");
        removeCallbacks(mRunnable);
        isStopped = true;
    }

    /**
     * set the banner data
     *
     * @param vodBeanList
     */
    public void updateData(List<VODBanner> vodBeanList)
    {
        size = vodBeanList.size();
        getPagerAdapter().setDataRes(vodBeanList);
        if (size > 1)
        {
            fViewPager.setCurrentItem(size * 100);
        }
    }

    /**
     * updatePadImageURL
     *
     * @param urlMap
     * @param hcsSalveAddressMap
     * @param vodBeanList
     */
    public void updatePadImageURL(SparseArray<String> urlMap, SparseArray<List<String>>
            hcsSalveAddressMap, List<VODBanner> vodBeanList)
    {
        size = vodBeanList.size();
        getPagerAdapter().setDataRes(vodBeanList);

        getSmallGalleryAdapter().setURLAndNameMap(urlMap, hcsSalveAddressMap);
        if (size > 1)
        {
            postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (null != smallGalleryView.getChildAt(0))
                    {
                        smallGalleryView.getChildAt(0).findViewById(R.id.small_poster_iamge)
                                .performClick();
                    }
                }
            }, 200);
        }
    }

    /**
     * get banner viewpager adapter
     *
     * @return
     */
    private AdsBannerAdapter getPagerAdapter()
    {
        if (mGalleryAdapter == null)
        {
            mGalleryAdapter = new AdsBannerAdapter(fContext, fViewPager);
        }
        return mGalleryAdapter;
    }

    /**
     * get banner small poster adapter
     *
     * @return
     */
    private PreviewAdapter getSmallGalleryAdapter()
    {
        if (smallGalleryAdapter == null)
        {
            smallGalleryAdapter = new PreviewAdapter(fContext);
        }
        return smallGalleryAdapter;
    }
}
