package com.picturecapture;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import android.widget.Toast;


public class PictureLibActivity extends AppCompatActivity  {

    private Toolbar mToolbar;
    private Spinner mSpinner;
    private MenuItem mDeleteMenuItem;
    private ArrayAdapter<String> mRankSpinnerAdapter;
    private ArrayAdapter<String> mSelectSpinnerAdapter;
    private MyGridView mGridView;
    private MemoryCache mMemoryCache;
    private Set<AsyncTask> mTaskCollection;
    private boolean mIsDeleteModule;
    private int mSelectedCount;
    private ArrayList<Long> mImgIds;
    private Map<Integer,Boolean> mIsImageSelected;
    private ImageView mMyImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_lib);

        mIsDeleteModule = false;
        mSelectedCount = 0;

        mMyImageView = (ImageView)findViewById(R.id.showImage);
        mMyImageView.setVisibility(View.INVISIBLE);
        mMyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyImageView.setVisibility(View.INVISIBLE);
                mGridView.setVisibility(View.VISIBLE);
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.picture_lib_toobar);
        mToolbar.setTitle("图库");

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_48dp);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.delete)
                {
                    showDeleteDialog();
                }
                return true;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //-------------mGridView-----------------
        mIsImageSelected = new HashMap<>();
        mMemoryCache = new MemoryCache();

        queryImageIds();

      //  MainActivity.mDatabase.queryImageIds();

        mGridView = (MyGridView) findViewById(R.id.grid_view);
        mGridView.setAdapter(new ImageAdapter(this));
      //  mGridView.setmIsIntercept(true);
        mGridView.setLongClickable(true);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridItemView item = (GridItemView) mGridView.getAdapter().getItem(position);
                if(mIsDeleteModule) {
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
                    Log.e("e","select position="+position+"  "+item.toString());
                }
                else {
                    ArrayList<Byte> byteArrayList = MainActivity.mDatabase.queryImage(mImgIds.get(position));
                    Bitmap bitmap = null;
                    byte[] bytes = new byte[byteArrayList.size()];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = byteArrayList.get(i);
                    }
                    bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    if(bitmap!=null) {
                        mMyImageView.setImageBitmap(bitmap);
                        mMyImageView.setVisibility(View.VISIBLE);
                        mGridView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //    mGridView.setmIsIntercept(false);
                mDeleteMenuItem.setVisible(true);
                GridItemView item = (GridItemView)mGridView.getAdapter().getItem(position);
                Log.e("e","long select position="+position+"  "+item.toString());
                item.setSelected(true);
                mIsImageSelected.put(position,true);
                mSelectedCount++;
                mIsDeleteModule = true;
                mSpinner.setVisibility(View.VISIBLE);
                mSpinner.setAdapter(mSelectSpinnerAdapter);
                mSpinner.invalidate();
                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.picture_lib_menu, menu);

        mDeleteMenuItem = menu.findItem(R.id.delete);
        mDeleteMenuItem.setVisible(false);
        mSpinner = (Spinner) menu.findItem(R.id.rank).getActionView();
        List<String> list = new ArrayList<>();

        mRankSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        mRankSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mRankSpinnerAdapter);

        List<String> listSelect = new ArrayList<>();
        listSelect.add("0已选择");
        listSelect.add("全部取消");
        mSelectSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listSelect);
        mSelectSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(mIsDeleteModule) {
                        switch (position) {
                            case 1:
                                selectNoneImage();
                                break;
                        }
                        updateSelectedCount();
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        return super.onCreateOptionsMenu(menu);
    }
    private void showDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("将删除"+mSelectedCount+"张图片");
        builder.setTitle("提示");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                undoSelect();
            }
        });
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteImage();
            }
        });
        builder.create().show();
    }
    private void queryImageIds()
    {
        mImgIds=null;
        mImgIds = MainActivity.mDatabase.queryImageIds();
        if(mImgIds==null)
            mImgIds = new ArrayList<>();
    }
    private void undoSelect()
    {
        mDeleteMenuItem.setVisible(false);
        mIsDeleteModule = false;
        mSpinner.setVisibility(View.INVISIBLE);
        mSpinner.setAdapter(mRankSpinnerAdapter);
        mRankSpinnerAdapter.notifyDataSetChanged();
        mSpinner.invalidate();
        selectNoneImage();
    }
    private void deleteImage()
    {
        ImageAdapter adapter = (ImageAdapter) mGridView.getAdapter();
        for(int i=0;i<adapter.getCount();i++)
        {
            Boolean isSelected = mIsImageSelected.get(i);
            if(isSelected!= null && isSelected)
            {
                MainActivity.mDatabase.deleteImage(mImgIds.get(i));
            }
        }
        queryImageIds();
        ((ImageAdapter) mGridView.getAdapter()).notifyDataSetChanged();
        int selectCount = mSelectedCount;
        undoSelect();
        Toast.makeText(PictureLibActivity.this, "成功删除"+selectCount+"张图片", Toast.LENGTH_LONG).show();

    }
    private void updateSelectedCount()
    {
        String text = ""+mSelectedCount+"已选择";
        String oldText = mSelectSpinnerAdapter.getItem(0);
        mSelectSpinnerAdapter.remove(oldText);
        mSelectSpinnerAdapter.insert(text,0);
    }
    private void selectNoneImage()
    {
        ImageAdapter adapter = (ImageAdapter) mGridView.getAdapter();
        for(int i=0;i<adapter.getCount();i++)
        {
            GridItemView itemView = (GridItemView) adapter.getItem(i);
            if(itemView!=null) {
                itemView.setSelected(false);
                mIsImageSelected.put(i,false);
            }
        }
        mSelectedCount = 0;
    }

    class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private boolean[] mIsSelect;
        private Map<Integer,GridItemView> mMapItemViews;
        public ImageAdapter(Context context) {
            this.mContext = context;
            mTaskCollection = new HashSet<>();
            mIsSelect= new boolean[mImgIds.size()];
            mMapItemViews = new HashMap<>();
            for(int i=0;i<mIsSelect.length;i++)
            {
                mIsSelect[i] = false;
            }

        }

        @Override
        public int getCount() {
            return mImgIds.size();
        }

        @Override
        public Object getItem(int position) {
            return mMapItemViews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridItemView item;

            item = new GridItemView(mContext);
            item.setLayoutParams(new GridView.LayoutParams(MainActivity.mThumbnailWidth, MainActivity.mThumbnailWidth));
            item.setItemImageScaleType(ImageView.ScaleType.CENTER_CROP);

            item.setTag(mImgIds.get(position));
            String imageKey = String.valueOf(mImgIds.get(position));
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

                LoadBitmapFromMemTask task = new LoadBitmapFromMemTask(mTaskCollection,mMemoryCache,mGridView,mIsImageSelected);
                mTaskCollection.add(task);
                task.execute(mImgIds.get(position), position);
            }

            if(!mMapItemViews.containsKey(position)) {
                mMapItemViews.put(position, item);
                Log.e("e","position="+position+"  "+item.toString());
            }
            else
            {
                mMapItemViews.remove(position);
                mMapItemViews.put(position, item);
                Log.e("e","position="+position+"  "+item.toString());
            }

            return item;
        }
    }


}
