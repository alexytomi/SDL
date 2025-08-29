package org.libsdl.app;

interface SDLApplication {
    /**
     * The application entry point, called on a dedicated thread (SDLThread).
     * The default implementation uses the getMainSharedObject() and getMainFunction() methods
     * to invoke native code from the specified shared library.
     * It can be overridden by derived classes.
     */
    void main();

    void finish();
}
