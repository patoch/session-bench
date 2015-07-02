package com.datastax.session.bench.cassandra;

/**
 * Created by Patrick on 06/05/15.
 */
public abstract class Settings {

    public static String[] getContactPoints() {
        return new String[]{"127.0.0.1"};
    }

    public static String getLocalDC() {
        return "DC1";
    }

    public static int getReadtimeout() {
        return 10500;
    }
}
