package com.example.makoto.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button mButtonGo;
    Button mButtonBack;
    Button mButtonOnOff;
    ImageView mImageView;
    Cursor cursor;

    boolean mShowMustGoOn = false;
    Timer mTimer;

    Handler mHandler = new Handler();

    public static final int PERMISSION_REQUESST_CODE = 100;

    int fieldIndex;
    long id;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);

        // 写真ストレージへのパーミッションの確認

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUESST_CODE);
            }
        } else {
            getContentsInfo();
        }


        // 「進む」ボタンの処理
        mButtonGo = (Button) findViewById(R.id.go);
        mButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mShowMustGoOn) {
                    if (cursor.moveToNext()) {

                        imageViewChanger();

                    } else {

                        cursor.moveToFirst();
                        imageViewChanger();

                    }

                }
            }

        });



        // 「戻る」ボタンの処理
        mButtonBack = (Button) findViewById(R.id.back);
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mShowMustGoOn) {
                    if (cursor.moveToPrevious()) {

                        imageViewChanger();

                    } else {

                        cursor.moveToLast();
                        imageViewChanger();
                    }
                }
            }
        });


        // 再生/停止ボタンの処理
        mButtonOnOff = (Button) findViewById(R.id.onof);
        mButtonOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mShowMustGoOn) {
                    mShowMustGoOn = true;
                    mButtonOnOff.setText("停止");

                    mButtonBack.setEnabled(false);
                    mButtonGo.setEnabled(false);

                    startTimer();

                } else {
                    mShowMustGoOn = false;
                    mButtonOnOff.setText("再生");

                    mButtonBack.setEnabled(true);
                    mButtonGo.setEnabled(true);

                    stopTimer();

                }
            }
        });

    }

    protected void onRestart(){
        super.onRestart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUESST_CODE);
            }
        } else {
            getContentsInfo();
        }
    }

    protected void onDestroy(){
        super.onDestroy();

        if(cursor != null) {
            if(!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUESST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    allButtonUnable();
                }
                break;
            default:
                break;
        }
    }

    private void imageViewChanger() {
        fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        id = cursor.getLong(fieldIndex);
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        mImageView.setImageURI(imageUri);

    }

    private void getContentsInfo() {
        //ストレージ内を探索するためのカーソル作成
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            imageViewChanger();
        } else {
            cursor.close();
            allButtonUnable();
        }
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (cursor.moveToNext()) {

                        fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        id = cursor.getLong(fieldIndex);
                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageURI(imageUri);
                            }
                        });
                    } else {
                        cursor.moveToFirst();

                        fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        id = cursor.getLong(fieldIndex);
                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageURI(imageUri);
                            }
                        });
                    }
                }
            }, 2000, 2000);
        }


    }

    private void stopTimer() {
        if (mTimer != null) {

            mTimer.cancel();
            mTimer = null;
        }
    }


    protected void allButtonUnable() {
        mButtonGo.setEnabled(false);
        mButtonBack.setEnabled(false);
        mButtonOnOff.setEnabled(false);
    }

}