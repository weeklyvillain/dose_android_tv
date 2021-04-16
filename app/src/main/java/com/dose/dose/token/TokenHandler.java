package com.dose.dose.token;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenHandler {
    private static TokenHandler tokenHandler = null;
    private Token mainToken;
    private Token contentToken;

    private TokenHandler() {

    }

    public static TokenHandler Tokenhandler(Context context) {
        // Setup saved token data if the singleton hasn't been initiated yet
        if (tokenHandler == null) {
            tokenHandler = new TokenHandler();

            SharedPreferences settings = context.getSharedPreferences("UserInfo", 0);
            Token mainToken = new Token(
                    settings.getString("MainServerJWT", ""),
                    settings.getString("MainServerRefreshToken", ""),
                    settings.getLong("MainServerValidTo", 0));
            Token contentToken = new Token(
                    settings.getString("ContentServerJWT", ""),
                    settings.getLong("ContentServerValidTo", 0));

            tokenHandler.setMainToken(mainToken, context);
            tokenHandler.setContentToken(contentToken, context);
        }
        return tokenHandler;
    }

    public void setMainToken(Token token, Context context) {
        SharedPreferences settings = context.getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();

        if (token != null) {
            editor.putString("MainServerJWT", token.getToken());
            editor.putString("MainServerRefreshToken", token.getRefreshToken());
            editor.putLong("MainServerValidTo", (long) token.getValidTo());
        } else {
            editor.remove("MainServerJWT");
            editor.remove("MainServerRefreshToken");
            editor.remove("MainServerValidTo");
        }
        editor.apply();
        this.mainToken = token;
    }

    public void setContentToken(Token token, Context context) {
        SharedPreferences settings = context.getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();

        if (token != null) {
            editor.putString("ContentServerJWT", token.getToken());
            editor.putLong("ContentServerValidTo", (long) token.getValidTo());
        } else {
            editor.remove("ContentServerJWT");
            editor.remove("ContentServerValidTo");
            editor.remove("ContentServerURL");

        }
        editor.apply();
        this.contentToken = token;
    }

    public Token getMainToken() {
        return this.mainToken;
    }

    public Token getContentToken() {
        return this.contentToken;
    }
}
