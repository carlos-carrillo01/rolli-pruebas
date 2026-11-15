package com.diego.rolli;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class RateLimiter {
    private static final String PREF_NAME = "rolli_rate_limit_prefs";
    private static final String KEY_ADMIN_FAILS = "admin_fail_count";
    private static final String KEY_ADMIN_LOCK_UNTIL = "admin_lock_until_ms";
    private static final String KEY_CODE_FAIL_PREFIX = "code_fails_";
    private static final String KEY_CODE_LOCK_PREFIX = "code_lock_";

    private static final int MAX_AUTH_ATTEMPTS = 5;
    private static final long BASE_LOCK_MS = 60_000L;
    private static final long MAX_LOCK_MS = 15 * 60_000L;

    private final SharedPreferences prefs;

    public RateLimiter(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAdminLocked() {
        return getAdminLockRemainingSeconds() > 0;
    }

    public long getAdminLockRemainingSeconds() {
        long lockUntil = prefs.getLong(KEY_ADMIN_LOCK_UNTIL, 0L);
        long remainingMs = lockUntil - System.currentTimeMillis();
        return remainingMs <= 0L ? 0L : (long) Math.ceil(remainingMs / 1000.0);
    }

    public void recordAdminFailure() {
        int fails = prefs.getInt(KEY_ADMIN_FAILS, 0) + 1;
        SharedPreferences.Editor editor = prefs.edit().putInt(KEY_ADMIN_FAILS, fails);
        if (fails >= MAX_AUTH_ATTEMPTS) {
            int multiplier = Math.min(4, fails - MAX_AUTH_ATTEMPTS);
            long lockDuration = Math.min(MAX_LOCK_MS, BASE_LOCK_MS << multiplier);
            editor.putLong(KEY_ADMIN_LOCK_UNTIL, System.currentTimeMillis() + lockDuration);
        }
        editor.apply();
    }

    public void resetAdminFailures() {
        prefs.edit()
                .remove(KEY_ADMIN_FAILS)
                .remove(KEY_ADMIN_LOCK_UNTIL)
                .apply();
    }

    public boolean isOrderCodeLocked(String orderId) {
        return getOrderCodeLockRemainingSeconds(orderId) > 0;
    }

    public long getOrderCodeLockRemainingSeconds(String orderId) {
        if (TextUtils.isEmpty(orderId)) return 0L;
        long lockUntil = prefs.getLong(KEY_CODE_LOCK_PREFIX + orderId, 0L);
        long remainingMs = lockUntil - System.currentTimeMillis();
        return remainingMs <= 0L ? 0L : (long) Math.ceil(remainingMs / 1000.0);
    }

    public void recordOrderCodeFailure(String orderId) {
        if (TextUtils.isEmpty(orderId)) return;
        int fails = prefs.getInt(KEY_CODE_FAIL_PREFIX + orderId, 0) + 1;
        SharedPreferences.Editor editor = prefs.edit().putInt(KEY_CODE_FAIL_PREFIX + orderId, fails);
        if (fails >= MAX_AUTH_ATTEMPTS) {
            editor.putLong(KEY_CODE_LOCK_PREFIX + orderId, System.currentTimeMillis() + BASE_LOCK_MS);
        }
        editor.apply();
    }

    public void resetOrderCodeFailures(String orderId) {
        if (TextUtils.isEmpty(orderId)) return;
        prefs.edit()
                .remove(KEY_CODE_FAIL_PREFIX + orderId)
                .remove(KEY_CODE_LOCK_PREFIX + orderId)
                .apply();
    }
}
