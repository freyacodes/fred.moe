package com.frederikam.fred.moe;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Route;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Routes {

    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(\\.\\w+)$");

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

            InputStream mimeIs = new BufferedInputStream(new FileInputStream(f));
            String mimeType = URLConnection.guessContentTypeFromStream(mimeIs);
            response.type(mimeType);

            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, f.toString());

            MediaType mediaType = tika.getDetector().detect(
                    TikaInputStream.get(f), metadata);


            //log.info("File " + f + " is " + mediaType.toString());
            response.type(mediaType.toString());

            InputStream fis = new FileInputStream(f);
            IOUtils.copy(fis, response.raw().getOutputStream());
            return "";
        };
    }

    public static Route upload(){
        return (request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("java.io.tmpdir")));
            Part part = request.raw().getPart("file");

            String path = request.pathInfo();

            log.info("POST "+path);

            //Check if the file limit is reached
            if (part.getSize() > FredDotMoe.MAX_UPLOAD_SIZE) {
                log.error("Uploaded size was " + part.getSize() + ", max size is " + FredDotMoe.MAX_UPLOAD_SIZE);
                throw new FileTooBigException();
            }

            String filename = part.getSubmittedFileName();
            if (request.headers("name") != null) {
                filename = request.headers("name");
            }

            //No .exe files please
            if (filename.toLowerCase().endsWith(".exe")) {
                throw new RuntimeException(".exe files are now allowed");
            }

            String extension = "";
            Matcher m = FILE_EXTENSION_PATTERN.matcher(filename);
            if (m.find()) {
                extension = m.group(1);
            }

            String storeName = ResourceManager.getUniqueName(extension);
            File f = ResourceManager.getResource(storeName);

            try (FileWriter fw = new FileWriter(f)) {
                IOUtils.copy(part.getInputStream(), fw);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] bytes = IOUtils.toByteArray(new FileInputStream(f));
            log.info("got bytes");
            MessageDigest md = MessageDigest.getInstance("md5");
            log.info("digest");

            String hash = Base64.getEncoder().encodeToString(md.digest(bytes));


            log.info(f.getAbsolutePath());
            log.info(hash);

            //Now generate a response
            JSONObject root = new JSONObject();
            root.put("success", true);

            JSONArray files = new JSONArray();
            JSONObject arrayInner = new JSONObject();
            arrayInner.put("hash", hash);
            arrayInner.put("name", filename);
            arrayInner.put("url", FredDotMoe.baseUrl + storeName);
            arrayInner.put("size", f.length());

            files.put(arrayInner);
            root.put("files", files);

            log.info("File " + f + "was uploaded");

            log.info(root.toString());

            return root.toString();
        };
    }

    public static ExceptionHandler exceptionHandler() {
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
