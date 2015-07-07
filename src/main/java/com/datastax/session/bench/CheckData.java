package com.datastax.session.bench;

/**
 * Created by Patrick on 06/07/15.
 */
public class CheckData {

    public static void main(String[] args) {
        try {
            String contactPoint = args[0];
            String logFilePath = args[1];
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please add argumens: [Cassandra contact point] [JMeter log file path]");
            System.exit(1);
        }



    }
}
