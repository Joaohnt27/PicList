package com.example.listacompras.item.ui

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.listacompras.common.IconesCategoria
import com.example.listacompras.item.data.model.Item
import com.example.listacompras.databinding.ItemItemBinding

class ItensAdapter(
    private val itens: MutableList<Item>,
    private val onClick: (Item) -> Unit,
    private val onCheck: (Item) -> Unit
) : RecyclerView.Adapter<ItensAdapter.VH>() {

    inner class VH(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = itens.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itens[position]

        holder.binding.tvNomeItem.text = item.nome
        holder.binding.tvDetalhe.text = "${item.quantidade} ${item.unidade}"
        holder.binding.imgCategoria.setImageResource(IconesCategoria.iconFor(item.categoria))

        // Limpa listener para evitar loops
        holder.binding.cb.setOnCheckedChangeListener(null)
        holder.binding.cb.isChecked = item.marcado

        // Aplicaa estilos visual
        applyCheckedStyle(holder, item.marcado)

        holder.binding.cb.setOnCheckedChangeListener { _, checked ->
            item.marcado = checked
            applyCheckedStyle(holder, checked)

            onCheck(item)

            if (checked) {
                Toast.makeText(holder.itemView.context, "Item riscado!", Toast.LENGTH_SHORT).show()
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
        notifyDataSetChanged()
    }

    private fun applyCheckedStyle(holder: VH, checked: Boolean) {
        if (checked) {
            holder.binding.root.setCardBackgroundColor(Color.parseColor("#F3F3F3"))
            holder.binding.tvNomeItem.paintFlags = holder.binding.tvNomeItem.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.root.alpha = 0.6f
        } else {
            holder.binding.root.setCardBackgroundColor(Color.WHITE)
            holder.binding.tvNomeItem.paintFlags = holder.binding.tvNomeItem.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.binding.root.alpha = 1.0f
        }
    }

    fun getItemAt(position: Int): Item = itens[position]

    fun removeAt(position: Int) {
        if (position in itens.indices) {
            itens.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}