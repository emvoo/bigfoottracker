package uk.ac.solent.marcinwisniewski.bigfoottracker.db;

import android.util.Log;

/**
 * Model class to represent user.
 */
public class User {
    private int id;
    private String username, password;

    // constructors
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        setPassword(password);
    }

    // GETTERS
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // SETTERS
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = MD5(password);
    }

    private String MD5(String pw) {
        try {
            java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(pw.getBytes());
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                stringBuffer.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return stringBuffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            Log.e("USER", e.getMessage());
        }
        return null;
    }
}
