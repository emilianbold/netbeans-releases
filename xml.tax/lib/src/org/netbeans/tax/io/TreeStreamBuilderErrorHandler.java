/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.io;

/**
 * All errors spotted during building a Tree should be reported
 * by this interface.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface TreeStreamBuilderErrorHandler {
    
    // message types
    public static final int ERROR_WARNING     = 0;
    public static final int ERROR_ERROR       = 1;
    public static final int ERROR_FATAL_ERROR = 2;
    
    public static final String [] ERROR_NAME  = new String [] {
        "Warning",      // NOI18N
        "Error",        // NOI18N
        "Fatal error"   // NOI18N
    };
    
    public void message (int type, org.xml.sax.SAXParseException e);
    
}
