package com.example.nutrilife.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListAdapterPubliPer;
import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListPubliPerfil;
import com.example.nutrilife.R;
import com.example.nutrilife.databinding.FragmentHomeBinding;
import com.example.nutrilife.ui.perfil.PerfilFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener {
    List<ListPubliPerfil> myPubli;
    ListAdapterPubliPer adapter;
    String cor, usu_log;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipe;
    SearchView search;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView=root.findViewById(R.id.RecyclePubliInicio);
        swipe=root.findViewById(R.id.swipeRefInicio);
        search=root.findViewById(R.id.searchInicio);

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
                search.setQuery("", false);
                search.clearFocus();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipe.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        initListener();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void MostrarPublica(){
        myPubli=new ArrayList<>();
        SacarPublicaciones(new PublicacionesCallback() {
            @Override
            public void onCallbackD(String Descripcion, Date Fecha, String Image_Uri, String Usuario) {
                myPubli.add(new ListPubliPerfil(Descripcion,Fecha, Uri.parse(Image_Uri),Usuario));
                adapter=new ListAdapterPubliPer(myPubli);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);
            }
        });

    }

    private void SacarPublicaciones(final PublicacionesCallback callback){
        db = FirebaseFirestore.getInstance();
        db.collection("Publicaciones").orderBy("Fecha", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
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

    private void initListener(){
        search.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }

    public interface PublicacionesCallback {
        void onCallbackD(String Descripcion,Date Fecha, String Image_Uri, String Usuario);
    }
}