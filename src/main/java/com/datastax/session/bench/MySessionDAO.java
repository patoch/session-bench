package com.datastax.session.bench;


/**
 * Created by Patrick on 02/07/15.
 */
public interface MySessionDAO {


    void save(MySession session);

    MySession load(MySession session);

    void delete(MySession session);

}
