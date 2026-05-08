package com.example.nutrilife.Colecciones.Lists.Publicaciones;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrilife.Colecciones.Comentarios.AdapterComentarios;
import com.example.nutrilife.Colecciones.Comentarios.ListComentPubli;
import com.example.nutrilife.PerfilVisuActivity;
import com.example.nutrilife.PublicacionActivity;
import com.example.nutrilife.R;
import com.example.nutrilife.loginActivity;
import com.example.nutrilife.nav_draw;
import com.example.nutrilife.ui.perfil.PerfilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListAdapterPubliPer extends RecyclerView.Adapter<ListAdapterPubliPer.MyPublicacionesHolder>{
    List<ListPubliPerfil> myPublicacions;
    List<ListPubliPerfil> original;
    private static final int COD_SEL_IMAGE=300;
    boolean isLiked = true;
    int like=0;
    String Des, usu_log;
    Date fechaa;
    private FirebaseFirestore db;
    List<ListComentPubli> Coments;
    ViewGroup contextt;

    public ListAdapterPubliPer(List<ListPubliPerfil> myPublicacions) {
        this.myPublicacions = myPublicacions;
        original=new ArrayList<>();
        original.addAll(myPublicacions);
    }

    @NonNull
    @Override
    public MyPublicacionesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_publiperfil,parent,false);
        MyPublicacionesHolder holder = new MyPublicacionesHolder(v);
        contextt=parent;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyPublicacionesHolder holder, int position) {
        db = FirebaseFirestore.getInstance();
        ListPubliPerfil publi = myPublicacions.get(position);

        SharedPreferences compartir_usu_log = contextt.getContext().getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        usu_log =compartir_usu_log.getString("Usu_Log", "");

        if(!usu_log.equals(publi.getUsuario())){
            holder.EditPubli.setVisibility(View.GONE);
        }

        if(publi.getImagen().toString().equals("")){
            holder.Imagen.setVisibility(View.GONE);
        }else{
            Picasso.get().load(publi.getImagen()).into(holder.Imagen);
        }

        if(Objects.equals(publi.getDescripcion(), "")){
            holder.Descripcion.setVisibility(View.GONE);
            holder.Descripcion.setText(publi.getDescripcion());
        }else{
            holder.Descripcion.setText(publi.getDescripcion());
        }
        holder.Usuario.setText(publi.getUsuario());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String formattedDate = publi.getFecha() != null ? sdf.format(publi.getFecha()) : "N/A";

        holder.Fecha.setText(formattedDate);
        fechaa=publi.getFecha();
        Des=holder.Descripcion.getText().toString();

        holder.Nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!usu_log.equals(holder.Usuario.getText().toString())){
                    iniciarPerVisu(contextt.getContext());
                    Context context= contextt.getContext();
                    SharedPreferences Usuarios = context.getSharedPreferences("Usuarios", MODE_PRIVATE);
                    SharedPreferences.Editor editor = Usuarios.edit();
                    editor.putString("Usu_Log", usu_log);
                    editor.putString("Usu_Visu", holder.Usuario.getText().toString());
                    editor.apply();
                }
            }
        });

        SacarFN(publi.getUsuario(), new FNCallback() {
            @Override
            public void onCallbackFN(String Foto, String Nombre) {
                holder.Nombre.setText(Nombre);
                publi.setNombre(Nombre);
                Picasso.get().load(Uri.parse(Foto)).into(holder.FotoPer);
            }
        });

        holder.Imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv;

                AlertDialog.Builder builder = new AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
                View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_image,null);
                builder.setView(view);
                ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                iv=view.findViewById(R.id.Ver_Imagen);
                Picasso.get().load(publi.getImagen()).into(iv);

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        SaberLikes(holder.Descripcion.getText().toString(), publi.getFecha(), usu_log, new SLCallback() {
            @Override
            public void onCallbackSL(boolean islike) {
                publi.setLiked(islike);
                holder.Like.setImageResource(R.drawable.ig_like);
            }
        });

        holder.Like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(publi.getLiked()){
                    db.collection("Publicaciones").whereEqualTo("Descripcion", holder.Descripcion.getText().toString()).whereEqualTo("Fecha",publi.getFecha()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentId = document.getId();

                                    Date fechaHora = new Date();
                                    Timestamp timestamp = new Timestamp(fechaHora);

                                    HashMap<String, Object> like = new HashMap<>();
                                    like.put("Publicacion", documentId);
                                    like.put("Usuario", usu_log);
                                    like.put("Fecha", timestamp);

                                    db.collection("Likes").add(like).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            holder.Like.setImageResource(R.drawable.ig_like);
                                            publi.setLiked(false);
                                            ContadorLikes(holder.Descripcion.getText().toString(), publi.getFecha(), new CLCallback() {
                                                @Override
                                                public void onCallbackCL(int Likes) {
                                                    holder.Likes.setText(String.valueOf(Likes));
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Like","Incorrect");
                                        }
                                    });
                                }
                            }
                        }
                    });
                }else{
                    db.collection("Publicaciones").whereEqualTo("Descripcion", holder.Descripcion.getText().toString()).whereEqualTo("Fecha",publi.getFecha()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentId = document.getId();

                                    db.collection("Likes").whereEqualTo("Publicacion", documentId).whereEqualTo("Usuario", usu_log).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String documentId = document.getId();
                                                    db.collection("Likes").document(documentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            holder.Like.setImageResource(R.drawable.ig_borderlik);
                                                            publi.setLiked(true);
                                                            ContadorLikes(holder.Descripcion.getText().toString(), publi.getFecha(), new CLCallback() {
                                                                @Override
                                                                public void onCallbackCL(int Likes) {
                                                                    holder.Likes.setText(String.valueOf(Likes));
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });

        holder.Comentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button Comentar;
                EditText Des;
                RecyclerView recyclerView;
                TextView t;

                AlertDialog.Builder builder = new AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
                View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_comments,null);
                builder.setView(view);
                ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                Comentar=view.findViewById(R.id.btnComentar);
                Des=view.findViewById(R.id.txtText_Com);
                recyclerView=view.findViewById(R.id.RecycleComentPubli);
                t=view.findViewById(R.id.txtVacio);

                ContadorCom(holder.Descripcion.getText().toString(), publi.getFecha(), new CCCallback() {
                    @Override
                    public void onCallbackCC(int Coment) {
                        if(Coment>=1){
                            t.setVisibility(View.GONE);
                        }
                    }
                });

                SCom(holder.Descripcion.getText().toString(),publi.getFecha(),recyclerView);

                Comentar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String des = "";
                        des=Des.getText().toString();
                        if(!des.isEmpty()){
                            MComentar(holder.Descripcion.getText().toString(),des,publi.getFecha(),usu_log);
                            Des.setText("");
                            SCom(holder.Descripcion.getText().toString(),publi.getFecha(),recyclerView);
                            ContadorCom(holder.Descripcion.getText().toString(), publi.getFecha(), new CCCallback() {
                                @Override
                                public void onCallbackCC(int Coment) {
                                    holder.Comentarios.setText(String.valueOf(Coment));
                                    if(Coment>=1){
                                        t.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }else{
                            MensajeE("No puedes dejar el comentario vacio");
                        }
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        holder.EditPubli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
                View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_menupubli,null);
                builder.setView(view);
                ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                view.findViewById(R.id.btnEditPub).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        iniciarEditPubli(contextt.getContext(),holder.Descripcion.getText().toString(),publi.getImagen(),publi.getFecha());
                    }
                });

                view.findViewById(R.id.btnDeletPub).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(contextt.getContext()).inflate(
                                R.layout.layout_warning_dialog,null);
                        builder.setView(view);
                        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
                        ((TextView) view.findViewById(R.id.textTittle)).setText("Eliminacion publicacion");
                        ((TextView) view.findViewById(R.id.textMessage)).setText("¿Seguro(a) que quieres eliminar la publicacion?");
                        ((Button) view.findViewById(R.id.buttonYes)).setText("Borrar");
                        ((Button) view.findViewById(R.id.buttonNo)).setText("Cancelar");
                        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_warning);

                        final AlertDialog alertDialog = builder.create();

                        view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                String path = extractFilePath(String.valueOf(publi.getImagen()));
                                DeleteStorage(path);

                                SacarDPubli(holder.Descripcion.getText().toString(), publi.getFecha(), new DPCallback() {
                                    @Override
                                    public void onCallbackDC(String Publicacion) {
                                        DeleteLikes(Publicacion);
                                        DeleteComentarios(Publicacion);
                                        DeletePublicaciones(Publicacion);
                                    }
                                });
                            }
                        });

                        view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
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
                });

                view.findViewById(R.id.btnRegPub).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        ContadorLikes(holder.Descripcion.getText().toString(), publi.getFecha(), new CLCallback() {
            @Override
            public void onCallbackCL(int Likes) {
                holder.Likes.setText(String.valueOf(Likes));
            }
        });

        ContadorCom(holder.Descripcion.getText().toString(), publi.getFecha(), new CCCallback() {
            @Override
            public void onCallbackCC(int Coment) {
                holder.Comentarios.setText(String.valueOf(Coment));
            }
        });
    }

    @Override
    public int getItemCount() {
        return myPublicacions.size();
    }

    public void filter(String textSearch) {
        int length = textSearch.length();
        if (length == 0) {
            myPublicacions.clear();
            myPublicacions.addAll(original);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<ListPubliPerfil> collect = myPublicacions.stream()
                        .filter(i -> (i.getDescripcion() != null && i.getDescripcion().toLowerCase().contains(textSearch)) ||
                                (i.getNombre() != null && i.getNombre().toLowerCase().contains(textSearch)))
                        .collect(Collectors.toList());
                myPublicacions.clear();
                myPublicacions.addAll(collect);
            } else {
                myPublicacions.clear();
                for (ListPubliPerfil i : original) {
                    if ((i.getDescripcion() != null && i.getDescripcion().toLowerCase().contains(textSearch)) ||
                            (i.getNombre() != null && i.getNombre().toLowerCase().contains(textSearch))) {
                        myPublicacions.add(i);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class MyPublicacionesHolder extends RecyclerView.ViewHolder{
        ImageView FotoPer, Imagen;
        TextView Nombre, Likes, Fecha, Descripcion, Usuario, Comentarios;
        ImageButton Like, EditPubli;
        Button Comentar;

        public MyPublicacionesHolder(@NonNull View itemView) {
            super(itemView);

            FotoPer=itemView.findViewById(R.id.iVFotoPer_Publi);
            Imagen=itemView.findViewById(R.id.img_publi);
            Nombre=itemView.findViewById(R.id.lblNom_Publi);
            Likes=itemView.findViewById(R.id.lblCountLike_Publi);
            Fecha=itemView.findViewById(R.id.lblFecha_Publi);
            Descripcion=itemView.findViewById(R.id.lblDes_Publi);
            Like=itemView.findViewById(R.id.btnLike_Publi);
            Comentar=itemView.findViewById(R.id.btnComentar_Publi);
            Usuario=itemView.findViewById(R.id.lblUsu_Publi);
            Comentarios=itemView.findViewById(R.id.lblCountCome_Publi);
            EditPubli=itemView.findViewById(R.id.btnMenuPubli);
        }
    }

    private void SacarFN(String Document, FNCallback callback){
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

                        String nombre = nom + " " + pat + " " + mat;

                        callback.onCallbackFN(fot,nombre);
                    }
                }
            }
        });
    }

    public void ContadorLikes(String Desc, Date Fechaa, CLCallback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Publicaciones").whereEqualTo("Descripcion", Desc).whereEqualTo("Fecha",Fechaa).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();

                        db.collection("Likes").whereEqualTo("Publicacion", documentId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int count = task.getResult().size();
                                    callback.onCallbackCL(count);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void ContadorCom(String Desc, Date Fechaa, CCCallback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Publicaciones").whereEqualTo("Descripcion", Desc).whereEqualTo("Fecha",Fechaa).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();

                        db.collection("Comentarios").whereEqualTo("Publicacion", documentId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int count = task.getResult().size();
                                    callback.onCallbackCC(count);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void SaberLikes(String Desc, Date Fecha, String usu, SLCallback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Publicaciones").whereEqualTo("Descripcion", Desc).whereEqualTo("Fecha",Fecha).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();

                        db.collection("Likes").whereEqualTo("Publicacion", documentId).whereEqualTo("Usuario", usu).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String documentId = document.getId();
                                        callback.onCallbackSL(false);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }



    public interface FNCallback {
        void onCallbackFN(String Foto, String Nombre);
    }
    public interface CLCallback {
        void onCallbackCL(int Likes);
    }
    public interface CCCallback {
        void onCallbackCC(int Coment);
    }
    public interface SLCallback {
        void onCallbackSL(boolean islike);
    }

    private void MensajeE(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_error_dialog,null);
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

    private void MensajeOk(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_okey_dialog,null);
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
                iniciarInicio(contextt.getContext());
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void MComentar(String Des1, String Des2, Date Fecha, String Usuario){
        db=FirebaseFirestore.getInstance();
        db.collection("Publicaciones").whereEqualTo("Descripcion", Des1).whereEqualTo("Fecha", Fecha).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();

                        Date fechaHora = new Date();
                        Timestamp timestamp = new Timestamp(fechaHora);

                        HashMap<String, Object> coment = new HashMap<>();
                        coment.put("Publicacion", documentId);
                        coment.put("Usuario", Usuario);
                        coment.put("Fecha", timestamp);
                        coment.put("Descripcion",Des2);

                        db.collection("Comentarios").add(coment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                            }
                        });
                    }
                }
            }
        });
    }

    private void SacarComentarios(String DesPubli, Date FechaPubli, DCCallback callback){
        db = FirebaseFirestore.getInstance();
        SacarDPubli(DesPubli, FechaPubli, new DPCallback() {
            @Override
            public void onCallbackDC(String Publicacion) {
                db.collection("Comentarios").whereEqualTo("Publicacion",Publicacion).orderBy("Fecha", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String des = document.getString("Descripcion");
                                Date fec = document.getDate("Fecha");
                                String usu = document.getString("Usuario");
                                callback.onCallbackDC(des,Publicacion,usu,fec);
                            }
                        }
                    }
                });
            }
        });
    }

    private void SacarDPubli(String DesPubli, Date FechaPubli, DPCallback callback){
        db=FirebaseFirestore.getInstance();
        db.collection("Publicaciones").whereEqualTo("Descripcion", DesPubli).whereEqualTo("Fecha",FechaPubli).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        callback.onCallbackDC(documentId);
                    }
                }
            }
        });
    }

    public interface DPCallback {
        void onCallbackDC(String Publicacion);
    }
    public interface DCCallback {
        void onCallbackDC(String Descripcion, String Publicacion, String Usuario, Date Fecha);
    }

    private void SCom(String Des, Date Fec, RecyclerView recyclerView){
        Coments = new ArrayList<>();
        SacarComentarios(Des, Fec, new DCCallback() {
            @Override
            public void onCallbackDC(String Descripcion, String Publicacion, String Usuario, Date Fecha) {

                Coments.add(new ListComentPubli(Descripcion,Publicacion,Usuario,Fecha));
                AdapterComentarios adapter = new AdapterComentarios(Coments);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(contextt.getContext()));
                recyclerView.setAdapter(adapter);
            }
        });
    }

    public void iniciarInicio(Context context) {
        Intent intent = new Intent(context, nav_draw.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public void iniciarEditPubli(Context context, String Dess, Uri Fot, Date fec) {
        SacarDPubli(Dess, fec, new DPCallback() {
            @Override
            public void onCallbackDC(String Publicacion) {
                SharedPreferences datos_pub = context.getSharedPreferences("Datos_Per", MODE_PRIVATE);
                SharedPreferences.Editor editor = datos_pub.edit();
                editor.putString("Des", Dess);
                editor.putString("Fot", String.valueOf(Fot));
                editor.putString("Pub",Publicacion);
                editor.apply();

                Intent intent = new Intent(context, PublicacionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    private String extractFilePath(String fileUrl) {
        int startIndex = fileUrl.indexOf("/o/") + 3;
        int endIndex = fileUrl.indexOf("?");
        return fileUrl.substring(startIndex, endIndex).replace("%2F", "/");
    }

    private void DeleteStorage(String filePath){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference desertRef = storageRef.child(filePath);
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }

    private void DeleteLikes(String DocumentoPub){
        db=FirebaseFirestore.getInstance();
        db.collection("Likes").whereEqualTo("Publicacion", DocumentoPub).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String Document = document.getId();
                        db.collection("Likes").document(Document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                    }
                }
            }
        });
    }

    private void DeleteComentarios(String DocumentoPub){
        db=FirebaseFirestore.getInstance();
        db.collection("Comentarios").whereEqualTo("Publicacion", DocumentoPub).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        String Document = document.getId();
                        db.collection("Comentarios").document(Document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                    }
                }
            }
        });
    }
    private void DeletePublicaciones(String Documento) {
        db = FirebaseFirestore.getInstance();
        db.collection("Publicaciones").document(Documento).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                MensajeOk("Se ha borrado la publicación correctamente");
            }
        });
    }
    public void iniciarPerVisu(Context context) {
        Intent intent = new Intent(context, PerfilVisuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
