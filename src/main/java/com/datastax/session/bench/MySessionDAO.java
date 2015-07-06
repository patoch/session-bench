package com.datastax.session.bench;


/**
 * Created by Patrick on 02/07/15.
 */
public interface MySessionDAO {


    void save(MySession session, String consistency) throws Throwable;

    MySession load(MySession session) throws Throwable;

    void delete(MySession session, String consistency) throws Throwable;

}
