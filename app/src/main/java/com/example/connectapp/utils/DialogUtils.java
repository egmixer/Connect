package com.example.connectapp.utils;


import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.connectapp.R;

public class DialogUtils {
    private final Context mContext;
    private MaterialDialog dialog;

    public DialogUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showMessageDialog(String title, String content) {
        dialog = new MaterialDialog.Builder(mContext)
                .title(title)
                .content(content)
                .positiveText(R.string.dialog_ok)
                .show();
    }

    public void showLoadingDialog(String title, String content) {
        dialog = new MaterialDialog.Builder(mContext)
                .title(title)
                .content(content)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    public void showErrorDialog(String content) {
        showMessageDialog(mContext.getString(R.string.dialog_error), content);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

}
