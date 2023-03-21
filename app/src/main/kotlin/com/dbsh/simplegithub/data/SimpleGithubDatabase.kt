package com.dbsh.simplegithub.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dbsh.simplegithub.api.model.GithubRepo

@Database(entities = [GithubRepo::class], version = 1)
abstract class SimpleGithubDatabase: RoomDatabase() {
    // DB 연결 DAO 객체 정의
    abstract fun searchHistoryDao(): SearchHistoryDao
}