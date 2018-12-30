package com.wangpeiyuan.zxingbarcode.core.core;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class QRCodeParams {
    /**
     * 字符串内容
     */
    private String content;
    /**
     * 位图宽度,要求>=0(单位:px)
     */
    private int width;
    /**
     * 位图高度,要求>=0(单位:px)
     */
    private int height;
    /**
     * 空白边距 (要求:整型且>=0), 传null时,zxing源码默认使用"4"。
     */
    private int margin;
    /**
     * 背景颜色，默认白色
     */
    private int bgColor = Color.WHITE;
    /**
     * 信息部分的颜色，默认黑色
     */
    private int preColor = Color.BLACK;
    /**
     * 目标图片 (preBitmap != null, 黑色色块将会被该图片像素色值替代)
     * 注：选用的Bitmap图片一定不能有白色色块,否则会识别不出来
     */
    private Bitmap preBitmap;
    /**
     * logo 图片信息
     */
    private LogoInfo logoInfo;
    private BackgroundInfo backgroundInfo;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getPreColor() {
        return preColor;
    }

    public void setPreColor(int preColor) {
        this.preColor = preColor;
    }

    public Bitmap getPreBitmap() {
        return preBitmap;
    }

    /**
     * 目标图片 (preBitmap != null, 黑色色块将会被该图片像素色值替代)
     * 注：选用的Bitmap图片一定不能有白色色块,否则会识别不出来
     */
    public void setPreBitmap(Bitmap preBitmap) {
        this.preBitmap = preBitmap;
    }

    public LogoInfo getLogoInfo() {
        return logoInfo;
    }

    public void setLogoInfo(LogoInfo logoInfo) {
        this.logoInfo = logoInfo;
    }

    public BackgroundInfo getBackgroundInfo() {
        return backgroundInfo;
    }

    public void setBackgroundInfo(BackgroundInfo backgroundInfo) {
        this.backgroundInfo = backgroundInfo;
    }

    public boolean validate() {
        return !TextUtils.isEmpty(content) && width > 0 && height > 0;
    }

    public static class LogoInfo {
        /**
         * logo 图片
         */
        private Bitmap logoBitmap;
        /**
         * logo 边缘的白色大小
         */
        private int paddingWhite;
        /**
         * logo 边缘的白色弧度
         */
        private float round;
        /**
         * logo 边框颜色
         */
        private int strokeColor = -1;

        public LogoInfo() {
        }

        public LogoInfo(Bitmap logoBitmap, int paddingWhite, float round, int strokeColor) {
            this.logoBitmap = logoBitmap;
            this.paddingWhite = paddingWhite;
            this.round = round;
            this.strokeColor = strokeColor;
        }

        public Bitmap getLogoBitmap() {
            return logoBitmap;
        }

        public void setLogoBitmap(Bitmap logoBitmap) {
            this.logoBitmap = logoBitmap;
        }

        public int getPaddingWhite() {
            return paddingWhite;
        }

        public void setPaddingWhite(int paddingWhite) {
            this.paddingWhite = paddingWhite;
        }

        public float getRound() {
            return round;
        }

        public void setRound(float round) {
            this.round = round;
        }

        public int getStrokeColor() {
            return strokeColor;
        }

        public void setStrokeColor(int strokeColor) {
            this.strokeColor = strokeColor;
        }
    }

    public static class BackgroundInfo {

        private Bitmap bgBitmap;
        /**
         * 图形码在背景图左边顶点
         */
        private int barCodeLeft;
        /**
         * 图形码在背景图上边顶点
         */
        private int barCodeTop;
        /**
         * 图形码的透明度 0 ~ 100
         */
        private int barCodeAlpha = 100;

        public BackgroundInfo() {
        }

        public BackgroundInfo(Bitmap bgBitmap, int barCodeLeft, int barCodeTop, int barCodeAlpha) {
            this.bgBitmap = bgBitmap;
            this.barCodeLeft = barCodeLeft;
            this.barCodeTop = barCodeTop;
            this.barCodeAlpha = barCodeAlpha;
        }

        public Bitmap getBgBitmap() {
            return bgBitmap;
        }

        public void setBgBitmap(Bitmap bgBitmap) {
            this.bgBitmap = bgBitmap;
        }

        public int getBarCodeLeft() {
            return barCodeLeft;
        }

        public void setBarCodeLeft(int barCodeLeft) {
            this.barCodeLeft = barCodeLeft;
        }

        public int getBarCodeTop() {
            return barCodeTop;
        }

        public void setBarCodeTop(int barCodeTop) {
            this.barCodeTop = barCodeTop;
        }

        public int getBarCodeAlpha() {
            return barCodeAlpha;
        }

        public void setBarCodeAlpha(int barCodeAlpha) {
            this.barCodeAlpha = barCodeAlpha;
        }
    }

    public static class Builder {
        private String content;
        private int width;
        private int height;
        private int margin;
        private int bgColor = Color.WHITE;
        private int preColor = Color.BLACK;
        private Bitmap preBitmap;
        private LogoInfo logoInfo;
        private BackgroundInfo backgroundInfo;

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setMargin(int margin) {
            this.margin = margin;
            return this;
        }

        public Builder setBgColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Builder setPreColor(int preColor) {
            this.preColor = preColor;
            return this;
        }

        /**
         * 目标图片 (preBitmap != null, 黑色色块将会被该图片像素色值替代)
         * 注：选用的Bitmap图片一定不能有白色色块,否则会识别不出来
         */
        public Builder setPreBitmap(Bitmap preBitmap) {
            if (preBitmap != null && !preBitmap.isRecycled()) {
                this.preBitmap = preBitmap;
            }
            return this;
        }

        public Builder setLogoInfo(Bitmap logoBitmap) {
            return setLogoInfo(logoBitmap, 0, 0, -1);
        }

        public Builder setLogoInfo(Bitmap logoBitmap, int paddingWhite) {
            return setLogoInfo(logoBitmap, paddingWhite, 0, -1);
        }

        public Builder setLogoInfo(Bitmap logoBitmap, int paddingWhite, float round) {
            return setLogoInfo(logoBitmap, paddingWhite, round, -1);
        }

        public Builder setLogoInfo(Bitmap logoBitmap, int paddingWhite, float round, int strokeColor) {
            if (logoBitmap != null && !logoBitmap.isRecycled()) {
                this.logoInfo = new LogoInfo(logoBitmap, paddingWhite, round, strokeColor);
            }
            return this;
        }

        public Builder setBackgroundInfo(Bitmap bgBitmap) {
            return setBackgroundInfo(bgBitmap, 0, 0, 100);
        }

        /**
         * @param bgBitmap    背景图
         * @param barCodeLeft 图形码在背景图左边顶点
         * @param barCodeTop  图形码在背景图上边顶点
         * @return
         */
        public Builder setBackgroundInfo(Bitmap bgBitmap, int barCodeLeft, int barCodeTop) {
            if (bgBitmap != null && !bgBitmap.isRecycled()) {
                this.backgroundInfo = new BackgroundInfo(bgBitmap, barCodeLeft, barCodeTop, 100);
            }
            return this;
        }

        /**
         * @param bgBitmap     背景图
         * @param barCodeLeft  图形码在背景图左边顶点
         * @param barCodeTop   图形码在背景图上边顶点
         * @param barCodeAlpha 图形码的透明度 0 ~ 100
         * @return
         */
        public Builder setBackgroundInfo(Bitmap bgBitmap, int barCodeLeft, int barCodeTop, int barCodeAlpha) {
            if (bgBitmap != null && !bgBitmap.isRecycled()) {
                this.backgroundInfo = new BackgroundInfo(bgBitmap, barCodeLeft, barCodeTop, barCodeAlpha);
            }
            return this;
        }

        public QRCodeParams build() {
            QRCodeParams codeParams = new QRCodeParams();
            codeParams.setContent(content);
            codeParams.setWidth(width);
            codeParams.setHeight(height);
            codeParams.setMargin(margin);
            codeParams.setBgColor(bgColor);
            codeParams.setPreColor(preColor);
            codeParams.setPreBitmap(preBitmap);
            codeParams.setLogoInfo(logoInfo);
            codeParams.setBackgroundInfo(backgroundInfo);
            return codeParams;
        }
    }
}
