/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * PluginResourceNotFoundException.java
 *
 * Created on July 22, 2003, 4:18 PM
 */

package org.netbeans.xtest.plugin;

/**
 *
 * @author  mb115822
 */
public class PluginResourceNotFoundException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>PluginResourceNotFoundException</code> without detail message.
     */
    public PluginResourceNotFoundException() {
    }
    
    
    /**
     * Constructs an instance of <code>PluginResourceNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PluginResourceNotFoundException(String msg) {
        super(msg);
    }
    
    public PluginResourceNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }    
}
