package com.owentech.DevDrawer.utils;

import android.os.AsyncTask;
import eu.chainfire.libsuperuser.Shell;

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

    public static void clearCache(String packageName, Listener listener) {
        new Worker(listener).execute(new Command(CLEAR_CACHE, packageName));
    }

    public static interface Listener {
        void onFinished(boolean result);
    }

    private static final int CHECK_ACCESS   = 42;
    private static final int UNINSTALL      = 43;
    private static final int CLEAR_CACHE    = 44;

    private static class Command {
        int id;
        String param;

        Command(int id) {
            this.id = id;
        }

        Command(int id, String param) {
            this.id = id;
            this.param = param;
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
                List<String> res = Shell.SU.run(new String[] { "pm uninstall " + cmds[0].param, "echo \"OK\"" });
                result = (res != null && res.size() > 0);
            }
            else if (cmds[0].id == CLEAR_CACHE) {
                List<String> res = Shell.SU.run(new String[] { "pm clear " + cmds[0].param, "echo \"OK\"" });
                result = (res != null && res.size() > 0);
            }
            return result;
        }

        protected void onPostExecute(Boolean result) {
            listener.onFinished(result);
        }
    }
}
