package com.owentech.DevDrawer.utils;

import com.squareup.otto.Bus;

/**
 * Created by tonyowen on 09/07/2014.
 */
public class OttoManager {
    public static Bus instance;

    public static Bus getInstance(){
        if (instance == null){
            instance = new Bus();
        }
        return instance;
    }
}
