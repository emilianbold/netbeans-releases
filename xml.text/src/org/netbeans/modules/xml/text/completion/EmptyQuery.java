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

package org.netbeans.modules.xml.text.completion;

import java.util.Enumeration;

import org.w3c.dom.*;

import org.netbeans.modules.xml.api.model.*;

/**
 * This query always returns an empty result from all its query methods.
 *
 * @author  Petr Kuzel
 */
public class EmptyQuery implements GrammarQuery {

    /**
     * Shared instance.
     */
    public static final GrammarQuery INSTANCE = new EmptyQuery();

    // inherit JavaDoc from interface description
    
    public Enumeration queryEntities(String prefix) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryAttributes(HintContext ctx) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryElements(HintContext ctx) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryNotations(String prefix) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryValues(HintContext ctx) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public boolean isAllowed(Enumeration en) {
        return false;
    }
    
    public GrammarResult queryDefault(HintContext virtualNodeCtx) {
        return null;
    }
    
    public java.awt.Component getCustomizer(HintContext ctx) {
        return null;
    }
    
    public boolean hasCustomizer(HintContext ctx) {
        return false;
    }

    public org.openide.nodes.Node.Property[] getProperties(HintContext ctx) {
        return null;
    }
    
    
}
