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

import org.w3c.dom.*;

import org.netbeans.modules.xml.spi.model.*;
import org.netbeans.modules.xml.text.syntax.dom.*;

/**
 * This query always return an empty result.
 *
 * @author  Petr Kuzel
 */
public class EmptyQuery implements GrammarQuery {

    /**
     * Shared instance.
     */
    public static final GrammarQuery INSTANCE = new EmptyQuery();

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return list of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public NodeList queryEntities(String prefix) {
        return NodeListImpl.EMPTY; 
    }
    
    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms.
     * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
     * @return list of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public NodeList queryAttributes(HintContext ctx) {
        return NodeListImpl.EMPTY;
    }
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
     *        Every list member represents one possibility.
     */
    public NodeList queryElements(HintContext ctx) {
        return NodeListImpl.EMPTY;
    }
    
    /**
     * Allow to get names of <b>declared notations</b>.
     * @return list of <code>GrammarResult</code>s (NOTATION_NODEs)
     */
    public NodeList queryNotations(String prefix) {
        return NodeListImpl.EMPTY;
    }
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>GrammarResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public NodeList queryValues(HintContext ctx) {
        return NodeListImpl.EMPTY;
    }
    
}
