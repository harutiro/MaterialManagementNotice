package app.makino.harutiro.materialmanagementnotice

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.adapter.OriginTagSelectRecyclerViewAdapter
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.R
import io.realm.Realm
import io.realm.RealmResults
import java.util.*

class SelectTagActivity : AppCompatActivity() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var adapter: OriginTagSelectRecyclerViewAdapter? = null
    var stateTagList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tag)

        title = "タグの選択"

        stateTagList = intent.getStringArrayListExtra("stateTagList") as ArrayList<String>

        val rView = findViewById<RecyclerView>(R.id.selectTagRV)
        adapter = OriginTagSelectRecyclerViewAdapter(this , object: OriginTagSelectRecyclerViewAdapter.OnItemClickListner{
            override fun onItemClick(item: OriginTagDateClass,state:Boolean) {

                if(state){
                    stateTagList.add(item.id.toString())
                }else{
                    stateTagList.remove(item.id.toString())
                }
            }
        })
        rView.layoutManager = LinearLayoutManager(this)
        rView.adapter = adapter

        rVGo()
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()

    }

    //戻るボタンの処理
    override fun onBackPressed() {
        //        タグデータを受け渡す
        val intent = Intent()
        intent.putExtra("stateTagList",stateTagList)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    fun rVGo(){

        val mainPersons: RealmResults<OriginTagDateClass> = realm.where(OriginTagDateClass::class.java).findAll()
        adapter?.setList(mainPersons,stateTagList)
    }
}