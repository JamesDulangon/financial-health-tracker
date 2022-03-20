package com.example.financialhealthtracker

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_expenses.*
import kotlinx.android.synthetic.main.dialog_deadline_picker.view.*
import java.util.*

class Expenses : AppCompatActivity() {
    lateinit var usersDBHelper : UsersDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        usersDBHelper = UsersDBHelper(this)
        val lastEntry : FinancialModel = usersDBHelper.readLatestEntry()
        val countEntries : Int = usersDBHelper.getEntryCount()

        var wBillDeadline = 1
        var eBillDeadline = 1
        // Show last entry in db.

        if(countEntries > 0)
        {
            income.setText(lastEntry.income)
            wBill.setText(lastEntry.wBill)
            eBill.setText(lastEntry.eBill)
            othExpenses.setText(lastEntry.otherExpenses)
            savingsGoal.setText(lastEntry.savingsGoal)

            wBillDeadline = lastEntry.wBillDl
            eBillDeadline = lastEntry.eBillDl
        }

        deadlineBtn.setOnClickListener(){
            val deadlineDialog = LayoutInflater.from(this).inflate(R.layout.dialog_deadline_picker, null)
            val builder = AlertDialog.Builder(this)
                .setView(deadlineDialog)
                .setTitle("Set Deadlines")

            val responses = builder.show()

            // Show last picked deadline
            if(countEntries > 0) {
                deadlineDialog.eBillDeadline.setText(lastEntry.eBillDl.toString())
                deadlineDialog.wBillDeadline.setText(lastEntry.wBillDl.toString())
            }

            deadlineDialog.setBtn.setOnClickListener {
                val validWBill = deadlineDialog.wBillDeadline.text.toString().toInt() in 1..31
                val validEBill = deadlineDialog.eBillDeadline.text.toString().toInt() in 1..31

                if (validEBill && validWBill) {
                    responses.dismiss()

                    wBillDeadline = deadlineDialog.wBillDeadline.text.toString().toInt()
                    eBillDeadline = deadlineDialog.eBillDeadline.text.toString().toInt()
                }
                else
                    Toast.makeText(this, "Deadlines are not valid!", Toast.LENGTH_SHORT).show()
            }
        }

        confirmBtn.setOnClickListener(){
            //Add entry
            usersDBHelper.insertEntry(FinancialModel(income = income.text.toString(), savingsGoal = savingsGoal.text.toString(),
                todaysExpenditure = lastEntry.todaysExpenditure, eBill = eBill.text.toString(), eBillDl = eBillDeadline, wBill = wBill.text.toString(),
                wBillDl = wBillDeadline, otherExpenses = othExpenses.text.toString(), index = System.currentTimeMillis()))

            // to main activity
            val nextIntent = Intent(this, MainActivity::class.java)
            startActivity(nextIntent)
        }


        // SQLITE DATABASE METHODS
        //NOTE: 'todaysExpenditure' is the primary key
//        fun addEntry(v: View){

//            var userid = this.edittext_userid.text.toString()
//            var name = this.edittext_name.text.toString()
//            var result = usersDBHelper.insertUser(StudentModel(userid = userid,name = name))
/*
            var result = usersDBHelper.insertEntry(FinancialModel(income = income,savingsGoal = savingsGoal,
                todaysExpenditure = todaysExpenditure, eBill = eBill, eBillDl = eBillDl,wBill = wBill,wBillDl = wBillDl otherExpenses = otherExpenses))
            /*
             clear all edittext, refer sample code below:

                this.edittext_name.setText("")
                this.edittext_userid.setText("")
                this.textview_result.text = "Added student : "+result
                this.ll_entries.removeAllViews()
             */
        }
        fun readEntry(v: View){
            val result = usersDBHelper.readEntry(todaysExpenditure)
        }
        fun deleteEntry(v: View){
            /*
                toString here the inputs, refer sample code below:

                    var userid = this.edittext_userid.text.toString()
             */

            val result = usersDBHelper.deleteEntry(todaysExpenditure)
            /*
             clear all edittext, refer sample code below:

                this.edittext_name.setText("")
                this.edittext_userid.setText("")
                this.textview_result.text = "Deleted student : "+result
                this.ll_entries.removeAllViews()
             */
        }
        fun showAllEntry(v: View){
            var users = usersDBHelper.readAllEntry()

            /*
                Print all entry

                    this.ll_entries.removeAllViews()
                    users.forEach {
                        var tv_user = TextView(this)
                        tv_user.textSize = 30F
                        tv_user.text = it.userid.toString()  + "   " + it.name.toString()
                        this.ll_entries.addView(tv_user)
                    }
                    this.textview_result.text = "Fetched " + users.size + " students"
             */

 */

//        }
    }
}