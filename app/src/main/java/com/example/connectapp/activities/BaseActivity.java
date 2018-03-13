package com.example.connectapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.connectapp.utils.DialogUtils;

import butterknife.ButterKnife;


public class BaseActivity extends AppCompatActivity {
    DialogUtils dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new DialogUtils(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    protected void showLoadingDialog(String title, String content) {
        dialog.showLoadingDialog(title, content);
    }

    protected void showLoadingDialog(int titleId, int contentId) {
        showLoadingDialog(getString(titleId), getString(contentId));
    }

    protected void dismissDialog() {
        dialog.dismissDialog();
    }
}
