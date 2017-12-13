package com.muhriddin.iutbanking;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = findViewById(R.id.text);

        String plain = "Akmaljon";
        String password = "Jonny";

        byte[] salt = new byte[16 - password.length()];
        new SecureRandom().nextBytes(salt);

        byte[] encKey = new byte[16];
        for (int p = 0, s = 0; p < password.length() || s < salt.length; p++, s++) {
            if (p < password.length())
                encKey[p] = (byte) password.charAt(p);
            if (s < salt.length)
                encKey[s + password.length()] = salt[s];
        }

        String username = "akmaljon";

        try {
            if (!new File(getFilesDir(), username + ".log").exists() && new File(getFilesDir(), username + ".log").createNewFile())
                Log.e("ERROR", "Log file was not created!");

            writeLogContent(this, plain, username, encKey);
            text.setText(readLogContent(this, username, encKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void writeLogContent(Context context, String plain, String username, byte[] encKey) throws Exception {
        FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), username + ".log"));
        fos.write(encrypt(plain.getBytes("UTF-8"), encKey));
        fos.close();
    }

    static String readLogContent(Context context, String username, byte[] encKey) throws Exception {
        FileInputStream fis = new FileInputStream(new File(context.getFilesDir(), username + ".log"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = fis.read(buffer)) > 0)
            bos.write(buffer, 0, read);
        bos.close();
        fis.close();
        return new String(decrypt(bos.toByteArray(), encKey), "UTF-8");
    }


    static final IvParameterSpec iv = new IvParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});

    static byte[] encrypt(byte[] plain, byte[] key) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(plain);
        for (int n = 0; n < 16 - plain.length % 16; n++)
            os.write(0);
        os.close();

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        return cipher.doFinal(os.toByteArray());
    }

    static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] plain = cipher.doFinal(data);

        if (plain.length == 0)
            return new byte[0];

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int n = plain.length - 1;
        while (plain[n] == 0)
            n--;
        os.write(plain, 0, n + 1);
        os.close();
        return os.toByteArray();
    }
}
