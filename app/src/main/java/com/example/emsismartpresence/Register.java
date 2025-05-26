package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.activity.EdgeToEdge;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText RegFirstName, RegLastName, RegEmail, RegPassword, RegConPassword;
    private Button btnRegister;
    private TextView linkToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        RegEmail = findViewById(R.id.RegisterEmail);
        RegFirstName = findViewById(R.id.FirstName);
        RegLastName = findViewById(R.id.LastName);
        RegPassword = findViewById(R.id.RegisterPassword);
        RegConPassword = findViewById(R.id.ConfirmPassword);
        linkToLogin = findViewById(R.id.LoginPage);
        btnRegister =findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                registerUser();
            }
        });
        linkToLogin.setOnClickListener(v->{
            Intent intent = new Intent(Register.this, Signin.class);
            startActivity(intent);
        });
    }
    private void registerUser(){
        String nom = RegFirstName.getText().toString().trim();
        String prenom = RegLastName.getText().toString().trim();
        String email = RegEmail.getText().toString().trim();
        String passwordone = RegPassword.getText().toString().trim();
        String passwordtwo = RegConPassword.getText().toString().trim();
        String fullname = nom + " " + prenom;


        if(nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || passwordone.isEmpty() || passwordtwo.isEmpty()){
            Toast.makeText(this,"Veuillez remplir tout les champs",Toast.LENGTH_SHORT).show();
            return;
        }else{
            if(!passwordone.equals(passwordtwo)){
                Toast.makeText(this,"Mot de passes ne sont pas identiques!",Toast.LENGTH_SHORT).show();
                return;
            }else{
                /*Ajouter vérification d email pro*/
                mAuth.createUserWithEmailAndPassword(email,passwordone).addOnCompleteListener(this, task ->{
                    if(task.isSuccessful()) {
                        Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                        userId = mAuth.getCurrentUser().getUid();
                        store_user_firestore(userId,email,fullname);
                        Intent i=new Intent(Register.this, Signin.class);
                        i.putExtra("name",fullname);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void store_user_firestore(String uid, String email, String name){
        Map<String, Object> user = new HashMap<>();
        user.put("user_email", email);
        user.put("date_inscription", new Timestamp(new Date()));
        user.put("name", name);

        db.collection("users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
               Toast.makeText(Register.this,"Bienvenue" + name, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register.this,"Failed to create user",Toast.LENGTH_SHORT).show();
            }
        });
    }
}