package com.example.purchase

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasemessager.DB
import com.example.purchase.Dialogs.Group
import com.example.purchase.Dialogs.Shopin
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var shopin: Shopin ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar) //Установить тулбар
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //Запрет на горизонтальный разворот

        shopin = Shopin(this)

        //Вызывается метод, выводящий все группы

        Group(this, false).listGroups(listGroupsContainer, "first")
        shopin?.listShopinID()
    }


    //Добавить контекстное меню на тулбар верхней части экрана
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Добавление новой группы
    fun addGroup(v: View){
        Group(this)
    }

    fun addShopin(v: View){
        shopin?.addShopin()
    }
}
