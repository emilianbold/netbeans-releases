/*
 * InitializationException.java
 *
 * $Id$
 */
package org.netbeans.installer.utils.exceptions;

/**
 *
 * @author ks152834
 */
public class UninstallationException extends Exception {
    /**
     * 
     * @param message 
     */
    public UninstallationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public UninstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
