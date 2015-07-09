package com.datastax.session.bench;

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
    protected static boolean consistencyCheckOn = true;


    public SampleResult runTest(JavaSamplerContext context) {

        int maxBytes = 50 * 1024;
        int addBytes = 1024;
        String consistency = "P3";
        Boolean deleteSessions = true;

        if (context.containsParameter("MAXBYTES")) {
            maxBytes = context.getIntParameter("MAXBYTES");
        }
        if (context.containsParameter("ADDBYTES")) {
            addBytes = context.getIntParameter("ADDBYTES");
        }
        if (context.containsParameter("CONSISTENCY")) {
            consistency = context.getParameter("CONSISTENCY");
        }
        if (context.containsParameter("DELETESESSIONS")) {
            deleteSessions = context.getIntParameter("DELETESESSIONS") == 1;
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
        try {
            session = dao.load(session);
        } catch (Throwable t) {
            transaction.setSuccessful(false);
            if (deleteSessions)
                transaction.setSampleLabel("Update session");
            else
                transaction.setSampleLabel(session.getId() + "|R|" + t.getMessage());
            return transaction;
        }
        read.sampleEnd();read.setSuccessful(true);read.setBytes(session.getJson().length());

        // append data
        session.appendRandomAscii(addBytes);
        String md5 = session.getMD5();
        String json = session.getJson();

        // write
        write.sampleStart();
        try {
            dao.save(session, consistency);
        } catch (Throwable t) {
            transaction.setSuccessful(false);
            if (deleteSessions)
                transaction.setSampleLabel("Update session");
            else
                transaction.setSampleLabel(session.getId() + "|W|" + t.getMessage());
            return transaction;
        }
        write.sampleEnd();
        write.setBytes(session.getJson().length());

        transaction.sampleEnd();


        try {
            // check consistency
            String md52 = dao.load(session).getMD5();
            String json2 = dao.load(session).getJson();
            if (consistencyCheckOn && !md52.equals(md5)) {
                transaction.setSuccessful(false);
                if (deleteSessions)
                    transaction.setSampleLabel("Update session");
                else
                    transaction.setSampleLabel(session.getId() + "|R|inconsistent");
            }
        } catch (Throwable t) {
            transaction.setSuccessful(false);
            if (deleteSessions)
                transaction.setSampleLabel("Update session");
            else
                transaction.setSampleLabel(session.getId() + "|CCR|" +  t.getMessage());
            return transaction;
        }

        // check if round is over
        if (session.getJson().length() >= maxBytes) {
            try {
                if (deleteSessions) {
                    dao.delete(session, consistency);
                }
            } catch (Throwable t) {
                transaction.setSuccessful(false);
                if (deleteSessions)
                    transaction.setSampleLabel("Update session");
                else
                    transaction.setSampleLabel(session.getId() + "|D|" + t.getMessage());
                return transaction;
            }
            roundsByThread.put(threadId, roundsByThread.get(threadId) + 1);
        }
        if (deleteSessions)
            transaction.setSampleLabel("Update session");
        else
            transaction.setSampleLabel(session.getId() + "|updated");
        transaction.setSuccessful(true);transaction.setBytes(session.getJson().length());
        
        // pause
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        return transaction;
    }

}

