package com.example.mainapp.preview.signin;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;

import com.example.mainapp.R;
import com.example.mainapp.firstpage.FirstPageActivity;

public class SignInActivity extends AppCompatActivity {


    private NbButton button;

    private NbButton signbutton;
    private RelativeLayout rlContent;
    private Handler handler;
    private Animator animator;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        button = findViewById(R.id.button_singin);
        signbutton = findViewById(R.id.button_signon);
        rlContent = findViewById(R.id.rl_content);

        rlContent.getBackground().setAlpha(0);
        handler = new Handler();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            button.startAnim();
            handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //跳转
                        gotoNew0();
                    }
                },1000);
            }
        });
        signbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signbutton.startAnim();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //跳转
                        gotoNew1();
                    }
                },1000);
            }
        });
    }

    private void gotoNew0(){

        button.gotoNew();

        final Intent intent = new Intent(this, FirstPageActivity.class);

        int xc = (button.getLeft()+button.getRight())/2;
        int yc = (button.getTop()+button.getBottom())/2;

        animator = ViewAnimationUtils.createCircularReveal(rlContent,xc,yc,0,1111);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_in,R.anim.anim_out);
                    }
                },200);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
        rlContent.getBackground().setAlpha(255);
    }

    private void gotoNew1(){

        signbutton.gotoNew();

        final Intent intent = new Intent(this, zhuceActivity.class);

        int xc = (signbutton.getLeft()+signbutton.getRight())/2;
        int yc = (signbutton.getTop()+signbutton.getBottom())/2;

        animator = ViewAnimationUtils.createCircularReveal(rlContent,xc,yc,0,1111);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_in,R.anim.anim_out);
                    }
                },200);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
        rlContent.getBackground().setAlpha(255);
    }
    @Override
    protected void onStop() {
        super.onStop();
        animator.cancel();
        rlContent.getBackground().setAlpha(0);
        button.regainBackground();
    }
}
