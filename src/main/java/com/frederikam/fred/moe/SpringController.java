package com.frederikam.fred.moe;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@EnableAutoConfiguration
public class SpringController {

    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(\\.\\w+)$");

    private static final Logger log = LoggerFactory.getLogger(SpringController.class);
    private static TikaConfig tika = createTikaConfig();

    private static TikaConfig createTikaConfig() {
        try {
            return new TikaConfig();
        } catch (TikaException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/**")
    @ResponseBody
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        log.info("GET " + path);

        if (path.equals("/")) {
            path = "/index.html";
        }

        File f = ResourceManager.getResource(path.substring(1));
        boolean isInPublic = f.getAbsolutePath().startsWith(ResourceManager.PUBLIC_DIR.getAbsolutePath());
        //Verify that the file requested is in a public directory
        if (!f.getParentFile().getAbsolutePath().equals(ResourceManager.getDataDir().getAbsolutePath())
                && !isInPublic) {
            throw new FileNotFoundException();
        }

        if (!f.exists()) {
            throw new FileNotFoundException();
        }

        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, f.toString());

        MediaType mediaType = tika.getDetector().detect(
                TikaInputStream.get(f.toPath()), metadata);

        response.setContentType(mediaType.toString());

        IOUtils.copy(new FileInputStream(f), response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestHeader(required = false) String name,
                         @RequestParam("file") MultipartFile file
    ) throws IOException, FileUploadException {
        log.info("POST "+request.getServletPath());

        String filename = file.getOriginalFilename();
        if (name != null) {
            filename = name;
        }

        //No .exe files please
        if (filename.toLowerCase().endsWith(".exe")) {
            throw new RuntimeException(".exe files are not allowed");
        }

        String extension = "";
        Matcher m = FILE_EXTENSION_PATTERN.matcher(filename);
        if (m.find()) {
            extension = m.group(1);
        }

        String storeName = ResourceManager.getUniqueName(extension);
        File f = ResourceManager.getResource(storeName);

        //noinspection ResultOfMethodCallIgnored
        file.transferTo(f);

        String hash = Base64.getEncoder().encodeToString(DigestUtils.md5(new FileInputStream(f)));

        log.info("Hash: " + hash);

        //Now generate a response
        JSONObject root = new JSONObject();
        root.put("success", true);

        JSONArray files = new JSONArray();
        JSONObject arrayInner = new JSONObject();
        arrayInner.put("hash", hash);
        arrayInner.put("name", filename);
        arrayInner.put("url", "https://" + request.getServerName() + "/" + storeName);
        arrayInner.put("size", f.length());

        files.put(arrayInner);
        root.put("files", files);

        log.info("File " + f + " was uploaded");

        response.setContentType("application/json");

        if(FredDotMoe.getVirusScanner() != null)
            FredDotMoe.getVirusScanner().scanAsync(f);

        return root.toString();
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        log.error("Request raised an error: " + req.getRequestURL(), ex);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error");

        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        return mav;
    }

}
