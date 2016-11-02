package com.frederikam.fred.moe;

import java.io.File;
import java.net.URISyntaxException;
import java.security.SecureRandom;

public class ResourceManager {

    public static File dataDir = new File("./data");
    public static final File PUBLIC_DIR = getPublicDir();
    private static final String BASE64_CHARS = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-";
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
        //Check the public dir first. This always takes priority
        File f = new File(PUBLIC_DIR, name);
        System.out.println(f);
        if (f.exists()) {
            return f;
        }

        return new File(dataDir, name);
    }

    private static char getRandomChar() {
        return BASE64_CHARS.charAt(RANDOM.nextInt(63));
    }

    private static File getPublicDir() {
        try {
            File jarDir = new File(ResourceManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            
            //Check for maven dir
            if("classes".equals(jarDir.getName()) && "target".equals(jarDir.getParentFile().getName())){
                return new File(jarDir.getParentFile().getParentFile(), "public");
            } else {
                return new File(jarDir, "public");
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
