package app.makino.harutiro.materialmanagementnotice

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.date.MainDate
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.date.TagDateClass
import app.makino.harutiro.materialmanagementnotice.R
import app.makino.harutiro.materialmanagementnotice.adapter.EditRecyclerViewAdapter
import app.makino.harutiro.materialmanagementnotice.date.StockDayDate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.realm.Realm
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
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







//＝＝＝＝＝＝＝＝＝＝＝＝＝＝Realm保存部分
        findViewById<FloatingActionButton>(R.id.saveFAB).setOnClickListener {
            if (mainEdit?.text.toString().isBlank()) {
                val snackbar = Snackbar.make(findViewById(android.R.id.content),"タイトルが入力されていません。", Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundResource(R.color.error)
                snackbar.show()
            }else{
//                Snackbar.make(findViewById(android.R.id.content),"保存しました。", Snackbar.LENGTH_SHORT).show()
                save()
            }
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
        realm.executeTransaction{

            val new = if(id == null){
                it.createObject(MainDate::class.java,UUID.randomUUID().toString())
            }else{
                it.where(MainDate::class.java).equalTo("id",id).findFirst()
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

            if(id == null){
                val stock: StockDayDate = StockDayDate(UUID.randomUUID().toString(),
                    formatted,
                    "最初",
                    0)

                new?.stockDayList?.plusAssign(stock)
            }

            finish()
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

    //戻るボタンの処理
    override fun onBackPressed() {
        // 行いたい処理
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("ホームへ戻る")
            .setMessage("入力したデータを保存しないでホームに戻りますか？")
            .setPositiveButton("OK") { _, _ ->
                //Yesが押された時の挙動
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Noが押された時の挙動
            }
            .show()
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()

    }

    //    タグインテントにおける戻りデータの受け取り部分
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            val intent = result.data

            stateTagList = intent?.getStringArrayListExtra("stateTagList")
            setChip()
        }
    }

    //　アプリバーの部分
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //ボトムシートを上に浮き上がらせる
//            val view = findViewById<ConstraintLayout>(R.id.edit_bottom_sheet)
//            val mBottomSheetBehavior = BottomSheetBehavior.from(view)
//            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

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


        return super.onCreateOptionsMenu(menu)
    }


}