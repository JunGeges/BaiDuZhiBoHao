package com.zmtmt.zhibohao;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.orhanobut.logger.Logger;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.zmtmt.zhibohao.tools.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_login;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    int postion = 0;

    private static final String UPGRADE_URL = "http://app.zmtmt.com/down/zhibohao.json";
    AlertDialog alertDialog;
    private BroadcastReceiver upgradeReceiver;//版本更新广播
    private static final String TAG = "IndexActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.t(TAG).d("onCreate");
        setContentView(R.layout.activity_index);
        MyApplication.list.add(this);
        if (savedInstanceState == null) {
            checkVersion();
        }
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.t(TAG).d("onDestroy");
    }

    /**
     * 初始化控件
     */
    private void initView() {
        btn_login = (Button) findViewById(R.id.btn_wx_login);
        surfaceView = (SurfaceView) findViewById(R.id.index_surfaceView);
        btn_login.setOnClickListener(this);
        mediaPlayer = new MediaPlayer();
        //默认为false  设置为true的时候 允许surface被显示的时候是否启用或者禁用屏幕保持打开状态
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (postion == 0) {
                    try {
                        play();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }

    public void play() throws IllegalArgumentException, SecurityException,
            IllegalStateException, IOException {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        AssetFileDescriptor fd = this.getAssets().openFd("index.mp4");
        mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                fd.getLength());
        mediaPlayer.setLooping(true);
        mediaPlayer.setDisplay(surfaceView.getHolder());
        // 通过异步的方式装载媒体资源
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 装载完毕回调
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_wx_login:
                if(MyApplication.isInstallWx){
                    btn_login.setText("正在登录...");
                    SendAuth.Req req = new SendAuth.Req();
                    req.scope = "snsapi_userinfo";
                    req.state = "wechat_sdk_demo_test";
                    boolean isSucceed = MyApplication.api.sendReq(req);
                }else {
                    Utils.showToast(getApplicationContext(),"您还没安装微信，请安装后重试！");
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        btn_login.setText("微信授权登录");
    }

    //以下针对更新升级
    private void checkVersion() {
        new UpdateVersionTask().execute(UPGRADE_URL);

    }

    int versionCode = 0;

    class UpdateVersionTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String str = Utils.get(strings[0]);
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            boolean hasVersionCode = false;
            String url = "";
            try {
                if (!TextUtils.isEmpty(s)) {
                    JSONObject object = new JSONObject(s);
                    versionCode = object.getInt("versionCode");
                    url = object.getString("url");
                    Logger.t(TAG).d("版本升级地址:"+url);
                    hasVersionCode = true;
                }
            } catch (JSONException ignored) {
                ignored.printStackTrace();
            }
            final String updateUrl = url;

            if (hasVersionCode && BuildConfig.VERSION_CODE < versionCode) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IndexActivity.this);
                alertDialogBuilder.setCancelable(false);
                alertDialog = alertDialogBuilder.setTitle("版本升级")
                        .setMessage("您当前使用的应用已不是最新版本，请升级到最新版本。")
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                IndexActivity.this.update(updateUrl);
                                alertDialog.dismiss();
                            }
                        });
                    }
                });
                alertDialog.show();
            }
            super.onPostExecute(s);

        }
    }

    public void update(String url) {
        downloadAndInstall(url);
    }

    private void downloadAndInstall(String url) {
        final DownloadManager dManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir("zmtmt", "zmtmt_zhibohao_update");
        request.setDescription("新版本正在下载");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
        }
        request.setMimeType("application/vnd.android.package-archive");
        request.setVisibleInDownloadsUi(true);
        final long refernece = dManager.enqueue(request);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        upgradeReceiver = new UpgradeBroadcastReceiver(dManager, refernece);
        registerReceiver(upgradeReceiver, filter);
    }

    //版本更新广播
    private static class UpgradeBroadcastReceiver extends BroadcastReceiver {
        private DownloadManager dManager;
        private long refernece;

        public UpgradeBroadcastReceiver(DownloadManager dManager, long refernece) {
            this.dManager = dManager;
            this.refernece = refernece;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (refernece == myDwonloadID) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                Uri downloadFileUri;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    downloadFileUri = dManager.getUriForDownloadedFile(refernece);
                    install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(install);
                }
            }
        }
    }

}
