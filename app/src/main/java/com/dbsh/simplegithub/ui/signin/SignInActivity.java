package com.dbsh.simplegithub.ui.signin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.*;
import com.dbsh.simplegithub.BuildConfig;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.dbsh.simplegithub.api.AuthApi;
import com.dbsh.simplegithub.api.GithubApiProvider;
import com.dbsh.simplegithub.api.model.GithubAccessToken;
import com.dbsh.simplegithub.data.AuthTokenProvider;
import com.dbsh.simplegithub.databinding.ActivitySignInBinding;
import com.dbsh.simplegithub.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    AuthApi api;
    AuthTokenProvider authTokenProvider;
    Call<GithubAccessToken> accessTokenCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnActivitySignInStart.setOnClickListener(v -> {
            // 형식 : http://github.com/login/oauth/authorize?client_id={ ID }
            Uri authUri = new Uri.Builder().scheme("https")
                    .authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build();

            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
            intent.launchUrl(SignInActivity.this, authUri);
        });

        api = GithubApiProvider.provideAuthApi();
        authTokenProvider = new AuthTokenProvider(this);

        if(authTokenProvider.getToken() != null) {
            launchMainActivity();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        showProgress();

        Uri uri = intent.getData();
        if(uri == null) {
            throw new IllegalArgumentException("No data exists");
        }

        String code = uri.getQueryParameter("code");
        if(code == null) {
            throw new IllegalStateException("No code exists");
        }
        getAccessToken(code);
    }

    private void getAccessToken(@NonNull String code) {
        accessTokenCall = api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code);

        accessTokenCall.enqueue(new Callback<GithubAccessToken>() {
            @Override
            public void onResponse(Call<GithubAccessToken> call, Response<GithubAccessToken> response) {
                hideProgress();

                GithubAccessToken token = response.body();
                if(response.isSuccessful() && token != null) {
                    authTokenProvider.updateToken(token.accessToken);
                    launchMainActivity();
                } else {
                    showError(new IllegalStateException("Not successful " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<GithubAccessToken> call, Throwable t) {
                hideProgress();
                showError(t);
            }
        });
    }

    private void showProgress() {
        binding.btnActivitySignInStart.setVisibility(View.GONE);
        binding.pbActivitySignIn.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        binding.btnActivitySignInStart.setVisibility(View.VISIBLE);
        binding.pbActivitySignIn.setVisibility(View.GONE);
    }

    private void showError(Throwable throwable) {
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void launchMainActivity() {
        startActivity(new Intent(
                SignInActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}