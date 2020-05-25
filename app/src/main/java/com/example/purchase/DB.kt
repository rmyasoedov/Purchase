package com.example.firebasemessager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.purchase.MainActivity

private val DATABASE_VERSION = 4
private val DATABASE_NAME = "Purchase"

class DB(сontext: Context?) : SQLiteOpenHelper(сontext, DATABASE_NAME, null, DATABASE_VERSION) {

    val GROUPS = "GROUPS"
    val SHOPIN = "SHOPIN"
    val ANNOT = "ANNOTATIONS"

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL("CREATE TABLE "+GROUPS+" (" +
                "GROUP_ID integer primary key, " +
                "GROUP_NAME text NOT NULL)")

        db?.execSQL("CREATE TABLE "+SHOPIN+" ("+
                    "SH_ID integer primary key, "+
                    "SH_NAME text NOT NULL, "+
                    "SH_COUNTS integer, "+
                    "SH_COST real, "+
                    "SH_ACTIVATE integer DEFAULT 0, "+
                    "SH_GROUP_ID integer, "+
                    "FOREIGN KEY (SH_GROUP_ID) REFERENCES "+GROUPS+"(GROUP_ID) ON DELETE CASCADE)")

        db?.execSQL("CREATE TABLE "+ANNOT+" ("+
                    "ANNOT_ID INTEGER PRIMARY KEY, "+
                    "ANNOT_NAME TEXT NOT NULL, "+
                    "ANNOT_COST REAL)")

        db?.execSQL("CREATE TRIGGER ADD_ANNOTATIONS_INSERT AFTER INSERT "+
                        "ON "+SHOPIN+
                        " BEGIN "+
                        "INSERT INTO "+ANNOT+" (ANNOT_NAME, ANNOT_COST) VALUES(NEW.SH_NAME, NEW.SH_COST);"+
                        "END;")

        db?.execSQL("CREATE TRIGGER ADD_ANNOTATIONS_UPDATE AFTER UPDATE "+
                "ON "+SHOPIN+
                " BEGIN "+
                "INSERT INTO "+ANNOT+" (ANNOT_NAME, ANNOT_COST) VALUES(NEW.SH_NAME, NEW.SH_COST);"+
                "END;")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("drop table if exists "+GROUPS)
        db?.execSQL("drop table if exists "+SHOPIN)
        db?.execSQL("drop table if exists "+ANNOT)
        db?.execSQL("DROP TRIGGER IF EXISTS "+ANNOT+".ADD_ANNOTATIONS_INSERT")
        db?.execSQL("DROP TRIGGER IF EXISTS "+ANNOT+".ADD_ANNOTATIONS_UPDATE")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        db?.execSQL("PRAGMA foreign_keys = ON;");
        super.onOpen(db)
    }

    fun oneval(table: String, field: String, where: String): String{
        var database = writableDatabase
        var cursor = database.rawQuery("SELECT "+field+" FROM "+table+" WHERE "+where, null)
        cursor.moveToFirst()
        var s: String = cursor.getString(cursor.getColumnIndex(field))
        cursor.close()
        database.close()
        close()
        return s
    }

}