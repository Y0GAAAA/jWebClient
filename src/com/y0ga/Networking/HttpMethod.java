package com.y0ga.Networking;

enum HttpMethod {

    GET("GET", false),
    POST("POST", true),
    ;

    private String MethodString = "";
    private boolean doOutput = false;

    public String getMethodString() {

        return this.MethodString;

    }
    public boolean getDoOutput() {return this.doOutput;}

    private HttpMethod(String methodString, boolean doOutput) {

        this.MethodString = methodString;

        this.doOutput = doOutput;

    }

}
