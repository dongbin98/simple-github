package com.dbsh.simplegithub.api

import android.content.Context
import com.dbsh.simplegithub.data.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.IllegalStateException

object GithubApiProvider {
    // get accessToken
    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
            .baseUrl("https://github.com/")
            .client(provideOkHttpClient(provideLoggingInterceptor(), null))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create<AuthApi>(AuthApi::class.java)
    }

    // get Repository or search Repository
    fun provideGithubApi(context: Context): GithubApi {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(provideOkHttpClient(provideLoggingInterceptor(), provideAuthInterceptor(
                provideAuthTokenProvider(context))))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubApi::class.java)
    }

    // set OkHttpClient
    private fun provideOkHttpClient(
        interceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor?,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (authInterceptor != null) {
            builder.addInterceptor(authInterceptor)
        }
        builder.addInterceptor(interceptor)
        return builder.build()
    }

    // set HttpLoggingInterceptor -> Log that Network Request or Response
    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    // set AuthInterceptor -> add accessToken at Header
    private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
        val token: String =
            provider.token ?: throw IllegalStateException("authToken cannot be null")
        return AuthInterceptor(token)
    }

    private fun provideAuthTokenProvider(context: Context): AuthTokenProvider {
        return AuthTokenProvider(context.applicationContext)
    }

    // custom Interceptor -> accessToken add Impl
    internal class AuthInterceptor(private val token: String) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original: Request = chain.request()

            // add token at header
            val builder = original.newBuilder()
                .addHeader("Authorization", "token $token")
            val request: Request = builder.build()
            return chain.proceed(request)
        }
    }
}