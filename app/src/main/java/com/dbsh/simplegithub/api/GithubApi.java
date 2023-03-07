package com.dbsh.simplegithub.api;

import com.dbsh.simplegithub.api.model.GithubRepo;
import com.dbsh.simplegithub.api.model.RepoSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

// REST Api 처리부
public interface GithubApi {
    // https://docs.github.com/en/rest/search?apiVersion=2022-11-28#search-repositories
    @GET("search/repositories")
    Call<RepoSearchResponse> searchRepository(@Query("q") String query);

    // https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#get-a-repository
    @GET("repos/{owner}/{name}")
    Call<GithubRepo> getRepository(@Path("owner") String ownerLogin, @Path("name") String repoName);
}
