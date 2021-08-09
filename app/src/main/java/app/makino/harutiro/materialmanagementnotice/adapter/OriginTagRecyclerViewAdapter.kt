package app.makino.harutiro.materialmanagementnotice.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.R
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import io.realm.Realm

class OriginTagRecyclerViewAdapter(private val context: Context,private val listener: OnItemClickListner):
    RecyclerView.Adapter<OriginTagRecyclerViewAdapter.ViewHolder>() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    //リサイクラービューに表示するリストを宣言する
    val items: MutableList<OriginTagDateClass> = mutableListOf()

    //データをcourseDateと結びつける？？
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemEditTagText: EditText = view.findViewById(R.id.itemEditTagText)
        val dellEditButton: ImageButton = view.findViewById(R.id.dellEditButton)

    }

    //はめ込むものを指定
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_course_edittag_date_call,parent,false)
        return ViewHolder(view)
    }

    //itemsのposition番目の要素をviewに表示するコード
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.itemEditTagText.setText(item.name)

//      消去するときに呼び出し元に1temを送る
        holder.dellEditButton.setOnClickListener {
            listener.onItemClick(item)

        }
//
//        EditTextをフォーカスしているか判定
        holder.itemEditTagText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val new = realm.where(OriginTagDateClass::class.java).equalTo("id",item.id).findFirst()
                realm.executeTransaction{ new?.name = holder.itemEditTagText.text.toString() }
            }
        }
//        TODO: メンターにオートセーブについて考える。
//        Gameとかではオートセーブだと自分の変更点まで戻るということを行う時があるのだが、タグなどの簡単な文字数で少ない変更点ならば、オートセーブのほうがいちいちボタンを
//        押さなくてもよく、なおかつボタンを押し忘れて保存し忘れる問題もなくなるのではないかと考える。

//        オートセーブの方が便利だが実際に保存されたか感覚的に判断できない。
//        しかし、なにか通知してるとうざいため、理解している人はボタンも何も押さないで、保存するようにする。
//        なおかつ、動作的にボタンを押したときに保存していると考えている人も、ボタンを押したときに保存をしてなおかつフォーカスを外して保存されたことを通知するようにオートセーブ寄りの
//        セルフセーブを取り入れたほうがユーザー的にもわかりやすいのでわ無いかと改めて考えた。
//        実際GoogleのタグやYoutubeの再生リスト等もオートセーブでも動くし、ボタンでも動くようになっているため、両方できるのがいいと考える。

//        OnPauseでデータを保存するようにするとデータを消したときに変更していたデータが全部消えてしまうというのがあるため、消したときに差分だけ変更するようにすればいいのだが、
//        今はこれで動くため一旦このままで放置

//        TODO: 戻るボタンで戻ったときに保存されないバグ








    }



    //リストの要素数を返すメソッド
    override fun getItemCount(): Int {

        return items.size
    }

    // RecyclerViewの要素をタップするためのもの
    interface OnItemClickListner{
        fun onItemClick(item: OriginTagDateClass)
    }

    fun reView(){
        notifyDataSetChanged()
    }

    fun setList(list: List<OriginTagDateClass>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}