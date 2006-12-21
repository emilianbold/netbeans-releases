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
package org.netbeans.modules.xml.tools.generator;

import java.util.*;

/**
 * Utility class generating XML document content holding model data.
 *
 * @author  Petr Kuzel
 * @version
 */
final class SAXBindingsGenerator {


    public static String toXML(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();

        s.append("<?xml version='1.0' encoding='UTF-8'?>\n"); // NOI18N
        s.append("<!DOCTYPE SAX-bindings PUBLIC \"-//XML Module//DTD SAX Bindings 1.0//EN\" \"\">\n"); // NOI18N
        s.append("<SAX-bindings version='1'>\n"); // NOI18N
        s.append(elementBindings(model));
        //TODO: Retouche
//        s.append(parsletBindings(model));
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
    
    //TODO: Retouche
//    private static String parsletBindings(SAXGeneratorModel model) {
        
//        StringBuffer s = new StringBuffer();
//        
//        Iterator it = model.getParsletBindings().values().iterator();
//        while (it.hasNext()) {
//            ParsletBindings.Entry next = (ParsletBindings.Entry) it.next();
//            s.append("\t<parslet parslet='" + next.getId() + "' return='" + next.getType() + "' />\n"); // NOI18N
//        }
//        
//        return s.toString();
//    }
        
}
