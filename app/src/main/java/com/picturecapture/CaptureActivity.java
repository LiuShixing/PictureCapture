package com.picturecapture;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CaptureActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Spinner mSpinner;
    private Vector<String> mImagePaths = null;
    private GridView mGridView;
    TextView mTextView;
    private MemoryCache mMemoryCache;
    private Set<AsyncTask> mTaskCollection;
    private Object [] mResources;
    private int mSelectedCount;

    private Map<String,Long> mLoadedImageIdMap;
    private Map<Integer,Boolean> mIsImageSelected;
    private ArrayList<Long> mImgIds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        mSelectedCount = 0;

        mToolbar = (Toolbar) findViewById(R.id.capture_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_48dp);
        mToolbar.setTitle("抓取");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.save:
                        if(mSelectedCount>0) {
                            deleteUnSaveImage();
                           finish();
                            Toast.makeText(CaptureActivity.this, "已保存"+mSelectedCount+"张图片", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(CaptureActivity.this, "请选择要保存的图片", Toast.LENGTH_LONG).show();
                        }


                        break;
                }

                return true;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedCount>0)
                showDeleteDialog();
            }
        });



        mGridView = (GridView)findViewById(R.id.cap_gridview);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridItemView item = (GridItemView) mGridView.getAdapter().getItem(position);
                    if (item.getSelected()) {
                        mSelectedCount--;
                        item.setSelected(false);
                        mIsImageSelected.put(position,false);
                    } else {
                        mSelectedCount++;
                        item.setSelected(true);
                        mIsImageSelected.put(position,true);
                    }
                    updateSelectedCount();
                }
        });
        //---------do load image---------------

        mMemoryCache = new MemoryCache();

        String htmlPath = getIntent().getStringExtra(MainActivity.WEB_ADDRESS);

        mTextView = (TextView) findViewById(R.id.text);

        AsynLoadImagePaths loadImagePaths = new AsynLoadImagePaths();
        loadImagePaths.execute(htmlPath);
        mLoadedImageIdMap= new HashMap<>();
        mIsImageSelected = new HashMap<>();
    }
    private void deleteUnSaveImage() {
            ImageAdapter adapter = (ImageAdapter) mGridView.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Boolean isSelected = mIsImageSelected.get(i);
                if (isSelected == null || !isSelected) {

                    if (mImgIds.size() > i) {
                        MainActivity.mDatabase.deleteImage(mImgIds.get(i));
                    }

                }
        }
    }
    private void showDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否保存已选"+mSelectedCount+"张图片");
        builder.setTitle("提示");
        builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                selectNoneImage();
                deleteUnSaveImage();
                finish();
            }
        });
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteUnSaveImage();
                Toast.makeText(CaptureActivity.this, "已保存"+mSelectedCount+"张图片", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.create().show();
    }
    private void updateSelectedCount()
    {
        ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) mSpinner.getAdapter();
        String text = ""+mSelectedCount+"已选择";
        String oldText = arrayAdapter.getItem(0);
        arrayAdapter.remove(oldText);
        arrayAdapter.insert(text,0);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.capture_toolbar_menu, menu);
        mSpinner = (Spinner) menu.findItem(R.id.num_selected).getActionView();

        List<String> list = new ArrayList<>();
        list.add("0已选择");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectAllmage();
                        break;
                    case 1:
                        selectNoneImage();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        return super.onCreateOptionsMenu(menu);
    }

    private void selectAllmage()
    {
        mSelectedCount = 0;
        ImageAdapter adapter = (ImageAdapter) mGridView.getAdapter();
        if(adapter!=null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                GridItemView itemView = (GridItemView) adapter.getItem(i);
                if (itemView != null) {
                    mSelectedCount++;
                    itemView.setSelected(true);
                }
                mIsImageSelected.put(i, true);
            }
        }
    }
    private void selectNoneImage()
    {
        ImageAdapter adapter = (ImageAdapter) mGridView.getAdapter();
        if(adapter!=null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                GridItemView itemView = (GridItemView) adapter.getItem(i);
                if (itemView != null) {
                    itemView.setSelected(false);
                }
                mIsImageSelected.put(i, false);
            }
            mSelectedCount = 0;
        }
    }
    class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private Object [] mObjects;
        private Map<String,GridItemView> mMapItemViews;
        public ImageAdapter(Context context,Object [] objects) {
            this.mContext = context;
            mTaskCollection = new HashSet<>();
            this.mObjects = objects;
            mMapItemViews = new HashMap<>();
        }

        @Override
        public int getCount() {
            return mObjects.length;
        }

        @Override
        public Object getItem(int position) {
            return mMapItemViews.get(mObjects[position]);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //定义一个ImageView,显示在GridView里
            GridItemView item;

            item = new GridItemView(mContext);
            item.setLayoutParams(new GridView.LayoutParams(MainActivity.mThumbnailWidth, MainActivity.mThumbnailWidth));
            item.setItemImageScaleType(ImageView.ScaleType.CENTER_CROP);

            item.setTag(mObjects[position]);
            String imageKey = String.valueOf(mObjects[position]);
            Bitmap image = mMemoryCache.getBitmapFromMemoryCache(imageKey);
            if (image != null) {
                item.setmItemImage(image);
                Boolean isSelected=mIsImageSelected.get(position);
                if( isSelected!=null && isSelected==true)
                {
                    item.setSelected(true);
                }
            } else {
                item.setItemResource(R.drawable.placeholder);


                if(mLoadedImageIdMap.containsKey(mObjects[position]))
                {
                    LoadBitmapFromMemTask task = new LoadBitmapFromMemTask(mTaskCollection,mMemoryCache,mGridView,mIsImageSelected,(String)mObjects[position]);
                    mTaskCollection.add(task);
                    task.execute(mLoadedImageIdMap.get(mObjects[position]), position);
                }
                else {
                    AsynLoadImage task = new AsynLoadImage();
                    mTaskCollection.add(task);
                    task.execute((String) mObjects[position], String.valueOf(position));
                }
            }
            if(!mMapItemViews.containsKey(mObjects[position]))
                mMapItemViews.put((String)mObjects[position],item);
            return item;
        }
    }
    class AsynLoadImagePaths extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            mImagePaths = ParseHtml.getImagePaths(params[0]);
            mResources = mImagePaths.toArray();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ImageAdapter adapter =new ImageAdapter(CaptureActivity.this,mResources);
            mGridView.setAdapter(adapter);
            mImgIds = new ArrayList<>(adapter.getCount());
        }
    }

    class AsynLoadImage extends AsyncTask<String, Void, Bitmap> {
        String mTag;
        int mPosition;
        @Override
        protected Bitmap doInBackground(String... params) {
            mTag = params[0];
            mPosition = Integer.parseInt(params[1]);
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                InputStream inStream = conn.getInputStream();
                byte [] bytes = Util.toByteArrayFromIStream(inStream);
              //  bitmap = BitmapFactory.decodeStream(inStream);
                bitmap = Util.decodeSampledBitmapFromBytes(bytes,MainActivity.mThumbnailWidth,MainActivity.mThumbnailWidth);
                if (bitmap != null) {
                    mMemoryCache.addBitmapToMemoryCache(mTag, bitmap);
                    long id =MainActivity.mDatabase.insertBitmaps(bytes);
                    mLoadedImageIdMap.put(mTag,id);
                    mImgIds.add(mPosition,id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            GridItemView item = (GridItemView) mGridView.findViewWithTag(mTag);
            if (bitmap != null && item != null) {
                item.setmItemImage(bitmap);
                Boolean isSelected=mIsImageSelected.get(mPosition);
                if( isSelected!=null && isSelected==true)
                {
                    item.setSelected(true);
                }
            }
            mTaskCollection.remove(this);
        }
    }
}
