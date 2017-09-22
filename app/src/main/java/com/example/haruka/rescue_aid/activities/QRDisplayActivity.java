package com.example.haruka.rescue_aid.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.haruka.rescue_aid.R;
import com.example.haruka.rescue_aid.utils.MedicalCertification;
import com.example.haruka.rescue_aid.utils.Utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRDisplayActivity extends FragmentActivity {

    private String mResult;
    private Intent mGetResultIntent;
    private Context context;
    private MedicalCertification medicalCertification;
    private boolean throughInterview;

    private LoaderCallbacks<Bitmap> callbacks = new LoaderCallbacks<Bitmap>() {
        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle bundle) {
            EncodeTaskLoader loader = new EncodeTaskLoader(
                    getApplicationContext(), bundle.getString("contents"));
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {
        }

        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
            getSupportLoaderManager().destroyLoader(0);
            if (bitmap == null) {
                Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_SHORT).show();
            } else {
                ImageView imageView = (ImageView) findViewById(R.id.result_view);
                imageView.setImageBitmap(bitmap);
            }
        }
    };

    public static class EncodeTaskLoader extends AsyncTaskLoader<Bitmap> {
        private String mContents;

        public EncodeTaskLoader(Context context, String contents) {
            super(context);
            mContents = contents;
        }

        @Override
        public Bitmap loadInBackground() {
            try {
                return encode(mContents);
            } catch (Exception e) {
                return null;
            }
        }

        private Bitmap encode(String contents) throws Exception {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bm = null;
            bm = writer.encode(mContents, BarcodeFormat.QR_CODE, 300, 300);
            // ピクセルを作る
            int width = bm.getWidth();
            int height = bm.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bm.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr);

        context = this;

        findViewById(R.id.btn_qr_show_certification).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CertificationActivity.class);
                intent.putExtra(Utils.TAG_INTENT_CERTIFICATION, medicalCertification);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.btn_qr_show_result).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResultActivity.class);
                intent.putExtra(Utils.TAG_INTENT_CERTIFICATION, medicalCertification);
                startActivity(intent);
                finish();
            }
        });

        throughInterview = getIntent().getBooleanExtra("THROUGH_INTERVIEW", false);
    }

    @Override
    protected void onResume(){
        super.onResume();

        mGetResultIntent = getIntent();
        medicalCertification = (MedicalCertification) mGetResultIntent.getSerializableExtra(Utils.TAG_INTENT_CERTIFICATION);
        mResult = medicalCertification.toString();
        Bundle bundle = new Bundle();
        bundle.putString("contents", mResult);
        getSupportLoaderManager().initLoader(0, bundle, callbacks);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if (throughInterview) {
                showDialog(0);
                return true;
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {

            case 0:
                //ダイアログの作成(AlertDialog.Builder)
                return new AlertDialog.Builder(QRDisplayActivity.this)
                        .setMessage("スタート画面に戻りますか？\n今回の問診データは\n「タイトル」→「問診履歴」\nから見れます")
                        .setCancelable(false)
                        .setPositiveButton("戻る", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton("戻らない", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .create();
        }
        return null;
    }
}