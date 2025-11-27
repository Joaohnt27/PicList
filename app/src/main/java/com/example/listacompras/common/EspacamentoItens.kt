package com.example.listacompras.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EspacamentoItens(private val spaceDp: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State) {
        val s = (v.resources.displayMetrics.density * spaceDp).toInt()
        outRect.set(s, s, s, s)
    }
}
