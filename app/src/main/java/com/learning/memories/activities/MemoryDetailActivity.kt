@file:Suppress("DEPRECATION")

package com.learning.memories.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.learning.memories.databinding.ActivityMemoryDetailBinding
import com.learning.memories.models.MemoryModel

class MemoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemoryDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivityMemoryDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)

        var memoryDetailModel:MemoryModel?=null

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            memoryDetailModel=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as MemoryModel
        }

        if(memoryDetailModel!=null){
            setSupportActionBar(binding.toolbarMemoryDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=memoryDetailModel.title
            binding.toolbarMemoryDetail.setNavigationOnClickListener {
                onBackPressed()
            }

            binding.ivPlaceImage.setImageURI(Uri.parse(memoryDetailModel.image))
            binding.tvDescription.text=memoryDetailModel.description
            binding.tvLocation.text=memoryDetailModel.location
        }
    }
}