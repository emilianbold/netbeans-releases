/*
 * HTTPException.java
 */
package org.netbeans.installer.utils.exceptions;

import java.io.IOException;

/**
 *
 * @author ks152834
 */
public class HTTPException extends IOException {
    /**
     * 
     * @param message 
     */
    public HTTPException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 
     * @param cause 
     */
    public HTTPException(String message, Throwable cause) {
        super(message);
        
        initCause(cause);
    }
}
