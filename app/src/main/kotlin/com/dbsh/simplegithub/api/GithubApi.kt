package com.dbsh.simplegithub.api

import retrofit2.http.GET
import com.dbsh.simplegithub.api.model.RepoSearchResponse
import com.dbsh.simplegithub.api.model.GithubRepo
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Query

// REST Api 처리부
interface GithubApi {
    // https://docs.github.com/en/rest/search?apiVersion=2022-11-28#search-repositories
    @GET("search/repositories")
    fun searchRepository(@Query("q") query: String): Observable<RepoSearchResponse>

    // https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#get-a-repository
    @GET("repos/{owner}/{name}")
    fun getRepository(
        @Path("owner") ownerLogin: String,
        @Path("name") repoName: String
    ): Observable<GithubRepo>
}