package com.example.quiz_app_java;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivityAdmin extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private Dialog loadingadminDialog,categoryDialog;
    private CircleImageView addImage;
    private EditText categoryname;
    private Button addBtn;

    private RecyclerView recyclerView;
    public static List<CatagoryModelAdmin> list;
    private CatagoryAdapterAdmin adapter;
    private Uri image;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_admin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        loadingadminDialog = new Dialog(this);
        loadingadminDialog.setContentView(R.layout.loadingadmin);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingadminDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        }
        loadingadminDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingadminDialog.setCancelable(false);

        setCategoryDialog();


        recyclerView = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        adapter = new CatagoryAdapterAdmin(list, new CatagoryAdapterAdmin.DeleteListner() {
            @Override
            public void onDelete(final String key, final int position) {

                new AlertDialog.Builder(CategoryActivityAdmin.this)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure, you want to delete this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingadminDialog.show();
                                myRef.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            for (String setIds : list.get(position).getSets()){
                                                myRef.child("SETS").child(setIds).removeValue();
                                            }
                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                            loadingadminDialog.dismiss();
                                        }
                                        else {
                                            Toast.makeText(CategoryActivityAdmin.this, "Failed to delete",Toast.LENGTH_LONG).show();
                                            loadingadminDialog.dismiss();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton("cencel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        loadingadminDialog.show();
        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    List<String> sets = new ArrayList<>();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("sets").getChildren()){
                        sets.add(dataSnapshot2.getKey());
                    }

                    list.add(new CatagoryModelAdmin(dataSnapshot1.child("name").getValue().toString(),
                            sets,
                            dataSnapshot1.child("url").getValue().toString(),
                            dataSnapshot1.getKey()
                    ));
                }
                adapter.notifyDataSetChanged();
                loadingadminDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CategoryActivityAdmin.this, databaseError.getMessage(),Toast.LENGTH_LONG).show();
                loadingadminDialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() == R.id.add){
            categoryDialog.show();
        }
        if (item.getItemId() ==  R.id.logout){

            new AlertDialog.Builder(CategoryActivityAdmin.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure, you want to Logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingadminDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(CategoryActivityAdmin.this,MainActivityAdmin.class);
                            startActivity(intent);
                            finish();

                        }
                    })
                    .setNegativeButton("cencel", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void setCategoryDialog(){
        categoryDialog = new Dialog(this);
        categoryDialog.setContentView(R.layout.add_category_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            categoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));
        }
        categoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryDialog.setCancelable(true);

        addImage = categoryDialog.findViewById(R.id.image);
        categoryname = categoryDialog.findViewById(R.id.categoryname);
        addBtn = categoryDialog.findViewById(R.id.add);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 101);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryname.getText() == null || categoryname.getText().toString().isEmpty()){
                    categoryname.setError("Required");
                    return;
                }
                for (CatagoryModelAdmin model : list){
                    if(categoryname.getText().toString().equals(model.getName())){
                        categoryname.setError("Category name already present!");
                        return;
                    }
                }
                if (image == null){
                    Toast.makeText(CategoryActivityAdmin.this, "Please select your image.",Toast.LENGTH_LONG).show();
                    return;
                }
                categoryDialog.dismiss();
                uploadData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101){
            if(resultCode == RESULT_OK){
                image = data.getData();
                addImage.setImageURI(image);
            }
        }
    }

    private void uploadData(){
        loadingadminDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageReference = storageReference.child("categories").child(image.getLastPathSegment());

        UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadUrl = task.getResult().toString();
                            uploadCategoryName();
                        }
                        else {
                            loadingadminDialog.dismiss();
                            Toast.makeText(CategoryActivityAdmin.this, "Something went wrong!",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    Toast.makeText(CategoryActivityAdmin.this, "Something went wrong!",Toast.LENGTH_LONG).show();
                    loadingadminDialog.dismiss();
                }
            }
        });
    }

    private void uploadCategoryName(){
        Map<String,Object> map = new HashMap<>();
        map.put("name", categoryname.getText().toString());
        map.put("sets",0);
        map.put("url",downloadUrl);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final String id  = UUID.randomUUID().toString();

        database.getReference().child("Categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    list.add(new CatagoryModelAdmin(categoryname.getText().toString(), new ArrayList<String>(),downloadUrl, id));
                    adapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(CategoryActivityAdmin.this, "Something went wrong!",Toast.LENGTH_LONG).show();
                }
                loadingadminDialog.dismiss();
            }
        });
    }
}
