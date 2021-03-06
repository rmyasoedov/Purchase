package com.example.purchase.Dialogs
import android.annotation.SuppressLint
import android.app.Activity
import com.example.purchase.MainActivity

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.firebasemessager.DB
import com.example.purchase.R
import com.example.purchase.R.drawable
import com.example.purchase.Variable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.container_group.view.*
import kotlinx.android.synthetic.main.dialog_add_group.view.*
import java.lang.Exception

class Group {
    private var inputGroup: AlertDialog?= null
    private var db: DB? = null
    private var context: Context ?= null
    protected var boolDelay = false


    //Конструктор для стартового вывода
    constructor(context: Context, type: Boolean){
        this.db = DB(context)
        this.context = context
    }

    constructor(context: Context){
        this.db = DB(context)
        this.context = context
        var group = ""
        var mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_group,null)//Создание View диалога из созданного layout
        var inputField = mDialogView.nameNewGroup //создание экземпляра поля ввода группы
        var clearText = mDialogView.clearFieldInputGroup //создание экземпляра текстового сообщения

        //события для снятия видимости сообщения если происходит ввод текста
        inputField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                clearText.visibility = View.INVISIBLE
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        //Наполнение диалога данными (кнопками, сообщениями)
        inputGroup = AlertDialog.Builder(context)
            ?.setTitle("Введите наименование группы")
            ?.setView(mDialogView)
            ?.setMessage("Имя группы")
            ?.setPositiveButton("OK", null)
            ?.setNegativeButton("ОТМЕНА", null)
            ?.show()

        //создание экземпляра кнопки подтверждения ввода
        val positiveButton = inputGroup?.getButton(AlertDialog.BUTTON_POSITIVE)

        //Обработчик события подтверждения ввода названия группы
        positiveButton?.setOnClickListener(View.OnClickListener() {
            group = inputField.text.toString()
            var database = db?.writableDatabase
            var cur = database?.rawQuery("select GROUP_ID from "+db?.GROUPS+" where GROUP_NAME='"+group+"'", null)


            if(group.length==0) { //Если поле пустое, то выводим предупреждение без создания записи
                clearText.setText("Поле не должно быть пустым")
                clearText.visibility = View.VISIBLE
                cur?.close()
                database?.close()
                return@OnClickListener
            }

            if(cur?.count!! >0){
                clearText.setText("Группа с таким наименованием уже существует")
                clearText.visibility = View.VISIBLE
                cur?.close()
                database?.close()
                return@OnClickListener
            }
                //Если группу можно создавать
            database?.close()
            cur?.close()
                inputGroup?.dismiss() //закрывает окно ввода
                saveGroup(group)
        })
    }

    //Процедура сохранения новой группы
    protected fun saveGroup(groupName: String){
        val database: SQLiteDatabase = db!!.writableDatabase
        val contentValues = ContentValues()

        try{
            contentValues.put("GROUP_NAME", groupName)
            database.insert(db?.GROUPS, null, contentValues)
            database.close()
            db?.close()
        }catch (e: Exception){
            Log.i("DB", "EXP: "+e.toString())
        }

        val view = (context as Activity).findViewById<View>(R.id.listGroupsContainer) as LinearLayout
        view.removeAllViews()
        listGroups("last")
        Shopin(context!!).listShopinID()
        val scroll = (context as Activity).findViewById<ScrollView>(R.id.scrollGroup) as ScrollView
        scroll.fullScroll(ScrollView.FOCUS_DOWN)
    }

    //Вывод списка групп
    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    fun listGroups(pos: String){
        val database: SQLiteDatabase = db!!.writableDatabase
        val cursor: Cursor = database?.query(db?.GROUPS, null, null, null, null, null, null)
        var view = (context as Activity).findViewById<LinearLayout>(R.id.listGroupsContainer) as LinearLayout
        view.removeAllViews()

        if(cursor.moveToFirst()){
            do{
                val id: Int = cursor.getColumnIndex("GROUP_ID") //Достаем индекс столбца из таблицы
                val name: Int = cursor.getColumnIndex("GROUP_NAME") //Достаем индекс столбца из таблицы

                var conGroup = LayoutInflater.from(context).inflate(R.layout.container_group,null)
                var linear = conGroup.bgContainer
                linear.tag = cursor.getInt(id)
                var textName = conGroup.findViewById<TextView>(R.id.nameGroupField) as TextView
                var countShopin = conGroup.findViewById<TextView>(R.id.countsShopin) as TextView
                var bg = conGroup.findViewById<LinearLayout>(R.id.bgContainer) as LinearLayout
                textName.text = cursor.getString(name)

                var countActive = Shopin(context!!).getCountActiveGroup(cursor.getInt(id))
                if(countActive==0) countShopin.visibility = View.INVISIBLE
                countShopin.text = countActive.toString()

                val title = (context as Activity).findViewById<View>(R.id.groupNameTitle) as TextView

                //Если идет загрузка списка после старта приложения
                if(pos==Variable.firstGroup && cursor.position==0) {
                    Variable.selectGroupID = cursor.getInt(id)
                    setGroupActive(true, bg)
                    title.setText(cursor.getString(name))
                }
                //Если загрузка списка вызывается после создания новой группы
                if(pos==Variable.lastGroup && cursor.position==cursor.count-1) {
                    setGroupActive(true, bg)
                    Variable.selectGroupID = cursor.getInt(id)
                    title.setText(cursor.getString(name))
                }

                //Задать событие по нажатию на группу
                bg.setOnTouchListener(object : View.OnTouchListener{
                    var startTime: Long = 0
                    var current: Long = 0
                    var delay: Long = 700 //Время ожидания удержания кнопки для опеределения клика или удержания
                    var getName: String = cursor.getString(name)
                    var getId = cursor.getInt(id)

                    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                        when (event!!.action) {
                            MotionEvent.ACTION_DOWN -> startTime = System.currentTimeMillis() //нажатие
                            MotionEvent.ACTION_MOVE -> {
                                current = System.currentTimeMillis() - startTime
                                if(current>=delay && boolDelay==false){ //Если произошло удержание
                                    boolDelay = true //параметр, чтобы не прослушивать больше зажатие
                                    popupMenuShow(getId, p0 as LinearLayout)
                                    return true
                                }
                            }
                            //MotionEvent.ACTION_UP,  //Отпускание
                            MotionEvent.ACTION_UP -> {
                                current = System.currentTimeMillis() - startTime
                                if(current<delay || boolDelay==true){ //Если произошел клик по группе
                                    var countActive = bg.findViewById<TextView>(R.id.countsShopin) as TextView
                                    countActive.text = Shopin(context!!).getCountActiveGroup(getId).toString()
                                    clickGr(getId,getName)
                                    setGroupActive(true, bg)
                                    Shopin(context!!).setTextSum()
                                }
                            }
                        }
                        return true
                    }
                })
                view.addView(conGroup)
            }while (cursor.moveToNext())
            cursor.close()
            db?.close()
        }

        Shopin(context!!).setTextSum()
    }

    fun clickGr(id: Int, name: String){
        Variable.selectGroupID = id
        val title = (context as Activity).findViewById<View>(R.id.groupNameTitle) as TextView
        val view = (context as Activity).findViewById<View>(R.id.listGroupsContainer) as LinearLayout

        title.setText(db?.oneval(db?.GROUPS.toString(), "GROUP_NAME", "GROUP_ID="+id))

        for(i in 0 until view.childCount){
            if( view.getChildAt( i ) is LinearLayout){
                setGroupActive(false, view.getChildAt(i) as LinearLayout)
            }
        }

        val shopin = (context as Activity).findViewById<View>(R.id.shopinContainer) as LinearLayout
        shopin.removeAllViews()
        Shopin(context!!).listShopinID()
    }

    fun setGroupActive(type: Boolean, view: LinearLayout){
        var text = (view.findViewById<TextView>(R.id.nameGroupField) as TextView)
        var count = (view.findViewById<TextView>(R.id.countsShopin) as TextView)

        Log.i("Tester", "P: "+text.text.toString())
        count.visibility = if(count.text.toString()=="0") View.INVISIBLE else View.VISIBLE
        if(type==false){
                view.setBackgroundResource(0)
                text.setTextColor(ContextCompat.getColor(context!!, R.color.defaultTextColor))
                text.setTypeface(null)
                //count.visibility = View.INVISIBLE
        }else {
            view.setBackgroundResource(R.drawable.stroke_bg_group_item)
            text.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
            text.setTypeface(null, Typeface.BOLD)
            //count.visibility = View.VISIBLE
        }
    }

    fun popupMenuShow(groupID: Int, view: LinearLayout){
        val menuItem = arrayOf(
            "Изменить",
            "Удалить группу",
            "Удалить отмеченные",
            "Поделиться списком",
            "Поделиться простым списком",
            "Отмена")

        var builder: AlertDialog
        builder = AlertDialog.Builder(this.context!!)
            ?.setTitle("Выберите режим("+
                    db?.oneval(db?.GROUPS.toString(),"GROUP_NAME", "GROUP_ID="+groupID)+")")
            ?.setItems(menuItem){dialog, which ->

                when(menuItem[which]){
                    "Изменить"->{this.editGroupName(groupID, view)}
                    "Удалить группу"->{this.deleteGroup(groupID)}
                    "Удалить отмеченные"->{Variable.deleteCheckedShopin(context!!)}
                    "Поделиться списком"->{shareEasyList(true, groupID)}
                    "Поделиться простым списком"->{clickShare(true, groupID)}
                    "Отмена"->{}
                }
            }!!.show()


        builder.setOnDismissListener {
            this.boolDelay = false
        }
    }

    fun deleteGroup(groupID: Int){
        var database: SQLiteDatabase = db!!.writableDatabase
        var cursor = database?.rawQuery("SELECT GROUP_NAME FROM "+ db?.GROUPS+" WHERE GROUP_ID="+groupID, null)
        cursor.moveToFirst()


        var msgDialog: AlertDialog
        msgDialog = AlertDialog.Builder(context!!)
            ?.setTitle("Удалить группу "+cursor.getString(cursor.getColumnIndex("GROUP_NAME")))
            ?.setMessage("Подтверждаете удаление?")
            ?.setPositiveButton("ДА - УДАЛИТЬ", null)
            ?.setNegativeButton("ОТМЕНА", null)
            ?.show()!!

        val positiveButton = msgDialog?.getButton(AlertDialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener(View.OnClickListener {
            var database: SQLiteDatabase = db!!.writableDatabase
            database.delete(db?.GROUPS, "GROUP_ID="+groupID, null)
            listGroups(Variable.firstGroup)
            Shopin(context!!).listShopinID()
            database.close()
            msgDialog.dismiss()
        })
        database.close()
        cursor.close()
    }


    fun editGroupName(groupID: Int, view: LinearLayout){
        var mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_group,null)
        var text = mDialogView.nameNewGroup
        var clear = mDialogView.clearFieldInputGroup
        var database: SQLiteDatabase = db!!.writableDatabase
        var cursor = database.rawQuery("SELECT GROUP_NAME FROM "+db?.GROUPS+" WHERE GROUP_ID="+groupID, null)
        cursor.moveToFirst()
        text.setText(cursor.getString(cursor.getColumnIndex("GROUP_NAME")))
        var builder: AlertDialog

        builder = AlertDialog.Builder(context!!)
            ?.setTitle("Введите наименование группы")
            ?.setMessage("Имя группы")
            ?.setView(mDialogView)
            ?.setPositiveButton("ОК", null)
            ?.setNegativeButton("ОТМЕНА", null)
            ?.show()!!

        cursor.close()


        var okButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)

        val contentValues = ContentValues()

        okButton.setOnClickListener(View.OnClickListener {
            var newName = text.text.toString()

            if(newName.length==0){
                clear.setText("Поле не должно быть пустым")
                clear.visibility = View.VISIBLE
            } else{
                contentValues.clear()
                contentValues.put("GROUP_NAME", newName)
                try{
                    database.update(db?.GROUPS, contentValues, "GROUP_ID="+groupID, null)
                }catch (e: SQLiteConstraintException){
                    clear.setText("Группа с таким наименованием уже существует")
                    clear.visibility = View.VISIBLE
                    return@OnClickListener
                }
                val groupNameField = view.findViewById<TextView>(R.id.nameGroupField) as TextView
                groupNameField.setText(newName)
                this.clickGr(groupID, newName)
                setGroupActive(true, view)
                builder.dismiss()
            }

        })

        clear.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                clear.visibility = View.INVISIBLE
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        builder.setOnDismissListener {
            database.close()
            db?.close()
        }
    }

    fun deleteAllGroups(){
        var database = db?.writableDatabase
        database?.delete(db?.GROUPS,null, null)
        database?.close()
        var view = (context as Activity).findViewById<LinearLayout>(R.id.listGroupsContainer) as LinearLayout
        view.removeAllViews()

        var listShoppin = (context as Activity).findViewById<LinearLayout>(R.id.shopinContainer) as LinearLayout
        listShoppin.removeAllViews()

        ((context as Activity).findViewById<View>(R.id.groupNameTitle) as TextView).setText("Список покупок")

        var blockPayments = (context as Activity).findViewById<LinearLayout>(R.id.blockSumm) as LinearLayout
        (blockPayments.findViewById<TextView>(R.id.sumBuyActive) as TextView).setText("0.00")
        (blockPayments.findViewById<TextView>(R.id.sumBuy) as TextView).setText("0.00")
    }


    //Поделиться списком
    fun clickShare(group: Boolean, groupID: Int){

        var where = if(group==false) "" else " WHERE SH_GROUP_ID="+groupID+" "

        var database = db?.writableDatabase
        var cursor = database?.rawQuery("select *from "+db?.SHOPIN+where+" order by SH_GROUP_ID, SH_NAME", null)

        if(cursor?.count==0) return

        var str = "мой список\n"
        cursor?.moveToFirst()!!
        var idGr = cursor?.getInt(cursor.getColumnIndex("SH_GROUP_ID"))
        if(cursor?.moveToFirst()!!){
            do{
                if(cursor?.getInt(cursor.getColumnIndex("SH_GROUP_ID"))!=idGr){
                    idGr = cursor?.getInt(cursor.getColumnIndex("SH_GROUP_ID"))
                    str= str+"\n"
                }

                val cost = if(cursor.getFloat(cursor.getColumnIndex("SH_COST"))==0F) "" else "   Ц: "+
                        cursor.getFloat(cursor.getColumnIndex("SH_COST"))+"="

                str = str+" "+cursor.getString(cursor.getColumnIndex("SH_NAME"))+cost+"\n"
            }while (cursor?.moveToNext())
        }
        str+="Пожалуйста, вот мой список :)"
        Variable.Share(str, context!!)
        cursor.close()
        database?.close()
    }

    //Поделиться простым списком
    fun shareEasyList(group: Boolean, groupID: Int){
        var where  = if(group==false) "" else " AND sh.SH_GROUP_ID="+groupID

        var database = db?.writableDatabase
        var cursor = database?.rawQuery("select sh.SH_NAME as NAME, " +
                "                                           sh.SH_COUNTS as COUNTS, " +
                "                                           sh.SH_COST as COST, " +
                "                                           (sh.SH_COST*sh.SH_COUNTS) AS SUMMA, " +
                "                                           SH_ACTIVATE as ACTIVATE," +
                "                                            gr.GROUP_NAME AS GROUPP " +
                "                                           FROM "+db?.SHOPIN+" sh, "+db?.GROUPS+" gr WHERE sh.SH_GROUP_ID=gr.GROUP_ID"+where, null)

        if(cursor?.count==0) return

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
        Variable.Share(str, context!!)
    }
}
