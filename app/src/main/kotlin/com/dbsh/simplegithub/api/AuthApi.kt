package com.dbsh.simplegithub.api

import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import com.dbsh.simplegithub.api.model.GithubAccessToken
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Field
import retrofit2.http.Headers

// Access Token 처리부
interface AuthApi {
    // https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps
    @FormUrlEncoded
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): Observable<GithubAccessToken>
}