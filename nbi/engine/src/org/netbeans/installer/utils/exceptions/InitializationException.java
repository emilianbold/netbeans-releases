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
public class InitializationException extends Exception {
    /**
     * 
     * @param message 
     */
    public InitializationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
