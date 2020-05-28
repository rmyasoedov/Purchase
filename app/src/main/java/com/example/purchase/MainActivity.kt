package com.example.purchase

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasemessager.DB
import com.example.purchase.Dialogs.Group
import com.example.purchase.Dialogs.Shopin
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var shopin = Shopin(this)
    var db = DB(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar) //Установить тулбар
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //Запрет на горизонтальный разворот

        //Вызывается метод, выводящий все группы
        Group(this, false).listGroups(Variable.firstGroup)

        //Вызывается метод реализующий вывод покупок самой верхней группы
        shopin?.listShopinID()
        //-------------------------------------------------------
    }

    //Добавить контекстное меню на тулбар верхней части экрана
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.deleteActiveShopin -> {
                Variable.deleteCheckedShopin(this)
                return true
            }

            R.id.annotationClick ->{
                val intent = Intent(this,AnnotationActivity::class.java)
                startActivity(intent)
            }

            R.id.clearAll ->{
                menuClearAll()
                return true
            }

            R.id.share -> {
                Group(this, true).clickShare(false, 0)
                return true
            }

            R.id.shareList ->{
                Group(this, true).shareEasyList(false,0)
                return true
            }

            R.id.about->{
                val intent = Intent(this,AboutActivity::class.java)
                startActivity(intent)
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


    fun menuClearAll(){
        var builder: AlertDialog
        builder = AlertDialog.Builder(this)
            ?.setTitle("Удаление")
            ?.setMessage("Список будет очищен!")
            ?.setPositiveButton("ДА ОЧИСТИТЬ", null)
            ?.setNegativeButton("ОТМЕНА", null)
            ?.show()!!

        var okButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)

        okButton.setOnClickListener(View.OnClickListener {
            Group(this, false).deleteAllGroups()
            builder.dismiss()
        })
    }
}
