package com.example.pronosticos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pronosticos.databinding.FragmentPronosticosBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PronosticosFragment extends Fragment implements PronosticoAdapter.OnPronosticoListener {

    private FragmentPronosticosBinding binding;
    private final List<Pronostico> lista = new ArrayList<>();
    private final List<String> ids = new ArrayList<>();
    private PronosticoAdapter adapter;
    private CollectionReference ref;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPronosticosBinding.inflate(inflater, container, false);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid).collection("pronosticos");

        adapter = new PronosticoAdapter(requireContext(), lista, ids, this);
        binding.rvPronosticos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPronosticos.setAdapter(adapter);

        // Boton flotante: registrar nuevo pronostico
        binding.fabAgregar.setOnClickListener(v ->
                startActivity(new Intent(getContext(), FormularioActivity.class)));

        return binding.getRoot();
    }

    // SnapshotListener: la lista se actualiza sola en tiempo real (Clase 09.2, pag. 48)
    @Override
    public void onStart() {
        super.onStart();
        registration = ref.orderBy("fechaPartido")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    lista.clear();
                    ids.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Pronostico p = doc.toObject(Pronostico.class);
                        lista.add(p);
                        ids.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // Buena practica: quitar el listener al pausar (Clase 09.2, pag. 49)
    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) registration.remove();
    }

    @Override
    public void onEditar(int position) {
        Pronostico p = lista.get(position);
        Intent intent = new Intent(getContext(), FormularioActivity.class);
        intent.putExtra("modo", "editar");
        intent.putExtra("id", ids.get(position));
        intent.putExtra("seleccionA", p.getSeleccionA());
        intent.putExtra("seleccionB", p.getSeleccionB());
        intent.putExtra("golesA", p.getGolesA());
        intent.putExtra("golesB", p.getGolesB());
        intent.putExtra("fecha", p.getFechaPartido().toDate().getTime());
        intent.putExtra("estado", p.getEstado());
        startActivity(intent);
    }

    @Override
    public void onEliminar(int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar pronostico")
                .setMessage("Se eliminara el pronostico "
                        + lista.get(position).getSeleccionA() + " vs "
                        + lista.get(position).getSeleccionB() + ". Esta operacion no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) ->
                        ref.document(ids.get(position)).delete()
                                .addOnSuccessListener(unused ->
                                        NotificationHelper.notificar(requireContext(),
                                                "Pronostico eliminado",
                                                "El pronostico fue eliminado correctamente."))
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error al eliminar",
                                                Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}