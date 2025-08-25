package org.libsdl.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.LocaleList;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.Locale;

public class SDLActivityComponent {

    private static String TAG = SDLActivityComponent.class.getName();
    private final Activity mSingleton;

    public SDLActivityComponent(Activity mSingleton) {
        this.mSingleton = mSingleton;
    }

    public static String getPreferredLocales(Locale mCurrentLocale) {
        String result = "";
        if (Build.VERSION.SDK_INT >= 24 /* Android 7 (N) */) {
            LocaleList locales = LocaleList.getAdjustedDefault();
            for (int i = 0; i < locales.size(); i++) {
                if (i != 0) result += ",";
                result += formatLocale(locales.get(i));
            }
        } else if (mCurrentLocale != null) {
            result = formatLocale(mCurrentLocale);
        }
        return result;
    }
    public static String formatLocale(Locale locale) {
        String result = "";
        String lang = "";
        if (locale.getLanguage() == "in") {
            // Indonesian is "id" according to ISO 639.2, but on Android is "in" because of Java backwards compatibility
            lang = "id";
        } else if (locale.getLanguage() == "") {
            // Make sure language is never empty
            lang = "und";
        } else {
            lang = locale.getLanguage();
        }

        if (locale.getCountry() == "") {
            result = lang;
        } else {
            result = lang + "_" + locale.getCountry();
        }
        return result;
    }

}

/**
 Simple runnable to start the SDL application
 */
class SDLMain implements Runnable {
    @Override
    public void run() {
        // Runs SDLActivity.main()

        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        } catch (Exception e) {
            Log.v("SDL", "modify thread properties failed " + e.toString());
        }

        SDLActivity.nativeInitMainThread();
        SDLActivity.mSingleton.main();
        SDLActivity.nativeCleanupMainThread();

        if (SDLActivity.mSingleton != null && !SDLActivity.mSingleton.isFinishing()) {
            // Let's finish the Activity
            SDLActivity.mSDLThread = null;
            SDLActivity.mSDLMainFinished = true;
            SDLActivity.mSingleton.finish();
        }  // else: Activity is already being destroyed

    }
}

class SDLClipboardHandler implements
    ClipboardManager.OnPrimaryClipChangedListener {

    protected ClipboardManager mClipMgr;

    SDLClipboardHandler() {
        mClipMgr = (ClipboardManager) SDL.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        mClipMgr.addPrimaryClipChangedListener(this);
    }

    public boolean clipboardHasText() {
        return mClipMgr.hasPrimaryClip();
    }

    public String clipboardGetText() {
        ClipData clip = mClipMgr.getPrimaryClip();
        if (clip != null) {
            ClipData.Item item = clip.getItemAt(0);
            if (item != null) {
                CharSequence text = item.getText();
                if (text != null) {
                    return text.toString();
                }
            }
        }
        return null;
    }

    public void clipboardSetText(String string) {
        mClipMgr.removePrimaryClipChangedListener(this);
        ClipData clip = ClipData.newPlainText(null, string);
        mClipMgr.setPrimaryClip(clip);
        mClipMgr.addPrimaryClipChangedListener(this);
    }

    @Override
    public void onPrimaryClipChanged() {
        SDLActivity.onNativeClipboardChanged();
    }
}

