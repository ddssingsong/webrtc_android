package com.dds.webrtclib;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class VideoProfile {
    public static final int VP120P = 0;
    public static final int VP120P_3 = 2;
    public static final int VP180P = 10;
    public static final int VP180P_3 = 12;
    public static final int VP180P_4 = 13;
    public static final int VP240P = 20;
    public static final int VP240P_3 = 22;
    public static final int VP240P_4 = 23;
    public static final int VP360P = 30;
    public static final int VP360P_3 = 32;
    public static final int VP360P_4 = 33;
    public static final int VP360P_6 = 35;
    public static final int VP360P_7 = 36;
    public static final int VP360P_8 = 37;
    public static final int VP480P = 40;
    public static final int VP480P_3 = 42;
    public static final int VP480P_4 = 43;
    public static final int VP480P_6 = 45;
    public static final int VP480P_8 = 47;
    public static final int VP480P_9 = 48;
    public static final int VP720P = 50;
    public static final int VP720P_3 = 52;
    public static final int VP720P_5 = 54;
    public static final int VP720P_6 = 55;
    public static final int VPDEFAULT = 30;
    public int width;
    public int height;
    public int fps;
    public int bitrate;

    public VideoProfile(int width, int height, int bitrate, int fps, boolean var5) {
        if (var5) {
            this.height = width;
            this.height = width;
        } else {
            this.width = width;
            this.height = height;
        }

        this.bitrate = bitrate;
        this.fps = fps;
    }

    public static VideoProfile getVideoProfile(int var0, boolean var1) {
        switch (var0) {
            case 0:
                return new VideoProfile(160, 120, 15, 120, var1);
            case 2:
                return new VideoProfile(120, 120, 15, 100, var1);
            case 10:
                return new VideoProfile(320, 180, 15, 280, var1);
            case 12:
                return new VideoProfile(180, 180, 15, 200, var1);
            case 13:
                return new VideoProfile(240, 180, 15, 240, var1);
            case 20:
                return new VideoProfile(320, 240, 15, 360, var1);
            case 22:
                return new VideoProfile(240, 240, 15, 240, var1);
            case 23:
                return new VideoProfile(424, 240, 15, 400, var1);
            case 30:
                return new VideoProfile(640, 360, 15, 800, var1);
            case 32:
                return new VideoProfile(360, 360, 15, 520, var1);
            case 33:
                return new VideoProfile(640, 360, 30, 1200, var1);
            case 35:
                return new VideoProfile(360, 360, 30, 780, var1);
            case 36:
                return new VideoProfile(480, 360, 15, 1000, var1);
            case 37:
                return new VideoProfile(480, 360, 30, 1500, var1);
            case 40:
                return new VideoProfile(640, 480, 15, 1000, var1);
            case 42:
                return new VideoProfile(480, 480, 15, 800, var1);
            case 43:
                return new VideoProfile(640, 480, 30, 1500, var1);
            case 45:
                return new VideoProfile(480, 480, 30, 1200, var1);
            case 47:
                return new VideoProfile(848, 480, 15, 1200, var1);
            case 48:
                return new VideoProfile(848, 480, 30, 1800, var1);
            case 50:
                return new VideoProfile(1280, 720, 15, 2400, var1);
            case 52:
                return new VideoProfile(1280, 720, 30, 3600, var1);
            case 54:
                return new VideoProfile(960, 720, 15, 1920, var1);
            case 55:
                return new VideoProfile(960, 720, 30, 2880, var1);
            default:
                return getVideoProfile(VPDEFAULT, var1);
        }
    }
}
