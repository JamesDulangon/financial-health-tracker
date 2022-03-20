package com.example.financialhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_expenses.*
import kotlinx.android.synthetic.main.activity_todaysexpenditure_input.*
import kotlinx.android.synthetic.main.activity_todaysexpenditure_input.view.*
import kotlinx.android.synthetic.main.dialog_deadline_picker.view.*
import java.util.*

class LaunchActivity : AppCompatActivity() {
    lateinit var usersDBHelper : UsersDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersDBHelper = UsersDBHelper(this)

        // if first run of app, initializes to expenses
        if (isNotInitialized()) {
            setContentView(R.layout.activity_expenses)

            AlertDialog.Builder(this)
                .setTitle("Welcome to your Financial Health Tracker!")
                .setMessage(R.string.initial_msg)
                .show()
        } else { // proceeds to launch next activity
            val nextIntent = Intent(this, MainActivity::class.java)
            // nextIntent.putExtra("IsNotInitialized", false)
            startActivity(nextIntent)
        }

        var isDeadlineSet = false

        var wBillDeadline = 1
        var eBillDeadline = 1

        deadlineBtn.setOnClickListener {
            val deadlineDialog = LayoutInflater.from(this).inflate(R.layout.dialog_deadline_picker, null)
            val builder = AlertDialog.Builder(this)
                .setView(deadlineDialog)
                .setTitle("Set Deadlines")

            if (isDeadlineSet) {
                deadlineDialog.wBillDeadline.setText(wBillDeadline.toString())
                deadlineDialog.eBillDeadline.setText(eBillDeadline.toString())
            }

            val responses = builder.show()

            deadlineDialog.setBtn.setOnClickListener {
                val validWBill = deadlineDialog.wBillDeadline.text.toString().toInt() in 1..31
                val validEBill = deadlineDialog.eBillDeadline.text.toString().toInt() in 1..31

                if (validEBill && validWBill) {
                    responses.dismiss()
                    isDeadlineSet = true

                    wBillDeadline = deadlineDialog.wBillDeadline.text.toString().toInt()
                    eBillDeadline = deadlineDialog.eBillDeadline.text.toString().toInt()
                }
                else
                    Toast.makeText(this, "Deadlines are not valid!", Toast.LENGTH_SHORT).show()
            }
        }

        confirmBtn.setOnClickListener {
            val nextIntent = Intent(this, MainActivity::class.java)

            if (income.text.isNotEmpty() && wBill.text.isNotEmpty() && eBill.text.isNotEmpty() &&
                othExpenses.text.isNotEmpty() && savingsGoal.text.isNotEmpty() && isDeadlineSet) {

                // Show today's expenditure dialog
                val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_todaysexpenditure_input,null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)
                    .setTitle("Input Today's Expenditure")

                val mAlertDialog = mBuilder.show()
                mDialogView.btn_submit.setOnClickListener {
                    if (mDialogView.et_todaysExpenditure.text.isNotEmpty()) {
                        mAlertDialog.dismiss()

                        val todaysExpenditure = mDialogView.et_todaysExpenditure.text.toString()

                        // Add entry to sqlite db instead of intent.putExtra
                        val test = usersDBHelper.insertEntry(FinancialModel(income = income.text.toString(), savingsGoal = savingsGoal.text.toString(),
                            todaysExpenditure = todaysExpenditure, eBill = eBill.text.toString(), eBillDl = eBillDeadline, wBill = wBill.text.toString(),
                            wBillDl = wBillDeadline, otherExpenses = othExpenses.text.toString(), index = System.currentTimeMillis()))

                        val prefsFile = "FinancialTracker"
                        val prefsFileKey = "IsNotInitialized"
                        val date = Calendar.getInstance().get(Calendar.DATE)

                        val prefs = getSharedPreferences(prefsFile, MODE_PRIVATE)
                        prefs.edit()
                            .putInt(prefsFileKey, 0)
                            .putInt("LastInputDate", date)
                            .apply()

                        if (test)
                            startActivity(nextIntent)
                        else
                            Toast.makeText(this, "Entry not added!", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this, "Today's expenses invalid!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Incomplete data provided!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNotInitialized(): Boolean {
        val prefsFile = "FinancialTracker"
        val prefsFileKey = "IsNotInitialized"
        val nonexistent = -1

        val prefs = getSharedPreferences(prefsFile, MODE_PRIVATE)

        return prefs.getInt(prefsFileKey, nonexistent) == -1
    }

}