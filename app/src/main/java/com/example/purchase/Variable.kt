package com.example.purchase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.View
import androidx.appcompat.app.AlertDialog
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

}