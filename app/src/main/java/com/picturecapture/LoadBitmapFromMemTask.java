package com.picturecapture;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.GridView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by 明白 on 2016/5/11.
 */

class LoadBitmapFromMemTask extends AsyncTask {
    long mId;
    int mPosition = 0;
    MemoryCache mMemoryCache;
    Set<AsyncTask> mTaskCollection;
    GridView mGridView;
    Map<Integer,Boolean> mIsSelected;
    String mTag;
    public LoadBitmapFromMemTask(Set<AsyncTask> collection, MemoryCache cache, GridView gridView, Map<Integer,Boolean> map,String tag) {
        mMemoryCache = cache;
        mTaskCollection= collection;
        mGridView = gridView;
        mIsSelected= map;
        mTag= tag;
    }
    public LoadBitmapFromMemTask(Set<AsyncTask> collection, MemoryCache cache, GridView gridView, Map<Integer,Boolean> map) {
        mMemoryCache = cache;
        mTaskCollection= collection;
        mGridView = gridView;
        mIsSelected= map;
        mTag= null;
    }

    @Override
    protected Bitmap doInBackground(Object[] params) {
        mId = (long) params[0];
        mPosition = (int) params[1];
        //从数据库查询图片
        ArrayList<Byte> byteArrayList = MainActivity.mDatabase.queryImage(mId);
        Bitmap bitmap = null;
        try {
            byte [] bytes = new byte[byteArrayList.size()];
            for (int i=0;i<bytes.length;i++) {
                bytes[i] = byteArrayList.get(i);
            }

            //缩略图片，避免内存不足
            bitmap = Util.decodeSampledBitmapFromBytes(bytes, MainActivity.mThumbnailWidth, MainActivity.mThumbnailWidth);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap != null) {
            //使用缓冲
            mMemoryCache.addBitmapToMemoryCache(String.valueOf(mId), bitmap);
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        GridItemView item;
        if(mTag!=null)
        {
            item= (GridItemView) mGridView.findViewWithTag(mTag);
        }
         else
        {
            item= (GridItemView) mGridView.findViewWithTag(mId);
        }
        if (o != null && item != null) {
            item.setmItemImage((Bitmap) o);

            Boolean isSelected=mIsSelected.get(mPosition);
            if( isSelected!=null && isSelected==true)
            {
                item.setSelected(true);
            }
        }
        mTaskCollection.remove(this);
    }
}
