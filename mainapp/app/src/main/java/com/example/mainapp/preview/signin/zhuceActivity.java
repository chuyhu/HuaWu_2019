package com.example.mainapp.preview.signin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.mainapp.R;
import com.example.mainapp.firstpage.FirstPageActivity;
import com.example.mainapp.lbs.LbsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class zhuceActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private ImageView picture;
    private Uri imageUri;
    private Matrix matrix = new Matrix();

    private Button zhucebutton;
    private Button daohangButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuce);
        zhucebutton = (Button)findViewById(R.id.zhucebutton) ;
        daohangButton = findViewById(R.id.button_daohang);

        final ImageView takePhoto = (ImageView) findViewById(R.id.take_photo);
        takePhoto.setVisibility(View.VISIBLE);
        picture = (ImageView)findViewById(R.id.picture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(getExternalCacheDir(),
                        "output_image.jpg");
                try{
                    if (outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24){
                    imageUri = FileProvider.getUriForFile(zhuceActivity.this,
                            "com.example.mainapp.provider",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }
                // 启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
                takePhoto.setVisibility(View.INVISIBLE);
            }
        });


        zhucebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(zhuceActivity.this, FirstPageActivity.class));
            }
        });

        daohangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(zhuceActivity.this, LbsActivity.class));
            }
        });
        
    }
    private Bitmap rotaingImageView(int angle2, Bitmap bitmap) {
        //旋转图片 动作
        matrix.postRotate(angle2);
        System.out.println("angle2=" + angle2);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try{
                        // 将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(rotaingImageView(90,bitmap));
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
