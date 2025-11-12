package de.ur.connect

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException

class Credentials(private val activity: Activity) {

    data class Login(
        val name: String,
        val passwd: String
    )

    private val credentialManager = CredentialManager.create(activity)

    suspend fun get(): Login? {
        return try {
            val passwordOption = GetPasswordOption()
            val request = GetCredentialRequest(
                credentialOptions = listOf(passwordOption)
            )
            val response: GetCredentialResponse =
                credentialManager.getCredential(
                    context = activity,
                    request = request
                )
            val credential = response.credential as? PasswordCredential
            credential?.let { Login(it.id, it.password) }

        } catch (e: GetCredentialCancellationException) {
            null // user cancelled the request
        } catch (e: NoCredentialException) {
            null // no credentials saved
        } catch (e: GetCredentialException) {
            null // any other error fetching credentials
        }
    }

    public suspend fun save(login: Login) {
        val request = CreatePasswordRequest(
            login.name,
            login.passwd
        )
        try {
            credentialManager.createCredential(
                context = activity,
                request = request
            )
        } catch (_: Throwable) {}
    }
}
