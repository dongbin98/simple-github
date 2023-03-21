package com.dbsh.simplegithub.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dbsh.simplegithub.api.model.GithubRepo
import io.reactivex.rxjava3.core.Flowable

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(repo: GithubRepo)

    @Query("SELECT * FROM repositories")
    fun getHistory(): Flowable<List<GithubRepo>> // Flowable 형태로 반환하여 DB 변경 시 알림을 받아 새로운 자료를 가져옴

    @Query("DELETE FROM repositories")
    fun clearAll()
}