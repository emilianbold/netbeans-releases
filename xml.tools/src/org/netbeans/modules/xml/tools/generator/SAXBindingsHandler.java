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
package org.netbeans.modules.xml.tools.generator;

import org.xml.sax.*;

public interface SAXBindingsHandler {
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_parslet(final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_SAX_bindings(final Attributes meta) throws SAXException;
    
    /**
     * A container element end event handling method.
     */
    public void end_SAX_bindings() throws SAXException;
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_attbind(final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_bind(final Attributes meta) throws SAXException;
    
    /**
     * A container element end event handling method.
     */
    public void end_bind() throws SAXException;
    
}
