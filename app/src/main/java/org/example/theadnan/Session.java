package org.example.theadnan;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    private static final String PREF = "desktop_app_session";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_ADMIN = "is_admin";

    public static void saveSession(Context ctx, String email, boolean isAdmin) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_EMAIL, email).putBoolean(KEY_IS_ADMIN, isAdmin).apply();
    }

    public static String getCurrentEmail(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_EMAIL, null);
    }

    public static boolean isAdmin(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(KEY_IS_ADMIN, false);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
