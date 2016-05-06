package it.unibs.appwow;

import android.app.Application;
import android.content.Context;

/**
 * Created by Massi on 06/05/2016.
 */
public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
