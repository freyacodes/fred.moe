package com.frederikam.fred.moe;

import org.apache.tika.exception.TikaException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class FredDotMoe {

    private static final Logger log = LoggerFactory.getLogger(FredDotMoe.class);
    
    protected static String baseUrl;

    public static final long MAX_UPLOAD_SIZE = 256 * 1000000;

    public static void main(String[] args) throws IOException, TikaException {
        InputStream is = new FileInputStream(new File("./config.json"));
        Scanner scanner = new Scanner(is);
        JSONObject config = new JSONObject(scanner.useDelimiter("\\A").next());
        ResourceManager.dataDir = new File(config.getString("dataDir"));
        baseUrl = config.optString("baseUrl", "http://localhost/");

        scanner.close();
        ResourceManager.dataDir.mkdirs();

        //System.setProperty("multipart.enabled", "false");

        SpringApplication.run(SpringController.class, args);

        log.info(System.getProperty("multipart.enabled"));
    }

}
