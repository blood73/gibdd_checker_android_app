package ru.bloodsoft.gibddchecker.util;

import android.util.Log;
import com.crashlytics.android.Crashlytics;

public class LogUtil {
    private static final String LOG_PREFIX = "gibdd_checker";

    private static final boolean LOGGING_ENABLED = true;
    private static final int MAX_TAG_LENGTH = 23;
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();

    public static String makeLogTag(Class clazz) {
        return makeLogTag(clazz.getSimpleName());
    }


    public static String makeLogTag(String str) {
        if (str.length() > MAX_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }


    public static void logD(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.DEBUG, tag, message);
        }
    }

    public static void logD(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.DEBUG, tag, message);
        }
    }

    public static void logV(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Crashlytics.log(Log.VERBOSE, tag, message);
            }
        }
    }

    public static void logV(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Crashlytics.log(Log.VERBOSE, tag, message);
            }
        }
    }

    public static void logI(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.INFO, tag, message);
        }
    }

    public static void logI(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.INFO, tag, message);
        }
    }

    public static void logW(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.WARN, tag, message);
        }
    }

    public static void logW(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.WARN, tag, message);
        }
    }

    public static void logE(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.ERROR, tag, message);
        }
    }

    public static void logE(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Crashlytics.log(Log.ERROR, tag, message);
        }
    }

    /**
     * Utility class
     */
    private LogUtil() {
    }
}