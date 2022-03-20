package com.example.financialhealthtracker

import android.provider.BaseColumns

class DBSchema {
    /* Inner class that defines the table contents */
    class UserEntity : BaseColumns {
        companion object {
            val TABLE_NAME = "finance"
            val COLUMN_INDEX = "time"
            val COLUMN_INCOME = "income"
            val COLUMN_GOAL = "savingsGoal"
            val COLUMN_TODAYS_EXPENDITURE = "todaysExpenditure"
            val COLUMN_E_BILLS = "eBill"
            val COLUMN_W_BILLS = "wBill"
            val COLUMN_OTHER_EXP = "otherExpenses"
            val COLUMN_EBILL_DAYDL = "eBillDeadline"
            val COLUMN_WBILL_DAYDL = "wBillDeadline"
        }
    }
}