package com.example.fit4u.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit4u.R;

public class IntroVideoActivity extends AppCompatActivity {

    private FullscreenVideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_video);

        videoView = findViewById(R.id.videoView);

        // Fullscreen (מסתיר סטטוס/ניווט)
        hideSystemUi();

        // הסרטון: res/raw/homepage.mp4 (אצלך קוראים לו homepage)
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.homepage);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
            videoView.start();
        });

        videoView.setOnCompletionListener(mp -> {
            startActivity(new Intent(IntroVideoActivity.this, HomeActivity.class));
            finish();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            startActivity(new Intent(IntroVideoActivity.this, HomeActivity.class));
            finish();
            return true;
        });
    }

    private void hideSystemUi() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (videoView != null) videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) videoView.pause();
    }
}
