/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.*;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Provide Ant grammar. It must be consistently registered at layer.
 *
 * @author  Petr Kuzel
 */
public final class AntGrammarQueryProvider extends GrammarQueryManager {
    
    public Enumeration enabled(GrammarEnvironment ctx) {
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.ELEMENT_NODE) {
                Element root = (Element) next;                
                // XXX should also check for any root <project> in NS "antlib:org.apache.tools.ant"
                // (but no NS support in HintContext impl anyway: #38340)
                if ("project".equals(root.getNodeName()) && root.getAttributeNode("default") != null) { // NOI18N
                    return org.openide.util.Enumerations.singleton (next);
                }
            }
        }
        return null;
    }
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        // XXX pick up env.fileObject too
        return new AntGrammar();
    }
    
}
