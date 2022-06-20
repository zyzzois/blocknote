package com.example.blocknote.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.util.ArrayList

class MyDbManager(context: Context) { //этот класс создан только для того чтобы весь этот код нам не пришлось писать в MainActivity
    val myDbHelper = MyDbHelper(context)
    var db: SQLiteDatabase? = null


    //Отркытие БД
    fun openDb() {
        db = myDbHelper.writableDatabase //чтобы БД открылась для записи
    }

    //Вставка в БД
    suspend fun insertToDb(title: String,  content: String, url: String, time: String) = withContext(Dispatchers.IO) { //для запись в БД, но изначально нужно открыть БД в функции openDb
        val values = ContentValues().apply { //ContentValues - что-то вроде массива где данные сохраняются по ключ/значению
            put(MyDbNameClass.COLUMN_NAME_TITLE, title)
            put(MyDbNameClass.COLUMN_NAME_CONTENT, content)
            put(MyDbNameClass.COlUMN_NAME_URL, url)
            put(MyDbNameClass.COlUMN_NAME_TIME , time)
        }
        db?.insert(MyDbNameClass.TABLE_NAME, null, values)
    }

    //Удаление из БД
    fun removeItemFromDb(id: String) { //для запись в БД, но изначально нужно открыть БД в функции openDb
        val selection = BaseColumns._ID + "=$id"
        db?.delete(MyDbNameClass.TABLE_NAME,  selection, null)
    }

    //Считывание с БД
    suspend fun readDbData(searchText: String): ArrayList<ListItem> = withContext(Dispatchers.IO) { //будет возвращаться массив в котором было записано все, что было в БД. Будем доставать из БД title
        val dataList = ArrayList<ListItem>()
        val selection = "${MyDbNameClass.COLUMN_NAME_TITLE} like ?" //этот запрос означает что мы будем искать в колонне где у нас находятся заголовки и будем искать по запросу который мы отправили
        val cursor = db?.query(MyDbNameClass.TABLE_NAME, null, selection, arrayOf("%$searchText%"), null, null, null)
        while(cursor?.moveToNext()!!){
            val dataTitle = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_TITLE))
            val dataContent = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_CONTENT))
            val dataUrl = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COlUMN_NAME_URL))
            val dataId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            val dataTime = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COlUMN_NAME_TIME))
            val item = ListItem()
            item.title = dataTitle
            item.description = dataContent
            item.url = dataUrl
            item.id = dataId
            item.time = dataTime
            dataList.add(item)
        }
        cursor.close()
        return@withContext dataList
    }

    //Редактирование заметки
    suspend fun editItemFromDb(title: String,  content: String, url: String, id: Int, time: String) = withContext(Dispatchers.IO){ //для запись в БД, но изначально нужно открыть БД в функции openDb
        val selection = BaseColumns._ID + "=$id"
        val values = ContentValues().apply { //ContentValues - что-то вроде массива где данные сохраняются по ключ/значению
            put(MyDbNameClass.COLUMN_NAME_TITLE, title)
            put(MyDbNameClass.COLUMN_NAME_CONTENT, content)
            put(MyDbNameClass.COlUMN_NAME_URL, url)
            put(MyDbNameClass.COlUMN_NAME_TIME, time)
        }

        db?.update(MyDbNameClass.TABLE_NAME, values, selection, null)
    }

    //Закрытие БД
    fun closeDb() {
        myDbHelper.close()
    }


}