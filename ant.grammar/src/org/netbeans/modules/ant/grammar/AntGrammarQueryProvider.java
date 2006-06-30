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

package org.netbeans.modules.ant.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.openide.util.Enumerations;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Provides the Ant grammar for any documents whose root elements matches a standard pattern.
 * See also ant/src/.../resources/mime-resolver.xml.
 * @author Petr Kuzel, Jesse Glick
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
                if ("project".equals(root.getNodeName()) && (root.getAttributeNode("default") != null || root.getAttributeNode("basedir") != null)) { // NOI18N
                    return Enumerations.singleton (next);
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
