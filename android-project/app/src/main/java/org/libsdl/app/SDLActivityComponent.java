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



}

