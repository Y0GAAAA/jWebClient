package com.y0ga.Networking.Exceptions;

import java.io.File;

public class FileNotEradicableException extends Exception {

    //not sure about that way to do it but it works...
    public static void ThrowFromFile(File file) throws FileNotEradicableException {

        String message = "File at \"" + file.getAbsolutePath() + "\" cannot be deleted within the current application context.";

        throw new FileNotEradicableException(message);

    }

    public FileNotEradicableException(String message) {

        super(message);

    }
}
