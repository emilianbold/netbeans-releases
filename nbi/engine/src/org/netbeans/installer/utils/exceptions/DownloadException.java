/*
 * DownloadException.java
 *
 * $Id$
 */
package org.netbeans.installer.utils.exceptions;

/**
 *
 * @author Kirill Sorokin
 */
public class DownloadException extends Exception {
    /**
     * 
     * @param message 
     */
    public DownloadException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
