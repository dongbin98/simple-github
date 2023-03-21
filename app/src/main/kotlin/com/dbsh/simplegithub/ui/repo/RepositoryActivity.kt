package com.dbsh.simplegithub.ui.repo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.dbsh.simplegithub.api.model.GithubRepo
import android.os.Bundle
import com.dbsh.simplegithub.api.provideGithubApi
import com.bumptech.glide.Glide
import android.view.View
import com.dbsh.simplegithub.R
import com.dbsh.simplegithub.databinding.ActivityRepositoryBinding
import com.dbsh.simplegithub.extensions.plusAssign
import com.dbsh.simplegithub.extensions.rx.AutoClearDisposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RepositoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    private lateinit var binding: ActivityRepositoryBinding
    private val api by lazy { provideGithubApi(this) }
//    private var repoCall: Call<GithubRepo>? = null
//    private val disposables = CompositeDisposable()
    private val disposables = AutoClearDisposable(this)

    private val dateFormatInResponse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    private val dateFormatToShow = SimpleDateFormat("yyyy년 M월 d일 H시 m분", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle += disposables

        val login = intent.getStringExtra(KEY_USER_LOGIN)
            ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
            ?: throw IllegalArgumentException("No repo info exists in extras")
        showRepositoryInfo(login, repo)
    }

//    override fun onStop() {
//        super.onStop()
////        repoCall?.run {
////            cancel()
////        }
//        disposables.clear()
//    }

    private fun showRepositoryInfo(login: String, repoName: String) {
        disposables += api.getRepository(login, repoName)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showProgress() }
            .doOnError { hideProgress(false) }
            .doOnComplete { hideProgress(true) }
            .subscribe ({ repo ->
                Glide.with(this@RepositoryActivity)
                        .load(repo.owner.avatarUrl)
                        .into(binding.ivActivityRepositoryProfile)
                    binding.tvActivityRepositoryName.text = repo.fullName
                    binding.tvActivityRepositoryStars.text =
                        resources.getQuantityString(R.plurals.star,
                            repo.stars,
                            repo.stars)
                    if (repo.description == null) {
                        binding.tvActivityRepositoryDescription.text = "No description provided"
                    } else {
                        binding.tvActivityRepositoryDescription.text = repo.description
                    }
                    if (repo.language == null) {
                        binding.tvActivityRepositoryLanguage.text = "No language specified"
                    } else {
                        binding.tvActivityRepositoryLanguage.text = repo.language
                    }
                    println(repo.updatedAt)
                    try {
                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                        binding.tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(
                            lastUpdate)
                    } catch (e: ParseException) {
                        binding.tvActivityRepositoryLastUpdate.text = "unknown"
                    }
            }) {
                showError(it.message)
            }
//        showProgress()
//        repoCall = api.getRepository(login, repoName)
//        repoCall!!.enqueue(object : Callback<GithubRepo?> {
//            @SuppressLint("SetTextI18n")
//            override fun onResponse(call: Call<GithubRepo?>, response: Response<GithubRepo?>) {
//                hideProgress(true)
//                val repo = response.body()
//                if (response.isSuccessful && repo != null) {
//                    Glide.with(this@RepositoryActivity)
//                        .load(repo.owner.avatarUrl)
//                        .into(binding.ivActivityRepositoryProfile)
//                    binding.tvActivityRepositoryName.text = repo.fullName
//                    binding.tvActivityRepositoryStars.text =
//                        resources.getQuantityString(R.plurals.star,
//                            repo.stars,
//                            repo.stars)
//                    if (repo.description == null) {
//                        binding.tvActivityRepositoryDescription.text = "No description provided"
//                    } else {
//                        binding.tvActivityRepositoryDescription.text = repo.description
//                    }
//                    if (repo.language == null) {
//                        binding.tvActivityRepositoryLanguage.text = "No language specified"
//                    } else {
//                        binding.tvActivityRepositoryLanguage.text = repo.language
//                    }
//                    println(repo.updatedAt)
//                    try {
//                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
//                        binding.tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(
//                            lastUpdate)
//                    } catch (e: ParseException) {
//                        binding.tvActivityRepositoryLastUpdate.text = "unknown"
//                    }
//                } else {
//                    showError("Not successful: " + response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<GithubRepo?>, t: Throwable) {
//                hideProgress(false)
//                showError(t.message)
//            }
//        })
    }

    private fun showProgress() {
        binding.ActivityRepositoryGroup.visibility = View.GONE
        binding.pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        binding.ActivityRepositoryGroup.visibility = if (isSucceed) View.VISIBLE else View.GONE
        binding.pbActivityRepository.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(binding.tvActivityRepositoryMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }
}