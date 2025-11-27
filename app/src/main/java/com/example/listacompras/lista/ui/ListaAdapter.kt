package com.example.listacompras.lista.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listacompras.R
import com.example.listacompras.lista.data.model.Lista
import com.example.listacompras.databinding.ItemListaBinding

class ListasAdapter(
    private var itens: MutableList<Lista>,
    private val onClick: (Lista) -> Unit,
    private val onEdit: ((Lista) -> Unit)? = null
) : RecyclerView.Adapter<ListasAdapter.VH>() {

    private var full: MutableList<Lista> = itens.toMutableList()

    inner class VH(val binding: ItemListaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itens[position]

        holder.binding.tvTitulo.text = item.titulo

        if (!item.imageUri.isNullOrEmpty()) {
            // Convertendo a String "content://..." para um objeto uri real.
            val uri = Uri.parse(item.imageUri)

            Glide.with(holder.itemView.context)
                .load(uri) // Carrega o objeto Uri
                .centerCrop()
                .placeholder(R.drawable.placeholderimg)
                .error(R.drawable.placeholderimg)
                .into(holder.binding.imgThumb)
        } else {
            // Se não tem link nenhum, coloca o placeholder manual
            holder.binding.imgThumb.setImageResource(R.drawable.placeholderimg)
        }

        holder.itemView.setOnClickListener { onClick(item) }

        holder.itemView.setOnLongClickListener {
            onEdit?.invoke(item)
            onEdit != null
        }
    }

    override fun getItemCount() = itens.size

    fun currentItems(): List<Lista> = full

    fun filter(query: String) {
        val q = query.trim().lowercase()
        itens = if (q.isEmpty()) full.toMutableList()
        else full.filter { it.titulo.lowercase().contains(q) }.toMutableList()
        notifyDataSetChanged()
    }

    fun addItem(item: Lista) {
        full.add(0, item)
        itens.sortBy { it.titulo_lower.lowercase() }
        notifyDataSetChanged()
    }

    fun setItems(novas: List<Lista>) {
        itens.clear()
        itens.addAll(novas)
        notifyDataSetChanged()
    }

    fun renameByTitle(oldTitle: String, newTitle: String, newImageUri: String? = null) {
        val idxFull = full.indexOfFirst { it.titulo_lower == oldTitle }
        if (idxFull >= 0) {
            val antigo = full[idxFull]
            full[idxFull] = antigo.copy(
                titulo_lower = newTitle,
                imageUri = newImageUri ?: antigo.imageUri
            )
        }

        val idxShown = itens.indexOfFirst { it.titulo_lower == oldTitle }
        if (idxShown >= 0) {
            val antigo = itens[idxShown]
            itens[idxShown] = antigo.copy(
                titulo_lower = newTitle,
                imageUri = newImageUri ?: antigo.imageUri
            )
            notifyItemChanged(idxShown)
        } else {
            notifyDataSetChanged()
        }
    }
}