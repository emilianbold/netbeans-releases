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
 * PluginConfigurationException.java
 * Exception when a plugin has a bad configuration
 *
 * Created on July 21, 2003, 6:07 PM
 */

package org.netbeans.xtest.plugin;

/**
 *
 * @author  mb115822
 */
public class PluginConfigurationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>PluginConfigurationException</code> without detail message.
     */
    public PluginConfigurationException() {
    }
    
    
    /**
     * Constructs an instance of <code>PluginConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PluginConfigurationException(String msg) {
        super(msg);
    }
    
    public PluginConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }    
    
}
