package com.picturecapture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String WEB_ADDRESS = "web_address";
    private EditText mWebAddress;
    private Toolbar mToolbar;
    private Button mCapture;
    public static int  mThumbnailWidth;
    public static Database mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mThumbnailWidth = (dm.widthPixels-4)/3;  //缩略图的宽度，每行3张图片

        mWebAddress = (EditText)findViewById(R.id.web_address);
        mToolbar = (Toolbar)findViewById(R.id.main_toolbar);

        mToolbar.setTitle("Capture");
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intentForPicLib = new Intent(MainActivity.this,PictureLibActivity.class);
                startActivity(intentForPicLib);
                return true;
            }
        });
        mCapture = (Button)findViewById(R.id.capture);
        mCapture.setOnClickListener(this);

        mDatabase = new Database(this);
        mDatabase.init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture:
                String webAddress=" ";
                webAddress = mWebAddress.getText().toString();
                Log.e("e",webAddress);

                if(!webAddress.equals(" ")) {
                    Intent intentForCapture = new Intent(this, CaptureActivity.class);
                    intentForCapture.putExtra(WEB_ADDRESS, webAddress);
                    intentForCapture.putExtra("name", "liu");
                    startActivity(intentForCapture);
                }
                break;
        }
    }
}
