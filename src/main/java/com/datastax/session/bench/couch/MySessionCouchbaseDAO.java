package com.datastax.session.bench.couch;

import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.ReplicateTo;
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

    public void save(MySession session, String consistency) throws Throwable {
        StringDocument doc = StringDocument.create(session.getId(), session.getJson());
        if (consistency.startsWith("R")) {
            Couchbase.getBucket().upsert(doc, getReplicateTo(consistency));
        } else {
            Couchbase.getBucket().upsert(doc, getPersistTo(consistency));
        }
    }

    public MySession load(MySession session) throws Throwable {
        StringDocument doc = Couchbase.getBucket().get(session.getId(), StringDocument.class);
        if (doc != null) {
            session.setJson(doc.content());
        }
        return session;
    }

    public void delete(MySession session, String consistency) throws Throwable {
        if (consistency.startsWith("R")) {
            Couchbase.getBucket().remove(session.getId(), getReplicateTo(consistency));
        } else {
            Couchbase.getBucket().remove(session.getId(), getPersistTo(consistency));
        }
    }

    private PersistTo getPersistTo(String consistency) {
        if ("P1".equals(consistency)) {
            return PersistTo.ONE;
        } else if ("P2".equals(consistency)) {
            return PersistTo.TWO;
        } else if ("P3".equals(consistency)) {
            return PersistTo.THREE;
        } else {
            return PersistTo.NONE;
        }
    }

    private ReplicateTo getReplicateTo(String consistency) {
        if ("R1".equals(consistency)) {
            return ReplicateTo.ONE;
        } else if ("R2".equals(consistency)) {
            return ReplicateTo.TWO;
        } else if ("R3".equals(consistency)) {
            return ReplicateTo.THREE;
        } else {
            return ReplicateTo.NONE;
        }
    }
}
