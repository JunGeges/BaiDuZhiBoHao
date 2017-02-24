package com.zmtmt.zhibohao.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author gaojun
 * @version 2.0
 * Created by Administrator on 2016/7/28.
 */


/**
 * 　　　　　　　　┏┓　　　┏┓+ +
 * 　　　　　　　┏┛┻━━━┛┻┓ + +
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃ ++ + + +
 * 　　　　　　 ████━████ ┃+
 * 　　　　　　　┃　　　　　　　┃ +
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　　┃ + +
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃ + + + +
 * 　　　　　　　　　┃　　　┃　　　　Code is far away from bug with the animal protecting
 * 　　　　　　　　　┃　　　┃ + 　　　　神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　+
 * 　　　　　　　　　┃　 　　┗━━━┓ + +
 * 　　　　　　　　　┃ 　　　　　　　┣┓
 * 　　　　　　　　　┃ 　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛+ + + +
 */

public class Utils {

    public static Toast mToast;

    //Toast的简单封装
    public static void showToast(Context context, String conetnt) {
        if (mToast == null) {
            mToast = Toast.makeText(context, conetnt, Toast.LENGTH_LONG);
        } else {
            mToast.setText(conetnt);
        }
        mToast.show();
    }

    //网络请求返回json  GET
    @Nullable //表示定义的字段可以为空
    public static String get(String url) {
        BufferedReader bufferedReader=null;
        try {
            URL urls = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            if (connection.getResponseCode() == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(is));
                String str;
                StringBuffer sb = new StringBuffer();
                while ((str = bufferedReader.readLine()) != null) {
                    sb.append(str);
                }
                connection.disconnect();
                is.close();
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (bufferedReader!=null){
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //网络请求 POST
    public static String post(String url, Map<String, String> params) {
        BufferedReader bufferedReader = null;
        try {
            URL urls = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            //设置允许向服务器获取数据
            conn.setDoInput(true);
            if (params != null) {
                //设置允许向服务器提交数据
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                //请求的数据
                String request = "";
                for (String key : params.keySet()) {
                    request += key + "=" + params.get(key) + "&";
                }
                //多了一个拼接符
                String out = request.substring(0, request.length() - 1);
                os.write(out.getBytes());
                os.flush();
                os.close();
            }
            if (conn.getResponseCode() == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String response;
                while ((response = bufferedReader.readLine()) != null) {
                    sb.append(response);
                }
                return sb.toString();
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param url 请求的url地址
     * @return 返回的流转换为Bitmap对象
     */
    public static Bitmap getBitmapByUrl(String url) {
        Bitmap bitmap = null;
        try {
            URL urls = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                conn.disconnect();
                is.close();
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //缓存类
    public LruCache<String, Bitmap> mLruCache;

    public Utils() {
        //获得可用最大的内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存时调用
                return value.getByteCount();
            }
        };
    }

    //添加图片到内存
    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmap(url) == null) {
            mLruCache.put(url, bitmap);
        }
    }

    //从内存中获取图片
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = mLruCache.get(url);
        return bitmap;
    }


    /**
     * 异步加载图片
     */
    public void showImageFromAsyncTask(ImageView imageView, String url, Context context) {
        //加载图片之前先从缓存中取 看是否有缓存的图片  如果有直接设置到控件上面 如果没有就联网加载
        Bitmap bitmap = getBitmap(url);
        if (bitmap == null) {
            new ImageAsyncTask(imageView, url, context).execute(url);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            //圆形图片宽高
            int width = bitmap.getWidth() / 6;
            int height = bitmap.getHeight() / 6;
            //正方形的边长
            int r = 0;
            //取最短边做边长
            if (width > height) {
                r = height;
            } else {
                r = width;
            }
            //构建一个bitmap
            Bitmap backgroundBmp = Bitmap.createBitmap(width,
                    height, Bitmap.Config.ARGB_8888);
            //new一个Canvas，在backgroundBmp上画图
            Canvas canvas = new Canvas(backgroundBmp);
            Paint paint = new Paint();
            //设置边缘光滑，去掉锯齿
            paint.setAntiAlias(true);
            //宽高相等，即正方形
            RectF rect = new RectF(0, 0, r, r);
            //通过制定的rect画一个圆角矩形，当圆角X轴方向的半径等于Y轴方向的半径时，
            //且都等于r/2时，画出来的圆角矩形就是圆形
            canvas.drawRoundRect(rect, r / 2, r / 2, paint);
            //设置当两个图形相交时的模式，SRC_IN为取SRC图形相交的部分，多余的将被去掉
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            //canvas将bitmap画在backgroundBmp上
            canvas.drawBitmap(bitmap, null, rect, paint);
            //返回已经绘画好的backgroundBmp
            return backgroundBmp;
        }
        return null;
    }

    public static void saveToSp(Context context,String paramName,String[] keys,String... values){
        SharedPreferences sp=context.getSharedPreferences(paramName,context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        for (int i=0;i<keys.length;i++){
            edit.putString(keys[i],values[i]);
        }
        edit.apply();
    }

    public static void saveToSp(Context context, String key, boolean isFirst) {
        SharedPreferences sp = context.getSharedPreferences("isFirstRecord", Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, isFirst).commit();
    }

    /**
     * 异步加载图片类
     */
    class ImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView mImageView;
        private String mUrl;
        private Context mContext;

        public ImageAsyncTask(ImageView imageView, String url, Context context) {
            this.mImageView = imageView;
            this.mUrl = url;
            this.mContext = context;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            //把图片添加到缓存
            Bitmap bitmap = getBitmapByUrl(strings[0]);
            if (bitmap != null) {
                addBitmapToCache(strings[0], bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mImageView.getTag().equals(mUrl)) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    //判断手机摄像头石头支持闪光灯
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))
                        return true;
                }
            }
        }
        return false;
    }
}
