package com.frederikam.fred.moe;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

@Configuration
@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
@Controller
@ComponentScan
public class FredDotMoe {

    private static String baseUrl;

    public static final long MAX_UPLOAD_SIZE = 128 * 1000000;
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(\\.\\w+)$");

    public static void main(String[] args) throws FileNotFoundException {
        InputStream is = new FileInputStream(new File("./config.json"));
        Scanner scanner = new Scanner(is);
        JSONObject config = new JSONObject(scanner.useDelimiter("\\A").next());
        ResourceManager.dataDir = new File(config.getString("dataDir"));
        baseUrl = config.optString("baseUrl", "http://localhost/");

        scanner.close();
        ResourceManager.dataDir.mkdirs();
        ApplicationContext ctx = SpringApplication.run(FredDotMoe.class, args);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    private static void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        File f = ResourceManager.getResource(path.substring(1));
        System.out.println(f.getParentFile().getAbsolutePath() + "\n" + ResourceManager.dataDir.getAbsolutePath());
        if (!f.getParentFile().getAbsolutePath().equals(ResourceManager.dataDir.getAbsolutePath())) {
            response.sendError(400);
            return;
        }

        if (!f.exists()) {
            response.sendError(404);
            return;
        }

        InputStream is = new FileInputStream(f);
        IOUtils.copy(is, response.getOutputStream());
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private static String upload(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("file") final MultipartFile file
    ) throws IOException, NoSuchAlgorithmException {
        //Check if the file limit is reached
        if (file.getSize() > MAX_UPLOAD_SIZE) {
            response.sendError(413);
            return "";
        }

        //No .exe files please
        if (file.getOriginalFilename().toLowerCase().endsWith(".exe")) {
            response.sendError(400);
            return "";
        }

        String extension = "";
        Matcher m = FILE_EXTENSION_PATTERN.matcher(request.getParameter("name"));
        if (m.find()) {
            extension = m.group(1);
        }

        String storeName = ResourceManager.getUniqueName(extension);
        File f = ResourceManager.getResource(storeName);

        MessageDigest md = MessageDigest.getInstance("md5");
        String hash = Base64.encode(md.digest(file.getBytes()));

        //Now generate a response
        JSONObject root = new JSONObject();
        root.put("success", true);

        JSONArray files = new JSONArray();
        JSONObject arrayInner = new JSONObject();
        arrayInner.put("hash", hash);
        arrayInner.put("name", request.getParameter("name"));
        arrayInner.put("url", baseUrl + storeName);
        arrayInner.put("size", file.getSize());

        files.put(arrayInner);
        root.put("files", files);

        file.transferTo(f);//Finally move the file

        return root.toString();
    }

}
