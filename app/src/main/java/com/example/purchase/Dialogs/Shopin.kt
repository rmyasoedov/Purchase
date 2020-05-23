package com.example.purchase.Dialogs

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.firebasemessager.DB
import com.example.purchase.R
import com.example.purchase.Variable
import kotlinx.android.synthetic.main.container_shopin.view.*
import kotlinx.android.synthetic.main.dialog_add_shopin.view.*

class Shopin{
    protected var context: Context ?= null
    protected var inputShopin: AlertDialog?= null
    protected var db: DB? = null


    constructor(context: Context){
        this.context = context
        this.db = DB(context)
    }

    //=================================================================================

    fun addShopin(){
        var mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_shopin,null)

        inputShopin = AlertDialog.Builder(context!!)
            ?.setView(mDialogView)
            ?.show()

        var cancel = inputShopin?.findViewById<Button>(R.id.cancelShopinForm) as Button
        var okButton = inputShopin?.findViewById<Button>(R.id.okShopinForm) as Button

        var nameText = mDialogView.nameShopinText
        var countText = mDialogView.countShopinText
        var costText = mDialogView.costShopinText

        cancel.setOnClickListener(View.OnClickListener {
            inputShopin?.dismiss()
        })

        //Подтверждение покупки
        okButton.setOnClickListener(View.OnClickListener {
            val name: String = nameText.text.toString()
            val count: Int = if(countText.text.toString().toInt()==null) 0 else countText.text.toString().toInt()
            val cost: Float = if(costText.text.toString().toFloat()==null) 0F else costText.text.toString().toFloat()
            val id = Variable.selectGroupID

            val database: SQLiteDatabase = db!!.writableDatabase
            val contentValues = ContentValues()

            contentValues.put("SH_NAME", name)
            contentValues.put("SH_COUNTS", count)
            contentValues.put("SH_COST", cost)
            contentValues.put("SH_GROUP_ID", id)
            database.insert(db?.SHOPIN, null, contentValues)
            db?.close()
            inputShopin?.dismiss()
            listShopinID()
        })
    }

    //=====================================================================================

    fun listShopinID(){
        val id = Variable.selectGroupID
        val database: SQLiteDatabase = db!!.writableDatabase
        //--------------------------------
        val cur = database?.rawQuery("SELECT *FROM "+db?.SHOPIN, null)
        if(cur.moveToNext()){
            do{
                Log.i("DB", cur.getString(cur.getColumnIndex("SH_NAME")))
            }while (cur.moveToNext())
        }


        Log.i("Tester", "Counts: "+cur.count)
        cur.close()
        //---------------------------
        val cursor: Cursor = database?.query(db?.SHOPIN, null, "SH_GROUP_ID="+id.toString(), null, null, null, "SH_ACTIVATE")

        val view = (context as Activity).findViewById<View>(R.id.shopinContainer) as LinearLayout
        view.removeAllViews()

        if(cursor.moveToFirst()){
            do {
                val shID = cursor.getInt(cursor.getColumnIndex("SH_ID"))
                val shGroupID = cursor.getInt(cursor.getColumnIndex("SH_GROUP_ID"))
                val shCount = cursor.getInt(cursor.getColumnIndex("SH_COUNTS"))
                val shCost = cursor.getFloat(cursor.getColumnIndex("SH_COST"))
                val shName = cursor.getString(cursor.getColumnIndex("SH_NAME"))
                val shAct = cursor.getInt(cursor.getColumnIndex("SH_ACTIVATE"))

                //Log.i("Tester","Id: "+shID.toString()+"; Name: "+shName+"; Count: "+shCount.toString()+"; cost: "+shCost.toString()+"; grID: "+shGroupID.toString()+"; Act: "+shAct.toString())

                //создать блок-контейнер для добавления в список покупок
                var contShopin = LayoutInflater.from(context).inflate(R.layout.container_shopin,null)

                val nameShopin = contShopin.nameShopin
                val paramShopin = contShopin.paramShopin
                val row = contShopin.shopinRow
                val check = contShopin.activeShopin
                val editShopin = contShopin.editShopin

                check.isChecked = if(shAct==0) false else true
                nameShopin.setText(shName)
                paramShopin.setText("Ц: "+shCost+" К: "+shCount+" Сум: "+(shCost*shCount))

                //Нажатие на кнопку редактирования покупки
                editShopin.setOnClickListener(View.OnClickListener {
                    this.editShopinDialog(shID)
                })


                //Обработчик нажатия строки покупки------------------------------------------------------
                check.setOnClickListener(View.OnClickListener {row.performClick()})

                row.setOnClickListener(View.OnClickListener {
                    val database: SQLiteDatabase = db!!.writableDatabase
                    val act = database?.rawQuery("SELECT *FROM "+db?.SHOPIN+" WHERE SH_ID="+shID, null)
                    val check = row.findViewById<CheckBox>(R.id.activeShopin) as CheckBox
                    act.moveToFirst()
                    val actId = act.getInt(act.getColumnIndex("SH_ACTIVATE"))
                    act.close()

                    var newAct = 0

                    if(actId==0){
                        newAct = 1
                        check.isChecked = true
                    } else{
                        newAct = 0
                        check.isChecked = false
                    }

                    val contentValues = ContentValues()
                    contentValues.put("SH_ACTIVATE", newAct)
                    database?.update(db?.SHOPIN, contentValues,"SH_ID="+shID,null)

                    var grBlock = (context as Activity).findViewById<LinearLayout>(R.id.listGroupsContainer) as LinearLayout
                    for(i in 0 until grBlock.childCount){
                        val text = (grBlock.getChildAt(i) as LinearLayout).findViewById<TextView>(R.id.countsShopin) as TextView

                        if(text.visibility==View.VISIBLE)
                            text.text = getCountActiveGroup(Variable.selectGroupID!!).toString()
                    }
                    Shopin(context!!).setTextSum()
                    database.close()
                })
                //--------------------------------------------------------------------------------------

                view.addView(contShopin)

            }while (cursor.moveToNext())
            cursor.close()
            db?.close()
            Shopin(context!!).setTextSum()
        }
    }
    //==============================================================================================

    //Функция для получения количества отмеченных покупок активной группы
    fun getCountActiveGroup(groupID: Int): Int{
        val database: SQLiteDatabase = db!!.writableDatabase
        var cur = database?.rawQuery("SELECT SH_ID FROM "+db?.SHOPIN+" WHERE SH_GROUP_ID="+
                groupID+" AND SH_ACTIVATE=0", null)
        var id = cur.count
        cur.close()
        return id
    }

    //Функция для получения суммы всех покупок активной группы
    fun getSumGroup(groupID: Int): Float{
        val database: SQLiteDatabase = db!!.writableDatabase
        var cursor = database?.rawQuery("SELECT SUM(SH_COUNTS*SH_COST) AS sumGr FROM "+db?.SHOPIN+" WHERE SH_GROUP_ID="+
                groupID, null)
        cursor.moveToFirst()
        var f = cursor.getFloat(cursor.getColumnIndex("sumGr"))
        cursor.close()
        return f
    }

    //Функция для получения суммы всех отмеченных покупок активной группы
    fun getSumActiveGroup(groupID: Int): Float{
        val database: SQLiteDatabase = db!!.writableDatabase
        var cursor = database?.rawQuery("SELECT SUM(SH_COUNTS*SH_COST) AS sumGr FROM "+
                db?.SHOPIN+" WHERE SH_GROUP_ID="+groupID+" AND SH_ACTIVATE=1", null)
        cursor.moveToFirst()
        var f = cursor.getFloat(cursor.getColumnIndex("sumGr"))
        cursor.close()
        database.close()
        return f
    }

    //Функция для обновления данных по суммам покупок активной группы
    fun setTextSum(){
        val database: SQLiteDatabase = db!!.writableDatabase
        var cursor = database?.rawQuery("SELECT GROUP_ID FROM "+db?.GROUPS,null)
        if(cursor.count==0) return

        var blockPayments = (context as Activity).findViewById<LinearLayout>(R.id.blockSumm) as LinearLayout
        var sumActive = blockPayments.findViewById<TextView>(R.id.sumBuyActive) as TextView
        var sumGroup = blockPayments.findViewById<TextView>(R.id.sumBuy) as TextView
        sumActive.text = this.getSumActiveGroup(Variable.selectGroupID!!).toString()
        sumGroup.text = this.getSumGroup(Variable.selectGroupID!!).toString()
    }

    //Удалить отмеченные покупки выделенной группы
    fun deleteActiveShopin(id: Int){
        val database: SQLiteDatabase = db!!.writableDatabase
        database?.delete(db?.SHOPIN,"SH_GROUP_ID="+Variable.selectGroupID+" AND SH_ACTIVATE=1", null)
        database.close()
    }

    //Удалить покупку
    fun deleteShopinID(id: Int){
        val database: SQLiteDatabase = db!!.writableDatabase
        database?.delete(db?.SHOPIN,"SH_ID="+id, null)
        database.close()
    }

    //Редактирование покупки
    fun editShopinDialog(id: Int){
        val database: SQLiteDatabase = db!!.writableDatabase
        var cursor = database?.rawQuery("SELECT *FROM "+db?.SHOPIN+" WHERE SH_ID="+id, null)
        cursor.moveToFirst()

        var editShopin: AlertDialog
        var mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_shopin,null)

        var deleteButton = mDialogView.deleteShopin
        var cancelButton = mDialogView.cancelShopinForm
        var okButton = mDialogView.okShopinForm
        var nameText = mDialogView.nameShopinText
        var countText = mDialogView.countShopinText
        var costText = mDialogView.costShopinText

        deleteButton.visibility = View.VISIBLE
        nameText.setText(cursor.getString(cursor.getColumnIndex("SH_NAME")))
        countText.setText(cursor.getInt(cursor.getColumnIndex("SH_COUNTS")).toString())
        costText.setText(cursor.getFloat(cursor.getColumnIndex("SH_COST")).toString())

        editShopin = AlertDialog.Builder(this.context!!)
            ?.setView(mDialogView)
            ?.show()!!


        okButton.setOnClickListener(View.OnClickListener {
            val database: SQLiteDatabase = db!!.writableDatabase
            val contentValues = ContentValues()
            contentValues.put("SH_NAME", nameText.text.toString())
            contentValues.put("SH_COUNTS", countText.text.toString())
            contentValues.put("SH_COST", costText.text.toString())
            database?.update(db?.SHOPIN,contentValues, "SH_ID="+id, null)
            editShopin.dismiss()
            listShopinID()
        })

        deleteButton.setOnClickListener(View.OnClickListener {
            deleteShopinID(id)
            listShopinID()
            editShopin.dismiss()
        })

        cancelButton.setOnClickListener(View.OnClickListener {
            editShopin.dismiss()
        })

        cursor.close()
        database.close()
    }
}