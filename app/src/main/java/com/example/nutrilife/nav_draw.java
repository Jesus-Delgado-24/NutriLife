package com.example.nutrilife;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListPubliPerfil;
import com.example.nutrilife.ui.perfil.PerfilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nutrilife.databinding.ActivityNavDrawBinding;
import com.google.firebase.StartupTime;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class nav_draw extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityNavDrawBinding binding;
    private FirebaseFirestore db;
    String Correo;
    ImageView Foto;
    TextView Nombre, lblCorreo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavDrawBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        NavigationView naveView = binding.navView;
        View headerView = naveView.getHeaderView(0);
        db = FirebaseFirestore.getInstance();

        Nombre = headerView.findViewById(R.id.lblNombre);
        lblCorreo = headerView.findViewById(R.id.lblCorreo);
        Foto = headerView.findViewById(R.id.iVFotoPer);

        SharedPreferences compartido = getSharedPreferences("Datos_Per",MODE_PRIVATE);
        Correo=compartido.getString("Id_Cor","");
        lblCorreo.setText(Correo);
        MNombre(Correo, new FirestoreCallback() {
            @Override
            public void onCallbackN(String nombreCompleto) {
                if (nombreCompleto != null) {
                    Nombre.setText(nombreCompleto);
                    SharedPreferences compartir = getSharedPreferences("Datos_Per_2",MODE_PRIVATE);
                    SharedPreferences.Editor editor = compartir.edit();
                    editor.putString("Id_Cor", lblCorreo.getText().toString());
                    editor.putString("Id_Nom", nombreCompleto);
                    editor.apply();
                }
            }
            @Override
            public void onCallbackN2(String foto) {
                Uri fotoUri = Uri.parse(foto);
                Picasso.get().load(fotoUri).into(Foto);
                SharedPreferences compartir = getSharedPreferences("Foto_Uri",MODE_PRIVATE);
                SharedPreferences.Editor editor = compartir.edit();
                editor.putString("Id_Foto", foto);
                editor.apply();
            }

        });

        SacarDocu(new FirestoreCallback1() {
            @Override
            public void onCallbackD(String Documento) {
                SharedPreferences compartir_usu_log = getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
                SharedPreferences.Editor editor = compartir_usu_log.edit();
                editor.putString("Usu_Log", Documento);
                editor.apply();
            }
        });
        SharedPreferences compartir_usu_log = getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        Correo=compartir_usu_log.getString("Usu_log","");

        setSupportActionBar(binding.appBarNavDraw.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_seguidos, R.id.nav_likes,R.id.nav_perfil,R.id.nav_cerrar_sesion)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_nav_draw);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_draw, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_nav_draw);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void MNombre(String cor, final FirestoreCallback callback) {
        Log.i("Entrando al metodo", cor);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Personas").whereEqualTo("Correo", cor).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (result != null && !result.isEmpty()) {
                                for (QueryDocumentSnapshot document : result) {
                                    String nombre = document.getString("Nombre");
                                    String paterno = document.getString("Paterno");
                                    String materno = document.getString("Materno");
                                    String foto = document.getString("Foto");
                                    String nombreCompleto = nombre + " " + paterno + " " + materno;
                                    callback.onCallbackN(nombreCompleto);
                                    callback.onCallbackN2(foto);
                                }
                            } else {
                                Log.i("Base de datos", "No se encontraron documentos");
                                callback.onCallbackN(null);
                            }
                        } else {
                            Log.e("Base de datos", "Error en la consulta: ", task.getException());
                            callback.onCallbackN(null);
                        }
                    }
                });
    }

    public interface FirestoreCallback {
        void onCallbackN(String nombreCompleto);
        void onCallbackN2(String Foto);
    }

    private void SacarDocu(final FirestoreCallback1 callback1){
        db.collection("Personas").whereEqualTo("Correo",Correo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        callback1.onCallbackD(documentId);
                    }
                }
            }
        });
    }

    public interface FirestoreCallback1 {
        void onCallbackD(String Documento);
    }
}