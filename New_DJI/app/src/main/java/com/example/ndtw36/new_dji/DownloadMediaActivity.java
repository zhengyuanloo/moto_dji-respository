package com.example.ndtw36.new_dji;

import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import dji.sdk.api.Camera.DJICameraSettingsTypeDef;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef;
import dji.sdk.api.DJIError;
import dji.sdk.api.media.DJIMedia;
import dji.sdk.api.media.DJIMediaDirInfo;
import dji.sdk.api.media.DJIMediaFile;
import dji.sdk.api.media.DJIMediaInfo;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIDownloadListener;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIMediaFetchCallBack;
import dji.sdk.interfaces.DJIReceivedFileDataCallBack;

public class DownloadMediaActivity extends AppCompatActivity {

    private static final String TAG ="DownloadMediaActivity";

    private static String ALBUM_NAME = "/DJI_SDK";
    private static final String MNT = Environment.getExternalStorageDirectory().getPath();
    private RandomAccessFile mSyncFile;

    private boolean isNotPhantom2 = false;

    private int currentProgress = -1;

    private FileListAdapter mListAdapter;
    private ProgressDialog mDialog;
    private ProgressDialog mDownloadDialog;

    private List<DJIMedia> mDJIMediaList = null;
    private List<DJIMediaInfo> mDJIMediaInfoList = null;
    private DJIMediaDirInfo mDJIMediaDirInfo = null;
    private DJICameraSystemStateCallBack mCameraSystemStateCallBack = null;

    private final int SHOWTOAST = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SHOW_DOWNLOAD_PROGRESS_DIALOG = 4;
    private final int HIDE_DOWNLOAD_PROGRESS_DIALOG = 5;
    private final int FETCH_FILE_LIST = 6;
    private final int NEED_REFRESH_FILE_LIST = 7;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj);
                    break;
                case SHOW_PROGRESS_DIALOG:
                    ShowProgressDialog();
                    break;
                case HIDE_PROGRESS_DIALOG:
                    HideProgressDialog();
                    break;
                case SHOW_DOWNLOAD_PROGRESS_DIALOG:
                    ShowDownloadProgressDialog();
                    break;
                case HIDE_DOWNLOAD_PROGRESS_DIALOG:
                    HideDownloadProgressDialog();
                    break;
                case FETCH_FILE_LIST:
                    getFileList();
                    break;
                case NEED_REFRESH_FILE_LIST:
                    mListAdapter.notifyDataSetChanged();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    private void setResultToToast(String result){
        Toast.makeText(DownloadMediaActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    private void CreateProgressDialog() {

        mDialog = new ProgressDialog(DownloadMediaActivity.this);
        mDialog.setMessage("Please Waiting...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        mDownloadDialog = new ProgressDialog(DownloadMediaActivity.this);
        mDownloadDialog.setTitle("Sync File");
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(false);
    }

    private void ShowProgressDialog() {
        if(mDialog != null){
            mDialog.show();
        }
    }

    private void HideProgressDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void ShowDownloadProgressDialog() {
        if(mDownloadDialog != null){
            mDownloadDialog.show();
        }
    }

    private void HideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
        }
    }

    private long getAvailableStore(String filePath)
    {
        StatFs statFs = new StatFs(filePath);
        long blockSize = statFs.getBlockSize();

        long availableSpareBlocks = statFs.getAvailableBlocks();

        return availableSpareBlocks*blockSize;
    }

    //get all media file and put into listview
    private void getFileList(){
        new Thread(){
            public void run() {
                if (!isNotPhantom2) {
                    DJIDrone.getDjiCamera().fetchMediaList(new DJIMediaFetchCallBack(){

                        @Override
                        public void onResult(List<DJIMedia> mList, DJIError mError) {
                            // TODO Auto-generated method stub
                            handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));

                            if(mError.errorCode == DJIError.RESULT_OK ){
                                if(mDJIMediaList != null){
                                    mDJIMediaList.clear();
                                }
                                Log.d(TAG, "fetchMediaList success");
                                mDJIMediaList.addAll(mList);
                                mListAdapter.addAllDJIMediaList(mDJIMediaList);
                                handler.sendMessage(handler.obtainMessage(NEED_REFRESH_FILE_LIST, null));
                            }
                            else{
                                Log.e(TAG, "fetchMediaList failed,errorCode = "+ mError.errorCode);
                                handler.sendMessage(handler.obtainMessage(SHOWTOAST, DJIError.getErrorDescriptionByErrcode(mError.errorCode)));
                            }
                        }

                    });
                } else {

                    DJIDrone.getDjiCamera().fetchMediaList(new DJIDownloadListener<DJIMediaDirInfo>() {

                        @Override
                        public void onFailure(DJIError mError)
                        {
                            // TODO Auto-generated method stub
                            handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, mError.errorDescription));
                        }

                        @Override
                        public void onProgress(long arg0, long arg1)
                        {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onRateUpdate(long arg0, long arg1, long arg2)
                        {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onStart()
                        {
                            // TODO Auto-generated method stub
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, "Start"));
                        }

                        @Override
                        public void onSuccess(DJIMediaDirInfo info)
                        {
                            handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                            // TODO Auto-generated method stub
                            if(mDJIMediaInfoList != null){
                                mDJIMediaInfoList.clear();
                            }
                            mDJIMediaDirInfo = info;
                            mDJIMediaInfoList.addAll(mDJIMediaDirInfo.fileInfoList);
                            mListAdapter.addAllDJIMediaInfo(mDJIMediaInfoList);
                            handler.sendMessage(handler.obtainMessage(NEED_REFRESH_FILE_LIST, null));
                        }

                    });
                }
            }
        }.start();
    }

    //download media file
    private void syncOneFileByIndex(final int index){
        if (!isNotPhantom2) {
            String state = Environment.getExternalStorageDirectory().getPath();
            if(!android.os.Environment.MEDIA_MOUNTED.equals(state))
            {
                Log.e(TAG,"!android.os.Environment.MEDIA_MOUNTED.equals(state)");
                return ;
            }

            if(mDJIMediaList == null){
                return;
            }

            if(index >= mDJIMediaList.size()){
                return;
            }

            File destDir = new File(MNT + ALBUM_NAME);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            final DJIMedia mMedia = mDJIMediaList.get(index);
            long size = mMedia.fileSize;
            long available = getAvailableStore(MNT);
            Log.d(TAG,"syncOneFileByIndex size = "+size);
            Log.d(TAG,"syncOneFileByIndex available = "+available);

            if(size > available)
            {
                Log.e(TAG,"syncOneFileByIndex size > available");
                return ;
            }


            if (size <= 0)
            {
                Log.e(TAG,"syncOneFileByIndex size <= 0 return ");
                return;
            }

            final String filePath = MNT  +ALBUM_NAME+ "/" + mMedia.fileName;

            //if sync fail exist or not ,delete the file
            try {
                File destCheck = new File(filePath);

                if (destCheck.exists())
                {
                    destCheck.delete();
                }
            } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }


            try {
                mSyncFile = new RandomAccessFile(filePath, "rw");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                mSyncFile = null;
                e.printStackTrace();
            }

            if(mSyncFile == null){
                return;
            }

            handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));

            new Thread(){
                public void run() {
                    DJIDrone.getDjiCamera().fetchMediaData(mMedia,new DJIReceivedFileDataCallBack(){

                        @Override
                        public void onResult(byte[] buffer, int size, int progress, DJIError mErr) {
                            // TODO Auto-generated method stub
                            if(mErr.errorCode == DJIError.RESULT_OK ){
                                Log.d(TAG, "syncOneFileByIndex fetchMediaData success,progress = "+ progress);
                                try {
                                    mSyncFile.write(buffer, 0, size);
                                    mDownloadDialog.setProgress(progress);
                                    if(progress == 100){
                                        mSyncFile.close();
                                        handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, "Syns File Success"));
                                    }
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            else{
                                handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                                if(mSyncFile!=null)
                                {
                                    try {
                                        mSyncFile.close();
                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }

                                    //if sync fail ,delete the file
                                    File destDir = new File(filePath);
                                    if (destDir.exists())
                                    {
                                        destDir.delete();
                                    }
                                }

                                Log.e(TAG, "syncOneFileByIndex fetchMediaData failed,errorCode = "+ mErr.errorDescription);
                                handler.sendMessage(handler.obtainMessage(SHOWTOAST, "Sync file fail"+","+DJIError.getErrorDescriptionByErrcode(mErr.errorCode)));
                            }
                        }

                    });
                }
            }.start();
        } else {
            File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Dji_Sdk_Test/");
            DJIDrone.getDjiCamera().fetchMediaData(destDir, mDJIMediaInfoList.get(index), new DJIDownloadListener<DJIMediaFile>() {

                @Override
                public void onFailure(DJIError mErr) {
                    // TODO Auto-generated method stub
                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "Failure!    " + mErr.errorCode));
                    currentProgress = -1;
                }

                @Override
                public void onProgress(long total, long current) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onRateUpdate(long total, long current, long persize) {
                    // TODO Auto-generated method stub
                    int tmpProgress = (int) (1.0 * current / total * 100);
                    if (tmpProgress != currentProgress) {
                        mDownloadDialog.setProgress(tmpProgress);
                        currentProgress = tmpProgress;
                    }

                }

                @Override
                public void onStart() {
                    // TODO Auto-generated method stub
                    handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
                    currentProgress = -1;
                }

                @Override
                public void onSuccess(DJIMediaFile data) {
                    // TODO Auto-generated method stub
                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "Sync File success"));

                    currentProgress = -1;
                }

            });
        }
    }

    private void getThumbnailByIndex(int index){
        if (!isNotPhantom2) {
            if(mDJIMediaList == null){
                return;
            }

            if(index >= mDJIMediaList.size()){
                return;
            }

            final DJIMedia mMedia = mDJIMediaList.get(index);

            //reset icon to ic_dialog_info
            DownloadMediaActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
                }
            });


            DJIDrone.getDjiCamera().fetchMediaThumbnail(mMedia,new DJIExecuteResultCallback(){

                @Override
                public void onResult(DJIError mErr) {
                    // TODO Auto-generated method stub
                    if(mErr.errorCode == DJIError.RESULT_OK){
                        Log.d(TAG, "getThumbnailByIndex success");

                        DownloadMediaActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if(mDownloadDialog != null && mDownloadDialog.isShowing()){
                                    @SuppressWarnings("deprecation")
                                    Drawable mDrawable = new BitmapDrawable(mMedia.thumbnail);
                                    mDownloadDialog.setIcon(mDrawable);
                                }
                            }
                        });

                    }
                    else{
                        Log.e(TAG, "getThumbnailByIndex failed,errorCode = "+ mErr.errorCode);
                    }
                }

            });
        } else {
            File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Dji_Sdk_Test/");
            DJIDrone.getDjiCamera().fetchMediaThumbnail(destDir, mDJIMediaInfoList.get(index), new DJIDownloadListener<DJIMediaFile>() {

                @Override
                public void onFailure(DJIError arg0) {
                    // TODO Auto-generated method stub
                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                    currentProgress = -1;
                }

                @Override
                public void onProgress(long total, long current) {
                    // TODO Auto-generated method stub
                    int tmpProgress = (int) (1.0 * current / total) * 100;
                    if (tmpProgress != currentProgress) {
                        mDownloadDialog.setProgress(tmpProgress);
                        currentProgress = tmpProgress;
                    }
                }

                @Override
                public void onRateUpdate(long arg0, long arg1, long arg2) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStart() {
                    // TODO Auto-generated method stub
                    handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
                    currentProgress = -1;
                }

                @Override
                public void onSuccess(DJIMediaFile data) {
                    // TODO Auto-generated method stub
                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, "Sync file success"));
                    currentProgress = -1;
                }

            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_media);

        ListView mListView = (ListView)findViewById(R.id.filelistView);

        if (DJIDrone.getDroneType().value() != DJIDroneTypeDef.DJIDroneType.DJIDrone_Unknown.value()
                && DJIDrone.getDroneType().value() != DJIDroneTypeDef.DJIDroneType.DJIDrone_Vision.value()) {
            //DJIDrone.changeCameraAPIType();

            isNotPhantom2 = true;
            mListAdapter = new FileListAdapter(getApplicationContext(),isNotPhantom2);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                    //   getThumbnailByIndex(index);
                    syncOneFileByIndex(index);
                }
            });
            mListView.setAdapter(mListAdapter);

            CreateProgressDialog();

            DJIDrone.getDjiCamera().initDownloadMode(new DJIExecuteResultCallback() {

                @Override
                public void onResult(DJIError mErr) {
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, mErr.errorDescription));
                    // TODO Auto-generated method stub
                    if (mErr.errorCode == DJIError.RESULT_OK) {
                        handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
                        mDJIMediaInfoList = new ArrayList<DJIMediaInfo>();
                        handler.sendMessageDelayed(handler.obtainMessage(FETCH_FILE_LIST, null), 2000);
                    }
                }
            });

        }

        mCameraSystemStateCallBack = new DJICameraSystemStateCallBack(){
            @Override
            public void onResult(DJICameraSystemState mState) {
                // TODO Auto-generated method stub
            }

        };
        DJIDrone.getDjiCamera().setDjiCameraSystemStateCallBack(mCameraSystemStateCallBack);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(DJIDrone.getDjiCamera() != null){
            DJIDrone.getDjiCamera().stopUpdateTimer();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        if(DJIDrone.getDjiCamera() != null){
            DJIDrone.getDjiCamera().setCameraMode(DJICameraSettingsTypeDef.CameraMode.Camera_Capture_Mode, new DJIExecuteResultCallback() {

                @Override
                public void onResult(DJIError mErr) {
                    // TODO Auto-generated method stub

                }

            });
        }

        if(mDJIMediaList != null){
            mDJIMediaList.clear();
        }

        super.onDestroy();
    }

    public void onReturn(View view){
        Log.d(TAG ,"onReturn");

        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (null != mDialog)
            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
    }
}
