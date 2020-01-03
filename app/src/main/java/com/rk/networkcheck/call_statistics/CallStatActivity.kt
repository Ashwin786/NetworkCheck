package com.rk.networkcheck.call_statistics

import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.widget.Toast
import com.rk.networkcheck.R
import kotlinx.android.synthetic.main.activity_call_stat.*
import java.text.SimpleDateFormat
import java.util.*

class CallStatActivity : AppCompatActivity() {
    private var selected_type: String = "Call"
    private var to_time: Long = 0
    private var from_time: Long = 0
    private var from: Boolean = false
    val myCalendar = Calendar.getInstance()
    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_stat)
        initView()
    }

    private fun initView() {
        ti_fromDate.setOnClickListener {
            from = true;
            datepicker()
        }
        ti_toDate.setOnClickListener {
            from = false;
            datepicker()
        }
        btn_check.setOnClickListener {
            if (from_time == 0L)
                Toast.makeText(this, "Choose from time", Toast.LENGTH_SHORT).show()
            else if (to_time == 0L)
                Toast.makeText(this, "Choose to time", Toast.LENGTH_SHORT).show()
            else /*if (rg_btn.checkedRadioButtonId == -1)
                Toast.makeText(this, "Select Type", Toast.LENGTH_SHORT).show()
            else */{
                checkType()
                var totalDuration = CallPresenter.getInstance(this).getOutgoingCallTime(from_time, to_time, type)
                tv_result.setText("Total " + selected_type + " Duration : " + totalDuration / 60 + " Mins")
            }

        }

    }

    private fun checkType() {

        var checkedId = rg_btn.checkedRadioButtonId
        if (R.id.rb_Outgoing == checkedId) {
            type = CallLog.Calls.OUTGOING_TYPE
            selected_type = "Outgoing Call"
        } else if (R.id.rb_Incoming == checkedId) {
            type = CallLog.Calls.INCOMING_TYPE
            selected_type = "Incoming Call"
        }

    }

    private fun datepicker() {
        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }
        DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()

    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        if (from) {
            from_time = myCalendar.timeInMillis
            ti_fromDate.setText(sdf.format(myCalendar.time))

        } else {
            to_time = myCalendar.timeInMillis
            ti_toDate.setText(sdf.format(myCalendar.time))
        }
    }
}
