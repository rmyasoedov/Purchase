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

                Log.i("Tester","Id: "+shID.toString()+"; Name: "+shName+"; Count: "+shCount.toString()+"; cost: "+shCost.toString()+"; grID: "+shGroupID.toString()+"; Act: "+shAct.toString())

                var contShopin = LayoutInflater.from(context).inflate(R.layout.container_shopin,null)

                val nameShopin = contShopin.nameShopin
                val paramShopin = contShopin.paramShopin
                val row = contShopin.shopinRow
                val check = contShopin.activeShopin

                check.isChecked = if(shAct==0) false else true
                nameShopin.setText(shName)
                paramShopin.setText("Ц: "+shCost+" К: "+shCount+" Сум: "+(shCost*shCount))


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
                })
                //--------------------------------------------------------------------------------------

                view.addView(contShopin)

            }while (cursor.moveToNext())
            cursor.close()
            db?.close()
        }
    }
    //==============================================================================================

    fun getCountActiveGroup(groupID: Int): Int{
        val database: SQLiteDatabase = db!!.writableDatabase
        var cur = database?.rawQuery("SELECT SH_ID FROM "+db?.SHOPIN+" WHERE SH_GROUP_ID="+
                groupID+" AND SH_ACTIVATE=0", null)
        var id = cur.count
        cur.close()
        return id
    }
}