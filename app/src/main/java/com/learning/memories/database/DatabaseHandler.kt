package com.learning.memories.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.learning.memories.models.MemoryModel

class DatabaseHandler(context:Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {
    companion object{
        private const val DATABASE_VERSION=1
        private const val DATABASE_NAME="MemoriesDatabase"
        private const val TABLE_MEMORY="MemoriesTable"

        private const val KEY_ID="_id"
        private const val KEY_TITLE="title"
        private const val KEY_IMAGE="image"
        private const val KEY_DESCRIPTION="description"
        private const val KEY_DATE="date"
        private const val KEY_LOCATION="location"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_MEMORY_TABLE = (
                "CREATE TABLE " + TABLE_MEMORY + "(" +
                        KEY_ID + " INTEGER PRIMARY KEY, " +
                        KEY_TITLE + " TEXT, " +
                        KEY_IMAGE + " TEXT, " +
                        KEY_DESCRIPTION + " TEXT, " +
                        KEY_DATE + " TEXT, " +
                        KEY_LOCATION + " TEXT)"
                )
        db?.execSQL(CREATE_MEMORY_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORY")
        onCreate(db)
    }

    fun addMemory(memory:MemoryModel):Long{
        val db =this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(KEY_TITLE,memory.title)
        contentValues.put(KEY_IMAGE,memory.image)
        contentValues.put(KEY_DESCRIPTION, memory.description)
        contentValues.put(KEY_DATE, memory.date)
        contentValues.put(KEY_LOCATION, memory.location)

        val result = db.insert(TABLE_MEMORY,null,contentValues)
        db.close()
        return result
    }

    fun updateMemory(memory: MemoryModel):Int{
        val db =this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(KEY_TITLE,memory.title)
        contentValues.put(KEY_IMAGE,memory.image)
        contentValues.put(KEY_DESCRIPTION, memory.description)
        contentValues.put(KEY_DATE, memory.date)
        contentValues.put(KEY_LOCATION, memory.location)

        val result = db.update(
            TABLE_MEMORY,contentValues,
            KEY_ID+"="+memory.id,null)
        db.close()
        return result
    }

    fun deleteHappyPlace(memory: MemoryModel):Int{
        val db =this.writableDatabase
        val result = db.delete(TABLE_MEMORY,KEY_ID+"="+memory.id,null)
        db.close()
        return result
    }

    @SuppressLint("Range")
    fun getMemoriesList():ArrayList<MemoryModel>{
        val memoryList=ArrayList<MemoryModel>()
        val selectQuery = "SELECT * FROM $TABLE_MEMORY"
        val db= this.readableDatabase
        try{
            val cursor:Cursor=db.rawQuery(selectQuery,null)
            if(cursor.moveToFirst()){
                do{
                    val memory=MemoryModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION))
                    )
                    memoryList.add(memory)
                }while(cursor.moveToNext())
            }
            cursor.close()
        }catch(e:SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return memoryList
    }
}