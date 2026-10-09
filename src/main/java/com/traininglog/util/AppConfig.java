package com.traininglog.util;

public class AppConfig {

    public static final String GOOGLE_CLIENT_ID = "293011053545-3nu9dptriqju9g80tnolf6lo0s8svoot.apps.googleusercontent.com";

    public static final String ANTHROPIC_API_KEY =
        System.getenv("ANTHROPIC_API_KEY") == null ? "" : System.getenv("ANTHROPIC_API_KEY");

    private AppConfig() { }
}
