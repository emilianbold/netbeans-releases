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

package org.netbeans.modules.j2ee.ddloaders.common.xmlutils;

import org.xml.sax.SAXParseException;

/**
 *
 * @author  mkuchtiak
 * @version 
 */
public class SAXParseError {
    
    private SAXParseException exception;

    /** Creates new XMLError */
    public SAXParseError(SAXParseException e) {
        exception=e;
    }  
    public SAXParseException getException(){return exception;}
    public int getErrorLine(){return exception.getLineNumber();}
    public int getErrorColumn(){return exception.getColumnNumber();}
    public String getErrorText(){return exception.getMessage();}
}
