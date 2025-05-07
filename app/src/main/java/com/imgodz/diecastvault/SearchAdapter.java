package com.imgodz.diecastvault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final List<Diecast> diecasts;
    private final SearchActivity context;
    private final OnDiecastClickListener listener;

    public interface OnDiecastClickListener {
        void onDiecastClick(Diecast diecast);
    }

    public SearchAdapter(SearchActivity context, List<Diecast> diecasts, OnDiecastClickListener listener) {
        this.context = context;
        this.diecasts = diecasts;
        this.listener = listener;
    }

    public void updateList(List<Diecast> newList) {
        diecasts.clear();
        diecasts.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Diecast diecast = diecasts.get(position);
        holder.textName.setText(diecast.getName());
        holder.textMaker.setText(diecast.getModelMaker());
        holder.textColor.setText(diecast.getPrimaryColor());

        // Add these new lines for set series and category
        holder.textSeries.setText(diecast.getSetSeries() + " | " + diecast.getCategory());

        if (diecast.getImagePath() != null) {
            File imgFile = new File(diecast.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onDiecastClick(diecast));
    }

    @Override
    public int getItemCount() {
        return diecasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textName;
        public TextView textMaker;
        public TextView textColor;
        public TextView textSeries;
        public TextView textCategory;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewSearchResult);
            textName = itemView.findViewById(R.id.textSearchName);
            textMaker = itemView.findViewById(R.id.textSearchMaker);
            textColor = itemView.findViewById(R.id.textSearchColor);
            textSeries = itemView.findViewById(R.id.textSearchSeriesCumCategory);

        }
    }
}
