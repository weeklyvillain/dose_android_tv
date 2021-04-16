package com.dose.dose.token;

import android.util.Log;

public class Token {
    private final String token;
    private final String refreshToken;
    private final double validTo;

    public Token(String token, double validTo) {
        this.token = token;
        this.validTo = validTo;
        this.refreshToken = null;
    }

    public Token (String token, String refreshToken, double validTo) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.validTo = validTo;
    }

    public boolean isTokenValid() {
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - validTo) >= -60;
    }

    public String getToken() {
        return this.token;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public double getValidTo() {
        return this.validTo;
    }
}
