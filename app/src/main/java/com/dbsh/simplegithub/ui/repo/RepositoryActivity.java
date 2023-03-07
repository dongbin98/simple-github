package com.dbsh.simplegithub.ui.repo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.dbsh.simplegithub.R;
import com.dbsh.simplegithub.api.GithubApi;
import com.dbsh.simplegithub.api.GithubApiProvider;
import com.dbsh.simplegithub.api.model.GithubRepo;
import com.dbsh.simplegithub.databinding.ActivityRepositoryBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepositoryActivity extends AppCompatActivity {

    private ActivityRepositoryBinding binding;

    public static final String KEY_USER_LOGIN = "user_login";
    public static final String KEY_REPO_NAME = "repo_name";

    GithubApi api;
    Call<GithubRepo> repoCall;

    SimpleDateFormat dateFormatInResponse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
    SimpleDateFormat dateFormatToShow = new SimpleDateFormat("yyyy년 M월 d일 H시 m분", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepositoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = GithubApiProvider.provideGithubApi(this);

        String login = getIntent().getStringExtra(KEY_USER_LOGIN);
        if(login == null) {
            throw new IllegalArgumentException("No login info exists in extras");
        }
        String repo = getIntent().getStringExtra(KEY_REPO_NAME);
        if(repo == null) {
            throw new IllegalArgumentException("No repo info exists in extras");
        }
        showRepositoryInfo(login, repo);
    }

    private void showRepositoryInfo(String login, String repo) {
        showProgress();

        repoCall = api.getRepository(login, repo);
        repoCall.enqueue(new Callback<GithubRepo>() {
            @Override
            public void onResponse(Call<GithubRepo> call, Response<GithubRepo> response) {
                hideProgress(true);

                GithubRepo repo = response.body();
                if(response.isSuccessful() && repo != null) {
                    Glide.with(RepositoryActivity.this)
                            .load(repo.owner.avatarUrl)
                            .into(binding.ivActivityRepositoryProfile);

                    binding.tvActivityRepositoryName.setText(repo.fullName);
                    binding.tvActivityRepositoryStars.setText(getResources().getQuantityString(R.plurals.star, repo.stars, repo.stars));

                    if(repo.description == null) {
                        binding.tvActivityRepositoryDescription.setText("No description provided");
                    } else {
                        binding.tvActivityRepositoryDescription.setText(repo.description);
                    }

                    if(repo.language == null) {
                        binding.tvActivityRepositoryLanguage.setText("No language specified");
                    } else {
                        binding.tvActivityRepositoryLanguage.setText(repo.language);
                    }

                    System.out.println(repo.updatedAt);
                    try {
                        Date lastUpdate = dateFormatInResponse.parse(repo.updatedAt);
                        binding.tvActivityRepositoryLastUpdate.setText(dateFormatToShow.format(lastUpdate));
                    } catch (ParseException e) {
                        binding.tvActivityRepositoryLastUpdate.setText("unknown");
                    }
                } else {
                    showError("Not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GithubRepo> call, Throwable t) {
                hideProgress(false);
                showError(t.getMessage());
            }
        });
    }

    private void showProgress() {
        binding.ActivityRepositoryGroup.setVisibility(View.GONE);
        binding.pbActivityRepository.setVisibility(View.VISIBLE);
    }

    private void hideProgress(boolean isSuceed) {
        binding.ActivityRepositoryGroup.setVisibility(isSuceed ? View.VISIBLE : View.GONE);
        binding.pbActivityRepository.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.tvActivityRepositoryMessage.setText(message);
        binding.tvActivityRepositoryMessage.setVisibility(View.VISIBLE);
    }
}