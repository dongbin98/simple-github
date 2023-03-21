package com.dbsh.simplegithub.ui.signin

import androidx.appcompat.app.AppCompatActivity
import com.dbsh.simplegithub.data.AuthTokenProvider
import com.dbsh.simplegithub.api.model.GithubAccessToken
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.dbsh.simplegithub.api.provideAuthApi
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.dbsh.simplegithub.BuildConfig
import com.dbsh.simplegithub.databinding.ActivitySignInBinding
import com.dbsh.simplegithub.extensions.plusAssign
import com.dbsh.simplegithub.extensions.rx.AutoClearDisposable
import com.dbsh.simplegithub.ui.main.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private val api by lazy { provideAuthApi() }
    private val authTokenProvider by lazy { AuthTokenProvider(this) }
//    private var accessTokenCall: Call<GithubAccessToken>? = null
//    private val disposables = CompositeDisposable() // 여러 disposable 객체를 관리할 수 있는 CompositeDisposable
    private val disposables = AutoClearDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle += disposables

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
        if (authTokenProvider.token != null) {
            launchMainActivity()
        }
    }

//    override fun onStop() {
//        super.onStop()
////        accessTokenCall?.run {
////            cancel()
////        }
//        disposables.clear()
//    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        showProgress()
        val uri = intent.data ?: throw IllegalArgumentException("No data exists")
        val code = uri.getQueryParameter("code")
            ?: throw IllegalStateException("No code exists")
        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        disposables += api.getAccessToken(
            BuildConfig.GITHUB_CLIENT_ID,
            BuildConfig.GITHUB_CLIENT_SECRET, code)
            .map { it.accessToken } // 받은 응답에서 accessToken 만 추출
            .observeOn(AndroidSchedulers.mainThread()) // 이후 수행되는 라인은 mainThread 에서 수행 (RxAndroid 에서 제공하는 스케줄러)
            .doOnSubscribe { showProgress() }   // 구독할 때 수행할 작업
            .doOnTerminate { hideProgress() }   // 스트림이 종료될 때 수행할 작업
            .subscribe( { token ->              // observable 구독
                authTokenProvider.updateToken(token)
                launchMainActivity()
            }) {    // 에러 블록
                showError(it)
            }

//        accessTokenCall = api.getAccessToken(
//            BuildConfig.GITHUB_CLIENT_ID,
//            BuildConfig.GITHUB_CLIENT_SECRET, code)
//        accessTokenCall!!.enqueue(object : Callback<GithubAccessToken?> {
//            override fun onResponse(
//                call: Call<GithubAccessToken?>,
//                response: Response<GithubAccessToken?>,
//            ) {
//                hideProgress()
//                val token = response.body()
//                if (response.isSuccessful && token != null) {
//                    authTokenProvider.updateToken(token.accessToken)
//                    launchMainActivity()
//                } else {
//                    showError(IllegalStateException("Not successful " + response.message()))
//                }
//            }
//
//            override fun onFailure(call: Call<GithubAccessToken?>, t: Throwable) {
//                hideProgress()
//                showError(t)
//            }
//        })
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