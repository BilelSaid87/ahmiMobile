package tn.ahmi.ble

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import tn.ahmi.R
import tn.ahmi.data.db.entities.User

class InteractionsAdapter(activity: Activity, resourceID:Int, debugData:ArrayList<DebugData>):
    ArrayAdapter<DebugData>(activity, resourceID,debugData) {

    private var activity: Activity
    private var layoutResourceID:Int
    private var debugArr:ArrayList<DebugData>

    init {
        this.activity = activity
        this.layoutResourceID =resourceID
        this.debugArr = debugData
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var convertView_out: View? = convertView

        if (convertView_out == null){
            val inflater = activity.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView_out = inflater.inflate(layoutResourceID, parent,false)
        }
        val debug = getItem(position)
        val mac = debug?.BleMac
        val avg_dist = debug?.avgDist
        val min_dist = debug?.minDist
        val interactionTime = debug?.interactionTime!!/Constants.sec2millisec
        val interactionStart = Utils.getDate(debug.startTimestamp!!,"dd/MM/yyyy hh:mm:ss.SSS")
        val txPower = debug?.txPower
        val build_model = debug?.buildModel
        val tv_mac = convertView_out!!.findViewById<TextView>(R.id.tv_mac)
        val tv_start= convertView_out.findViewById<TextView>(R.id.tv_startTime)
        val tv_period= convertView_out.findViewById<TextView>(R.id.tv_periodTime)
        val tv_avgDist= convertView_out.findViewById<TextView>(R.id.tv_avgDist)
        val tv_minDist= convertView_out.findViewById<TextView>(R.id.tv_minDist)
        val tv_txPower= convertView_out.findViewById<TextView>(R.id.tv_txpower)
        val tv_buildModel= convertView_out.findViewById<TextView>(R.id.tv_model)

        tv_mac.text = "MAC: $mac"
        tv_start.text = "Start: $interactionStart"
        tv_period.text = "Period: $interactionTime"
        tv_avgDist.text = "AvgDist: $avg_dist"
        tv_minDist.text = "MinDist: $min_dist"
        tv_txPower.text = "txPower: $txPower"
        tv_buildModel.text = "Model: $build_model"

        return convertView_out
    }
}