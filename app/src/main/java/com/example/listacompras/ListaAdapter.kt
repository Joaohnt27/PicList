package com.example.listacompras

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListasAdapter(
    private var itens: MutableList<Lista>,
    private val onClick: (Lista) -> Unit
) : RecyclerView.Adapter<ListasAdapter.VH>() {

    private var full: MutableList<Lista> = itens.toMutableList()

    fun currentItems(): List<Lista> = itens

    fun filter(query: String) {
        val q = query.trim().lowercase()
        itens = if (q.isEmpty()) full.toMutableList()
        else full.filter { it.titulo.lowercase().contains(q) }.toMutableList()
        notifyDataSetChanged()
    }

    fun addItem(item: Lista) {
        full.add(0, item)
        filter("") // reseta lista exibida
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

        // Verifica se há uma imagem URI e carrega a imagem, se não, carrega a imagem padrão
        if (item.imageUri != null) {
            holder.img.setImageURI(android.net.Uri.parse(item.imageUri))
        } else {
            holder.img.setImageResource(item.imageRes)
        }

        // Configura o clique para abrir a tela de detalhes da lista (AddItemActivity)
        holder.itemView.setOnClickListener {
            // Cria um Intent para abrir a AddItemActivity
            val intent = Intent(holder.itemView.context, AddItemListaActivity::class.java)
            intent.putExtra("nome_lista", item.titulo)  // Passa o nome da lista para a nova Activity
            holder.itemView.context.startActivity(intent)
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgThumb)
        val titulo: TextView = v.findViewById(R.id.tvTitulo)
    }
}
