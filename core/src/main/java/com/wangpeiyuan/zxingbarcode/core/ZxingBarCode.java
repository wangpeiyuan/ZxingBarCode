package com.wangpeiyuan.zxingbarcode.core;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeParams;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class ZxingBarCode {
    private static boolean debug;
    /**
     * 限制图片最大的宽高
     */
    private static int mMaxBitmapSize = 800;
    /**
     * 是否只解析二维码类型
     */
    private static boolean mIsOnlyQRCode = false;

    private ZxingBarCode() {
    }

    public static void setDebug(boolean debug) {
        ZxingBarCode.debug = debug;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setMaxBitmapSize(int maxBitmapSize) {
        ZxingBarCode.mMaxBitmapSize = maxBitmapSize;
    }

    public static int getMaxBitmapSize() {
        return mMaxBitmapSize;
    }

    public static void setOnlyQRCode(boolean isOnlyQRCode) {
        ZxingBarCode.mIsOnlyQRCode = isOnlyQRCode;
    }

    public static boolean isOnlyQRCode() {
        return mIsOnlyQRCode;
    }

    public static void decodeCodeBar(String imageLocalPath, boolean isMulti,
                                     DecodeListener decoderListener, DecodeType decodeType) {
        if (imagePathCanUse(imageLocalPath, decoderListener)) {
            new DecodeTask(imageLocalPath, isMulti, decoderListener, decodeType).perform();
        }
    }

    public static void decodeCodeBar(Bitmap bitmap, Rect clipRect, boolean isMulti,
                                     DecodeListener decoderListener, DecodeType decodeType) {
        if (bitmapCanUse(bitmap, decoderListener)) {
            new DecodeTask(bitmap, clipRect, isMulti, decoderListener, decodeType).perform();
        }
    }

    public static void decodeCodeBar(byte[] data, int width, int height, Rect clipRect, boolean isMulti,
                                     DecodeListener decoderListener, DecodeType decodeType) {
        if (byteDataCanUse(data, decoderListener)) {
            new DecodeTask(data, width, height, clipRect, isMulti, decoderListener, decodeType).perform();
        }
    }

    public static void encodeQRCode(QRCodeParams params, EncodeListener encoderListener) {
        new EncodeTask(params, encoderListener).perform();
    }

    public static void encodeQRCode(QRCodeParams params, String outPutPath, EncodeListener encoderListener) {
        new EncodeTask(params, outPutPath, encoderListener).perform();
    }

    private static boolean imagePathCanUse(String imageLocalPath, DecodeListener decoderListener) {
        boolean canUse = !TextUtils.isEmpty(imageLocalPath) && !imageLocalPath.contains("http");
        if (!canUse && decoderListener != null) {
            decoderListener.onDecodeFail(new Exception("imageLocalPath must be not null and local"));
        }
        return canUse;
    }

    private static boolean bitmapCanUse(Bitmap bitmap, DecodeListener decoderListener) {
        boolean canUse = bitmap != null && !bitmap.isRecycled();
        if (!canUse && decoderListener != null) {
            decoderListener.onDecodeFail(new Exception("bitmap must be not null or isRecycled"));
        }
        return canUse;
    }

    private static boolean byteDataCanUse(byte[] data, DecodeListener decoderListener) {
        boolean canUse = data != null && data.length > 0;
        if (!canUse && decoderListener != null) {
            decoderListener.onDecodeFail(new Exception("byte data must be not null"));
        }
        return canUse;
    }

    /**
     * 使用解析库的类型
     */
    public enum DecodeType {
        ZBar, ZXing, Both
    }
}
