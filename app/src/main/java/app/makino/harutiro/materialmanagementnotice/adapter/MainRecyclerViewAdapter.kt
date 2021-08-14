package app.makino.harutiro.materialmanagementnotice.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.R
import app.makino.harutiro.materialmanagementnotice.date.MainDate
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.date.StockDayDate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.jakewharton.threetenabp.AndroidThreeTen
import io.realm.Realm
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToLong

class MainRecyclerViewAdapter(private val context: Context,private val listener: OnItemClickListner):
    RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder>() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    //リサイクラービューに表示するリストを宣言する
    val items: MutableList<MainDate> = mutableListOf()

    //データをcourseDateと結びつける？？
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
        val mainTextView: TextView = view.findViewById(R.id.mainTextView)
        val container: ConstraintLayout = view.findViewById(R.id.constraint)
        val itemTagChipGroup: ChipGroup = view.findViewById(R.id.itemTagChipGroup)
        val archiveButton: ImageButton = view.findViewById(R.id.archiveButton)
        val itemRemoveButton: ImageButton = view.findViewById(R.id.itemRemoveButton)
        val stockButton: Button =view.findViewById(R.id.stockButton)
        val lastStackDayText:TextView = view.findViewById(R.id.lastStackDayText)
        val leadDayTimeText:TextView = view.findViewById(R.id.leadDayTimeText)




    }

    //はめ込むものを指定
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_course_maindate_call,parent,false)
        return ViewHolder(view)
    }

    //itemsのposition番目の要素をviewに表示するコード
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val person = realm.where(MainDate::class.java).equalTo("id",item.id).findFirst()

        // MainActivity側でタップしたときの動作を記述するため，n番目の要素を渡す
        holder.container.setOnClickListener { listener.onItemClick(item) }

        if(item.alertDay.isNotEmpty() && item.stockDayList?.size!! > 1 && LocalDate.now().isEqual(LocalDate.parse(item.stockDayList!![item.stockDayList!!.size - 1]?.day, DateTimeFormatter.ofPattern("yyyy年 MM月 dd日")))){
            holder.stockButton.setBackgroundColor(ContextCompat.getColor(context, R.color.textGray))
            holder.stockButton.text = "取り消し"

            holder.stockButton.setOnClickListener {
                val today = LocalDate.now()

                var leadTime = 0.0
                for (i in person?.stockDayList!!){
                    leadTime += i.interval
                }
                leadTime /= (person.stockDayList!!.size)

                realm.executeTransaction(){
                    person.stockDayList?.minusAssign(person.stockDayList?.get(person.stockDayList!!.size - 1))
                    person.leadTime = leadTime
                    person.alertDay = today.plusDays(leadTime.roundToLong()).format(DateTimeFormatter.ofPattern("yyyy年 MM月 dd日"))

                }
                listener.onStockReView("取り消しました")

            }
        }else {
            holder.stockButton.setBackgroundColor(ContextCompat.getColor(context, R.color.themeColor_Light))
            holder.stockButton.text = "入荷"


            holder.stockButton.setOnClickListener {

                AndroidThreeTen.init(context)
                val stockPerson = person?.stockDayList?.get(person.stockDayList!!.size - 1)
                val lastLocalDate = LocalDate.parse(stockPerson?.day, DateTimeFormatter.ofPattern("yyyy年 MM月 dd日"))
                val today = LocalDate.now()

                val stock = StockDayDate(UUID.randomUUID().toString(),
                    today.format(DateTimeFormatter.ofPattern("yyyy年 MM月 dd日")),
                    "入荷",
                    ChronoUnit.DAYS.between(lastLocalDate,today))

                var leadTime:Double = ChronoUnit.DAYS.between(lastLocalDate,today).toDouble()
                for (i in person?.stockDayList!!){
                    leadTime += i.interval
                }
                leadTime /= (person.stockDayList!!.size)

                realm.executeTransaction {
                    person.stockDayList?.plusAssign(stock)
                    person.leadTime = leadTime
                    person.alertDay = today.plusDays(leadTime.roundToLong()).format(DateTimeFormatter.ofPattern("yyyy年 MM月 dd日"))
                }

                listener.onStockReView("入荷しました")
            }
        }


//        itemとレイアウトの直接の結びつけ
        val decodedByte: ByteArray = Base64.decode(item.icon, 0)
        holder.iconImageView.setImageBitmap(BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.size))
        holder.mainTextView.text = item.mainText
        holder.lastStackDayText.text = item.stockDayList?.get(person?.stockDayList!!.size - 1)?.day
        holder.leadDayTimeText.text = String.format("%.1f",item.leadTime)

//        アーカイブの見た目の判断
        if(!item.archive){
            holder.archiveButton.setImageResource(R.drawable.delete_black_24dp__1_)
            holder.itemRemoveButton.visibility = GONE

        }else{
            holder.archiveButton.setImageResource(R.drawable.restore_from_trash_117571)
            holder.itemRemoveButton.visibility = VISIBLE
        }

//        アーカイブの動作
        holder.archiveButton.setOnClickListener{
            realm.executeTransaction{
                person?.archive = !item.archive
            }

            if(item.archive){
                listener.onArchiveReView(item.id.toString(),"ゴミ箱に移動しました")
            }else{
                listener.onArchiveReView(item.id.toString(),"ゴミ箱から復元しました")
           }

        }

//        消去の動作
        holder.itemRemoveButton.setOnClickListener{
            realm.executeTransaction {
                person?.deleteFromRealm()
            }
            listener.onRemoveReView(item.id.toString())

        }

        //chip関係
        holder.itemTagChipGroup.removeAllViews()

        for (index in item.tagList!!.toMutableList()) {
            val new = realm.where(OriginTagDateClass::class.java).equalTo("id",index.copyId).findFirst()

            if(new == null){
                realm.executeTransaction{
                    index.deleteFromRealm()
                }
            }else{
                val chip = Chip(holder.itemTagChipGroup.context)
                chip.text= new.name
                chip.isClickable = false
                holder.itemTagChipGroup.addView(chip)
            }
        }



    }

    //リストの要素数を返すメソッド
    override fun getItemCount(): Int {

        return items.size
    }

    // RecyclerViewの要素をタップするためのもの
    interface OnItemClickListner{
        fun onItemClick(item: MainDate)
        fun onArchiveReView(id:String,moji:String)
        fun onRemoveReView(id:String)
        fun onStockReView(moji:String)
    }

    fun reView(){
        notifyDataSetChanged()
    }

    fun setList(list: List<MainDate>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}