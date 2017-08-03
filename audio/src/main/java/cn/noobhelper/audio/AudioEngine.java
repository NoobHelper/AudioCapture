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

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.media.AudioRecord;
import android.util.Log;

class AudioEngine {
    private static final String TAG = AudioEngine.class.getSimpleName();

    private AudioRecord mAudioRecord;
    private AudioParams mAudioParams;
    private byte[] mInputBuffer = null;
    private ArrayList<ByteBuffer> mByteBufferList = null;
    private int mCurrentFrameNum = 0;
    private AudioCallback mAudioCallback;
    private static volatile boolean mRecording = false;

    public AudioEngine() {
    }

    public void setAudioCallback(AudioCallback audioCallback) {
        this.mAudioCallback = audioCallback;
    }

    public void setupAudioEngine(AudioParams audioParams) {
        int minBufferSize = AudioRecord.getMinBufferSize(audioParams.getSampleRate(), audioParams.getChannelConfig(),
                audioParams.getAudioFormat());
        if (audioParams.getFrameSize() < minBufferSize) {
            int optimalFrameSize =
                    ((minBufferSize / AudioParams.SAMPLES_PER_FRAME) + 1) * AudioParams.SAMPLES_PER_FRAME * 2;
            audioParams.setFrameSize(optimalFrameSize);
        }

        mAudioRecord = new AudioRecord(
                audioParams.getAudioSource(),       // source
                audioParams.getSampleRate(),        // sample rate, hz
                audioParams.getChannelConfig(),     // channel config
                audioParams.getAudioFormat(),       // audio format
                audioParams.getAudioBufferSize());  // buffer size (bytes)

        mAudioParams = audioParams;

        if (mAudioCallback != null) {
            mAudioCallback.onAudioSetup(true);
        }
    }

    public void startAudioEngine() {
        startAudioRecord();
        readBackAudioFrame();
    }

    public void stopAudioEngine() {
        mRecording = false;
    }

    public void releaseAudioEngine() {
        if (!mRecording) {
            mAudioRecord.release();
            mAudioRecord = null;
            if (mAudioCallback != null) {
                mAudioCallback.onAudioRelease();
            }
            mAudioCallback = null;
        }
    }

    private void startAudioRecord() {
        boolean result = false;
        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            try {
                mAudioRecord.startRecording();
                if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    result = true;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        mRecording = result;

        if (mAudioCallback != null) {
            mAudioCallback.onAudioStart(result);
        }
    }

    private void readBackAudioFrame() {
        if (mAudioParams.getFrameSize() <= 0) {
            return;
        }

        if (mByteBufferList == null) {
            mByteBufferList = new ArrayList<>();
            for (int i = 0; i < mAudioParams.getFrameBufferCount(); i++) {
                mByteBufferList.add(ByteBuffer.allocate(mAudioParams.getFrameSize()));
            }
        }

        mCurrentFrameNum = 0;
        if (mInputBuffer == null) {
            mInputBuffer = new byte[mAudioParams.getFrameSize()];
        }

        long presentationTimeNs = System.nanoTime();
        while (mRecording) {
            int inputLength = mAudioRecord.read(mInputBuffer, 0, mInputBuffer.length);
            ByteBuffer outputBuffer = mByteBufferList.get(mCurrentFrameNum);
            if (inputLength == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.e(TAG, "Audio read error");
            } else if (mAudioCallback != null && outputBuffer != null && outputBuffer.capacity() >= inputLength) {
                presentationTimeNs += (inputLength / 2 * 1000000000L / mAudioRecord.getSampleRate());
                outputBuffer.clear();
                outputBuffer.position(0);
                outputBuffer.put(mInputBuffer, 0, inputLength);
                outputBuffer.flip();
                mAudioCallback.onAudioFrameAvailable(outputBuffer, inputLength, presentationTimeNs);
            }
            mCurrentFrameNum++;
            mCurrentFrameNum %= mAudioParams.getFrameBufferCount();
        }

        mByteBufferList = null;
        mInputBuffer = null;
        try {
            mAudioRecord.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mAudioCallback != null) {
            mAudioCallback.onAudioStop(true);
        }
    }
}
