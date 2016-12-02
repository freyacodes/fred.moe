package com.frederikam.fred.moe;

import java.io.*;
import java.util.Scanner;

import org.apache.tika.exception.TikaException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import spark.route.RouteOverview;

public class FredDotMoe {

    private static final Logger log = LoggerFactory.getLogger(FredDotMoe.class);

    
    protected static String baseUrl;

    public static final long MAX_UPLOAD_SIZE = 128 * 1000000;

    public static void main(String[] args) throws IOException, TikaException {
        InputStream is = new FileInputStream(new File("./config.json"));
        Scanner scanner = new Scanner(is);
        JSONObject config = new JSONObject(scanner.useDelimiter("\\A").next());
        ResourceManager.dataDir = new File(config.getString("dataDir"));
        baseUrl = config.optString("baseUrl", "http://localhost/");

        scanner.close();
        ResourceManager.dataDir.mkdirs();

        Spark.port(8080);
        RouteOverview.enableRouteOverview();

        /* handlers */
        Spark.get("/*", Routes.onGet());
        Spark.post("/upload", Routes.upload());
        Spark.exception(Exception.class, Routes.exceptionHandler());
    }

}
