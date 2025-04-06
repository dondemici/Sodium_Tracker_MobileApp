package com.example.sodium_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }

    private final List<Recipe> recipeList;
    private final OnItemClickListener listener;

    public RecipeAdapter(List<Recipe> recipeList, OnItemClickListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(recipeList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeDetailsLine;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeDetailsLine = itemView.findViewById(R.id.recipeDetailsLine);
        }

        public void bind(Recipe recipe, OnItemClickListener listener) {
            String formatted = recipe.getName() + " | Ingredients: " + recipe.getIngredientCount()
                    + " | Sodium: " + recipe.getTotalSodium() + " mg";
            recipeDetailsLine.setText(formatted);

            itemView.setOnClickListener(v -> listener.onItemClick(recipe));
        }
    }
}
