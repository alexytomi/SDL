package org.libsdl.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

public class SDLActivityComponent {

    private static String TAG = SDLActivityComponent.class.getName();
    protected static Activity mSingletonActivity;
    protected static SDLActivityImplementation mSingleton;

    public SDLActivityComponent(Activity mSingleton) {
        if (SDLActivityComponent.mSingleton != null) throw new IllegalStateException("Only one activity can handle SDL at a time!");
        if (SDLActivityComponent.mSingletonActivity != null) throw new IllegalStateException("Only one activity can handle SDL at a time!");
        if (mSingleton == null) throw new IllegalStateException("Activity cannot be null!");
        if (!(mSingleton instanceof SDLActivityImplementation)) throw new IllegalStateException(("Activity must implement SDLActivityImplementation!"));
        SDLActivityComponent.mSingletonActivity = mSingleton;
        SDLActivityComponent.mSingleton = (SDLActivityImplementation) mSingleton;
    }
    private static boolean gotSingleton(){
        return mSingleton != null && mSingletonActivity != null;
    }

    public static int openFileDescriptor(String uri, String mode) throws Exception {
        if (gotSingleton()) {
            return -1;
        }

        try {
            ParcelFileDescriptor pfd = mSingletonActivity.getContentResolver().openFileDescriptor(Uri.parse(uri), mode);
            return pfd != null ? pfd.detachFd() : -1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Intent makeFileDialogIntent(String[] filters, boolean allowMultiple, boolean forWrite) {
        /* Convert string list of extensions to their respective MIME types */
        ArrayList<String> mimes = new ArrayList<>();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (filters != null) {
            for (String pattern : filters) {
                String[] extensions = pattern.split(";");

                if (extensions.length == 1 && extensions[0].equals("*")) {
                    /* Handle "*" special case */
                    mimes.add("*/*");
                } else {
                    for (String ext : extensions) {
                        String mime = mimeTypeMap.getMimeTypeFromExtension(ext);
                        if (mime != null) {
                            mimes.add(mime);
                        }
                    }
                }
            }
        }
        /* Display the file dialog */
        Intent intent = new Intent(forWrite ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
        switch (mimes.size()) {
            case 0:
                intent.setType("*/*");
                break;
            case 1:
                intent.setType(mimes.get(0));
                break;
            default:
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes.toArray(new String[]{}));
        }
        return intent;
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
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        } catch (Exception e) {
            Log.v("SDL", "modify thread properties failed " + e.toString());
        }

        SDLActivity.nativeInitMainThread();
        SDLActivityComponent.mSingleton.main();
        SDLActivity.nativeCleanupMainThread();

        if (SDLActivityComponent.mSingleton != null && !SDLActivity.mSingleton.isFinishing()) {
            // Let's finish the Activity
            SDLActivityComponent.mSDLThread = null;
            SDLActivityComponent.mSDLMainFinished = true;
            SDLActivityComponent.mSingleton.finish();
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

