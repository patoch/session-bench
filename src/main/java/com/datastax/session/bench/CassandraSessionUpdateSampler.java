package com.datastax.session.bench;

import com.datastax.session.bench.cassandra.MySessionCassandraDAO;

/**
 * Created by patrick on 04/02/15.
 */
public class CassandraSessionUpdateSampler extends SessionUpdateSampler {


    static {
        dao = MySessionCassandraDAO.getInstance();
    }

}

