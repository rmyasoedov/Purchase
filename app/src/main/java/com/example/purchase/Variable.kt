package com.example.purchase

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.example.firebasemessager.DB
import com.example.purchase.Dialogs.Group
import com.example.purchase.Dialogs.Shopin

object Variable {
    var selectGroupID: Int ?= null
    var lastGroup = "last"
    var firstGroup = "first"

    fun deleteCheckedShopin(context: Context){
        var msgDialog: AlertDialog
        msgDialog = AlertDialog.Builder(context)
            ?.setTitle("Удаление отмеченных")
            ?.setMessage("Помеченные покупки будут удалены")
            ?.setPositiveButton("ДА - УДАЛИТЬ", null)
            ?.setNegativeButton("ОТМЕНА", null)
            ?.show()!!

        val positiveButton = msgDialog?.getButton(AlertDialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener(View.OnClickListener {
            Shopin(context).deleteActiveShopin(this.selectGroupID!!)
            Shopin(context).listShopinID()
            msgDialog.dismiss()
        })
    }

    fun Share(str: String, context: Context){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, str)
        sendIntent.type = "text/plain"
        context.startActivity(Intent.createChooser(sendIntent, "Поделиться"))
    }

    fun getListAnnotShoppin(context: Context): ArrayList<ShoppinList>{
        var list: ArrayList<ShoppinList> ?= ArrayList()
        var db = DB(context)

        var database = db?.writableDatabase
        var cursor = database.rawQuery("SELECT *from "+db?.ANNOT+" ORDER BY ANNOT_NAME", null)

        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(cursor.getColumnIndex("ANNOT_NAME"))
                val cost = cursor.getString(cursor.getColumnIndex("ANNOT_COST"))
                list?.add(ShoppinList(name,cost))
            }while (cursor.moveToNext())
        }
        cursor.close()
        database.close()

        return list!!
    }
}