package com.example.listacompras

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.listacompras.data.model.Item
import com.example.listacompras.databinding.ItemItemBinding

class ItensAdapter(
    private val itens: MutableList<Item>,
    private val onClick: (Item) -> Unit,
    private val onCheck: (Item) -> Unit
) : RecyclerView.Adapter<ItensAdapter.VH>() {

    private var filteredItens = itens.toMutableList()

    inner class VH(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = filteredItens.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredItens[position]

        holder.binding.tvNomeItem.text = item.nome
        holder.binding.tvDetalhe.text = "${item.quantidade} ${item.unidade}"
        holder.binding.imgCategoria.setImageResource(IconesCategoria.iconFor(item.categoria))

        holder.binding.cb.setOnCheckedChangeListener(null)
        holder.binding.cb.isChecked = item.marcado
        applyCheckedStyle(holder, item.marcado)

        // Configura o clique do Checkbox
        holder.binding.cb.setOnCheckedChangeListener { _, checked ->
            item.marcado = checked

            // Chama a função que salva no Firebase
            onCheck(item)

            // Aplica o visual
            if (checked) {
                moverItemFimLista(position)
                applyCheckedStyle(holder, true)
                Toast.makeText(holder.itemView.context, "Item riscado!", Toast.LENGTH_SHORT).show()
            } else {
                applyCheckedStyle(holder, false)
            }
        }

        holder.itemView.setOnClickListener {
            if (!holder.binding.cb.isChecked) {
                onClick(item)
            }
        }
    }

    fun updateList(novaLista: List<Item>) {
        itens.clear()
        itens.addAll(novaLista)
        filteredItens.clear()
        filteredItens.addAll(novaLista)
        notifyDataSetChanged()
    }

    private fun moverItemFimLista(position: Int) {
        if (position < 0 || position >= filteredItens.size) return
        val item = filteredItens[position]
        filteredItens.removeAt(position)
        filteredItens.add(item)

        // Reordena visualmente
        val checkedItems = filteredItens.filter { it.marcado }
        val uncheckedItems = filteredItens.filterNot { it.marcado }
        val grouped = checkedItems.groupBy { it.categoria }
        val sortedGrouped = grouped.flatMap { it.value.sortedBy { i -> i.nome } }

        filteredItens.clear()
        filteredItens.addAll(uncheckedItems + sortedGrouped)
        notifyDataSetChanged()
    }

    private fun applyCheckedStyle(holder: VH, checked: Boolean) {
        if (checked) {
            holder.binding.root.setCardBackgroundColor(Color.parseColor("#F3F3F3"))
            holder.binding.tvNomeItem.paintFlags = holder.binding.tvNomeItem.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.binding.root.setCardBackgroundColor(Color.WHITE)
            holder.binding.tvNomeItem.paintFlags = holder.binding.tvNomeItem.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun removeAt(position: Int) {
        if (position >= 0 && position < filteredItens.size) {
            val item = filteredItens[position]
            filteredItens.removeAt(position)
            itens.remove(item)
            notifyItemRemoved(position)
        }
    }

    fun getItemAt(position: Int): Item = filteredItens[position]

    fun filter(query: String) {
        filteredItens = if (query.isEmpty()) {
            itens.toMutableList()
        } else {
            itens.filter {
                it.nome.contains(query, true) || it.categoria.contains(query, true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}