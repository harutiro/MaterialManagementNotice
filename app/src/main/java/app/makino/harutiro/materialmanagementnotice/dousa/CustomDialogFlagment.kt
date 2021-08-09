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




class CustomDialogFlagment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_flagment, container, false)
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