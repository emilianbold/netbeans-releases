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

import org.openide.util.enum.EmptyEnumeration;

import org.netbeans.modules.xml.spi.model.*;
import org.netbeans.modules.xml.text.syntax.dom.*;

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
        return EmptyEnumeration.EMPTY; 
    }
    
    public Enumeration queryAttributes(HintContext ctx) {
        return EmptyEnumeration.EMPTY; 
    }
    
    public Enumeration queryElements(HintContext ctx) {
        return EmptyEnumeration.EMPTY; 
    }
    
    public Enumeration queryNotations(String prefix) {
        return EmptyEnumeration.EMPTY; 
    }
    
    public Enumeration queryValues(HintContext ctx) {
        return EmptyEnumeration.EMPTY; 
    }
    
}
