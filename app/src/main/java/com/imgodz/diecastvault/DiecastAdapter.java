package com.imgodz.diecastvault;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class DiecastAdapter extends RecyclerView.Adapter<DiecastAdapter.DiecastViewHolder> implements Filterable {

    private final List<Diecast> diecastList;
    private final Context context;
    private List<Diecast> diecastListFull;

    public DiecastAdapter(Context context, List<Diecast> diecastList) {
        this.context = context;
        this.diecastList = diecastList;
        this.diecastListFull = new ArrayList<>(diecastList);
    }

    @Override
    public Filter getFilter() {
        return diecastFilter;
    }

    @NonNull
    @Override
    public DiecastAdapter.DiecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diecast, parent, false);
        return new DiecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiecastViewHolder holder, int position) {
        Diecast diecast = diecastList.get(position);
        holder.textViewName.setText(diecast.getName());
        holder.textViewMaker.setText(diecast.getModelMaker());
        holder.textViewCategory.setText(diecast.getCategory());
        holder.textViewModelNum.setText(diecast.getReleaseInfo());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DiecastDetailActivity.class);
            intent.putExtra("diecast_id", diecast.getId()); // 👈 assuming you have a unique ID
            context.startActivity(intent);
        });

        String imagePath = diecast.getImagePath();

        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                // Load 1/4th size image for performance
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;  // 1/4th resolution
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder); // fallback
            }
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder); // fallback
        }

    }

    @Override
    public int getItemCount() {
        return diecastList.size();
    }

    static class DiecastViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName, textViewMaker, textViewCategory, textViewModelNum;

        public DiecastViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewDiecast);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewMaker = itemView.findViewById(R.id.textViewMaker);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewModelNum = itemView.findViewById(R.id.textViewModelNum);
        }
    }

    private final Filter diecastFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Diecast> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(diecastListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Diecast item : diecastListFull) {
                    if (
                            item.getName().toLowerCase().contains(filterPattern) ||
                                    item.getModelMaker().toLowerCase().contains(filterPattern) ||
                                    item.getCategory().toLowerCase().contains(filterPattern)
                    ) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            diecastList.clear();
            diecastList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
