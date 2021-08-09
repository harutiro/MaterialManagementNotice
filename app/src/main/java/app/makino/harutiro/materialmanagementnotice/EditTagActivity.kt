package app.makino.harutiro.materialmanagementnotice

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.adapter.OriginTagRecyclerViewAdapter
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmResults
import java.util.*

class EditTagActivity : AppCompatActivity(){

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var adapter: OriginTagRecyclerViewAdapter? = null

    // キーボード表示を制御するためのオブジェクト
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var container: ConstraintLayout

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tag)

        title = "タグの編集"

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        container = findViewById(R.id.constraintEditTag)

        val editTextTextMultiLine = findViewById<EditText>(R.id.editTextTextMultiLine)

        val id = intent.getStringExtra("id")

        findViewById<ImageButton>(R.id.EditTagSaveButton).setOnClickListener{
            if(realm.where(OriginTagDateClass::class.java).findAll().any { it.name == editTextTextMultiLine.text.toString()}){
                val snackbar = Snackbar.make(findViewById(android.R.id.content),"重複した名前があります。", Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundResource(R.color.error)
                snackbar.show()

                return@setOnClickListener
            }

            if(editTextTextMultiLine.text.toString() != ""){
                realm.executeTransaction {
                    val new = realm.createObject(OriginTagDateClass::class.java,UUID.randomUUID().toString())
                    new?.name = editTextTextMultiLine.text.toString()
                }
            }

            editTextTextMultiLine.setText("")
            recyclerViewGo()


        }





        val rView = findViewById<RecyclerView>(R.id.rVTagEdit)
        adapter = OriginTagRecyclerViewAdapter(this , object: OriginTagRecyclerViewAdapter.OnItemClickListner{
            override fun onItemClick(item: OriginTagDateClass) {
                container.requestFocus()

                val persons = realm.where(OriginTagDateClass::class.java).equalTo("id",item.id).findFirst()
                realm.executeTransaction { persons?.deleteFromRealm() }
                recyclerViewGo()
            }
        })
        rView.layoutManager = LinearLayoutManager(this)
        rView.adapter = adapter

        recyclerViewGo()
    }

    fun recyclerViewGo(){

        val mainPersons: RealmResults<OriginTagDateClass> = realm.where(OriginTagDateClass::class.java).findAll()
        adapter?.setList(mainPersons)
    }

    // 画面タップ時に呼ばれる
    override fun onTouchEvent(event: MotionEvent): Boolean {

        // キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(container.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        // 背景にフォーカスを移す
        container.requestFocus()

        return false
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()

    }

    //戻るボタンの処理
    override fun onBackPressed() {
        // 行いたい処理
        container.requestFocus()
        finish()
    }
}