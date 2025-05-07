package com.imgodz.diecastvault;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Diecast.class}, version = 1, exportSchema = true)
public abstract class DiecastDatabase extends RoomDatabase {

    private static DiecastDatabase instance;

    public abstract DiecastDAO getDiecastDAO();


    public static synchronized DiecastDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            DiecastDatabase.class, "diecast_database")
                            .build();
        }
        return instance;
    }

}
