package com.owentech.DevDrawer.data.model;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Filter implements FilterModel {

    public static final Creator<Filter> CREATOR = new Creator<Filter>() {
        @Override
        public Filter create(@Nullable Long id, @Nullable String package_, @Nullable Long widgetid) {
            return new AutoValue_Filter(id, package_, widgetid);
        }
    };

    public static final Factory<Filter> FACTORY = new Factory<>(CREATOR);
    public static final Mapper<Filter> MAPPER = new Mapper<>(FACTORY);

}
