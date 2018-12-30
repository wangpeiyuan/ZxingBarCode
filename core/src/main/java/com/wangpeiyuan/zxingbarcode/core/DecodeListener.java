package com.wangpeiyuan.zxingbarcode.core;

import com.wangpeiyuan.zxingbarcode.core.core.DecoderResult;

import java.util.List;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public interface DecodeListener {
    void onDecodeSuccess(List<DecoderResult> resultList);

    void onDecodeFail(Throwable t);
}
