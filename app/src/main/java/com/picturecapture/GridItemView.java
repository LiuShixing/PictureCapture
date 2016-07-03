package com.picturecapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GridItemView extends RelativeLayout
{
    private ImageView mItemImage;
    private ImageView mItemSletected;
    private boolean mIsSelected;

    public GridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.grid_item_view, this);
        mItemImage = (ImageView)findViewById(R.id.item_image);
        mItemSletected = (ImageView)findViewById(R.id.item_selected);
        mItemSletected.setVisibility(INVISIBLE);
        mIsSelected = false;

    }

    public boolean getSelected()
    {
        return mIsSelected;
    }
    public void setSelected(boolean isSelected)
    {
        mIsSelected = isSelected;
        if(mIsSelected)
        {
            mItemSletected.setVisibility(VISIBLE);
        }
        else
        {
            mItemSletected.setVisibility(INVISIBLE);
        }
    }
    public void setmItemImage(Bitmap image)
    {
        mItemImage.setImageBitmap(image);
    }
    public void setItemResource(int resource)
    {
        mItemImage.setImageResource(resource);
    }
    public void setItemImageScaleType(ImageView.ScaleType scaleType)
    {
        mItemImage.setScaleType(scaleType);
    }
}