package com.dbsh.simplegithub.ui.search

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.dbsh.simplegithub.ui.search.SearchAdapter.RepositoryHolder
import com.dbsh.simplegithub.api.model.GithubRepo
import android.view.ViewGroup
import android.view.LayoutInflater
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.Glide
import com.dbsh.simplegithub.databinding.ItemRepositoryBinding
import java.util.ArrayList

class SearchAdapter : RecyclerView.Adapter<RepositoryHolder>() {
    private var items: MutableList<GithubRepo> = mutableListOf()
    private var listener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryHolder {
        val inflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding: ItemRepositoryBinding = ItemRepositoryBinding.inflate(inflater)
        return RepositoryHolder(binding)
    }

    override fun onBindViewHolder(holder: RepositoryHolder, position: Int) {
        val data = items[position]
        holder.bind(data)
    }

    override fun getItemCount() = items.size

    fun setItems(items: MutableList<GithubRepo>) {
        this.items = items.toMutableList()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun clearItems() {
        items.clear()
    }

    inner class RepositoryHolder(binding: ItemRepositoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var placeholder = ColorDrawable(Color.GRAY)
        private var _binding: ItemRepositoryBinding

        init {
            _binding = binding
        }

        fun bind(repo: GithubRepo) {
            Glide.with(_binding.root)
                .load(repo.owner.avatarUrl)
                .placeholder(placeholder)
                .into(_binding.ivItemRepositoryProfile)
            _binding.tvItemRepositoryName.text = repo.fullName
            _binding.tvItemRepositoryLanguage.text = repo.language
            _binding.root.setOnClickListener {
                if (listener != null) {
                    listener!!.onItemClick(repo)
                }
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(repository: GithubRepo)
    }
}