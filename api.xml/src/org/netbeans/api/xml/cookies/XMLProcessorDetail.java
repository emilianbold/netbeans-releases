/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.xml.cookies;

// for JavaDoc
import org.xml.sax.*;

/**
 * It is structured XML processor {@link CookieMessage} detail.
 * Its specification is based on {@link SAXParseException}, but can be
 * explicitly overwriten here.
 *
 * @author  Petr Kuzel
 * @since   0.5
 */
public abstract class XMLProcessorDetail {

    public abstract int getColumnNumber();

    public abstract int getLineNumber();

    public abstract String getPublicId();

    public abstract String getSystemId();

    public abstract Exception getException();
    
}
