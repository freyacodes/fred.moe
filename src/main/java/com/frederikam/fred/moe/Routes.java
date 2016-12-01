package com.frederikam.fred.moe;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Route;
import spark.Spark;
import spark.utils.IOUtils;

import java.io.*;
import java.net.URLConnection;

class Routes {

    private static final Logger log = LoggerFactory.getLogger(Routes.class);
    private static TikaConfig tika = createTikaConfig();

    private static TikaConfig createTikaConfig() {
        try {
            return new TikaConfig();
        } catch (TikaException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Route onGet(){
        return (request, response) -> {
            String path = request.pathInfo();
            log.info("GET " + path);

            if (path.equals("/")) {
                path = "/index.html";
            }

            File f = ResourceManager.getResource(path.substring(1));
            boolean isInPublic = f.getAbsolutePath().startsWith(ResourceManager.PUBLIC_DIR.getAbsolutePath());
            //Verify that the file requested is in a public directory
            if (!f.getParentFile().getAbsolutePath().equals(ResourceManager.dataDir.getAbsolutePath())
                    && !isInPublic) {
                throw new FileNotFoundException();
            }

            if (!f.exists()) {
                throw new FileNotFoundException();
            }

            log.info(f.getAbsolutePath());

            InputStream mimeIs = new BufferedInputStream(new FileInputStream(f));
            String mimeType = URLConnection.guessContentTypeFromStream(mimeIs);
            response.type(mimeType);

            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, f.toString());

            MediaType mediaType = tika.getDetector().detect(
                    TikaInputStream.get(f), metadata);


            log.info("File " + f + " is " + mediaType.toString());
            response.type(mediaType.toString());

            InputStream fis = new FileInputStream(f);
            IOUtils.copy(fis, response.raw().getOutputStream());
            return "";
        };
    }

    public static ExceptionHandler exceptonHandler() {
        return (e, request, response) -> {
            if(e instanceof FileNotFoundException){
                response.status(404);

                try {
                    FileInputStream fis = new FileInputStream(new File("public/404.html"));
                    response.body(IOUtils.toString(fis));
                } catch (IOException e1) {
                    log.error("", e1);
                }
            } else {
                response.status(500);
                log.error("500 internal server error", e);

                try {
                    FileInputStream fis = new FileInputStream(new File("public/500.html"));
                    response.body(IOUtils.toString(fis));
                } catch (IOException e1) {
                    log.error("", e1);
                }
            }
        };
    }


}
