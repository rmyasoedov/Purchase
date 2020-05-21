package com.example.purchase.Dialogs
import android.annotation.SuppressLint
import android.app.Activity
import com.example.purchase.MainActivity

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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
import kotlinx.android.synthetic.main.dialog_add_group.view.*

class Group {
    private var inputGroup: AlertDialog?= null
    private var db: DB? = null
    private var context: Context ?= null


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

            if(group.length==0){ //Если поле пустое, то выводим предупреждение без создания записи
                clearText.visibility = View.VISIBLE

            } else {//Если группу можно создавать

                inputGroup?.dismiss() //закрывает окно ввода
                saveGroup(group)
            }
        })
    }

    //Процедура сохранения новой группы
    protected fun saveGroup(groupName: String){
        val database: SQLiteDatabase = db!!.writableDatabase
        val contentValues = ContentValues()

        Log.i("Tester","добавляем: "+groupName)
        contentValues.put("GROUP_NAME", groupName)
        database.insert(db?.GROUPS, null, contentValues)
        db?.close()

        val view = (context as Activity).findViewById<View>(R.id.listGroupsContainer) as LinearLayout
        view.removeAllViews()
        listGroups(view, "last")
        Shopin(context!!).listShopinID()
        val scroll = (context as Activity).findViewById<ScrollView>(R.id.scrollGroup) as ScrollView
        scroll.fullScroll(ScrollView.FOCUS_DOWN)
    }

    //Вывод списка групп
    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    fun listGroups(view: LinearLayout, pos: String){
        val database: SQLiteDatabase = db!!.writableDatabase
        val cursor: Cursor = database?.query(db?.GROUPS, null, null, null, null, null, null)

        if(cursor.moveToFirst()){
            do{

                val id: Int = cursor.getColumnIndex("GROUP_ID") //Достаем индекс столбца из таблицы
                val name: Int = cursor.getColumnIndex("GROUP_NAME") //Достаем индекс столбца из таблицы

                var conGroup = LayoutInflater.from(context).inflate(R.layout.container_group,null)
                var textName = conGroup.findViewById<TextView>(R.id.nameGroupField) as TextView
                var countShopin = conGroup.findViewById<TextView>(R.id.countsShopin) as TextView
                var bg = conGroup.findViewById<LinearLayout>(R.id.bgContainer) as LinearLayout
                textName.text = cursor.getString(name)

                countShopin.text = Shopin(context!!).getCountActiveGroup(cursor.getInt(id)).toString()

                val title = (context as Activity).findViewById<View>(R.id.groupNameTitle) as TextView
                //Если идет загрузка списка после старта приложения
                if(pos=="first" && cursor.position==0) {
                    Variable.selectGroupID = cursor.getInt(id)
                    setGroupActive(true, bg)
                    title.setText(cursor.getString(name))
                }
                //Если загрузка списка вызывается после создания новой группы
                if(pos=="last" && cursor.position==cursor.count-1) {
                    setGroupActive(true, bg)
                    Variable.selectGroupID = cursor.getInt(id)
                    title.setText(cursor.getString(name))
                }

                //Задать событие по нажатию на группу
                bg.setOnTouchListener(object : View.OnTouchListener{
                    var startTime: Long = 0
                    var current: Long = 0
                    var delay: Long = 800 //Время ожидания удержания кнопки для опеределения клика или удержания
                    var getName: String = cursor.getString(name)
                    var getId = cursor.getInt(id)

                    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                        when (event!!.action) {
                            MotionEvent.ACTION_DOWN -> startTime = System.currentTimeMillis() //нажатие
                            MotionEvent.ACTION_MOVE -> {
                                current = System.currentTimeMillis() - startTime
                                if(current>=delay){ //Если произошло удержание
                                    Log.i("Tester", "Это удержание")
                                    return true
                                }
                            }
                            //MotionEvent.ACTION_UP,  //Отпускание
                            MotionEvent.ACTION_UP -> {
                                current = System.currentTimeMillis() - startTime
                                if(current<delay){ //Если произошел клик по группе
                                    var countActive = bg.findViewById<TextView>(R.id.countsShopin) as TextView
                                    countActive.text = Shopin(context!!).getCountActiveGroup(getId).toString()
                                    clickGr(getId,getName)
                                    setGroupActive(true, bg)
                                }
                            }
                        }
                        return true
                    }
                })

                view.addView(conGroup)
            }while (cursor.moveToNext())
            db?.close()
        }
    }

    fun clickGr(id: Int, name: String){
        Variable.selectGroupID = id
        val title = (context as Activity).findViewById<View>(R.id.groupNameTitle) as TextView
        val view = (context as Activity).findViewById<View>(R.id.listGroupsContainer) as LinearLayout
        title.setText(name)

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
        if(type==false){
                view.setBackgroundResource(0)
                text.setTextColor(ContextCompat.getColor(context!!, R.color.defaultTextColor))
                text.setTypeface(null)
                count.visibility = View.INVISIBLE
        }else {
            view.setBackgroundResource(R.drawable.stroke_bg_group_item)
            text.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
            text.setTypeface(null, Typeface.BOLD)
            count.visibility = View.VISIBLE
        }
    }

}
