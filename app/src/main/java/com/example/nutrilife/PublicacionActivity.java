package com.example.nutrilife;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutrilife.ui.LoadingDialog;
import com.example.nutrilife.ui.perfil.PerfilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PublicacionActivity extends AppCompatActivity {
    ImageView img_publi;

    FirebaseFirestore db;
    EditText Desc;
    Button Subir, Act, Reg;
    ImageButton DeletImg;
    private FirebaseAuth mAuth2;
    String Publicacion, filePath, path_publi = "Publicaciones/", photo = "photo", usu_log;
    private Uri image_url, URLNuevaI, URLActual;
    StorageReference storageRef;
    private static final int COD_SEL_IMAGE=300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicacion);

        storageRef = FirebaseStorage.getInstance().getReference();
        db=FirebaseFirestore.getInstance();
        mAuth2=FirebaseAuth.getInstance();

        img_publi=findViewById(R.id.igPubli);
        Subir=findViewById(R.id.btnSubirFP);
        Act=findViewById(R.id.btnPublicar);
        Desc=findViewById(R.id.txtDescripcion);
        Reg=findViewById(R.id.btnReg6);
        DeletImg=findViewById(R.id.btnDeleImg);

        SharedPreferences compartir_usu_log = this.getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        usu_log =compartir_usu_log.getString("Usu_Log", "");

        SharedPreferences datos_pub = getSharedPreferences("Datos_Per", MODE_PRIVATE);
        Desc.setText(datos_pub.getString("Des",""));
        Picasso.get().load(Uri.parse(datos_pub.getString("Fot",""))).into(img_publi);
        URLActual=Uri.parse(datos_pub.getString("Fot",""));
        Publicacion=datos_pub.getString("Pub","");
        if(!URLActual.toString().equals("")){
            filePath = extractFilePath(URLActual.toString());
        }
        URLNuevaI=URLActual;

        Subir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upPhoto();
            }
        });

        Act.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Desc.getText().toString().equals("") && URLNuevaI.toString().equals("")){
                    MensajeE("No puedes hacer una publicación vacía");
                }else{
                    UpdatePub(URLNuevaI);
                }

            }
        });

        Reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DeletImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_publi.setImageDrawable(getResources().getDrawable(R.drawable.ig_addimage, null));
                URLNuevaI=Uri.parse("");
            }
        });
    }

    public void upPhoto(){
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");

        startActivityForResult(i,COD_SEL_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == COD_SEL_IMAGE){
                image_url= data.getData();
                Picasso.get().load(image_url).into(img_publi);
                URLNuevaI=image_url;
            }
        }else{
            Toast.makeText(PublicacionActivity.this,"Subir imagen cancelado",Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdatePub(Uri imageUrl){
        LoadingDialog loadingDialog = new LoadingDialog(PublicacionActivity.this);
        loadingDialog.startAlertDialog();

        String nombreArchivo = null;
        if (imageUrl != null) {
            nombreArchivo = UUID.randomUUID().toString();
        }

        if(URLNuevaI.toString().equals("")){
            if(!URLActual.toString().equals("")){
                DeleteStorage(filePath);
            }

            HashMap<String, Object> pub = new HashMap<>();
            pub.put("Descripcion", Desc.getText().toString());
            pub.put("Foto","");

            db.collection("Publicaciones").document(Publicacion).update(pub).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    loadingDialog.dismissDialog();
                    MensajeOkInicio("Se ha actualizado la publicacion correctamente");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingDialog.dismissDialog();
                    MensajeE("No se ha podido actualizar la publicacion");
                }
            });
        }else{
            if(!URLActual.toString().equals("")){
                DeleteStorage(filePath);
            }

            String rute_storage_photo = path_publi + "" + photo + "" + mAuth2.getUid();
            StorageReference reference = storageRef.child(rute_storage_photo);
            reference.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String download_uri = task.getResult().toString();
                                HashMap<String, Object> pub = new HashMap<>();
                                pub.put("Descripcion", Desc.getText().toString());
                                pub.put("Foto", download_uri);

                                db.collection("Publicaciones").document(Publicacion).update(pub).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        loadingDialog.dismissDialog();
                                        MensajeOkInicio("Se ha actualizado la publicacion correctamente");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialog.dismissDialog();
                                        MensajeE("No se ha podido actualizar la publicacion");
                                    }
                                });
                            } else {
                                loadingDialog.dismissDialog();
                                MensajeE("Error al obtener la URL de descarga");
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingDialog.dismissDialog();
                    MensajeE("Error al subir la foto al Storage");
                }
            });
        }
    }

    private void DeleteStorage(String filePath){
        StorageReference desertRef = storageRef.child(filePath);
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }

    private String extractFilePath(String fileUrl) {
        int startIndex = fileUrl.indexOf("/o/") + 3;
        int endIndex = fileUrl.indexOf("?");
        return fileUrl.substring(startIndex, endIndex).replace("%2F", "/");
    }

    private void MensajeOkInicio(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PublicacionActivity.this,R.style.AlertDialogTheme);
        View view = LayoutInflater.from(PublicacionActivity.this).inflate(R.layout.layout_okey_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(PublicacionActivity.this, nav_draw.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void MensajeE(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PublicacionActivity.this,R.style.AlertDialogTheme);
        View view = LayoutInflater.from(PublicacionActivity.this).inflate(R.layout.layout_error_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Error");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}