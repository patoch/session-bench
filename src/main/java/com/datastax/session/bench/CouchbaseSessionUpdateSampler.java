package com.datastax.session.bench;

import com.datastax.session.bench.couch.MySessionCouchbaseDAO;

import java.io.Serializable;

/**
 * Created by patrick on 04/02/15.
 */
public class CouchbaseSessionUpdateSampler extends SessionUpdateSampler {

    static {
        dao = MySessionCouchbaseDAO.getInstance();
    }

    public CouchbaseSessionUpdateSampler() {
        super();
    }

}

