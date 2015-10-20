package com.universalinstaller;

public class IFSUnpackerException
extends Exception {
    private final Throwable cause;
    private static final long serialVersionUID = 1;

    public IFSUnpackerException(String message) {
        super(message);
        this.cause = null;
    }

    public IFSUnpackerException(String message, Throwable e) {
        super(String.valueOf(message) + ". Caused by " + e + ")");
        this.cause = e;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}