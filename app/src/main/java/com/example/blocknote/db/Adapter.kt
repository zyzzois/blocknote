package com.example.blocknote.db

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.blocknote.EditActivity
import com.example.blocknote.MainActivity
import com.example.blocknote.R

//наш адаптор будет типа ViewHolder, потому что внутри будет ViewHolder который будет содержать все наши элементы View одного Итема
class Adapter(listMain: ArrayList<ListItem>, contextM: Context): RecyclerView.Adapter<Adapter.Holder>() {
    var listArray = listMain
    var context = contextM

    class Holder(itemView: View, contextV: Context) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val tvTime: TextView = itemView.findViewById(R.id.textViewTime)
        val context = contextV


        fun setData(item: ListItem) {
            tvTitle.text = item.title
            tvTime.text = item.time

                itemView.setOnClickListener {
                    //Log.d("MyLog", "АШЫПКА")
                    /*
                    this.contentResolver.takePersistableUriPermission(
                    data.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                     */
                    val intent = Intent(context, EditActivity::class.java).apply {
                        putExtra(MyIntentConstance.INTENT_TITLE_KEY, item.title)
                        putExtra(MyIntentConstance.INTENT_DESCRIPTION_KEY, item.description)
                        putExtra(MyIntentConstance.INTENT_URL_KEY, item.url)
                        putExtra(MyIntentConstance.INTENT_ID_KEY, item.id)
                    }
                    context.startActivity(intent)
                }
        }

    }

    //это функция где мы создаем наш шаблон и этот шаблон передаем в Holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)//есть infalter который может взять наш XML-файл и надуть его
        return Holder(inflater.inflate(R.layout.rc_item, parent, false), context)

    }

    //эта функция которая подключает данные с массива к шаблону который уже готов для отрисовки
    //Получаем Holder с нашими элементами View одного Итема и еще получаем позицию
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setData(listArray.get(position))
    }

    //эта функция нужна нашему адаптеру для того чтобы сообщить скольо элементов ему придется
    // наприсовать, т.е. сколько эдлементов ему придется подлкючить к нашем RCView
    override fun getItemCount(): Int {
        return listArray.size
    }

    fun updateAdapter(listItems: ArrayList<ListItem>) {
        listArray.clear() //очистим старый массив если там что-то было
        listArray.addAll(listItems) //добавляем новый массив
        notifyDataSetChanged() //сообщаем об этом Адаптеру
    }

    fun removeItem(position: Int, dbManager: MyDbManager) {
        dbManager.removeItemFromDb(listArray[position].id.toString())
        listArray.removeAt(position)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemChanged(position)
    }

}
//Каждый раз когда рисуется на экране наш элемент rc_item, то к нему подключается один ViewHolder
//Этот класс содержит в себе эти элементы котоыре там будут, в нашем случае там будет один TextView
//и по ViewHolder мы будет получать этот элемент и заполнять его чтобы туда поместить какой-нибудь
//текст.