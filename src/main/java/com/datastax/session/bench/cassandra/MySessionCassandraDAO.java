package com.datastax.session.bench.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.session.bench.MySession;
import com.datastax.session.bench.MySessionDAO;


/**
 * Created by Patrick on 02/07/15.
 */
public class MySessionCassandraDAO implements MySessionDAO {


    private static MySessionDAO onlyInstance;

    public static synchronized MySessionDAO getInstance() {
        if (onlyInstance == null) {
            onlyInstance =new MySessionCassandraDAO();
        }
        return onlyInstance;
    }

    private MySessionCassandraDAO() {
        Cassandra.prepareStatement("insert", "INSERT INTO bench_ks.sessions(id, json) VALUES (?,?)");
        Cassandra.prepareStatement("select", "SELECT json FROM bench_ks.sessions WHERE id=?");
        Cassandra.prepareStatement("delete", "DELETE FROM bench_ks.sessions WHERE id=?");
    }

    public void save(MySession session, String consistency) {
        BoundStatement bs = Cassandra.getPreparedStatement("insert").bind(session.getId(), session.getJson());
        Cassandra.getSession().execute(bs);
    }

    public MySession load(MySession session) {
        BoundStatement bs = Cassandra.getPreparedStatement("select").bind(session.getId());
        ResultSet rs = Cassandra.getSession().execute(bs);
        if (rs.isExhausted()) {
            return session;
        }
        session.setJson(rs.one().getString("json"));
        return session;
    }

    public void delete(MySession session, String consistency) {
        BoundStatement bs = Cassandra.getPreparedStatement("delete").bind(session.getId());
        Cassandra.getSession().execute(bs);
    }
}
