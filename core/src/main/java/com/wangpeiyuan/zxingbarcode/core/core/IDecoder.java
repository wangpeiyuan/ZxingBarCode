package com.wangpeiyuan.zxingbarcode.core.core;

import android.graphics.Rect;

import java.util.List;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public interface IDecoder {
    void setQRCodeFormat();

    DecoderResult decodeImage(byte[] data, int width, int height);

    DecoderResult decodeImage(byte[] data, int width, int height, Rect clipRect);

    List<DecoderResult> decodeImageMulti(byte[] data, int width, int height);

    List<DecoderResult> decodeImageMulti(byte[] data, int width, int height, Rect clipRect);
}
