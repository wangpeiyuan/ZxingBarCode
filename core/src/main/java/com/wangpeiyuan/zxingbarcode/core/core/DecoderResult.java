package com.wangpeiyuan.zxingbarcode.core.core;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class DecoderResult {
    public static final String DECODER_TYPE_ZXING = "zxing";
    public static final String DECODER_TYPE_ZBAR = "zbar";

    private String content;
    private String formatName;
    /**
     * 图形码在图片的中位置，二维码返回 4 个点，条形码返回 2 个点
     */
    private PointF[] resultPoints;
    private int bitmapWidth, bitmapHeight;
    private String decoderType;

    public DecoderResult() {
    }

    public DecoderResult(String content, String formatName, PointF[] resultPoints, int bitmapWidth, int bitmapHeight, String decoderType) {
        this.content = content;
        this.formatName = formatName;
        this.resultPoints = resultPoints;
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        this.decoderType = decoderType;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public PointF[] getResultPoints() {
        return resultPoints;
    }

    public void setResultPoints(PointF[] resultPoints) {
        this.resultPoints = resultPoints;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public void setBitmapWidth(int bitmapWidth) {
        this.bitmapWidth = bitmapWidth;
    }

    public int getBitmapHeight() {
        return bitmapHeight;
    }

    public void setBitmapHeight(int bitmapHeight) {
        this.bitmapHeight = bitmapHeight;
    }

    public String getDecoderType() {
        return decoderType;
    }

    public void setDecoderType(String decoderType) {
        this.decoderType = decoderType;
    }

    /**
     * 获取二维码在图片中的位置
     */
    public RectF getQRCodeResultRectF() {
        if (resultPoints != null && resultPoints.length == 4) {
            float left = Float.MAX_VALUE;
            float top = Float.MAX_VALUE;
            float right = Float.MIN_VALUE;
            float bottom = Float.MIN_VALUE;
            for (PointF pointF : resultPoints) {
                if (pointF.x < left) {
                    left = pointF.x;
                }
                if (pointF.y < top) {
                    top = pointF.y;
                }
                if (pointF.x > right) {
                    right = pointF.x;
                }
                if (pointF.y > bottom) {
                    bottom = pointF.y;
                }
            }
            if (left != Float.MAX_VALUE && top != Float.MAX_VALUE &&
                    right != Float.MIN_VALUE && bottom != Float.MIN_VALUE) {
                RectF rectF = new RectF(left, top, right, bottom);
                QRCodeUtil.d("DecoderResult getQRCodeResultRectF " + rectF.toString());
                return rectF;
            }
        }
        return null;
    }

    /**
     * 获取二维码在图片中的位置；
     * 由于 zxing 返回的大小是以二维码检测点的中心点为起始点的，这样并不能使得整个二维码的位置全部包含在 rect 当中，
     * 因此在此做了下偏移，请注意，此处偏移数值大小是一个大概的数值并不能保证每个二维码都能正确；
     * 如果想保证完全正确需要去修改源码具体请查看{@link com.google.zxing.qrcode.detector.Detector#detect(Map)}
     */
    public RectF getQRCodeExtendedRect() {
        RectF resultRectF = getQRCodeResultRectF();
        if (resultRectF == null) return null;
        if (DECODER_TYPE_ZXING.equals(decoderType)) {
            float extra = 60;
            resultRectF.left = resultRectF.left - extra;
            resultRectF.top = resultRectF.top - extra;
            resultRectF.right = resultRectF.right + extra;
            resultRectF.bottom = resultRectF.bottom + extra;
            QRCodeUtil.d("DecoderResult getQRCodeExtendedRect " + resultRectF.toString());
        }
        return resultRectF;
    }

    @Override
    public String toString() {
        return "{DecoderResult: [" +
                "\nformatName: " + formatName +
                "\ncontent: " + content +
                "\nresultPoints: " + Arrays.toString(resultPoints) +
                "\nbitmapWidth: " + bitmapWidth +
                "\nbitmapHeight: " + bitmapHeight +
                "\ndecoderType: " + decoderType +
                "\n]}";
    }
}
