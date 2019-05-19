package com.example.bannerdemo.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;


import com.example.bannerdemo.R;
import com.example.bannerdemo.utils.DisplayUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by luwei on 2019/3/16
 */

public class AutoSlideView extends FrameLayout implements ViewPager.OnPageChangeListener {

    private static final int MSG_UPDATE_IMAGE = 1;
    private static final int MSG_PAGE_CHANGED = 2;
    private static final int MSG_PAGE_UPDATE = 3;
    private static final int INTERVAL = 2000;
    private int mWidth;
    private int MAX_VALUE = Integer.MAX_VALUE;
    private int mTabCount;

    private ViewPager mViewPager;
    private Context mContext;
    private ViewPagerAdapter mViewPagerAdapter;
    private List<View> mItems;
    private boolean canScroll;
    private boolean isRestore;//是否还原  解决之前修复只有两个item时手动添加两个，还原位置问题

    private BannerPhotoListener mBannerPhotoListener;

    private Handler mHandler = new Handler() {
        private int currentItem = 0;

        @Override
        public void handleMessage(Message msg) {
            if (this.hasMessages(MSG_UPDATE_IMAGE) && currentItem != 0) {
                this.removeMessages(MSG_UPDATE_IMAGE);
            }
            switch (msg.what) {
                case MSG_UPDATE_IMAGE:

                    currentItem++;

                    mViewPager.setCurrentItem(currentItem);
                    break;
                case MSG_PAGE_CHANGED:
                    currentItem = msg.arg1;
                    break;
                case MSG_PAGE_UPDATE:
                    currentItem = 0;
                    break;
            }
        }
    };
    private View mVTab;
    private View mVTabZero;
    private RelativeLayout mRlBannerBottom;

    public AutoSlideView(@NonNull Context context) {
        this(context, null);
    }

    public AutoSlideView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public AutoSlideView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mWidth = DisplayUtils.getScreenWidth(mContext);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(mContext).inflate(R.layout.auto_slide_view_layout, null);
        this.mViewPager = (ViewPager) view.findViewById(R.id.banner);
        this.mVTab = (View) view.findViewById(R.id.v_tab);
        this.mVTabZero = (View) view.findViewById(R.id.v_tab_zero);
        this.mRlBannerBottom = (RelativeLayout) view.findViewById(R.id.rl_banner_bottom);
        this.mViewPager.setOnPageChangeListener(this);
        addView(view);
    }

    /**
     * 需要暴露给用户使用的方法
     *
     * @param canScroll
     */
    public void init(boolean canScroll) {

        this.canScroll = canScroll;

        if (mItems == null) {
            mItems = new ArrayList<>();
        }

        if (mViewPagerAdapter == null) {
            mViewPagerAdapter = new ViewPagerAdapter(mItems, MAX_VALUE);
            mViewPager.setAdapter(mViewPagerAdapter);
            mViewPager.setOffscreenPageLimit(1);
            controlViewPagerSpeed(mContext, mViewPager, 1000);
        } else {
            mViewPagerAdapter.notifyDataSetChanged();
        }


    }

    public void setData(Context context, String[] photoList, BannerPhotoListener bannerPhotoListener) {

        mBannerPhotoListener = bannerPhotoListener;

        //数据层改动
        mItems.clear();

        //修复图片个数为2时左滑出现空白情况
        int lengh = 1;
        isRestore = false;
        if (photoList.length == 2) {
            lengh = 2;
            isRestore = true;
        }
        for (int i = 0; i < photoList.length * lengh; i++) {
            ImageView iv = new ImageView(context);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            mItems.add(iv);

            bannerPhotoListener.setImageResource(mItems, iv, photoList[i % photoList.length]);
        }

        //通知UI层
        mViewPagerAdapter.notifyDataSetChanged();

        mTabCount = photoList.length;
        if (mTabCount <= 0) {
            return;
        }

        LayoutParams layoutParams = (LayoutParams) mVTab.getLayoutParams();
        layoutParams.width = mWidth / mTabCount;
        mVTab.setLayoutParams(layoutParams);
        mVTab.setTranslationX(0);
        LayoutParams layoutParamsZero = (LayoutParams) mVTabZero.getLayoutParams();
        layoutParamsZero.width = mWidth / mTabCount;
        mVTabZero.setLayoutParams(layoutParamsZero);
        mVTabZero.setTranslationX(-mWidth / mItems.size());

        mRlBannerBottom.setVisibility(mTabCount == 1 ? View.GONE : View.VISIBLE);
        mVTab.setVisibility(mTabCount == 1 ? View.GONE : View.VISIBLE);
        mVTabZero.setVisibility(mTabCount == 1 ? View.GONE : View.VISIBLE);

    }

    public void cancel() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        if (mTabCount <= 0) {
            return;
        }

        position = position % mTabCount;
        int offset = (int) (positionOffsetPixels * 1.0f / mTabCount + position * 1.0f * mWidth / mTabCount);
        mVTab.setTranslationX(offset);

        if (position == (mTabCount - 1) && positionOffsetPixels > 0) {
            mVTabZero.setVisibility(View.VISIBLE);
        } else {
            mVTabZero.setVisibility(View.GONE);
        }
        if (position == mTabCount - 1 && offset > mWidth / mTabCount * position) {
            mVTabZero.setTranslationX(positionOffsetPixels * 1.0f / mTabCount - mWidth / mTabCount);
        }

    }

    @Override
    public void onPageSelected(int position) {
        recordPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING:
                pauseScroll();
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                keepScroll();
                break;
        }
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int i, float v, int i1);

        void onPageSelected(int position);
    }

    /**
     * 轮播相关方法
     *
     * @param pos
     */
    private void recordPosition(int pos) {
        if (mHandler != null) {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_PAGE_CHANGED, pos, 0));
        }
    }

    private void pauseScroll() {
        if (!canScroll) {
            return;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void keepScroll() {
        if (!canScroll) {
            return;
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, INTERVAL);
        }
    }

    public void startScroll() {
        if (!canScroll) {
            return;
        }
        if (mHandler == null) {
            return;
        }
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mHandler.hasMessages(MSG_UPDATE_IMAGE)) {
                    mHandler.removeMessages(MSG_UPDATE_IMAGE);
                }
                keepScroll();
            }
        });
    }

    /**
     * 自定义PagerAdapter
     */
    class ViewPagerAdapter extends PagerAdapter {

        private List<View> items;
        private int maxValue;

        public ViewPagerAdapter(List<View> items, int maxValue) {
            this.items = items;
            this.maxValue = maxValue;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position %= items.size();
            if (position < 0) {
                position = items.size() + position;
            }
            View view = items.get(position);
            ViewParent viewParent = view.getParent();
            if (viewParent != null) {
                ViewGroup parent = (ViewGroup) viewParent;
                parent.removeView(view);
            }

            final int[] finalPosition = {position};

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mBannerPhotoListener != null) {

                        if (isRestore) {
                            finalPosition[0] %= 2;
                        }

                        mBannerPhotoListener.onItemClick(finalPosition[0]);
                    }

                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            if (items.size() > 1) {
                return maxValue;
            }
            return items.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * 反射 修改滑动速度
     */
    private FixedSpeedScroller mScroller = null;

    //设置ViewPager的滑动时间
    private void controlViewPagerSpeed(Context context, ViewPager viewpager, int DurationSwitch) {
        try {
            Field mField;

            mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);

            mScroller = new FixedSpeedScroller(context);
            mScroller.setmDuration(DurationSwitch);
            mField.set(viewpager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FixedSpeedScroller extends Scroller {
        private int mDuration = 1500; // 默认滑动速度 1500ms

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        /**
         * set animation time
         *
         * @param time
         */
        public void setmDuration(int time) {
            mDuration = time;
        }

        /**
         * get current animation time
         *
         * @return
         */
        public int getmDuration() {
            return mDuration;
        }
    }


    public interface BannerPhotoListener {

        void setImageResource(List<View> mItems, ImageView imageView, String imageUrl);

        void onItemClick(int position);
    }

}
