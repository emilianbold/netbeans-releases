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

package org.netbeans.modules.ant.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.*;
import org.openide.util.enum.SingletonEnumeration;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Provide DTD grammar. It must be registered at layer.
 *
 * @author  Petr Kuzel <petr.kuzel@sun.com>
 */
public class AntGrammarQueryProvider extends GrammarQueryManager {
    
    public Enumeration enabled(GrammarEnvironment ctx) {
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            System.err.println("Ant queried for " + next);
            if (next.getNodeType() == next.ELEMENT_NODE) {
                Element root = (Element) next;                
                if ("project".equals(root.getNodeName()) && root.getAttributeNode("name") != null) { // NOI18N
                    System.err.println("Hit!");
                    return new SingletonEnumeration(next);
                }
            }
        }
        return null;
    }
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        return new AntGrammar();
    }
    
}
