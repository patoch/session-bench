package com.datastax.session.bench.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.*;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by patrick on 03/02/15.
 */
public class Cassandra {

    private static Cluster s_cluster;
    private static Session s_session;
    private static Map<String, PreparedStatement> psMap;


    public static synchronized Cluster getCluster() {

        if (s_cluster == null) {

            QueryOptions queryOptions = new QueryOptions()
                    .setConsistencyLevel(ConsistencyLevel.QUORUM)
                    .setFetchSize(100);

            SocketOptions socketOptions = new SocketOptions()
                    .setReadTimeoutMillis(Settings.getReadtimeout());

            s_cluster = Cluster.builder()
                    .withQueryOptions(queryOptions)
                    .withSocketOptions(socketOptions)
                    .addContactPoints(Settings.getContactPoints())
                    .withProtocolVersion(ProtocolVersion.V3)
                    .withPort(9042)
                    //.withCompression(ProtocolOptions.Compression.LZ4)
                    //.withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy(com.datastax.session.bench.cassandra.Settings.getLocalDC()), true))
                    .withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))
                    //.withReconnectionPolicy( new ExponentialReconnectionPolicy(100, 30000))
                    .withoutJMXReporting()
                    .withoutMetrics()
                    .build();

        }
        return s_cluster;
    }




    public static synchronized Session getSession() {
        if (s_session == null) {
            s_session = getCluster().connect();
        }
        return s_session;
    }

    public static void prepareStatement(String key, String cql) {
        if (psMap == null) {
            psMap = new HashMap<String, PreparedStatement>();
        }
        psMap.put(key, getSession().prepare(cql));
    }

    public static PreparedStatement getPreparedStatement(String key) {
        return psMap.get(key);
    }

    public static synchronized void close() {
        s_session.close();
        s_cluster.close();
    }

}
