import com.example.listacompras.auth.data.datasource.AuthDataSource
import com.example.listacompras.auth.data.datasource.AuthFirebaseDataSource
import com.example.listacompras.auth.data.model.Usuario
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<Usuario?>
    suspend fun cadastro(nome: String, email: String, pass: String): Result<Usuario?>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
    suspend fun recuperarSenha(email: String): Result<Boolean>
}


class AuthRepositoryImpl(
    private val dataSource: AuthDataSource = AuthFirebaseDataSource()
) : AuthRepository {

    override suspend fun cadastro(nome: String, email: String, pass: String): Result<Usuario?> {
        val authResult = dataSource.createAccount(nome, email, pass)

        return authResult.fold(
            onSuccess = { firebaseUser ->
                val uid = firebaseUser?.uid
                if (uid != null) {
                    val saveResult = dataSource.saveUserToFirestore(uid, nome, email)

                    return if (saveResult.isSuccess) {
                        val novoUsuario = Usuario(id = uid, nome = nome, email = email)
                        Result.success(novoUsuario)
                    } else {
                        Result.failure(saveResult.exceptionOrNull() ?: Exception("Erro ao salvar dados do usuário."))
                    }
                } else {
                    Result.success(null)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override suspend fun login(email: String, pass: String): Result<Usuario?> {
        val authResult = dataSource.signIn(email, pass)

        return authResult.fold(
            onSuccess = { firebaseUser ->
                val uid = firebaseUser?.uid
                if (uid != null) {
                    val firestoreResult = dataSource.getUserFromFirestore(uid)

                    firestoreResult.fold(
                        onSuccess = { usuario ->
                            val finalUser = usuario ?: Usuario(
                                id = uid,
                                nome = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: email
                            )
                            Result.success(finalUser)
                        },
                        onFailure = { error ->
                            Result.failure(error)
                        }
                    )
                } else {
                    Result.success(null)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override fun logout() = dataSource.signOut()

    override fun getCurrentUser() = dataSource.getCurrentUser()

    override suspend fun recuperarSenha(email: String): Result<Boolean> {
        return dataSource.sendPasswordResetEmail(email)
    }
}