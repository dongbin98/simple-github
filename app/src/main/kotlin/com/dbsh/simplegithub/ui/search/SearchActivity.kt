package com.dbsh.simplegithub.ui.search

import androidx.appcompat.app.AppCompatActivity
import com.dbsh.simplegithub.ui.search.SearchAdapter.ItemClickListener
import com.dbsh.simplegithub.api.GithubApi
import com.dbsh.simplegithub.api.model.RepoSearchResponse
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbsh.simplegithub.api.GithubApiProvider
import com.dbsh.simplegithub.R
import com.dbsh.simplegithub.api.model.GithubRepo
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import com.dbsh.simplegithub.databinding.ActivitySearchBinding
import com.dbsh.simplegithub.ui.repo.RepositoryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), ItemClickListener {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var menuItem: MenuItem
    private lateinit var searchView: SearchView
    private lateinit var adapter: SearchAdapter
    private lateinit var api: GithubApi
    private lateinit var searchCall: Call<RepoSearchResponse>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = SearchAdapter()
        adapter.setItemClickListener(this)
        binding.rvActivitySearchList.layoutManager = LinearLayoutManager(this)
        binding.rvActivitySearchList.adapter = adapter
        api = GithubApiProvider.provideGithubApi(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuItem = menu.findItem(R.id.menu_activity_search_query)
        searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        // Expand SearchView as ActionView
        menuItem.expandActionView()
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
        val intent = Intent(this@SearchActivity, RepositoryActivity::class.java)
        intent.putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
        intent.putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
        startActivity(intent)
    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()
        searchCall = api.searchRepository(query)
        searchCall.enqueue(object : Callback<RepoSearchResponse?> {
            override fun onResponse(
                call: Call<RepoSearchResponse?>,
                response: Response<RepoSearchResponse?>
            ) {
                hideProgress()
                val repoSearchResponse = response.body()
                if (response.isSuccessful && repoSearchResponse != null) {
                    adapter.setItems(repoSearchResponse.items)
                    adapter.notifyDataSetChanged()
                    if (repoSearchResponse.totalCount == 0) {
                        showError("No search result")
                    }
                } else {
                    showError("Not successful: " + response.message())
                }
            }

            override fun onFailure(call: Call<RepoSearchResponse?>, t: Throwable) {
                hideProgress()
                showError(t.message)
            }
        })
    }

    private fun updateTitle(query: String) {
        val ab = supportActionBar
        ab?.subtitle = query
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
    }

    private fun collapseSearchView() {
        menuItem.collapseActionView()
    }

    private fun clearResults() {
        adapter.clearItems()
        adapter.notifyDataSetChanged()
    }

    private fun showProgress() {
        binding.pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        binding.tvActivitySearchMessage.text = message ?: "Unexpected error."
        binding.tvActivitySearchMessage.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvActivitySearchMessage.text = ""
        binding.tvActivitySearchMessage.visibility = View.GONE
    }
}