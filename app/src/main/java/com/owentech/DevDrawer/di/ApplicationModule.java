package com.owentech.DevDrawer.di;

import android.content.Context;
import com.owentech.DevDrawer.utils.Database;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    Context context;

    public ApplicationModule(Context context){
        this.context = context;
    }

    @Provides
    @Singleton
    public Database providesDatabase(){
        return new Database(context);
    }
}
