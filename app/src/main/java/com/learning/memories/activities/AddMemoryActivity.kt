@file:Suppress("DEPRECATION")

package com.learning.memories.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.learning.memories.R
import com.learning.memories.database.DatabaseHandler
import com.learning.memories.databinding.ActivityAddMemoryBinding
import com.learning.memories.models.MemoryModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.karumi.dexter.Dexter as Dexter1

class AddMemoryActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityAddMemoryBinding
    private var cal= Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage :Uri?=null
    private var mMemoryDetails :MemoryModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener{
            onBackPressed()
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mMemoryDetails=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as MemoryModel
        }

        dateSetListener= DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if(mMemoryDetails!=null){
            supportActionBar?.title= getString(R.string.edit_memory)

            binding.etDate.setText(mMemoryDetails!!.date)
            binding.etDescription.setText(mMemoryDetails!!.description)
            binding.etLocation.setText(mMemoryDetails!!.location)
            binding.etTitle.setText(mMemoryDetails!!.title)
            saveImageToInternalStorage=Uri.parse((mMemoryDetails!!.image))
            binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)
            binding.btnSave.text= getString(R.string.update)
        }

        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(
                    this@AddMemoryActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()

            }
            R.id.tv_add_image ->{
                val pictureDialog= AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems= arrayOf("Select photo from Gallery","Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){
                        _, which->
                    when(which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save->{
                when {
                    binding.etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter Title",Toast.LENGTH_SHORT).show()
                    }
                    binding.etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter Description",Toast.LENGTH_SHORT).show()
                    }
                    binding.etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter Location",Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage==null->{
                        Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show()
                    }else->{
                        val memoryModel=MemoryModel(
                            if(mMemoryDetails==null)0 else mMemoryDetails!!.id,
                            binding.etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString()
                        )

                        val dbHandler=DatabaseHandler(this)
                        if(mMemoryDetails==null) {
                            val addMemory=dbHandler.addMemory(memoryModel)
                            if(addMemory>0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                        else{
                            val updateMemory=dbHandler.updateMemory(memoryModel)
                            if(updateMemory>0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode== GALLERY){
                if(data!=null){
                    val contentUri=data.data
                    try{
                        val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                        saveImageToInternalStorage=saveImageToInternalStorage(selectedImageBitmap)
                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    }catch(e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddMemoryActivity,"Failed to load image from gallery",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if(requestCode== CAMERA){
                val thumbnail : Bitmap =data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage=saveImageToInternalStorage(thumbnail)
                binding.ivPlaceImage.setImageBitmap(thumbnail)
            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter1.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken) {
                showRationalDialogPermission()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery(){
        Dexter1.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken) {
                showRationalDialogPermission()
            }
        }).onSameThread().check()
    }

    private fun showRationalDialogPermission(){
        AlertDialog.Builder(this).setMessage("It looks like you have turned of permissions required!")
            .setPositiveButton("GO TO SETTINGS")
            {_,_->
                try{
                    val intent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                }catch(e:ActivityNotFoundException){
                    e.printStackTrace()
                }

            }.setNegativeButton("Cancel"){
                    dialog, _ ->dialog.dismiss()
            }.show()
    }
    private fun updateDateInView(){
        val myFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }
    private fun saveImageToInternalStorage(bitmap:Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try{
            val stream : OutputStream =FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch(e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    companion object{
        private const val GALLERY=1
        private const val CAMERA=2
        private const val IMAGE_DIRECTORY="HappyPlacesImages"
    }
}