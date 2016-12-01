package com.frederikam.fred.moe;

import java.io.*;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.NameDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;
import spark.route.RouteOverview;
import spark.utils.IOUtils;

public class FredDotMoe {

    private static final Logger log = LoggerFactory.getLogger(FredDotMoe.class);

    
    private static String baseUrl;

    public static final long MAX_UPLOAD_SIZE = 128 * 1000000;
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(\\.\\w+)$");

    public static void main(String[] args) throws IOException, TikaException {
        InputStream is = new FileInputStream(new File("./config.json"));
        Scanner scanner = new Scanner(is);
        JSONObject config = new JSONObject(scanner.useDelimiter("\\A").next());
        ResourceManager.dataDir = new File(config.getString("dataDir"));
        baseUrl = config.optString("baseUrl", "http://localhost/");

        scanner.close();
        ResourceManager.dataDir.mkdirs();

        Spark.port(8080);
        Spark.staticFileLocation("/public");
        RouteOverview.enableRouteOverview();

        /* handlers */
        Spark.get("/*", Routes.onGet());


    }

    /*@RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    private static void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        
        log.info("GET "+path);
        
        if (path.equals("/")) {
            path = "/index.html";
        }

        File f = ResourceManager.getResource(path.substring(1));
        boolean isInPublic = f.getAbsolutePath().startsWith(ResourceManager.PUBLIC_DIR.getAbsolutePath());
        //Verify that the file requested is in a public directory
        if (!f.getParentFile().getAbsolutePath().equals(ResourceManager.dataDir.getAbsolutePath())
                && !isInPublic) {
            response.sendError(400);
            return;
        }

        if (!f.exists()) {
            response.sendError(404);
            return;
        }
        
        if(isInPublic && f.getName().endsWith(".css")){
            response.setContentType("text/css");
        }
        
        if(isInPublic && f.getName().endsWith(".appcache")){
            response.setContentType("text/cache-manifest");
        }

        InputStream is = new FileInputStream(f);
        IOUtils.copy(is, response.getOutputStream());
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private static String upload(HttpServletRequest request,
            HttpServletResponse response,
                                 @ModelAttribute("file") UploadedFile uploadedFile
    ) throws IOException, NoSuchAlgorithmException {
        MultipartFile file = uploadedFile.getFile();

        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        
        log.info("POST "+path);
        
        //Check if the file limit is reached
        if (file.getSize() > MAX_UPLOAD_SIZE) {
            response.sendError(413);
            return "";
        }

        String filename = file.getOriginalFilename();
        if (request.getParameter("name") != null) {
            filename = request.getParameter("name");
        }

        //No .exe files please
        if (filename.toLowerCase().endsWith(".exe")) {
            response.sendError(400);
            return "";
        }

        String extension = "";
        Matcher m = FILE_EXTENSION_PATTERN.matcher(filename);
        if (m.find()) {
            extension = m.group(1);
        }

        String storeName = ResourceManager.getUniqueName(extension);
        File f = ResourceManager.getResource(storeName);

        MessageDigest md = MessageDigest.getInstance("md5");
        String hash = Base64.getEncoder().encodeToString(md.digest(file.getBytes()));

        //Now generate a response
        JSONObject root = new JSONObject();
        root.put("success", true);

        JSONArray files = new JSONArray();
        JSONObject arrayInner = new JSONObject();
        arrayInner.put("hash", hash);
        arrayInner.put("name", filename);
        arrayInner.put("url", baseUrl + storeName);
        arrayInner.put("size", file.getSize());

        files.put(arrayInner);
        root.put("files", files);

        file.transferTo(f);//Finally move the file

        return root.toString();
    }*/

}
