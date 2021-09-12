package app.makino.harutiro.materialmanagementnotice

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.adapter.EditRecyclerViewAdapter
import app.makino.harutiro.materialmanagementnotice.date.MainDate
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.date.StockDayDate
import app.makino.harutiro.materialmanagementnotice.date.TagDateClass
import app.makino.harutiro.materialmanagementnotice.dousa.UserService
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.String as String1


class EditActivity : AppCompatActivity() {

    private val requestCode = 1000

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var subEdit:EditText? =  null
    var subIcon:ImageView? =  null
    var mainEdit:EditText? = null
    var mainIcon:ImageView? = null
    var dayText:TextView? =  null
    var dayIcon:ImageView? =  null
    var memoIcon:ImageView? = null
    var memoEdit:EditText? = null
    var image:ImageView? =    null
    var editTagChipGroup:ChipGroup? = null

    var id: String1? = ""

    var stateTagList: ArrayList<String1>? = ArrayList<String1>()

    var stateEditMode: Boolean = false

    var archive :Boolean = false

//    QRコード
    private lateinit var qrScanIntegrator: IntentIntegrator
    var janCode = ""

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        title = "編集"

        mainEdit = findViewById(R.id.mainEdit)
        mainIcon = findViewById(R.id.mainIcon)
        dayText = findViewById(R.id.dayText)
        dayIcon = findViewById(R.id.dayIcon)
        memoIcon = findViewById(R.id.memoIcon)
        memoEdit = findViewById(R.id.memoEdit)
        image = findViewById(R.id.image)
        editTagChipGroup = findViewById(R.id.editTagChipGroup)

        //スクロールできるように設定
        mainEdit?.movementMethod = ScrollingMovementMethod()
        subEdit?.movementMethod = ScrollingMovementMethod()
        memoEdit?.movementMethod = ScrollingMovementMethod()

        // MainActivityのRecyclerViewの要素をタップした場合はidが，fabをタップした場合は"空白"が入っているはず
        id = intent.getStringExtra("id")
        stateEditMode = intent.getBooleanExtra("editMode",false)

        //データのはめ込み
        if (id == null){

            //＝＝＝＝＝＝＝＝＝＝＝＝＝＝日付のはめ込み＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
            val date = Date(System.currentTimeMillis())
            val df = SimpleDateFormat("yyyy年 MM月 dd日", Locale.JAPANESE)
            val formatted = df.format(date)
            dayText?.text = formatted

        }else{

            val item = realm.where(MainDate::class.java).equalTo("id", id).findFirst()

            //＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝BASE６４の画像はめ込み＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
            val decodedByte: ByteArray = Base64.decode(item?.icon, 0)
            mainIcon?.setImageBitmap(BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.size))

            //＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝その他はめ込み＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
            dayText?.text = item?.dayText
            memoEdit?.setText(item?.memoText)
            mainEdit?.setText(item?.mainText)
            archive = item?.archive!!

            //＝＝＝＝＝＝＝＝＝＝＝＝タグのはめ込み部分＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
            for(i in item.tagList!!){
                stateTagList?.add(i.copyId.toString())
            }
            setChip()

            //==================リサイクラービュー==================================

            val adapter = EditRecyclerViewAdapter(this)
            val recycleView = findViewById<RecyclerView>(R.id.editRecyclerView)
            recycleView.layoutManager = LinearLayoutManager(this)
            recycleView.adapter = adapter

            //リサイクラービューアダプターで宣言したaddAllメソッドを呼んであげてデータも渡している
            adapter.setList(item.stockDayList!!)

        }


//    ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝消去部分
        findViewById<ImageButton>(R.id.removeButton2).setOnClickListener{
            val person = realm.where(MainDate::class.java).equalTo("id",id).findFirst()

            realm.executeTransaction {
                person?.deleteFromRealm()
            }

            finish()

        }

        findViewById<ImageButton>(R.id.shareButton).setOnClickListener{
            var putText = "【タイトル】\n"
            putText += mainEdit?.text.toString() + "\n"
            putText += "【URL】\n"
            putText += subEdit?.text.toString() + "\n"
            putText += "【内容】\n"
            putText += memoEdit?.text.toString()

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,putText )
            }
            startActivity(intent)
        }


    }

    fun save(){

        if (mainEdit?.text.toString().isNotBlank()) {
            realm.executeTransaction{
                var new: MainDate? = null
                if(id == null){
                    id = UUID.randomUUID().toString()
                    new = it.createObject(MainDate::class.java,id)
                }else{
                    new = it.where(MainDate::class.java).equalTo("id",id).findFirst()
                }

                //＝＝＝＝＝＝＝＝＝＝＝＝＝＝BASE６４＝＝＝＝＝＝＝＝＝＝＝＝＝＝
                //データ受け取り
                val bmp = (mainIcon?.drawable as BitmapDrawable).bitmap
                //エンコード
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val b: ByteArray = baos.toByteArray()
                val output = Base64.encodeToString(b, Base64.NO_WRAP)

                //====================その他データの保存=========================
                new?.icon = output
                new?.mainText = mainEdit?.text.toString()
                new?.memoText = memoEdit?.text.toString()
                new?.archive = archive

                //=====================日付==========================
                val date = Date(System.currentTimeMillis())
                val df = SimpleDateFormat("yyyy年 MM月 dd日", Locale.JAPANESE)
                val formatted = df.format(date)
                new?.dayText = formatted

                //＝＝＝＝＝＝＝＝＝＝＝＝タグの保存===================
                new?.tagList?.clear()
                for( i in stateTagList!!){
                    val tagObject = realm.createObject(TagDateClass::class.java ,UUID.randomUUID().toString()).apply {
                        this.copyId = i
                    }
                    new?.tagList?.add(tagObject)
                }

                if(new?.stockDayList?.size == 0){
                    val stock = StockDayDate(UUID.randomUUID().toString(),
                        formatted,
                        "記入",
                        0)

                    new.stockDayList?.plusAssign(stock)
                }
            }
        }
    }

    fun setChip(){
        editTagChipGroup?.removeAllViews()
        for (index in stateTagList!!) {
            val new = realm.where(OriginTagDateClass::class.java).equalTo("id",index).findFirst()


            val chip = Chip(editTagChipGroup?.context)
            chip.text= new?.name

            // necessary to get single selection working
            chip.isCloseIconVisible = true

            chip.setOnCloseIconClickListener {
                stateTagList!!.remove(index)
                setChip()
            }
            editTagChipGroup?.addView(chip)
        }
    }

    fun getGoodsName(){
        val gson: Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

        val retrofit: Retrofit =  Retrofit.Builder()
            .baseUrl("https://shopping.yahooapis.jp/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val userService: UserService = retrofit.create(UserService::class.java)

        //情報取得を別のスレッドでおこなうようにする
        runBlocking(Dispatchers.IO){
            runCatching {
                //userServiseで定義したメソッドを使ってユーザー情報を取得する
                userService.getUser(janCode)
            }
        }.onSuccess{
            //読み込んだデータをはめ込む
            mainEdit?.setText(it.hits[0].name)


        }.onFailure {
            //失敗した時のところ。
            val snackbar = Snackbar.make(findViewById(android.R.id.content),"商品名の取得に失敗しました。", Snackbar.LENGTH_SHORT)
            snackbar.view.setBackgroundResource(R.color.error)
            snackbar.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()

    }

    override fun onPause(){
        super.onPause()

        save()
    }

    //    タグインテントにおける戻りデータの受け取り部分
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            val intent = result.data

            stateTagList = intent?.getStringArrayListExtra("stateTagList")
            setChip()
        }
    }

    // 読取後に呼ばれる
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            // 読取結果はresult.contentsで参照できる
            Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
            janCode = result.contents

            getGoodsName()

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //　アプリバーの部分
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_code -> {
//            JanCode　ー＞　商品名

            this.qrScanIntegrator = IntentIntegrator(this)
            // 縦画面に固定
            this.qrScanIntegrator.setOrientationLocked(false)
            // QRコード読み取り後のビープ音を停止
            this.qrScanIntegrator.setBeepEnabled(false)
            // スキャン開始
            this.qrScanIntegrator.initiateScan()



            true
        }

        R.id.archive_settings -> {

            archive = !archive

            if(archive){
                Snackbar.make(findViewById(android.R.id.content),"ゴミ箱に移動しました", Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(findViewById(android.R.id.content),"ゴミ箱から復元しました", Snackbar.LENGTH_SHORT).show()
            }

            invalidateOptionsMenu()

            true
        }

        R.id.tag_settings ->{
            //タグへのインテント
            val intent = Intent(this , SelectTagActivity::class.java)
            intent.putExtra("stateTagList",stateTagList)
            launcher.launch(intent)

            true
        }

        android.R.id.home -> {
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_activity_menu, menu)

        val menuArchive = menu?.findItem(R.id.archive_settings)

        if(archive){
            menuArchive?.setIcon(R.drawable.restore_from_trash_117571)
        }else{
            menuArchive?.setIcon(R.drawable.delete_black_24dp__1_)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        return super.onCreateOptionsMenu(menu)
    }


}