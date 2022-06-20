package com.example.blocknote

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.blocknote.databinding.ActivityEditBinding
import com.example.blocknote.db.MyDbManager
import com.example.blocknote.db.MyIntentConstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    lateinit var bindingEditActivity: ActivityEditBinding

    val myDbManager = MyDbManager(this)
    var isEditState = false
    private var launcher: ActivityResultLauncher<Intent>? = null
    var id = 0
    var tempImageUrl = "empty"

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        bindingEditActivity = ActivityEditBinding.inflate(layoutInflater)
        setContentView(bindingEditActivity.root)

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    bindingEditActivity.imageView.setImageURI(result.data?.data)
                    tempImageUrl = result.data?.data.toString()
                    contentResolver.takePersistableUriPermission(
                        result.data?.data!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        getMyIntents()
    }

    override fun onResume() {
        super.onResume()
        myDbManager.openDb()
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManager.closeDb()
    }

    fun onClickAddImage(view: View) = with(bindingEditActivity) {
        myDbManager.openDb()
        imageLayout.visibility = View.VISIBLE
        buttonAddImage.visibility = View.GONE
    }

    fun OnClickDeleteImage(view: View) = with(bindingEditActivity) {
        imageView.visibility = View.INVISIBLE
        buttonAddImage.visibility = View.VISIBLE
        tempImageUrl = "empty"
    }

    fun onClickChooseImage(view: View) {
        //launcher?.launch(Intent.ACTION_PICK)
        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        launcher?.launch(intent)
    }


    fun OnClickSave(view: View) = with(bindingEditActivity) {
        val myTitle = editTitle.text.toString()
        val myDescription = editDescription.text.toString()
        if (myTitle != "" && myDescription != "") {
            CoroutineScope(Dispatchers.Main).launch {

                if (isEditState) {
                    myDbManager.editItemFromDb(myTitle, myDescription, tempImageUrl, id, getCurrentTime())
                    Toast.makeText(baseContext, "edited and saved", Toast.LENGTH_SHORT).show()
                } else {
                    myDbManager.insertToDb(myTitle, myDescription, tempImageUrl, getCurrentTime())
                    Toast.makeText(baseContext, "saved", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }

    }


    fun getMyIntents() = with(bindingEditActivity) {
        buttonEditNote.visibility = View.GONE
        val i = intent
        if (i != null) {

            if (i.getStringExtra(MyIntentConstance.INTENT_TITLE_KEY) != null) {
                isEditState = true
                editTitle.isEnabled = false
                editDescription.isEnabled = false
                buttonEditNote.visibility = View.VISIBLE
                editTitle.setText(i.getStringExtra(MyIntentConstance.INTENT_TITLE_KEY))
                editDescription.setText(i.getStringExtra(MyIntentConstance.INTENT_DESCRIPTION_KEY))
                id = i.getIntExtra(MyIntentConstance.INTENT_ID_KEY, 0)
                if (i.getStringExtra(MyIntentConstance.INTENT_URL_KEY) != "empty") {
                    tempImageUrl = i.getStringExtra(MyIntentConstance.INTENT_URL_KEY)!!
                    imageLayout.visibility = View.VISIBLE
                    imageView.setImageURI(Uri.parse(tempImageUrl))
                    buttonDeleteImage.visibility = View.GONE
                    imageChooseImage.visibility = View.GONE
                }
            }
        }
    }

    fun onClickEditText(view: View) = with(bindingEditActivity) {
        buttonEditNote.visibility = View.GONE
        editTitle.isEnabled = true
        editDescription.isEnabled = true
        buttonAddImage.visibility = View.VISIBLE
        if (tempImageUrl == "empty") return
        imageChooseImage.visibility = View.VISIBLE
        buttonDeleteImage.visibility = View.VISIBLE
    }

    //функция которая возвращает реальное время
    private fun getCurrentTime(): String {
        val time = Calendar.getInstance().time
        val formater = SimpleDateFormat("dd-MM-yy kk:mm",Locale.getDefault())
        return formater.format(time)
    }

}