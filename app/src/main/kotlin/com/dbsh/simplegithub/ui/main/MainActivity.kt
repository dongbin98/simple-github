package com.dbsh.simplegithub.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbsh.simplegithub.R
import com.dbsh.simplegithub.api.model.GithubRepo
import com.dbsh.simplegithub.data.provideSearchHistoryDao
import com.dbsh.simplegithub.databinding.ActivityMainBinding
import com.dbsh.simplegithub.extensions.plusAssign
import com.dbsh.simplegithub.extensions.runOnIoScheduler
import com.dbsh.simplegithub.extensions.rx.AutoClearedDisposable
import com.dbsh.simplegithub.ui.repo.RepositoryActivity
import com.dbsh.simplegithub.ui.search.SearchActivity
import com.dbsh.simplegithub.ui.search.SearchAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private val adapter by lazy { SearchAdapter().apply { setItemClickListener(this@MainActivity) }}
    private val searchHistoryDao by lazy { provideSearchHistoryDao(this) }
    private val disposables = AutoClearedDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycle += disposables
        lifecycle += LifecycleEventObserver { _, event ->
            if(event == Lifecycle.Event.ON_START) {
                fetchSearchHistory()
            }
        }

        binding.btnActivityMainSearch.setOnClickListener {
            startActivity(Intent(this@MainActivity,
                SearchActivity::class.java))
        }

        with(binding.rvActivityMainList) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(R.id.menu_activity_main_clear_all == item.itemId) {
            clearAll()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity(Intent(this@MainActivity, RepositoryActivity::class.java).apply {
            putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
            putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
        })
    }

    private fun fetchSearchHistory(): Disposable = searchHistoryDao.getHistory()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ items ->
            with(adapter) {
                setItems(items as MutableList<GithubRepo>)
                notifyDataSetChanged()
            }
            if(items.isEmpty()) {
                Toast.makeText(this, "No recent repository", Toast.LENGTH_SHORT).show()
            } else {
                hideMessage()
            }
        }) {
            showMessage(it.message)
        }

    private fun clearAll() {
        disposables += runOnIoScheduler { searchHistoryDao.clearAll() }
    }

    private fun showMessage(message: String?) {
        with(binding.tvActivityMainMessage) {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage() {
        with(binding.tvActivityMainMessage) {
            text = ""
            visibility = View.GONE
        }
    }
}