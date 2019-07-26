package com.ssnwa.cargoin.fieldtracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SigninActivity extends AppCompatActivity {

    EditText phoneNumber,OTP;
    public static String phone,otp;
    Button otpbutton,sendbutton;
    FirebaseAuth auth;
    public static String verificationCode;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);


        auth = FirebaseAuth.getInstance();
        phoneNumber=findViewById(R.id.phoneNumber);
        sendbutton=findViewById(R.id.sendotpbutton);
        otpbutton=findViewById(R.id.verifyotpbutton);
        OTP=findViewById(R.id.otp);


        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone=phoneNumber.getText().toString();
                if(phone.equals("+91")) {
                    Toast.makeText(SigninActivity.this, "Enter The Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phone,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        SigninActivity.this,               // Activity (for callback binding)
                        mCallback);        // OnVerificationStateChangedCallbacks
            }
        });
        StartFirebaseLogin();
        otpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp= OTP.getText().toString();
                if(otp.equals("")) {
                    Toast.makeText(SigninActivity.this, "Enter The OTP", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(phone))
                {
                    Toast.makeText(SigninActivity.this, "Enter Your Credentials", Toast.LENGTH_SHORT).show();
                    return;
                }
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode,otp);
                SigninWithPhone(credential);
            }
        });

    }
    private void StartFirebaseLogin() {


        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(SigninActivity.this,"verification completed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(SigninActivity.this,"verification failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                Toast.makeText(SigninActivity.this,"Otp sent",Toast.LENGTH_SHORT).show();
            }
        };
    }
    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SigninActivity.this,ChooseActivity.class));
                            finish();

                        } else {
                            Toast.makeText(SigninActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent i=new Intent(SigninActivity.this,ChooseActivity.class);
            startActivity(i);
            finish();
        }

    }
}
