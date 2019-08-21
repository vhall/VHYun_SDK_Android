package com.vhall.opensdk.document;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.opensdk.R;
import com.vhall.ops.VHOPS;

import java.io.File;

/**
 * Created by zwp on 2019/4/25
 */
public class UploadDocumentActivity extends Activity {

    private TextView tvPath, tvConfig;
    private Button btnFromFile, btnFromCamera, btnUpload;
    private ProgressBar pb;
    private String mAccessToken = "";
    private String imagePath;
    private EditText etReName;
    private static final String TAG = "UploadDocumentActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_layout);
        mAccessToken = getIntent().getStringExtra("token");
        intiView();

    }

    private void intiView() {
        tvConfig = findViewById(R.id.tv_config);
        tvConfig.setText(VHOPS.getUploadConfig());
        tvPath = findViewById(R.id.tv_file_path);
        btnFromFile = findViewById(R.id.btn_from_file);
        btnFromCamera = findViewById(R.id.btn_take_photo);
        btnUpload = findViewById(R.id.btn_upload);
        etReName = findViewById(R.id.et_rename);
        pb = findViewById(R.id.progress_bar);
        pb.setMax(100);
    }

    public void fromFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "appImage";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        imagePath = path + File.separator + System.currentTimeMillis() + ".jpg";
        Uri contentUri = FileProvider.getUriForFile(this, getPackageName(), new File(imagePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        startActivityForResult(intent, 2);
    }

    public void uploadDocument(View view) {
        VHOPS.upload(tvPath.getText().toString().trim(), etReName.getText().toString().trim(), mAccessToken, new VHOPS.DocUploadCallback() {
            @Override
            public void onSuccess(String documentId) {
                tvPath.setText(documentId);
            }

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                Toast.makeText(UploadDocumentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done) {
                Log.e(TAG, "onProgress: totalBytes=" + totalBytes + "remainingBytes=" + remainingBytes);
                int no = (int) ((totalBytes - remainingBytes) * 1.0 / totalBytes * 100);
                pb.setProgress(no);
                Log.e(TAG, "onProgress: no = " + no);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = getPath(this, uri);
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        String upLoadFilePath = file.toString();
                        tvPath.setText(upLoadFilePath);
                    }
                } else {
                    tvPath.setText("当前目录不支持，换个目录试试！");
                }
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            tvPath.setText(imagePath);

        }
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
                    Log.i(TAG, "isDownloadsDocument***" + uri.toString());
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