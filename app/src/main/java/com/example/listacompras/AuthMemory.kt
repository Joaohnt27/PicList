package com.example.listacompras

object AuthMemory {
    private val users = mutableListOf<Usuario>()

    fun register(u: Usuario): Boolean {
        if (users.any { it.email.equals(u.email, ignoreCase = true) }) return false
        users.add(u)
        return true
    }

    fun isValid(email: String, senha: String): Boolean {
        // Responsável por manter as credenciais mockadas
        if (email == "admin@teste.com" && senha == "bruno123") return true
        return users.any { it.email.equals(email, true) && it.senha == senha }
    }
}
