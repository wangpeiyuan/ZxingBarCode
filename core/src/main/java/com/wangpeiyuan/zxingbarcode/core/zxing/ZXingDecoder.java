package com.wangpeiyuan.zxingbarcode.core.zxing;

import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextUtils;
import com.google.zxing.*;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.wangpeiyuan.zxingbarcode.core.core.DecoderResult;
import com.wangpeiyuan.zxingbarcode.core.core.IDecoder;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeUtil;

import java.util.*;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class ZXingDecoder implements IDecoder {
    private MultiFormatReader mMultiFormatReader;
    private List<BarcodeFormat> mFormats;
    private boolean mIsOnlyQRCode = false;

    public ZXingDecoder() {
        setupMultiFormatReader();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        mIsOnlyQRCode = false;
        mFormats = formats;
        setupMultiFormatReader();
    }

    @Override
    public void setQRCodeFormat() {
        mIsOnlyQRCode = true;
        setupMultiFormatReader();
    }

    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            return ZXingBarcodeFormat.ALL_FORMATS;
        }
        return mFormats;
    }

    private void setupMultiFormatReader() {
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(getHints());
    }

    private Map<DecodeHintType, Object> getHints() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, mIsOnlyQRCode ? Collections.singletonList(BarcodeFormat.QR_CODE) : getFormats());
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        return hints;
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
        QRCodeUtil.d("ZXingDecoder decodeImage: data is null or empty");
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
        QRCodeUtil.d("ZXingDecoder decodeImageMulti: data is null or empty");
        return null;
    }

    private List<DecoderResult> decodeDataMulti(byte[] data, int width, int height, Rect clipRect) {
        QRCodeUtil.d("ZXingDecoder decodeDataMulti begin: width " + width + " height " + height + " clipRect " + (clipRect != null ? clipRect.toString() : "null"));
        long start = System.currentTimeMillis();
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height, clipRect);
        if (source == null) {
            QRCodeUtil.d("ZXingDecoder decodeDataMulti: source is null");
            return null;
        }
        Result[] rawResults = decodeMultiCode(source);
        QRCodeUtil.d("ZXingDecoder decodeDataMulti end: total time  " + (System.currentTimeMillis() - start) + "ms");
        return analysisResult(rawResults, width, height);
    }

    private DecoderResult decodeData(byte[] data, int width, int height, Rect clipRect) {
        QRCodeUtil.d("ZXingDecoder decodeData begin: width " + width + " height " + height + " clipRect " + (clipRect != null ? clipRect.toString() : "null"));
        long start = System.currentTimeMillis();
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height, clipRect);
        if (source == null) {
            QRCodeUtil.d("ZXingDecoder decodeData: source is null");
            return null;
        }
        Result rawResult = decodeCode(source);
        QRCodeUtil.d("ZXingDecoder decodeData end: total time  " + (System.currentTimeMillis() - start) + "ms");
        return analysisResult(rawResult, width, height);
    }

    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height, Rect clipRect) {
        PlanarYUVLuminanceSource source = null;
        try {
            if (clipRect != null && !clipRect.isEmpty()) {
                source = new PlanarYUVLuminanceSource(data, width, height, clipRect.left, clipRect.top,
                        clipRect.width(), clipRect.height(), false);
            } else {
                source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height,
                        false);
            }
        } catch (Exception e) {
            // continue
        }
        return source;
    }

    private Result decodeCode(PlanarYUVLuminanceSource source) {
        Result rawResult = null;
        try {
            /*
              HybridBinarizer算法使用了更高级的算法，但使用GlobalHistogramBinarizer识别效率确实比HybridBinarizer要高一些。

              GlobalHistogram算法：（http://kuangjianwei.blog.163.com/blog/static/190088953201361015055110/）

              二值化的关键就是定义出黑白的界限，我们的图像已经转化为了灰度图像，每个点都是由一个灰度值来表示，就需要定义出一个灰度值，大于这个值就为白（0），低于这个值就为黑（1）。
              在GlobalHistogramBinarizer中，是从图像中均匀取5行（覆盖整个图像高度），每行取中间    五分之四作为样本；以灰度值为X轴，每个灰度值的像素个数为Y轴建立一个直方图，
              从直方图中取点数最多的一个灰度值，然后再去给其他的灰度值进行分数计算，按照点数乘以与最多点数灰度值的距离的平方来进行打分，选分数最高的一个灰度值。接下来在这两个灰度值中间选取一个区分界限，
              取的原则是尽量靠近中间并且要点数越少越好。界限有了以后就容易了，与整幅图像的每个点进行比较，如果灰度值比界限小的就是黑，在新的矩阵中将该点置1，其余的就是白，为0。
             */
            rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
            if (rawResult == null) {
                rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
                if (rawResult != null) {
                    QRCodeUtil.d("ZXingDecoder decodeCode: GlobalHistogramBinarizer decode fail but HybridBinarizer decode success");
                }
            }
        } catch (NotFoundException e) {
            QRCodeUtil.e("ZXingDecoder decodeCode end: fail " + e.toString());
        } finally {
            mMultiFormatReader.reset();
        }

        //try again
        if (rawResult == null) {
            QRCodeUtil.d("ZXingDecoder decodeCode: try again");
            LuminanceSource invertedSource = source.invert();
            try {
                rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(invertedSource)));
                if (rawResult != null) {
                    QRCodeUtil.d("ZXingDecoder decodeCode: try again decode success");
                }
            } catch (NotFoundException e) {
                QRCodeUtil.e("ZXingDecoder decodeCode end: fail " + e.toString());
            } finally {
                mMultiFormatReader.reset();
            }
        }
        return rawResult;
    }

    private Result[] decodeMultiCode(PlanarYUVLuminanceSource source) {
        Result[] rawResults = null;
        //识别一张图片中的多个二维码不采用 QRCodeMultiReader 是因为出现了多次识别不出来，或者只能识别一个其他的无法识别
        MultipleBarcodeReader multipleBarcodeReader = new GenericMultipleBarcodeReader(mMultiFormatReader);
        try {
            rawResults = multipleBarcodeReader.decodeMultiple(new BinaryBitmap(new GlobalHistogramBinarizer(source)), getHints());
            if (rawResults == null || rawResults.length <= 0) {
                rawResults = multipleBarcodeReader.decodeMultiple(new BinaryBitmap(new HybridBinarizer(source)), getHints());
                if (rawResults != null && rawResults.length > 0) {
                    QRCodeUtil.d("ZXingDecoder decodeMultiCode: GlobalHistogramBinarizer decode fail but HybridBinarizer decode success");
                }
            }
        } catch (NotFoundException e) {
            QRCodeUtil.e("ZXingDecoder decodeMultiCode end: fail " + e.toString());
        } finally {
            if (!mIsOnlyQRCode) {
                mMultiFormatReader.reset();
            }
        }

        //try again
        if (rawResults == null || rawResults.length <= 0) {
            QRCodeUtil.d("ZXingDecoder decodeMultiCode: try again");
            LuminanceSource invertedSource = source.invert();
            try {
                rawResults = multipleBarcodeReader.decodeMultiple(new BinaryBitmap(new HybridBinarizer(invertedSource)), getHints());
                if (rawResults != null && rawResults.length > 0) {
                    QRCodeUtil.d("ZXingDecoder decodeMultiCode: try again decode success");
                }
            } catch (NotFoundException e) {
                QRCodeUtil.e("ZXingDecoder decodeMultiCode end: fail " + e.toString());
            } finally {
                if (!mIsOnlyQRCode) {
                    mMultiFormatReader.reset();
                }
            }
        }
        return rawResults;
    }

    private List<DecoderResult> analysisResult(Result[] results, int width, int height) {
        if (results != null && results.length > 0) {
            List<DecoderResult> resultList = new ArrayList<>();
            for (Result result : results) {
                DecoderResult decoderResult = analysisResult(result, width, height);
                if (decoderResult != null) {
                    resultList.add(decoderResult);
                }
            }
            return resultList;
        }
        return null;
    }

    private DecoderResult analysisResult(Result result, int width, int height) {
        if (result == null) return null;
        String content = result.getText();
        if (!TextUtils.isEmpty(content)) {

            ResultPoint[] resultPoints = result.getResultPoints();
            final PointF[] pointArr = new PointF[resultPoints.length];
            int pointIndex = 0;
            for (ResultPoint resultPoint : resultPoints) {
                pointArr[pointIndex] = new PointF(resultPoint.getX(), resultPoint.getY());
                pointIndex++;
            }

            DecoderResult decoderResult = new DecoderResult(content, result.getBarcodeFormat().name(),
                    pointArr, width, height, DecoderResult.DECODER_TYPE_ZXING);
            QRCodeUtil.d("ZXingDecoder analysisResult: " + decoderResult.toString());
            return decoderResult;
        }
        return null;
    }
}
