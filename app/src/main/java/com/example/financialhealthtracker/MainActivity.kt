package com.example.financialhealthtracker

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_todaysexpenditure_input.view.*
import kotlinx.android.synthetic.main.fin_h.view.*
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var usersDBHelper : UsersDBHelper

//    private val prefsFile = "FinancialTracker"
//    private val prefs: SharedPreferences = getSharedPreferences(prefsFile, MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService()

        usersDBHelper = UsersDBHelper(this)

        // Read latest entry or 'inputs' in db
        val lastEntry : FinancialModel = usersDBHelper.readLatestEntry()
        val latest_income : String = lastEntry.income
        val latest_wBill : String = lastEntry.wBill
        val latest_eBill : String = lastEntry.eBill
        val latest_othExpenses : String = lastEntry.otherExpenses
        val latest_todExpenses : String = lastEntry.todaysExpenditure
        val latest_savingsGoal : String = lastEntry.savingsGoal
        val latest_wBillDl : Int = lastEntry.wBillDl
        val latest_eBillDl: Int = lastEntry.eBillDl

        val prev_savingsGoal : String = if (usersDBHelper.getEntryCount() > 1) {
            val prevEntry : FinancialModel = usersDBHelper.readLatestEntry()
            prevEntry.savingsGoal
        } else {
            "0"
        }

        enterExpensesBtn.setOnClickListener {
            lateinit var mAlertDialog : AlertDialog

            val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_todaysexpenditure_input ,null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Input Today's Expenditure")

            if (checkIfInputtedToday()) {
                AlertDialog.Builder(this)
                    .setTitle("Overwrite previous input?")
                    .setMessage("You have inputted today's expenses. Would you like to overwrite it?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                        mAlertDialog = mBuilder.show()
                    })
                    .setNegativeButton("No",DialogInterface.OnClickListener { _, _ ->
                        // do nothing
                    }).show()
            } else
                mAlertDialog = mBuilder.show()

            val prev_todExpenses : String

            if (usersDBHelper.getEntryCount() > 0) {
                val currEntry : FinancialModel = usersDBHelper.readLatestEntry()
                prev_todExpenses = currEntry.todaysExpenditure
            } else
                prev_todExpenses = ""

            mDialogView.et_todaysExpenditure.setText(prev_todExpenses)

            mDialogView.btn_submit.setOnClickListener {
                mAlertDialog.dismiss()

                // indicates that there's input today
                val prefsKey = "LastInputDate"
                val currentDate = Calendar.getInstance().get(Calendar.DATE)
                val prefsFile = "FinancialTracker"
                val prefs = getSharedPreferences(prefsFile, MODE_PRIVATE)

                prefs.edit().putInt(prefsKey, currentDate).apply()

                // get text from EditText of Custom Layout
                val todaysExpenditure = mDialogView.et_todaysExpenditure.text.toString()

                usersDBHelper.insertEntry(
                    FinancialModel(
                        income = latest_income,
                        savingsGoal = latest_savingsGoal,
                        todaysExpenditure = todaysExpenditure,
                        eBill = latest_eBill,
                        eBillDl = latest_eBillDl,
                        wBill = latest_wBill,
                        wBillDl = latest_wBillDl,
                        otherExpenses = latest_othExpenses,
                        index = System.currentTimeMillis()
                    )
                )
            }
        }

        // Calculations for the "Check Financial Health" Alert Dialog (Pop-up from Button)
        checkFinHealthBtn.setOnClickListener {
            val nextIntent = Intent(this, FinancialHealthPage::class.java)
            val dialogView = LayoutInflater.from(this).inflate(R.layout.fin_h, null)
            val builder = AlertDialog.Builder(this)
                .setIcon(R.drawable.crop_health_icon)
                .setTitle(resources.getString(R.string.fhval))
                .setView(dialogView)

            var calc: Float = latest_income.toFloat() - latest_wBill.toFloat() - latest_eBill.toFloat() - latest_othExpenses.toFloat() - (30 * latest_todExpenses.toFloat())
            dialogView.networth.text = resources.getString(R.string.tv_1) + ": %.2f".format(calc)
            calc = (latest_savingsGoal.toFloat() / latest_income.toFloat()) * 100
            nextIntent.putExtra("networth", calc)

            dialogView.savingsrate.text = resources.getString(R.string.tv_2) + ": %.1f".format(calc)
            calc = 100 * ((latest_eBill.toFloat() + latest_wBill.toFloat() + latest_othExpenses.toFloat() + (30 * latest_todExpenses.toFloat())) / latest_income.toFloat())
            nextIntent.putExtra("savingsrate", calc)

            dialogView.dtiratio.text = resources.getString(R.string.tv_3) + ": %.1f".format(calc)
            nextIntent.putExtra("dtiratio", calc)

            calc = ((latest_income.toFloat() / prev_savingsGoal.toFloat()) - 1) * 100
            nextIntent.putExtra("savingsgrowth", calc)
            dialogView.incomegrowth.text = resources.getString(R.string.tv_6) + ": %.1f".format(calc)

            val alertDialog = builder.show()
            dialogView.buttonAdd.setOnClickListener {
                alertDialog.dismiss()
                nextIntent.putExtra("mode","AdDet")
                startActivity(nextIntent)
            }

            dialogView.buttonRetFund.setOnClickListener {
                alertDialog.dismiss()
                val webIntent = Intent(this, WebActivity::class.java)
                startActivity(webIntent)
            }

            dialogView.buttonBLog.setOnClickListener {
                alertDialog.dismiss()
                nextIntent.putExtra("mode","BLog")
                startActivity(nextIntent)
            }
        }

        editMonthlyBtn.setOnClickListener {
            val nextIntent = Intent(this, Expenses::class.java)
            nextIntent.putExtra("EditExpenses", true)
            startActivity(nextIntent)
        }

        // check deadline
        val date = Calendar.getInstance()

        if (date.get(Calendar.DATE) == latest_eBillDl || date.get(Calendar.DATE) == latest_wBillDl)
            deadline()
    }

    private fun checkIfInputtedToday() : Boolean {
        val prefsKey = "LastInputDate"

        // check if last input is same as current date
        val currentDate = Calendar.getInstance().get(Calendar.DATE)

        val prefsFile = "FinancialTracker"
        val prefs = getSharedPreferences(prefsFile, MODE_PRIVATE)

        return prefs.getInt(prefsKey, 0) == currentDate
    }


    //LOCAL PUSH NOTIFICATIONS FUNCTIONS
    private fun startService() {
        Toast.makeText(this, "Started Service", Toast.LENGTH_SHORT).show()
        val x = 0
        val serviceIntent: Intent = Intent(this, ExampleService::class.java)
        serviceIntent.putExtra("input", x)
        startService(serviceIntent)
    }

    fun stopService(){
        Toast.makeText(this, "Stopped Service", Toast.LENGTH_SHORT).show()
        val serviceIntent: Intent = Intent(this, ExampleService::class.java)
        stopService(serviceIntent)
    }

    private fun deadline (){
        // Toast.makeText(this, "PaymentDeficit", Toast.LENGTH_SHORT).show()
        val x = 1
        val serviceIntent: Intent = Intent(this, ExampleService::class.java)
        serviceIntent.putExtra("input", x)
        startService(serviceIntent)
    }
}


