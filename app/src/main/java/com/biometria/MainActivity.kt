package com.biometria

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("user_data", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val biometricManager = BiometricManager.from(this)

        edit_text_user.setText(sharedPreferences.getString("user", ""))

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Toast.makeText(this, "SUCCESS", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(this, "ERROR HARDWARE", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(this, "ERROR UNAVAILABLE", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(this, "ERROR NONE ENROLLED", Toast.LENGTH_LONG).show()

        }

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errorString: CharSequence) {
                    super.onAuthenticationError(errorCode, errorString)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("biometric", false)
                    editor.apply()
                    edit_text_password.visibility = View.VISIBLE
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login por biometria")
            .setSubtitle("Coloque sua impressão digital para fazer o login")
            .setNegativeButtonText("Continuar com senha")
            .build()

        button_login.setOnClickListener {
            button_login.visibility = View.GONE
            button_login2.visibility = View.VISIBLE
            if (edit_text_user.text.toString() == "rafinha" && sharedPreferences.getBoolean("biometric", false))
                biometricPrompt.authenticate(promptInfo)
            else
                edit_text_password.visibility = View.VISIBLE
        }
        button_login2.setOnClickListener{
            if (edit_text_user.text.toString() == "rafinha" && edit_text_password.text.toString() == "12345" )
                showDialog()
        }
    }

    private fun showDialog() {
        AlertDialog.Builder(this)
            .setTitle("Login por Biometria")
            .setMessage("Deseja fazer o login por biometria na proxima vez?")
            .setPositiveButton("SIM") { _, _ ->
                val editor = sharedPreferences.edit()
                editor.putBoolean("biometric", true)
                editor.putString("user", edit_text_user.text.toString())
                editor.apply()
                startActivity()
            }
            .setNegativeButton("NÃO") { _, _ ->
                val editor = sharedPreferences.edit()
                editor.putBoolean("biometric", false)
                editor.putString("user", edit_text_user.text.toString())
                editor.apply()
                startActivity()
            }
            .setNeutralButton("PERGUNTAR DEPOIS", null)
            .setIcon(R.drawable.ic_fingerprint)
            .show()
    }

    private fun startActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}