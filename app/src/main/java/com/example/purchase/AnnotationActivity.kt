package com.example.purchase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.firebasemessager.DB
import kotlinx.android.synthetic.main.activity_annotation.*
import kotlinx.android.synthetic.main.container_anotation.view.*

class AnnotationActivity : AppCompatActivity() {

    protected var db: DB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annotation)

        db = DB(this)
        listAnnotationsView()
    }


    protected fun listAnnotationsView(){
        var database = db?.writableDatabase
        var cursor = database?.rawQuery("SELECT *FROM "+db?.ANNOT, null)
        listAnotation.removeAllViews()

        if(cursor?.moveToFirst()!!){
            do{
                var containerAnnot = LayoutInflater.from(this).inflate(R.layout.container_anotation,null)

                containerAnnot.annot_name.setText(cursor.getString(cursor.getColumnIndex("ANNOT_NAME")))
                containerAnnot.annot_cost.setText(cursor.getFloat(cursor.getColumnIndex("ANNOT_COST")).toString())

                listAnotation.addView(containerAnnot)
            }while(cursor.moveToNext())
        }
        cursor?.close()
        database?.close()
        db?.close()
    }

}
