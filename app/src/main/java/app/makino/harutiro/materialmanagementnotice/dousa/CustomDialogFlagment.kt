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
import android.widget.TextView


class CustomDialogFlagment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_flagment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.dialogOkButton).setOnClickListener{
            dialog?.dismiss()
        }
        view.findViewById<TextView>(R.id.dialogLastStockText).text = arguments?.getString("lastStockDay", "")
        view.findViewById<TextView>(R.id.dialogTitleText).text = arguments?.getString("title", "")
        view.findViewById<TextView>(R.id.dialogRemainingText).text = arguments?.getInt("remaining", 0).toString()
        view.findViewById<TextView>(R.id.dialogLeadTimeText).text = arguments?.getDouble("leadTime", 0.0).toString()

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