package com.dbsh.simplegithub.api.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "repositories")
class GithubRepo(
    val name: String,

    @field:SerializedName("full_name")
    @PrimaryKey @ColumnInfo(name = "full_name") val fullName: String,

    @Embedded val owner: GithubOwner,

    val description: String?, val language: String?,

    @field:SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
    @field:SerializedName("stargazers_count") val stars: Int,
)