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

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioController implements CommonHandlerListener, IAudio {
    private static final String TAG = AudioController.class.getSimpleName();

    private static final String AUDIO_THREAD_NAME = "AudioHandlerThread";

    private AudioEngine mAudioEngine;
    private HandlerThread mAudioThread;
    private Handler mAudioHandler;

    private static volatile AudioController sInstance;

    public static AudioController getInstance() {
        if (sInstance == null) {
            synchronized (AudioController.class) {
                if (sInstance == null) {
                    sInstance = new AudioController();
                }
            }
        }
        return sInstance;
    }

    private AudioController() {
    }

    public boolean hasPermission(Context context) {
        return AudioHelper.checkAudioRecordPermission(context);
    }

    public boolean isRunning() {
        return mAudioThread != null && mAudioThread.isAlive();
    }

    private void startAudioThread() {
        mAudioThread = new HandlerThread(AUDIO_THREAD_NAME);
        mAudioThread.start();
        mAudioHandler = new AudioHandler(mAudioThread.getLooper(), this);
    }

    public boolean setupAudio(AudioParams audioParams, AudioCallback audioCallback) {
        if (isRunning()) {
            Log.e(TAG, "setupAudio error! As last audio thread is alive!");
            return false;
        }
        if (mAudioEngine == null) {
            mAudioEngine = new AudioEngine();
        }
        mAudioEngine.setAudioCallback(audioCallback);
        startAudioThread();
        mAudioHandler.sendMessage(mAudioHandler.obtainMessage(AudioHandler.MSG_SETUP_AUDIO_ENGINE, audioParams));
        return true;
    }

    public void startAudio() {
        if (mAudioHandler != null) {
            mAudioHandler.sendMessage(mAudioHandler.obtainMessage(AudioHandler.MSG_START_AUDIO_ENGINE));
        }
    }

    public void stopAudio() {
        // When the AudioEngine started, there is a while(true) looping execution,
        // so we must change the state in another thread.
        // mAudioHandler.sendMessage(mAudioHandler.obtainMessage(AudioHandler.MSG_STOP_AUDIO_ENGINE));
        handleStopAudioEngine();
    }

    @Override
    public void releaseAudio() {
        if (mAudioHandler != null) {
            mAudioHandler.sendMessage(mAudioHandler.obtainMessage(AudioHandler.MSG_RELEASE_AUDIO_ENGINE));
            mAudioHandler.sendMessage(mAudioHandler.obtainMessage(AudioHandler.MSG_QUIT));
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case AudioHandler.MSG_SETUP_AUDIO_ENGINE:
                handleSetupAudioEngine((AudioParams) msg.obj);
                break;
            case AudioHandler.MSG_START_AUDIO_ENGINE:
                handleStartAudioEngine();
                break;
            case AudioHandler.MSG_STOP_AUDIO_ENGINE:
                handleStopAudioEngine();
                break;
            case AudioHandler.MSG_RELEASE_AUDIO_ENGINE:
                handleReleaseAudioEngine();
                break;
            case AudioHandler.MSG_QUIT:
                handleQuit();
                break;
            default:
                break;
        }
    }

    private void handleSetupAudioEngine(AudioParams audioParams) {
        if (mAudioEngine != null) {
            mAudioEngine.setupAudioEngine(audioParams);
        }
    }

    private void handleStartAudioEngine() {
        if (mAudioEngine != null) {
            mAudioEngine.startAudioEngine();
        }
    }

    private void handleStopAudioEngine() {
        if (mAudioEngine != null) {
            mAudioEngine.stopAudioEngine();
        }
    }

    private void handleReleaseAudioEngine() {
        if (mAudioEngine != null) {
            mAudioEngine.releaseAudioEngine();
        }
        mAudioEngine = null;
    }

    private void handleQuit() {
        mAudioThread.getLooper().quit();
        mAudioThread = null;
        mAudioHandler = null;
        sInstance = null;
    }

    private static class AudioHandler extends Handler {
        public static final int MSG_SETUP_AUDIO_ENGINE = 1001;
        public static final int MSG_START_AUDIO_ENGINE = 1002;
        public static final int MSG_STOP_AUDIO_ENGINE = 1003;
        public static final int MSG_RELEASE_AUDIO_ENGINE = 1004;
        public static final int MSG_QUIT = 1005;

        private CommonHandlerListener listener;

        public AudioHandler(Looper looper, CommonHandlerListener listener) {
            super(looper);
            this.listener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            listener.handleMessage(msg);
        }
    }
}
