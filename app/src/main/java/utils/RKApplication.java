package utils;

import android.app.Application;



public class RKApplication extends Application {

    boolean isAddDog;



    @Override
    public void onCreate() {
        super.onCreate();
        Preferences.init(this);


    }



    @Override
    public void onTerminate() {
        // Executed when the program terminates
        super.onTerminate();
    }
}
