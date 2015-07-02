package com.datastax.session.bench.couch;

import com.couchbase.client.java.document.BinaryDocument;
import com.couchbase.client.java.document.StringDocument;
import com.datastax.session.bench.MySession;
import com.datastax.session.bench.MySessionDAO;

/**
 * Created by Patrick on 02/07/15.
 */
public class MySessionCouchbaseDAO implements MySessionDAO {

    private static MySessionDAO onlyInstance;

    public static synchronized MySessionDAO getInstance() {
        if (onlyInstance == null) {
            onlyInstance =new MySessionCouchbaseDAO();
        }
        return onlyInstance;
    }

    public void save(MySession session) {
        StringDocument doc = StringDocument.create(session.getId(), session.getJson());
        Couchbase.getBucket().insert(doc);

    }

    public MySession load(MySession session) {
        StringDocument doc = Couchbase.getBucket().get(session.getId(), StringDocument.class);
        session.setJson(doc.content());
        return session;
    }

    public void delete(MySession session) {
        Couchbase.getBucket().remove(session.getId());
    }
}
