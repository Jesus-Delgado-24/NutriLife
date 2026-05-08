package com.example.nutrilife.Colecciones.Seguidores;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrilife.Colecciones.Comentarios.AdapterComentarios;
import com.example.nutrilife.Colecciones.Comentarios.ListComentPubli;
import com.example.nutrilife.PerfilVisuActivity;
import com.example.nutrilife.R;
import com.example.nutrilife.nav_draw;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterSeguidores extends RecyclerView.Adapter<AdapterSeguidores.SeguidoresHolder> {
    ViewGroup contextt;
    List<ListSeguidores> seguidores;
    FirebaseFirestore db;

    public AdapterSeguidores(List<ListSeguidores> seguidores) {
        this.seguidores = seguidores;
    }

    @NonNull
    @Override
    public AdapterSeguidores.SeguidoresHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_seguidores,parent,false);
        AdapterSeguidores.SeguidoresHolder holder = new AdapterSeguidores.SeguidoresHolder(v);
        contextt=parent;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSeguidores.SeguidoresHolder holder, int position) {
        ListSeguidores segui =seguidores.get(position);

        SharedPreferences Usuario = contextt.getContext().getSharedPreferences("Usuario",MODE_PRIVATE);
        String usu_log =Usuario.getString("Usu","");

        SacarFN(segui.getSeguidor(), new FNCallback() {
            @Override
            public void onCallbackFN(String Foto, String Nombre) {
                holder.Usuario.setText(segui.Seguidor);
                holder.Nombre.setText(Nombre);
                Picasso.get().load(Uri.parse(Foto)).into(holder.Foto);
            }
        });

        holder.Nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarPerVisu(contextt.getContext());
                Context context= contextt.getContext();
                SharedPreferences Usuarios = context.getSharedPreferences("Usuarios", MODE_PRIVATE);
                SharedPreferences.Editor editor = Usuarios.edit();
                editor.putString("Usu_Log", usu_log);
                editor.putString("Usu_Visu", holder.Usuario.getText().toString());
                editor.putBoolean("Seg", segui.isSeg);
                editor.apply();
            }
        });
    }

    @Override
    public int getItemCount() {
        return seguidores.size();
    }

    public class SeguidoresHolder extends RecyclerView.ViewHolder {
        ImageView Foto;
        TextView Nombre, Usuario;
        public SeguidoresHolder(@NonNull View itemView) {
            super(itemView);

            Nombre=itemView.findViewById(R.id.lblNom_Seg);
            Usuario=itemView.findViewById(R.id.lblUsu_Seg);
            Foto=itemView.findViewById(R.id.iVFotoSeg);
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

    public void iniciarPerVisu(Context context) {
        Intent intent = new Intent(context, PerfilVisuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
