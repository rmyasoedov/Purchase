package com.example.purchase

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar) //Установить тулбар
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //Запрет на горизонтальный разворот


        ratingBar.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.ivanovolegdevstudio.us.shop2")
            startActivity(intent)
        })

    }

}
