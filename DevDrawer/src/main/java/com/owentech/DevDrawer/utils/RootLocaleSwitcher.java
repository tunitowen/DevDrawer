package com.owentech.DevDrawer.utils;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.res.Configuration;
import android.os.RemoteException;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: ramdroid
 * Date: 5/11/13
 * Time: 10:39 PM
 * To change this template use File | Settings | File Templates.
 */
public final class RootLocaleSwitcher {

    public static void main(String[] args) {
        if (args.length == 2) {
            switchLocales(args[0], args[1]);
        }
        else {
            System.err.println("invalid parameters");
        }
    }

    private static void switchLocales(String language, String country) {
        Locale locale;

        if (country != null)
        {
            locale = new Locale(language, country);
        }
        else
        {
            locale = new Locale(language);
        }

        IActivityManager am = ActivityManagerNative.getDefault();

        try {
            Configuration config = am.getConfiguration();
            config.locale = locale;
            am.updateConfiguration(config);

        } catch (RemoteException e) {
            System.err.println(e.toString());
        }

        System.out.println("Switched locale to " + language + " " + country);
    }
}
