/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.util;

/**
 * UnsupportedLoggerException
 * @author  Stepan Herold
 */
public class UnsupportedLoggerException extends Exception {
    private String loggerClassName = null;
    
    /**
     * Creates new UnsupportedLoggerException
     * @param loggerClassName logger class name
     */
    public UnsupportedLoggerException(String loggerClassName) {
        super();
        this.loggerClassName = loggerClassName;        
    }
    
    /**
     * Returns logger class name
     * @return logger class name
     */
    public String getLoggerClassName() {
        return loggerClassName;
    }
}
