package com.darren.backend.documents;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "sprites")
public class Sprite {
    private String fileName;
    private String contentType;
    private long size;

    public Sprite(@JsonProperty("file_name") String fileName, @JsonProperty("content_type") String contentType, @JsonProperty("size")  long size) {
        setFileName(fileName);
        setContentType(contentType);
        setSize(size);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String filename) {
        this.fileName = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}