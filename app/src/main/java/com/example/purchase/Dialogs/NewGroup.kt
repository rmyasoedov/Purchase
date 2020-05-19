package com.example.purchase.Dialogs

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.purchase.R
import kotlinx.android.synthetic.main.dialog_add_group.view.*

class NewGroup {
    private var inputGroup: AlertDialog?= null


    constructor(context: Context){
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
            if(group.length==0){
                clearText.visibility = View.VISIBLE

            } else {
                inputGroup?.dismiss()
            }
        })
    }
}