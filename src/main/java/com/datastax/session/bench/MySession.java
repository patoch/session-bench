package com.datastax.session.bench;

import org.apache.commons.lang.RandomStringUtils;
import java.security.*;


/**
 * Created by Patrick on 02/07/15.
 */
public class MySession {

    private String id;
    private String data;

    public MySession(String id) {
        this.id = id;
        this.data = "";
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void appendRandomAscii(int count) {
        data += RandomStringUtils.randomAscii(count);
    }

    public String getMD5() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] mdbytes = md.digest(this.data.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
