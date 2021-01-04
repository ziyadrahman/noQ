package com.example.e_rationqueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class authentication extends AppCompatActivity {
    TextInputEditText rationCardNo;
    TextInputEditText customerName;
    TextInputEditText mobileNumber;
    TextInputEditText otp;

    RadioGroup radioGroup;
    RadioButton apl,bpl,aayOn,nonPriorityOn;

    MaterialButton sendOtp,sumbit;

    String mobile;
    String mVerificationId;
    private FirebaseAuth mAuth;

    String cardType;
    String getCustomer;
    String getRationNo;
    String getMobile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        otp=findViewById(R.id.OTPInputEditText);
        sendOtp=findViewById(R.id.send);
        sumbit=findViewById(R.id.otpBtn);
        radioGroup=findViewById(R.id.radioGroup);
        apl=findViewById(R.id.aplOn);
        bpl=findViewById(R.id.bplOn);
        aayOn=findViewById(R.id.aayOn);
        nonPriorityOn=findViewById(R.id.nonpriorityOn);
        mAuth=FirebaseAuth.getInstance();
        rationCardNo=findViewById(R.id.cardNumberTextInputEditText);
        customerName=findViewById(R.id.customerNameTextInputEditText);
        mobileNumber=findViewById(R.id.mobileNumberInputEditText);
        getCardType();

        sendOtp.setOnClickListener(v -> {
            getAllText();
            mobile=mobileNumber.getText().toString().trim();
            if (validation()){
            sendVerificationCode(mobile);}
            else
            {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    mVerificationId = s;

                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        otp.setText(code);
                        verifyVerificationCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(authentication.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            };


    private void verifyVerificationCode(String otp) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(authentication.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            addUser();
                            Intent intent = new Intent(authentication.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(authentication.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    private void addUser(){
        user user=new user(getRationNo,cardType,getCustomer);
        DatabaseReference users= FirebaseDatabase.getInstance().getReference("users");
        users.child(getCurrentUser()).setValue(user);

    }
    private String getCurrentUser()
    {
        FirebaseAuth mAuth;
        mAuth=FirebaseAuth.getInstance();

        return   Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }
    private Boolean validation()
    {
       return  !getCustomer.isEmpty() && !getRationNo.isEmpty() && !getMobile.isEmpty() && !cardType.isEmpty();
    }
    private void getAllText()
    {
        getCustomer= Objects.requireNonNull(customerName.getText()).toString().toLowerCase().trim();
        getRationNo= Objects.requireNonNull(rationCardNo.getText()).toString().trim();
        getMobile= Objects.requireNonNull(mobileNumber.getText()).toString().trim();

    }
    private void getCardType(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.aplOn:
                        cardType="apl";
                        break;
                    case  R.id.bplOn:
                        cardType="bpl";
                        break;
                    case  R.id.aayOn:
                        cardType="ayy";
                        break;
                    case  R.id.nonpriorityOn:
                        cardType="non-priority";
                        break;
                }
            }
        });
    }

}

