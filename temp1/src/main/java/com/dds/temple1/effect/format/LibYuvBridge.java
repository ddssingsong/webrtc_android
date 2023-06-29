package com.dds.temple1.effect.format;

import java.nio.ByteBuffer;

/** 负责在 I420 和 RGBA 之间转换的类。
 *
 * 我想使用 RGBA 作为内存图像。 libyuv的RGBA转换是内存字节序
 * 顺序相反。
 * I420ToARGB() 的输出字节顺序为 B G R A，I420ToABGR() 为 R G B A。
 * 作为函数命名，Java 世界以内存顺序命名，本地方法使用 libyuv
 * 以相反的顺序放在一起。
 */
public class LibYuvBridge {

    static {
        System.loadLibrary("yuvconv");
    }

    public LibYuvBridge() {}

    public void i420ToRgba(ByteBuffer dataYBuffer, int strideY,
                           ByteBuffer dataUBuffer, int strideU,
                           ByteBuffer dataVBuffer, int strideV,
                           int width, int height,
                           ByteBuffer outRgbaBuffer) {
        i420ToAbgrInternal(
                dataYBuffer, strideY,
                dataUBuffer, strideU,
                dataVBuffer, strideV,
                width, height,
                outRgbaBuffer);
    }

    public void rgbaToI420(ByteBuffer rgbaBuffer,
                           int width, int height,
                           ByteBuffer outDataYBuffer, int strideY,
                           ByteBuffer outDataUBuffer, int strideU,
                           ByteBuffer outDataVBuffer, int strideV) {
        abgrToI420Internal(
                rgbaBuffer,
                width, height,
                outDataYBuffer, strideY,
                outDataUBuffer, strideU,
                outDataVBuffer, strideV);
    }

    private native void i420ToAbgrInternal(
            ByteBuffer dataYBuffer, int strideY,
            ByteBuffer dataUBuffer, int strideU,
            ByteBuffer dataVBuffer, int strideV,
            int width, int height,
            ByteBuffer outRgbaBuffer);

    private native void abgrToI420Internal(
            ByteBuffer rgbaBuffer,
            int width, int height,
            ByteBuffer outDataYBuffer, int strideY,
            ByteBuffer outDataUBuffer, int strideU,
            ByteBuffer outDataVBuffer, int strideV);
}
