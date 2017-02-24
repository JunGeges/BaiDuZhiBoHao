package com.zmtmt.zhibohao;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.recorder.api.LiveConfig;
import com.baidu.recorder.api.LiveSession;
import com.baidu.recorder.api.LiveSessionHW;
import com.baidu.recorder.api.LiveSessionSW;
import com.baidu.recorder.api.SessionStateListener;
import com.orhanobut.logger.Logger;
import com.zmtmt.zhibohao.entity.Comment;
import com.zmtmt.zhibohao.entity.CommentContent;
import com.zmtmt.zhibohao.entity.Products;
import com.zmtmt.zhibohao.entity.ShareInfo;
import com.zmtmt.zhibohao.tools.CommentAdapter;
import com.zmtmt.zhibohao.tools.NetWorkType;
import com.zmtmt.zhibohao.tools.ProductsAdapter;
import com.zmtmt.zhibohao.tools.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.zmtmt.zhibohao.tools.ShareUtils.shareToWX;

//import com.baidu.recorder.api.LiveConfig;


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

public class CameraActivity extends FragmentActivity implements View.OnClickListener, Runnable,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final String TAG = "CameraActivity";
    private static final String KEY_STREAM = "stream";
    private static final String KEY_RESOLUTION = "resolution";
    private static final String KEY_ORIENTATION = "orientation";

    private static final String UPGRADE_URL = "http://app.zmtmt.com/down/zhibohao.json";
    //录制连接
    private static final int UI_EVENT_RECORDER_CONNECTING = 0;
    private static final int UI_EVENT_RECORDER_STARTED = 1;//录制开始
    private static final int UI_EVENT_RECORDER_STOPPED = 2;//录制停止
    private static final int UI_EVENT_SESSION_PREPARED = 3;//准备会话
    private static final int UI_EVENT_HIDE_FOCUS_ICON = 4;//隐藏焦点图片
    private static final int UI_EVENT_RESTART_STREAMING = 5;
    private static final int UI_EVENT_RECONNECT_SERVER = 6;//重新连接推流
    private static final int UI_EVENT_STOP_STREAMING = 7;//停止推流
    private static final int UI_EVENT_SHOW_TOAST_MESSAGE = 8;//弹窗显示
    private static final int UI_EVENT_RESIZE_CAMERA_PREVIEW = 9;//预览相机重新调整的尺寸大小
    //开始计时
    private static final int UI_EVENT_START_TIME = 10;
    //评论
    private static final int UI_EVENT_GET_COMMENT = 11;
    //推流时间
    private static final int UI_EVENT_RECORD_TIME = 12;

    private static final int UI_EVENT_STOP_COUNT_TIME = 13;

    private boolean mflag;
    private TextView mtv_time_s, mtv_time_m, mtv_time_h;//时间文本
    private TextView mTv_logo;
    private TextView mTv_watch_person;
    private TextView mTv_band_width;
    private Thread mThread;
    private int time_s = 0;
    int time_m = 0;
    int time_h = 0;
    private LinearLayout ll_time;
    private Button btn_pop_talk;
    private Button btn_pop_pro;

    private ShareInfo mShareInfo; //分享信息带直播会话ID
    private String eventUrl;//请求地址
    private String openID;//直播用户openid
    private static final String START_STATE = "2";//开始直播状态码
    private static final String STOP_STATE = "1";//暂停直播状态码
    private static final String OPERATION = "state";
    private static final String IS_NEW = "1";//最新数据
    private String ID = "0"; //默认是0  最大评论值的ID  每次都替换 获取最新数据
    private String person = "0";//在线人数
    MenuDialog menuDialog;

    private ListView mListView;
    private ArrayList<Products> pList;
    private boolean isFirstClickFlash = true; //是否第一次开启闪光灯
    private boolean hasBueatyEffect = false;

    /**
     * 屏幕触摸事件处理类
     */
    GestureDetectorCompat mDetector;
    private Button menuBtn;//菜单按钮
    private SurfaceView mCameraView;
    private Button swtBtn;//前后摄像头切换按钮
    private Button flashBtn;//闪光灯切换按钮
//    private Button effectBtn;//美颜效果
    private Button mRecorderButton;//录制按钮
    private Button shareBtn; //分享按钮
    private ProgressBar mLoadingAnimation;
    private int mCurrentCamera = -1;
    private LiveSession mLiveSession = null;//直播
    private SessionStateListener mStateListener = null;//会话准备监听
    private Handler mUIEventHandler = null;//消息处理事件
    private int mVideoWidth = 1280;
    private int mVideoHeight = 720;
    private int mFrameRate = 15;
    private int mBitrate = 1024000;
    private int mWeakConnectionHintCount = 0;
    private boolean isSessionReady = false;
    private boolean isSessionStarted = false;
    private boolean isConnecting = false;
    private boolean needRestartAfterStopped = false;

    private ImageView guide_view;
    private ImageView mFoucsIcon;
    private RelativeLayout rl_guide_view;
    private String currentTime = null;
    private boolean isFirstPush = true;

    private final int WXSceneTimeline = 1;
    private final int WXSceneSession = 2;
    private View c_pop_talk_empty_tip;
    private PopupWindow pop_products;
    private ListView pop_products_listview;
    private ImageView iv_time_switch;
    private int count = 0;//计时旁边的闪烁的指示灯次数

    private ArrayList<Comment> cList = new ArrayList<Comment>();
    int firstSize = 0;
    int lastSize = 0;
    private String pushurl = "rtmp://push.bj.bcelive.com/live/gfwr7avf1mspq406haq";//推流地址
    /**
     * 会员等级
     */
    private int memberlevelid;
    private Timer timer_comment = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d("onCrate");

        //设置保持屏幕处于亮的状态
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        //时间子线程的初始化
        mThread = new Thread(this);

        //设置分辨率
        setResolutionConfig(getResolution());
        Utils.showToast(this, getResolution());
        getData();
        initUIElements();//初始化组件元素
        mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        initUIEventHandler();//消息处理方法
        initStateListener();//直播准备开始的监听事件
        initRTMPSession(mCameraView.getHolder());//初始化推流配置参数等
        initGuideView();//初始化引导view
        int netWorkType = NetWorkType.getNetWorkType(this);
        if (netWorkType == NetWorkType.NETWORKTYPE_WIFI) {
            Utils.showToast(this, "当前的网络为wifi");
        }
    }

    /*获取上一个Activity带过来的相关参数*/
    public void getData() {
        Intent intent = getIntent();
        pushurl = intent.getStringExtra("pushurl");
        eventUrl = intent.getStringExtra("eventurl");
        openID = intent.getStringExtra("openid");
        memberlevelid = Integer.parseInt(intent.getStringExtra("memberlevelid"));
        Logger.t(TAG).d(eventUrl + "liveconsumeajax" + pushurl);
        pList = intent.getParcelableArrayListExtra("products_list");
        mShareInfo = intent.getParcelableExtra("shareinfo");
        Logger.t(TAG).d(mShareInfo);
    }

    /*初始化蒙版效果图*/
    private void initGuideView() {
        SharedPreferences sp = getSharedPreferences("isFirstRecord", MODE_PRIVATE);
        boolean isFirsts = sp.getBoolean("isFirst", false);
        if (!isFirsts) {
            isFirsts = true;
            Utils.saveToSp(this, "isFirst", isFirsts);
            guide_view.setOnClickListener(this);
        } else {
            guide_view.setVisibility(View.GONE);
            rl_guide_view.setVisibility(View.GONE);
        }
    }

    /**
     * 评论弹窗
     */
    private PopupWindow pop_comment;

    private void initUIElements() {
        mLoadingAnimation = (ProgressBar) findViewById(R.id.progressBar);
        mRecorderButton = (Button) findViewById(R.id.btn_start_capture);
        mRecorderButton.setEnabled(false);
        mCameraView = (SurfaceView) findViewById(R.id.cameraView);
        this.swtBtn = ((Button) findViewById(R.id.btn_switch_camera));
        this.menuBtn = ((Button) findViewById(R.id.btn_menu));
        flashBtn = (Button) findViewById(R.id.btn_flash);
//        effectBtn = (Button) findViewById(R.id.btn_effect);
        shareBtn = (Button) findViewById(R.id.btn_share);

        mtv_time_s = (TextView) findViewById(R.id.tv_time_second);
        mtv_time_m = (TextView) findViewById(R.id.tv_time_minute);
        mtv_time_h = (TextView) findViewById(R.id.tv_time_hour);
        mTv_watch_person = (TextView) findViewById(R.id.tv_watch_person);
        ll_time = (LinearLayout) findViewById(R.id.ll_time);
        btn_pop_pro = (Button) findViewById(R.id.btn_pro);
        btn_pop_talk = (Button) findViewById(R.id.btn_talk);
        guide_view = (ImageView) findViewById(R.id.iv_guide);
        rl_guide_view = (RelativeLayout) findViewById(R.id.rl_guide);
        rl_guide_view.setOnClickListener(this);
        mFoucsIcon = (ImageView) findViewById(R.id.iv_ico_focus);

        btn_pop_talk.setOnClickListener(this);
        btn_pop_pro.setOnClickListener(this);
        this.swtBtn.setOnClickListener(this);
        this.mRecorderButton.setOnClickListener(this);
        this.menuBtn.setOnClickListener(this);
        flashBtn.setOnClickListener(this);
//        effectBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        iv_time_switch = (ImageView) findViewById(R.id.iv_time_switch);

        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        //logo设置区域
        mTv_logo = (TextView) findViewById(R.id.tv_logo);

        mTv_band_width=(TextView)findViewById(R.id.tv_band_width);

        //微信登录后存的用户信息
        SharedPreferences sp = getSharedPreferences("WXUserParams", MODE_PRIVATE);
        final String wxLogoUrl = sp.getString("WXLogoUrl", null);
        final String wxNickName = sp.getString("WXNickName", null);
        //通过请求加载得到的图片
        new Thread() {
            @Override
            public void run() {
                super.run();
                final Bitmap bitmap = Utils.getBitmapByUrl(wxLogoUrl);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap roundBitmap = Utils.toRoundBitmap(bitmap);
                        Drawable drawable = new BitmapDrawable(getResources(), roundBitmap);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        mTv_logo.setText(wxNickName);
                        mTv_logo.setCompoundDrawables(drawable, null, null, null);
                        mTv_logo.setCompoundDrawablePadding(18);
                        mTv_logo.setVisibility(View.VISIBLE);
                    }
                });
            }
        }.start();

        mTv_logo.post(new Runnable() {
            @Override
            public void run() {
                //获取窗口管理类
                LayoutInflater layoutInflater = LayoutInflater.from(CameraActivity.this);
                View pop_talk_view = layoutInflater.inflate(R.layout.pop_talk_layout, null);
                c_pop_talk_empty_tip = pop_talk_view.findViewById(R.id.c_pop_talk_empty_tip);
                pop_comment = new PopupWindow(pop_talk_view);
                pop_comment.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                pop_comment.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                //聚焦内部控件
                pop_comment.setFocusable(true);
                //设置pop背景
                pop_comment.setBackgroundDrawable(new ColorDrawable());
                //设置弹窗进入的动画
                pop_comment.setAnimationStyle(R.style.AnimationFade);
                mListView = (ListView) pop_talk_view.findViewById(R.id.lv_pop_comment);
                getComment();

                //设置点击外部隐藏pop弹窗
                pop_comment.setOutsideTouchable(true);
                //设置弹窗显示位置
                View ll = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_camera, null);
                pop_comment.showAtLocation(ll, Gravity.LEFT, 0, 0);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_menu: {
                showStreamEditor();
            }
            break;
            case R.id.btn_start_capture: {
                if (hasStreamUrl() || pushurl != null) {
                    String strUrl = "";
                    if (menuDialog != null) {
                        strUrl = menuDialog.getStreamUrl();
                    }
                    if (TextUtils.isEmpty(strUrl)) {
                        strUrl = pushurl;
                    }
                    startVideoStream(strUrl);
                }
            }
            break;
            case R.id.btn_switch_camera: {
                toggleCamera();
            }
            break;

            case R.id.btn_pro:
                //商品推荐方法
                showPopPro();
                break;
            case R.id.btn_talk:
                //评论处理方法
                showPopTalk();
                break;

            case R.id.iv_guide:
                guide_view.setVisibility(View.GONE);
                rl_guide_view.setVisibility(View.GONE);
                break;

            case R.id.rl_guide:
                //由于CmeraActivity引导页的界面图片不是全屏
                break;

            case R.id.btn_flash:
                //闪光灯切换
                toggleFlash(CameraActivity.this);
                break;

/*            case R.id.btn_effect:
                //美颜
                onClickSwitchBeautyEffect();
                break;*/

            case R.id.btn_share:
                showSharePop();
                break;

            case R.id.c_ll_share_pyq:
                shareToWX(mShareInfo, WXSceneTimeline);
                break;

            case R.id.c_ll_share_wx:
                shareToWX(mShareInfo, WXSceneSession);
                break;
        }
    }


    @Override
    protected void onResume() {
        Logger.t(TAG).d("onResume");
        super.onResume();
        if (checkNecessityPermissions()) {

        } else {
            Utils.showToast(this, getString(R.string.c_camera_permission));
        }
        blockScreenOrientation(getOrientation());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //检查权限设置是否都有  但是不知道有没有判断权限是否被拒绝
    private boolean checkNecessityPermissions() {
        String[] checkPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };
        boolean result = true;
        for (String permission : checkPermissions) {
            if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(this, permission)) {
                result = false;
                break;
            }
        }
        return result;
    }

    private void showPopTalk() {
        if (pop_comment.isShowing()) {
            pop_comment.dismiss();
        } else {
            View ll = LayoutInflater.from(this).inflate(R.layout.activity_camera, null);
            pop_comment.showAtLocation(ll, Gravity.LEFT, 0, 0);
        }
    }

    public void showSharePop() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View shareView = inflater.inflate(R.layout.pop_camera_share_layout, null);
        PopupWindow popupWindow = new PopupWindow(shareView);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        View ll = inflater.inflate(R.layout.activity_camera, null);
        popupWindow.showAtLocation(ll, Gravity.CENTER, 0, 0);
        shareView.findViewById(R.id.c_ll_share_wx).setOnClickListener(this);
        shareView.findViewById(R.id.c_ll_share_pyq).setOnClickListener(this);
    }

    private void showPopPro() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pop_products_layout = inflater.inflate(R.layout.pop_pro_layout, null);
        pop_products = new PopupWindow(pop_products_layout);
        pop_products.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        pop_products.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        pop_products.setFocusable(true);
        pop_products.setBackgroundDrawable(new ColorDrawable());
        pop_products.setOutsideTouchable(true);
        pop_products.setAnimationStyle(R.style.AnimationFade);
        pop_products_listview = (ListView) pop_products_layout.findViewById(R.id.lv_pop_products);

        Map<String, String> params = new HashMap<String,String>();
        params.put("liveid", mShareInfo.getLiveId());
        params.put("op", "goods");

        ProductsAdapter productsAdapter = new ProductsAdapter(pList, this, eventUrl + "applogin", params);
        pop_products_listview.setAdapter(productsAdapter);

        View ll = inflater.inflate(R.layout.activity_camera, null);
        pop_products.showAtLocation(ll, Gravity.LEFT, 0, 0);

        pop_products_layout.findViewById(R.id.c_pop_pro_empty_tip).setVisibility(pList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mLiveSession != null) {
            mLiveSession.focusToPosition((int) (e.getX()), (int) (e.getY()));
            mFoucsIcon.setX(e.getX() - mFoucsIcon.getWidth() / 2);
            mFoucsIcon.setY(e.getY() - mFoucsIcon.getHeight() / 2);
            mFoucsIcon.setVisibility(View.VISIBLE);
            mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_HIDE_FOCUS_ICON, 1000);
        }
        return true;
    }

    boolean flag = false;

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mLiveSession != null) {
            int maxZoomFactor = mLiveSession.getMaxZoomFactor();
            int currentZoomFactor = mLiveSession.getCurrentZoomFactor();
            if (!flag) {
                if (currentZoomFactor < maxZoomFactor) {
                    mLiveSession.zoomInCamera();
                } else {
                    Utils.showToast(this, "已经放大至最大等级");
                    flag = true;
                }
            } else {
                mLiveSession.zoomOutCamera();
                if (currentZoomFactor == 0) {
                    Utils.showToast(this, "已经缩放到最小等级");
                    flag = false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    class CommentAsyncTask extends AsyncTask<String, Void, ArrayList<Comment>> {

        @Override
        protected ArrayList<Comment> doInBackground(final String... strings) {
            firstSize = cList.size();
            Map<String, String> params = new HashMap<String, String>();
            params.put("isNew", IS_NEW);
            params.put("liveid", mShareInfo.getLiveId());
            params.put("id", ID);
            final String json = Utils.post(strings[0] + "roomlivegetajax", params);
            if (json != null) {
                try {
                    JSONArray array = new JSONArray(json);
/*                    Logger.t(TAG).d("liveid" + mShareInfo.getLiveId() + "---" + array.length());
                    Logger.t(TAG).json(json);*/
                    JSONArray array_1 = array.getJSONArray(0);
                    String isNew = array_1.getString(0);
                    ID = array_1.getString(1);
                    person = array_1.getString(2);
                    for (int i = 1; i < array.length(); i++) {
                        final Comment c = new Comment();//评论类
                        CommentContent c_content = new CommentContent();//评论内容类
                        JSONObject object = array.getJSONObject(i);
//                        Logger.t(TAG).json(object.toString());
                        //commenttype  1：普通评论 2:主播推荐商品  3：系统礼物提示  4：系统商品买卖提示
                        String comment_type = object.getString("commenttype");
                        if (comment_type.equals("1")) {
                            c_content.setCommentContent(object.getString("commentcontent"));
                            c.setComment_content(c_content);
                            c.setCommenttype(object.getString("commenttype"));
                            c.setIssystem(object.getString("issystem"));
                        } else if (comment_type.equals("2")) {//商品推荐
                            String comment_json = object.getString("commentcontent");
                            JSONObject object_comment_recommend_products = new JSONObject(comment_json);
                            c_content.setName(object_comment_recommend_products.getString("name"));
                            c.setComment_content(c_content);
                            c.setCommenttype(comment_type);
                            c.setIssystem(object.getString("issystem"));
                        } else if (comment_type.equals("3")) {//送礼物的
                            String comment_json = object.getString("commentcontent");
                            JSONObject object_comment = new JSONObject(comment_json);
                            c_content.setName(object_comment.getString("name").substring(4));//设置礼物名字
                            c.setComment_content(c_content);
                            c.setCommenttype(comment_type);
                            c.setIssystem(object.getString("issystem"));
                        } else if (comment_type.equals("4")) {//购买
                            String comment_json = object.getString("commentcontent");
                            JSONObject object_comment_products = new JSONObject(comment_json);
                            c_content.setName(object_comment_products.getString("name"));//设置购买商品的名字
                            c.setComment_content(c_content);
                            c.setCommenttype(comment_type);
                            c.setIssystem(object.getString("issystem"));
                        }
                        c.setComment_head_url(object.getString("userimg"));
                        c.setComment_nick_name(object.getString("usernickname"));
                        c.setComment_floor(object.getString("louhao"));
                        c.setComment_time(object.getString("addtime"));
                        cList.add(c);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    lastSize = cList.size();
                }
            }
            return cList;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> comments) {
            c_pop_talk_empty_tip.setVisibility(comments.size() > 0 ? View.GONE : View.VISIBLE);
            //更新在线人数
            int personFormat = Integer.parseInt(person);
            if (personFormat >= 10000) {
                int num = personFormat / 10000;
                int num2 = personFormat / 1000 % 10;
                int num3 = personFormat / 100 % 10;
                person = num + "." + num2 + num3 + "万";
            }
            mTv_watch_person.setText(person);

            if (firstSize != lastSize) {
                CommentAdapter adapter = new CommentAdapter(CameraActivity.this, comments);
                mListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isSessionStarted) {
            Utils.showToast(this, getString(R.string.c_backPressed));
        } else {
            if (getRecordTime() > 0) {
                Intent in = new Intent(this, endActivity.class);
                in.putExtra("livetime", mtv_time_h.getText().toString().trim() + ":" + mtv_time_m.getText().toString().trim() + ":" + mtv_time_s.getText().toString());
                in.putExtra("livepersons", mTv_watch_person.getText().toString().trim());
                in.putExtra("shareinfo", mShareInfo);
                startActivity(in);
                finish();
            } else {
                finish();
            }

        }
    }

    @Override
    protected void onStart() {
        Logger.t(TAG).d("onStart");
        super.onStart();
        if (timer != null) {
            timer.cancel();
        }
    }

    private int totaltime = 0;
    private Timer timer;

    @Override
    @Nullable
    protected void onStop() {
        Logger.t(TAG).d("onStop");
        super.onStop();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mUIEventHandler != null) {
                    Logger.d(totaltime);
                    Message msg = Message.obtain();
                    msg.what = UI_EVENT_STOP_COUNT_TIME;
                    mUIEventHandler.sendMessage(msg);
                    if (totaltime > 120) {
                        timer.cancel();
                        CameraActivity.this.finish();
                    }
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.t(TAG).d("onConfigurationChanged");
    }

    @Override
    protected void onDestroy() {
        Logger.t(TAG).d("onDestroy");

        /**定时器取消*/
        timer_comment.cancel();
        if (timer_time != null) {
            timer_time.cancel();
        }
        Logger.t(TAG).d("推流时间:" + getRecordTime());
        final Map<String, String> params = new HashMap<String, String>();
        params.put("op", OPERATION);
        params.put("liveid", mShareInfo.getLiveId());
        params.put("state", STOP_STATE);
        params.put("openid", openID);
        params.put("livetime", String.valueOf(getRecordTime()));
        params.put("starttime", currentTime);
        new Thread() {
            @Override
            public void run() {
                super.run();
                String post = Utils.post(eventUrl + "applogin", params);
                Logger.t(TAG).d("直播结束" + post);
            }
        }.start();

        mUIEventHandler.removeCallbacksAndMessages(null);
        if (isSessionStarted) {
            Logger.t(TAG).d("isSessionStarted:" + isSessionStarted);
            mLiveSession.stopRtmpSession();
            isSessionStarted = false;
        }
        if (isSessionReady) {
            Logger.t(TAG).d("isSessionReady:" + isSessionReady);

            mLiveSession.destroyRtmpSession();
            mLiveSession = null;
            mStateListener = null;
            mUIEventHandler = null;
            isSessionReady = false;
        }
        super.onDestroy();
    }

    //开始录制了
    private void startVideoStream(String stream) {

        stream = stream.trim();
        if (!stream.startsWith("rtmp://")) {
            Utils.showToast(this, getString(R.string.c_streamUrl_error));
            return;
        }
        if (!BuildConfig.DEBUG) {
            pushurl = stream;
        }

        if (!isSessionReady) {
            return;
        }
        if (!isSessionStarted && !TextUtils.isEmpty(pushurl)) {
            if (mLiveSession.startRtmpSession(pushurl)) {
                blockScreenOrientation(true);
                mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
                Logger.t(TAG).d("Starting Streaming in right state!");
            } else {
                Logger.t(TAG).d("Starting Streaming in wrong state!");
            }
        } else {
            if (mLiveSession.stopRtmpSession()) {
                Logger.t(TAG).d("Stopping Streaming in right state!");
                blockScreenOrientation(false);
                mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
            } else {
                Logger.t(TAG).d("Stopping Streaming in wrong state!");
            }
        }
    }

    private boolean hasStreamUrl() {
        String stream = getPreferences(MODE_PRIVATE).getString("stream", "");
        return !TextUtils.isEmpty(stream);
    }

    private MenuDialog getMenuDialog() {
        return new MenuDialog(this);
    }

    private String getStream() {
        return getPreferences(MODE_PRIVATE).getString(KEY_STREAM, "");
    }

    private String getResolution() {
        return getPreferences(MODE_PRIVATE).getString(KEY_RESOLUTION, MenuDialog.DEFAULT_RESOLUTION);
    }

    private String getOrientation() {
        return getPreferences(MODE_PRIVATE).getString(KEY_ORIENTATION, MenuDialog.DEFAULT_ORIENTATION);
    }

    //菜单选项的相关处理
    private void showStreamEditor() {
        menuDialog = getMenuDialog();
        menuDialog.setStreamUrl(getStream());
        menuDialog.setResolution(getResolution());
        menuDialog.setOrientationVal(getOrientation());

        final AlertDialog alertdialog = new AlertDialog.Builder(this)
                .setView(menuDialog)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertdialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveBtn = alertdialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String streamVal = menuDialog.getStreamUrl();
                        String resolution = menuDialog.getResolution();
                        String orientation = menuDialog.getOrientationVal();
                        saveMenuConfig(streamVal, resolution, orientation);
                        if (TextUtils.isEmpty(streamVal) && TextUtils.isEmpty(pushurl)) {
                            menuDialog.setStreamUrlErrorInfo();
                            return;
                        }
                        alertdialog.dismiss();
                    }
                });
            }
        });
        alertdialog.show();
    }

    private void saveMenuConfig(String streamVal, String resolutionVal, String orientationVal) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(KEY_STREAM, streamVal);
        editor.putString(KEY_RESOLUTION, resolutionVal);
        editor.putString(KEY_ORIENTATION, orientationVal);
        editor.apply();
        setResolutionConfig(resolutionVal);
    }

    //前后摄像头的切换方法
    private void toggleCamera() {
        if (mLiveSession.canSwitchCamera()) {
            mCurrentCamera = mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_FRONT ?
                    Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
            mLiveSession.switchCamera(mCurrentCamera);
        } else {
            Utils.showToast(this, getString(R.string.c_cameraSwitch_resolution));
        }
    }

    //闪光灯切换的方法
    private void toggleFlash(Context context) {
        if (!Utils.isSupportCameraLedFlash(context.getPackageManager())) {
            return;
        }
        if (isFirstClickFlash) {
            isFirstClickFlash = false;
            if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mLiveSession.toggleFlash(true);
                flashBtn.setBackgroundResource(R.drawable.flash_on);
            } else {
                Utils.showToast(this, getString(R.string.c_flashlightSwitch));
            }
        } else {
            isFirstClickFlash = true;
            mLiveSession.toggleFlash(false);
            flashBtn.setBackgroundResource(R.drawable.flash_off);
        }
    }

    //美颜效果开启
/*    private void onClickSwitchBeautyEffect() {
        hasBueatyEffect = !hasBueatyEffect;
        mLiveSession.enableDefaultBeautyEffect(hasBueatyEffect);
        effectBtn.setBackgroundResource(hasBueatyEffect ? R.drawable.beautiful : R.drawable.no_beautiful);
    }*/

    /**
     * 向服务器获取评论的方法
     */
    public void getComment() {
        timer_comment = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = mUIEventHandler.obtainMessage();
                message.what = UI_EVENT_GET_COMMENT;
                mUIEventHandler.sendMessage(message);
            }
        };
        timer_comment.schedule(task, 1000, 3000);
    }

    private void initRTMPSession(SurfaceHolder sh) {
        Logger.t(TAG).d(getOrientation());
        int orientation = getOrientation().equals("portrait") ? LiveConfig.ORIENTATION_PORTRAIT : LiveConfig.ORIENTATION_LANDSCAPE;//  2  1
        LiveConfig liveConfig = new LiveConfig.Builder()
                .setCameraId(LiveConfig.CAMERA_FACING_BACK) // 选择摄像头为后置摄像头
                .setCameraOrientation(orientation) //设置摄像头
                .setVideoWidth(mVideoWidth) // 设置推流视频宽度, 需传入长的一边
                .setVideoHeight(mVideoHeight) // 设置推流视频高度，需传入短的一边
                .setVideoFPS(mFrameRate) // 设置视频帧率
                .setInitVideoBitrate(mBitrate) // 设置初始视频码率，单位为bit per seconds
                .setAudioBitrate(64 * 1000) // 设置音频码率，单位为bit per seconds
                .setAudioSampleRate(LiveConfig.AUDIO_SAMPLE_RATE_44100) // 设置音频采样率
                .setGopLengthInSeconds(2) // 设置I帧间隔，单位为秒
                .setQosEnabled(true)//设置码率自适应
                .setMinVideoBitrate(200*1000)//码率自适应，最低码率
                .setMaxVideoBitrate(mBitrate)//码率自适应，最高码率
                .setQosSensitivity(5)//码率自适应，调整的灵敏度，单位为秒，可接受[5, 10]之间的值
                .build();
        Logger.t(TAG).d("Calling initRTMPSession..." + liveConfig.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mLiveSession = new LiveSessionHW(this, liveConfig);
        } else {
            mLiveSession = new LiveSessionSW(this, liveConfig);
        }
        mLiveSession.setStateListener(mStateListener);
        mLiveSession.bindPreviewDisplay(sh);
        mLiveSession.prepareSessionAsync();
    }

    private void initUIEventHandler() {
        Logger.t(TAG).d("initUIEventHandler");
        mUIEventHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UI_EVENT_RECORDER_CONNECTING:
                        isConnecting = true;
                        mRecorderButton.setBackgroundResource(R.drawable.block);
                        mRecorderButton.setEnabled(false);
                        Utils.showToast(CameraActivity.this, getString(R.string.c_pushStream_connect));
                        break;

                    case UI_EVENT_RECORDER_STARTED:
                        if (isFirstPush) {
                            currentTime = String.valueOf(System.currentTimeMillis() / 1000);
                            Logger.t(TAG).d("currentTime" + currentTime);
                            isFirstPush = false;
                        }

                        Logger.t(TAG).d("Starting Streaming succeeded!");

                        Utils.showToast(CameraActivity.this, getString(R.string.c_pushStream_start));

                        checkRecordTime();

                        final Map<String, String> param = new HashMap<String, String>();
                        param.put("op", OPERATION);
                        param.put("liveid", mShareInfo.getLiveId());
                        param.put("state", START_STATE);
                        param.put("openid", openID);

                        new Thread() {
                            @Override
                            public void run() {
                                final String posts = Utils.post(eventUrl + "applogin", param);//做网络请求 通知服务器 改变直播状态
                                Logger.t(TAG).d("repsonse_start_str:" + posts);
                            }
                        }.start();

                        ll_time.setVisibility(View.VISIBLE);

                        if (!mThread.isAlive()) {
                            //开始计时器或者是重启计时器，设置标记为true
                            mflag = true;
                            //判断是否是第一次启动，如果是不是第一次启动，那么状态就是Thread.State.TERMINATED
                            //不是的话，就需要重新的初始化，因为之前的已经结束了。
                            //并且要判断这个mCount 是否为-1，如果是的话，说明上一次的计时已经完成了，那么要重新设置。
                            if (mThread.getState() == Thread.State.TERMINATED) {
                                mThread = new Thread(CameraActivity.this);
                                mThread.start();
                            } else {
                                mThread.start();
                            }
                        } else {
                            mflag = false;//暂停计时器，设置标记为false
                        }
                        serverFailTryingCount = 0;
                        isSessionStarted = true;
                        needRestartAfterStopped = false;
                        isConnecting = false;
                        mRecorderButton.setBackgroundResource(R.drawable.ic_video_stop);
                        mRecorderButton.setEnabled(true);
                        break;

                    case UI_EVENT_RECORDER_STOPPED:

                        mflag = false;
                        final Map<String, String> params = new HashMap<String, String>();
                        params.put("op", OPERATION);
                        params.put("liveid", mShareInfo.getLiveId());
                        params.put("state", STOP_STATE);
                        params.put("openid", openID);
                        params.put("livetime", String.valueOf(getRecordTime()));
                        params.put("starttime", currentTime);
                        new Thread() {
                            @Override
                            public void run() {
                                final String post = Utils.post(eventUrl + "applogin", params);
                                Logger.t(TAG).d("response_stop_str:" + post);
                            }
                        }.start();
                        Logger.t(TAG).d("Stopping Streaming succeeded!");
                        serverFailTryingCount = 0;
                        isSessionStarted = false;
                        needRestartAfterStopped = false;
                        isConnecting = false;
                        mRecorderButton.setBackgroundResource(R.drawable.ic_video_start);

                        int arg = msg.arg1;
                        if (arg == 13) {
                            //普通会员正常到达时间
                            timer_time.cancel();//取消获取时间的定时器
                            mRecorderButton.setEnabled(false);
                            Utils.showToast(CameraActivity.this, getString(R.string.c_member_pushStreamTime));
                        } else if (arg == 14) {
                            //号外棒不足
                            timer_time.cancel();
                            mRecorderButton.setEnabled(false);
                            Utils.showToast(CameraActivity.this, getString(R.string.c_member_shortage));
                        } else if (arg == 15) {
                            //会员过期
                            timer_time.cancel();
                            mRecorderButton.setEnabled(false);
                            Utils.showToast(CameraActivity.this, getString(R.string.c_member_overdue));
                        } else if (arg == 16) {
                            //记次失败
                            timer_time.cancel();
                            mRecorderButton.setEnabled(false);
                            Utils.showToast(CameraActivity.this, getString(R.string.c_member_count_error));
                        } else if (arg == 17) {
                            //超级会员正常到达的时间
                            timer_time.cancel();
                            mRecorderButton.setEnabled(false);
                            Utils.showToast(CameraActivity.this, getString(R.string.c_member_pushStreamTime));
                        } else {
                            //正常的用户操作
                            timer_time.cancel();
                            mRecorderButton.setEnabled(true);
                            Utils.showToast(CameraActivity.this, getString(R.string.c_pushStream_stop));
                        }
                        break;
                    case UI_EVENT_SESSION_PREPARED:
                        isSessionReady = true;
                        mLoadingAnimation.setVisibility(View.GONE);
                        mRecorderButton.setVisibility(View.VISIBLE);
                        mRecorderButton.setEnabled(true);
                        break;
                    case UI_EVENT_STOP_STREAMING:
                        if (!isConnecting) {
                            Logger.t(TAG).d("Stopping current session...");
                            if (isSessionReady) {
                                mLiveSession.stopRtmpSession();
                            }
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                        break;
                    case UI_EVENT_RESTART_STREAMING:
                        if (!isConnecting) {
                            Logger.t(TAG).d("Restarting session...");
                            isConnecting = true;
                            needRestartAfterStopped = true;
                            if (isSessionReady) {
                                mLiveSession.stopRtmpSession();
                            }
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
                        }
                        break;
                    case UI_EVENT_SHOW_TOAST_MESSAGE:
                        String text = (String) msg.obj;
                        Utils.showToast(CameraActivity.this, text);
                        break;
                    case UI_EVENT_RESIZE_CAMERA_PREVIEW:
                        String hint = String.format("注意：当前摄像头不支持您所选择的分辨率\n实际分辨率为%dx%d",
                                mVideoWidth, mVideoHeight);
                        Utils.showToast(CameraActivity.this, hint);
                        fitPreviewToParentByResolution(mCameraView.getHolder(), mVideoWidth, mVideoHeight);
                        break;
                    case UI_EVENT_START_TIME:
                        startTime();
                        count++;
                        iv_time_switch.setVisibility(count % 2 == 0 ? View.VISIBLE : View.INVISIBLE);
                        break;
                    case UI_EVENT_GET_COMMENT:
                        new CommentAsyncTask().execute(eventUrl);
                        BigDecimal bd=new BigDecimal(mLiveSession.getCurrentUploadBandwidthKbps());
                        double upBandWidth = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        mTv_band_width.setText(upBandWidth+"kb/s");
                        mTv_band_width.setTextColor(upBandWidth<100? Color.RED:Color.WHITE);
                        break;

                    case UI_EVENT_RECORD_TIME:
                        postToServer();
                        break;

                    case UI_EVENT_HIDE_FOCUS_ICON:
                        mFoucsIcon.setVisibility(View.GONE);
                        break;

                    case UI_EVENT_STOP_COUNT_TIME:
                        totaltime++;
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    //直播间计时方法
    private void startTime() {
        time_s++;
        if (time_s <= 9) {
            mtv_time_s.setText("0" + time_s);
        } else {
            mtv_time_s.setText(time_s + "");
        }
        if (time_s == 59) {
            mtv_time_s.setText("00");
            time_m++;
            time_s = 0;
            if (time_m <= 9) {
                mtv_time_m.setText("0" + time_m);
            } else {
                mtv_time_m.setText(time_m + "");
            }
        }
        if (time_m == 59) {
            mtv_time_m.setText("00");
            time_h++;
            time_m = 0;
            if (time_h <= 9) {
                mtv_time_h.setText("0" + time_h);
            } else {
                mtv_time_h.setText(time_h);
            }
        }
    }

    /**
     * 获取录制时间
     */
    private long getRecordTime() {
        long liveTime = 0;
        //获取时间
        String h = mtv_time_h.getText().toString().trim();
        String mm = mtv_time_m.getText().toString().trim();
        String ss = mtv_time_s.getText().toString().trim();
        if (h.startsWith("0")) {
            liveTime += Integer.parseInt(h.substring(1)) * 3600;
        } else {
            liveTime += Integer.parseInt(h) * 3600;
        }
        if (mm.startsWith("0")) {
            liveTime += Integer.parseInt(mm.substring(1)) * 60;
        } else {
            liveTime += Integer.parseInt(mm) * 60;
        }
        if (ss.startsWith("0")) {
            liveTime += Integer.parseInt(ss.substring(1));
        } else {
            liveTime += Integer.parseInt(ss);
        }
        return liveTime;
    }


    private Timer timer_time = null;

    /**
     * 每分钟检查会员扣取号外帮以及直播记次情况
     */
    private void checkRecordTime() {
        timer_time = new Timer();
        TimerTask timerTsak = new TimerTask() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = UI_EVENT_RECORD_TIME;
                mUIEventHandler.sendMessage(msg);
            }
        };
        timer_time.schedule(timerTsak, 60000, 60000);
    }

    /**
     * 是否已经记次
     */

    private boolean isCount = false;

    /**
     * 请求后台扣取号外棒以及记次，根据响应参数做出处理
     */
    private void postToServer() {
        //外面的流就不需要记次与计时
        if (hasStreamUrl()) {
            return;
        }
        int recrodTime = (int) getRecordTime();
        Logger.t(TAG).d("postToServer: " + recrodTime);
        if (!isCount && (recrodTime >= 300)) {
            // 记次失败后的五次重新记次如果没成功，就停止推流  没考虑VIP3 与VIP5的情况 这种情况几率很小
            if (recrodTime > 600) {
                //如果到10分钟还没记次成功就强制停止推流
                Message msg = Message.obtain();
                msg.arg1 = 16;
                msg.what = UI_EVENT_RECORDER_STOPPED;
                mUIEventHandler.sendMessage(msg);
            }
            Logger.t(TAG).d("isCount" + isCount);
            final Map<String, String> params = new HashMap<String, String>();
            params.put("openid", openID);
            params.put("liveid", mShareInfo.getLiveId());
            Logger.t(TAG).d("openid:" + openID + "---" + mShareInfo.getLiveId());
            RequestTask task = new RequestTask(params);
            task.execute(eventUrl + "liveconsumeajax");
        }
        if (recrodTime >= 2700 && (memberlevelid == 0 || memberlevelid == 1 || memberlevelid == 2)) {
            Logger.t(TAG).d("普通会员" + memberlevelid);
            Message msg = Message.obtain();
            msg.arg1 = 13;
            msg.what = UI_EVENT_RECORDER_STOPPED;
            mUIEventHandler.sendMessage(msg);
        } else {
            if (recrodTime >= 14400 && (memberlevelid == 3 || memberlevelid == 5)) {
                Logger.t(TAG).d("超级会员" + memberlevelid);
                Message msg = Message.obtain();
                msg.arg1 = 17;
                msg.what = UI_EVENT_RECORDER_STOPPED;
                mUIEventHandler.sendMessage(msg);
            }
        }
    }

    class RequestTask extends AsyncTask<String, Void, Integer> {
        public Map<String, String> requestParams;
        int stateCode = 0;

        public RequestTask(Map<String, String> params) {
            this.requestParams = params;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String post = Utils.post(params[0], requestParams);
            try {
                JSONObject object = new JSONObject(post);
                stateCode = object.getInt("response");
                Logger.t(TAG).d("stateCode" + stateCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return stateCode;
        }

        @Override
        protected void onPostExecute(Integer stateCode) {
            super.onPostExecute(stateCode);
            switch (stateCode) {
                //记次成功
                case 200:
                    isCount = true;
                    Logger.t(TAG).d(getString(R.string.count_ok));
                    break;

                //号外棒不足
                case 300:
                    Logger.t(TAG).d(getString(R.string.insufficient));
                    Message msg_300 = Message.obtain();
                    msg_300.arg1 = 14;
                    msg_300.what = UI_EVENT_RECORDER_STOPPED;
                    mUIEventHandler.sendMessage(msg_300);
                    break;

                //会员过期
                case 301:
                    Logger.t(TAG).d(getString(R.string.members_expired));
                    Message msg_301 = Message.obtain();
                    msg_301.arg1 = 15;
                    msg_301.what = UI_EVENT_RECORDER_STOPPED;
                    mUIEventHandler.sendMessage(msg_301);
                    break;

                //非法操作
                case 400:
                    Logger.t(TAG).d(getString(R.string.illegal_operation));
                    break;

                //记次失败
                case 500:
                    Logger.t(TAG).d(getString(R.string.count_error));
                    isCount = false;
                    break;
            }
        }
    }

    @Override
    public void run() {
        while (mflag) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message = Message.obtain();
            message.what = UI_EVENT_START_TIME;
            mUIEventHandler.sendMessage(message);
        }
    }

    //监听推流状态
    private void initStateListener() {
        mStateListener = new SessionStateListener() {
            @Override
            public void onSessionPrepared(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_SESSION_PREPARED);
                    }
                    int realWidth = mLiveSession.getAdaptedVideoWidth();
                    int realHeight = mLiveSession.getAdaptedVideoHeight();
                    if (realHeight != mVideoHeight || realWidth != mVideoWidth) {
                        mVideoHeight = realHeight;
                        mVideoWidth = realWidth;
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RESIZE_CAMERA_PREVIEW);
                    }
                }
            }

            @Override
            public void onSessionStarted(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STARTED);
                    }
                } else {
                    Logger.t(TAG).w("Starting Streaming failed!");
                }
            }

            @Override
            public void onSessionStopped(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        if (needRestartAfterStopped && isSessionReady) {
                            mLiveSession.startRtmpSession(pushurl);
                        } else {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                    }
                } else {
                    Logger.t(TAG).w("Stopping Streaming failed!");
                }
            }

            @Override
            public void onSessionError(int code) {
                switch (code) {
                    case SessionStateListener.ERROR_CODE_OF_OPEN_MIC_FAILED:
                        Logger.t(TAG).e("Error occurred while opening MIC!");
                        onOpenDeviceFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_OPEN_CAMERA_FAILED:
                        Logger.t(TAG).e("Error occurred while opening Camera!");
                        onOpenDeviceFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_PREPARE_SESSION_FAILED:
                        Logger.t(TAG).e("Error occurred while preparing recorder!");
                        onPrepareFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_CONNECT_TO_SERVER_FAILED:
                        Logger.t(TAG).e("Error occurred while connecting to server!");
                        if (mUIEventHandler != null) {
                            serverFailTryingCount++;
                            if (serverFailTryingCount > 5) {
                                Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
                                msg.obj = "自动重连服务器失败，请检查网络设置";
                                mUIEventHandler.sendMessage(msg);
                                mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                            } else {
                                Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
                                msg.obj = "连接推流服务器失败，自动重试5次，当前为第" + serverFailTryingCount + "次";
                                mUIEventHandler.sendMessage(msg);
                                mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_RECONNECT_SERVER, 2000);
                            }

                        }
                        break;
                    case SessionStateListener.ERROR_CODE_OF_DISCONNECT_FROM_SERVER_FAILED:
                        Logger.t(TAG).e("Error occurred while disconnecting from server!");
                        isConnecting = false;
                        // Although we can not stop session successfully, we still
                        // need to take it as stopped
                        if (mUIEventHandler != null) {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                        break;
                    default:
                        onStreamingError(code);
                        break;
                }
            }
        };
    }

    int serverFailTryingCount = 0;

    private void onOpenDeviceFailed() {
        if (mUIEventHandler != null) {
            Message msg = new Message();
            msg.what = UI_EVENT_SHOW_TOAST_MESSAGE;
            msg.obj = "摄像头或MIC打开失败！请确认您已开启相关硬件使用权限！";
            mUIEventHandler.sendMessage(msg);
        }
    }

    private void onPrepareFailed() {
        isSessionReady = false;
    }

    private void onStreamingError(int errno) {
        Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
        switch (errno) {
            case SessionStateListener.ERROR_CODE_OF_SERVER_INTERNAL_ERROR:
                msg.obj = "因服务器异常，当前直播已经中断！正在尝试重新推流...";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                }
                break;
            //ERROR_CODE_OF_WEAK_CONNECTION_ERROR  ERROR_CODE_OF_WEAK_CONNECTION
            case SessionStateListener.ERROR_CODE_OF_WEAK_CONNECTION_ERROR:
                Logger.t(TAG).i("Weak connection...");
                msg.obj = "当前网络不稳定，请检查网络信号！";
                mWeakConnectionHintCount++;
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    if (mWeakConnectionHintCount >= 5) {
                        mWeakConnectionHintCount = 0;
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                    }
                }
                break;
            //ERROR_CODE_OF_LOCAL_NETWORK_ERROR ERROR_CODE_OF_CONNECTION_TIMEOUT
            case SessionStateListener.ERROR_CODE_OF_LOCAL_NETWORK_ERROR:
                Logger.t(TAG).i("Timeout when streaming...");
                msg.obj = "本地网络错误，请检查当前网络是否畅通！我们正在努力重连...";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                }
                break;
            default:
                Logger.t(TAG).i("Unknown error when streaming...");
                msg.obj = "未知错误，当前直播已经中断！正在重试！";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_RESTART_STREAMING, 1000);
                }
                break;
        }
    }

    private void fitPreviewToParentByResolution(SurfaceHolder holder, int width, int height) {
        // Adjust the size of SurfaceView dynamically
        int screenHeight = getWindow().getDecorView().getRootView().getHeight();
        int screenWidth = getWindow().getDecorView().getRootView().getWidth();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = width ^ height;
            height = width ^ height;
            width = width ^ height;
        }
        // Fit height
        int adjustedVideoHeight = screenHeight;
        int adjustedVideoWidth = screenWidth;
        if (width * screenHeight > height * screenWidth) { // means width/height
            // >
            // screenWidth/screenHeight
            // Fit width
            adjustedVideoHeight = height * screenWidth / width;
            adjustedVideoWidth = screenWidth;
        } else {
            // Fit height
            adjustedVideoHeight = screenHeight;
            adjustedVideoWidth = width * screenHeight / height;
        }
        holder.setFixedSize(adjustedVideoWidth, adjustedVideoHeight);
    }

    public void blockScreenOrientation(String orientation) {
        Logger.t(TAG).d("blockScreenOrientation:" + orientation);

        if ("auto".equals(orientation)) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            return;
        }

        int currentOrientation = getResources().getConfiguration().orientation;

        if (MenuDialog.ORIENTATION_LANDSCAPE.equals(orientation)
                && currentOrientation == Configuration.ORIENTATION_PORTRAIT
                ) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return;
        }

        if (MenuDialog.ORIENTATION_PORTRAIT.equals(orientation)
                && currentOrientation == Configuration.ORIENTATION_LANDSCAPE
                ) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private Handler mHandler = new Handler();

    private void setOrientation(final int orientation) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isSessionReady) {
                    setRequestedOrientation(orientation);
                }
            }
        }, 1000);
    }

    public void blockScreenOrientation(boolean block) {
        String configOrientation = getOrientation();
        if (block) {
            if ("auto".equals(configOrientation)) {
                int currentOrientation = getResources().getConfiguration().orientation;
                if (Configuration.ORIENTATION_LANDSCAPE == currentOrientation) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (Configuration.ORIENTATION_PORTRAIT == currentOrientation) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        } else {
            if ("auto".equals(configOrientation)) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }
    }

    //超清 1280*720, 21帧, 1500kbps
    //高清 1280*720: bit 1024000 * 15
    //标清 960*540       800000 * 15
    //流畅 640*360       512000 * 15
    private void setResolutionConfig(String resolution) {
        switch (resolution) {
            case "xhight": {
                mVideoWidth = 1280;
                mVideoHeight = 720;
                mFrameRate = 21;
                mBitrate = 1500000;
            }
            break;
            case "hight": {
                mVideoWidth = 1280;
                mVideoHeight = 720;
                mFrameRate = 15;
                mBitrate = 1024000;
            }
            break;
            case "standard": {
                mVideoWidth = 960;
                mVideoHeight = 540;
                mFrameRate = 15;
                mBitrate = 800000;
            }
            break;
            case "fluent": {
                mVideoWidth = 640;
                mVideoHeight = 360;
                mFrameRate = 15;
                mBitrate = 512000;
            }
            break;
        }
    }
}

