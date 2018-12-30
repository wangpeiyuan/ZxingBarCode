package com.wangpeiyuan.zxingbarcode.core;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeParams;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeUtil;
import com.wangpeiyuan.zxingbarcode.core.zxing.ZXingEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
class EncodeTask extends AsyncTask<Void, Void, EncodeTask.EncodeResult> {
    private QRCodeParams mQrCodeParams;
    private String mOutPutPath;
    private EncodeListener mEncodeListener;

    public EncodeTask(QRCodeParams mQrCodeParams, EncodeListener mEncodeListener) {
        this.mQrCodeParams = mQrCodeParams;
        this.mEncodeListener = mEncodeListener;
    }

    public EncodeTask(QRCodeParams mQrCodeParams, String mOutPutPath, EncodeListener mEncodeListener) {
        this.mQrCodeParams = mQrCodeParams;
        this.mOutPutPath = mOutPutPath;
        this.mEncodeListener = mEncodeListener;
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
    }

    @Override
    protected EncodeResult doInBackground(Void... voids) {
        try {
            Bitmap bitmap = ZXingEncoder.encoderQRCode(mQrCodeParams);
            if (bitmap == null) {
                if (mEncodeListener != null) {
                    mEncodeListener.onEncodeFail(new Exception("EncodeTask encode fail result bitmap is null"));
                }
                return null;
            }
            if (!TextUtils.isEmpty(mOutPutPath)) {
                boolean isSuccess = saveBitmap(bitmap, mOutPutPath);
                QRCodeUtil.d("EncodeTask save bitmap into file " + mOutPutPath + " is " + isSuccess);
            }
            return new EncodeResult(bitmap, mOutPutPath);
        } catch (Exception e) {
            if (mEncodeListener != null) {
                mEncodeListener.onEncodeFail(e);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(EncodeResult encodeResult) {
        if (mEncodeListener != null) {
            if (encodeResult != null && encodeResult.bitmap != null) {
                mEncodeListener.onEncodeSuccess(encodeResult.bitmap, encodeResult.outPutPath);
                return;
            }
            mEncodeListener.onEncodeFail(new Exception("EncodeTask encode fail"));
        }
    }

    /**
     * 保存图片的存储空间
     */
    private boolean saveBitmap(Bitmap bt, String path) {
        if (bt == null || TextUtils.isEmpty(path)) return false;
        File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bt.compress(Bitmap.CompressFormat.PNG, 100, out);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class EncodeResult {
        Bitmap bitmap;
        String outPutPath;

        EncodeResult(Bitmap bitmap, String outPutPath) {
            this.bitmap = bitmap;
            this.outPutPath = outPutPath;
        }
    }
}
