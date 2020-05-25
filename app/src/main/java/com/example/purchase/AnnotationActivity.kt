package com.example.purchase

import android.content.ContentValues
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.firebasemessager.DB
import kotlinx.android.synthetic.main.activity_annotation.*
import kotlinx.android.synthetic.main.container_anotation.view.*
import kotlinx.android.synthetic.main.dialog_annotations_edit.view.*

class AnnotationActivity : AppCompatActivity() {

    protected var db: DB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annotation)

        db = DB(this)
        listAnnotationsView()

        clickClearAll.setOnClickListener(View.OnClickListener {
            clearAllAnnotations()
        })
    }


    protected fun listAnnotationsView(){
        var database = db?.writableDatabase
        var cursor = database?.rawQuery("SELECT *FROM "+db?.ANNOT+" ORDER BY ANNOT_NAME", null)
        listAnotation.removeAllViews()

        if(cursor?.moveToFirst()!!){
            do{
                var containerAnnot = LayoutInflater.from(this).inflate(R.layout.container_anotation,null)

                containerAnnot.annot_name.setText(cursor.getString(cursor.getColumnIndex("ANNOT_NAME")))
                containerAnnot.annot_cost.setText(cursor.getFloat(cursor.getColumnIndex("ANNOT_COST")).toString())
                var id = cursor.getInt(cursor.getColumnIndex("ANNOT_ID"))
                var edit = containerAnnot.editClick

                edit.setOnClickListener(View.OnClickListener {
                    editAnnotationsDialog(id, containerAnnot as LinearLayout)
                })

                listAnotation.addView(containerAnnot)
            }while(cursor.moveToNext())
        }

        cursor?.close()
        database?.close()
    }

    protected fun editAnnotationsDialog(annotID: Int, view: LinearLayout){
        Log.i("Tester", "Edit ID: "+annotID)
        var database = db?.writableDatabase
        var cursor = database?.rawQuery("SELECT *FROM "+db?.ANNOT+" WHERE ANNOT_ID="+annotID, null)
        cursor?.moveToFirst()
        var dialogAnnot = LayoutInflater.from(this).inflate(R.layout.dialog_annotations_edit,null)

        var textName = dialogAnnot.editName
        var costText = dialogAnnot.editCost

        var nameAnnot: TextView = view.findViewById(R.id.annot_name) as TextView
        var costAnnot: TextView = view.findViewById(R.id.annot_cost) as TextView

        textName.setText(cursor?.getString(cursor?.getColumnIndex("ANNOT_NAME")))
        costText.setText(cursor?.getFloat(cursor.getColumnIndex("ANNOT_COST")).toString())

        //var builder: AlertDialog
        var builder = AlertDialog.Builder(this)
                    ?.setView(dialogAnnot)
                    ?.show()

        var cancelButton = dialogAnnot.cancelAnnot
        var okButton = dialogAnnot.okAnnot

        cancelButton.setOnClickListener(View.OnClickListener {
            builder?.dismiss()
        })

        var contentValues = ContentValues()
        okButton.setOnClickListener(View.OnClickListener {
            contentValues.clear()
            contentValues.put("ANNOT_NAME", textName.text.toString())
            contentValues.put("ANNOT_COST", costText.text.toString())
            database?.update(db?.ANNOT,contentValues, "ANNOT_ID="+annotID, null)

            nameAnnot.setText(textName.text.toString())
            costAnnot.setText(costText.text.toString().toFloat().toString())

            builder?.dismiss()
        })

        builder?.setOnDismissListener {
            cursor?.close()
            database?.close()
            db?.close()
        }

    }

    protected fun clearAllAnnotations(){
        var builder: AlertDialog
        builder = AlertDialog.Builder(this@AnnotationActivity)
            ?.setTitle("Очистить список?")
            ?.setMessage("Список будет очищен")
            ?.setPositiveButton("ДА - УДАЛИТЬ", null)
            ?.setNegativeButton("ОТМЕНА",null)
            ?.show()!!

        var button = builder.getButton(AlertDialog.BUTTON_POSITIVE)
        button.setOnClickListener(View.OnClickListener {
            var database = db?.writableDatabase
            database?.delete(db?.ANNOT,null,null)
            database?.close()
            listAnotation.removeAllViews()
            builder.dismiss()
        })


    }



}
