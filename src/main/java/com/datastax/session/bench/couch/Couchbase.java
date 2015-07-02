package com.datastax.session.bench.couch;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

/**
 * Created by Patrick on 02/07/15.
 */
public class Couchbase {

    private static Cluster cluster;
    private static Bucket bucket;

    public static synchronized Cluster getCluster() {
        if (cluster == null) {
            cluster = CouchbaseCluster.create("127.0.0.1");
        }
        return cluster;
    }

    public static synchronized Bucket getBucket() {
        bucket = getCluster().openBucket("session-bucket");
        return bucket;
    }

    public static synchronized void close() {
        bucket.close();
        cluster.disconnect();
    }

}
