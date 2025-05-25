package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddGroupActivity extends AppCompatActivity {
    private EditText groupNameInput, centerInput;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        db = FirebaseFirestore.getInstance();
        groupNameInput = findViewById(R.id.groupNameInput);
        centerInput = findViewById(R.id.centerInput);

        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> saveGroup());
    }

    private void saveGroup() {
        String groupName = groupNameInput.getText().toString().trim();
        String center = centerInput.getText().toString().trim();

        if (groupName.isEmpty() || center.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> group = new HashMap<>();
        group.put("name", groupName);
        group.put("center", center);

        db.collection("groups")
                .add(group)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Group added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}