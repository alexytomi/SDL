package org.libsdl.app;

import static org.libsdl.app.SDL.getContext;

import android.app.Activity;
import android.util.Log;

interface SDLActivityImplementation {
    /**
     * The application entry point, called on a dedicated thread (SDLThread).
     * The default implementation uses the getMainSharedObject() and getMainFunction() methods
     * to invoke native code from the specified shared library.
     * It can be overridden by derived classes.
     */
    void main();

    void finish();
}
