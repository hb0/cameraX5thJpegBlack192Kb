package com.example.cameraxblackjpegbug;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

public class App extends android.app.Application implements CameraXConfig.Provider {

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
