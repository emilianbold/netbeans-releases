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

package org.netbeans.modules.xml.api.model;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.openide.util.enum.EmptyEnumeration;
import org.xml.sax.InputSource;

/**
 *
 * @author  Petr Kuzel <petr.kuzel@sun.com>
 */
public class SampleGrammarQueryManager extends GrammarQueryManager {

    /** Creates a new instance of Test */
    public SampleGrammarQueryManager() {
    }

    /** Can this manager provide a grammar for given context?
     * @param ctx Enumeration of DOM Nodes at Document level
     *        (never <code>null</code>). Method must not
     *        invoke <code>remove</remove> at the iterator.
     * @return <code>null</code> if a grammar cannot be provided for
     *         the context else return context items (subenum of
     *         passed one) that defines grammar enableness context.
     *
     */
    public Enumeration enabled(GrammarEnvironment ctx) {
        return EmptyEnumeration.EMPTY;
    }
    
    /** @return detailed description.
     *
     */
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    /** Factory method providing a root grammar for given document.
     * @param input XML document input source that generated
     *        context passed to {@link #enable}.
     * @return GrammarQuery being able to work in the context
     *         or <code>null</null> if {@link #enabled} returns
     *         for the same context false.
     *
     */
    public GrammarQuery getGrammar(GrammarEnvironment input) {
        return new TestGrammar();
    }
    
    private static class TestGrammar implements GrammarQuery{
        
        /** @stereotype query
         * @output list of results that can be queried on name, and attributes
         * @time Performs fast up to 300 ms.
         * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
         * @return enumeration of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
         *         Every list member represents one possibility.
         *
         */
        public Enumeration queryAttributes(HintContext ctx) {
            return EmptyEnumeration.EMPTY;
        }
        
        /** @semantics Navigates through read-only Node tree to determine context and provide right results.
         * @postconditions Let ctx unchanged
         * @time Performs fast up to 300 ms.
         * @stereotype query
         * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
         * @return enumeration of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
         *         Every list member represents one possibility.
         *
         */
        public Enumeration queryElements(HintContext ctx) {
            return EmptyEnumeration.EMPTY;
        }
        
        /** Allow to get names of <b>parsed general entities</b>.
         * @return enumeration of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
         *
         */
        public Enumeration queryEntities(String prefix) {
            return EmptyEnumeration.EMPTY;
        }
        
        /** Allow to get names of <b>declared notations</b>.
         * @return enumeration of <code>GrammarResult</code>s (NOTATION_NODEs)
         *
         */
        public Enumeration queryNotations(String prefix) {
            return EmptyEnumeration.EMPTY;
        }
        
        /** Return options for value at given context.
         * It could be also used for completing of value parts such as Ant or XSLT property names (how to trigger it?).
         * @semantics Navigates through read-only Node tree to determine context and provide right results.
         * @postconditions Let ctx unchanged
         * @time Performs fast up to 300 ms.
         * @stereotype query
         * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
         * @return enumeration of <code>GrammarResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
         *         Every list member represents one possibility.
         *
         */
        public Enumeration queryValues(HintContext ctx) {
            return EmptyEnumeration.EMPTY;
        }
        
    }
    
}
