package com.wangpeiyuan.zxingbarcode.core;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.wangpeiyuan.zxingbarcode.core.core.DecoderResult;
import com.wangpeiyuan.zxingbarcode.core.core.IDecoder;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeUtil;
import com.wangpeiyuan.zxingbarcode.core.zbar.ZBarDecoder;
import com.wangpeiyuan.zxingbarcode.core.zxing.ZXingDecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
class DecodeTask extends AsyncTask<Void, Void, List<DecoderResult>> {
    private DecodeListener mDecodeListener;
    private String mImageLocalPath;
    private Bitmap mBitmap;
    private byte[] mData;
    private int mWidth, mHeight;
    private Rect mClipRect;
    private ZxingBarCode.DecodeType mDecodeType;
    private boolean mIsMulti;

    DecodeTask(String imageLocalPath, boolean isMulti, DecodeListener decodeListener,
               ZxingBarCode.DecodeType decodeType) {
        this.mImageLocalPath = imageLocalPath;
        this.mIsMulti = isMulti;
        this.mDecodeListener = decodeListener;
        this.mDecodeType = decodeType;
    }

    DecodeTask(Bitmap bitmap, Rect clipRect, boolean isMulti,
               DecodeListener decodeListener, ZxingBarCode.DecodeType decodeType) {
        this.mBitmap = bitmap;
        this.mClipRect = clipRect;
        this.mIsMulti = isMulti;
        this.mDecodeListener = decodeListener;
        this.mDecodeType = decodeType;
    }

    DecodeTask(byte[] data, int width, int height, Rect clipRect, boolean isMulti,
               DecodeListener decodeListener, ZxingBarCode.DecodeType decodeType) {
        this.mData = data;
        this.mWidth = width;
        this.mHeight = height;
        this.mClipRect = clipRect;
        this.mIsMulti = isMulti;
        this.mDecodeListener = decodeListener;
        this.mDecodeType = decodeType;
    }

    void perform() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mBitmap = null;
        mData = null;
    }

    @Override
    protected List<DecoderResult> doInBackground(Void... voids) {
        if (!TextUtils.isEmpty(mImageLocalPath)) {
            Bitmap bitmap = QRCodeUtil.getDecodeAbleBitmap(mImageLocalPath, ZxingBarCode.getMaxBitmapSize());
            if (bitmap == null) return null;
            mWidth = bitmap.getWidth();
            mHeight = bitmap.getHeight();
            mData = QRCodeUtil.getYUV420sp(mWidth, mHeight, bitmap);
            bitmap.recycle();
            bitmap = null;
            return decoderData();
        } else if (mBitmap != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
            mData = QRCodeUtil.getYUV420sp(mWidth, mHeight, mBitmap);
            return decoderData();
        } else if (mData != null && mData.length > 0) {
            return decoderData();
        }
        return null;
    }

    private List<DecoderResult> decoderData() {
        if (mData == null || mData.length <= 0) return null;
        IDecoder decoder;
        if (mDecodeType == ZxingBarCode.DecodeType.ZXing) {
            decoder = new ZXingDecoder();
            return decoderResultList(decoder);
        } else if (mDecodeType == ZxingBarCode.DecodeType.ZBar) {
            decoder = new ZBarDecoder();
            return decoderResultList(decoder);
        } else {
            decoder = new ZBarDecoder();
            List<DecoderResult> resultList = decoderResultList(decoder);
            if (resultList != null && !resultList.isEmpty()) return resultList;
            decoder = new ZXingDecoder();
            return decoderResultList(decoder);
        }
    }

    private List<DecoderResult> decoderResultList(IDecoder decoder) {
        if (ZxingBarCode.isOnlyQRCode()) {
            decoder.setQRCodeFormat();
        }
        List<DecoderResult> resultList = null;
        if (mIsMulti) {
            resultList = decoder.decodeImageMulti(mData, mWidth, mHeight, mClipRect);
        } else {
            DecoderResult decoderResult = decoder.decodeImage(mData, mWidth, mHeight, mClipRect);
            if (decoderResult != null) {
                resultList = new ArrayList<>();
                resultList.add(decoderResult);
            }
        }
        return resultList;
    }

    @Override
    protected void onPostExecute(List<DecoderResult> resultList) {
        if (mDecodeListener != null) {
            if (resultList != null && !resultList.isEmpty()) {
                mDecodeListener.onDecodeSuccess(resultList);
            } else {
                mDecodeListener.onDecodeFail(new Exception("DecodeTask decode fail"));
            }
        }
    }
}
