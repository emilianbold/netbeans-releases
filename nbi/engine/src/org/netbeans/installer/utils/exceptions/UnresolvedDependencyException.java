/*
 * UnresolvedDependencyException.java
 *
 * $Id$
 */
package org.netbeans.installer.utils.exceptions;

/**
 *
 * @author Kirill Sorokin
 */
public class UnresolvedDependencyException extends Exception {
    /**
     * 
     * @param message 
     */
    public UnresolvedDependencyException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public UnresolvedDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
