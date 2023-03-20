package com.dbsh.simplegithub.ui.search

import androidx.appcompat.app.AppCompatActivity
import com.dbsh.simplegithub.ui.search.SearchAdapter.ItemClickListener
import com.dbsh.simplegithub.api.model.RepoSearchResponse
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbsh.simplegithub.api.provideGithubApi
import com.dbsh.simplegithub.R
import com.dbsh.simplegithub.api.model.GithubRepo
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import com.dbsh.simplegithub.databinding.ActivitySearchBinding
import com.dbsh.simplegithub.extensions.plusAssign
import com.dbsh.simplegithub.ui.repo.RepositoryActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), ItemClickListener {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var menuItem: MenuItem
    private lateinit var searchView: SearchView
    private val adapter by lazy {
        SearchAdapter().apply { setItemClickListener(this@SearchActivity) }
    }
    private val api by lazy { provideGithubApi(this) }
//    private var searchCall: Call<RepoSearchResponse>? = null
    private val disposables = CompositeDisposable() // 여러 disposable 객체를 관리할 수 있는 CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.rvActivitySearchList) {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }
        binding.rvActivitySearchList.layoutManager = LinearLayoutManager(this)
        binding.rvActivitySearchList.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
//        searchCall?.run {
//            cancel()
//        }
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuItem = menu.findItem(R.id.menu_activity_search_query)
        searchView = (menuItem.actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

        // Expand SearchView as ActionView
        with(menuItem) {
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    if (searchView.query == "") finish()
                    return true
                }
            })
            expandActionView()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.menu_activity_search_query == item.itemId) {
            item.expandActionView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity(Intent(this@SearchActivity, RepositoryActivity::class.java).apply {
            putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
            putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
        })
    }

    private fun searchRepository(query: String) {
        disposables += api.searchRepository(query)
            .flatMap {
                if(it.totalCount == 0) {
                    Observable.error(IllegalStateException("No search result"))
                } else {
                    Observable.just(it.items)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                clearResults()
                hideError()
                showProgress()
            }
            .doOnTerminate {
                hideProgress()
            }
            .subscribe({ items ->
                with(adapter) {
                    setItems(items as MutableList<GithubRepo>)
                    notifyDataSetChanged()
                }
            }) {
                showError(it.message)
            }
//        clearResults()
//        hideError()
//        showProgress()
//        searchCall = api.searchRepository(query)
//        searchCall!!.enqueue(object : Callback<RepoSearchResponse?> {
//            override fun onResponse(
//                call: Call<RepoSearchResponse?>,
//                response: Response<RepoSearchResponse?>,
//            ) {
//                hideProgress()
//                val repoSearchResponse = response.body()
//                if (response.isSuccessful && repoSearchResponse != null) {
//                    with(adapter) {
//                        setItems(repoSearchResponse.items as MutableList<GithubRepo>)
//                        notifyDataSetChanged()
//                    }
//                    if (repoSearchResponse.totalCount == 0) {
//                        showError("No search result")
//                    }
//                } else {
//                    showError("Not successful: " + response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<RepoSearchResponse?>, t: Throwable) {
//                hideProgress()
//                showError(t.message)
//            }
//        })
    }

    private fun updateTitle(query: String) {
        supportActionBar?.run { subtitle = query }
    }

    private fun hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run {
            hideSoftInputFromWindow(searchView.windowToken, 0)
        }
    }

    private fun collapseSearchView() {
        menuItem.collapseActionView()
    }

    private fun clearResults() {
        with(adapter) {
            clearItems()
            notifyDataSetChanged()
        }
    }

    private fun showProgress() {
        binding.pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(binding.tvActivitySearchMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        with(binding.tvActivitySearchMessage) {
            text = ""
            visibility = View.GONE
        }
    }
}