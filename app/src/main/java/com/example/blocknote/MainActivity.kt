package com.example.blocknote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blocknote.databinding.ActivityMainBinding
import com.example.blocknote.db.Adapter
import com.example.blocknote.db.MyDbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    //Напрямую MyDbHelper мы не используем, поэтому нам нужно создать менеджер
    val myDbManager = MyDbManager(this)
    val myAdapter = Adapter(ArrayList(), this)

    private  var job: Job? = null

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initSearchView()
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManager.closeDb()
    }

    override fun onResume() {
        super.onResume()
        myDbManager.openDb()
        fillAdapter("")
    }

    fun onClickNew(view: View) {
        val i = Intent(this, EditActivity::class.java)
        startActivity(i)
    }

    //В этой функции мы будем инициализировать наш RCView
    private fun init(){
        binding.recyclerView.layoutManager = LinearLayoutManager(this) // сказали что элементы будут располагаться по верткили
        val swipeHelper = getSwapManager()
        swipeHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.adapter = myAdapter
    }

    //setOnQueryTextListener - слушатель который замечает изменения внутри searchView
    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                /*Версия до корутин
                //val list = myDbManager.readDbData(newText!!) //считывание с БД - трудоемкая операция
                //myAdapter.updateAdapter(list) //обновление адаптера это не трудоемкая операция и она запускается на основном потоке без всяких проблем.
                */
                fillAdapter(newText!!)
                return true

            }
        })
    }

    private fun fillAdapter(text: String) {

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch{ //создаем корутину которая будет работать на основном потоке
            val list = myDbManager.readDbData(text)

            myAdapter.updateAdapter(list)
            if(list.size > 0){
                binding.textViewNoElements.visibility = View.GONE
            }
            else {
                binding.textViewNoElements.visibility = View.VISIBLE

            }
        }

    }

    //Обработка свайпа
    private fun getSwapManager(): ItemTouchHelper{
        return ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT
        or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                myAdapter.removeItem(viewHolder.adapterPosition, myDbManager)
            }
        })
    }

}