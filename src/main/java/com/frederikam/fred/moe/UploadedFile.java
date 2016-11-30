package com.frederikam.fred.moe;

import org.springframework.web.multipart.MultipartFile;

public class UploadedFile {
    private String type;

    private MultipartFile file;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setFile(MultipartFile file)
    {
        this.file = file;
    }

    public MultipartFile getFile()
    {
        return file;
    }
}