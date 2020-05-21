package com.example.firebasemessager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.purchase.MainActivity

private val DATABASE_VERSION = 1
private val DATABASE_NAME = "Purchase"

class DB(сontext: Context?) : SQLiteOpenHelper(сontext, DATABASE_NAME, null, DATABASE_VERSION) {

    val GROUPS = "GROUPS"
    val SHOPIN = "SHOPIN"

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
                    "SH_GROUP_ID, "+
                    "FOREIGN KEY (SH_GROUP_ID) REFERENCES "+GROUPS+"(GROUP_ID) ON DELETE CASCADE)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("drop table if exists "+GROUPS)
        db?.execSQL("drop table if exists "+SHOPIN)
        onCreate(db)
    }

}