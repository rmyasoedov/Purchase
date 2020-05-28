package com.example.purchase.Dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.firebasemessager.DB
import com.example.purchase.R
import com.example.purchase.ShoppinAdapter
import com.example.purchase.Variable
import kotlinx.android.synthetic.main.activity_main.*
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
        var database = db?.writableDatabase
        var cur = database?.rawQuery("select GROUP_ID from "+db?.GROUPS, null)
        if(cur?.count==0){
            val toast = Toast.makeText(context,"Создайте группу товаров",Toast.LENGTH_SHORT)
            toast.show()
            return
        }
        var mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_shopin,null)

        inputShopin = AlertDialog.Builder(context!!)
            ?.setView(mDialogView)
            ?.show()

        var cancel = inputShopin?.findViewById<Button>(R.id.cancelShopinForm) as Button
        var okButton = inputShopin?.findViewById<Button>(R.id.okShopinForm) as Button

        var nameText = mDialogView.nameShopinText
        var countText = mDialogView.countShopinText
        var costText = mDialogView.costShopinText
        var label = mDialogView.clearFieldInputShoppin

        nameText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                label.visibility = View.INVISIBLE
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        var adapter = ShoppinAdapter(context!!, R.layout.autocomplete, Variable.getListAnnotShoppin(context!!))
        nameText.setAdapter(adapter)

        nameText.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, position, id ->
                var cost = (view.findViewById<TextView>(R.id.annotCost) as TextView).text.toString()
                costText.setText(cost)
            }


        cancel.setOnClickListener(View.OnClickListener {
            inputShopin?.dismiss()
        })

        //Подтверждение покупки
        okButton.setOnClickListener(View.OnClickListener {
            val name: String = nameText.text.toString()
            val count: Float = if(countText.text.toString().length==0) 0F else countText.text.toString().toFloat()
            val cost: Float = if(costText.text.toString().length==0) 0F else costText.text.toString().toFloat()
            val id = Variable.selectGroupID

            if(name.length==0){
                label.visibility = View.VISIBLE
                return@OnClickListener
            }

            val database: SQLiteDatabase = db!!.writableDatabase
            val contentValues = ContentValues()

            contentValues.put("SH_NAME", name)
            contentValues.put("SH_COUNTS", count)
            contentValues.put("SH_COST", cost)
            contentValues.put("SH_GROUP_ID", id)
            database.insert(db?.SHOPIN, null, contentValues)
            db?.close()
            inputShopin?.dismiss()
            updateCountsInGroup()
            listShopinID()
        })
    }

    //=====================================================================================

    @SuppressLint("ResourceAsColor")
    fun listShopinID(){
        val id = Variable.selectGroupID
        val database: SQLiteDatabase = db!!.writableDatabase
        //--------------------------------
        val cur = database?.rawQuery("SELECT *FROM "+db?.SHOPIN, null)
        if(cur.moveToNext()){
            do{
                //Log.i("DB", cur.getString(cur.getColumnIndex("SH_NAME")))
            }while (cur.moveToNext())
        }


        //Log.i("Tester", "Counts: "+cur.count)
        cur.close()
        //---------------------------
        val cursor: Cursor = database?.query(db?.SHOPIN, null, "SH_GROUP_ID="+id.toString(), null, null, null, "SH_ACTIVATE")

        val view = (context as Activity).findViewById<View>(R.id.shopinContainer) as LinearLayout
        view.removeAllViews()

        if(cursor.moveToFirst()){
            do {
                val shID = cursor.getInt(cursor.getColumnIndex("SH_ID"))
                val shGroupID = cursor.getInt(cursor.getColumnIndex("SH_GROUP_ID"))
                val shCount = cursor.getFloat(cursor.getColumnIndex("SH_COUNTS"))
                val shCost = cursor.getFloat(cursor.getColumnIndex("SH_COST"))
                val shName = cursor.getString(cursor.getColumnIndex("SH_NAME"))
                val shAct = cursor.getInt(cursor.getColumnIndex("SH_ACTIVATE"))

                //Log.i("Tester","Id: "+shID.toString()+"; Name: "+shName+"; Count: "+shCount.toString()+"; cost: "+shCost.toString()+"; grID: "+shGroupID.toString()+"; Act: "+shAct.toString())

                //создать блок-контейнер для добавления в список покупок
                var contShopin = LayoutInflater.from(context).inflate(R.layout.container_shopin,null)

                val nameShopin = contShopin.nameShopin
                val paramCostShoppin = contShopin.parapCostShoppin.setText("Ц: "+shCost)
                val paramCountShoppin = contShopin.paramCountShoppin.setText("К: "+shCount)
                val paramSumShopin = contShopin.paramSumShoppin.setText("Сум: "+(shCost*shCount))
                val row = contShopin.shopinRow
                val check = contShopin.activeShopin
                val editShopin = contShopin.editShopin

                //check.isChecked = if(shAct==0) false else true

                if(shAct==0){
                    nameShopin.setTextColor(Color.BLACK)
                    check.isChecked = false
                }else{
                    nameShopin.setTextColor(Color.GRAY)
                    check.isChecked = true
                }

                nameShopin.setText(shName)

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
                    val nameShoppin = row.findViewById<TextView>(R.id.nameShopin) as TextView

                    act.moveToFirst()
                    val actId = act.getInt(act.getColumnIndex("SH_ACTIVATE"))
                    act.close()

                    var newAct = 0

                    if(actId==0){
                        nameShopin.setTextColor(Color.GRAY)
                        newAct = 1
                        check.isChecked = true
                    } else{
                        nameShopin.setTextColor(Color.BLACK)
                        newAct = 0
                        check.isChecked = false
                    }

                    nameShoppin.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))

                    val contentValues = ContentValues()
                    contentValues.put("SH_ACTIVATE", newAct)
                    database?.update(db?.SHOPIN, contentValues,"SH_ID="+shID,null)

                    updateCountsInGroup()

                    setTextSum()
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

    //Обновление информации о количестве не отмеченных покупок в правом блоке
    fun updateCountsInGroup(){
        var grBlock = (context as Activity).findViewById<LinearLayout>(R.id.listGroupsContainer) as LinearLayout
        for(i in 0 until grBlock.childCount){
            val text = (grBlock.getChildAt(i) as LinearLayout).findViewById<TextView>(R.id.countsShopin) as TextView

            var tagCount = getCountActiveGroup((grBlock.getChildAt(i) as LinearLayout).tag as Int)
            text.text = tagCount.toString()
            text.visibility= if(tagCount==0) View.INVISIBLE else View.VISIBLE
        }
    }

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
        updateCountsInGroup()
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
        var label = mDialogView.clearFieldInputShoppin

        nameText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                label.visibility = View.INVISIBLE
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        var adapter = ShoppinAdapter(context!!, R.layout.autocomplete, Variable.getListAnnotShoppin(context!!))
        nameText.setAdapter(adapter)

        nameText.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, position, id ->
                var cost = (view.findViewById<TextView>(R.id.annotCost) as TextView).text.toString()
                costText.setText(cost)
            }

        deleteButton.visibility = View.VISIBLE
        nameText.setText(cursor.getString(cursor.getColumnIndex("SH_NAME")))
        countText.setText(cursor.getFloat(cursor.getColumnIndex("SH_COUNTS")).toString())
        costText.setText(cursor.getFloat(cursor.getColumnIndex("SH_COST")).toString())

        editShopin = AlertDialog.Builder(this.context!!)
            ?.setView(mDialogView)
            ?.show()!!


        okButton.setOnClickListener(View.OnClickListener {
            val database: SQLiteDatabase = db!!.writableDatabase
            val contentValues = ContentValues()

            if(nameText.text.toString().length==0){
                label.visibility = View.VISIBLE
                return@OnClickListener
            }

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