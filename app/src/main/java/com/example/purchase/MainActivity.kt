package com.example.purchase

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

            R.id.annotationClick ->{
                val intent = Intent(this,AnnotationActivity::class.java)
                startActivity(intent)
            }

            R.id.clearAll ->{
                menuClearAll()
                return true
            }

            R.id.share -> {
                clickShare()
                return true
            }

            R.id.shareList ->{
                shareEasyList()
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

    fun clickShare(){

        var database = db?.writableDatabase
        var cursor = database?.rawQuery("select *from "+db?.ANNOT+" order by ANNOT_NAME", null)

        var str = "мой список\n"
        if(cursor?.moveToFirst()!!){
            do{
                str = str+" "+cursor.getString(cursor.getColumnIndex("ANNOT_NAME"))+"   Ц: "+
                        cursor.getFloat(cursor.getColumnIndex("ANNOT_COST"))+"=\n"
            }while (cursor?.moveToNext())
        }
        str+="Пожалуйста, вот мой список :)"
        Variable.Share(str, this)
        cursor.close()
        database.close()
    }

    fun shareEasyList(){
        var database = db?.writableDatabase
        var cursor = database?.rawQuery("select sh.SH_NAME as NAME, " +
                "                                           sh.SH_COUNTS as COUNTS, " +
                "                                           sh.SH_COST as COST, " +
                "                                           (sh.SH_COST*sh.SH_COUNTS) AS SUMMA, " +
                "                                           SH_ACTIVATE as ACTIVATE," +
                "                                            gr.GROUP_NAME AS GROUPP " +
                "                                           FROM "+db.SHOPIN+" sh, "+db.GROUPS+" gr WHERE sh.SH_GROUP_ID=gr.GROUP_ID", null)

        var str = "Список покупок\n"
        if(cursor?.moveToFirst()!!){
            do{
                Log.i("Tester", cursor.getString(cursor.getColumnIndex("NAME"))+" "+cursor.getString(cursor.getColumnIndex("SUMMA"))+" "+cursor.getString(cursor.getColumnIndex("GROUPP")))

                var activ = if(cursor.getInt(cursor.getColumnIndex("ACTIVATE"))==0) " " else "+"

                str=str+"`"+cursor.getString(cursor.getColumnIndex("NAME"))+"  `   Q:"+
                            cursor.getString(cursor.getColumnIndex("COUNTS"))+
                            "  ` P:"+cursor.getString(cursor.getColumnIndex("COST"))+
                            "  ` S:"+cursor.getString(cursor.getColumnIndex("SUMMA"))+
                            " `"+activ+" `"+cursor.getString(cursor.getColumnIndex("GROUPP"))+" `\n"
            }while (cursor.moveToNext())
        }
        str+="Пожалуйста, вот мой список :)"
        Variable.Share(str, this)
    }
}
