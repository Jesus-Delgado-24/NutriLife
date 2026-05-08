package com.example.nutrilife.Colecciones.Comentarios;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListAdapterPubliPer;
import com.example.nutrilife.R;
import com.example.nutrilife.nav_draw;
import com.example.nutrilife.ui.perfil.PerfilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class AdapterComentarios extends RecyclerView.Adapter<AdapterComentarios.ComentariosHolder>{
    List<ListComentPubli> comentario;
    private FirebaseFirestore db;
    ViewGroup contextt;
    String usu_log;

    public AdapterComentarios(List<ListComentPubli> comentario) {
        this.comentario = comentario;
    }

    @NonNull
    @Override
    public AdapterComentarios.ComentariosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_compubli,parent,false);
        AdapterComentarios.ComentariosHolder holder = new AdapterComentarios.ComentariosHolder(v);
        contextt=parent;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterComentarios.ComentariosHolder holder, int position) {
        ListComentPubli coment = comentario.get(position);

        SharedPreferences compartir_usu_log = contextt.getContext().getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        usu_log =compartir_usu_log.getString("Usu_Log", "");

        if(!usu_log.equals(coment.getUsuario())){
            holder.Menu.setVisibility(View.GONE);
        }

        SacarFN(coment.getUsuario(), new FNCallback() {
            @Override
            public void onCallbackFN(String Foto, String Nombre) {
                holder.Nombre.setText(Nombre);
                Picasso.get().load(Uri.parse(Foto)).into(holder.Foto);
            }
        });
        holder.Descripcion.setText(coment.getDescripcion());

        holder.Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
                View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_menupubli,null);
                builder.setView(view);
                ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                ((Button)view.findViewById(R.id.btnEditPub)).setText("Editar comentario");
                ((Button)view.findViewById(R.id.btnDeletPub)).setText("Borrar comentario");

                view.findViewById(R.id.btnEditPub).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText textCom;
                        Button Act;
                        AlertDialog.Builder builder = new AlertDialog.Builder(contextt.getContext(),R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(contextt.getContext()).inflate(R.layout.layout_editcomment,null);
                        builder.setView(view);
                        ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                        final AlertDialog alertDialog = builder.create();

                        textCom=view.findViewById(R.id.txtText_Com);
                        Act=view.findViewById(R.id.btnComentar);

                        textCom.setText(coment.getDescripcion());

                        Act.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String dess="";
                                dess=textCom.getText().toString();
                                if(!dess.equals("")) {
                                    SacarDocuC(coment.getUsuario(), coment.getPublicacion(), coment.getFecha(), coment.getDescripcion(), new DCCallback() {
                                        @Override
                                        public void onCallbackDC(String DocumentC) {
                                            Update(DocumentC, textCom.getText().toString());
                                        }
                                    });
                                }else{
                                    MensajeE("No puedes dejar un comentario vacío");
                                }
                            }
                        });

                        if (alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        alertDialog.show();
                    }
                });

                view.findViewById(R.id.btnDeletPub).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SacarDocuC(coment.getUsuario(), coment.getPublicacion(), coment.getFecha(), coment.getDescripcion(), new DCCallback() {
                            @Override
                            public void onCallbackDC(String DocumentC) {
                                alertDialog.dismiss();
                                DeletComentario(DocumentC);
                            }
                        });
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

    }

    @Override
    public int getItemCount() {
        return comentario.size();
    }

    public class ComentariosHolder extends RecyclerView.ViewHolder {
        ImageView Foto;
        TextView Nombre, Descripcion;
        ImageButton Menu;

        public ComentariosHolder(@NonNull View itemView) {
            super(itemView);

            Foto=itemView.findViewById(R.id.iVFotoPer_Com2);
            Nombre=itemView.findViewById(R.id.lblNom_Com2);
            Descripcion=itemView.findViewById(R.id.lblDes_Com2);
            Menu=itemView.findViewById(R.id.btnMenuComent);
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

    public interface FNCallback {
        void onCallbackFN(String Foto, String Nombre);
    }

    private void SacarDocuC(String DocUsu, String DocPub, Date Fecha, String Desc, DCCallback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Comentarios").whereEqualTo("Descripcion", Desc).whereEqualTo("Fecha", Fecha).whereEqualTo("Usuario", DocUsu).whereEqualTo("Publicacion", DocPub).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    public interface DCCallback {
        void onCallbackDC(String DocumentC);
    }

    private void DeletComentario(String Document){
        db = FirebaseFirestore.getInstance();
        db.collection("Comentarios").document(Document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                MensajeOk("Se ha borrado el comentario correctamente");
            }
        });
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

    private void SacarComentarios(String DocPub, DCCallback2 callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Comentarios").whereEqualTo("Publicacion",DocPub).orderBy("Fecha", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String des = document.getString("Descripcion");
                        Date fec = document.getDate("Fecha");
                        String usu = document.getString("Usuario");
                        callback.onCallbackDC(des,DocPub,usu,fec);
                    }
                }
            }
        });
    }
    public interface DCCallback2 {
        void onCallbackDC(String Descripcion, String Publicacion, String Usuario, Date Fecha);
    }

    private void SCom(String DocPub, RecyclerView recyclerView){
        List<ListComentPubli> Coments = new ArrayList<>();
        SacarComentarios(DocPub, new DCCallback2() {
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

    private void Update(String DocumentC, String Des){
        db=FirebaseFirestore.getInstance();

        HashMap<String, Object> com = new HashMap<>();
        com.put("Descripcion", Des);

        db.collection("Comentarios").document(DocumentC).update(com).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                MensajeOk("Se ha actualizado tu comentario correctamente");
            }
        });
    }
}
