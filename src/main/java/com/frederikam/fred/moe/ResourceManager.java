package com.frederikam.fred.moe;

import java.io.File;
import java.security.SecureRandom;

public class ResourceManager {

    public static File dataDir = new File("./data");
    private static final String BASE64_CHARS = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-";
    //private static final String BASE64_CHARS = "01";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String getUniqueName(String extension) {
        String name = "";

        while (resourceExists(name + extension) || name.equals("")) {
            name = name + getRandomChar();
        }

        return name + extension;
    }

    public static boolean resourceExists(String name) {
        return getResource(name).exists();
    }

    public static File getResource(String name) {
        return new File(dataDir, name);
    }

    private static char getRandomChar() {
        return BASE64_CHARS.charAt(RANDOM.nextInt(63));
    }

}
