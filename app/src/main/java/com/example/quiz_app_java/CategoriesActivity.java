//package com.example.quiz_app_java;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//public class CategoriesActivity extends AppCompatActivity {
//
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference();
//
//    private Dialog loadingDialog;
//
//    private List<CategoryModel> list;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_categories);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle("categories");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        loadingDialog = new Dialog(this);
//        loadingDialog.setContentView(R.layout.loading);
//        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
//        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//         loadingDialog.setCancelable(false);
//
//        RecyclerView recyclerView = findViewById(R.id.recycleView);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
//        recyclerView.setLayoutManager(layoutManager);
//
//       list = new ArrayList<>();
//
//        CategoryAdapter adapter = new CategoryAdapter(list);
//        recyclerView.setAdapter(adapter);
//
//        loadingDialog.show();
//        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot  snapshot1: snapshot.getChildren()){
//                    list.add(snapshot1.getValue(CategoryModel.class));
//                }
//                adapter.notifyDataSetChanged();
//                loadingDialog.dismiss();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(CategoriesActivity.this,  error.getMessage(), Toast.LENGTH_SHORT).show();
//                loadingDialog.dismiss();
//                finish();
//            }
//        });
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//        if(item.getItemId() == android.R.id.home){
//            finish();
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}



package com.example.quiz_app_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoriesActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private Dialog loadingDialog;

    private List<CategoryModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        RecyclerView recyclerView = findViewById(R.id.recycleView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();

        CategoryAdapter adapter = new CategoryAdapter(list);
        recyclerView.setAdapter(adapter);

        loadingDialog.show();
        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        list.add(snapshot1.getValue(CategoryModel.class));
                    }
                    adapter.notifyDataSetChanged();
                    loadingDialog.dismiss();
                } else {
                    // Handle the case when there are no categories
                    loadingDialog.dismiss();
                    // Add appropriate handling (e.g., show a message)
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoriesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
