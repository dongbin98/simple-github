package com.dbsh.simplegithub.data

import android.content.Context
import androidx.room.Room

private var instance: SimpleGithubDatabase? = null

fun provideSearchHistoryDao(context: Context): SearchHistoryDao = provideDatabase(context).searchHistoryDao()

// 싱글톤 패턴
private fun provideDatabase(context: Context): SimpleGithubDatabase {
    if (instance == null) {
        instance = Room.databaseBuilder(context.applicationContext,
            SimpleGithubDatabase::class.java,
            "simple_github.db").build()
    }
    return instance!!
}