package com.example.listacompras.common

import com.example.listacompras.R

object IconesCategoria {

    private val MAP = mapOf(
        "hortifruti" to R.drawable.nutrition_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "hortifrúti" to R.drawable.nutrition_24dp_e3e3e3_fill0_wght400_grad0_opsz24, // com acento

        "padaria e confeitaria" to R.drawable.breakfast_dining_24dp_e3e3e3_fill0_wght400_grad0_opsz24,

        "acougue e peixaria" to R.drawable.set_meal_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "açougue e peixaria" to R.drawable.set_meal_24dp_e3e3e3_fill0_wght400_grad0_opsz24,

        "frios e laticinios" to R.drawable.local_pizza_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "frios e laticínios" to R.drawable.local_pizza_24dp_e3e3e3_fill0_wght400_grad0_opsz24,

        "congelados" to R.drawable.mode_cool_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "mercearia seca" to R.drawable.shopping_basket_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "doces e snacks" to R.drawable.cookie_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "bebidas" to R.drawable.local_drink_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "infantil" to R.drawable.child_care_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "pet shop" to R.drawable.pets_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "limpeza" to R.drawable.cleaning_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "higiene pessoal e beleza" to R.drawable.clean_hands_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "saude e farmacia" to R.drawable.medical_services_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "saúde e farmácia" to R.drawable.medical_services_24dp_e3e3e3_fill0_wght400_grad0_opsz24,

        "utilidades domesticas e outros" to R.drawable.home_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
        "utilidades domésticas e outros" to R.drawable.home_24dp_e3e3e3_fill0_wght400_grad0_opsz24
    )

    private fun normalize(s: String?) = s
        ?.trim()
        ?.lowercase()
        ?.replace(Regex("[áàâã]"), "a")
        ?.replace(Regex("[éê]"), "e")
        ?.replace(Regex("[í]"), "i")
        ?.replace(Regex("[óôõ]"), "o")
        ?.replace(Regex("[ú]"), "u")
        ?.replace(Regex("[ç]"), "c")

    fun iconFor(category: String?): Int {
        val key = normalize(category) ?: return R.drawable.shopping_bag_24dp_e3e3e3_fill0_wght400_grad0_opsz24
        return MAP[key] ?: R.drawable.shopping_bag_24dp_e3e3e3_fill0_wght400_grad0_opsz24
    }
}
