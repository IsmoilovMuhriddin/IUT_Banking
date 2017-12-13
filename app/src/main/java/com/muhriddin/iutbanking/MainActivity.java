package com.muhriddin.iutbanking;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
        for (int n = 0; n < 8; n++) {
            encKey[n] = (byte) password.charAt(n);
            encKey[n + 8] = salt[n];
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

    static byte[] encrypt(byte[] plain, byte[] key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(plain);
    }

    static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(data);
        return encrypted;
    }
}
