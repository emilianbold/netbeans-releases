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
public class InstallationException extends Exception {
    /**
     * 
     * @param message 
     */
    public InstallationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public InstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
