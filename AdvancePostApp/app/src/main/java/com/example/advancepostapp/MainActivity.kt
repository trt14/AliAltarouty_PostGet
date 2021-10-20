package com.example.advancepostapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var InputName: EditText
    lateinit var txtvName: TextView
    lateinit var btnAdd: Button
    lateinit var btnView: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InputName = findViewById(R.id.etName)
        txtvName = findViewById(R.id.txtvName)
        btnAdd = findViewById(R.id.btnAdd)
        btnView = findViewById(R.id.btnViewAll)

        btnAdd.setOnClickListener {
            if(InputName.text.isNotEmpty()){
                addNewName(InputName.text.toString())

            }else{
                Toast.makeText(this,"please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        btnView.setOnClickListener {
            getAllNames()
        }
    }

    private fun addNewName(name: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val jsonObject = JSONObject()
            try {
                jsonObject.put("name", name)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://dojo-recipes.herokuapp.com/custom-people/")
                .post(requestBody)
                .build()

            var response: Response? = null
            try {
                response = client.newCall(request).execute()
                if(response.code == 201){
                    withContext(Dispatchers.Main){
                        InputName.text.clear()
                        Toast.makeText(this@MainActivity,"added successfully",Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity,"something wrong",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun getAllNames() {
        txtvName.text = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                    .url("https://dojo-recipes.herokuapp.com/custom-people/")
                    .build()
                val response =
                    withContext(Dispatchers.Default) {
                        okHttpClient.newCall(request).execute()
                    }
                if (response != null) {
                    if (response.code == 200) {
                        val jsonArray = JSONArray(response.body!!.string())
                        Log.d("Something", jsonArray.toString())
                        for(index in 0 until jsonArray.length()){
                            val nameObj = jsonArray.getJSONObject(index)
                            val name = nameObj.getString("name")
                            withContext(Main){
                                txtvName.text = "${txtvName.text}\n$name"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("txtError", e.message.toString())
            }
        }
    }
}
