package com.datastax.session.bench;

import com.datastax.session.bench.couch.Couchbase;
import com.datastax.session.bench.couch.MySessionCouchbaseDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Patrick on 06/07/15.
 */
public class CheckData {

    public static void main(String[] args) {
        String fileStr = null;
        Map<String, Integer> counts = new HashMap<String, Integer>();
        MySessionDAO dao = MySessionCouchbaseDAO.getInstance();

        try {
            //fileStr = args[0];
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please add argument:  [JMeter log file path]");
            System.exit(1);
        }

        fileStr = "/Users/Patrick/jmeter.csv";
        System.out.println("=== Reading logs and counting succesfull updates ===");
        try {
            File file = new File(fileStr);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = null, ssid = null, status= null;
            boolean isSuccess;
            String[] data;
            int updateCount = 0;
            while((line = br.readLine()) != null) {
                data = line.split(",");
                status = data[2];
                ssid = status.split("\\|")[0];
                status = status.split("\\|")[1];
                isSuccess = Boolean.valueOf(data[7]);
                if ("W".equals(status) && isSuccess) {
                    if (counts.containsKey(ssid))
                        counts.put(ssid, counts.get(ssid) + 1);
                    else
                        counts.put(ssid, 1);
                }
            }
            br.close();
            fr.close();

            // test
            System.out.println("=== Checking data ===");
            int expectedBytes;
            String storedData;
            int totalDataCount = counts.keySet().size();
            int dataLossCount = 0;
            for (String key: counts.keySet()) {
                expectedBytes = counts.get(key)  * 1024;
                try {
                    storedData = dao.load(new MySession(key)).getData();
                    if (storedData.length() < expectedBytes) {
                        System.out.println("SSID " + key +": doesn't match expected length. Found :" + storedData.length() + ", expected :" + expectedBytes + ", diff:" + (expectedBytes - storedData.length()));
                        dataLossCount ++;
                    }
                } catch (Throwable throwable) {
                    dataLossCount ++;
                    System.out.println("SSID " + key +": not found:" +throwable.getMessage());
                }
            }

            System.out.println("================================================");
            System.out.println((dataLossCount/totalDataCount)*100 + "% data loss.");
            System.out.println(String.format("%d incorrect sessions out of %d",dataLossCount, totalDataCount));

            Couchbase.close();


        } catch (IOException e) {
            System.out.println("Couldn't read file: " + e.getMessage());
            System.exit(1);
        }
    }
}
