package cn.noobhelper.audiodemo;

import java.nio.ByteBuffer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import cn.noobhelper.audio.AudioCallback;
import cn.noobhelper.audio.AudioController;
import cn.noobhelper.audio.AudioParams;

public class MainActivity extends FragmentActivity implements AudioCallback, View.OnClickListener {

    private Button mRecordButton;
    private boolean mRecording = false;

    private AudioController mAudioController;
    private AudioParams mAudioParams;

    private static final int MSG_AUDIO_START = 1001;
    private static final int MSG_AUDIO_STOP = 1002;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUDIO_START:
                    mRecordButton.setText(R.string.stop_record);
                    break;
                case MSG_AUDIO_STOP:
                    mRecordButton.setText(R.string.start_record);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecordButton = (Button) findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(this);

        mAudioController = AudioController.getInstance();
        mAudioParams = new AudioParams();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_button:
                if (mRecording) {
                    mAudioController.stopAudio();
                } else {
                    mAudioController.setupAudio(mAudioParams, this);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mRecording) {
            mAudioController.stopAudio();
        }
        super.onDestroy();
    }

    @Override
    public void onAudioSetup(boolean result) {
        if (result) {
            mAudioController.startAudio();
        }
    }

    @Override
    public void onAudioStart(boolean result) {
        mRecording = result;
        if (result) {
            mHandler.sendEmptyMessage(MSG_AUDIO_START);
        }
    }

    @Override
    public void onAudioFrameAvailable(ByteBuffer inputBuffer, int inputLength, long timestampNanos) {

    }

    @Override
    public void onAudioStop(boolean result) {
        mRecording = !result;
        if (result) {
            mAudioController.releaseAudio();
            mHandler.sendEmptyMessage(MSG_AUDIO_STOP);
        }
    }

    @Override
    public void onAudioRelease() {

    }
}