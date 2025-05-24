package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_documents);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


            private ListView documentsListView;
            private FirebaseFirestore db;
            private FirebaseAuth mAuth;




            private void loadDocuments() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    db.collection("professors")
                            .document(user.getUid())
                            .collection("documents")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<String> documentTitles = new ArrayList<>();
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String title = document.getString("title");
                                    if (title != null) {
                                        documentTitles.add(title);
                                    }
                                }

                                // Utilisation d'un ArrayAdapter simple au lieu de DocumentAdapter
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        this,
                                        android.R.layout.simple_list_item_1,
                                        documentTitles
                                );
                                documentsListView.setAdapter(adapter);

                                documentsListView.setOnItemClickListener((parent, view, position, id) -> {
                                    String selectedDoc = documentTitles.get(position);
                                    Toast.makeText(this, "Document sélectionné: " + selectedDoc,
                                            Toast.LENGTH_SHORT).show();
                                    // Ajouter ici la logique pour ouvrir le document si nécessaire
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur de chargement des documents",
                                        Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }