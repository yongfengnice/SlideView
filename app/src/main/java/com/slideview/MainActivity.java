package com.slideview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.slideview.view.InterceptRelativeLayout;
import com.slideview.view.SlideView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ScrollView mScrollView;
    private InterceptRelativeLayout mRlSlide;
    private ListView mLvTab;
    private SlideView mTvHandle;
    private RelativeLayout.LayoutParams mLayoutParams;

    private void assignViews() {
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mRlSlide = (InterceptRelativeLayout) findViewById(R.id.rl_slide);
        mLvTab = (ListView) findViewById(R.id.lv_tab);
        mTvHandle = (SlideView) findViewById(R.id.tv_handle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        assignData();
    }

    private void assignData() {
        final List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i + "");
        }
        mLvTab.setAdapter(new ArrayAdapter<String>(this, R.layout.text, R.id.tv, list));
        mLvTab.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        mTvHandle.setSlideListener(new SlideView.SlideListener() {
            @Override
            public void onDrag(float minParentY, float nowParentY, float maxParentY) {
                if (mLayoutParams == null)
                    mLayoutParams = (RelativeLayout.LayoutParams) mScrollView.getLayoutParams();
                mLayoutParams.height = (int) nowParentY;
                mScrollView.setLayoutParams(mLayoutParams);
            }
        });
    }
}
