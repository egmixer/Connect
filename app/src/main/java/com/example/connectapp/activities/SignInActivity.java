package com.example.connectapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.connectapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

public class SignInActivity extends BaseActivity {
    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    @BindView(R.id.phone_input_et)
    EditText phoneInput;
    @BindView(R.id.submit_number_btn)
    Button submitBtn;
    @BindView(R.id.code_input)
    EditText verificationCodeInput;
    @BindView(R.id.timer_tv)
    TextView timer;
    @BindView(R.id.resend_btn)
    Button resendBtn;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private String phone = "";
    private String mVerificationId;
    private DatabaseReference userDatabase;
    private FirebaseUser mCurrentUser;
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        resendBtn.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                showLoadingDialog(getString(R.string.activating_account), getString(R.string.wait));
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_number),
                            Snackbar.LENGTH_SHORT).show();
                else if (e instanceof FirebaseTooManyRequestsException)
                    Snackbar.make(findViewById(android.R.id.content), R.string.quota_exceeded,
                            Snackbar.LENGTH_SHORT).show();
                else
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.verification_failed),
                            Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                setTwoMinutes();
                Log.d(TAG, "onCodeSent:" + verificationId);
                resendToken = token;
                mVerificationId = verificationId;
            }
        };

        verificationCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 6)
                    verifyPhoneNumberWithCode(mVerificationId, editable.toString().trim());
            }
        });
    }

    @OnClick(R.id.submit_number_btn)
    void setSubmitBtn() {
        phone = phoneInput.getEditableText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError(getString(R.string.phone_input_error));
            return;
        }
        if (phone.length() < 12) {
            phoneInput.setError(getString(R.string.country_code_input_error));
            return;
        }
        startPhoneNumberVerification(phone);
    }

    @OnClick(R.id.resend_btn)
    void setResendBtn() {
        phone = phoneInput.getEditableText().toString().trim();
        resendVerificationCode(phone, resendToken);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mCurrentUser = mAuth.getCurrentUser();
                            deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String userId = mCurrentUser.getUid();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Map userMap = new HashMap();
                            userMap.put("phone", phone);
                            userMap.put("device_token", deviceToken);
                            showLoadingDialog(getString(R.string.new_user), getString(R.string.wait));
                            userDatabase
                                    .child("Users")
                                    .child(userId).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                        startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
                                    else
                                        Snackbar.make(findViewById(android.R.id.content), task.getException().toString(),
                                                Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                Snackbar.make(findViewById(android.R.id.content), R.string.invalid_number,
                                        Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void setTwoMinutes() {
        CountDownTimer counter = new CountDownTimer(TWO_MINUTES, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int hours = seconds / (60 * 60);
                int tempMint = (seconds - (hours * 60 * 60));
                int minutes = tempMint / 60;
                seconds = tempMint - (minutes * 60);
                timer.setTextColor(getResources().getColor(R.color.colorAccent));
                timer.setText(String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
                resendBtn.setEnabled(false);
            }

            public void onFinish() {
                timer.setText(R.string.time_out);
                timer.setTextColor(Color.RED);
                resendBtn.setEnabled(true);
            }
        };
        counter.start();
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                3,                 // Timeout duration
                TimeUnit.MINUTES,    // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
}
