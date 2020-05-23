package com.example.purchase

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasemessager.DB
import com.example.purchase.Dialogs.Group
import com.example.purchase.Dialogs.Shopin
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var shopin = Shopin(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar) //Установить тулбар
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //Запрет на горизонтальный разворот


        //Вызывается метод, выводящий все группы
        Group(this, false).listGroups(Variable.firstGroup)

        //Вызывается метод реализующий вывод покупок самой верхней группы
        shopin?.listShopinID()
    }


    //Добавить контекстное меню на тулбар верхней части экрана
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.deleteActiveShopin -> {
                //deleteCheckedShopin()
                Variable.deleteCheckedShopin(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //Добавление новой группы
    fun addGroup(v: View){
        Group(this)
    }

    //Добавить новую покупку
    fun addShopin(v: View){
        shopin?.addShopin()
    }

    //Переместиться вверх по списку покупок
    fun topScroll(v: View){
        scroll.scrollTo(0,0)
    }

    /*fun deleteCheckedShopin(){
        var msgDialog: AlertDialog
        msgDialog = AlertDialog.Builder(this)
            ?.setTitle("Удаление отмеченных")
            ?.setMessage("Помеченные покупки будут удалены")
            ?.setPositiveButton("ДА - УДАЛИТЬ", null)
            ?.setNegativeButton("ОТМЕНА", null)
            ?.show()!!

        val positiveButton = msgDialog?.getButton(AlertDialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener(View.OnClickListener {
            shopin.deleteActiveShopin(Variable.selectGroupID!!)
            shopin.listShopinID()
            msgDialog.dismiss()
        })
    }*/
}
