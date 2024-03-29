package com.zmtmt.zhibohao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.zmtmt.zhibohao.entity.LiveRoomInfo;
import com.zmtmt.zhibohao.entity.Products;
import com.zmtmt.zhibohao.entity.ShareInfo;
import com.zmtmt.zhibohao.tools.ShareUtils;
import com.zmtmt.zhibohao.tools.Utils;
import com.zmtmt.zhibohao.widget.CustomDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.webkit.WebSettings.LOAD_NO_CACHE;


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

@SuppressLint("JavascriptInterface")
public class WebActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WebActivity";

    private static final String URL = "http://www.zipindao.tv/app/index.php?c=entry&m=wg_test&i=4&do=applogin";
    private WebView mWebView;
    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private ProgressBar mProgressBar;
    private PopupWindow pop;
    private RelativeLayout rl_back, rl_share;
    private String url_title;//当前页面标题
    private TextView tv_url_title;
    private LiveRoomInfo lri;//直播房间信息
    private Products products;
    private ArrayList<Products> pList;
    private WindowManager.LayoutParams params;
    private final int WXSceneTimeline = 1;//朋友圈
    private final int WXSceneSession = 2;//好友
    private Bitmap bitmap = null;//分享的logo
    private int versionCode;
    private TextView tv_cancel;

    private ShareInfo shareInfo;
    private List<ShareInfo> sList = new ArrayList<>();
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        MyApplication.list.add(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*    //TODO  新加功能
    public void startAudioLive(View view) {
        Intent intent = new Intent(this, AudioActivity.class);
        startActivity(intent);
    }*/

    /**
     * 初始化
     */
    private void initViews() {
        versionCode = BuildConfig.VERSION_CODE;
        mWebView = (WebView) findViewById(R.id.wv);
        tv_url_title = (TextView) findViewById(R.id.tv_url_title);
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        rl_back.setOnClickListener(this);
        rl_share = (RelativeLayout) findViewById(R.id.rl_share);
        rl_share.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        WebSettings settings = mWebView.getSettings();
        //开启javascript
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JsToJava(), "app");
        //设置支持缩放
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);//设置隐藏缩放按钮
        //将图片调整到适合webview的大小
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        //屏幕自适应网页  解决低分辨率可能会显示异常的问题
        settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //自适应屏幕
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //设置缓存机制
        //settings.setAppCacheEnabled(true);
        settings.setCacheMode(LOAD_NO_CACHE);
        //设置在webview内部跳转
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        //设置webview禁止长按出现裁剪
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 长按事件监听（注意：需要实现LongClickCallBack接口并传入对象）
                final WebView.HitTestResult htr = mWebView.getHitTestResult();//获取所点击的内容
                if (htr.getType() == WebView.HitTestResult.IMAGE_TYPE) {//判断被点击的类型为图片
                    // 获取到图片地址后做相应的处理
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            saveMyBitmap(Utils.getBitmapByUrl(htr.getExtra()), "code");
                        }
                    }.start();
                    showDialog();
                } else {
                    //屏蔽长按出现编辑项
                }
                return true;
            }
        });
        mWebView.loadUrl(URL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                }
                break;

            case R.id.rl_back:
                if (mWebView.canGoBack() && !mWebView.getUrl().contains("i=4")) {
                    mWebView.goBack();
                } else {
                    Utils.showToast(WebActivity.this, getString(R.string.w_tip));
                }
                break;

            case R.id.rl_share:
                showPopupWindow();
                break;

            case R.id.ll__share_circle:
                if (sList.size() != 0) {
                    ShareUtils.shareToWX(shareInfo, WXSceneTimeline);
                } else {
                    Utils.showToast(WebActivity.this, getString(R.string.w_shareTip));
                }
                break;

            case R.id.ll_share_friend:
                if (sList.size() != 0) {
                    ShareUtils.shareToWX(shareInfo, WXSceneSession);
                } else {
                    Utils.showToast(WebActivity.this, getString(R.string.w_shareTip));
                }
                break;
        }
    }

    public void showPopupWindow() {
        //当分享窗口弹出的时候设置主窗口的背景为50%的透明度，窗口消失的时候恢复
        params = WebActivity.this.getWindow().getAttributes();
        params.alpha = 0.5f;
        WebActivity.this.getWindow().setAttributes(params);
        LayoutInflater layoutInflater = LayoutInflater.from(WebActivity.this);
        View pop_share_view = layoutInflater.inflate(R.layout.pop_share_layout, null);
        pop = new PopupWindow(pop_share_view);
        pop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        pop.setFocusable(true);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setAnimationStyle(R.style.ShareAnimation);
        pop.setOutsideTouchable(true);
        View ll = layoutInflater.inflate(R.layout.activity_webview, null);
        pop.showAtLocation(ll, Gravity.BOTTOM, 0, 0);
        LinearLayout ll__share_circle = (LinearLayout) pop_share_view.findViewById(R.id.ll__share_circle);
        ll__share_circle.setOnClickListener(this);
        LinearLayout ll_share_friend = (LinearLayout) pop_share_view.findViewById(R.id.ll_share_friend);
        ll_share_friend.setOnClickListener(this);
        tv_cancel = (TextView) pop_share_view.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(WebActivity.this);

        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!pop.isShowing()) {
                    params.alpha = 1.0f;
                    WebActivity.this.getWindow().setAttributes(params);
                }
            }
        });
    }

    class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            mWebView.requestFocus();
            mWebView.requestFocusFromTouch();
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            sList.clear();
            mProgressBar.setVisibility(View.VISIBLE);
        }


        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            mProgressBar.setVisibility(View.GONE);
            tv_url_title.setText(webView.getTitle());
        }

        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {
            super.onReceivedError(webView, i, s, s1);
            Utils.showToast(WebActivity.this, "后台数据走丢啦! ");
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            WebActivity.this.startActivityForResult(Intent.createChooser(i, "选择上传文件"), FILECHOOSER_RESULTCODE);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            WebActivity.this.startActivityForResult(
                    Intent.createChooser(i, "选择上传文件"),
                    FILECHOOSER_RESULTCODE);
        }

        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            WebActivity.this.startActivityForResult(Intent.createChooser(i, "选择上传文件"), WebActivity.FILECHOOSER_RESULTCODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
        } else if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Utils.showToast(this, getString(R.string.w_exit_app));
                exitTime = System.currentTimeMillis();
            } else {
                for (int i = 0; i < MyApplication.list.size(); i++) {
                    MyApplication.list.get(i).finish();
                }
            }
        }
        return true;
    }

    private ArrayAdapter<String> adapter;
    private File file;

    private CustomDialog mCustomDialog;

    /**
     * 显示Dialog
     * param v
     */
    private void showDialog() {
        initAdapter();
        mCustomDialog = new CustomDialog(this) {
            @Override
            public void initViews() {
                // 初始CustomDialog化控件
                ListView mListView = (ListView) findViewById(R.id.lv_dialog);
                mListView.setAdapter(adapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // 点击事件
                        switch (position) {
                            case 0:
                                sendToFriends();//把图片发送给好友
                                closeDialog();
                                break;
                            case 1:
                                saveImageToGallery(WebActivity.this);
                                Utils.showToast(WebActivity.this, "成功保存到图库");
                                closeDialog();
                                break;
                            case 2:
                                Utils.showToast(WebActivity.this, "已收藏");
                                closeDialog();
                                break;
                        }

                    }
                });
            }
        };
        mCustomDialog.show();
    }

    /**
     * 初始化数据
     */
    private void initAdapter() {
        adapter = new ArrayAdapter<String>(this, R.layout.item_dialog);
        adapter.add("发送给朋友");
        adapter.add("保存到手机");
        adapter.add("收藏");
    }

    /**
     * 发送给好友
     */
    private void sendToFriends() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri imageUri = Uri.parse(file.getAbsolutePath());
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    /**
     * bitmap 保存为jpg 图片
     *
     * @param mBitmap 图片源
     * @param bitName 图片名
     */
    public void saveMyBitmap(Bitmap mBitmap, String bitName) {
        file = new File(Environment.getExternalStorageDirectory() + "/" + bitName + ".jpg");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 先保存到本地再广播到图库
     */
    public void saveImageToGallery(Context context) {

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), "code", null);
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
                    + file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给javaScript调用的类以及方法
     */
    public class JsToJava {
        /***
         * @param s liveid
         */
        @JavascriptInterface
        public void openCamera(String s) {
                Intent in = new Intent(WebActivity.this, CameraActivity.class);
                in.putExtra("pushurl", lri.getPushUrl());
                in.putExtra("eventurl", lri.getEventUrl());
                in.putExtra("openid", lri.getOpenId());
                in.putExtra("memberlevelid", lri.getMemberlevelid());
                in.putParcelableArrayListExtra("products_list", pList);
                shareInfo.setLiveId(s);
                in.putExtra("shareinfo", shareInfo);
                startActivity(in);
//            Logger.t(TAG).d(shareInfo);
        }

        @JavascriptInterface
        public void setProfile(String json) {//base属性 + img属性  拼接去取图片  goods是JSONobject
            if (json != null) {
                try {
                    pList = new ArrayList<Products>();
                    //解析json
                    JSONObject object = new JSONObject(json);
                    lri = new LiveRoomInfo();
                    lri.setuId(object.getString("uid"));
                    lri.setAcId(object.getString("acid"));
                    lri.setNickName(object.getString("nickname"));
                    lri.setOpenId(object.getString("openid"));
                    lri.setEventUrl(object.getString("eventurl"));
                    lri.setPushUrl(object.getString("pushurl"));
                    lri.setRoomImgUrl(object.getString("roomimg"));
                    lri.setMemberlevelid(String.valueOf(object.getInt("memberlevelid")));
                    String goods = object.getString("goods");
                    JSONArray object_goods = new JSONArray(goods);
                    for (int i = 0; i < object_goods.length(); i++) {
                        JSONObject object_good = object_goods.getJSONObject(i);
                        products = new Products();
                        products.setProducts_id(object_good.getString("id"));
                        products.setProducts_name(object_good.getString("name"));
                        products.setProducts_price(object_good.getString("price"));
                        products.setProducts_icon(object.getString("base") + object_good.getString("img"));
                        pList.add(products);
                    }
                    Logger.t(TAG).json(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @JavascriptInterface
        public String getToken() {
            String unionId = getSharedPreferences("unionId", WebActivity.MODE_PRIVATE).getString("unionId", "");
            return unionId;
        }

        @JavascriptInterface
        public void setShare(String json) {
            try {
                JSONObject object = new JSONObject(json);
                shareInfo = new ShareInfo();
                shareInfo.setTitle(object.getString("title"));
                shareInfo.setDesc(object.getString("desc"));
                shareInfo.setLink(object.getString("link"));
                shareInfo.setImgUrl(object.getString("imgUrl"));
                sList.add(shareInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Logger.t("SHAREINFO").d(shareInfo.toString() + sList.size() + "");
        }

        @JavascriptInterface
        public int getVersion() {
            return versionCode;
        }
    }
}
