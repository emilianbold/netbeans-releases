package org.netbeans.modules.form;

public class PersistenceException extends Exception {

    private Throwable originalException;

    public PersistenceException() {
    }

    public PersistenceException(String s) {
        super(s);
    }

    public PersistenceException(Throwable t) {
        originalException = t;
    }

    public PersistenceException(Throwable t, String s) {
        super(s);
        originalException = t;
    }

    public Throwable getOriginalException() {
        return originalException;
    }
}

