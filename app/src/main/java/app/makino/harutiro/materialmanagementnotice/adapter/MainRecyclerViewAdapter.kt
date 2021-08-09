package app.makino.harutiro.materialmanagementnotice.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.date.MainDate
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.realm.Realm

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
        val subTextView:TextView = view.findViewById(R.id.subTextView)
        val imageView:ImageView = view.findViewById(R.id.imageView)
        val container: ConstraintLayout = view.findViewById(R.id.constraint)
        val itemTagChipGroup: ChipGroup = view.findViewById(R.id.itemTagChipGroup)
        val memoTextView:TextView = view.findViewById(R.id.memoTextView)
        val archiveButton: ImageButton = view.findViewById(R.id.archiveButton)
        val itemRemoveButton: ImageButton = view.findViewById(R.id.itemRemoveButton)



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

//        itemとレイアウトの直接の結びつけ
        val decodedByte: ByteArray = Base64.decode(item.icon, 0)
        holder.iconImageView.setImageBitmap(BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.size))
        holder.mainTextView.text = item.mainText
        holder.subTextView.text = item.subText
        holder.memoTextView.text = item.memoText

//        URLを表示するか判断するところ
        if(!(Regex("http://").containsMatchIn(item.subText) || Regex("https://").containsMatchIn(item.subText))) {
            holder.subTextView.visibility = GONE
        }

//        アーカイブの見た目の判断
        if(!item.archive){
            holder.archiveButton.setImageResource(R.drawable.archive_black_24dp)
        }else{
            holder.archiveButton.setImageResource(R.drawable.unarchive_black_24dp)
        }

//        アーカイブの動作
        holder.archiveButton.setOnClickListener{
            realm.executeTransaction{
                person?.archive = !item.archive
            }

            if(item.archive){
                listener.onReView("アーカイブしました")
            }else{
                listener.onReView("アーカイブを解除しました")
           }

        }

//        消去の動作
        holder.itemRemoveButton.setOnClickListener{
            realm.executeTransaction {
                person?.deleteFromRealm()
            }
            listener.onReView("消去しました")

        }



//        画像の結びつけ
        if(item.image != ""){
//            holder.imageView.setImageResource(item.image)
        }else{
//            holder.imageView.visibility = GONE
            holder.imageView.setImageResource(R.drawable.ramen2)
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
        fun onReView(moji: String)
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