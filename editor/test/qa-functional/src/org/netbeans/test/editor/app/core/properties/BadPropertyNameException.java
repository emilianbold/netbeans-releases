/*
 * BadPropertyNameException.java
 *
 * Created on November 18, 2002, 10:46 AM
 */

package org.netbeans.test.editor.app.core.properties;

/**
 *
 * @author  eh103527
 */
public class BadPropertyNameException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>BadPropertyNameException</code> without detail message.
     */
    public BadPropertyNameException() {
    }
    
    
    /**
     * Constructs an instance of <code>BadPropertyNameException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BadPropertyNameException(String msg) {
        super(msg);
    }
}
