/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
