package org.libsdl.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public abstract class SDLActivity extends Activity implements View.OnSystemUiVisibilityChangeListener, SDLComponentReceiver, SDLConstants {
    private SDLActivityComponent component;

    protected String[] getLibraries() {
        return null;
    }
    protected String[] getArguments() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        component = new SDLActivityComponent(this);
        String[] libraries = getLibraries();
        if (libraries != null) {
            component.setLibraries(libraries);
        }
        String[] args = getArguments();
        if (args != null) {
            component.setArguments(args);
        }

        component.onCreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        component.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        component.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        component.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        component.onStart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        component.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        component.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        component.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        component.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (component.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        component.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return SDLActivityComponent.dispatchKeyEvent(event, this);
    }

    public static boolean dispatchingKeyEvent() {
        return SDLActivityComponent.mDispatchingKeyEvent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        component.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void superOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        component.onSystemUiVisibilityChange(visibility);
    }

    protected static SDLGenericMotionListener_API14 getMotionListener(){
        return SDLActivityComponent.getMotionListener();
    }

    protected boolean sendCommand(int command, Object data) {
        return component.sendCommand(command, data);
    }

    /**
     * This method returns the name of the application entry point
     * It can be overridden by derived classes.
     */
    protected String getMainFunction() {
        return component.getMainFunction();
    }

    /**
     * This method returns the name of the shared object with the application entry point
     * It can be overridden by derived classes.
     */
    protected String getMainSharedObject() {
        return component.getMainSharedObject();
    }

    /**
     * This method is called by SDL if SDL did not handle a message itself.
     * This happens if a received message contains an unsupported command.
     * Method can be overwritten to handle Messages in a different class.
     * @param command the command of the message.
     * @param param the parameter of the message. May be null.
     * @return if the message was handled in overridden method.
     */
    protected boolean onUnhandledMessage(int command, Object param) {
        return component.onUnhandledMessage(command, param);
    }

    protected SDLSurface createSDLSurface(Context context) {
        return component.createSDLSurface(context);
    }

    protected void main() {
        component.main();
    }

    protected void pauseNativeThread() {
        component.pauseNativeThread();
    }

    protected void resumeNativeThread() {
        component.resumeNativeThread();
    }
}
