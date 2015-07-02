package com.datastax.session.bench;

import org.apache.commons.lang.RandomStringUtils;
import java.security.*;


/**
 * Created by Patrick on 02/07/15.
 */
public class MySession {

    private String id;
    private String json;

    public MySession(String id) {
        this.id = id;
        this.json = "";
    }

    public String getId() {
        return id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void appendRandomAscii(int count) {
        json += RandomStringUtils.randomAscii(count);
    }

    public String getMD5() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] mdbytes = md.digest(this.json.getBytes());
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
