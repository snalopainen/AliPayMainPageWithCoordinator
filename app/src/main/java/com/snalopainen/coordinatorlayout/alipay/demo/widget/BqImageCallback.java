package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.graphics.Bitmap;

/**
 * Created by jinyan on 16/8/16.
 */
public interface BqImageCallback {

    /**
     * bitmap加载完成回调。注意: 此调用在后台线程!!!
     *
     * @param bitmap null表示加载失败
     */
    void onBitmapLoaded(Bitmap bitmap);

}
