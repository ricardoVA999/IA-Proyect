package com.ia_proyect

import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ia_proyect.ml.JapaneseFoodModel
import com.theartofdev.edmodo.cropper.CropImage
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category


class MainActivity : AppCompatActivity() {

    lateinit var selectButton: Button
    lateinit var predButton: Button
    lateinit var camaraButton: Button
    lateinit var imgView: ImageView
    lateinit var text : TextView
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectButton = findViewById(R.id.button)
        predButton = findViewById(R.id.button2)
        imgView = findViewById(R.id.imageView)
        text = findViewById(R.id.textView)
        camaraButton = findViewById(R.id.button3)

        val labels = application.assets.open("dict.txt").bufferedReader().use { it.readText() }.split("\n")
        print(labels)

        selectButton.setOnClickListener(View.OnClickListener {
            Log.d("mssg", "button pressed")
            var intent : Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 100)
        })

        predButton.setOnClickListener(View.OnClickListener {
            var resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val model = JapaneseFoodModel.newInstance(this)

// Creates inputs for reference.
            val image = TensorImage.fromBitmap(resized)

// Runs model inference and gets result.
            val outputs = model.process(image)
            val scores = outputs.scoresAsCategoryList
            Log.d("myTag", scores.toString())

            val pred = getPred(scores)

            text.setText(pred)

// Releases model resources if no longer used.
            model.close()
        })

        camaraButton.setOnClickListener(View.OnClickListener {
            CropImage.activity().start(this)
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                imgView.setImageURI(result.uri)
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, result.uri)
            }
        }else{
            imgView.setImageURI(data?.data)
            var uri : Uri ?= data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }




    fun getPred(scores: MutableList<Category>):String{

        var pred = ""
        var max = 0.0f

        for(i in scores){
            if(max<i.score){
                pred = i.label
                max = i.score
            }
        }

        if(max>0.5){
            return (pred.capitalize()+" --- score: "+String.format("%.3f", max).toDouble())
        }else{
            return "No se encontro ninguna categoria adecuada"
        }
    }
}