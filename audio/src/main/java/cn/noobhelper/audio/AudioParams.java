/*
 * Copyright (C) 2017 NoobHerlper (http://noobhelper.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.noobhelper.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Parcel;
import android.os.Parcelable;

public class AudioParams implements Parcelable {
    public static final int SAMPLES_PER_FRAME = 1024; // AAC

    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int DEFAULT_SAMPLE_RATE = 16000; // 44100
    public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_FRAME_SIZE = 640;
    public static final int DEFAULT_BUFFER_FRAME_COUNT = 16;
    public static final int DEFAULT_AUDIO_BUFFER_SIZE = DEFAULT_FRAME_SIZE * DEFAULT_BUFFER_FRAME_COUNT;

    // 音频源ID
    private int mAudioSource;
    // 音频的采样率(Hz)
    private int mSampleRate;
    // 音频通道Config
    private int mChannelConfig;
    // 音频数据格式
    private int mAudioFormat;
    // 音频每一帧Buffer大小
    private int mFrameSize;
    // 用于缓存音频frame的buffer个数，用于解决外部占用framebuffer时间过长而导致数据被重写造成的错误
    private int mFrameBufferCount;
    // 给AudioRecorder分配的缓存大小
    private int mAudioBufferSize;

    public AudioParams() {
        mAudioSource = DEFAULT_AUDIO_SOURCE;
        mSampleRate = DEFAULT_SAMPLE_RATE;
        mChannelConfig = DEFAULT_CHANNEL_CONFIG;
        mAudioFormat = DEFAULT_AUDIO_FORMAT;
        mFrameSize = DEFAULT_FRAME_SIZE;
        mFrameBufferCount = DEFAULT_BUFFER_FRAME_COUNT;
        mAudioBufferSize = DEFAULT_AUDIO_BUFFER_SIZE;
    }

    protected AudioParams(Parcel in) {
        this.mAudioSource = in.readInt();
        this.mSampleRate = in.readInt();
        this.mChannelConfig = in.readInt();
        this.mAudioFormat = in.readInt();
        this.mFrameSize = in.readInt();
        this.mFrameBufferCount = in.readInt();
        this.mAudioBufferSize = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAudioSource);
        dest.writeInt(this.mSampleRate);
        dest.writeInt(this.mChannelConfig);
        dest.writeInt(this.mAudioFormat);
        dest.writeInt(this.mFrameSize);
        dest.writeInt(this.mFrameBufferCount);
        dest.writeInt(this.mAudioBufferSize);
    }

    public static final Parcelable.Creator<AudioParams> CREATOR = new Parcelable.Creator<AudioParams>() {
        @Override
        public AudioParams createFromParcel(Parcel source) {
            return new AudioParams(source);
        }

        @Override
        public AudioParams[] newArray(int size) {
            return new AudioParams[size];
        }
    };

    public int getAudioSource() {
        return mAudioSource;
    }

    public void setAudioSource(int audioSource) {
        this.mAudioSource = audioSource;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.mSampleRate = sampleRate;
    }

    public int getChannelConfig() {
        return mChannelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.mChannelConfig = channelConfig;
    }

    public int getAudioFormat() {
        return mAudioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.mAudioFormat = audioFormat;
    }

    public int getFrameSize() {
        return mFrameSize;
    }

    public void setFrameSize(int frameSize) {
        this.mFrameSize = frameSize;
    }

    public int getFrameBufferCount() {
        return mFrameBufferCount;
    }

    public void setFrameBufferCount(int frameBufferCount) {
        this.mFrameBufferCount = frameBufferCount;
    }

    public int getAudioBufferSize() {
        return mAudioBufferSize;
    }

    public void setAudioBufferSize(int audioBufferSize) {
        this.mAudioBufferSize = audioBufferSize;
    }
}
