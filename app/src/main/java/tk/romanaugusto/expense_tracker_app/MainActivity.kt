package tk.romanaugusto.expense_tracker_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Job())
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var response:HttpResponse<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnListener()
    }

    private fun btnListener () {
        findViewById<Button>(R.id.login).setOnClickListener {
            Toast.makeText(this@MainActivity, "Attempting to Login...", Toast.LENGTH_SHORT).show()
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()
            try {
                scope.launch {
                callAPILoginTask(email, password)
            }.invokeOnCompletion {
                    mainScope.launch {
                        if (it == null) {
                            when (response.code) {
                                404 -> Toast.makeText(
                                    this@MainActivity,
                                    "Service Unavailable",
                                    Toast.LENGTH_SHORT
                                ).show()
                                in 200..299 -> {
                                    val resBody = if (response.body?.isNotBlank() == true) JSONObject(response.body) else null
                                    if (resBody?.optBoolean("isSuccess", false) == true) {
                                        saveSettings(resBody)
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Login Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val i = Intent(this@MainActivity, HomeScreen::class.java)
                                        startActivity(i)
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Login Failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    println("Login Data => ${response.body}")
                                }
                                else -> Toast.makeText(
                                    this@MainActivity,
                                    "Login Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            it.printStackTrace()
                            println("Login Request Failed: ${it.cause}")
                        }
                    }

            }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Login Request Failed: ${e.cause}")
            }
        }
    }

    private fun saveSettings(settings:JSONObject): Boolean {
        return try {
            val fileName = "settings.json"
            applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(settings.toString().toByteArray())
            }
            true
        } catch (e:Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun callAPILoginTask (email: String, password: String) {
            val url = "http://10.178.24.180:8080/logon"
            val requestBody = JSONObject(mapOf("email" to email, "password" to password))
            println(requestBody.toString())
            response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("accept", "*/*")
                .body(requestBody.toString())
                .asString()
    }

}