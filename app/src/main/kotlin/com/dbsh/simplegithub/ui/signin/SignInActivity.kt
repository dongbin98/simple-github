package com.dbsh.simplegithub.ui.signin

import androidx.appcompat.app.AppCompatActivity
import com.dbsh.simplegithub.api.AuthApi
import com.dbsh.simplegithub.data.AuthTokenProvider
import com.dbsh.simplegithub.api.model.GithubAccessToken
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.dbsh.simplegithub.api.GithubApiProvider
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.dbsh.simplegithub.BuildConfig
import com.dbsh.simplegithub.databinding.ActivitySignInBinding
import com.dbsh.simplegithub.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var api: AuthApi
    private lateinit var authTokenProvider: AuthTokenProvider
    private lateinit var accessTokenCall: Call<GithubAccessToken>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnActivitySignInStart.setOnClickListener {
            // 형식 : http://github.com/login/oauth/authorize?client_id={ ID }
            val authUri = Uri.Builder().scheme("https")
                .authority("github.com")
                .appendPath("login")
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                .build()
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        }
        api = GithubApiProvider.provideAuthApi()
        authTokenProvider = AuthTokenProvider(this)
        if (authTokenProvider.token != null) {
            launchMainActivity()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        showProgress()
        val uri = intent.data ?: throw IllegalArgumentException("No data exists")
        val code = uri.getQueryParameter("code")
            ?: throw IllegalStateException("No code exists")
        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        accessTokenCall = api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID,
            BuildConfig.GITHUB_CLIENT_SECRET,
            code)
        accessTokenCall.enqueue(object : Callback<GithubAccessToken?> {
            override fun onResponse(
                call: Call<GithubAccessToken?>,
                response: Response<GithubAccessToken?>
            ) {
                hideProgress()
                val token = response.body()
                if (response.isSuccessful && token != null) {
                    authTokenProvider.updateToken(token.accessToken)
                    launchMainActivity()
                } else {
                    showError(IllegalStateException("Not successful " + response.message()))
                }
            }

            override fun onFailure(call: Call<GithubAccessToken?>, t: Throwable) {
                hideProgress()
                showError(t)
            }
        })
    }

    private fun showProgress() {
        binding.btnActivitySignInStart.visibility = View.GONE
        binding.pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.btnActivitySignInStart.visibility = View.VISIBLE
        binding.pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(throwable: Throwable) {
        Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
    }

    private fun launchMainActivity() {
        startActivity(Intent(
            this@SignInActivity, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}