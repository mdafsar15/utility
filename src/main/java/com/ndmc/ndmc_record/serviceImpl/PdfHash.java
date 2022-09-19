package com.ndmc.ndmc_record.serviceImpl;

import java.io.BufferedInputStream;
        import java.security.MessageDigest;



        import java.io.FileInputStream;
        import java.io.IOException;
        import java.security.DigestInputStream;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;

public class PdfHash {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance("SHA-256"); //SHA, MD2, MD5, SHA-256, SHA-384...
        String hex = checksum("/Users/shashankawasthi/Downloads/demo/src/main/java/xtras/shashankPassport.pdf", md);
        System.out.println(hex);
    }

    public static String checksum(String filepath, MessageDigest md) throws IOException {

        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

}

