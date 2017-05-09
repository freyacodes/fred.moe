package com.frederikam.fred.moe;

import com.frederikam.fred.moe.util.VirusScanner;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.io.IOException;

public class FredDotMoe {

    private static final Logger log = LoggerFactory.getLogger(FredDotMoe.class);

    @Value("{moe.baseUrl}")
    static String baseUrl;
    @Value("{moe.dataDir}")
    private static String dataDir = "data";

    private static VirusScanner virusScanner = null;

    public static void main(String[] args) throws IOException, TikaException {
        //Tomcat changes the working dir, so we make this absolute
        ResourceManager.setDataDir(new File(dataDir).getAbsoluteFile());

        //noinspection ResultOfMethodCallIgnored
        ResourceManager.getDataDir().mkdirs();

        SpringApplication.run(SpringController.class, args);

        new Caddy().start();

        if(VirusScanner.isAvInstalled()) {
            virusScanner = new VirusScanner();
            virusScanner.start();
        }
    }

}
