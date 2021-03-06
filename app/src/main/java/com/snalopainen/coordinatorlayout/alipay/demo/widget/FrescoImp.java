package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Fresco支持的uri格式：
 * <p> 1. 远程图片 	http://, https://
 * <p> 2. 本地文件 	file://
 * <p> 3. Content provider 	content://
 * <p> 4. asset目录下的资源 	asset://
 * <p> 5. res目录下的资源 	res://包名/R.drawable.ic_launcher
 * <p> 6. Uri中指定图片数据 	data:mime/type;base64,
 * <p/>
 * Created by JinYan on 2016/7/29.
 */
final class FrescoImp implements ImageImp {

    private Executor loadBitmapExecutor;

    @Override
    public void initialize(Context context, Object initParam) {
        if (initParam == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                    .newBuilder(context, okHttpClient)
                    .setDownsampleEnabled(true)
                    .build();
            initParam = config;
        }
        Fresco.initialize(context, (ImagePipelineConfig) initParam);
    }

    @Override
    public void loadBitmap(String uri, final BqImageCallback callback) {
        loadBitmap(uri, 0, 0, callback);
    }

    @Override
    public void loadBitmap(String uri, final int width, final int height, final BqImageCallback callback) {
        if (BqImage.getGlobalIntercepter() != null) {
            uri = BqImage.getGlobalIntercepter().intercept(null, Integer.MAX_VALUE, Integer.MAX_VALUE, uri);
        }
        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri));
        if (width > 0 && height > 0) {
            imageRequestBuilder.setResizeOptions(new ResizeOptions(width, height));
        }
        imageRequestBuilder.setAutoRotateEnabled(true);
        ImageRequest imageRequest = imageRequestBuilder.build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);

        DataSubscriber dataSubscriber =
                new BaseBitmapDataSubscriber() {
                    @Override
                    protected void onNewResultImpl(Bitmap bitmap) {
                        if (bitmap == null) {
                            callback.onBitmapLoaded(null);
                        } else {
                            try {
                                if (width == 0 || height == 0) {
                                    callback.onBitmapLoaded(Bitmap.createBitmap(bitmap));
                                } else {
                                    /* 因为fresco resize不是精确活动width*height到图片，需要进行等比缩放 */
                                    final int bw = bitmap.getWidth();
                                    final int bh = bitmap.getHeight();
                                    callback.onBitmapLoaded(Bitmap.createScaledBitmap(bitmap, width, bh * width / bw, true));
                                }
                            } catch (Throwable t) {
                                callback.onBitmapLoaded(null);
                            }
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        callback.onBitmapLoaded(null);
                    }
                };
        if (loadBitmapExecutor == null) {
            loadBitmapExecutor = new ThreadPoolExecutor(0, 3, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        }
        dataSource.subscribe(dataSubscriber, loadBitmapExecutor);
    }

    @Override
    public void clearCache() {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearDiskCaches();
    }

    @Override
    public long getCacheSize() {
        return Fresco.getImagePipelineFactory().getMainFileCache().getSize();
    }
}
