package com.dds.rtc.effect.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;

import org.webrtc.GlShader;

import java.nio.FloatBuffer;


public class SkinSmoothFilter {
    private GlShader shader;
    private int paramsLocation;
    private int brightnessLocation;
    private int singleStepOffsetLocation;

    private float toneLevel;
    private float beautyLevel;
    private float brightLevel;

    public void prepare() {
        if (shader == null) {
            shader = new GlShader(getVertexShader(), getGreyFragmentShader());
            paramsLocation = shader.getUniformLocation("params");
            brightnessLocation = shader.getUniformLocation("brightness");
            singleStepOffsetLocation = shader.getUniformLocation("singleStepOffset");
        }

        toneLevel = 0.47f;
        beautyLevel = 0.42f;
        brightLevel = 0.34f;

    }

    public void setBrightLevel(float brightLevel) {
        this.brightLevel = brightLevel;
        GLES20.glUniform1f(brightnessLocation, 0.6f * (-0.5f + brightLevel));
    }

    public void setParams(float beauty, float tone) {
        float[] vector = new float[4];
        vector[0] = 1.0f - 0.6f * beauty;
        vector[1] = 1.0f - 0.3f * beauty;
        vector[2] = 0.1f + 0.3f * tone;
        vector[3] = 0.1f + 0.3f * tone;
        GLES20.glUniform4fv(paramsLocation, 1, FloatBuffer.wrap(vector));
    }

    public void draw(int textureId, float[] mSTMatrix, int mWidth, int mHeight) {

        shader.useProgram();

        Matrix.setIdentityM(mSTMatrix, 0);

        setParams(beautyLevel, toneLevel);
        setBrightLevel(brightLevel);
        GLES20.glUniform2fv(singleStepOffsetLocation, 1, FloatBuffer.wrap(new float[]{2.0f / mWidth, 2.0f / mHeight}));


        GLES20.glUniformMatrix4fv(shader.getUniformLocation("tex_mat"), 1, false, mSTMatrix, 0);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(shader.getUniformLocation("sTexture"), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    public void release() {
        if (shader != null) {
            shader.release();
        }

    }

    private String getVertexShader() {
        return "varying vec2 tc;\n"
                + "attribute vec4 in_pos;\n"
                + "attribute vec4 in_tc;\n"
                + "uniform mat4 tex_mat;\n"
                + "void main() {\n"
                + "  gl_Position = in_pos;\n"
                + "  tc = (tex_mat * in_tc).xy;\n"
                + "}\n";
    }

    private String getGreyFragmentShader() {
        return "   varying highp vec2 tc;\n" +
                "    uniform sampler2D sTexture;\n" +
                "    uniform highp vec2 singleStepOffset;\n" +
                "    uniform highp vec4 params;\n" +
                "    uniform highp float brightness;\n" +
                "    const highp vec3 W = vec3(0.299, 0.587, 0.114);\n" +
                "    const highp mat3 saturateMatrix = mat3(\n" +
                "        1.1102, -0.0598, -0.061,\n" +
                "        -0.0774, 1.0826, -0.1186,\n" +
                "        -0.0228, -0.0228, 1.1772);\n" +
                "    highp vec2 blurCoordinates[24];\n" +
                "    highp float hardLight(highp float color) {\n" +
                "    if (color <= 0.5)\n" +
                "        color = color * color * 2.0;\n" +
                "    else\n" +
                "        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
                "    return color;\n" +
                "}\n" +
                "    void main(){\n" +
                "    highp vec3 centralColor = texture2D(sTexture, tc).rgb;\n" +
                "    blurCoordinates[0] = tc.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
                "    blurCoordinates[1] = tc.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
                "    blurCoordinates[2] = tc.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
                "    blurCoordinates[3] = tc.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
                "    blurCoordinates[4] = tc.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
                "    blurCoordinates[5] = tc.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
                "    blurCoordinates[6] = tc.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
                "    blurCoordinates[7] = tc.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
                "    blurCoordinates[8] = tc.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
                "    blurCoordinates[9] = tc.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
                "    blurCoordinates[10] = tc.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
                "    blurCoordinates[11] = tc.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
                "    blurCoordinates[12] = tc.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
                "    blurCoordinates[13] = tc.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
                "    blurCoordinates[14] = tc.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
                "    blurCoordinates[15] = tc.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
                "    blurCoordinates[16] = tc.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
                "    blurCoordinates[17] = tc.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
                "    blurCoordinates[18] = tc.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
                "    blurCoordinates[19] = tc.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
                "    blurCoordinates[20] = tc.xy + singleStepOffset * vec2(-2.0, -2.0);\n" +
                "    blurCoordinates[21] = tc.xy + singleStepOffset * vec2(-2.0, 2.0);\n" +
                "    blurCoordinates[22] = tc.xy + singleStepOffset * vec2(2.0, -2.0);\n" +
                "    blurCoordinates[23] = tc.xy + singleStepOffset * vec2(2.0, 2.0);\n" +
                "    highp float sampleColor = centralColor.g * 22.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[0]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[1]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[2]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[3]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[4]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[5]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[6]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[7]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[8]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[9]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[10]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[11]).g;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[12]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[13]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[14]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[15]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[16]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[17]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[18]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[19]).g * 2.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[20]).g * 3.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[21]).g * 3.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[22]).g * 3.0;\n" +
                "    sampleColor += texture2D(sTexture, blurCoordinates[23]).g * 3.0;\n" +
                "    sampleColor = sampleColor / 62.0;\n" +
                "    highp float highPass = centralColor.g - sampleColor + 0.5;\n" +
                "    for (int i = 0; i < 5; i++) {\n" +
                "        highPass = hardLight(highPass);\n" +
                "    }\n" +
                "    highp float lumance = dot(centralColor, W);\n" +
                "    highp float alpha = pow(lumance, params.r);\n" +
                "    highp vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;\n" +
                "    smoothColor.r = clamp(pow(smoothColor.r, params.g), 0.0, 1.0);\n" +
                "    smoothColor.g = clamp(pow(smoothColor.g, params.g), 0.0, 1.0);\n" +
                "    smoothColor.b = clamp(pow(smoothColor.b, params.g), 0.0, 1.0);\n" +
                "    highp vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);\n" +
                "    highp vec3 bianliang = max(smoothColor, centralColor);\n" +
                "    highp vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;\n" +
                "    gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);\n" +
                "    gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);\n" +
                "    gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);\n" +
                "    highp vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n" +
                "    gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);\n" +
                "    gl_FragColor.rgb = vec3(gl_FragColor.rgb + vec3(brightness));\n" +
                "}";
    }
}
