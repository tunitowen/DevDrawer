package com.owentech.DevDrawer.utils;

import android.os.AsyncTask;
import eu.chainfire.libsuperuser.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ramdroid
 * Date: 5/1/13
 * Time: 10:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class RootFeatures {

    public static void checkAccess(Listener listener) {
        new Worker(listener).execute(new Command(CHECK_ACCESS));
    }

    public static void uninstall(String packageName, Listener listener) {
        new Worker(listener).execute(new Command(UNINSTALL, packageName));
    }

    public static void clearData(String packageName, Listener listener) {
        new Worker(listener).execute(new Command(CLEAR_DATA, packageName));
    }

    public static void changeSystemLocale(String path, String language, String country, Listener listener) {
        List<String> params = new ArrayList<String>();
        params.add(path);
        params.add(language);
        params.add(country);
        new Worker(listener).execute(new Command(SYSTEM_LOCALE, params));
    }

    public static interface Listener {
        void onFinished(boolean result);
    }

    private static final int CHECK_ACCESS   = 42;
    private static final int UNINSTALL      = 43;
    private static final int CLEAR_DATA     = 44;
    private static final int SYSTEM_LOCALE  = 45;

    private static class Command {
        int id;
        List<String> params = new ArrayList<String>();

        Command(int id) {
            this.id = id;
        }

        Command(int id, String param) {
            this.id = id;
            this.params.add(param);
        }

        Command(int id, List<String> params) {
            this.id = id;
            this.params.addAll(params);
        }
    }

    private static class Worker extends AsyncTask<Command, Void, Boolean> {

        private Listener listener;

        Worker(Listener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Command... cmds) {
            boolean result = false;
            if (cmds[0].id == CHECK_ACCESS) {
                result = Shell.SU.available();
            }
            else if (cmds[0].id == UNINSTALL) {
                List<String> res = Shell.SU.run(new String[] { "pm uninstall " + cmds[0].params.get(0), "echo \"OK\"" });
                result = (res != null && res.size() > 0);
            }
            else if (cmds[0].id == CLEAR_DATA) {
                List<String> res = Shell.SU.run(new String[] { "pm clear " + cmds[0].params.get(0), "echo \"OK\"" });
                result = (res != null && res.size() > 0);
            }
            else if (cmds[0].id == SYSTEM_LOCALE) {
                String path = cmds[0].params.get(0);
                String language = cmds[0].params.get(1);
                String country = cmds[0].params.get(2);
                List<String> res = Shell.run("su",
                        new String[]{"app_process /system/bin com.owentech.DevDrawer.utils.RootLocaleSwitcher " + language + " " + country},
                        new String[]{"CLASSPATH=" + path},
                        true);
                result = (res != null && res.size() > 0);
            }
            return result;
        }

        protected void onPostExecute(Boolean result) {
            listener.onFinished(result);
        }
    }
}
