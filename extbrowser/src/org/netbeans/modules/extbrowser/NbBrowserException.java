/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

/**
 * An exception thrown from native code if something goes wrong
 *
 * @author  Radim Kubacki
 * @version 
 */
public class NbBrowserException extends java.lang.Exception {

    /**
     * Creates new <code>NbBrowserException</code> without detail message.
     */
    public NbBrowserException() {
    }


    /**
     * Constructs an <code>NbBrowserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NbBrowserException(String msg) {
        super(msg);
    }
}

