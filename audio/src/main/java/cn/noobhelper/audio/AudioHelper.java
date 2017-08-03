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
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Build;

class AudioHelper {
    public static boolean checkAudioRecordPermission(Context context) {
        String packageName = context.getApplicationContext().getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkPermissionOverVersionM(context, packageName);
        } else {
            return checkPermissionUnderVersionM();
        }
    }

    private static boolean checkPermissionOverVersionM(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        int permission = packageManager.checkPermission("android.permission.RECORD_AUDIO", packageName);
        if (PackageManager.PERMISSION_GRANTED == permission) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkPermissionUnderVersionM() {
        AudioRecord audioRecord = new AudioRecord(
                AudioParams.DEFAULT_AUDIO_SOURCE,
                AudioParams.DEFAULT_SAMPLE_RATE,
                AudioParams.DEFAULT_CHANNEL_CONFIG,
                AudioParams.DEFAULT_AUDIO_FORMAT,
                AudioParams.DEFAULT_FRAME_SIZE);
        // 防止某些手机崩溃，例如联想
        try {
            // 开始录制音频
            audioRecord.startRecording();
            // 根据开始录音判断是否有录音权限
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                return false;
            }
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
