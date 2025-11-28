package com.example.listacompras.auth.data.datasource

import com.example.listacompras.auth.data.model.Usuario
import com.google.firebase.auth.FirebaseUser

interface AuthDataSource {
    suspend fun createAccount(nome: String, email: String, pass: String): Result<FirebaseUser?>
    suspend fun saveUserToFirestore(uid: String, nome: String, email: String): Result<Unit>
    suspend fun signIn(email: String, pass: String): Result<FirebaseUser?>
    suspend fun getUserFromFirestore(uid: String): Result<Usuario?>
    fun signOut()
    fun getCurrentUser(): FirebaseUser?
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean>
}