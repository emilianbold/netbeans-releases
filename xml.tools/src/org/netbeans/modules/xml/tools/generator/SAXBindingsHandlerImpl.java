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

public class SAXBindingsHandlerImpl implements SAXBindingsHandler {
    
    public static final boolean DEBUG = false;
    
    private ParsletBindings parslets = new ParsletBindings();
    private ElementBindings elements = new ElementBindings();
    
    private static final String ATT_PARSLET = "parslet"; // NOI18N
    private static final String ATT_RETURN = "return"; // NOI18N
    
    private static final String ATT_ELEMENT = "element"; // NOI18N
    private static final String ATT_TYPE = "type"; // NOI18N
    private static final String ATT_METHOD = "method"; // NOI18N
    
    public ParsletBindings getParsletBindings() {
        if (parslets.isEmpty()) return null;
        return parslets;
    }
    
    public ElementBindings getElementBindings() {
        if (elements.isEmpty()) return null;
        return elements;
    }
    
    public void handle_parslet(final Attributes meta) throws SAXException {
        if (DEBUG) Util.debug ("handle_parslet: " + meta); // NOI18N
        
        String parslet = meta.getValue(ATT_PARSLET);
        String back = meta.getValue(ATT_RETURN);
        
        parslets.put(parslet, back);
    }
    
    public void start_SAX_bindings(final Attributes meta) throws SAXException {
        if (DEBUG) Util.debug ("start_SAX_bindings: " + meta); // NOI18N
                
    }
    
    public void end_SAX_bindings() throws SAXException {
        if (DEBUG) Util.debug ("end_SAX_bindings()"); // NOI18N
    }
    
    public void start_bind(final Attributes meta) throws SAXException {
        if (DEBUG) Util.debug ("start_bind: " + meta); // NOI18N
        
        String element = meta.getValue(ATT_ELEMENT);
        String method = meta.getValue(ATT_METHOD);
        String parslet = meta.getValue(ATT_PARSLET);
        String type = meta.getValue(ATT_TYPE);
        
        elements.put(element, method, parslet, type);
    }
    
    public void end_bind() throws SAXException {
        if (DEBUG) Util.debug ("end_bind()"); // NOI18N
    }
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_attbind(Attributes meta) throws SAXException {
    }
    
}
