package com.example.listacompras

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox

class ItensAdapter(
    private val itens: MutableList<Item>,
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<ItensAdapter.VH>() {

    private var filteredItens = itens

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_item, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = filteredItens.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredItens[position]

        // Texto
        holder.nome.text = item.nome
        holder.detalhe.text = "${item.quantidade} ${item.unidade}"

        // Ícone de categoria
        holder.imgCategoria.setImageResource(IconesCategoria.iconFor(item.categoria))

        // Checkbox + estilo marcado
        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = item.marcado
        applyCheckedStyle(holder, item.marcado)

        holder.cb.setOnCheckedChangeListener { _, checked ->
            item.marcado = checked
            if (checked) {
                // Se estiver marcado, move o item para o final da lista
                moverItemFimLista(position)
                applyCheckedStyle(holder, checked)
            } else {
                val itemDesmarcado = itens[position]
                itens.removeAt(position)
                itens.add(position, itemDesmarcado)
                notifyItemMoved(position, itens.indexOf(itemDesmarcado))
                applyCheckedStyle(holder, checked)
            }
        }

        holder.itemView.setOnClickListener {
            if (!holder.cb.isChecked) {
                onClick(item)
            }
        }
    }
    private fun moverItemFimLista(position: Int) {
        val item = itens[position]
        // Remove o item da posição atual
        itens.removeAt(position)
        // Adiciona o item ao final
        itens.add(item)

        // Filtra os itens marcados e não marcados
        val checkedItems = itens.filter { it.marcado } // Itens marcados
        val uncheckedItems = itens.filterNot { it.marcado } // Itens não marcados

        // Agrupar os itens marcados por categoria
        val groupedByCategory = checkedItems.groupBy { it.categoria }

        // Ordena as categorias dentro dos itens marcados
        val sortedGroupedItems = groupedByCategory.flatMap { entry ->
            entry.value.sortedBy { it.nome } // Ordena os itens dentro de cada categoria
        }

        // Recria a lista final com os itens não marcados primeiro, seguidos dos itens marcados organizados por categoria
        val newList = uncheckedItems + sortedGroupedItems

        // Atualiza a lista original
        itens.clear()
        itens.addAll(newList)

        // Notifica o RecyclerView da mudança
        notifyDataSetChanged() // Atualiza a lista
    }

    private fun applyCheckedStyle(holder: VH, checked: Boolean) {
        if (checked) {
            holder.card.setCardBackgroundColor(Color.parseColor("#F3F3F3"))
            holder.nome.paintFlags = holder.nome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.nome.paintFlags = holder.nome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun removeAt(position: Int) {
        itens.removeAt(position)
        notifyItemRemoved(position)
    }
    fun getItemAt(position: Int): Item = itens[position]

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v as MaterialCardView
        val imgCategoria: ImageView = v.findViewById(R.id.imgCategoria)
        val nome: TextView = v.findViewById(R.id.tvNomeItem)
        val detalhe: TextView = v.findViewById(R.id.tvDetalhe)
        val cb: MaterialCheckBox = v.findViewById(R.id.cb)
    }

    fun filter(query: String) {
        filteredItens = if (query.isEmpty()) {
            itens
        }else{
            itens.filter {
                it.nome.contains(query, true) || it.categoria.contains(query, true)
            }
        }.toMutableList()
        notifyDataSetChanged()
    }
}