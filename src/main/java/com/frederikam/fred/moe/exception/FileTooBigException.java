package com.frederikam.fred.moe.exception;

public class FileTooBigException extends RuntimeException {

    public FileTooBigException() {
    }

    public FileTooBigException(String message) {
        super(message);
    }

    public FileTooBigException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileTooBigException(Throwable cause) {
        super(cause);
    }
}
