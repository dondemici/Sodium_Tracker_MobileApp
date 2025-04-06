package com.example.sodium_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private ArrayList<Recipe> recipeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }

    public RecipeAdapter(ArrayList<Recipe> recipeList, OnItemClickListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView detailsLine;

        public ViewHolder(View itemView) {
            super(itemView);
            detailsLine = itemView.findViewById(R.id.recipeDetailsLine);
        }

        public void bind(Recipe recipe, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(recipe));
        }
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        String detailsLine = recipe.getName() + " | Ingredients: " +
                recipe.getIngredientCount() + " | Sodium (per serving): " +
                String.format("%.2f", recipe.getSodiumPerServing()) + " mg";

        holder.detailsLine.setText(detailsLine);
        holder.bind(recipe, listener);
    }


    @Override
    public int getItemCount() {
        return recipeList.size();
    }
}
