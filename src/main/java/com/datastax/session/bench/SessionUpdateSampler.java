package com.datastax.session.bench;

import com.datastax.session.bench.cassandra.MySessionCassandraDAO;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patrick on 04/02/15.
 */
public class SessionUpdateSampler extends AbstractJavaSamplerClient implements Serializable {

    protected static Map<Long, Integer> roundsByThread = new HashMap<Long, Integer>();
    protected static MySessionDAO dao;
    protected static boolean consitencyCheckOn = true;

    static {
        dao = MySessionCassandraDAO.getInstance();
    }


    public SampleResult runTest(JavaSamplerContext context) {

        int maxBytes = 50 * 1024;
        int addBytes = 1024;

        if (context.containsParameter("MAXBYTES")) {
            maxBytes = context.getIntParameter("MAXBYTES");
        }
        if (context.containsParameter("ADDBYTES")) {
            addBytes = context.getIntParameter("ADDBYTES");
        }

        SampleResult transaction = new SampleResult();transaction.setSampleLabel("Session update latency");
        SampleResult read = new SampleResult();read.setSampleLabel("Read latency");
        SampleResult write = new SampleResult();write.setSampleLabel("Write Latency");

        // build session id
        long threadId =Thread.currentThread().getId();
        int round = 0;
        if (!roundsByThread.containsKey(threadId)) {
            roundsByThread.put(threadId, 0);
        } else {
            round = roundsByThread.get(threadId);
        }

        // TRANSACTION START
        transaction.sampleStart();
        MySession session = new MySession(Thread.currentThread().getId() + "-" + round);

        // read
        read.sampleStart();
        session = dao.load(session);
        read.sampleEnd();read.setSuccessful(true);read.setBytes(session.getJson().length());

        // append data
        session.appendRandomAscii(addBytes);
        String md5 = session.getMD5();
        String json = session.getJson();

        // write
        write.sampleStart();
        dao.save(session);

        write.sampleEnd();write.setSuccessful(true);write.setBytes(session.getJson().length());

        transaction.sampleEnd();transaction.setSuccessful(true);transaction.setBytes(session.getJson().length());

        // check consistency
        String md52=dao.load(session).getMD5();
        String json2 = dao.load(session).getJson();
        if (!md52.equals(md5)) {
            transaction.setSuccessful(false);
            transaction.setResponseCode("INCONSISTENT");
            System.out.println("INCONSISTENT MD5 = " + md5 + "/" + md52);
        }

        // check if round is over
        if (consitencyCheckOn && session.getJson().length() >= maxBytes) {
            dao.delete(session);
            roundsByThread.put(threadId, roundsByThread.get(threadId) + 1);
        }

        // pause
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        transaction.addRawSubResult(write);
        transaction.addRawSubResult(read);
        return transaction;
    }

}

