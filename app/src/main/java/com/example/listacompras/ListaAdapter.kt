package com.example.listacompras

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListasAdapter(
    private var itens: MutableList<Lista>,
    private val onClick: (Lista) -> Unit,
    private val onEdit: ((Lista) -> Unit)? = null // segurar para editar
) : RecyclerView.Adapter<ListasAdapter.VH>() {

    private var full: MutableList<Lista> = itens.toMutableList()

    // Sempre retorna todas as listas
    fun currentItems(): List<Lista> = full

    fun filter(query: String) {
        val q = query.trim().lowercase()
        itens = if (q.isEmpty()) full.toMutableList()
        else full.filter { it.titulo.lowercase().contains(q) }.toMutableList()
        notifyDataSetChanged()
    }

    fun addItem(item: Lista) {
        full.add(0, item)
        filter("") // reseta exibição
    }

    // Renomeia em full e na lista visível; atualiza só a célula quando possível
    fun renameByTitle(oldTitle: String, newTitle: String, newImageUri: String? = null) {
        // atualiza em full
        val idxFull = full.indexOfFirst { it.titulo == oldTitle }
        if (idxFull >= 0) {
            val antigo = full[idxFull]
            full[idxFull] = antigo.copy(
                titulo = newTitle,
                imageUri = newImageUri ?: antigo.imageUri
            )
        }

        // atualizaa na lista exibida
        val idxShown = itens.indexOfFirst { it.titulo == oldTitle }
        if (idxShown >= 0) {
            val antigo = itens[idxShown]
            itens[idxShown] = antigo.copy(
                titulo = newTitle,
                imageUri = newImageUri ?: antigo.imageUri
            )
            notifyItemChanged(idxShown)
        } else {
            // se estiver filtrado e o item não aparecer, sincroniza a UI
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lista, parent, false)
        return VH(v)
    }

    override fun getItemCount() = itens.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itens[position]
        holder.titulo.text = item.titulo

        if (item.imageUri != null) {
            holder.img.setImageURI(Uri.parse(item.imageUri))
        } else {
            holder.img.setImageResource(item.imageRes)
        }

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener {
            onEdit?.invoke(item)
            onEdit != null
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgThumb)
        val titulo: TextView = v.findViewById(R.id.tvTitulo)
    }
}
