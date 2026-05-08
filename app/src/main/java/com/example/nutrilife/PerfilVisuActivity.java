package com.example.nutrilife;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nutrilife.Colecciones.Comentarios.AdapterComentarios;
import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListAdapterPubliPer;
import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListPubliPerfil;
import com.example.nutrilife.ui.perfil.PerfilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PerfilVisuActivity extends AppCompatActivity {
    List<ListPubliPerfil> myPubli;
    ImageView Fotoo;
    ImageButton Seguir;
    TextView Nombree, Correoo;
    RecyclerView recyclerVisu;
    String Usu_Log, Usu_Visu, usu_log;
    FirebaseFirestore db;
    boolean bool;

    boolean isSeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_visu);

        Fotoo=findViewById(R.id.iVFotoPer_Visu);
        Seguir=findViewById(R.id.btnSeguirVisu);
        Nombree=findViewById(R.id.lblNom_Visu);
        Correoo=findViewById(R.id.lblCor_Visu);
        recyclerVisu=findViewById(R.id.RecyclePubliPerfilVisu);

        SharedPreferences Usuarios = this.getSharedPreferences("Usuarios",MODE_PRIVATE);
        Usu_Log =Usuarios.getString("Usu_Log","");
        Usu_Visu = Usuarios.getString("Usu_Visu","");

        SharedPreferences compartir_usu_log = this.getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        usu_log =compartir_usu_log.getString("Usu_Log", "");

        SaberSeg(new SaCallback() {
            @Override
            public void onCallbackCS2L(boolean Seguido) {
                if(Seguido){
                    Seguir.setImageResource(R.drawable.ig_removeperson);
                    ((TextView)findViewById(R.id.lblSeguir)).setText("Dejar de seguir");
                }
            }
        });

        SacarFNC(Usu_Visu, new FNCallback() {
            @Override
            public void onCallbackFN(String Foto, String Nombre, String Correo) {
                Picasso.get().load(Uri.parse(Foto)).into(Fotoo);
                Nombree.setText(Nombre);
                Correoo.setText(Correo);
            }
        });

        Seguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db=FirebaseFirestore.getInstance();
                SaberSeg(new SaCallback() {
                    @Override
                    public void onCallbackCS2L(boolean Seguido) {
                        if(Seguido){
                            SaberDocSeg(new SDCallback() {
                                @Override
                                public void onCallbackSD(String DocSeg) {
                                    db.collection("Seguidores").document(DocSeg).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Seguir.setImageResource(R.drawable.ig_addperson);
                                            ((TextView)findViewById(R.id.lblSeguir)).setText("Seguir");
                                            ContadorSeguidores(new CS1Callback() {
                                                @Override
                                                public void onCallbackCS1L(int Seguidores) {
                                                    ((TextView)findViewById(R.id.lblSeguidoresVisu)).setText(""+Seguidores);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }else{
                            HashMap<String, Object> segui = new HashMap<>();
                            segui.put("Seguidor", usu_log);
                            segui.put("Seguido", Usu_Visu);

                            db.collection("Seguidores").add(segui).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Seguir.setImageResource(R.drawable.ig_removeperson);
                                    ((TextView)findViewById(R.id.lblSeguir)).setText("Dejar de seguir");
                                    ContadorSeguidores(new CS1Callback() {
                                        @Override
                                        public void onCallbackCS1L(int Seguidores) {
                                            ((TextView)findViewById(R.id.lblSeguidoresVisu)).setText(""+Seguidores);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });

        MostrarPublica();
        ContadorSeguidores(new CS1Callback() {
            @Override
            public void onCallbackCS1L(int Seguidores) {
                ((TextView)findViewById(R.id.lblSeguidoresVisu)).setText(""+Seguidores);
            }
        });
        ContadorSeguidor(new CS2Callback() {
            @Override
            public void onCallbackCS2L(int Seguidores) {
                ((TextView)findViewById(R.id.lblSeguidosVisu)).setText(""+Seguidores);
            }
        });
    }

    private void SacarFNC(String Document, FNCallback callback){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Personas").document(Document);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String nom = document.getString("Nombre");
                        String pat = document.getString("Paterno");
                        String mat = document.getString("Materno");
                        String fot = document.getString("Foto");
                        String cor = document.getString("Correo");

                        String nombre = nom + " " + pat + " " + mat;

                        callback.onCallbackFN(fot,nombre,cor);
                    }
                }
            }
        });
    }

    public interface FNCallback {
        void onCallbackFN(String Foto, String Nombre, String Correo);
    }

    public void MostrarPublica(){
        myPubli=new ArrayList<>();
        SacarPublicaciones(new PublicacionesCallback() {
            @Override
            public void onCallbackD(String Descripcion, Date Fecha, String Image_Uri, String Usuario) {
                myPubli.add(new ListPubliPerfil(Descripcion,Fecha,Uri.parse(Image_Uri),Usuario));
                ListAdapterPubliPer adapter=new ListAdapterPubliPer(myPubli);
                recyclerVisu.setHasFixedSize(true);
                recyclerVisu.setLayoutManager(new LinearLayoutManager(PerfilVisuActivity.this));
                recyclerVisu.setAdapter(adapter);
            }
        });
    }

    private void SacarPublicaciones(PublicacionesCallback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Personas").document(Usu_Visu).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String fotoPer = documentSnapshot.getString("Foto");
                db.collection("Publicaciones").whereEqualTo("Usuario",Usu_Visu).orderBy("Fecha", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String des = document.getString("Descripcion");
                                String image = document.getString("Foto");
                                Date fecha = document.getDate("Fecha");
                                callback.onCallbackD(des,fecha,image,Usu_Visu);
                            }
                        }
                    }
                });
            }
        });
    }

    public interface PublicacionesCallback {
        void onCallbackD(String Descripcion,Date Fecha, String Image_Uri, String Usuario);
    }

    public void ContadorSeguidores(CS1Callback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Seguidores").whereEqualTo("Seguido", Usu_Visu).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    int count = task.getResult().size();
                    callback.onCallbackCS1L(count);
                }
            }
        });
    }

    public interface CS1Callback {
        void onCallbackCS1L(int Seguidores);
    }

    public void ContadorSeguidor(CS2Callback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Seguidores").whereEqualTo("Seguidor", Usu_Visu).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    int count = task.getResult().size();
                    callback.onCallbackCS2L(count);
                }
            }
        });
    }

    public interface CS2Callback {
        void onCallbackCS2L(int Seguidores);
    }

    private void SaberSeg(SaCallback callback){
        db=FirebaseFirestore.getInstance();
        db.collection("Seguidores").whereEqualTo("Seguidor",usu_log).whereEqualTo("Seguido",Usu_Visu).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        callback.onCallbackCS2L(true);
                    } else {
                        callback.onCallbackCS2L(false);
                    }
                }
            }
        });
    }
    public interface SaCallback {
        void onCallbackCS2L(boolean Seguido);
    }

    private void SaberDocSeg(SDCallback callback){
        db=FirebaseFirestore.getInstance();
        db.collection("Seguidores").whereEqualTo("Seguidor",usu_log).whereEqualTo("Seguido",Usu_Visu).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String doc = document.getId();
                        callback.onCallbackSD(doc);
                    }
                }
            }
        });
    }
    public interface SDCallback {
        void onCallbackSD(String DocSeg);
    }
}