package com.adiamas.umpireassistant.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "teams",
    foreignKeys = [ForeignKey(
        entity = StoredConfigEntity::class,
        parentColumns = ["id"],
        childColumns = ["configId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(index = true) val configId: Int,
    val name: String,
)
