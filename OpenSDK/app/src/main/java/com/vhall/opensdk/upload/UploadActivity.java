package com.vhall.opensdk.upload;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vhall.opensdk.R;
import com.vhall.upload.VHUploadCallBack;
import com.vhall.upload.VhallUploadFile;

import java.io.File;


/**
 * @author hkl
 */
public class UploadActivity extends AppCompatActivity {

    private TextView upload;
    private TextView path, tvRecordId, tvSpeed;
    private SeekBar progress;
    private EditText name;
    private EditText token;
    private String filePath;
    private int count = 0;
    private CheckBox safe_video;
    private static final String TAG = "UploadActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        upload = findViewById(R.id.tv_upload);
        path = findViewById(R.id.tv_path);
        progress = findViewById(R.id.progress);
        name = findViewById(R.id.ed_name);
        token = findViewById(R.id.ed_token);
        tvRecordId = findViewById(R.id.tv_record_id);
        tvSpeed = findViewById(R.id.tv_speed);
        safe_video = findViewById(R.id.safe_video);

        findViewById(R.id.tv_choose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, 1);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(filePath)) {
                    Toast.makeText(UploadActivity.this, "上传文件不能为空  ", Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadFile(filePath);
            }
        });
    }

    private void uploadFile(String filePath) {
        VhallUploadFile.getInstance().uploadFile(token.getText().toString().trim(), filePath, name.getText().toString(), safe_video.isChecked()?"1":"0",new VHUploadCallBack() {
            @Override
            public void onSuccess(String recordId) {
                Toast.makeText(UploadActivity.this, "上传成功  " + recordId, Toast.LENGTH_SHORT).show();
                tvRecordId.setText(recordId);
            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError: " + msg);
                Toast.makeText(UploadActivity.this, "上传失败  " + msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(long currentSize, long totalSize) {
                progress.setMax(100);
                progress.setProgress((int) (currentSize * 100.0 / totalSize));
//                if (totalSize > 1024 * 1024) {
//                    tvSpeed.setText(currentSize / 1024 / 1024 + "M/" + totalSize / 1024 / 1024 + "M");
//                } else {
                    tvSpeed.setText(currentSize / 1024 + "Kb/" + totalSize / 1024 + "Kb");
//                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
        // 选取图片的返回值
        if (requestCode == 1) {
            //
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                if (uri != null) {
                    String filePath = getPath(this, uri);
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            String upLoadFilePath = file.toString();
                            this.filePath = upLoadFilePath;
                            path.setText(this.filePath);
                        }
                    } else {
                        Toast.makeText(UploadActivity.this, "无法获取文件路径，请换个文件夹试试！ ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                以下是打印示例：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                try {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
//                Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
