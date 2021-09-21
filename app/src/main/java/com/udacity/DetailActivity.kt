package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private var status = ""
    private var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        fileName = intent.getStringExtra("fileName").toString()
        file_name.text = fileName
        status = intent.getStringExtra("status").toString()
        status_text.text = status

        btnOk.setOnClickListener {
            val  mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
        }
    }
}
