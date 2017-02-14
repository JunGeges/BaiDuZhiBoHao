package com.zmtmt.zhibohao;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.recorder.api.LiveSession;
import com.baidu.recorder.api.SessionStateListener;

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

public class AudioActivity extends AppCompatActivity {
    private static final String TAG = "AudioActivity";
    private WebView mWebView;

    private LiveSession mLiveSession = null;
    private Button mRecorderButton = null;
    private TextView statusView = null;
    private boolean isSessionReady = false;
    private boolean isSessionStarted = false;
    private boolean isConnecting = false;
    private boolean needRestartAfterStopped = false;

    private static final int UI_EVENT_RECORDER_CONNECTING = 0;
    private static final int UI_EVENT_RECORDER_STARTED = 1;
    private static final int UI_EVENT_RECORDER_STOPPED = 2;
    private static final int UI_EVENT_SESSION_PREPARED = 3;
    private static final int UI_EVENT_RESTART_STREAMING = 5;
    private static final int UI_EVENT_RECONNECT_SERVER = 6;
    private static final int UI_EVENT_STOP_STREAMING = 7;
    private static final int UI_EVENT_SHOW_TOAST_MESSAGE = 8;
    private static final int TEST_EVENT_SHOW_UPLOAD_BANDWIDTH = 10;
    private Handler mUIEventHandler = null;
    private SessionStateListener mStateListener = null;

    private String mStreamingUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_audio);

        mStreamingUrl = "rtmp://rtmp.zipindao.tv/live/4c3e9e252662f054";

        initUIElements();

        initUIEventHandler();
        initStateListener();
        initRTMPSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged orientation=" + newConfig.orientation);
        super.onConfigurationChanged(newConfig);
    }

    private void initUIElements() {
        mRecorderButton = (Button) findViewById(R.id.btn_streaming_action);
        mRecorderButton.setEnabled(false);
        statusView = (TextView) findViewById(R.id.tv_streaming_action);
    }

    private void initUIEventHandler() {
        mUIEventHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UI_EVENT_RECORDER_CONNECTING:
                        isConnecting = true;
                        mRecorderButton.setBackgroundResource(R.drawable.record_normal);
                        mRecorderButton.setPadding(0, 0, 0, 0);
                        statusView.setText("连接中");
                        mRecorderButton.setEnabled(false);
                        break;
                    case UI_EVENT_RECORDER_STARTED:
                        Log.i(TAG, "Starting Streaming succeeded!");
                        serverFailTryingCount = 0;
                        isSessionStarted = true;
                        needRestartAfterStopped = false;
                        isConnecting = false;
                        mRecorderButton.setBackgroundResource(R.drawable.btn_live_audio_stop);
                        mRecorderButton.setPadding(0, 0, 0, 0);
                        statusView.setText("点击按钮停止广播");
                        mRecorderButton.setEnabled(true);
                        break;
                    case UI_EVENT_RECORDER_STOPPED:
                        Log.i(TAG, "Stopping Streaming succeeded!");
                        serverFailTryingCount = 0;
                        isSessionStarted = false;
                        needRestartAfterStopped = false;
                        isConnecting = false;
                        mRecorderButton.setBackgroundResource(R.drawable.btn_live_audio_start);
                        mRecorderButton.setPadding(0, 0, 0, 0);
                        statusView.setText("点击按钮开始广播");
                        mRecorderButton.setEnabled(true);
                        break;
                    case UI_EVENT_SESSION_PREPARED:
                        isSessionReady = true;
//                        mLoadingAnimation.setVisibility(View.GONE);
                        mRecorderButton.setEnabled(true);
                        break;
                    case UI_EVENT_RECONNECT_SERVER:
                        Log.i(TAG, "Reconnecting to server...");
                        if (isSessionReady && mLiveSession != null) {
                            mLiveSession.startRtmpSession(mStreamingUrl);
                        }
                        if (mUIEventHandler != null) {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
                        }

                        break;
                    case UI_EVENT_STOP_STREAMING:
                        if (!isConnecting) {
                            Log.i(TAG, "Stopping current session...");
                            if (isSessionReady) {
                                mLiveSession.stopRtmpSession();
                            }
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                        break;
                    case UI_EVENT_RESTART_STREAMING:
                        if (!isConnecting) {
                            Log.i(TAG, "Restarting session...");
                            isConnecting = true;
                            needRestartAfterStopped = true;
                            if (isSessionReady && mLiveSession != null) {
                                mLiveSession.stopRtmpSession();
                            }
                            if (mUIEventHandler != null) {
                                mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
                            }

                        }
                        break;
                    case UI_EVENT_SHOW_TOAST_MESSAGE:
                        String text = (String) msg.obj;
                        Toast.makeText(AudioActivity.this, text, Toast.LENGTH_SHORT).show();
                        break;
                    case TEST_EVENT_SHOW_UPLOAD_BANDWIDTH:
                        if (mLiveSession != null) {
                            Log.d(TAG, "Current upload bandwidth is " + mLiveSession.getCurrentUploadBandwidthKbps()
                                    + " KBps.");
                        }
                        if (mUIEventHandler != null) {
                            mUIEventHandler.sendEmptyMessageDelayed(TEST_EVENT_SHOW_UPLOAD_BANDWIDTH, 2000);
                        }
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void initStateListener() {
        mStateListener = new SessionStateListener() {
            @Override
            public void onSessionPrepared(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_SESSION_PREPARED);
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
                    Log.e(TAG, "Starting Streaming failed!");
                }
            }

            @Override
            public void onSessionStopped(int code) {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED) {
                    if (mUIEventHandler != null) {
                        if (needRestartAfterStopped && isSessionReady) {
                            mLiveSession.startRtmpSession(mStreamingUrl);
                        } else {
                            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_STOPPED);
                        }
                    }
                } else {
                    Log.e(TAG, "Stopping Streaming failed!");
                }
            }

            @Override
            public void onSessionError(int code) {
                switch (code) {
                    case SessionStateListener.ERROR_CODE_OF_OPEN_MIC_FAILED:
                        Log.e(TAG, "Error occurred while opening MIC!");
                        onOpenDeviceFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_OPEN_CAMERA_FAILED:
                        Log.e(TAG, "Error occurred while opening Camera!");
                        onOpenDeviceFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_PREPARE_SESSION_FAILED:
                        Log.e(TAG, "Error occurred while preparing recorder!");
                        onPrepareFailed();
                        break;
                    case SessionStateListener.ERROR_CODE_OF_CONNECT_TO_SERVER_FAILED:
                        Log.e(TAG, "Error occurred while connecting to server!");
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
                        Log.e(TAG, "Error occurred while disconnecting from server!");
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
            Message msg = mUIEventHandler.obtainMessage(UI_EVENT_SHOW_TOAST_MESSAGE);
            msg.obj = "摄像头或MIC打开失败！请确认您已开启相关硬件使用权限！";
            mUIEventHandler.sendMessage(msg);
        }
    }

    private void onPrepareFailed() {
        isSessionReady = false;
    }

    int mWeakConnectionHintCount = 0;

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
/*            case SessionStateListener.ERROR_CODE_OF_WEAK_CONNECTION_ERROR:
                Log.i(TAG, "Weak connection...");
                msg.obj = "当前网络不稳定，请检查网络信号！";
                mWeakConnectionHintCount++;
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    if (mWeakConnectionHintCount >= 5) {
                        mWeakConnectionHintCount = 0;
                        mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                    }
                }
                break;*/
/*            case SessionStateListener.ERROR_CODE_OF_LOCAL_NETWORK_ERROR:
                Log.i(TAG, "Timeout when streaming...");
                msg.obj = "本地网络错误，请检查当前网络是否畅通！我们正在努力重连...";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessage(UI_EVENT_RESTART_STREAMING);
                }
                break;*/
            default:
                Log.i(TAG, "Unknown error when streaming...");
                msg.obj = "未知错误，当前直播已经中断！正在重试！";
                if (mUIEventHandler != null) {
                    mUIEventHandler.sendMessage(msg);
                    mUIEventHandler.sendEmptyMessageDelayed(UI_EVENT_RESTART_STREAMING, 1000);
                }
                break;
        }
    }

    private void initRTMPSession() {
/*        LiveConfig liveConfig = new LiveConfig.Builder()
                .setAudioBitrate(64 * 1000) // 设置音频码率，单位为bit per seconds
                .setAudioSampleRate(LiveConfig.AUDIO_SAMPLE_RATE_44100) // 设置音频采样率
                .setGopLengthInSeconds(2) // 设置I帧间隔，单位为秒
                .setVideoEnabled(false)
                .setAudioEnabled(true)
                .build();
        Log.d(TAG, "Calling initRTMPSession..." + liveConfig.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mLiveSession = new LiveSessionHW(this, liveConfig);
        } else {
            mLiveSession = new LiveSessionSW(this, liveConfig);
        }*/
        mLiveSession.setStateListener(mStateListener);
        mLiveSession.prepareSessionAsync();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public void onClickStreamingButton(View v) {
        if (!isSessionReady) {
            return;
        }
        if (!isSessionStarted && !TextUtils.isEmpty(mStreamingUrl)) {
            if (mLiveSession.startRtmpSession(mStreamingUrl)) {
                Log.i(TAG, "Starting Streaming in right state!");
            } else {
                Log.e(TAG, "Starting Streaming in wrong state!");
            }
            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
        } else {
            if (mLiveSession.stopRtmpSession()) {
                Log.i(TAG, "Stopping Streaming in right state!");
            } else {
                Log.e(TAG, "Stopping Streaming in wrong state!");
            }
            mUIEventHandler.sendEmptyMessage(UI_EVENT_RECORDER_CONNECTING);
        }
    }

    @Override
    public void onBackPressed() {
        if (isSessionStarted) {
            Toast.makeText(this, "直播过程中不能返回，请先停止直播！", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    public void onStart() {
        Log.i(TAG, "===========> onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "===========> onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "===========> onDestroy()");
        mUIEventHandler.removeCallbacksAndMessages(null);
        if (isSessionStarted) {
            mLiveSession.stopRtmpSession();
            isSessionStarted = false;
        }
        if (isSessionReady) {
            mLiveSession.destroyRtmpSession();
            mLiveSession = null;
            mStateListener = null;
            mUIEventHandler = null;
            isSessionReady = false;
        }
        super.onDestroy();
    }

    private void initWebViewCogfig() {
//        mWebView=(WebView)findViewById(R.id.audio_wv);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JsToJava(), "app");
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        //将图片调整到适合webview的大小
        settings.setUseWideViewPort(true);
        //自适应屏幕
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl("http://n01.zipindao.live/app/?c=entry&m=wg_test&i=1&do=template");
    }

    public class JsToJava {

        @JavascriptInterface
        public void stopRecord() {

        }

        @JavascriptInterface
        public void starRecord(String str) {
            Log.i(TAG, "starRecord: " + str);
        }
    }
}
