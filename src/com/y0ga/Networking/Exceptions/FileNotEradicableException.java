package com.y0ga.Networking.Exceptions;

import java.io.File;

public class FileNotEradicableException extends Exception {

    public static void ThrowFromFile(File file) throws FileNotEradicableException {

        String message = "File at \"" + file.getAbsolutePath() + "\" cannot be deleted within the current application context.";

        throw new FileNotEradicableException(message);

    }

    public FileNotEradicableException(String message) {

        super(message);

    }
}
