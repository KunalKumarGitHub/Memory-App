package com.learning.memories.models

import java.io.Serializable

data class MemoryModel(
    val id: Int,
    val title:String,
    val image:String,
    val description:String,
    val date:String,
    val location: String
):Serializable
