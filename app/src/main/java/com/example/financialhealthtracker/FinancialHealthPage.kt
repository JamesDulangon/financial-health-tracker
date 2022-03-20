package com.example.financialhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
/* import android.content.Intent
import android.os.PersistableBundle
import android.text.Spannable
import android.text.SpannableString */
import kotlinx.android.synthetic.main.activity_fhpage.*
import java.util.*

class FinancialHealthPage: AppCompatActivity() {
    private lateinit var usersDBHelper : UsersDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fhpage)

        usersDBHelper = UsersDBHelper(this)

        val bundle : Bundle? = intent.extras
        val lastEntry : FinancialModel = usersDBHelper.readLatestEntry()
        val allEntry : ArrayList<FinancialModel> = usersDBHelper.readAllEntry()

        var badCounter = 0
        val latestIncome : String = lastEntry.income
        val latestWBill : String = lastEntry.wBill
        val latestEBill : String = lastEntry.eBill
        val latestOthExpenses : String = lastEntry.otherExpenses
        val latestTodExpenses : String = lastEntry.todaysExpenditure
        val latestSavingsGoal : String = lastEntry.savingsGoal

        val prevEntry : FinancialModel
        val prevNetWorth : Float
        val nWChange : Float

        if (usersDBHelper.getEntryCount() > 1) {
            prevEntry = usersDBHelper.readLatestEntry()
            prevNetWorth = prevEntry.income.toFloat() - prevEntry.wBill.toFloat() - prevEntry.eBill.toFloat() - prevEntry.otherExpenses.toFloat() - (30 * prevEntry.todaysExpenditure.toFloat())
            nWChange = ((bundle!!.getFloat("networth") / prevNetWorth) - 1) * 100
        } else
            nWChange = 0.0f

        var combiStr: String
        when (bundle!!.getString("mode")) {
            "AdDet" -> {
                combiStr = resources.getString(R.string.FHP_1) + ": %.2f\n".format(latestIncome.toFloat())
                combiStr += resources.getString(R.string.FHP_2) + ": %.2f\n".format(latestEBill.toFloat())
                combiStr += resources.getString(R.string.FHP_3) + ": %.2f\n".format(latestWBill.toFloat())
                combiStr += resources.getString(R.string.FHP_4) + ": %.2f\n".format(latestOthExpenses.toFloat())
                combiStr += resources.getString(R.string.FHP_5) + ": %.2f\n".format(latestTodExpenses.toFloat())
                combiStr += resources.getString(R.string.FHP_6) + ": %.2f\n\n".format(latestSavingsGoal.toFloat())
                combiStr += resources.getString(R.string.tv_1) + ": %.2f\n".format(bundle.getFloat("networth"))
                combiStr += resources.getString(R.string.FHP_8) + ": %.1f\n".format(nWChange)
                combiStr += resources.getString(R.string.FHP_7) + ": "
                combiStr += if (nWChange.toInt() in 0 .. 5) resources.getString(R.string.FHP_9) + "\n\n"
                else if(nWChange > 5) resources.getString(R.string.FHP_10) + "\n\n"
                else {
                    resources.getString(R.string.FHP_11) + "\n\n"
                    badCounter++
                }
                combiStr += resources.getString(R.string.tv_2) + ": %.1f".format(bundle.getFloat("savingsrate")) + "\n"
                combiStr += resources.getString(R.string.FHP_7) + ": "
                combiStr += if(bundle.getFloat("savingsrate").toInt() in 0 .. 20) resources.getString(R.string.FHP_9) + "\n\n"
                else if(bundle.getFloat("savingsrate").toInt() > 20) resources.getString(R.string.FHP_10) + "\n\n"
                else {
                    resources.getString(R.string.FHP_11) + "\n\n"
                    badCounter++
                }
                combiStr += resources.getString(R.string.tv_3) + ": %.1f".format(bundle.getFloat("dtiratio")) + "\n"
                combiStr += resources.getString(R.string.FHP_7) + ": "
                combiStr += if(bundle.getFloat("dtiratio").toInt() in 25 .. 30) resources.getString(R.string.FHP_9) + "\n\n"
                else if(bundle.getFloat("dtiratio").toInt() < 20) resources.getString(R.string.FHP_10) + "\n\n"
                else {
                    resources.getString(R.string.FHP_11) + "\n\n"
                    badCounter++
                }
                combiStr += resources.getString(R.string.tv_6) + ": %.1f".format(bundle.getFloat("savingsgrowth")) + "\n"
                combiStr += resources.getString(R.string.FHP_7) + ": "
                combiStr += if(bundle.getFloat("savingsgrowth").toInt() in 3 .. 5) resources.getString(R.string.FHP_9) + "\n\n"
                else if(bundle.getFloat("savingsgrowth").toInt() > 5) resources.getString(R.string.FHP_10) + "\n\n"
                else {
                    resources.getString(R.string.FHP_11) + "\n\n"
                    badCounter++
                }
                if(badCounter in 2..4) unhealthy()
                FText.setText(combiStr)
            }
            "BLog" -> {
                combiStr = resources.getString(R.string.BLog_1) + "\n"
                for(count in allEntry.size-1 downTo 0) {
                    combiStr += resources.getString(R.string.FHP_1) + ": %.2f\n".format(allEntry[count].income.toFloat())
                    combiStr += resources.getString(R.string.FHP_2) + ": %.2f\n".format(allEntry[count].eBill.toFloat())
                    combiStr += resources.getString(R.string.FHP_3) + ": %.2f\n".format(allEntry[count].wBill.toFloat())
                    combiStr += resources.getString(R.string.FHP_4) + ": %.2f\n".format(allEntry[count].otherExpenses.toFloat())
                    combiStr += resources.getString(R.string.FHP_5) + ": %.2f\n".format(allEntry[count].todaysExpenditure.toFloat())
                    combiStr += resources.getString(R.string.FHP_6) + ": %.2f\n\n".format(allEntry[count].savingsGoal.toFloat())
                    if(count != 0) combiStr += resources.getString(R.string.BLog_2) + " $count\n"
                }
                FText.setText(combiStr)
            }
        }
    }

    fun unhealthy (){
        // Toast.makeText(this, "PaymentDeficit", Toast.LENGTH_SHORT).show()
        val x = 2
        val serviceIntent = Intent(this, ExampleService::class.java)
        serviceIntent.putExtra("input", x)
        startService(serviceIntent)
    }
}