package app.makino.harutiro.materialmanagementnotice.dousa

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.makino.harutiro.materialmanagementnotice.R
import android.util.DisplayMetrics

import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import app.makino.harutiro.materialmanagementnotice.date.MainDate
import io.realm.Realm
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter


class CustomDialogFlagment : DialogFragment() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_flagment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getString("id", "")
        val person = realm.where(MainDate::class.java).equalTo("id",id).findFirst()

        view.findViewById<Button>(R.id.dialogOkButton).setOnClickListener{

            val day = when(view.findViewById<Spinner>(R.id.dialogSpinner).selectedItemPosition){
                0 -> { 7L }
                1 -> { 14L }
                2 -> { 21L }
                3 -> { 28L }
                4 -> { 922330L }
                else -> { 0L }
            }

            realm.executeTransaction {
                person?.alertDay = LocalDate.now().plusDays(day).format(DateTimeFormatter.ofPattern("yyyy年 MM月 dd日"))
            }


            dialog?.dismiss()
        }
        if (person != null) {
            view.findViewById<TextView>(R.id.dialogLastStockText).text = person.stockDayList!![person.stockDayList!!.size - 1]!!.day
        }
        view.findViewById<TextView>(R.id.dialogTitleText).text = person?.mainText
        view.findViewById<TextView>(R.id.dialogRemainingText).text = arguments?.getInt("remaining", 0).toString()
        view.findViewById<TextView>(R.id.dialogLeadTimeText).text = person?.leadTime.toString()

        view.findViewById<ImageView>(R.id.dialogIcon)


    }

    /**
     * LayoutParamsを操作し、横幅を適用
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dialog: Dialog? = dialog

        //AttributeからLayoutParamsを求める
        val layoutParams: WindowManager.LayoutParams = dialog?.window!!.attributes

        //display metricsでdpのもと(?)を作る
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

        //LayoutParamsにdpを計算して適用(今回は横幅300dp)(※metrics.scaledDensityの返り値はfloat)
        val dialogWidth = 330 * metrics.scaledDensity
        layoutParams.width = dialogWidth.toInt()

        //LayoutParamsをセットする
        dialog.getWindow()?.attributes = layoutParams
    }


}