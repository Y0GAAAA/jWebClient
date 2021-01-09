package com.y0ga.Networking;

import java.net.HttpURLConnection;

enum RequestSpecification {

    PostBytes("application/octet-stream", "*/*"),
    PostString("text/plain", "text/*"),
    PostFile("multipart/form-data", "*/*"),

    DownloadBytes("", "*/*"),
    DownloadString("", "text/*"),
    DownloadFile("", "*/*"),
    ;

    private String ContentType;
    private String Accept;

    public String getContentType() {

        return this.ContentType;

    }
    public String getAccept() {

        return this.Accept;

    }

    RequestSpecification(String contentType, String accept) {

        this.ContentType    = contentType;
        this.Accept         = accept;

    }

    public void SetHeaders(HttpURLConnection connection) {

        if (!this.getContentType().isEmpty()) {

            connection.setRequestProperty("Content-Type", this.getContentType());

        }

        connection.setRequestProperty("Accept", this.getAccept());

    }

}
