package com.owentech.DevDrawer.data.model;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class App implements AppModel {

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App create(@Nullable Long id, @Nullable String package_, @Nullable Long filterid, @Nullable Long widgetid) {
            return new AutoValue_App(id, package_, filterid, widgetid);
        }
    };

    public static final Factory<App> FACTORY = new Factory<>(CREATOR);
    public static final Mapper<App> MAPPER = new Mapper<>(FACTORY);
}
