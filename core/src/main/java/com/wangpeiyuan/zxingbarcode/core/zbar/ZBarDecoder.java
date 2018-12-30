package com.wangpeiyuan.zxingbarcode.core.zbar;

import android.graphics.Rect;
import android.text.TextUtils;
import com.wangpeiyuan.zxingbarcode.core.core.DecoderResult;
import com.wangpeiyuan.zxingbarcode.core.core.IDecoder;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeUtil;
import net.sourceforge.zbar.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class ZBarDecoder implements IDecoder {

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mScanner;
    private List<ZBarcodeFormat> mFormatList;

    public ZBarDecoder() {
        setupScanner();
    }

    public void setFormats(List<ZBarcodeFormat> formats) {
        mFormatList = formats;
        setupScanner();
    }

    @Override
    public void setQRCodeFormat() {
        mFormatList = new ArrayList<>();
        mFormatList.add(ZBarcodeFormat.QRCODE);
        setupScanner();
    }

    public Collection<ZBarcodeFormat> getFormats() {
        if (mFormatList == null) {
            return ZBarcodeFormat.ALL_FORMATS;
        }
        return mFormatList;
    }

    private void setupScanner() {
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (ZBarcodeFormat format : getFormats()) {
            mScanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    @Override
    public DecoderResult decodeImage(byte[] data, int width, int height) {
        return decodeImage(data, width, height, null);
    }

    @Override
    public DecoderResult decodeImage(byte[] data, int width, int height, Rect clipRect) {
        if (data != null && data.length > 0) {
            return decodeData(data, width, height, clipRect);
        }
        QRCodeUtil.d("ZBarDecoder decodeImage: data is null or empty");
        return null;
    }

    @Override
    public List<DecoderResult> decodeImageMulti(byte[] data, int width, int height) {
        return decodeImageMulti(data, width, height, null);
    }

    @Override
    public List<DecoderResult> decodeImageMulti(byte[] data, int width, int height, Rect clipRect) {
        if (data != null && data.length > 0) {
            return decodeDataMulti(data, width, height, clipRect);
        }
        QRCodeUtil.d("ZBarDecoder decodeImageMulti: data is null or empty");
        return null;
    }

    private DecoderResult decodeData(byte[] data, int width, int height, Rect clipRect) {
        List<DecoderResult> decoderResults = decodeDataMulti(data, width, height, clipRect);
        if (decoderResults != null && !decoderResults.isEmpty()) {
            return decoderResults.get(0);
        }
        QRCodeUtil.d("ZBarDecoder decodeData: decoderResults is null or empty");
        return null;
    }

    private List<DecoderResult> decodeDataMulti(byte[] data, int width, int height, Rect clipRect) {
        QRCodeUtil.d("ZBarDecoder decodeData begin: width " + width + " height " + height + " clipRect " + (clipRect != null ? clipRect.toString() : "null"));
        long start = System.currentTimeMillis();
        Image barcode = new Image(width, height, "Y800");
        if (clipRect != null && !clipRect.isEmpty()) {
            barcode.setCrop(clipRect.left, clipRect.top, clipRect.width(), clipRect.height());
        }
        barcode.setData(data);
        int result = mScanner.scanImage(barcode);
        QRCodeUtil.d("ZBarDecoder decodeData scanImage end: total time  " + (System.currentTimeMillis() - start) + "ms");
        if (result != 0) {
            QRCodeUtil.d("ZBarDecoder decodeData: success");
            return analysisResult(mScanner.getResults(), width, height);
        }
        QRCodeUtil.d("ZBarDecoder decodeData: fail");
        return null;
    }

    private List<DecoderResult> analysisResult(SymbolSet symbolSet, int width, int height) {
        if (symbolSet != null) {
            List<DecoderResult> resultList = new ArrayList<>();
            for (Symbol symbol : symbolSet) {
                if (symbol.getType() == Symbol.NONE) {
                    continue;
                }

                String symData;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    symData = new String(symbol.getDataBytes(), StandardCharsets.UTF_8);
                } else {
                    symData = symbol.getData();
                }

                if (!TextUtils.isEmpty(symData)) {
                    DecoderResult result = new DecoderResult(symData,
                            ZBarcodeFormat.getFormatById(symbol.getType()).getName(),
                            symbol.getLocationPoints(), width, height, DecoderResult.DECODER_TYPE_ZBAR);
                    QRCodeUtil.d("ZBarDecoder analysisResult: " + result.toString());
                    resultList.add(result);
                }
            }
            return resultList;
        }
        return null;
    }
}
