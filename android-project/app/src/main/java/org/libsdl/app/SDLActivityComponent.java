package org.libsdl.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.PointerIcon;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import static org.libsdl.app.SDLConstants.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

public class SDLActivityComponent {

    private static String TAG = SDLActivityComponent.class.getName();
    protected static Activity mSingletonActivity;
    protected static SDLActivityImplementation mSingleton;

    // This is what SDL runs in. It invokes SDL_main(), eventually
    protected static Thread mSDLThread;
    protected static boolean mSDLMainFinished = false;
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
    public static boolean setSystemCursor(int cursorID) {
        int cursor_type = 0; //PointerIcon.TYPE_NULL;
        switch (cursorID) {
            case SDL_SYSTEM_CURSOR_ARROW:
                cursor_type = 1000; //PointerIcon.TYPE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_IBEAM:
                cursor_type = 1008; //PointerIcon.TYPE_TEXT;
                break;
            case SDL_SYSTEM_CURSOR_WAIT:
                cursor_type = 1004; //PointerIcon.TYPE_WAIT;
                break;
            case SDL_SYSTEM_CURSOR_CROSSHAIR:
                cursor_type = 1007; //PointerIcon.TYPE_CROSSHAIR;
                break;
            case SDL_SYSTEM_CURSOR_WAITARROW:
                cursor_type = 1004; //PointerIcon.TYPE_WAIT;
                break;
            case SDL_SYSTEM_CURSOR_SIZENWSE:
                cursor_type = 1017; //PointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_SIZENESW:
                cursor_type = 1016; //PointerIcon.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_SIZEWE:
                cursor_type = 1014; //PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_SIZENS:
                cursor_type = 1015; //PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_SIZEALL:
                cursor_type = 1020; //PointerIcon.TYPE_GRAB;
                break;
            case SDL_SYSTEM_CURSOR_NO:
                cursor_type = 1012; //PointerIcon.TYPE_NO_DROP;
                break;
            case SDL_SYSTEM_CURSOR_HAND:
                cursor_type = 1002; //PointerIcon.TYPE_HAND;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_TOPLEFT:
                cursor_type = 1017; //PointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_TOP:
                cursor_type = 1015; //PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_TOPRIGHT:
                cursor_type = 1016; //PointerIcon.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_RIGHT:
                cursor_type = 1014; //PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_BOTTOMRIGHT:
                cursor_type = 1017; //PointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_BOTTOM:
                cursor_type = 1015; //PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_BOTTOMLEFT:
                cursor_type = 1016; //PointerIcon.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW;
                break;
            case SDL_SYSTEM_CURSOR_WINDOW_LEFT:
                cursor_type = 1014; //PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW;
                break;
        }
        if (Build.VERSION.SDK_INT >= 24 /* Android 7.0 (N) */) {
            try {
                mSurface.setPointerIcon(PointerIcon.getSystemIcon(SDL.getContext(), cursor_type));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT < 23 /* Android 6.0 (M) */) {
            SDLActivity.nativePermissionResult(requestCode, true);
            return;
        }

        Activity activity = (Activity) SDLActivity.getContext();
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{permission}, requestCode);
        } else {
            SDLActivity.nativePermissionResult(requestCode, true);
        }
    }

    public static boolean openURL(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));

            int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
            if (Build.VERSION.SDK_INT >= 21 /* Android 5.0 (LOLLIPOP) */) {
                flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
            } else {
                flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
            }
            i.addFlags(flags);

            mSingletonActivity.startActivity(i);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static boolean showToast(String message, int duration, int gravity, int xOffset, int yOffset) {
        try
        {
            class OneShotTask implements Runnable {
                private final String mMessage;
                private final int mDuration;
                private final int mGravity;
                private final int mXOffset;
                private final int mYOffset;

                OneShotTask(String message, int duration, int gravity, int xOffset, int yOffset) {
                    mMessage  = message;
                    mDuration = duration;
                    mGravity  = gravity;
                    mXOffset  = xOffset;
                    mYOffset  = yOffset;
                }

                public void run() {
                    try
                    {
                        Toast toast = Toast.makeText(mSingletonActivity, mMessage, mDuration);
                        if (mGravity >= 0) {
                            toast.setGravity(mGravity, mXOffset, mYOffset);
                        }
                        toast.show();
                    } catch(Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            }
            mSingletonActivity.runOnUiThread(new OneShotTask(message, duration, gravity, xOffset, yOffset));
        } catch(Exception ex) {
            return false;
        }
        return true;
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

