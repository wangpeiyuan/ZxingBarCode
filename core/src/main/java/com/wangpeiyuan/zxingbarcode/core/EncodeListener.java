package com.wangpeiyuan.zxingbarcode.core;

import android.graphics.Bitmap;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public interface EncodeListener {
    void onEncodeSuccess(Bitmap bitmap, String outPutPath);

    void onEncodeFail(Throwable t);
}
