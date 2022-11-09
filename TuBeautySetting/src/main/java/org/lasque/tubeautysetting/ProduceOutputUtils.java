package org.lasque.tubeautysetting;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Range;

import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.utils.TLog;

/**
 * TuSDK
 * org.lsque.tusdkevademo.utils
 * PulseDemo
 *
 * @author H.ys
 * @Date 2020/9/15  14:48
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class ProduceOutputUtils {

    public static TuSdkSize getSupportSize(String mimeType){
        TuSdkSize screenSize = TuSdkContext.getDisplaySize();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            MediaCodecInfo codecInfo = getEncoderCodecInfo(mimeType);

            if (codecInfo == null) return screenSize;

            String[] types = codecInfo.getSupportedTypes();

            for (String type : types)
            {
                // get capabilities of the given media type of the current codec
                MediaCodecInfo.CodecCapabilities capabilitiesForType = codecInfo.getCapabilitiesForType(type);
                MediaCodecInfo.VideoCapabilities videoCapabilities = capabilitiesForType.getVideoCapabilities();

                if (videoCapabilities != null)
                {
                    TuSdkSize size = TuSdkSize.create(videoCapabilities.getSupportedWidths().getUpper(),videoCapabilities.getSupportedHeights().getUpper());
                    return size;
                }
            }
        }
        return screenSize;
    }

    /**
     * 根据mimeType获取当前设备支持的视频硬编码格式
     *
     * @param mimeType
     * @return
     */
    @SuppressWarnings("deprecation")
    public static MediaCodecInfo getEncoderCodecInfo(String mimeType)
    {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++)
        {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++)
            {
                if (types[j].equalsIgnoreCase(mimeType))
                    return codecInfo;
            }
        }
        return null;
    }
}
