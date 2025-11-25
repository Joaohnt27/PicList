package com.example.listacompras

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.listacompras.data.model.Item
import com.example.listacompras.databinding.ItemItemBinding // Import do Binding

class ItensAdapter(
    private val itens: MutableList<Item>,
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<ItensAdapter.VH>() {

    // Lista auxiliar para o filtro de busca
    private var filteredItens = itens.toMutableList()

    // --- VIEW BINDING (Requisito do Projeto) ---
    inner class VH(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = filteredItens.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredItens[position]

        // Usando Binding ao invés de findViewById
        holder.binding.tvNomeItem.text = item.nome
        holder.binding.tvDetalhe.text = "${item.quantidade} ${item.unidade}"

        // Ícone de categoria (Sua lógica mantida)
        holder.binding.imgCategoria.setImageResource(IconesCategoria.iconFor(item.categoria))

        // Checkbox + estilo marcado
        // Removemos o listener temporariamente para evitar loops infinitos ao setar o valor
        holder.binding.cb.setOnCheckedChangeListener(null)
        holder.binding.cb.isChecked = item.marcado
        applyCheckedStyle(holder, item.marcado)

        holder.binding.cb.setOnCheckedChangeListener { _, checked ->
            item.marcado = checked

            // Aqui seria ideal chamar o ViewModel para salvar no Firestore:
            // onCheck(item) <--- Implementar futuro

            if (checked) {
                moverItemFimLista(position)
                applyCheckedStyle(holder, true)
                Toast.makeText(holder.itemView.context, "Item riscado, jovem!", Toast.LENGTH_SHORT).show()
            } else {
                // Lógica inversa simplificada para UX imediata
                applyCheckedStyle(holder, false)
            }
        }

        holder.itemView.setOnClickListener {
            if (!holder.binding.cb.isChecked) {
                onClick(item)
            }
        }
    }

    // --- FUNÇÃO QUE FALTAVA (Resolve o erro da Activity) ---
    fun updateList(novaLista: List<Item>) {
        itens.clear()
        itens.addAll(novaLista)

        // Atualiza a lista filtrada também para refletir os dados novos
        filteredItens.clear()
        filteredItens.addAll(novaLista)

        notifyDataSetChanged()
    }

    // --- SUA LÓGICA DE REORDENAÇÃO (MANTIDA) ---
    private fun moverItemFimLista(position: Int) {
        if (position < 0 || position >= filteredItens.size) return

        val item = filteredItens[position]

        // Atualiza a lista visual (filteredItens)
        filteredItens.removeAt(position)
        filteredItens.add(item)

        // Aqui repetimos sua lógica de agrupar, mas operando sobre a lista visual
        val checkedItems = filteredItens.filter { it.marcado }
        val uncheckedItems = filteredItens.filterNot { it.marcado }

        val groupedByCategory = checkedItems.groupBy { it.categoria }
        val sortedGroupedItems = groupedByCategory.flatMap { entry ->
            entry.value.sortedBy { it.nome }
        }

        val newList = uncheckedItems + sortedGroupedItems

        filteredItens.clear()
        filteredItens.addAll(newList)

        notifyDataSetChanged()
    }

    private fun applyCheckedStyle(holder: VH, checked: Boolean) {
        if (checked) {
            holder.binding.root.setCardBackgroundColor(Color.parseColor("#F3F3F3")) // root é o CardView
            holder.binding.tvNomeItem.paintFlags = holder.binding.tvNomeItem.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.binding.root.setCardBackgroundColor(Color.WHITE)
            holder.binding.tvNomeItem.paintFlags = holder.binding.tvNomeItem.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    // --- MÉTODOS AUXILIARES ---
    fun removeAt(position: Int) {
        if (position >= 0 && position < filteredItens.size) {
            val itemRemovido = filteredItens[position]
            filteredItens.removeAt(position)
            itens.remove(itemRemovido) // Remove da lista original também
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