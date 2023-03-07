package com.dbsh.simplegithub.ui.search;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dbsh.simplegithub.api.model.GithubRepo;
import com.dbsh.simplegithub.databinding.ItemRepositoryBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.RepositoryHolder> {
    private List<GithubRepo> items = new ArrayList<>();

    @Nullable
    private ItemClickListener listener;

    @NonNull
    @Override
    public RepositoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ItemRepositoryBinding binding = ItemRepositoryBinding.inflate(inflater);
        return new RepositoryHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RepositoryHolder holder, int position) {
        GithubRepo data = items.get(position);
        holder.bind(data);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<GithubRepo> items) {
        this.items = items;
    }

    public void setItemClickListener(@Nullable ItemClickListener listener) {
        this.listener = listener;
    }

    public void clearItems() {
        items.clear();
    }

    class RepositoryHolder extends RecyclerView.ViewHolder {

        ColorDrawable placeholder = new ColorDrawable(Color.GRAY);
        ItemRepositoryBinding _binding;

        public RepositoryHolder(ItemRepositoryBinding binding) {
            super(binding.getRoot());
            _binding = binding;
        }

        void bind(GithubRepo repo) {
            Glide.with(_binding.getRoot())
                    .load(repo.owner.avatarUrl)
                    .placeholder(placeholder)
                    .into(_binding.ivItemRepositoryProfile);
            _binding.tvItemRepositoryName.setText(repo.fullName);
            _binding.tvItemRepositoryLanguage.setText(repo.language);
            _binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(repo);
                }
            });
        }
    }

    interface ItemClickListener {
        void onItemClick(GithubRepo repository);
    }
}
