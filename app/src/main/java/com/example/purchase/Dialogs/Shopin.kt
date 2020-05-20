package com.example.purchase.Dialogs

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.firebasemessager.DB
import com.example.purchase.R
import com.example.purchase.Variable
import kotlinx.android.synthetic.main.dialog_add_shopin.view.*

class Shopin{
    protected var context: Context ?= null
    protected var inputShopin: AlertDialog?= null
    protected var db: DB? = null


    constructor(context: Context){
        this.context = context
        this.db = DB(context)
    }

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
        })
    }

    fun listShopinID(){
        val id = Variable.selectGroupID
        val database: SQLiteDatabase = db!!.writableDatabase
        val cursor: Cursor = database?.query(db?.SHOPIN, null, null, null, null, null, null)

        if(cursor.moveToFirst()){
            do {
                val shID = cursor.getInt(cursor.getColumnIndex("SH_ID"))
                val shGroupID = cursor.getInt(cursor.getColumnIndex("SH_GROUP_ID"))
                val shCount = cursor.getInt(cursor.getColumnIndex("SH_COUNTS"))
                val shCost = cursor.getFloat(cursor.getColumnIndex("SH_COST"))
                val shName = cursor.getString(cursor.getColumnIndex("SH_NAME"))

                Log.i("Tester","Id: "+shID.toString()+"; Name: "+shName+"; Count: "+shCount.toString()+"; cost: "+shCost.toString()+"; grID: "+shGroupID.toString())
            }while (cursor.moveToNext())

        }
    }
}