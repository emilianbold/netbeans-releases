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

import java.util.*;

/**
 *
 * @author  pkuzel
 * @version 
 */
class SAXBindingsGenerator {

    /** Creates new SAXBindingsGenerator */
    public SAXBindingsGenerator() {
    }

    public static String toXML(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();
        
        s.append("<?xml version='1.0' encoding='UTF-8'?>\n"); // NOI18N
        s.append("<!DOCTYPE SAX-bindings PUBLIC \"-//XML Module//DTD SAX Bindings 1.0//EN\" \"\">\n"); // NOI18N
        s.append("<SAX-bindings version='1'>\n"); // NOI18N
        s.append(elementBindings(model));
        s.append(parsletBindings(model));
        s.append("</SAX-bindings>"); // NOI18N
        
        return s.toString();
    }
    
    private static String elementBindings(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();
        
        Iterator it = model.getElementBindings().values().iterator();
        while (it.hasNext()) {
            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
            s.append("\t<bind element='" + next.getElement() + "' method='" + next.getMethod() + "' "); // NOI18N
            s.append("type='" + next.getType() + "' "); // NOI18N
            if (next.getParslet() != null) {
                s.append("parslet='" + next.getParslet() + "' "); // NOI18N
            }
            s.append("></bind>\n"); // NOI18N
        }
        return s.toString();
    }
    
    private static String parsletBindings(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();
        
        Iterator it = model.getParsletBindings().values().iterator();
        while (it.hasNext()) {
            ParsletBindings.Entry next = (ParsletBindings.Entry) it.next();
            s.append("\t<parslet parslet='" + next.getId() + "' return='" + next.getType() + "' />\n"); // NOI18N
        }
        
        return s.toString();
    }
}
