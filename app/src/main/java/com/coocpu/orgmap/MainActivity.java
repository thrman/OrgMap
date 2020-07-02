package com.coocpu.orgmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    /**
     * 是否是横屏
     * 默认false
     */
    boolean mIsPreviewLand = false;

    OrgMapView orgMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        orgMapView = findViewById(R.id.orgMapView);

        /*设置数据源*/
        String val = orgMapView.getOriginalFundData(this);
        Gson gson = new Gson();
        MorgDataBean data = gson.fromJson(val, MorgDataBean.class);
        data.setOrgnameShow(data.getOrgname());
        orgMapView.setData(data);
    }

    public void expand(View view) {
        setRequestedOrientation(!mIsPreviewLand ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mIsPreviewLand = !mIsPreviewLand;
    }
}
