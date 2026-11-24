package com.amit.amitclip.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clips",
    indices = [Index(value = ["text"], unique = true)]
)
data class Clip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val type: String = "text"
)
