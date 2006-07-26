/*
 * IgnoreAttributeException.java
 *
 */

package org.netbeans.installer.utils.exceptions;

/**
 *
 * @author Dmitry Lipin
 */
public class IgnoreAttributeException extends Exception {
     /** Creates a new instance of IgnoreAttributeException    
     *
     * @param message
     */    
    public IgnoreAttributeException(String message) {
        super(message);
    }
    
    /**
     *
     * @param message
     * @param cause
     */
    public IgnoreAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}