package com.example.financialhealthtracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

import android.database.DatabaseUtils
import java.util.*

class UsersDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertEntry(entry: FinancialModel): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBSchema.UserEntity.COLUMN_INCOME, entry.income)
        values.put(DBSchema.UserEntity.COLUMN_GOAL, entry.savingsGoal)
        values.put(DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE, entry.todaysExpenditure)
        values.put(DBSchema.UserEntity.COLUMN_E_BILLS, entry.eBill)
        values.put(DBSchema.UserEntity.COLUMN_EBILL_DAYDL, entry.eBillDl)
        values.put(DBSchema.UserEntity.COLUMN_W_BILLS, entry.wBill)
        values.put(DBSchema.UserEntity.COLUMN_WBILL_DAYDL, entry.wBillDl)
        values.put(DBSchema.UserEntity.COLUMN_OTHER_EXP, entry.otherExpenses)
        values.put(DBSchema.UserEntity.COLUMN_INDEX, System.currentTimeMillis())

        // Insert the new row, returning the primary key value of the new row
        val success = db.insert(DBSchema.UserEntity.TABLE_NAME, null, values)

        db.close()
        return (Integer.parseInt("$success")!=-1)
    }

    @Throws(SQLiteConstraintException::class)
    fun deleteEntry(todaysExpenditure: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase
        // Define 'where' part of query.
        val selection = DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE + " LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(todaysExpenditure)
        // Issue SQL statement.
        val success = db.delete(DBSchema.UserEntity.TABLE_NAME, selection, selectionArgs)

        db.close()
        return (Integer.parseInt("$success")!= 0)
    }

    fun readEntry(todaysExpenditure: String): ArrayList<FinancialModel> {
        val entry = ArrayList<FinancialModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + DBSchema.UserEntity.TABLE_NAME + " WHERE " + DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE + "='" + todaysExpenditure + "'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return ArrayList()
        }

        var goal: String
        var income: String
        var eBill: String
        var eBillDl: Int
        var wBill: String
        var wBillDl: Int
        var otherExpenses : String
        var time : Long

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                goal = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_GOAL))
                income = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE))
                eBill = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_E_BILLS))
                eBillDl = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_EBILL_DAYDL))
                wBill = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_W_BILLS))
                wBillDl = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_WBILL_DAYDL))
                otherExpenses = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_OTHER_EXP))
                time = cursor.getLong(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INDEX))

                entry.add(FinancialModel(income, goal, todaysExpenditure, eBill, eBillDl, wBill, wBillDl, otherExpenses, time))
                cursor.moveToNext()
            }
        }
        db.close()
        return entry
    }

    fun readLatestEntry(): FinancialModel {
        val lastEntryAL = ArrayList<FinancialModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + DBSchema.UserEntity.TABLE_NAME , null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            print("Error")
        }

        cursor!!.moveToLast()

        var income: String = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INCOME))
        var goal: String = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_GOAL))
        var todaysExpenditure: String = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE))
        var eBill: String = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_E_BILLS))
        var eBillDl: Int = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_EBILL_DAYDL))
        var wBill: String = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_W_BILLS))
        var wBillDl: Int = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_WBILL_DAYDL))
        var otherExpenses : String = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_OTHER_EXP))
        var time : Long = cursor.getLong(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INDEX))

        lastEntryAL.add(FinancialModel(income, goal, todaysExpenditure, eBill,eBillDl, wBill, wBillDl,otherExpenses, time))

        db.close()
        return lastEntryAL[0]
    }


    fun getEntryCount(): Int {
        val db = writableDatabase
        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM finance", null).toInt()

    }

    fun readEntryAtIndex(index: Int): FinancialModel {
        val entryAtIndex = ArrayList<FinancialModel>()
        val db = writableDatabase
        var cursor: Cursor? = null

        if((getEntryCount()+1) <= index)
        {
            print("Out of bounce! There are ${getEntryCount()} entries in db. You passed $index");
        }
        try {
            cursor = db.rawQuery("select * from " + DBSchema.UserEntity.TABLE_NAME , null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            print("Error")
        }

        var income: String
        var goal: String
        var todaysExpenditure: String
        var eBill: String
        var eBillDl: Int
        var wBill: String
        var wBillDl: Int
        var otherExpenses : String
        var time : Long

        cursor!!.move(index-1)
        if(cursor != null)
        {
            income = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INCOME))
            goal = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_GOAL))
            todaysExpenditure = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE))
            eBill = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_E_BILLS))
            eBillDl = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_EBILL_DAYDL))
            wBill = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_W_BILLS))
            wBillDl = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_WBILL_DAYDL))
            otherExpenses = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_OTHER_EXP))
            time = cursor.getLong(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INDEX))

            entryAtIndex.add(FinancialModel(income, goal, todaysExpenditure, eBill,eBillDl, wBill, wBillDl,otherExpenses, time))
        }

        db.close()
        return entryAtIndex[0]
    }

    fun readAllEntry(): ArrayList<FinancialModel> {
        val entry = ArrayList<FinancialModel>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + DBSchema.UserEntity.TABLE_NAME , null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        var income: String
        var goal: String
        var todaysExpenditure: String
        var eBill: String
        var eBillDl: Int
        var wBill: String
        var wBillDl: Int
        var otherExpenses : String
        var time : Long

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                income = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INCOME))
                goal = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_GOAL))
                todaysExpenditure = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE))
                eBill = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_E_BILLS))
                eBillDl = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_EBILL_DAYDL))
                wBill = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_W_BILLS))
                wBillDl = cursor.getInt(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_WBILL_DAYDL))
                otherExpenses = cursor.getString(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_OTHER_EXP))
                time = cursor.getLong(cursor.getColumnIndex(DBSchema.UserEntity.COLUMN_INDEX))

                entry.add(FinancialModel(income, goal, todaysExpenditure,  eBill,eBillDl,    wBill, wBillDl,otherExpenses, time))
                cursor.moveToNext()
            }
        }
        db.close()
        return entry
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 2
        val DATABASE_NAME = "FeedReader.db"

        private val SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBSchema.UserEntity.TABLE_NAME + " (" +
                    DBSchema.UserEntity.COLUMN_TODAYS_EXPENDITURE + " TEXT," +
                    DBSchema.UserEntity.COLUMN_INCOME + " TEXT," +
                    DBSchema.UserEntity.COLUMN_GOAL + " TEXT," +
                    DBSchema.UserEntity.COLUMN_E_BILLS + " TEXT," +
                    DBSchema.UserEntity.COLUMN_EBILL_DAYDL + " INT," +
                    DBSchema.UserEntity.COLUMN_W_BILLS + " TEXT," +
                    DBSchema.UserEntity.COLUMN_WBILL_DAYDL + " INT," +
                    DBSchema.UserEntity.COLUMN_OTHER_EXP + " TEXT," +
                    DBSchema.UserEntity.COLUMN_INDEX + " INT PRIMARY KEY)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBSchema.UserEntity.TABLE_NAME
    }

}