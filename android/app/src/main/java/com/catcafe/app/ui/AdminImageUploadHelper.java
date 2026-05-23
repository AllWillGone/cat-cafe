package com.catcafe.app.ui;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

final class AdminImageUploadHelper {
    private AdminImageUploadHelper() {
    }

    static void loadPreview(Activity activity, ImageView preview, String path) {
        if (preview == null) {
            return;
        }
        if (path == null || path.trim().isEmpty()) {
            preview.setVisibility(View.GONE);
            return;
        }
        preview.setVisibility(View.VISIBLE);
        Glide.with(activity)
                .load(AppConfig.buildImageUrl(path))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(preview);
    }

    static void upload(Activity activity, Uri uri, String type, String filenameBase,
                       TextInputEditText targetInput, ImageView preview) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(activity, "无法读取图片", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), buffer.toByteArray());
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", filenameBase + resolveExtension(activity, uri), requestFile);
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), type);

            NetworkHelper.enqueue(activity,
                    ApiClient.getService(activity).uploadAdminImage(typeBody, body),
                    new ApiCallback<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> data) {
                            Object urlObj = data == null ? null : data.get("url");
                            String url = urlObj == null ? "" : urlObj.toString();
                            targetInput.setText(url);
                            loadPreview(activity, preview, url);
                            Toast.makeText(activity, "上传成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(activity, "上传失败: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(activity, "读取图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static String resolveExtension(Activity activity, Uri uri) {
        String mimeType = activity.getContentResolver().getType(uri);
        String extension = mimeType == null ? null : MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension == null || extension.trim().isEmpty()) {
            return ".jpg";
        }
        return "." + extension;
    }
}
