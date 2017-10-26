package com.huawei.ott.composite.banner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.ott.gadget.extview.ImageViewExt;
import com.huawei.ott.sdk.glide.OTTGlide;
import com.huawei.ott.sdk.log.DebugLog;import com.huawei.ott.sdk.log.LogScenario;

import java.util.List;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder>
{
    private static final String TAG = "PreviewAdapter";
    private SparseArray<String> urlMap = new SparseArray<>();
    private SparseArray<List<String>> hcsSlaveAddressMap = new SparseArray<>();

    private LayoutInflater mInflater;
    private OnSmallPosterItemClickListener onSmallItemClickListener;

    private Context context;

    public PreviewAdapter(Context context)
    {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setOnSmallItemClickListener(PreviewAdapter.OnSmallPosterItemClickListener
            onSmallItemClickListener)
    {
        this.onSmallItemClickListener = onSmallItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.ads_preview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        if (urlMap.size() != 0)
        {
            new OTTGlide().display(context, holder.imageTxt, urlMap.get(position),
                    hcsSlaveAddressMap.get(position), R.drawable.default_image_96);

            holder.selectedImage.setVisibility(View.INVISIBLE);

            holder.imageTxt.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    DebugLog.info(TAG, "onBindViewHolder onClick position = " + position);

                    if (null != onSmallItemClickListener)
                    {
                        onSmallItemClickListener.onSmallPosterItemClick(position + urlMap.size() * 100);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return urlMap.size();
    }

    public void setURLAndNameMap(SparseArray<String> urlMap, SparseArray<List<String>>
            hcsSalveAddressMap)
    {
        this.urlMap = urlMap;
        this.hcsSlaveAddressMap = hcsSalveAddressMap;
        notifyDataSetChanged();
    }

    interface OnSmallPosterItemClickListener
    {
        void onSmallPosterItemClick(final int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageViewExt imageTxt;
        ImageViewExt selectedImage;

        public ViewHolder(View itemView)
        {
            super(itemView);

            imageTxt = (ImageViewExt) itemView.findViewById(R.id.small_poster_iamge);
            selectedImage = (ImageViewExt) itemView.findViewById(R.id.poster_selected_image);
        }
    }

}
