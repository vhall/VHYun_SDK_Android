package com.vhall.opensdk.util;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerationAndroid;

import java.util.List;

/**
 * Created by zwp on 2019/3/15
 */
public class CameraUtil {

    public static boolean compareDpi(int with) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        String[] names = enumerator.getDeviceNames();
        String name = names[0];
        for (String item : names) {
            if (enumerator.isFrontFacing(item)) {
                name = item;
                break;
            }
        }
        List<CameraEnumerationAndroid.CaptureFormat> captureFormats = enumerator.getSupportedFormats(name);
        for (CameraEnumerationAndroid.CaptureFormat format : captureFormats) {
            if (format.width == with) {
                return true;
            }
        }
        return false;
    }
}
