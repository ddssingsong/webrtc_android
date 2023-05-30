/*
 *  Copyright 2018 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc.audio;

import android.content.Context;
import android.media.AudioManager;

import org.webrtc.JniCommon;
import org.webrtc.Logging;

/**
 * AudioDeviceModule implemented using android.media.AudioRecord as input and
 * android.media.AudioTrack as output.
 */
public class JavaAudioDeviceModule implements AudioDeviceModule {
    private static final String TAG = "JavaAudioDeviceModule";

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private final Context context;
        private final AudioManager audioManager;
        private int inputSampleRate;
        private int outputSampleRate;
        private int audioSource = WebRtcAudioRecord.DEFAULT_AUDIO_SOURCE;
        private int audioFormat = WebRtcAudioRecord.DEFAULT_AUDIO_FORMAT;
        private AudioTrackErrorCallback audioTrackErrorCallback;
        private AudioRecordErrorCallback audioRecordErrorCallback;
        private SamplesReadyCallback samplesReadyCallback;
        private boolean useHardwareAcousticEchoCanceler = isBuiltInAcousticEchoCancelerSupported();
        private boolean useHardwareNoiseSuppressor = isBuiltInNoiseSuppressorSupported();
        private boolean useStereoInput;
        private boolean useStereoOutput;

        private Builder(Context context) {
            this.context = context;
            this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            this.inputSampleRate = WebRtcAudioManager.getSampleRate(audioManager);
            this.outputSampleRate = WebRtcAudioManager.getSampleRate(audioManager);
        }

        /**
         * Call this method if the default handling of querying the native sample rate shall be
         * overridden. Can be useful on some devices where the available Android APIs are known to
         * return invalid results.
         */

        // 设置音频采样率
        // Android放音的采样率固定为44.1khz，录音的采样率固定为8khz，
        // 因此底层的音频设备驱动只需设置好这两个固定的采样率。
        // 如果上层传过来的采样率与其不符的话，则Android Framework层会对音频流做resample（重采样）处理。
        public Builder setSampleRate(int sampleRate) {
            Logging.d(TAG, "Input/Output sample rate overridden to: " + sampleRate);
            this.inputSampleRate = sampleRate;
            this.outputSampleRate = sampleRate;
            return this;
        }

        /**
         * Call this method to specifically override input sample rate.
         */
        // 设置输入音频采样率
        public Builder setInputSampleRate(int inputSampleRate) {
            Logging.d(TAG, "Input sample rate overridden to: " + inputSampleRate);
            this.inputSampleRate = inputSampleRate;
            return this;
        }

        /**
         * Call this method to specifically override output sample rate.
         */
        // 设置输出音频采样率
        public Builder setOutputSampleRate(int outputSampleRate) {
            Logging.d(TAG, "Output sample rate overridden to: " + outputSampleRate);
            this.outputSampleRate = outputSampleRate;
            return this;
        }

        /**
         * Call this to change the audio source. The argument should be one of the values from
         * android.media.MediaRecorder.AudioSource. The default is AudioSource.VOICE_COMMUNICATION.
         */
        // 设置音频源
        public Builder setAudioSource(int audioSource) {
            this.audioSource = audioSource;
            return this;
        }

        /**
         * Call this to change the audio format. The argument should be one of the values from
         * android.media.AudioFormat ENCODING_PCM_8BIT, ENCODING_PCM_16BIT or ENCODING_PCM_FLOAT.
         * Default audio data format is PCM 16 bit per sample.
         * Guaranteed to be supported by all devices.
         */

        // 设置音频格式
        public Builder setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }

        /**
         * Set a callback to retrieve errors from the AudioTrack.
         */
        public Builder setAudioTrackErrorCallback(AudioTrackErrorCallback audioTrackErrorCallback) {
            this.audioTrackErrorCallback = audioTrackErrorCallback;
            return this;
        }

        /**
         * Set a callback to retrieve errors from the AudioRecord.
         */
        public Builder setAudioRecordErrorCallback(AudioRecordErrorCallback audioRecordErrorCallback) {
            this.audioRecordErrorCallback = audioRecordErrorCallback;
            return this;
        }

        /**
         * Set a callback to listen to the raw audio input from the AudioRecord.
         */
        public Builder setSamplesReadyCallback(SamplesReadyCallback samplesReadyCallback) {
            this.samplesReadyCallback = samplesReadyCallback;
            return this;
        }

        /**
         * Control if the built-in HW noise suppressor should be used or not. The default is on if it is
         * supported. It is possible to query support by calling isBuiltInNoiseSuppressorSupported().
         */

        // 设置是否开启硬件噪声抑制器，默认如果支持就打开
        public Builder setUseHardwareNoiseSuppressor(boolean useHardwareNoiseSuppressor) {
            if (useHardwareNoiseSuppressor && !isBuiltInNoiseSuppressorSupported()) {
                Logging.e(TAG, "HW NS not supported");
                useHardwareNoiseSuppressor = false;
            }
            this.useHardwareNoiseSuppressor = useHardwareNoiseSuppressor;
            return this;
        }

        /**
         * Control if the built-in HW acoustic echo canceler should be used or not. The default is on if
         * it is supported. It is possible to query support by calling
         * isBuiltInAcousticEchoCancelerSupported().
         */
        // 设置是否开启内置回声消除，如果支持，默认打开
        public Builder setUseHardwareAcousticEchoCanceler(boolean useHardwareAcousticEchoCanceler) {
            if (useHardwareAcousticEchoCanceler && !isBuiltInAcousticEchoCancelerSupported()) {
                Logging.e(TAG, "HW AEC not supported");
                useHardwareAcousticEchoCanceler = false;
            }
            this.useHardwareAcousticEchoCanceler = useHardwareAcousticEchoCanceler;
            return this;
        }

        /**
         * Control if stereo input should be used or not. The default is mono.
         */

        // 控制是否使用立体声输入
        public Builder setUseStereoInput(boolean useStereoInput) {
            this.useStereoInput = useStereoInput;
            return this;
        }

        /**
         * Control if stereo output should be used or not. The default is mono.
         */

        // 控制是否使用立体声输出
        public Builder setUseStereoOutput(boolean useStereoOutput) {
            this.useStereoOutput = useStereoOutput;
            return this;
        }

        /**
         * Construct an AudioDeviceModule based on the supplied arguments. The caller takes ownership
         * and is responsible for calling release().
         */
        public AudioDeviceModule createAudioDeviceModule() {
            Logging.d(TAG, "createAudioDeviceModule");
            if (useHardwareNoiseSuppressor) {
                Logging.d(TAG, "HW NS will be used.");
            } else {
                if (isBuiltInNoiseSuppressorSupported()) {
                    Logging.d(TAG, "Overriding default behavior; now using WebRTC NS!");
                }
                Logging.d(TAG, "HW NS will not be used.");
            }
            if (useHardwareAcousticEchoCanceler) {
                Logging.d(TAG, "HW AEC will be used.");
            } else {
                if (isBuiltInAcousticEchoCancelerSupported()) {
                    Logging.d(TAG, "Overriding default behavior; now using WebRTC AEC!");
                }
                Logging.d(TAG, "HW AEC will not be used.");
            }
            final WebRtcAudioRecord audioInput = new WebRtcAudioRecord(context, audioManager, audioSource,
                    audioFormat, audioRecordErrorCallback, samplesReadyCallback,
                    useHardwareAcousticEchoCanceler, useHardwareNoiseSuppressor);
            final WebRtcAudioTrack audioOutput =
                    new WebRtcAudioTrack(context, audioManager, audioTrackErrorCallback);
            return new JavaAudioDeviceModule(context, audioManager, audioInput, audioOutput,
                    inputSampleRate, outputSampleRate, useStereoInput, useStereoOutput);
        }
    }

    /* AudioRecord */
    // Audio recording error handler functions.
    public enum AudioRecordStartErrorCode {
        AUDIO_RECORD_START_EXCEPTION,
        AUDIO_RECORD_START_STATE_MISMATCH,
    }

    public static interface AudioRecordErrorCallback {
        void onWebRtcAudioRecordInitError(String errorMessage);

        void onWebRtcAudioRecordStartError(AudioRecordStartErrorCode errorCode, String errorMessage);

        void onWebRtcAudioRecordError(String errorMessage);
    }

    /**
     * Contains audio sample information.
     */
    public static class AudioSamples {
        /**
         * See {@link android.media.AudioRecord#getAudioFormat()}
         */
        private final int audioFormat;
        /**
         * See {@link android.media.AudioRecord#getChannelCount()}
         */
        private final int channelCount;
        /**
         * See {@link android.media.AudioRecord#getSampleRate()}
         */
        private final int sampleRate;

        private final byte[] data;

        public AudioSamples(int audioFormat, int channelCount, int sampleRate, byte[] data) {
            this.audioFormat = audioFormat;
            this.channelCount = channelCount;
            this.sampleRate = sampleRate;
            this.data = data;
        }

        public int getAudioFormat() {
            return audioFormat;
        }

        public int getChannelCount() {
            return channelCount;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public byte[] getData() {
            return data;
        }
    }

    /**
     * Called when new audio samples are ready. This should only be set for debug purposes
     */
    public static interface SamplesReadyCallback {
        void onWebRtcAudioRecordSamplesReady(AudioSamples samples);
    }

    /* AudioTrack */
    // Audio playout/track error handler functions.
    public enum AudioTrackStartErrorCode {
        AUDIO_TRACK_START_EXCEPTION,
        AUDIO_TRACK_START_STATE_MISMATCH,
    }

    public static interface AudioTrackErrorCallback {
        void onWebRtcAudioTrackInitError(String errorMessage);

        void onWebRtcAudioTrackStartError(AudioTrackStartErrorCode errorCode, String errorMessage);

        void onWebRtcAudioTrackError(String errorMessage);
    }

    /**
     * Returns true if the device supports built-in HW AEC, and the UUID is approved (some UUIDs can
     * be excluded).
     */
    public static boolean isBuiltInAcousticEchoCancelerSupported() {
        return WebRtcAudioEffects.isAcousticEchoCancelerSupported();
    }

    /**
     * Returns true if the device supports built-in HW NS, and the UUID is approved (some UUIDs can be
     * excluded).
     */
    public static boolean isBuiltInNoiseSuppressorSupported() {
        return WebRtcAudioEffects.isNoiseSuppressorSupported();
    }

    private final Context context;
    private final AudioManager audioManager;
    private final WebRtcAudioRecord audioInput;
    private final WebRtcAudioTrack audioOutput;
    private final int inputSampleRate;
    private final int outputSampleRate;
    private final boolean useStereoInput;
    private final boolean useStereoOutput;

    private final Object nativeLock = new Object();
    private long nativeAudioDeviceModule;

    private JavaAudioDeviceModule(Context context, AudioManager audioManager,
                                  WebRtcAudioRecord audioInput, WebRtcAudioTrack audioOutput, int inputSampleRate,
                                  int outputSampleRate, boolean useStereoInput, boolean useStereoOutput) {
        this.context = context;
        this.audioManager = audioManager;
        this.audioInput = audioInput;
        this.audioOutput = audioOutput;
        this.inputSampleRate = inputSampleRate;
        this.outputSampleRate = outputSampleRate;
        this.useStereoInput = useStereoInput;
        this.useStereoOutput = useStereoOutput;
    }

    @Override
    public long getNativeAudioDeviceModulePointer() {
        synchronized (nativeLock) {
            if (nativeAudioDeviceModule == 0) {
                nativeAudioDeviceModule = nativeCreateAudioDeviceModule(context, audioManager, audioInput,
                        audioOutput, inputSampleRate, outputSampleRate, useStereoInput, useStereoOutput);
            }
            return nativeAudioDeviceModule;
        }
    }

    @Override
    public void release() {
        synchronized (nativeLock) {
            if (nativeAudioDeviceModule != 0) {
                JniCommon.nativeReleaseRef(nativeAudioDeviceModule);
                nativeAudioDeviceModule = 0;
            }
        }
    }

    // 设置扬声器
    @Override
    public void setSpeakerMute(boolean mute) {
        Logging.d(TAG, "setSpeakerMute: " + mute);
        audioOutput.setSpeakerMute(mute);
    }

    // 设置静音
    @Override
    public void setMicrophoneMute(boolean mute) {
        Logging.d(TAG, "setMicrophoneMute: " + mute);
        audioInput.setMicrophoneMute(mute);
    }

    private static native long nativeCreateAudioDeviceModule(Context context,
                                                             AudioManager audioManager, WebRtcAudioRecord audioInput, WebRtcAudioTrack audioOutput,
                                                             int inputSampleRate, int outputSampleRate, boolean useStereoInput, boolean useStereoOutput);
}
