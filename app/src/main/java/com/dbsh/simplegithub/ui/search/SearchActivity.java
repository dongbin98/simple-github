package com.dbsh.simplegithub.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.dbsh.simplegithub.R;
import com.dbsh.simplegithub.api.GithubApi;
import com.dbsh.simplegithub.api.GithubApiProvider;
import com.dbsh.simplegithub.api.model.GithubRepo;
import com.dbsh.simplegithub.api.model.RepoSearchResponse;
import com.dbsh.simplegithub.databinding.ActivitySearchBinding;
import com.dbsh.simplegithub.ui.repo.RepositoryActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.ItemClickListener {

    private ActivitySearchBinding binding;

    MenuItem menuItem;
    SearchView searchView;
    SearchAdapter adapter;
    GithubApi api;
    Call<RepoSearchResponse> searchCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new SearchAdapter();
        adapter.setItemClickListener(this);

        binding.rvActivitySearchList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvActivitySearchList.setAdapter(adapter);

        api = GithubApiProvider.provideGithubApi(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_search, menu);
        menuItem = menu.findItem(R.id.menu_activity_search_query);

        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateTitle(query);
                hideSoftKeyboard();
                collapseSearchView();
                searchRepository(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Expand SearchView as ActionView
        menuItem.expandActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(R.id.menu_activity_search_query == item.getItemId()) {
            item.expandActionView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(GithubRepo repository) {
        Intent intent = new Intent(SearchActivity.this, RepositoryActivity.class);
        intent.putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login);
        intent.putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name);
        startActivity(intent);
    }

    private void searchRepository(String query) {
        clearResults();
        hideError();
        showProgress();

        searchCall = api.searchRepository(query);
        searchCall.enqueue(new Callback<RepoSearchResponse>() {
            @Override
            public void onResponse(Call<RepoSearchResponse> call, Response<RepoSearchResponse> response) {
                hideProgress();

                RepoSearchResponse repoSearchResponse = response.body();
                if(response.isSuccessful() && repoSearchResponse != null) {
                    adapter.setItems(repoSearchResponse.items);
                    adapter.notifyDataSetChanged();

                    if(repoSearchResponse.totalCount == 0) {
                        showError("No search result");
                    }
                } else {
                    showError("Not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RepoSearchResponse> call, Throwable t) {
                hideProgress();
                showError(t.getMessage());
            }
        });
    }

    private void updateTitle(String query) {
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setSubtitle(query);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    private void collapseSearchView() {
        menuItem.collapseActionView();
    }

    private void clearResults() {
        adapter.clearItems();
        adapter.notifyDataSetChanged();
    }

    private void showProgress() {
        binding.pbActivitySearch.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        binding.pbActivitySearch.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.tvActivitySearchMessage.setText(message);
        binding.tvActivitySearchMessage.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvActivitySearchMessage.setText("");
        binding.tvActivitySearchMessage.setVisibility(View.GONE);
    }
}