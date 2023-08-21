package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Admin on 2/15/2016.
 */
public class Preferences {
    public static final String AUTH_TOKEN = "Authorization";
    private static SharedPreferences sharedPref;
    private static final String PREF_NAME = "MunchePreference";
    public static final String CURRENT_RUID = "CURRENT_RUID";
    public static final String CurrentAddress = "CurrentAddress";
    public static final String COMPANY_NAME = "CompanyName";
    public static final String PROFILE_PHOTO = "profile_pic";
    public static final int DEPTID = 0;
    public static final String APP_USER_NAME = "AppUserName";
    public static final String DEPARTMENT = "department";
    public static final String USER_CODE = "userCode";
    public static final String USER_NAME = "UserName";
    public static final String PASSWORD = "password";
    public static final String CONFIRM_PASSWORD = "confirmpassword";
    public static final String MOBILE = "mobile";
    public static final String DOB = "dob";
    public static final String GENDER = "gender";
    public static final String DOCTOR_NAME = "doctorName";
    public static final String QUALIFICATION = "qualification";
    public static final String ADDRESS = "address";
    public static final String USER_ID = "userid";
    public static final String DOC_ID = "doc_id";
    public static final String LUSER_ID = "luserId";
    public static final String DOC_IMG = "docImg";
    public static final String MAC_ID = "macId";


    public static void init(Context context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String saveValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
        return key;
    }

    public static void saveValue(String key, long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }


    public static void saveValue(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void saveValue(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void saveNoti(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static String getString(String key) {
        return sharedPref.getString(key, "");
    }

    public static int getInt(String key) {
        return sharedPref.getInt(key, 0);
    }

    public static int getDays(String key) {
        return sharedPref.getInt(key, 90);
    }

    public static long getLong(String key) {
        return sharedPref.getLong(key, 0L);
    }

    public static boolean getBoolean(String key) {
        return sharedPref.getBoolean(key, false);
    }

    public static boolean getNoti(String key) {
        return sharedPref.getBoolean(key, true);
    }

    public static void clearAll() {
        sharedPref.edit().clear().apply();
    }

    public static void clear(String key) {


    }


}
