package com.frederikam.fred.moe;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

@Component
public class ErrorHandler implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        return new ModelAndView(new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
                int code = response.getStatus();
                File errorPage = ResourceManager.getResource(code + ".html");
                if(!errorPage.exists()){
                    errorPage = ResourceManager.getResource("500.html");
                }
                IOUtils.copy(new FileInputStream(errorPage), response.getOutputStream());
            }
        });
    }
}
