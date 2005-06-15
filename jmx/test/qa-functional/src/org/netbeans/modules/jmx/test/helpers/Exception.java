/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.helpers;

/**
 *
 * @author an156382
 */
public class Exception {
    
    private String excepClass = "";
    private String excepComment = "";
    
    public Exception(String excepClass, String excepComment) {
        
        this.excepClass = excepClass;
        this.excepComment = excepComment;
    }
    
    /**
     * Returns the class of the exception
     * @return excepClass the class of the exception
     */
    public String getExcepClass() {
        return excepClass;
    }
    
    /**
     * Returns the comment of the exception
     * @return excepComment the comment of the exception
     */
    public String getExepComment() {
        return excepComment;
    }
    
}
