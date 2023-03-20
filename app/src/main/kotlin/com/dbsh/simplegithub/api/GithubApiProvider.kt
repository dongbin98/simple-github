package com.dbsh.simplegithub.api

import android.content.Context
import com.dbsh.simplegithub.data.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.IllegalStateException

// 패키지 단위 함수로 변환, 단일 표현식 변환, 범위 지정 함수

// get accessToken
fun provideAuthApi(): AuthApi = Retrofit.Builder()
    .baseUrl("https://github.com/")
    .client(provideOkHttpClient(provideLoggingInterceptor(), null))
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())  // retrofit response를 observable 형태로 변환
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create<AuthApi>(AuthApi::class.java)


// get Repository or search Repository
fun provideGithubApi(context: Context): GithubApi = Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .client(provideOkHttpClient(provideLoggingInterceptor(), provideAuthInterceptor(
        provideAuthTokenProvider(context))))
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())  // retrofit response를 observable 형태로 변환
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(GithubApi::class.java)

// set OkHttpClient
private fun provideOkHttpClient(
    interceptor: HttpLoggingInterceptor,
    authInterceptor: AuthInterceptor?,
): OkHttpClient {
    return OkHttpClient.Builder().run {
        if(authInterceptor != null) {
            addInterceptor(authInterceptor)
        }
        addInterceptor(interceptor)
        build()
    }
}

// set HttpLoggingInterceptor -> Log that Network Request or Response
private fun provideLoggingInterceptor(): HttpLoggingInterceptor =  HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
}

// set AuthInterceptor -> add accessToken at Header
private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
    val token: String =
        provider.token ?: throw IllegalStateException("authToken cannot be null")
    return AuthInterceptor(token)
}

private fun provideAuthTokenProvider(context: Context): AuthTokenProvider =
    AuthTokenProvider(context.applicationContext)


// custom Interceptor -> accessToken add Impl
internal class AuthInterceptor(private val token: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
        val newRequest = request().newBuilder().run {
            addHeader("Authorization", "token $token")
            build()
        }
        proceed(newRequest)
    }
}
