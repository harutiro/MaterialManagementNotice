package app.makino.harutiro.materialmanagementnotice

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.makino.harutiro.materialmanagementnotice.adapter.MainRecyclerViewAdapter
import app.makino.harutiro.materialmanagementnotice.date.MainDate
import app.makino.harutiro.materialmanagementnotice.date.OriginTagDateClass
import app.makino.harutiro.materialmanagementnotice.dousa.CustomDialogFlagment
import app.makino.harutiro.materialmanagementnotice.dousa.JapaneseChange
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.threetenabp.AndroidThreeTen
import io.realm.Realm
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var adapter: MainRecyclerViewAdapter? = null
    var serchTagChipGroup:ChipGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ????????????????????????????????????????????????????????????
        val versionNow = PackageInfoCompat.getLongVersionCode(this.packageManager.getPackageInfo(this.packageName, 0))

        val sp: SharedPreferences = getSharedPreferences("DateStore", Context.MODE_PRIVATE)
        val vCode: Int = sp.getInt("VersionCode", 1)

        if(versionNow > vCode){
            val editor = sp.edit()
            editor.putInt("VersionCode", versionNow.toInt())
            editor.apply()

            updateWebView()


//            ???????????????????????????????????????????????????????????????????????????
            val mainDate = realm.where(MainDate::class.java).findAll()

            realm.executeTransaction {
                for (person in mainDate){
                    for ((i,value) in person.stockDayList!!.withIndex()){
                        if (i == 0){ value.state = "??????"}
                        if (i == 1){ value.state = "??????"
                                     value.interval = 0}
                    }

                    var leadTime = 0.0
                    for (i in person?.stockDayList!!.drop(2)){
                        Log.d("debug",i.interval.toString())
                        leadTime += i.interval
                    }

                    val parameter = if((person.stockDayList!!.size - 2) > 0){
                        person.stockDayList!!.size - 2
                    }else{
                        1
                    }

                    person.leadTime = leadTime / (parameter)
                }
            }

        }



//        AdMob??????
        MobileAds.initialize(this)
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest: AdRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        serchTagChipGroup = findViewById(R.id.serchTagChipGroup)

//        ?????????????????????????????????????????????
        val settingSp = PreferenceManager.getDefaultSharedPreferences(this)
        val settingState = settingSp.getBoolean("alarm",false)

        if(!settingState){
            AndroidThreeTen.init(this)
            val new = realm.where(MainDate::class.java).findAll()
            var number = 0

            for(i in new){
                if(i.alertDay.isNotEmpty() && i.leadTime >= 1.0 && LocalDate.now().plusDays(1).isAfter(LocalDate.parse(i?.alertDay, DateTimeFormatter.ofPattern("yyyy??? MM??? dd???")))){
                    number++

                    val dialog = CustomDialogFlagment()
                    val args = Bundle()

                    args.putInt("remaining",number)
                    args.putString("id",i.id)
                    dialog.arguments = args

                    dialog.show(supportFragmentManager,"customDialog")
                }
            }
        }


        findViewById<EditText>(R.id.searchEditText).doOnTextChanged{ _, _, _, _ ->
            recyclerViewGo()
        }

        findViewById<FloatingActionButton>(R.id.puraFAB).setOnClickListener{
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("editMode", true)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.dellButton).setOnClickListener{
            findViewById<EditText>(R.id.searchEditText).setText("")
            recyclerViewGo()
        }

        val rView = findViewById<RecyclerView>(R.id.RView)
        adapter = MainRecyclerViewAdapter(this , object: MainRecyclerViewAdapter.OnItemClickListner{
            override fun onItemClick(item: MainDate) {
                // SecondActivity????????????????????????Intent
                val intent = Intent(applicationContext, EditActivity::class.java)
                // RecyclerView??????????????????????????????intent?????????SecondActivity???????????????
                // ??????????????????id???SecondActivity?????????
                intent.putExtra("id", item.id)
                startActivity(intent)
            }

            override fun onArchiveReView(id:String,moji:String) {
                Snackbar.make(findViewById(android.R.id.content),moji, Snackbar.LENGTH_SHORT)
                    .setAction("????????????"){
                        val person = realm.where(MainDate::class.java).equalTo("id",id).findFirst()
                        realm.executeTransaction {
                            person?.archive = !person?.archive!!
                        }
                        recyclerViewGo()
                    }
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity,R.color.themeColor))
                    .show()

                recyclerViewGo()
            }

            override fun onRemoveReView() {
                Snackbar.make(findViewById(android.R.id.content),"??????????????????", Snackbar.LENGTH_SHORT).show()

                recyclerViewGo()
            }

            override fun onStockReView(moji:String) {
                Snackbar.make(findViewById(android.R.id.content),moji, Snackbar.LENGTH_SHORT)
                    .show()

                recyclerViewGo()
            }
        })
        rView.layoutManager = LinearLayoutManager(this)
        rView.adapter = adapter
    }

    var tagState:Boolean = true

    override fun onResume() {
        super.onResume()

        //TODO: ?????????????????????????????????????????????????????????????????????????????????
        if(tagState){
            setChip()
            tagState = false
        }
        recyclerViewGo()
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()

    }

    fun updateWebView(){
        val tabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setToolbarColor(ContextCompat.getColor(this, R.color.themeColor_Light))
            .setStartAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .build()

        // Chrome?????????
        tabsIntent.launchUrl(this, Uri.parse(getString(R.string.NewURL)))
    }

    fun setChip(){
        serchTagChipGroup?.removeAllViews()
        val new = realm.where(OriginTagDateClass::class.java).findAll()

        //???????????????????????????????????????????????????
        val firstChip = Chip(serchTagChipGroup?.context)

        firstChip.text= "?????????"

        // necessary to get single selection working
        firstChip.isCheckable = true
        firstChip.isClickable = true
        firstChip.checkedIcon = ContextCompat.getDrawable(this,R.drawable.ic_mtrl_chip_checked_black)

        firstChip.setOnCheckedChangeListener { _, _ ->
            recyclerViewGo()
        }

        serchTagChipGroup?.addView(firstChip)

        for (i in new) {

            val chip = Chip(serchTagChipGroup?.context)

            chip.text= i.name

            // necessary to get single selection working
            chip.isCheckable = true
            chip.isClickable = true
            chip.checkedIcon = ContextCompat.getDrawable(this,R.drawable.ic_mtrl_chip_checked_black)

            chip.setOnCheckedChangeListener { _, _ ->
                recyclerViewGo()
            }

            serchTagChipGroup?.addView(chip)
        }
    }


    @Suppress("DEPRECATION")
    fun recyclerViewGo(){

        //Realm?????????????????????????????????
        val word = findViewById<EditText>(R.id.searchEditText).text.toString()
        var realmResalt: MutableList<MainDate> = realm.where(MainDate::class.java).findAll().toMutableList()

        //???????????????????????????
        val mutableNewPerson: MutableList<MainDate> = mutableListOf()
        val stateTagList = mutableListOf<String>()

        //????????????????????????????????????
        for(i in serchTagChipGroup?.checkedChipIds!!){
            stateTagList.add(findViewById<Chip>(i).text.toString())
        }

        //????????????????????????
        if(stateTagList.contains("?????????")){
            realmResalt = realmResalt.filter{it.archive}.toMutableList()
            stateTagList.remove("?????????")

            title = "?????????"

        }else{
            realmResalt = realmResalt.filter{!it.archive}.toMutableList()

            title = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy??? MM??? dd???"))
        }

        if(realmResalt.size > 0){
            findViewById<ImageView>(R.id.hintImage).visibility = GONE
            findViewById<TextView>(R.id.hintText).visibility = GONE
        }else{
            findViewById<ImageView>(R.id.hintImage).visibility = VISIBLE
            findViewById<TextView>(R.id.hintText).visibility = VISIBLE
        }

        var mutablePerson: MutableList<MainDate> = realmResalt


        //????????????????????????
        if(word != ""){
            val all = word.split(" ","???")

            for(p in all){

                //????????????????????????????????????????????????
                //??????????????????????????????????????????????????????
                val filterMain = mutablePerson.filter{Regex(JapaneseChange().converted(p.toLowerCase(Locale.ROOT)))
                    .containsMatchIn(JapaneseChange().converted(it.mainText.toLowerCase(Locale.ROOT)))
                }

                val filterMemo = mutablePerson.filter{Regex(JapaneseChange().converted(p.toLowerCase(Locale.ROOT)))
                    .containsMatchIn(JapaneseChange().converted(it.memoText.toLowerCase(Locale.ROOT)))}

                mutablePerson = (filterMain + filterMemo) as MutableList<MainDate>


            }
        }

        var tempItemTagList = mutableListOf<String>()
        val realmStateResult = mutableListOf<String>()

        //tag??????????????????
        for(data in mutablePerson){

            tempItemTagList = mutableListOf()
            for(tag in data.tagList!!){
                tempItemTagList.add(tag.copyId.toString())
            }
            for( item in stateTagList){
                realmStateResult.add(realm.where(OriginTagDateClass::class.java)
                    .equalTo("name",item)
                    .findFirst()
                    ?.id.toString())
            }
            if(tempItemTagList.containsAll(realmStateResult)) mutableNewPerson.add(data)

        }

        adapter?.setList(mutableNewPerson.distinct())


        if(mutableNewPerson.size == 0 && realmResalt.size > 0){
            findViewById<ImageView>(R.id.hintImage).visibility = VISIBLE
            findViewById<TextView>(R.id.hintText).visibility = VISIBLE

            findViewById<ImageView>(R.id.hintImage).setImageResource(R.drawable._290859_close_delete_document_note_shut_down_icon)
            findViewById<TextView>(R.id.hintText).text = "???????????????????????????????????????????????????\nm(_ _)m"

        }else{
            findViewById<ImageView>(R.id.hintImage).setImageResource(R.drawable.description_black_24dp__1_)
            findViewById<TextView>(R.id.hintText).text = "????????????????????????????????????????????????"
        }




    }

    //???????????????????????????
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
//            updateWebView()
            val intent = Intent(this,SettingsActivity::class.java)
            startActivity(intent)

            true
        }

        R.id.create_tag_settings ->{
            tagState = true
            val intent = Intent(this,EditTagActivity::class.java)
            startActivity(intent)

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
        inflater.inflate(R.menu.main_activity_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    //????????????????????????
    override fun onBackPressed() {
        // ??????????????????
        //????????????????????????????????????????????????
        val view = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.bottom_sheet)
        val mBottomSheetBehavior = BottomSheetBehavior.from(view)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}