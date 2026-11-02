package com.example.pronosticos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pronosticos.databinding.ItemPronosticoBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PronosticoAdapter extends RecyclerView.Adapter<PronosticoAdapter.PronosticoViewHolder> {

    private final Context context;
    private final List<Pronostico> lista;
    private final List<String> ids;
    private final OnPronosticoListener listener;

    public interface OnPronosticoListener {
        void onEditar(int position);
        void onEliminar(int position);
    }

    public PronosticoAdapter(Context context, List<Pronostico> lista, List<String> ids,
                             OnPronosticoListener listener) {
        this.context = context;
        this.lista = lista;
        this.ids = ids;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PronosticoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPronosticoBinding binding = ItemPronosticoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PronosticoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PronosticoViewHolder holder, int position) {
        Pronostico p = lista.get(position);

        holder.binding.tvSelecciones.setText(p.getSeleccionA() + "  vs  " + p.getSeleccionB());
        holder.binding.tvMarcador.setText(p.getGolesA() + " - " + p.getGolesB());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.binding.tvFecha.setText("Fecha: " + sdf.format(p.getFechaPartido().toDate()));

        holder.binding.tvEstado.setText(p.getEstado());
        switch (p.getEstado()) {
            case "Acertado": holder.binding.tvEstado.setTextColor(Color.parseColor("#2E7D32")); break;
            case "Fallado":  holder.binding.tvEstado.setTextColor(Color.parseColor("#C62828")); break;
            default:         holder.binding.tvEstado.setTextColor(Color.parseColor("#F9A825")); break;
        }

        // Click corto: editar (solo si esta Pendiente)
        holder.binding.cardPronostico.setOnClickListener(v -> {
            if ("Pendiente".equals(p.getEstado())) {
                listener.onEditar(position);
            } else {
                Toast.makeText(context, "El pronostico esta " + p.getEstado()
                        + " y ya no se puede editar", Toast.LENGTH_SHORT).show();
            }
        });

        // Click largo: eliminar (solo si esta Pendiente) con dialogo de confirmacion
        holder.binding.cardPronostico.setOnLongClickListener(v -> {
            if ("Pendiente".equals(p.getEstado())) {
                listener.onEliminar(position);
            } else {
                Toast.makeText(context, "Solo se pueden eliminar pronosticos pendientes",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class PronosticoViewHolder extends RecyclerView.ViewHolder {
        ItemPronosticoBinding binding;
        PronosticoViewHolder(ItemPronosticoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}