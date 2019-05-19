package com.example.bannerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bannerdemo.widget.AutoSlideView;

import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private AutoSlideView mAsvBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initBanner();
    }

    private void initBanner() {
        mAsvBanner.init(true);

        Integer[] pics = new Integer[3];
        //轮播图
        for (int i = 0; i <3; i++) {
            pics[i] = R.mipmap.timg;
        }

        mAsvBanner.setData(this, pics, new AutoSlideView.BannerPhotoListener() {

            @Override
            public void setImageResource(List<View> items, ImageView imageView, int imageUrl) {
                imageView.setImageResource(imageUrl);
            }

            @Override
            public void onItemClick(int position) {

                try {
                    Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });
        mAsvBanner.startScroll();
    }

    private void initView() {
        mAsvBanner = findViewById(R.id.asv_banner);
    }
}
