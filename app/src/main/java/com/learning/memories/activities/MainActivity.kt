package com.learning.memories.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.learning.memories.adapters.memoryAdapter
import com.learning.memories.database.DatabaseHandler
import com.learning.memories.databinding.ActivityMainBinding
import com.learning.memories.models.MemoryModel
import com.learning.memories.utils.SwipeToDeleteCallback
import com.learning.memories.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view=binding.root
        setContentView(view)
        binding.fabAddMemory.setOnClickListener {
            val intent = Intent(this@MainActivity, AddMemoryActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getMemoriesListFromLocalDB()
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                getMemoriesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }
    private fun getMemoriesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this@MainActivity)

        val getHappyPlacesList = dbHandler.getMemoriesList()

        if (getHappyPlacesList.size > 0) {
            binding.rvMemoriesList.visibility = View.VISIBLE
            binding.tvNoRecordsAvailable.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            binding.rvMemoriesList.visibility = View.GONE
            binding.tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }
    private fun setupHappyPlacesRecyclerView(memoriesList: ArrayList<MemoryModel>) {

        binding.rvMemoriesList.layoutManager = LinearLayoutManager(this)
        binding.rvMemoriesList.setHasFixedSize(true)

        val memoriesAdapter = memoryAdapter(this, memoriesList)
        binding.rvMemoriesList.adapter = memoriesAdapter

        memoriesAdapter.setOnCLickListener(object :
            memoryAdapter.OnClickListener {
            override fun onClick(position: Int, model: MemoryModel) {
                val intent = Intent(this@MainActivity, MemoryDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvMemoriesList.adapter as memoryAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvMemoriesList)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvMemoriesList.adapter as memoryAdapter
                adapter.notifyDeleteItem(viewHolder.adapterPosition)
                getMemoriesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvMemoriesList)
    }

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}