package com.zmtmt.zhibohao.tools;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/18.
 */

public class PagerAdapder extends PagerAdapter {
    private ArrayList<ImageView> mImageViews;

    public PagerAdapder(ArrayList<ImageView> imageViews) {
        this.mImageViews = imageViews;
    }

    @Override
    public int getCount() {
        return mImageViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = mImageViews.get(position);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mImageViews.get(position));
    }
}
