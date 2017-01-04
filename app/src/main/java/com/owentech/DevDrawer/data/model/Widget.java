package com.owentech.DevDrawer.data.model;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Widget implements WidgetModel {

    public static final Creator<Widget> CREATOR = new Creator<Widget>() {
        @Override
        public Widget create(@Nullable Long id, @Nullable String name) {
            return new AutoValue_Widget(id, name);
        }
    };

    public static final Factory<Widget> FACTORY = new Factory<>(CREATOR);
    public static final Mapper<Widget> MAPPER = new Mapper<>(FACTORY);

}
