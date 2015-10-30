package com.example.ndtw36.new_dji;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.api.media.DJIMedia;
import dji.sdk.api.media.DJIMediaInfo;

public class FileListAdapter extends BaseAdapter {

    Context CTX;
    private List<DJIMedia> mDJIMediaList = null;
    private List<DJIMediaInfo> mDJIMediaInfoList = null;
    private boolean isNotPhantom2 = false;

    class ItemHolder {
        //ImageView thumbnail_img;
        TextView file_name;
        ImageView fileImage;
    }

    public FileListAdapter(Context context, boolean var2) {
        super();
        CTX=context;
        isNotPhantom2=var2;
        mDJIMediaList= new ArrayList<DJIMedia>();
        mDJIMediaInfoList=new ArrayList<DJIMediaInfo>();
    }

    public void addAllDJIMediaList(List<DJIMedia> mDJIMediaList){
        this.mDJIMediaList=mDJIMediaList;
    }

    public void addAllDJIMediaInfo(List<DJIMediaInfo> mDJIMediaInfoList){
        this.mDJIMediaInfoList=mDJIMediaInfoList;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {

        final ItemHolder mItemHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(CTX).inflate(
                    R.layout.media_info_item, null);

            mItemHolder = new ItemHolder();
            //mItemHolder.thumbnail_img = (ImageView) convertView.findViewById(R.id.item_image);
            mItemHolder.file_name = (TextView) convertView.findViewById(R.id.filename);
            mItemHolder.fileImage=(ImageView)convertView.findViewById(R.id.fileImage);
            convertView.setTag(mItemHolder);
        }
        else {
            mItemHolder = (ItemHolder)convertView.getTag();
        }

        Bitmap img=null;
        String str = "";
        if (!isNotPhantom2) {
            str = mDJIMediaList.get(index).fileName;
            img=mDJIMediaList.get(index).thumbnail;
            mItemHolder.file_name.setText(str);
            mItemHolder.fileImage.setImageBitmap(img);
        } else {
            str = mDJIMediaInfoList.get(index).getFileName();
            if(mDJIMediaInfoList != null){
                mItemHolder.file_name.setText(str);
            }
            else{
                mItemHolder.file_name.setText("");
            }
        }

        return convertView;
    }
    @Override
    public int getCount() {
        if(null != mDJIMediaInfoList || mDJIMediaList != null){
            if (!isNotPhantom2) {
                return mDJIMediaList.size();
            } else {
                return mDJIMediaInfoList.size();
            }
        }
        return 0;
    }
    @Override
    public Object getItem(int index) {
        if(null != mDJIMediaInfoList || mDJIMediaList != null){
            if (!isNotPhantom2) {
                return mDJIMediaList.get(index);
            } else {
                return mDJIMediaInfoList.get(index);
            }
        }
        return  null;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }
}
