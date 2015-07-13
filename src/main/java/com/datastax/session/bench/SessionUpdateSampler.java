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

    protected static Map<Long, Integer[]> roundsByThread = new HashMap<Long, Integer[]>();
    protected static MySessionDAO dao;
    protected static boolean consistencyCheckOn = true;


    public SampleResult runTest(JavaSamplerContext context) {

        int maxBytes = 50 * 1024;
        int addBytes = 1024;
        long sleepTime = 1000;
        String consistency = "P2";
        Boolean deleteSessions = true;

        if (context.containsParameter("MAXBYTES")) {
            maxBytes = context.getIntParameter("MAXBYTES");
        }
        if (context.containsParameter("ADDBYTES")) {
            addBytes = context.getIntParameter("ADDBYTES");
        }
        if (context.containsParameter("SLEEPTIME")) {
            sleepTime = context.getLongParameter("SLEEPTIME");
        }
        if (context.containsParameter("CONSISTENCY")) {
            consistency = context.getParameter("CONSISTENCY");
        }
        if (context.containsParameter("DELETESESSIONS")) {
            deleteSessions = context.getIntParameter("DELETESESSIONS") == 1;
        }

        SampleResult transaction = new SampleResult();

        // build session id
        long threadId = Thread.currentThread().getId();
        int round = 0;
        int sessionUpdateCount = 0;
        if (!roundsByThread.containsKey(threadId)) {
            roundsByThread.put(threadId, new Integer[]{0, 0});
        } else {
            round = roundsByThread.get(threadId)[0];
            sessionUpdateCount = roundsByThread.get(threadId)[1];
        }


        // TRANSACTION START
        transaction.sampleStart();
        MySession session = new MySession(Thread.currentThread().getId() + "-" + round);

        // read
        try {
            session = dao.load(session);
        } catch (Throwable t) {
            transaction.setSuccessful(false);
            transaction.setSampleLabel(session.getId() + "|R|" + t.getMessage());
            return transaction;
        }

        //if (!deleteSessions && session.getData().length() != (sessionUpdateCount) * addBytes) {
        //    System.out.println("WARNING: " + session.getId() + " has " + session.getData().length() + " bytes, expected " + sessionUpdateCount * addBytes + " at update #" + sessionUpdateCount);
        //}

        // append data
        session.appendRandomAscii(addBytes);
        String md5 = session.getMD5();

        // write
        try {
            dao.save(session, consistency);
            transaction.setSampleLabel(session.getId() + "|W|updated");
        } catch (Throwable t) {
            transaction.setSuccessful(false);
            transaction.setSampleLabel(session.getId() + "|W|" + t.getMessage());
            System.exit(0);
            return transaction;
        }

        transaction.sampleEnd();


        try {
            // check consistency
            MySession session2 = dao.load(session);

            if (consistencyCheckOn && !session2.getMD5().equals(md5)) {
                transaction.setSuccessful(false);
                System.out.println("Inconsistent read");
                transaction.setSampleLabel(session.getId() + "|R|inconsistent");
            }
        } catch (Throwable t) {
            transaction.setSuccessful(false);
            transaction.setSampleLabel(session.getId() + "|CCR|" +  t.getMessage());
            return transaction;
        }



        transaction.setSuccessful(true);transaction.setBytes(session.getData().length());

        // check if round is over
        if (session.getData().length() >= maxBytes) {
            try {
                if (deleteSessions) {
                    dao.delete(session, consistency);
                }
            } catch (Throwable t) {
                transaction.setSuccessful(false);
                transaction.setSampleLabel(session.getId() + "|D|" + t.getMessage());
                return transaction;
            }

            roundsByThread.put(threadId, new Integer[]{round, sessionUpdateCount + 1});
            roundsByThread.put(threadId, new Integer[] {round + 1, 0});
        }

        // pause
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }

        return transaction;
    }

}

