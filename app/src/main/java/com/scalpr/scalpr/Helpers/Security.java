package com.scalpr.scalpr.Helpers;

import android.content.Context;
import android.util.Base64;

import com.scalpr.scalpr.Objects.User;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by Cam on 10/29/2016.
 */
public class Security {

    public static String getAccessToken(Context c){
        UserHelper helper = new UserHelper(c);
        User u = helper.getLoggedInUser();

        if(u.getUserID() == 0){
            return "";
        }else{
            return u.getAccessToken();
        }
    }

    public static String encryptToken(String input, String key){
        byte[] crypted = null;
        try{
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input.getBytes());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return new String(Base64.encode(crypted,Base64.DEFAULT));
    }

    public static String decryptToken(String input, String key){
        byte[] output = null;
        try{
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skey);
            output = cipher.doFinal(Base64.decode(input, Base64.DEFAULT));
        }catch(Exception e){
            System.out.println(e.toString());
        }

        if(output == null){
            return ""; //avoid null pointer error, will be null if user not logged in
        }else{
            return new String(output);
        }
    }
}
