package com.dbsh.simplegithub.api.model;

import com.google.gson.annotations.SerializedName;

public class GithubOwner {
    public final String login;

    @SerializedName("avatar_url")
    public final String avatarUrl;

    public GithubOwner(String login, String avatarUrl) {
        this.login = login;
        this.avatarUrl = avatarUrl;
    }
}
