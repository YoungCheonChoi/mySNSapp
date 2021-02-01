package com.example.basicsns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, fullname, email, password;
    Button register;
    TextView txt_login;

    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //회원가입 정보 기입란 연결
        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        //회원가입버튼 연결
        register = findViewById(R.id.register);
        //로그인하러가기 연결
        txt_login = findViewById(R.id.txt_login);

        auth = FirebaseAuth.getInstance();

        //이미 회원인 경우 로그인하러 가기
        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        //회원가입 버튼 클릭
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //프로그레스바 생성
                pd=new ProgressDialog(RegisterActivity.this);
                pd.setMessage("회원을 등록 중입니다...");
                pd.show();

                //입력받은 정보를 String으로 변환
                String str_username  =username.getText().toString();
                String str_fullname  =fullname.getText().toString();
                String str_email  =email.getText().toString();
                String str_password  =password.getText().toString();

                //모든 칸을 기입하지 않았다면
                //비밀번호 생성 조건: 6자리 이상
                if(TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_fullname)
                        || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
                    Toast.makeText(RegisterActivity.this, "모든 칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else if(str_password.length()<6){
                    Toast.makeText(RegisterActivity.this, "비밀번호는 6자 이상이여야 합니다.", Toast.LENGTH_SHORT).show();
                }else{
                    //회원가입 성공
                    register(str_username, str_fullname, str_email, str_password);
                }
            }
        });


    } //oncreate 끝

    //회원가입하기
    public void register(String username, String fullname, String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            reference= FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                            //회원정보 해쉬맵 객체로 저장
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username.toLowerCase());  //toLowerCase() 뭔지 알아보기
                            hashMap.put("fullname", fullname);   //나중에 닉네임으로 바꿔야하나
                            hashMap.put("bio", "");
                            hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/basicsns-7ed61.appspot.com/o/logo.png?alt=media&token=feb1f47a-0dea-47c0-8929-194872545229");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //회원가입 성공하면 메인액티비티로 이동
                                        pd.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);       //뭔지 알아보기 addFlags
                                        startActivity(intent);
                                    }
                                }
                            });
                        }else{
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "이 이메일 혹은 비밀번호로는 계정을 만드실 수 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}//마지막 괄호