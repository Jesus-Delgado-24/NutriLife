package com.example.nutrilife.ui.seguidos;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListAdapterPubliPer;
import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListPubliPerfil;
import com.example.nutrilife.R;
import com.example.nutrilife.databinding.FragmentSeguidosBinding;
import com.example.nutrilife.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GalleryFragment extends Fragment {

    List<ListPubliPerfil> myPubli;
    String cor, usu_log;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipe;

    private FragmentSeguidosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentSeguidosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView=root.findViewById(R.id.RecyclePubliSegui);
        swipe=root.findViewById(R.id.swipeRefSeg);

        SharedPreferences compartido = requireContext().getSharedPreferences("Datos_Per_2",MODE_PRIVATE);
        cor=compartido.getString("Id_Cor","");
        SharedPreferences compartir_usu_log = requireContext().getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        usu_log =compartir_usu_log.getString("Usu_Log", "");

        MostrarPublica();

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.green_ok);
        int primaryDarkColor = ContextCompat.getColor(requireContext(), R.color.green);
        swipe.setColorSchemeColors(primaryColor, primaryDarkColor);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myPubli.clear();
                MostrarPublica();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipe.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void SacarDocu(final FirestoreCallback2 callback2){
        db = FirebaseFirestore.getInstance();

        db.collection("Personas").whereEqualTo("Correo",cor).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        callback2.onCallbackD(documentId);
                    }
                }
            }
        });
    }

    public interface FirestoreCallback2 {
        void onCallbackD(String Documento);
    }

    public void MostrarPublica(){
        myPubli=new ArrayList<>();
        SacarPublicaciones(new PublicacionesCallback() {
            @Override
            public void onCallbackD(String Descripcion, Date Fecha, String Image_Uri, String Usuario) {
                myPubli.add(new ListPubliPerfil(Descripcion,Fecha, Uri.parse(Image_Uri),Usuario));
                ListAdapterPubliPer adapter=new ListAdapterPubliPer(myPubli);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);
            }
        });

    }

    private void SacarPublicaciones(final PublicacionesCallback callback){
        db = FirebaseFirestore.getInstance();
        SacarDocSeg(new PublicacionesCallback2() {
            @Override
            public void onCallbackD2(String DocSeg) {
                db.collection("Publicaciones").whereEqualTo("Usuario",DocSeg).orderBy("Fecha",Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String des = document.getString("Descripcion");
                                String image = document.getString("Foto");
                                Date fecha = document.getDate("Fecha");
                                String usu = document.getString("Usuario");
                                callback.onCallbackD(des,fecha,image,usu);
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

    private void SacarDocSeg(PublicacionesCallback2 callback2){
        db = FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Seguidores").whereEqualTo("Seguidor",Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (result != null && !result.isEmpty()) {
                                for (QueryDocumentSnapshot document : result) {
                                    String seguido = document.getString("Seguido");
                                    callback2.onCallbackD2(seguido);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    public interface PublicacionesCallback2 {
        void onCallbackD2(String DocSeg);
    }
}