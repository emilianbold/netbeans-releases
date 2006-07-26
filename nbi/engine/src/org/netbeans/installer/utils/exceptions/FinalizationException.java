/*
 * FinalizationException.java
 *
 * $Id$
 */
package org.netbeans.installer.utils.exceptions;

/**
 *
 * @author Kirill Sorokin
 */
public class FinalizationException extends Exception {
    /**
     * 
     * @param message 
     */
    public FinalizationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public FinalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
