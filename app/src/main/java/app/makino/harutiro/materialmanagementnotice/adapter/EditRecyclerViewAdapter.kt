package app.makino.harutiro.materialmanagementnotice.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.R
import app.makino.harutiro.materialmanagementnotice.date.StockDayDate

class EditRecyclerViewAdapter(private val context: Context):
    RecyclerView.Adapter<EditRecyclerViewAdapter.ViewHolder>() {

    //リサイクラービューに表示するリストを宣言する
    val items: MutableList<StockDayDate> = mutableListOf()

    //データをcourseDateと結びつける？？
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val stockItemDayText: TextView = view.findViewById(R.id.stockItemDayText)
        val stockItemIntervalText: TextView = view.findViewById(R.id.stockItemIntervalText)
        val stockItemStateText: TextView = view.findViewById(R.id.stockItemStateText)
    }

    //はめ込むものを指定
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_course_stock_day_date_call,parent,false)
        return ViewHolder(view)
    }

    //itemsのposition番目の要素をviewに表示するコード
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.stockItemDayText.text = item.day
        holder.stockItemIntervalText.text = item.interval.toString()
        holder.stockItemStateText.text = item.state

    }

    fun setList(list: List<StockDayDate>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    //リストの要素数を返すメソッド
    override fun getItemCount(): Int {

        return items.size
    }
}