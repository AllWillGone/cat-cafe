package com.catcafe.app.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.UserDetail;
import com.catcafe.app.model.UserUpdateRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProfileActivity extends BaseToolbarActivity {
    public static Intent intent(Context context) {
        return new Intent(context, ProfileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SessionManager(this).isAdmin()) {
            Toast.makeText(this, "管理员账号为共用账号，不能修改个人信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_profile);
        setupToolbar(R.id.profileToolbar);

        TextInputEditText nameInput = findViewById(R.id.profileNameInput);
        TextInputEditText phoneInput = findViewById(R.id.profilePhoneInput);
        TextInputEditText birthdayInput = findViewById(R.id.profileBirthdayInput);
        TextInputEditText avatarInput = findViewById(R.id.profileAvatarInput);
        RadioGroup genderGroup = findViewById(R.id.profileGenderGroup);
        ShapeableImageView avatarImage = findViewById(R.id.profileAvatarImage);
        TextView headerName = findViewById(R.id.profileHeaderName);
        TextView headerType = findViewById(R.id.profileHeaderType);
        TextView headerMeta = findViewById(R.id.profileHeaderMeta);
        MaterialButton submitButton = findViewById(R.id.profileSubmitButton);

        avatarInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAvatar(avatarImage, s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        MaterialButton uploadButton = findViewById(R.id.profileUploadButton);
        ImageView uploadPreview = findViewById(R.id.profileUploadPreview);

        ActivityResultLauncher<String> pickMedia = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatarFile(uri, avatarInput, uploadPreview);
                    }
                }
        );

        uploadButton.setOnClickListener(v -> pickMedia.launch("image/*"));

        birthdayInput.setOnClickListener(v -> showBirthdayPicker(birthdayInput));

        NetworkHelper.enqueue(this, ApiClient.getService(this).getMe(), new ApiCallback<UserDetail>() {
            @Override
            public void onSuccess(UserDetail data) {
                nameInput.setText(data.userName);
                phoneInput.setText(data.userPhone);
                birthdayInput.setText(data.birthday);
                renderGender(data.gender, genderGroup);
                avatarInput.setText(data.userAvatar);
                renderHeader(data, headerName, headerType, headerMeta, avatarImage);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> {
            String userName = textOf(nameInput);
            String phone = textOf(phoneInput);
            String birthday = textOrNull(birthdayInput);
            String avatar = textOrNull(avatarInput);
            if (userName.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            SessionManager session = new SessionManager(this);
            NetworkHelper.enqueue(this,
                    ApiClient.getService(this).updateMe(new UserUpdateRequest(
                            userName,
                            selectedGender(genderGroup),
                            birthday,
                            phone,
                            avatar
                    )),
                    new ApiCallback<UserDetail>() {
                        @Override
                        public void onSuccess(UserDetail data) {
                            session.saveUserDetail(data);
                            Toast.makeText(ProfileActivity.this, "已更新", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void uploadAvatarFile(Uri uri, TextInputEditText avatarInput, ImageView preview) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "无法读取图片", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            inputStream.close();
            byte[] bytes = buffer.toByteArray();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "avatar");

            NetworkHelper.enqueue(this,
                    ApiClient.getService(this).uploadAvatar(typeBody, body),
                    new ApiCallback<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> data) {
                            Object urlObj = data.get("url");
                            String url = urlObj != null ? urlObj.toString() : "";
                            avatarInput.setText(url);
                            runOnUiThread(() -> {
                                Glide.with(ProfileActivity.this)
                                        .load(AppConfig.buildImageUrl(url))
                                        .placeholder(R.drawable.ic_image_placeholder)
                                        .error(R.drawable.ic_image_placeholder)
                                        .into(preview);
                                preview.setVisibility(android.view.View.VISIBLE);
                            });
                            Toast.makeText(ProfileActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(ProfileActivity.this, "上传失败: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "读取图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String textOrNull(TextInputEditText input) {
        String value = textOf(input);
        return value.isEmpty() ? null : value;
    }

    private void renderGender(Integer gender, RadioGroup group) {
        if (gender == null) {
            group.clearCheck();
        } else if (gender == 1) {
            group.check(R.id.profileGenderMale);
        } else if (gender == 2) {
            group.check(R.id.profileGenderFemale);
        } else {
            group.clearCheck();
        }
    }

    private Integer selectedGender(RadioGroup group) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == R.id.profileGenderMale) {
            return 1;
        }
        if (checkedId == R.id.profileGenderFemale) {
            return 2;
        }
        return null;
    }

    private void showBirthdayPicker(TextInputEditText input) {
        Calendar calendar = Calendar.getInstance();
        String current = textOf(input);
        if (current.matches("\\d{4}-\\d{2}-\\d{2}")) {
            calendar.set(
                    Integer.parseInt(current.substring(0, 4)),
                    Integer.parseInt(current.substring(5, 7)) - 1,
                    Integer.parseInt(current.substring(8, 10))
            );
        }
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> input.setText(String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                )),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void renderHeader(UserDetail user, TextView name, TextView type, TextView meta,
                              ShapeableImageView avatar) {
        name.setText(emptyToDefault(user.userName, "用户"));
        type.setText(user.userType == 1 ? "管理员账号" : "普通用户");
        meta.setText("UID " + user.userId + " · " + formatRegisterTime(user.registerTime));
        loadAvatar(avatar, user.userAvatar);
    }

    private void loadAvatar(ShapeableImageView avatar, String path) {
        Glide.with(this)
                .load(AppConfig.buildImageUrl(path))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(avatar);
    }

    private String emptyToDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String formatRegisterTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "注册时间未知";
        }
        return "注册于 " + value.substring(0, Math.min(10, value.length()));
    }
}
